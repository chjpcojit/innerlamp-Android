# Prospective vs Retroactive mode

Ask which applies before writing (see `gathering_questions.md`).

## Prospective — before the feature is built

- `requirements.md`: criteria describe intended behavior, each tagged with the product ID it
  implements. Open questions start empty or with genuinely open Android decisions.
- `design.md`: the planned screen, ViewModel, use cases, repository, data sources and DI. An
  operation not in innerlamp-be `openapi.yaml` is "planned, contract not yet confirmed" and the UI
  runs on a fake repository until it is.
- `tasks.md`: every item `[ ]`.
- `changelog.md`: `v1.0 (initial)`, naming the product spec version.

## Retroactive — the feature already runs

Read the code before writing, so the spec describes what ships, not an idealized version. Paths are
under `app/src/main/java/com/example/innerlamp/`:

- UI: `ui/screens/<feature>/` (`XxxScreen.kt`, `XxxViewModel.kt`, `XxxUiState.kt`),
  `ui/navigation/Routes.kt`, `AppNavHost.kt`.
- Domain: `domain/model/`, `domain/usecase/`, `domain/repository/`, `domain/calendar/` (local rules).
- Data: `data/remote/api/`, `data/remote/dto/`, `data/mapper/`, `data/repository/`, `core/database/`,
  `core/storage/`, `di/<X>Module.kt`.
- Tests: `app/src/test/java/com/example/innerlamp/…`.
- Assumptions already recorded in `Docs/<x>_assumptions.md` — read them as Android build choices; a
  business rule there is a product question, not a fact (SKILL.md rule 2).

Then:

- Behavior that differs from the product spec is not "fixed" in the spec: write what the code does,
  mark the criterion `⚠️` or Pending, and report the drift (the product matrix should show `drift`
  for Android).
- Stubs (in-memory seed data, a no-op button, a fake repository, a switch that only stores a flag)
  go under Pending with their tasks.md Phase.
- `tasks.md`: `[x]` only for what you read and verified; record the date ("`[x]` only when verified to
  exist in the code on …").
- `changelog.md`: `v1.0 (initial)` saying it was written retroactively.

## Mixed

Partly built is the usual case (UI on a fake repository, data layer pending): retroactive for the
done parts, prospective for the rest, in the same files.
