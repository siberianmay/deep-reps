plugins {
    id("deepreps.android.library")
    id("deepreps.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.deepreps.core.data"

    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.all { it.useJUnitPlatform() }
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:common"))

    // Room KTX for withTransaction() in WorkoutSessionRepositoryImpl
    implementation(libs.room.ktx)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.timber)

    // EncryptedSharedPreferences for ConsentManager
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Firebase (BOM-managed) for FirebaseAnalyticsTracker and FirebaseFeatureFlagProvider
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit5.engine)
}
