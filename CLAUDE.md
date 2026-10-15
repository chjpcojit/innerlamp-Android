# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**InnerLamp** is a native Android app built with Kotlin and Jetpack Compose. Currently in scaffold state—single module with default Android Studio template structure (MainActivity rendering "Hello Android"). No custom architecture (ViewModel/data/network layers) yet; check source before assuming MVVM, DI, or navigation patterns exist.

### Tech Stack
- **Kotlin 2.2.10**, AGP 9.4.0
- **Jetpack Compose** (BOM `2026.02.01`), Material3
- **Namespace/App ID**: `com.example.innerlamp`
- **SDK**: minSdk 31, targetSdk/compileSdk 37
- **Java**: VERSION_11 source/target compatibility
- Single Gradle module: `:app`

## Language policy

Decided by the product owner (anh Đức), 2026-10-15. Applies to every agent and human contributor, and
**overrides any language rule in a skill, template or older doc** in this repo.

| Where | Language |
|---|---|
| Conversation with the user, questions, reports, notes and drafts that are **not committed** | Vietnamese ("anh" for the user, "em" for the agent), technical terms kept in English |
| Everything **committed to git**: docs, specs, ADRs, READMEs, commit messages, PR titles and bodies, identifiers | English |
| Code comments | Bilingual: English first, then Vietnamese |
| User-facing copy (UI strings, localisation catalogues, copy quoted in a spec) | Its own language (Vietnamese first), with an English gloss when quoted in a doc |

- Existing Vietnamese docs stay as they are until they are next edited; a change that edits such a file
  rewrites it in English in the same change. Do not open translation-only PRs.
- Files that are bilingual by convention (EN + VI, e.g. assumptions and tech-debt entries) keep that
  convention; their English part is authoritative.

## Build & Test Commands

Always use the Gradle wrapper (`./gradlew`, or `gradlew.bat` on Windows), not a global `gradle` installation.

### Build
- **Debug APK**: `./gradlew assembleDebug`

### Unit Tests (JVM, `app/src/test/`)
- **Full suite**: `./gradlew testDebugUnitTest`
- **Single class**: `./gradlew testDebugUnitTest --tests "com.example.innerlamp.ExampleUnitTest"`
- **Single method**: `./gradlew testDebugUnitTest --tests "com.example.innerlamp.ExampleUnitTest.addition_isCorrect"`

> **Build notes (from the retired `Docs/gio_assumptions.md`):** the project has product flavors
> (`dev`/`staging`/`prod`), so the unit-test task is `testDevDebugUnitTest` (not `testDebugUnitTest`)
> and lint is `lintDevDebug`; `lintDevDebug` has 8 pre-existing `ViewModelConstructorInComposable`
> errors in Login/Onboarding/Signup/Splash.

### Instrumented/UI Tests (`app/src/androidTest/`, requires emulator/device)
- **Full suite**: `./gradlew connectedDebugAndroidTest`

### Lint
- **Run lint**: `./gradlew lint`
- **Report**: `app/build/reports/lint-results-debug.html`

## Code Architecture & Layout

### Directory Structure
```
app/src/main/
  java/com/example/innerlamp/
    MainActivity.kt          ← Entry point
    ui/theme/
      Color.kt              ← Compose theming (template-generated)
      Theme.kt
      Type.kt
  AndroidManifest.xml       ← Single launcher activity
  res/                      ← Resources (drawables, strings, colors)

app/src/test/               ← JUnit4 unit tests (local JVM)
app/src/androidTest/        ← Instrumented tests (androidx.test/Espresso/Compose UI)
```

### High-Level Architecture Patterns

**Compose Conventions**:
- Composables are top-level `@Composable fun` with trailing `modifier: Modifier = Modifier` parameter
- Example pattern in `MainActivity.kt`: `Greeting(name, modifier)`
- Previews use `@Preview(showBackground = true)` wrapped in `InnerLampTheme { ... }`

**Theme Integration**:
- Wrap new screens/composables in `InnerLampTheme { ... }` for consistent Material3 styling
- Theme assets are template-generated; modify `Color.kt`, `Theme.kt`, `Type.kt` for customization

**Package Structure**:
- Keep all code under `com.example.innerlamp` package (matches `namespace`/`applicationId`)
- Organize new features/screens logically (e.g., `ui/screens/`, `ui/components/` as the app grows)

## Dependencies

Version catalog is in `gradle/libs.versions.toml`. Add new dependencies there (`[libraries]` and `[versions]` sections) rather than hardcoding coordinates in `app/build.gradle.kts`.

**Current dependencies**:
- Compose UI, Material3, Activity Compose
- Lifecycle Runtime, Core KTX
- JUnit4, Espresso, Compose UI testing

## Development Notes

- **No custom layers yet**: This scaffold has no ViewModel, repository, network, or DI patterns pre-configured. Introduce these when needed for specific features.
- **Material3 baseline**: All UI should follow Material3 design guidelines via Compose.
- **Instrumented tests require a device or emulator**: connectedDebugAndroidTest will fail if none is connected.
- **Lint warnings in debugImplementation** (UI tooling, test manifests) are expected; they don't affect release builds.

## Common Tasks

### Add a new dependency
1. Open `gradle/libs.versions.toml`
2. Add version entry to `[versions]` (if needed)
3. Add library entry to `[libraries]`
4. Reference in `app/build.gradle.kts` using `alias()` or direct dependency

### Add a new Composable screen
1. Create file in `app/src/main/java/com/example/innerlamp/ui/screens/` (create dir as needed)
2. Define `@Composable fun ScreenName(modifier: Modifier = Modifier) { ... }`
3. Add `@Preview` function for quick preview in Android Studio
4. Wrap content in `InnerLampTheme { ... }`
5. Reference in `MainActivity` as needed (navigation/routing not set up yet)

### Run unit tests locally
```bash
./gradlew testDebugUnitTest
```
Output is in `app/build/reports/tests/testDebugUnitTest/index.html`
