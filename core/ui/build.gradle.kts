plugins {
    id("deepreps.android.library")
    id("deepreps.android.compose")
}

android {
    namespace = "com.deepreps.core.ui"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    implementation(libs.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.coil.compose)
    api(libs.compose.material.icons.extended)
}
