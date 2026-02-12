plugins {
    id("deepreps.jvm.library")
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit5.engine)
}
