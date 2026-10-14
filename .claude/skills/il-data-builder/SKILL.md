---
name: il-data-builder
description: Builds the Android data layer for InnerLamp — Room entities/DAOs/database, Retrofit API services + DTOs, mappers, repository implementations, and manual DI wiring — implementing the domain repository interfaces and wiring them up to the use cases. Use this skill whenever the user wants to implement data access (local storage, server/network calls, offline-first), create or complete a repository implementation, map DTOs/entities to domain models, wire dependencies in di/, build core/network or core/storage, or turn the data-layer tasks of a Docs/Specs/<Feature>/ spec into working Kotlin. It follows the project's clean-architecture rules (domain is pure Kotlin and owns the repository interfaces; data implements them via core/network's NetworkClient.safeCall and core/storage, with DispatcherProvider injected instead of Dispatchers.IO), with Vietnamese code comments. It stops at the domain boundary in the other direction: it may create the domain repository interface/model it implements, but never writes UI/Compose/ViewModels/navigation — that is il-ui-builder's job.
---

# InnerLamp Data Builder

## Overview

Generates the Android data layer for InnerLamp, implementing the domain repository interfaces and
wiring them up to the use cases.

InnerLamp uses clean architecture: `ui → domain ← data → core/{network,storage}`. This skill owns
the **`data/` side of the seam** plus the infrastructure it sits on:

- `data/local/entity/` + `data/local/dao/` — Room entities, DAOs, and the database class.
- `data/remote/api/` + `data/remote/dto/` — Retrofit `XxxApiService` interfaces and `@Serializable`
  DTOs.
- `data/mapper/` — DTO ↔ domain and entity ↔ domain mapping.
- `data/repository/` — `XxxRepositoryImpl` implementing a `domain/repository` interface.
- `di/` — manual wiring modules (no DI framework — this is a firm project decision).
- `core/network/` and `core/storage/` — the shared infrastructure, when the feature needs it
  (built per `Docs/Specs/Networking/` and `Docs/Specs/Storage/`).

The seam this skill stops at is the **domain layer**. A repository implementation depends on the
domain *contract* (`domain/repository/XxxRepository` interface + `domain/model` types) and returns
`Result<T>`/`Flow<T>` that the use cases already expect. It never touches `ui/`, Compose, ViewModel,
or navigation.

**This skill never writes UI.** Screens, components, ViewModels, UI state, and navigation are
`il-ui-builder`'s job. If the domain contract you need is missing, create it as pure Kotlin under
`domain/` (interface + model only) — but never create a ViewModel or screen to "prove" the data works.

This skill is modeled on `Docs/Specs/Networking/design.md` (the `NetworkClient.safeCall` pipeline,
envelope, error mapping) and `Docs/Specs/Storage/design.md` (the `Storage` facade), and on the
existing `domain/` files. Read those first.

## Source of truth (read before writing anything)

| What you need | Where to read |
|---|---|
| The domain contract you implement | `app/src/main/java/com/example/innerlamp/domain/repository/*.kt` + `domain/model/*.kt` |
| The network pipeline + envelope + error types | `Docs/Specs/Networking/design.md` |
| The storage facade (prefs + secure) | `Docs/Specs/Storage/design.md` |
| The feature's intended architecture + tasks | `Docs/Specs/<Feature>/design.md` + `tasks.md` |
| Repo-wide conventions (layering, dispatchers, comment language) | `AGENTS.md` |
| A worked repository pattern | `references/repository_pattern.md` in this skill |

## Hard rules that override everything else below

1. **Domain stays pure.** You may add files to `domain/` (interface + model only, pure Kotlin, no
   Android/Room/Retrofit/`core` import), but never import `domain` from the wrong side and never let
   `data` leak a DTO/entity into a `domain` type.
2. **Never call `Dispatchers.IO`/`Main` directly.** Inject `DispatcherProvider`
   (`core/util/DispatcherProvider.kt`) into anything that switches threads, and run on
   `dispatcherProvider.io`. Use cases own the dispatcher decision, not repositories — check the
   existing use cases (`domain/usecase/`) for the exact shape.
3. **Duration is elapsed time, never wall clock.** `Session.startedAtEpochMillis` is display-only.
   Measure durations with `core/util/ElapsedClock` (create it if it's still missing) so clock/timezone
   changes can't corrupt data.
4. **All network access goes through `NetworkClient.safeCall`, never a raw Retrofit call or a
   hand-rolled `try/catch`.** Map `NetworkResult<T>` → `Result<T>` via `NetworkException` in the
   repository; `domain` must only ever see `Result<T>`.
5. **Structured data → Room; light key/value → `core/storage` prefs; secrets → `core/storage` secure.**
   Never stuff structured history into SharedPreferences/DataStore, and never store a token in plain
   prefs.
6. **DI is manual** (`di/XxxModule.kt` with `object`/provider functions). Do not add Hilt or Koin —
   the specs explicitly rejected a DI framework.
7. **Every generated comment is Vietnamese**, matching AGENTS.md and the existing `domain/` files.
8. **Never mark a `tasks.md` checkbox `[x]` in this skill.** You write code; `il-spec-android` owns the
   spec lifecycle.

## Reference map

| Reference | Covers |
|---|---|
| `references/repository_pattern.md` | A worked end-to-end example: entity + DAO + database, `@Serializable` DTO + `ApiService`, mapper, `XxxRepositoryImpl` (offline-first, `safeCall`, error mapping), and `di/XxxModule.kt` wiring — the canonical shape every feature's data layer should follow |

## Workflow

### 1. Read the contract and the spec

Read the `domain/repository` interface(s) and `domain/model` you must implement, then the feature's
`Docs/Specs/<Feature>/design.md` + `tasks.md` (if present) to learn the exact file/layer names the
spec already decided. Also read `Docs/Specs/Networking/design.md` before any network work and
`Docs/Specs/Storage/design.md` before any storage work.

If the repository interface or model you need does not exist yet, create it under `domain/` first —
pure Kotlin, `Flow`/`suspend`/`Result` signatures, Vietnamese KDoc. Keep it to the contract only.

### 2. Build the data sources

- **Room** (`data/local/`): entity in `entity/`, DAO in `dao/` (suspend/`Flow` methods), and the
  `@Database` class. Export schema via the KSP arg already in `app/build.gradle.kts`
  (`room.schemaLocation`). Use `@Entity`, `@Dao`, `@Query` — no KAPT (Room runs through KSP).
- **Remote** (`data/remote/`): `@Serializable` DTOs in `dto/` (match the backend envelope:
  `ApiResponse<T>`), and the `XxxApiService` interface in `api/` with `suspend fun` returning
  `ApiResponse<...>`. Mark endpoints that need auth with `@Authenticated` (per the Networking spec's
  opt-in auth).

### 3. Map

Write `data/mapper/XxxMapper.kt` (extension functions or a small object) for DTO → domain and
entity → domain (and back where you persist). Keep the mapping in `data/`, never in `domain`.

### 4. Implement the repository

Create `data/repository/XxxRepositoryImpl.kt` implementing the domain interface. Follow
`references/repository_pattern.md`:

- **Local-first**: read/write Room first, expose `Flow` from the DAO where the UI needs reactivity.
- **Network**: call `networkClient.safeCall { apiService.xxx(...) }`, unwrap `NetworkResult`, map
  failures to `Result.failure(NetworkException(error))`.
- **Sync**: record locally first, then push pending records; tolerate offline (this app is
  explicitly offline-first — users practice meditation without a connection).

### 5. Wire DI

Create/update `di/XxxModule.kt` with manual providers for: `Json`, `OkHttpClient`, `Retrofit`,
`NetworkClient` (shared — likely already in `di/NetworkModule.kt`), the DAO/database, the
repository, and the use cases that consume it. Wire `DispatcherProvider` everywhere threading is
needed. Follow the existing `di/` layout once `NetworkModule`/`StorageModule` exist.

### 6. Verify before reporting done

- No `data/` file imports `ui.*`/Compose/ViewModel; no `domain/` file imports Android/Room/Retrofit/
  `core/*`.
- The repository returns `Result`/`Flow` of domain models, never DTOs or entities.
- Threading goes through `DispatcherProvider` (search for `Dispatchers.IO`/`Dispatchers.Main` in the
  files you wrote — there should be none).
- Durations use `ElapsedClock`, not wall clock.
- Room schema exports and `./gradlew assembleDebug` compiles (KSP runs Room).
- Comments are Vietnamese.
- Any token/secret uses `core/storage` secure, not plain prefs.
