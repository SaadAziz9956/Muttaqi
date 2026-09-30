# Muttaqi

An Islamic companion app for iOS and Android: the Quran, duas, dhikr, the 99 Names, prayer times and Qibla, a
gratitude journal, and topics and emotions with authentic texts in English and Urdu.

## Structure

```
iosApp/       SwiftUI app (Xcode). Views, the soft design system, navigation and platform services
androidApp/   Compose app with Material 3 Expressive. Screens, theme, navigation and platform services
shared/       Kotlin Multiplatform: domain, data and MVI view models used by both apps
content/      The pipeline that builds the bundled texts from published sources (see content/tools/README.md)
```

Xcode builds `shared` itself: a build phase runs `./gradlew :shared:embedAndSignAppleFrameworkForXcode` and the
app imports it as `Shared`. Android depends on it as a Gradle module.

## Architecture: Clean Architecture, MVI, SOLID

Each feature in `shared` has three layers, and dependencies only point inward:

- **domain**: entities, repository interfaces and use cases. Pure Kotlin, no platform or library types.
- **data**: repository implementations (bundled content, network, database, settings) and their mappers.
- **presentation**: one `MviViewModel` per screen.

A screen works in one direction (`shared/.../core/mvi`):

```
UiIntent ──dispatch──▶ ViewModel.handle ──use cases──▶ UiMutation ──Reducer (pure)──▶ UiState ──▶ view
                                  └──────────────▶ UiEffect (one-off: navigate, message)
```

Rules:

- **Single responsibility**: a use case does one thing; a reducer only turns state and a mutation into new state.
- **Open/closed**: new behaviour is a new intent, mutation or use case, not an edit to shared base types.
- **Liskov**: any implementation of a repository or platform interface can stand in for another, e.g. fakes in tests.
- **Interface segregation**: small interfaces (`fun interface` use cases, one repository per kind of data), so a
  view model depends only on what it uses.
- **Dependency inversion**: domain defines the interfaces; data and the apps implement them and are wired by DI.
  Platform services (location, compass, notifications) are interfaces in `shared` implemented natively in each app.

State is immutable and changes only through the reducer. Values that change many times a second (the compass
heading, the reading position) get their own flows, so they don't redraw a whole screen.

## Building

- iOS: open `iosApp/Muttaqi.xcodeproj` and run. Needs a JDK (17+) for the Kotlin build.
- Android: `./gradlew :androidApp:installDebug`.
- Shared tests: `./gradlew :shared:iosSimulatorArm64Test :shared:testAndroidHostTest`.
