plugins {
    id("deepreps.android.library")
    id("deepreps.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.deepreps.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        // API key from local.properties or CI environment.
        // local.properties: geminiApiKey=your_key_here
        // CI: -PgeminiApiKey=$SECRET
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${project.findProperty("geminiApiKey") ?: ""}\""
        )
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)

    testImplementation(libs.bundles.testing)
    testImplementation(libs.ktor.client.mock)
    testRuntimeOnly(libs.junit5.engine)
}
