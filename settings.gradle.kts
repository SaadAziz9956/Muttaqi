pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Muttaqi"

// Lets modules depend on each other as projects.shared rather than by string
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Shared logic (Kotlin Multiplatform): domain, data and MVI view models for both apps
include(":shared")
// Android app: Compose with Material 3 Expressive. The iOS app (SwiftUI) lives in iosApp/ and builds with Xcode
include(":androidApp")
