# Migrating a feature to shared Kotlin

Each feature moves its logic into `shared/` and gets its Android screens, while the iOS app switches to the shared
code and keeps looking and behaving exactly as it does now. **Hisn al-Muslim Duas is the finished template**: copy
its structure, naming and patterns for every feature.

| Layer | Template |
|---|---|
| Domain | `shared/src/commonMain/kotlin/com/muttaqi/shared/feature/dua/domain/` |
| Data | `shared/.../feature/dua/data/` |
| Presentation (MVI) | `shared/.../feature/dua/presentation/{list,category,chapter}/` |
| DI | `shared/.../feature/dua/di/DuaModule.kt` |
| iOS accessor | `shared/src/iosMain/kotlin/com/muttaqi/shared/feature/dua/DuaViewModels.kt` |
| Tests | `shared/src/commonTest/.../feature/dua/`, `shared/src/androidHostTest/.../feature/dua/` |
| iOS screens | `iosApp/Muttaqi/Presentation/Dua/Views/` (read `SharedViewModel` in `iosApp/Muttaqi/Bridge/`) |
| Android screens | `androidApp/src/main/kotlin/com/muttaqi/android/feature/dua/` |
| Android screenshots | `androidApp/src/test/kotlin/com/muttaqi/android/feature/dua/DuaScreenshotTest.kt` |

## Rules

- **Clean Architecture, MVI, SOLID**, as in the root README. Domain has no platform or library types except
  kotlinx-datetime and coroutines. Repositories are small interfaces in domain, implemented in data. Every state
  change goes through the pure reducer. One-off events (navigate, share, copy, haptics) are effects.
- **The texts are sacred.** Every religious text comes verbatim from `content/data/` (or the Quran API the app
  already uses). Never edit, generate, paraphrase or translate any. English and Urdu only; a missing Urdu text falls
  back to the published English, never to anything written by you.
- **iOS must look and behave exactly as before.** Only the source of state changes: views read `screen.state` and send
  intents. Keep every visual detail, animation, haptic and accessibility label.
- **Android matches the iOS design** in Compose with Material 3 Expressive: the same layout, spacing, type, colours and
  icons, using `designsystem/component/` (SoftCard, SoftBackdrop, SoftChip, SoftIconButton, SoftSearchField,
  SoftTopBar, ArabicText, TranslationText, PageHeader). Add a component there only if it's generic; otherwise keep it in
  your feature. Arabic and Urdu align with `TextAlign.Right` (in right-to-left text `End` is the left edge).
  `ArabicText` and `TranslationText` take `lineSpacing` (SwiftUI's `.lineSpacing`: pass what the iOS view sets, `0.sp`
  where it sets none) and `maxLines` (cut off with an ellipsis, as iOS's `.lineLimit`). A segmented control is
  Material's connected button group (`ToggleButton` with `ButtonGroupDefaults.connected…ButtonShapes()`), as on the
  topic page.
- **Stored data carries over.** Use the same keys and formats as the Swift code (`NSUserDefaults.standardUserDefaults`
  on iOS through multiplatform-settings), so nothing a user saved is lost. If stored data can't be read in place
  (SwiftData), migrate it once on first launch, and say how in the PR.
- **Stay in your lane.** Edit only your feature's folders on all three platforms, plus the lines in shared files that
  are yours: your cases in `AppRouter.swift` and `MainTabView.swift`, your factories in `DependencyContainer.swift`.
  Delete Swift code only when nothing else uses it (Home uses a lot; leave what it needs).
  Don't edit `libs.versions.toml`, the Gradle build files, `MuttaqiApp.kt`, `Koin.kt` or the design system's existing
  components unless you must, and explain why in the PR.
- **Code style**: match the surrounding code. Comments explain why, briefly. Names follow the template.

## Patterns

**Shared view model** (see `DuaListViewModel`): `MviViewModel<State, Intent, Mutation, Effect>(initialState, Reducer)`.
Load in `init` from `selectedLanguage.changes.collect { … }` so the screen follows the translation language. Handle
intents in `handle`, change state only by `mutate(…)`, send one-off events with `emit(…)`. Values that change many times
a second (compass, counters while scrolling) get their own `StateFlow` rather than living in the main state.

**Koin**: bindings go in your feature's module file (`feature/<x>/di/<X>Module.kt`, already listed in `Koin.kt`).
View models with arguments: `viewModel { (id: String) -> XViewModel(id, get(), get()) }`.

**iOS**: add `shared/src/iosMain/.../feature/<x>/<X>ViewModels.kt` (a `KoinComponent` object with one function per
screen). In SwiftUI:

```swift
@State private var screen = SharedViewModel(DuaViewModels.shared.list()) { $0.state }
...
screen.state.query                                   // read state
screen.viewModel.dispatch(intent: DuaListIntentQueryChanged(query: text))
.task { for await effect in screen.viewModel.effects { switch onEnum(of: effect) { … } } }
```

A view model with an argument is made in `init`: `_screen = State(initialValue: SharedViewModel(…(id: id)) { $0.state })`.
A control that edits state (a text field, picker or toggle) reads the view model's value as it is now, not the last
one drawn, or fast typing is lost and a cleared field can refill:
`TextField("Search", text: Binding(get: { screen.viewModel.state.value.query }, set: { … dispatch … }))`.
Kotlin types that share a name with a Swift type are `Shared.X` until the Swift one is deleted.

**Android**: each screen is a stateful `XRoute(…)` (gets the view model with `koinViewModel`, collects `state` with
`collectAsStateWithLifecycle`, handles `effects` in a `LaunchedEffect`) and a stateless `XScreen(state, onIntent, …)`
for previews and screenshots. Register destinations in your feature's existing `…Destinations` function in
`androidApp/.../feature/<x>/`; routes are `@Serializable` and carry ids, not objects. Share with
`navController.navigate(ShareRoute(passage))`.

## Verifying

Run these from your worktree's root. Create `local.properties` with `sdk.dir=/Users/vyro/Library/Android/sdk` first.

```
./gradlew :shared:testAndroidHostTest :shared:iosSimulatorArm64Test      # shared tests, both platforms
./gradlew :androidApp:assembleDebug                                      # Android builds
./gradlew :androidApp:recordRoborazziDebug                               # Android screenshots → androidApp/screenshots/
cd iosApp && xcodebuild -project Muttaqi.xcodeproj -scheme Muttaqi \
  -destination 'platform=iOS Simulator,id=<your simulator>' -derivedDataPath <your scratch>/dd build
```

- Tests: reducers, view models (intents → state and effects, language switch), repositories with fakes
  (`shared/src/commonTest/.../testing/Fakes.kt`), and a host test that decodes the real bundled file if you read one.
- iOS: install the build on your own simulator (`xcrun simctl install/launch`), walk every screen of your feature in
  light and dark and in Urdu (`plutil -replace reading_selected_language -string ur` on the app's preferences plist in
  its data container), and take screenshots (`xcrun simctl io <id> screenshot`). They must match the pre-migration app.
- Android: screenshot tests for every screen in light and dark, compared by eye with the iOS screenshots.

## Pull request

Work on your own branch, commit in logical steps, push, and open a PR against `feature/kmp-setup`:
`gh pr create --repo SaadAziz9956/Muttaqi --base feature/kmp-setup`. End commit messages with
`Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>` and the PR body with
`🤖 Generated with [Claude Code](https://claude.com/claude-code)`. In the body: what moved, what's new on Android,
how stored data carries over, what you verified (with the screenshot paths), and anything you weren't sure about.
Don't merge it; the lead reviews and merges.
