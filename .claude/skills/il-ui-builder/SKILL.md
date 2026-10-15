---
name: il-ui-builder
description: Builds Android Jetpack Compose UI for InnerLamp — screens, shared components, ViewModels, UI state, and navigation — wiring the UI layer up to (but never past) the domain use cases. Use this skill whenever the user wants to implement a screen or component from the InnerLamp design handoff prototype (Docs/design_handoff_innerlamp/), build the Compose theme/design tokens, create a screen + ViewModel for a feature, wire navigation, or turn the UI-layer tasks of a Docs/Specs/<Feature>/ spec into working Kotlin. It reads the HTML prototype + design-token README + the feature spec, then produces Kotlin under com.example.innerlamp (ui/theme, ui/components, ui/screens, ui/navigation) following the project's MVVM + clean-architecture conventions, with bilingual code comments (English first, then Vietnamese). It stops at the domain boundary: it may create the domain model/use case it calls, but never writes the data layer (Room/Retrofit/DataStore/repository impl/DI) — that is il-data-builder's job.
---

# InnerLamp UI Builder

## Overview

Generates Android Jetpack Compose UI for InnerLamp from the design handoff and the feature
spec, and wires it down to — but never past — the domain use cases.

InnerLamp is a native Android app (Kotlin + Jetpack Compose, single Gradle module `:app`) in
clean-architecture layers: `ui → domain ← data → core/{network,storage}`. This skill owns the
**`ui/` side of the seam**:

- `ui/theme/` — the Compose `Color`/`Type`/`Theme` built from the design tokens.
- `ui/components/` — the shared component library (IL-Btn, IL-Field, IL-Chip, IL-Row,
  IL-SettingRow, IL-NavBar, IL-BottomNav, IL-Avatar, IL-State).
- `ui/screens/<feature>/` — one folder per screen, each with `XxxScreen.kt` + `XxxViewModel.kt`
  (+ `XxxUiState.kt` when state is non-trivial).
- `ui/navigation/` — the NavHost + routes + bottom-nav shell.

The seam this skill stops at is the **domain layer** (`domain/model`, `domain/usecase`,
`domain/repository`). A ViewModel calls a use case (`useCase(...)`) and receives a `Result<T>` or
`Flow<T>`; it never sees Room, Retrofit, `core/network`, or `core/storage`.

**This skill never writes the data layer.** Implementing a repository interface, mapping DTOs,
building Room/Retrofit/DataStore, or wiring `di/` is `il-data-builder`'s job. If a screen you are
building needs data that isn't wired yet, use a fake/in-memory repository for now and leave a
bilingual `TODO` comment (English first, then Vietnamese) pointing at `il-data-builder`, e.g.
`// TODO(il-data-builder): bind to the API … / Nối API …` — do not stub the data layer inside the UI.

This skill is modeled on `Docs/design_handoff_innerlamp/README.md` (the design tokens, 9
components, 45 screens) and on any `Docs/Specs/<Feature>/` that already exists. Read those first.

## Source of truth (read before writing anything)

| What you need | Where to read |
|---|---|
| Design tokens (colors, typography, spacing, dark/light) | `Docs/design_handoff_innerlamp/README.md` + `references/design_system.md` in this skill |
| A specific component's pixels/states | `Docs/design_handoff_innerlamp/design/IL-<Component>.dc.html` (markup + `renderVals()` for state colors) |
| A specific screen's layout | `Docs/design_handoff_innerlamp/design/InnerLamp Prototype.dc.html` (the screen section) |
| The feature's intended architecture + tasks | `Docs/Specs/<Feature>/design.md` + `tasks.md` (if it exists) |
| Repo-wide conventions (layering, dispatchers, comment language) | `AGENTS.md` |
| The theme/type files you must build on | `app/src/main/java/com/example/innerlamp/ui/theme/*.kt` |

## Hard rules that override everything else below

1. **Stop at the domain boundary.** A `XxxViewModel` may import `domain.model.*` and
   `domain.usecase.*`, and call `useCase(...)`. It must never import `data.*`, `core/network.*`,
   `core/storage.*`, Room, Retrofit, or OkHttp. A screen/component is even narrower: it imports
   `domain.model.*` (for the values it renders) but never calls a use case or repository directly —
   only the ViewModel does.
2. **If a domain type or use case you need is missing, create it as pure Kotlin under `domain/`** —
   but only the *shape* (data class / interface / `operator fun invoke`). Do not write its data
   implementation, and do not invent data classes that belong to the data layer (DTOs, entities).
3. **Every generated comment is bilingual: English first, then Vietnamese**, per the Language policy
   in `CLAUDE.md` (and AGENTS.md). Identifiers stay English; KDoc/`//` comments (and `TODO`s) give the
   English line first, then the Vietnamese one. User-facing strings stay Vietnamese (see step 4).
4. **Every top-level composable takes a trailing `modifier: Modifier = Modifier`**, and every
   `@Preview` is wrapped in `InnerLampTheme { ... }` (see `ui/theme/Theme.kt`).
5. **Do not call `Dispatchers.IO`/`Main` anywhere in the UI layer.** ViewModels use `viewModelScope`
   (Main-backed, correct for UI); use cases own the threading via `DispatcherProvider`. If you are
   tempted to launch IO work from a ViewModel, you are about to cross the seam — hand it to a use
   case instead.
6. **Design tokens come from the prototype, not from defaults.** The current `Color.kt` is the
   template purple; the first real UI task must replace it with the `--il-*` palette from
   `references/design_system.md`. Do not ship new screens on the template colors.
7. **Never mark a `tasks.md` checkbox `[x]` in this skill.** You write code; `il-spec-android` owns
   the spec lifecycle. If a task is now implemented, say so in your reply — leave the checkbox edit
   to the spec workflow.

## Reference map

| Reference | Covers |
|---|---|
| `references/design_system.md` | The `--il-*` tokens mapped to Compose `Color`/`Typography`/`Dp` (light + dark), the always-dark screens, and the 9 shared components with their props/states — the concrete translation from the prototype to Compose |

## Workflow

### 1. Determine scope and read the source

From the user's request, decide which of these you are building:

1. **Theme/tokens** — `ui/theme/Color.kt`, `Type.kt`, `Theme.kt`. Required before any real screen.
2. **Shared component(s)** — `ui/components/IL-*.kt` (e.g. `ILButton`, `ILField`, `ILNavBar`).
3. **A screen** — `ui/screens/<feature>/XxxScreen.kt` + `XxxViewModel.kt` (+ `XxxUiState.kt`).
4. **Navigation** — `ui/navigation/` NavHost + routes + the 5-tab bottom-nav shell.

Read the prototype README and `references/design_system.md` first. If a `Docs/Specs/<Feature>/`
exists for the feature, read its `design.md` (Layers & components table + navigation flow) and
`tasks.md` (which phases are UI vs data) so you build exactly what the spec names — no more.

### 2. Build the theme first (once)

Translate the `--il-*` token table in `references/design_system.md` into:

- `Color.kt` — a `val` per token (light + dark where they differ), named close to the token
  (`IlCanvas`, `IlCard`, `IlInk`, `IlGold`, …). Keep the "always-dark" screens' palette as separate
  constants so they don't flip in light mode.
- `Type.kt` — `Typography` with `Lora` (serif, display/scripture text) and `Be Vietnam Pro` (body/UI).
  Google Fonts, subset `vietnamese`.
- `Theme.kt` — `InnerLampTheme` with 3 modes: light / dark / system (read the `theme` preference),
  `dynamicColor = false` (the brand palette must not be overridden by Material You).

The prototype README §"Dark / Light mode" is canonical for which token changes vs stays fixed.

### 3. Build shared components (before screens that use them)

Implement each `IL-*` component from `references/design_system.md`'s component table as a
`@Composable fun IlXxx(...)` in `ui/components/`. Match props, states, and pixel values (heights
50px buttons / 48px inputs / 72px rows, radii, spacing) from the `.dc.html` files. Prefer real icon
set (Lucide/Phosphor thin) for glyphs — the prototype uses CSS shapes as placeholders.

### 4. Build a screen + ViewModel + UI state

For each screen:

- `XxxUiState.kt` — a `sealed interface` or `data class` capturing the render state
  (`Idle`/`Loading`/`Success(data)`/`Error(message)`), plus any form field state. Field-level
  validation state belongs here (or in the ViewModel), not in domain.
- `XxxViewModel.kt` — constructor-injected use cases (manual DI: the ViewModel takes them as
  constructor params). Expose state via `StateFlow`; collect `Flow` use cases with
  `collectAsStateWithLifecycle()` / `stateIn(viewModelScope, ...)`. Call `useCase(...)` and map
  `Result`/`Flow` to `XxxUiState`. Map domain errors (e.g. a `sealed` error type) to user-facing
  Vietnamese messages here.
- `XxxScreen.kt` — `@Composable fun XxxScreen(...)`, reads the ViewModel state via
  `collectAsStateWithLifecycle()`, renders with the shared components, and exposes events via
  callbacks/lambdas. Include `@Preview` wrapped in `InnerLampTheme`.

Form rules from the prototype (`README.md` §Interactions): OTP 6 digits, phone 10 digits (error →
`IlField` error state), password ≥ 8 chars, CTA disabled until terms checkbox ticked, etc. Encode
these in the ViewModel/screen.

### 5. Wire navigation

Add the route + destination to `ui/navigation/`, and register the screen in the bottom-nav shell if
it is a root tab (Home, Library, Lamp→setup, Sangha, Me). Match the prototype's stack behavior
(`go(screen)` push / back pop / tab-to-home clears the stack).

### 6. Verify before reporting done

- The theme is the `--il-*` palette, not the template purple (unless the task is explicitly
  theme-independent).
- Every composable has a trailing `modifier: Modifier = Modifier`; every `@Preview` is in
  `InnerLampTheme`.
- No `ui/` file imports `data.*`, `core/network.*`, `core/storage.*`, Room, Retrofit, or OkHttp.
- ViewModels call use cases only; screens call ViewModels only.
- Comments are bilingual (English first, then Vietnamese).
- Any domain type you had to create is pure Kotlin with no Android import.
- Any not-yet-wired data need is a bilingual `TODO` (English first, then Vietnamese) pointing at
  `il-data-builder`, not a half-written repository.
- `./gradlew assembleDebug` compiles (or note clearly if a dependency you expect from
  `il-data-builder` is still missing).
