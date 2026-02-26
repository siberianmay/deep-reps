plugins {
    id("deepreps.android.feature")
}

android {
    namespace = "com.deepreps.feature.profile"
}

dependencies {
    // ConsentManager lives in :core:data (EncryptedSharedPreferences).
    // The ViewModel needs it to read/write consent preferences.
    implementation(project(":core:data"))
}
