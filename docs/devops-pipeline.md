# Deep Reps — DevOps & Infrastructure Blueprint

**Status:** Pre-implementation architecture
**Last Updated:** 2026-02-11
**Owner:** DevOps / Platform Engineer

---

## Table of Contents

1. [CI/CD Pipeline Design](#1-cicd-pipeline-design)
2. [Build Configuration](#2-build-configuration)
3. [Environment Management](#3-environment-management)
4. [Play Store Deployment](#4-play-store-deployment)
5. [Monitoring & Observability](#5-monitoring--observability)
6. [Infrastructure](#6-infrastructure)
7. [Security](#7-security)
8. [Disaster Recovery](#8-disaster-recovery)
9. [Cost Management](#9-cost-management)

---

## 1. CI/CD Pipeline Design

### 1.1 Pipeline Philosophy

**Speed is non-negotiable.** A slow CI pipeline kills developer velocity. Every pipeline stage must be optimized for execution time through aggressive caching and parallelization. Target: PR pipeline completes in under 5 minutes, full release pipeline under 15 minutes.

**Fail fast.** Lint and static analysis run first. If code doesn't pass style checks, don't waste compute running tests.

**Reproducible builds.** Every build must be reproducible from commit SHA alone. No "works on my machine" issues.

### 1.2 Branch Strategy

```
main (production)
  └─ Protected branch
  └─ Deploys to Play Store production track
  └─ Requires PR approval + passing CI
  └─ Never commit directly

develop (integration)
  └─ Integration branch for completed features
  └─ Deploys to internal testing track
  └─ Nightly builds with full test suite

feature/* (work branches)
  └─ All new work happens here
  └─ PR merges into develop
  └─ Runs PR pipeline on every push
```

**No long-lived feature branches.** Features exceeding 3 days of work must be broken down or feature-flagged. Merge to develop frequently.

### 1.3 PR Pipeline (feature/* → develop)

Runs on every push to a PR branch. Optimized for speed and early failure detection.

```yaml
# .github/workflows/pr-check.yml
name: PR Check

on:
  pull_request:
    branches: [develop]

jobs:
  lint:
    runs-on: ubuntu-latest
    timeout-minutes: 5
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Restore Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: ${{ runner.os }}-gradle-
      - name: Ktlint check
        run: ./gradlew ktlintCheck --no-daemon
      - name: Detekt static analysis
        run: ./gradlew detekt --no-daemon

  unit-tests:
    runs-on: ubuntu-latest
    timeout-minutes: 10
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Restore Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
      - name: Run unit tests
        run: ./gradlew testDebugUnitTest --no-daemon
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport --no-daemon
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: ./build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
          fail_ci_if_error: true
          flags: unittests

  build-debug:
    runs-on: ubuntu-latest
    needs: [lint, unit-tests]
    timeout-minutes: 10
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Restore Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
            build-cache
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
      - name: Build debug APK
        run: ./gradlew assembleDebug --build-cache --no-daemon
      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7

  instrumentation-tests:
    runs-on: ubuntu-latest
    needs: build-debug
    timeout-minutes: 20
    strategy:
      matrix:
        api-level: [28, 33] # Min SDK and latest stable
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Enable KVM (for faster emulator)
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm
      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      - name: AVD cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.android/avd/*
            ~/.android/adb*
          key: avd-${{ matrix.api-level }}
      - name: Run instrumentation tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          target: google_apis
          arch: x86_64
          profile: pixel_6
          disable-animations: true
          disk-size: 2048M
          heap-size: 1024M
          script: ./gradlew connectedDebugAndroidTest --no-daemon
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: instrumentation-test-results-${{ matrix.api-level }}
          path: app/build/reports/androidTests/
          retention-days: 14
```

**Key optimizations:**

- Gradle dependency caching (90% cache hit rate in practice)
- Gradle build cache enabled (incremental compilation)
- AVD caching for emulator tests (saves 3-5 minutes per run)
- Parallel job execution (lint, unit tests run simultaneously)
- KVM acceleration for emulator (2-3x faster than software emulation)
- Matrix testing across min SDK and latest SDK only (not all versions — too slow)

**Failure modes:**

- Ktlint fail → Fix code style before proceeding
- Unit test fail → Fix before merge
- Coverage drops below 80% → Fail CI (enforced via Codecov threshold)
- Instrumentation test fail → Fix before merge (no flaky test tolerance)

### 1.4 Merge Pipeline (develop → main or merge to develop)

Runs on successful merge to `develop`. Full test suite + staging deployment.

```yaml
# .github/workflows/develop-merge.yml
name: Develop Merge

on:
  push:
    branches: [develop]

jobs:
  full-test-suite:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      - name: Run all tests
        run: ./gradlew test connectedDebugAndroidTest --parallel --build-cache --no-daemon
      - name: Generate full coverage report
        run: ./gradlew jacocoTestReport --no-daemon
      - name: Enforce 80% coverage threshold
        run: |
          COVERAGE=$(grep -oP 'Total.*?(\d+)%' build/reports/jacoco/jacocoTestReport/html/index.html | grep -oP '\d+')
          if [ "$COVERAGE" -lt 80 ]; then
            echo "Coverage is $COVERAGE%, below 80% threshold"
            exit 1
          fi

  build-staging:
    runs-on: ubuntu-latest
    needs: full-test-suite
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
            build-cache
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      - name: Decode keystore
        run: |
          echo "${{ secrets.STAGING_KEYSTORE_BASE64 }}" | base64 -d > app/staging.keystore
      - name: Build staging bundle
        run: ./gradlew bundleStaging --no-daemon
        env:
          KEYSTORE_PASSWORD: ${{ secrets.STAGING_KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.STAGING_KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.STAGING_KEY_PASSWORD }}
      - name: Upload to Play Store internal track
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
          packageName: com.deepreps.app
          releaseFiles: app/build/outputs/bundle/staging/app-staging.aab
          track: internal
          status: completed

  dependency-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Snyk vulnerability scan
        uses: snyk/actions/gradle@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=high --fail-on=all
```

**Post-merge actions:**

- Full test suite (unit + instrumentation across all modules)
- Coverage enforcement (fail if <80%)
- Dependency vulnerability scan (fail on high/critical vulnerabilities)
- Build and deploy to Play Store internal track (for QA testing)

### 1.5 Release Pipeline (tag → production)

Triggered by creating a release tag (e.g., `v1.0.0`). Builds signed production bundle and deploys to Play Store with staged rollout.

```yaml
# .github/workflows/release.yml
name: Production Release

on:
  push:
    tags:
      - 'v*.*.*'

jobs:
  build-release:
    runs-on: ubuntu-latest
    timeout-minutes: 20
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Gradle cache
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
            build-cache
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
      - name: Extract version from tag
        id: get_version
        run: echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_OUTPUT
      - name: Decode upload keystore
        run: |
          echo "${{ secrets.UPLOAD_KEYSTORE_BASE64 }}" | base64 -d > app/upload.keystore
      - name: Build release bundle
        run: ./gradlew bundleRelease --no-daemon
        env:
          KEYSTORE_PASSWORD: ${{ secrets.UPLOAD_KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.UPLOAD_KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.UPLOAD_KEY_PASSWORD }}
      - name: Sign bundle
        run: |
          jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
            -keystore app/upload.keystore \
            -storepass "${{ secrets.UPLOAD_KEYSTORE_PASSWORD }}" \
            app/build/outputs/bundle/release/app-release.aab \
            "${{ secrets.UPLOAD_KEY_ALIAS }}"
      - name: Generate release notes
        id: release_notes
        run: |
          # Extract commits since last tag
          PREVIOUS_TAG=$(git describe --tags --abbrev=0 HEAD^ 2>/dev/null || echo "")
          if [ -z "$PREVIOUS_TAG" ]; then
            NOTES=$(git log --pretty=format:"- %s" HEAD)
          else
            NOTES=$(git log --pretty=format:"- %s" $PREVIOUS_TAG..HEAD)
          fi
          echo "NOTES<<EOF" >> $GITHUB_OUTPUT
          echo "$NOTES" >> $GITHUB_OUTPUT
          echo "EOF" >> $GITHUB_OUTPUT
      - name: Create GitHub release
        uses: actions/create-release@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ steps.get_version.outputs.VERSION }}
          body: ${{ steps.release_notes.outputs.NOTES }}
          draft: false
          prerelease: false
      - name: Upload to Play Store (closed testing track initially)
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
          packageName: com.deepreps.app
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: beta
          status: completed
          releaseNotes: |
            ${{ steps.release_notes.outputs.NOTES }}
          inAppUpdatePriority: 2
      - name: Notify team (Slack)
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "Production release ${{ steps.get_version.outputs.VERSION }} deployed to closed testing track",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*Deep Reps ${{ steps.get_version.outputs.VERSION }}* deployed to Play Store closed testing track.\n\nRelease notes:\n${{ steps.release_notes.outputs.NOTES }}"
                  }
                }
              ]
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

**Manual promotion to production:**

After monitoring crash rates in closed testing for 24-48 hours, manually promote to production with staged rollout via Play Console or using a separate workflow:

```yaml
# Manual workflow dispatch for production rollout
# .github/workflows/promote-to-production.yml
name: Promote to Production

on:
  workflow_dispatch:
    inputs:
      rollout_percentage:
        description: 'Rollout percentage (1, 5, 20, 50, 100)'
        required: true
        default: '1'

jobs:
  promote:
    runs-on: ubuntu-latest
    steps:
      - name: Promote to production
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
          packageName: com.deepreps.app
          track: beta
          promoteTrack: production
          userFraction: ${{ github.event.inputs.rollout_percentage }}
          status: inProgress
```

### 1.6 Pipeline Optimization Targets

| Stage | Target Time | Optimization Strategy |
|-------|-------------|----------------------|
| Lint | <1 min | Ktlint + Detekt with parallel execution |
| Unit Tests | <3 min | Parallel test execution, Gradle test caching |
| Build (debug) | <2 min | Gradle build cache, configuration cache, dependency caching |
| Instrumentation Tests | <10 min | AVD caching, KVM acceleration, matrix only min/max SDK |
| Full Release Build | <5 min | All caching enabled, --build-cache, --parallel |

**Total PR pipeline:** <5 minutes
**Total release pipeline:** <15 minutes

**If these targets are not met within 2 weeks of pipeline setup, investigate:**

- Gradle daemon not being reused (add `--daemon`)
- Cache miss rate >10% (verify cache keys)
- Emulator startup time >90s (KVM not enabled)
- Dependency resolution >30s (Gradle dependency locking not used)

---

## 2. Build Configuration

### 2.1 Build Variants

Three build types:

```gradle
// app/build.gradle.kts
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
            isMinifyEnabled = false

            // Debug-specific config
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.deepreps.com\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"${System.getenv("GEMINI_DEV_API_KEY")}\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_STRICT_MODE", "true")
        }

        staging {
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("staging")

            buildConfigField("String", "API_BASE_URL", "\"https://staging-api.deepreps.com\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"${System.getenv("GEMINI_STAGING_API_KEY")}\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_STRICT_MODE", "false")
        }

        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")

            buildConfigField("String", "API_BASE_URL", "\"https://api.deepreps.com\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"${System.getenv("GEMINI_PROD_API_KEY")}\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("boolean", "ENABLE_STRICT_MODE", "false")
        }
    }
}
```

### 2.2 Product Flavors (Future: Free vs Premium)

If a freemium model is chosen, use product flavors:

```gradle
productFlavors {
    create("free") {
        dimension = "tier"
        applicationIdSuffix = ".free"

        buildConfigField("boolean", "IS_PREMIUM", "false")
        buildConfigField("int", "MAX_TEMPLATES", "3")
        buildConfigField("boolean", "AI_PLAN_GENERATION_ENABLED", "false")
    }

    create("premium") {
        dimension = "tier"

        buildConfigField("boolean", "IS_PREMIUM", "true")
        buildConfigField("int", "MAX_TEMPLATES", "999")
        buildConfigField("boolean", "AI_PLAN_GENERATION_ENABLED", "true")
    }
}
```

This generates builds like `freeDebug`, `premiumRelease`, etc. Deferred until monetization model is decided.

### 2.3 Signing Configuration

**Never commit keystores or passwords to version control.** Use environment variables in CI and local `keystore.properties` file (gitignored) for local builds.

```gradle
// app/build.gradle.kts
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("staging") {
            storeFile = file(keystoreProperties["stagingStoreFile"] ?: System.getenv("STAGING_KEYSTORE_PATH") ?: "staging.keystore")
            storePassword = keystoreProperties["stagingStorePassword"] as String? ?: System.getenv("STAGING_KEYSTORE_PASSWORD")
            keyAlias = keystoreProperties["stagingKeyAlias"] as String? ?: System.getenv("STAGING_KEY_ALIAS")
            keyPassword = keystoreProperties["stagingKeyPassword"] as String? ?: System.getenv("STAGING_KEY_PASSWORD")
        }

        create("release") {
            storeFile = file(keystoreProperties["uploadStoreFile"] ?: System.getenv("UPLOAD_KEYSTORE_PATH") ?: "upload.keystore")
            storePassword = keystoreProperties["uploadStorePassword"] as String? ?: System.getenv("UPLOAD_KEYSTORE_PASSWORD")
            keyAlias = keystoreProperties["uploadKeyAlias"] as String? ?: System.getenv("UPLOAD_KEY_ALIAS")
            keyPassword = keystoreProperties["uploadKeyPassword"] as String? ?: System.getenv("UPLOAD_KEY_PASSWORD")
        }
    }
}
```

**keystore.properties (gitignored, local development only):**

```properties
stagingStoreFile=../keystores/staging.keystore
stagingStorePassword=REDACTED
stagingKeyAlias=staging
stagingKeyPassword=REDACTED

uploadStoreFile=../keystores/upload.keystore
uploadStorePassword=REDACTED
uploadKeyAlias=upload
uploadKeyPassword=REDACTED
```

**CI environment uses GitHub Secrets:**

- `STAGING_KEYSTORE_BASE64` (base64-encoded keystore file)
- `STAGING_KEYSTORE_PASSWORD`
- `STAGING_KEY_ALIAS`
- `STAGING_KEY_PASSWORD`
- `UPLOAD_KEYSTORE_BASE64`
- `UPLOAD_KEYSTORE_PASSWORD`
- `UPLOAD_KEY_ALIAS`
- `UPLOAD_KEY_PASSWORD`

### 2.4 Build Time Optimization

Android builds are slow. Target: clean build under 90 seconds, incremental build under 10 seconds.

**gradle.properties (project root):**

```properties
# Gradle daemon
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true

# JVM heap size
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError

# Kotlin compiler options
kotlin.incremental=true
kotlin.incremental.android=true
kotlin.caching.enabled=true

# Android build optimizations
android.enableJetifier=false
android.useAndroidX=true
android.nonTransitiveRClass=true
android.defaults.buildfeatures.buildconfig=true
android.defaults.buildfeatures.aidl=false
android.defaults.buildfeatures.renderscript=false
android.defaults.buildfeatures.resvalues=false
android.defaults.buildfeatures.shaders=false

# R8 optimizations
android.enableR8.fullMode=true
```

**settings.gradle.kts (enable configuration cache):**

```kotlin
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 7
    }
}
```

**Dependency locking (prevents unexpected dependency updates mid-build):**

```gradle
// app/build.gradle.kts
dependencyLocking {
    lockAllConfigurations()
}
```

Run `./gradlew dependencies --write-locks` to generate lock files. Commit these to version control.

### 2.5 Dependency Vulnerability Scanning

**Dependabot (GitHub native):**

`.github/dependabot.yml`:

```yaml
version: 2
updates:
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
    reviewers:
      - "lead-android-dev"
    assignees:
      - "lead-android-dev"
    labels:
      - "dependencies"
    commit-message:
      prefix: "chore"
      include: "scope"
```

**Snyk (deeper vulnerability detection):**

Integrated in CI pipeline (see 1.4). Runs on every merge to `develop`. Fails build on high/critical vulnerabilities.

**Manual scan command (run weekly):**

```bash
./gradlew snyk-test
```

---

## 3. Environment Management

### 3.1 Environment Definitions

| Environment | Purpose | Backend URL | Data Isolation | Deployment Target |
|-------------|---------|-------------|----------------|------------------|
| **Development** | Local dev work | `localhost:8080` or dev backend | Local SQLite + Firebase emulator | Developer machines |
| **Staging** | QA and beta testing | `staging-api.deepreps.com` | Staging Firebase project | Play Store internal track |
| **Production** | Live users | `api.deepreps.com` | Production Firebase project | Play Store production track |

### 3.2 Environment-Specific Configuration

All environment config is injected via `BuildConfig` at compile time (see 2.1). No runtime environment detection.

**Example usage in code:**

```kotlin
// data/network/GeminiApiClient.kt
class GeminiApiClient(
    private val httpClient: HttpClient
) {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val baseUrl = BuildConfig.API_BASE_URL

    suspend fun generatePlan(request: PlanRequest): PlanResponse {
        // ...
    }
}
```

### 3.3 Feature Flags (Optional, Recommended)

Use Firebase Remote Config for runtime feature toggling without redeploying:

```kotlin
// domain/config/FeatureFlags.kt
object FeatureFlags {
    var enableSupersets: Boolean = false
    var enableWearOsSync: Boolean = false
    var maxTemplatesForFreeUsers: Int = 3

    suspend fun refresh() {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.fetchAndActivate().await()

        enableSupersets = remoteConfig.getBoolean("enable_supersets")
        enableWearOsSync = remoteConfig.getBoolean("enable_wear_os_sync")
        maxTemplatesForFreeUsers = remoteConfig.getLong("max_templates_free_users").toInt()
    }
}
```

Benefits:

- Kill switch for buggy features without redeploying
- A/B test feature rollout
- Gradual feature rollout (enable for 10% of users, monitor, expand to 100%)

### 3.4 Secret Management

**GitHub Secrets (CI/CD):**

All secrets stored in GitHub repository settings → Secrets and variables → Actions.

Required secrets:

- `GEMINI_DEV_API_KEY`
- `GEMINI_STAGING_API_KEY`
- `GEMINI_PROD_API_KEY`
- `STAGING_KEYSTORE_BASE64`
- `STAGING_KEYSTORE_PASSWORD`
- `STAGING_KEY_ALIAS`
- `STAGING_KEY_PASSWORD`
- `UPLOAD_KEYSTORE_BASE64`
- `UPLOAD_KEYSTORE_PASSWORD`
- `UPLOAD_KEY_ALIAS`
- `UPLOAD_KEY_PASSWORD`
- `PLAY_SERVICE_ACCOUNT_JSON`
- `SLACK_WEBHOOK_URL`
- `SNYK_TOKEN`

**Google Secret Manager (Backend, if needed):**

If backend services are deployed (Cloud Run, Cloud Functions), use Secret Manager:

```bash
# Store Gemini API key
gcloud secrets create gemini-api-key --data-file=- <<< "$GEMINI_API_KEY"

# Grant Cloud Run service account access
gcloud secrets add-iam-policy-binding gemini-api-key \
  --member="serviceAccount:backend@deepreps-prod.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

Access in Cloud Run:

```yaml
# cloud-run-service.yaml
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: deepreps-api
spec:
  template:
    spec:
      containers:
      - image: gcr.io/deepreps-prod/api:latest
        env:
        - name: GEMINI_API_KEY
          valueFrom:
            secretKeyRef:
              name: gemini-api-key
              key: latest
```

### 3.5 Local Development Setup

**Reproducible environment setup (documented in `docs/setup.md`):**

1. Install Android Studio (latest stable)
2. Install Java 17 (Temurin distribution)
3. Clone repository
4. Copy `keystore.properties.template` to `keystore.properties` and fill in staging keystore details (shared via 1Password or similar secure method)
5. Create `local.properties` with `sdk.dir=/path/to/android-sdk`
6. Run `./gradlew assembleDebug` to verify setup
7. (Optional) Install Firebase CLI and run `firebase emulators:start` for local backend

**Firebase Emulator setup (optional for offline dev):**

```json
// firebase.json
{
  "emulators": {
    "auth": {
      "port": 9099
    },
    "firestore": {
      "port": 8080
    },
    "functions": {
      "port": 5001
    },
    "storage": {
      "port": 9199
    },
    "ui": {
      "enabled": true,
      "port": 4000
    }
  }
}
```

Connect app to emulator in debug builds:

```kotlin
// App initialization (debug builds only)
if (BuildConfig.DEBUG) {
    Firebase.firestore.useEmulator("10.0.2.2", 8080) // 10.0.2.2 = localhost from Android emulator
    Firebase.auth.useEmulator("10.0.2.2", 9099)
}
```

---

## 4. Play Store Deployment

### 4.1 App Signing Strategy

**Use Play App Signing.** Google manages the app signing key, you manage the upload key. Benefits:

- Google stores the signing key securely (you can't lose it)
- Upload key can be reset if compromised
- Enables smaller download sizes via split APKs

**Setup (one-time):**

1. Generate upload key:

```bash
keytool -genkey -v -keystore upload.keystore -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

2. Enroll in Play App Signing (Play Console → Setup → App integrity → Use Play App Signing)
3. Upload the upload key certificate to Play Console
4. Base64-encode the keystore for GitHub Secrets:

```bash
base64 upload.keystore > upload.keystore.base64
cat upload.keystore.base64 | pbcopy # Paste into GitHub Secrets
```

### 4.2 Release Tracks

| Track | Purpose | Audience | Update Strategy |
|-------|---------|----------|----------------|
| **Internal** | CI builds from `develop` branch | Team only (QA, devs, PM) | Auto-deployed on every merge |
| **Closed Testing** | Beta releases | Opted-in beta testers (100-500 users) | Manual promotion from internal after QA pass |
| **Open Testing** | Public beta | Anyone who opts in | Manual promotion from closed after 48h of monitoring |
| **Production** | Live release | All users | Staged rollout (see 4.3) |

### 4.3 Staged Rollout Strategy

Never go 0→100% in production. Crash rate variability across devices/OS versions is high.

**Rollout schedule:**

| Stage | Percentage | Duration | Promotion Criteria |
|-------|-----------|----------|-------------------|
| Closed beta | 100% of beta users | 48 hours | Crash rate <0.5%, no P0 bugs reported |
| Production (1%) | 1% of production users | 24 hours | Crash rate <1%, ANR rate <0.3% |
| Production (5%) | 5% | 24 hours | Crash rate <0.8%, no spike in negative reviews |
| Production (20%) | 20% | 48 hours | Crash rate <0.7%, metrics stable |
| Production (50%) | 50% | 48 hours | Crash rate <0.6%, performance metrics stable |
| Production (100%) | 100% | - | Metrics stable across all stages |

**Halt rollout if:**

- Crash rate spikes >1.5% at any stage
- ANR rate >0.5%
- 1-star reviews spike >10% of new reviews
- Critical bug (data loss, security issue) reported

**Rollback procedure:**

1. Halt rollout immediately (Play Console → Release → Halt rollout)
2. Fix the issue in a hotfix branch
3. Deploy hotfix to internal → closed → production (restart staged rollout)
4. Post-mortem: document root cause and prevention measures

### 4.4 Version Naming Convention

**Semantic versioning + build number:**

- **Version name:** `MAJOR.MINOR.PATCH` (e.g., `1.2.3`)
- **Version code:** Integer, incremented on every build (e.g., `10203`)

```gradle
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 10000 // Increment per build
        versionName = "1.0.0"
    }
}
```

**Versioning rules:**

- **MAJOR:** Breaking changes (e.g., complete UI redesign, data migration)
- **MINOR:** New features (e.g., superset support, Wear OS sync)
- **PATCH:** Bug fixes, performance improvements

**Automated version bumping (optional):**

Use a script or Gradle task to auto-increment version code based on commit count or CI build number:

```gradle
// app/build.gradle.kts
val gitCommitCount = "git rev-list --count HEAD".execute()

android {
    defaultConfig {
        versionCode = 10000 + gitCommitCount.toInt()
        versionName = "1.0.0" // Update manually on releases
    }
}

fun String.execute(): String {
    val process = Runtime.getRuntime().exec(this)
    process.waitFor()
    return process.inputStream.bufferedReader().readText().trim()
}
```

### 4.5 Release Notes Automation

Generate release notes from git commit messages (see 1.5). Use conventional commits for structured notes:

```
feat: add superset support
fix: resolve crash on exercise reordering
perf: optimize database query in progress view
```

**Categorize commits automatically:**

```bash
# Extract features
git log v1.0.0..v1.1.0 --pretty=format:"%s" | grep "^feat:" | sed 's/^feat: /- /'

# Extract fixes
git log v1.0.0..v1.1.0 --pretty=format:"%s" | grep "^fix:" | sed 's/^fix: /- /'
```

**Play Store release notes (80 chars per language, max 500 chars total):**

```
What's new in v1.1.0:
- Superset support for efficient workouts
- Exercise reordering crash fixed
- Faster progress view loading

Full changelog: https://github.com/deepreps/deep-reps/releases/tag/v1.1.0
```

### 4.6 App Bundle Configuration

Use Android App Bundles (AAB) for all production builds. Reduces download size by 15-30% compared to universal APK.

```gradle
// app/build.gradle.kts
android {
    bundle {
        language {
            enableSplit = false // English only at launch
        }
        density {
            enableSplit = true // Split by screen density
        }
        abi {
            enableSplit = true // Split by CPU architecture
        }
    }
}
```

**Build command:**

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

---

## 5. Monitoring & Observability

### 5.1 Crash Reporting: Firebase Crashlytics

**Setup:**

```gradle
// app/build.gradle.kts
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

**Initialize in Application class:**

```kotlin
// App.kt
class DeepRepsApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Disable Crashlytics in debug builds
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        // Set custom keys for better crash context
        FirebaseCrashlytics.getInstance().apply {
            setUserId(getCurrentUserId()) // Anonymized user ID
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
            setCustomKey("build_type", BuildConfig.BUILD_TYPE)
        }
    }
}
```

**Log non-fatal errors:**

```kotlin
try {
    riskyOperation()
} catch (e: Exception) {
    FirebaseCrashlytics.getInstance().recordException(e)
    // Show user-friendly error message
}
```

**Alerting rules (Firebase Console → Crashlytics → Alerts):**

| Alert | Condition | Notification |
|-------|-----------|-------------|
| New crash | First occurrence of any crash | Slack + email (on-call engineer) |
| Crash spike | Crash-free users drops >5% in 1 hour | Slack + email (entire team) |
| Regressed crash | Previously fixed crash reappears | Slack + email (engineer who fixed it) |
| ANR spike | ANR rate >0.3% | Slack + email (lead dev) |

### 5.2 Performance Monitoring: Firebase Performance Monitoring

**Setup:**

```gradle
// app/build.gradle.kts
plugins {
    id("com.google.firebase.firebase-perf")
}

dependencies {
    implementation("com.google.firebase:firebase-perf-ktx")
}
```

**Automatic traces (no code needed):**

- App startup time
- Screen rendering (slow/frozen frames)
- Network requests

**Custom traces for critical operations:**

```kotlin
// Example: Measure AI plan generation time
val trace = Firebase.performance.newTrace("ai_plan_generation")
trace.start()

try {
    val plan = geminiApiClient.generatePlan(request)
    trace.putMetric("exercise_count", request.exercises.size.toLong())
    trace.putAttribute("muscle_groups", request.muscleGroups.joinToString(","))
    trace.incrementMetric("success_count", 1)
} catch (e: Exception) {
    trace.incrementMetric("failure_count", 1)
    throw e
} finally {
    trace.stop()
}
```

**Key metrics to track:**

- App startup time (cold start target: <2s, warm start: <1s)
- Screen transition time (target: <300ms)
- Database query time (target: <50ms for reads, <100ms for writes)
- AI plan generation time (target: <3s)
- Network request latency (P50, P90, P99)

**Alerting:**

| Metric | Threshold | Action |
|--------|-----------|--------|
| App startup time (P90) | >2.5s | Investigate with profiler |
| Plan generation (P90) | >5s | Check Gemini API latency, optimize prompt |
| Database write (P90) | >150ms | Review query optimization |

### 5.3 ANR Detection

Crashlytics automatically detects ANRs. Additional monitoring via StrictMode in debug builds:

```kotlin
// App.kt (debug builds only)
if (BuildConfig.DEBUG && BuildConfig.ENABLE_STRICT_MODE) {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .penaltyFlashScreen() // Visual indication of violations
            .build()
    )

    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
}
```

**Common ANR causes in gym apps:**

- Database writes on main thread (use coroutines + IO dispatcher)
- Image loading blocking UI (use Coil with proper threading)
- Heavy JSON parsing on main thread (move to background)

### 5.4 Custom Metrics: Firebase Analytics

Track business-critical events:

```kotlin
// Track workout completion
Firebase.analytics.logEvent("workout_completed") {
    param("exercise_count", exerciseCount.toLong())
    param("total_sets", totalSets.toLong())
    param("duration_minutes", durationMinutes.toLong())
    param("muscle_groups", muscleGroups.joinToString(","))
}

// Track AI plan usage
Firebase.analytics.logEvent("ai_plan_generated") {
    param("exercise_count", exerciseCount.toLong())
    param("experience_level", userExperienceLevel)
    param("generation_time_ms", generationTimeMs.toLong())
}

// Track feature usage
Firebase.analytics.logEvent("template_used") {
    param("template_name", templateName)
}
```

**Key events to track:**

- `app_opened`
- `workout_started`
- `workout_completed`
- `workout_abandoned` (started but not finished)
- `ai_plan_generated`
- `ai_plan_edited` (user modified AI suggestion)
- `template_created`
- `template_used`
- `exercise_detail_viewed`
- `pr_achieved`

### 5.5 Dashboard Design

**Grafana dashboard (if backend exists) or Firebase Console:**

**Panel 1: Health Overview**

- Crash-free users (last 24h, 7d, 30d)
- ANR rate (last 24h)
- Active users (DAU, MAU)

**Panel 2: Performance**

- App startup time (P50, P90, P99)
- Plan generation latency (P50, P90, P99)
- API error rate

**Panel 3: Engagement**

- Workouts logged per day
- Templates created per day
- AI plan generation requests per day

**Panel 4: Quality**

- Crash rate by app version
- Top 10 crashes (by occurrence count)
- ANR rate by device/OS version

**Access control:**

- Entire team has read access
- DevOps + lead dev have write access (dashboard editing)

### 5.6 Alerting Thresholds & Escalation

**Severity levels:**

| Severity | Criteria | Response Time | Escalation Path |
|----------|----------|---------------|----------------|
| **P0 (Critical)** | Crash rate >2%, data loss bug, security breach | <15 minutes | Slack @channel + on-call engineer page |
| **P1 (High)** | Crash rate 1-2%, ANR spike, API downtime | <1 hour | Slack @channel |
| **P2 (Medium)** | New crash type, performance degradation >20% | <4 hours | Slack (no @channel) |
| **P3 (Low)** | Minor performance issue, cosmetic bug | Next business day | Jira ticket |

**On-call rotation:**

- Primary: Lead Android Dev
- Secondary: Mid-Senior Android Dev
- Escalation: DevOps Engineer

Use PagerDuty or Opsgenie for on-call scheduling.

---

## 6. Infrastructure

### 6.1 Backend Hosting Decision

**Three options:**

| Option | Pros | Cons | Cost (1K MAU) | Cost (100K MAU) |
|--------|------|------|---------------|----------------|
| **Firebase (Cloud Functions + Firestore)** | Easiest setup, generous free tier, auto-scaling, built-in auth | Vendor lock-in, cold start latency, limited query flexibility | Free | ~$200/mo |
| **Google Cloud Run** | Full Docker flexibility, near-zero cold start, better pricing at scale | Requires container management, more setup | ~$10/mo | ~$150/mo |
| **AWS Lambda + DynamoDB** | Mature ecosystem, excellent scaling | Steeper learning curve, cold starts, more expensive | ~$15/mo | ~$250/mo |

**Recommendation: Firebase for MVP, migrate to Cloud Run if needed post-PMF.**

**Rationale:**

- Firebase free tier (Spark plan) covers development and early launch (up to ~5K MAU)
- Cloud Functions handle AI plan generation API (Gemini API calls) without managing servers
- Firestore handles user data sync (workout logs, templates, profile) with offline support
- Firebase Authentication integrates seamlessly
- If AI plan generation becomes a bottleneck or costs spike, migrate only that service to Cloud Run (hybrid architecture)

**Migration trigger:** If Firebase costs exceed $500/mo OR plan generation latency P90 exceeds 5s consistently.

### 6.2 Firebase Architecture (Recommended MVP Setup)

**Services used:**

- **Firestore:** User profiles, workout logs, templates, exercise library (read-only)
- **Cloud Functions:** AI plan generation endpoint, data aggregation jobs
- **Authentication:** Email/password, Google Sign-In (optional)
- **Cloud Storage:** Exercise images/animations (future)
- **Hosting:** (Not needed — native app only)

**Firestore data model (simplified):**

```
users/
  {userId}/
    profile: { age, weight, experienceLevel, preferredUnit }
    templates/
      {templateId}/
        name: string
        exercises: [{ id, order }]
        createdAt: timestamp
    workouts/
      {workoutId}/
        date: timestamp
        exercises: [
          {
            exerciseId,
            sets: [{ weight, reps, type }],
            notes
          }
        ]
        duration: number
        muscleGroups: [string]

exercises/ (read-only, seeded by CSCS)
  {exerciseId}/
    name, description, muscleGroup, equipment, isolationLevel, tips, pros
```

**Cloud Function for AI plan generation:**

```typescript
// functions/src/generatePlan.ts
import * as functions from 'firebase-functions';
import { GoogleGenerativeAI } from '@google/generative-ai';

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY!);

export const generatePlan = functions.https.onCall(async (data, context) => {
  // Enforce authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'Must be logged in');
  }

  const { exercises, trainingHistory, userProfile } = data;

  // Rate limiting (prevent abuse)
  // TODO: Implement Firestore-based rate limiting (max 10 requests per hour per user)

  const prompt = buildPrompt(exercises, trainingHistory, userProfile);

  const model = genAI.getGenerativeModel({ model: 'gemini-pro' });
  const result = await model.generateContent(prompt);
  const plan = parsePlan(result.response.text());

  return { plan };
});
```

**Deployment:**

```bash
firebase deploy --only functions
```

### 6.3 API Gateway & Rate Limiting

**Firebase Cloud Functions has built-in rate limiting** (per-project quotas). For per-user rate limiting, implement in function:

```typescript
// functions/src/rateLimit.ts
import { firestore } from 'firebase-admin';

const RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000; // 1 hour
const MAX_REQUESTS_PER_WINDOW = 10;

export async function checkRateLimit(userId: string): Promise<boolean> {
  const now = Date.now();
  const windowStart = now - RATE_LIMIT_WINDOW_MS;

  const rateLimitDoc = firestore().collection('rateLimits').doc(userId);
  const doc = await rateLimitDoc.get();

  if (!doc.exists) {
    await rateLimitDoc.set({ requests: [now] });
    return true;
  }

  const requests = doc.data()!.requests.filter((ts: number) => ts > windowStart);

  if (requests.length >= MAX_REQUESTS_PER_WINDOW) {
    return false; // Rate limit exceeded
  }

  requests.push(now);
  await rateLimitDoc.set({ requests });
  return true;
}
```

### 6.4 CDN for Static Assets

**Exercise images/animations:** Store in Firebase Cloud Storage or Google Cloud Storage with CDN enabled.

```bash
# Enable Cloud CDN for GCS bucket
gsutil mb -c STANDARD -l us-central1 gs://deepreps-exercise-media
gsutil iam ch allUsers:objectViewer gs://deepreps-exercise-media
gcloud compute backend-buckets create deepreps-media-backend --gcs-bucket-name=deepreps-exercise-media --enable-cdn
```

**Image optimization:**

- Serve WebP format (80% smaller than JPEG)
- Generate multiple resolutions (1x, 2x, 3x) for different screen densities
- Use Coil image loader in app with disk caching

### 6.5 Infrastructure as Code (IaC)

**Terraform for GCP resources (if using Cloud Run or GCS):**

```hcl
# terraform/main.tf
provider "google" {
  project = "deepreps-prod"
  region  = "us-central1"
}

resource "google_storage_bucket" "exercise_media" {
  name          = "deepreps-exercise-media"
  location      = "US"
  storage_class = "STANDARD"

  uniform_bucket_level_access = true

  lifecycle_rule {
    action {
      type = "Delete"
    }
    condition {
      age = 365
    }
  }
}

resource "google_cloud_run_service" "api" {
  name     = "deepreps-api"
  location = "us-central1"

  template {
    spec {
      containers {
        image = "gcr.io/deepreps-prod/api:latest"
        env {
          name = "GEMINI_API_KEY"
          value_from {
            secret_key_ref {
              name = "gemini-api-key"
              key  = "latest"
            }
          }
        }
      }
    }
  }

  traffic {
    percent         = 100
    latest_revision = true
  }
}

resource "google_cloud_run_service_iam_member" "public_access" {
  service  = google_cloud_run_service.api.name
  location = google_cloud_run_service.api.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}
```

**Apply infrastructure:**

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

---

## 7. Security

### 7.1 App Signing Key Management

**Play App Signing is mandatory.** Google holds the app signing key, you hold the upload key.

**Upload key security:**

- Generate with 2048-bit RSA, validity 10,000 days
- Store keystore file in 1Password or similar vault (NOT in version control)
- Base64-encode for GitHub Secrets
- Rotate upload key every 2 years (reset via Play Console)

**Disaster recovery:**

- Backup keystore file to 3 locations: 1Password, offline USB drive, encrypted cloud storage
- Document upload key password in secure vault accessible to 2+ team members

### 7.2 API Key Rotation Strategy

**Gemini API key rotation (every 6 months):**

1. Generate new API key in Google AI Studio
2. Update GitHub Secrets (`GEMINI_STAGING_API_KEY`, `GEMINI_PROD_API_KEY`)
3. Deploy new build with updated key
4. Wait 7 days (grace period for users on old app versions)
5. Revoke old API key

**Automated rotation (future):**

Use Google Secret Manager with automatic rotation enabled.

### 7.3 Dependency Vulnerability Scanning

**Schedule:**

- **Automated:** Snyk runs on every merge to `develop` (see 1.4)
- **Manual audit:** Monthly review of dependency versions and CVEs

**Action on vulnerability:**

- **Critical/High:** Fix within 48 hours, hotfix release if actively exploited
- **Medium:** Fix within 2 weeks, include in next release
- **Low:** Fix within 1 month or defer

### 7.4 SAST/DAST Tools

**Static Analysis (SAST):**

- **Detekt:** Kotlin static analysis (runs in CI, see 1.3)
- **Android Lint:** Built-in Android checks

```gradle
// app/build.gradle.kts
android {
    lint {
        warningsAsErrors = true
        abortOnError = true
        checkDependencies = true

        disable += listOf("ObsoleteLintCustomCheck")

        // Security-critical checks
        enable += listOf(
            "HardcodedDebugMode",
            "HardcodedText",
            "UnusedResources",
            "SetJavaScriptEnabled"
        )
    }
}
```

**Dynamic Analysis (DAST):**

Not applicable for native Android apps (DAST is for web apps). Equivalent: **instrumentation tests with security test cases**.

```kotlin
// Security test: Ensure no sensitive data in logs
@Test
fun testNoSensitiveDataInLogs() {
    val logcatOutput = getLogcatOutput()
    assertFalse(logcatOutput.contains("password"))
    assertFalse(logcatOutput.contains("api_key"))
}
```

### 7.5 Code Signing & Supply Chain Integrity

**Verify GitHub Actions workflows use pinned versions (not `@latest`):**

```yaml
# BAD: Unpinned version
- uses: actions/checkout@v4

# GOOD: Pinned to specific commit
- uses: actions/checkout@b4ffde65f46336ab88eb53be808477a3936bae11 # v4.1.1
```

**Dependabot monitors action versions:**

```yaml
# .github/dependabot.yml
updates:
  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"
```

**Sign commits (optional but recommended):**

```bash
# Generate GPG key
gpg --full-generate-key

# Configure Git
git config --global user.signingkey YOUR_KEY_ID
git config --global commit.gpgsign true
```

---

## 8. Disaster Recovery

### 8.1 Backup Strategy

**User data (Firestore):**

- **Automatic backups:** Firebase Blaze plan includes automated daily backups (retained for 30 days)
- **Export schedule:** Weekly exports to Google Cloud Storage using Cloud Scheduler

```bash
# Cloud Scheduler job (weekly Firestore export)
gcloud scheduler jobs create http firestore-backup \
  --schedule="0 2 * * 0" \
  --uri="https://firestore.googleapis.com/v1/projects/deepreps-prod/databases/(default):exportDocuments" \
  --message-body='{\"outputUriPrefix\":\"gs://deepreps-backups/firestore\"}' \
  --oauth-service-account-email=firebase-adminsdk@deepreps-prod.iam.gserviceaccount.com
```

**Code and configuration:**

- Git is the backup (GitHub as remote)
- Mirror repository to GitLab or Bitbucket (disaster recovery if GitHub is unavailable)

**Signing keys:**

- See 7.1 (backup to 1Password, offline USB, encrypted cloud storage)

### 8.2 Recovery Procedures

**Scenario 1: Firestore data corruption**

1. Identify corruption timestamp
2. Restore from nearest automated backup before corruption:

```bash
gcloud firestore import gs://deepreps-backups/firestore/[BACKUP_ID]
```

3. Notify users of data restoration (last X hours of data may be lost)

**Scenario 2: Catastrophic app signing key loss (upload key)**

1. Verify backup locations (1Password, USB, cloud)
2. If all backups lost, reset upload key via Play Console (requires account verification)
3. Generate new upload key, update CI/CD secrets
4. Deploy new build with new key

**Scenario 3: GitHub Actions outage**

1. Download latest commit from GitHub
2. Build locally:

```bash
./gradlew assembleRelease
```

3. Manually upload AAB to Play Console
4. Resume CI/CD when GitHub Actions recovers

### 8.3 Incident Response Playbook

**Incident classification:**

- **P0:** Data loss, security breach, crash rate >5%
- **P1:** Crash rate 2-5%, ANR spike, API downtime
- **P2:** New crash type, performance degradation
- **P3:** Minor bug, cosmetic issue

**P0 Incident Response (Data Loss Example):**

1. **Detect:** Alert fires (Crashlytics, monitoring dashboard)
2. **Assess:** On-call engineer investigates scope (how many users affected, what data lost)
3. **Contain:** Halt Play Store rollout, revert to previous version if possible
4. **Communicate:** Post in Slack #incidents channel, notify Product Owner
5. **Fix:** Emergency hotfix, test locally, fast-track through CI
6. **Deploy:** Hotfix to internal → closed → production (accelerated rollout)
7. **Monitor:** Watch crash rate, validate fix effectiveness
8. **Post-mortem:** Within 48 hours, document root cause and prevention measures

**Post-mortem template:**

```markdown
# Incident Post-Mortem: [Title]

**Date:** YYYY-MM-DD
**Severity:** P0/P1/P2/P3
**Duration:** X hours
**Impact:** X users affected, Y% crash rate

## Timeline
- HH:MM - Incident detected
- HH:MM - Root cause identified
- HH:MM - Fix deployed
- HH:MM - Incident resolved

## Root Cause
[Technical explanation]

## Resolution
[What was done to fix]

## Prevention
[Actionable items to prevent recurrence]

## Action Items
- [ ] Task 1 (owner: Name, due: Date)
- [ ] Task 2 (owner: Name, due: Date)
```

### 8.4 SLA Definitions

**Availability SLA (production backend):**

- **Target:** 99.5% uptime (43.8 minutes downtime per month)
- **Measurement:** Firebase uptime + API endpoint health checks

**Performance SLA:**

- App startup (P90): <2.5s
- Plan generation (P90): <5s
- Database queries (P90): <100ms

**Crash rate SLA:**

- Crash-free users: >99.5%
- ANR rate: <0.3%

**Data recovery SLA:**

- Recovery Point Objective (RPO): <24 hours (max data loss in disaster)
- Recovery Time Objective (RTO): <4 hours (time to restore service)

---

## 9. Cost Management

### 9.1 Firebase Free Tier Limits (Spark Plan)

| Service | Free Tier Limit | Overage Cost |
|---------|----------------|-------------|
| Firestore | 50K reads/day, 20K writes/day, 1GB storage | $0.06 per 100K reads, $0.18 per 100K writes |
| Cloud Functions | 2M invocations/month, 400K GB-sec compute | $0.40 per 1M invocations, $0.0000025 per GB-sec |
| Cloud Storage | 5GB storage, 1GB downloads/day | $0.026 per GB/month, $0.12 per GB egress |
| Authentication | Unlimited (free) | Free |

**Exceeded when:**

- ~5K MAU (assuming 10 workouts/month per user → 500K Firestore writes/month)

**Mitigation:** Batch writes, cache aggressively, reduce unnecessary reads.

### 9.2 Cost Estimation at Scale

**Assumptions:**

- Average user logs 10 workouts/month
- Each workout = 30 Firestore writes (sets logged incrementally)
- 20% of workouts use AI plan generation (Cloud Function call)
- Average workout history read per AI generation = 500 Firestore reads

**1K MAU:**

| Service | Usage | Cost |
|---------|-------|------|
| Firestore writes | 300K/month | Free (under 600K/month) |
| Firestore reads | 100K/month | Free (under 1.5M/month) |
| Cloud Functions | 2K invocations/month | Free (under 2M/month) |
| Gemini API | 2K requests/month | ~$2 (free tier + $0.001/request) |
| **Total** | | **~$2/month** (Spark plan) |

**10K MAU:**

| Service | Usage | Cost |
|---------|-------|------|
| Firestore writes | 3M/month | $54 ($0.18 per 100K writes) |
| Firestore reads | 1M/month | Free |
| Cloud Functions | 20K invocations/month | Free |
| Gemini API | 20K requests/month | ~$20 |
| Cloud Storage | 10GB media | $0.26 |
| **Total** | | **~$75/month** (Blaze plan required) |

**100K MAU:**

| Service | Usage | Cost |
|---------|-------|------|
| Firestore writes | 30M/month | $540 |
| Firestore reads | 10M/month | $36 |
| Cloud Functions | 200K invocations/month | Free |
| Gemini API | 200K requests/month | ~$200 (negotiate volume discount) |
| Cloud Storage | 50GB media + CDN | $50 |
| **Total** | | **~$826/month** |

**Cost spike trigger:** If monthly Firebase bill exceeds $500, evaluate migration to Cloud Run for backend API.

### 9.3 Cost Optimization Strategies

**Firestore optimization:**

- **Batch writes:** Group set logs, write once per exercise instead of per set
- **Cache aggressively:** Use Room database for local-first architecture, sync to Firestore only on WiFi
- **Denormalize data:** Reduce reads by embedding related data (e.g., store exercise name in workout log, not just ID)

**Cloud Functions optimization:**

- **Increase memory allocation:** Higher memory = faster execution = lower GB-sec cost (counterintuitive but true)
- **Use HTTP functions instead of callable functions** when possible (lower overhead)

**Gemini API optimization:**

- **Prompt compression:** Minimize token count in prompt (exclude redundant history)
- **Cache plans client-side:** Reuse last plan if user runs identical workout within 7 days
- **Fallback to baseline plans:** If API fails, use experience-level baseline (no API cost)

**Cloud Storage optimization:**

- **Lifecycle policies:** Delete old backups after 90 days
- **Compress media:** WebP for images, H.265 for videos

### 9.4 Budget Alerts

**Google Cloud Billing alerts:**

1. Go to Cloud Console → Billing → Budgets & alerts
2. Create budget with thresholds:
   - 50% of budget → Email to DevOps + Product Owner
   - 80% of budget → Email + Slack alert
   - 100% of budget → Email + Slack @channel + pause non-critical Cloud Functions

**Budget per environment:**

- **Development:** $10/month
- **Staging:** $25/month
- **Production:** $100/month initially, scale with MAU

**Review cadence:** Monthly finance review with Product Owner and DevOps to assess cost trends.

---

## Appendix: Quick Reference

### Essential Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release bundle
./gradlew bundleRelease

# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport

# Lint check
./gradlew ktlintCheck

# Deploy Firebase Functions
firebase deploy --only functions

# Terraform apply infrastructure
cd terraform && terraform apply

# Manual Play Store upload (emergency)
# Go to Play Console → Deep Reps → Release → Production → Create new release → Upload AAB
```

### Key Metrics to Watch Daily

- Crash-free users (last 24h)
- ANR rate (last 24h)
- Active users (DAU)
- API error rate
- App startup time (P90)

### Rollback Checklist

- [ ] Halt Play Store rollout immediately
- [ ] Notify team in Slack #incidents
- [ ] Identify bad version and last known good version
- [ ] Revert code to last good commit
- [ ] Fast-track hotfix through CI (internal → closed → production)
- [ ] Monitor metrics after hotfix deployment
- [ ] Schedule post-mortem within 48 hours

---

**End of DevOps & Infrastructure Blueprint**

This document is version-controlled and must be updated with every major infrastructure or pipeline change. All pipeline configurations (GitHub Actions workflows, Terraform scripts) referenced here must be committed to the repository.
