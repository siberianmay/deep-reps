plugins {
    id("deepreps.android.feature")
}

android {
    namespace = "com.deepreps.feature.workout"
}

dependencies {
    implementation(project(":core:data"))
}
