plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.muttaqi.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // The same identifier as the iOS app
        applicationId = "com.muttaqi.islamic"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    // The texts both apps bundle, kept once in content/data
    sourceSets["main"].assets.directories.add("../content/data")

    testOptions {
        // Screenshot tests render Compose on the JVM with Robolectric, so screens can be checked without an emulator
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(projects.shared)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
}
