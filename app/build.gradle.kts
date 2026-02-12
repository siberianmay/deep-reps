plugins {
    id("deepreps.android.application")
    id("deepreps.android.compose")
    id("deepreps.android.hilt")
}

// Firebase plugins require google-services.json to exist.
// Apply conditionally so the project builds without Firebase configured.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.gradle.get().pluginId)
}

android {
    namespace = "com.deepreps.app"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.deepreps.app"
    }

    signingConfigs {
        getByName("debug") {
            // Uses default debug keystore
        }
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
                ?: project.findProperty("KEYSTORE_PATH") as? String
            val keystorePass = System.getenv("KEYSTORE_PASSWORD")
                ?: project.findProperty("KEYSTORE_PASSWORD") as? String
            val keyAliasValue = System.getenv("KEY_ALIAS")
                ?: project.findProperty("KEY_ALIAS") as? String
            val keyPass = System.getenv("KEY_PASSWORD")
                ?: project.findProperty("KEY_PASSWORD") as? String

            if (keystorePath != null && keystorePass != null && keyAliasValue != null && keyPass != null) {
                storeFile = file(keystorePath)
                storePassword = keystorePass
                keyAlias = keyAliasValue
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
        }
    }
}

dependencies {
    implementation(project(":feature:workout"))
    implementation(project(":feature:exercise-library"))
    implementation(project(":feature:progress"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:ai-plan"))
    implementation(project(":feature:templates"))
    implementation(project(":feature:onboarding"))

    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))

    implementation(libs.core.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.timber)
    implementation(libs.profileinstaller)

    // Firebase (BOM-managed)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    // ProcessLifecycleOwner for app-level lifecycle tracking
    implementation(libs.lifecycle.process)

    debugImplementation(libs.leakcanary)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit5.engine)
}
