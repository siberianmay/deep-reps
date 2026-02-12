plugins {
    id("deepreps.android.library")
    id("deepreps.android.hilt")
    alias(libs.plugins.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.deepreps.core.database"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit5.engine)
    androidTestImplementation(libs.room.testing)
}
