plugins {
    id("deepreps.android.feature")
}

android {
    namespace = "com.deepreps.feature.onboarding"
}

dependencies {
    // ConsentManager lives in :core:data (EncryptedSharedPreferences).
    // The ViewModel needs it to save consent preferences during onboarding completion.
    implementation(project(":core:data"))
}
