# AGENTS.md

Guidance for OpenCode sessions in this repo.

## Hard rules while coding

- **Do NOT start an emulator/device or run any tests while coding.** No `./gradlew connectedDebugAndroidTest`,
  no `adb`/AVD/emulator commands, no `testDebugUnitTest`/`test` runs, no launching the app. The user runs
  builds and tests manually.
- While coding, your only obligations are:
  1. the code must **compile / build without errors** (types, imports, Gradle wiring, resources), and
  2. there must be **adequate tests** for the change (unit tests, and instrumented tests where relevant) —
     write them, but do not execute them.
- Prefer static verification (reading code, careful API/type checks) over running anything. If a build/test
  seems essential, ask the user first instead of running it.

## Behavioral principles

### 1. Think before coding
Don't assume. Don't hide confusion. Surface tradeoffs.
- State assumptions explicitly; if uncertain, ask rather than guess.
- If several interpretations exist, present them — don't pick silently.
- Push back when a simpler approach exists.
- Stop when confused: name what's unclear and ask.

### 2. Simplicity first
Minimum code that solves the problem. Nothing speculative.
- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility"/configurability that wasn't requested.
- No error handling for impossible scenarios.
- If 200 lines could be 50, rewrite it. Test: would a senior engineer call this overcomplicated?

### 3. Surgical changes
Touch only what you must. Clean up only your own mess.
- Don't "improve" adjacent code, comments, or formatting; don't refactor what isn't broken.
- Match existing style, even if you'd do it differently.
- Unrelated dead code: mention it, don't delete it.
- Remove imports/variables/functions that **your** changes made unused; leave pre-existing dead code.
- Test: every changed line traces directly to the user's request.

### 4. Goal-driven execution
Define success criteria, then verify them.
- Turn tasks into verifiable goals: "add validation" → write tests for invalid inputs; "fix the bug" →
  write a test that reproduces it; "refactor X" → keep existing tests valid before and after.
- For multi-step tasks, state a brief plan: `1. [step] → verify: [check]`.
- **Verification here is static** (see Hard rules): write the tests and check compile-correctness by
  reading code; do not run tests or the app. Ask the user to run them.

## Current state (read this first)

InnerLamp is a native Android app (Kotlin + Jetpack Compose, single Gradle module `:app`).
It is **mid-migration**, not a blank scaffold and not a finished app:

- Gradle is fully wired for the planned stack: Room + KSP, Retrofit/OkHttp,
  SharedPreferences/AndroidKeyStore storage + BiometricPrompt, Navigation Compose, ViewModel,
  kotlinx.serialization (see `app/build.gradle.kts`). DataStore was **removed**.
- `domain/`, `core/` (gồm `core/storage/`, `core/network/`) and `di/` (NetworkModule,
  StorageModule) have real code. `data/` and `service/` are still empty placeholder dirs.
- `MainActivity.kt` is a `FragmentActivity` rendering the onboarding screen (Compose); there is
  no navigation graph yet.
- `core/util/ElapsedClock` is referenced by `domain/model/Session.kt` but **does not exist yet**.
- `CLAUDE.md` and `.github/copilot-instructions.md` still describe the initial scaffold and are
  **stale** (they claim no data/network/DI layers). Trust this file and the build files over them.

There is **no git repository** in this directory: `git status`/`diff`/`log` will fail. Don't rely on history.

## Commands

Always use the wrapper (`./gradlew`), never a global `gradle`.

> These commands are documented for reference only. Per the hard rules above, **do not run them yourself
> while coding** — the user runs builds/tests manually.

- Debug APK: `./gradlew assembleDebug`
- Unit tests (JVM, `app/src/test/`): `./gradlew testDebugUnitTest`
- Single class: `./gradlew testDebugUnitTest --tests "com.example.innerlamp.ExampleUnitTest"`
- Single method: `./gradlew testDebugUnitTest --tests "com.example.innerlamp.ExampleUnitTest.addition_isCorrect"`
- Instrumented tests (`app/src/androidTest/`, needs a device/emulator): `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lint` → `app/build/reports/lint-results-debug.html`
- Unit test report: `app/build/reports/tests/testDebugUnitTest/index.html`

## Build/toolchain quirks

- AGP 9 DSL is in use: `compileSdk { version = release(37) }`; release disables optimization via
  `optimization { enable = false }` (R8/minification off for release builds).
- `buildConfig = true` is enabled, but there is **no `buildConfigField` for the backend base URL**
  despite the misleading comment in `app/build.gradle.kts`. There is no code reading it yet.
- The Gradle daemon toolchain is Java 25 (`gradle/gradle-daemon-jvm.properties`, auto-downloaded via
  the foojay resolver), while `compileOptions` targets Java 11. Don't "fix" one to match the other.
- `org.gradle.configuration-cache=true` — changing build scripts affects cache reuse.
- Room schemas export to `app/schemas` via the KSP arg in `app/build.gradle.kts`; the dir is created on
  the first Room database compile. Room compiler runs through KSP, not KAPT.
- Add dependencies to `gradle/libs.versions.toml` (`[versions]` + `[libraries]`), not as hardcoded
  coordinates in `app/build.gradle.kts`.

## Architecture rules (enforced by existing code/comments)

- Layering: `domain` is pure Kotlin and owns the repository interfaces
  (`domain/repository/SessionRepository.kt`) and models; `data` implements them. Domain must never
  import Room/Retrofit/Android. Keep this boundary when adding the data layer.
- Use cases are classes with `operator fun invoke(...)` returning `Result`/`Flow` (see
  `domain/usecase/`). Follow that shape for new use cases.
- Never call `Dispatchers.IO`/`Main` directly in repositories/use cases; inject `DispatcherProvider`
  (`core/util/DispatcherProvider.kt`) so tests can swap in `StandardTestDispatcher`.
- Duration measurement must use elapsed time, not wall clock. `Session.startedAtEpochMillis` is for
  display only (clock/timezone changes would corrupt durations). Implement/use `ElapsedClock`.
- Structured data (session history) goes through Room; only light key/value settings
  (onboarding, default pattern) go through `core/storage/` prefs (`SharedPreferences`, not
  DataStore). Secrets/credentials must go through `core/storage/` secure (`SecureStorage` +
  AndroidKeyStore) — never store them in plain prefs or scattered across features.
- UI: top-level `@Composable fun` with trailing `modifier: Modifier = Modifier`, previews wrapped in
  `InnerLampTheme { ... }`. All code stays under `com.example.innerlamp`.
- Code comments and docs are written in **Vietnamese**; match that when adding comments.

## Testing

- Local unit test deps available: JUnit4, `kotlinx-coroutines-test`, Turbine (Flow), MockK.
- Empty test dirs mirror the planned layers (`test/.../data/repository`, `domain/usecase`,
  `ui/screens/home`); `ExampleUnitTest`/`ExampleInstrumentedTest` are the only tests today.
