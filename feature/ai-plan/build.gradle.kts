plugins {
    id("deepreps.android.feature")
}

android {
    namespace = "com.deepreps.feature.aiplan"
}

dependencies {
    implementation(project(":core:network"))
}
