---
name: il-spec-android
description: This skill should be used when the user wants a written spec (requirements, design, or task breakdown) for an InnerLamp Android feature — the Kotlin/Compose platform spec (layer 2), which starts from the cross-platform product spec innerlamp-be `docs/product/<feature>/product.md` (written by `ba-writer` in the backend repo) and the backend contract innerlamp-be `docs/specs/api/openapi.yaml`, citing both instead of copying them — before, during, or retroactively after building it. It covers the convention under `Docs/Specs/<Feature>/` (requirements.md, design.md, tasks.md) plus a changelog.md: a header citing the product spec version and the BR-/FR- IDs it implements, EARS acceptance criteria each tagged with its product ID, an error → UI mapping table, a design that follows this repo's clean architecture (`ui → domain ← data → core/{network,storage}`, manual DI in `di/`, `NetworkClient.safeCall`, `DispatcherProvider`), a mandatory Step-by-step Flow for any flow with 2+ sequential backend calls, a strict one-way cascade (requirements → design → tasks), version headers + changelog entries on every update, and the rule that any unanswered question is recorded inline as "⚠️ Not confirmed" — never guessed. Docs-only: it never writes Kotlin (that is `il-ui-builder` and `il-data-builder`), never decides product questions (hand back to `ba-writer`), and never writes to `Docs/Knowledge/` (OKF, `il-okf-writer`).
---

# InnerLamp Android Spec Writer (`il-spec-android`)

## Overview

Generates and maintains the spec for one Android feature under `Docs/Specs/<Feature>/`
(`<Feature>` = PascalCase of the product spec's kebab-case slug, e.g. `memorial` → `Memorial`;
infrastructure specs such as `Networking`, `Storage`, `Navigation` keep their own names):

- `requirements.md` — **what** the app must do for the user: user story, EARS acceptance criteria
  tagged with product IDs, the error → UI mapping, out of scope, pending, open questions.
- `design.md` — **how** it is built here: screen + ViewModel + UI state, route, use cases and
  repository interface, data layer (API service, DTO, mapper, Room, storage), DI wiring, and a
  Step-by-step Flow for multi-call flows.
- `tasks.md` — **what to do, in order**, broken down from `design.md`'s Layers & components table,
  numbered Phases with checkboxes (`[ ]` pending / `[x]` verified in code).
- `changelog.md` — one shared version history for the three files above.

`Docs/Specs/Networking/` and `Docs/Specs/Storage/` are the worked examples for shape (English
headings, layer tables with full paths). Read one before writing; their prose may still be
Vietnamese — new and edited specs are English (see Language below).

These files are **layer 2** of InnerLamp's documentation stack. Above them sit the cross-platform
product spec in the backend repo, innerlamp-be `docs/product/<feature>/product.md` (layer 0, owned
by `ba-writer`, which exists only in that repo), and the API contract innerlamp-be
`docs/specs/api/openapi.yaml` (layer 1, owned by the backend). The backend, website and iOS repos
have their own layer-2 spec skills (`il-spec-be`, `il-spec-web`, `il-spec-ios`) citing the same
documents. Read `references/upstream_product_spec.md` once — it is the shared rulebook.

**Language.** Language follows the repo's Language policy in CLAUDE.md, which overrides this skill.
Everything written into the spec files is English: headings, EARS keywords (`WHEN` /
`THE SYSTEM SHALL`), table headers, version headers, prose, user story and notes. User-facing copy
quoted in a spec keeps its own language (Vietnamese first) with an English gloss, e.g.
"Đăng nhập" (Log in). Questions and reports to the user in chat are Vietnamese ("anh"/"em",
technical terms in English). Editing an existing Vietnamese spec rewrites the file in English in the
same change.

**This skill never writes code** and never touches `app/`, `Docs/Knowledge/` or the backend repo.

## Hard rules that override everything else below

1. **Start from the product spec, cite it, never restate or decide it.** A new feature needs
   innerlamp-be `docs/product/<feature>/product.md` first — if there is none, stop and send the user
   to `ba-writer` in innerlamp-be. `requirements.md` carries the `Product spec:` / `Contract:` /
   `Implements:` header; every EARS criterion ends with the `FR-*`/`BR-*` it implements or
   `(platform)`; a product gap or ambiguity becomes `⚠️ Not confirmed (product)` handed back to
   `ba-writer`. Legacy features with no product spec may be updated with
   `Product spec: none yet (legacy)`. See `references/upstream_product_spec.md`.
2. **`Docs/*_assumptions.md` holds Android-only assumptions, never product rules.** Those files
   (e.g. `Docs/home_assumptions.md`) record build-time choices (in-memory seed data, a Material3
   stand-in for a prototype control). A business rule found there (a date rule, an anniversary
   count) is a product question: cite the BR if the product spec has it, otherwise
   `⚠️ Not confirmed (product)` and hand it to `ba-writer` — never copy it into this spec as fact.
3. **Contract is cited, never copied.** Operations are named by method + path and the `Contract:`
   line records the `openapi.yaml` version used. No request/response schemas, DTO field lists or
   error-code lists are pasted in; the error → UI table names only what the user sees for each case.
   An operation not in `openapi.yaml` is `⚠️ Not confirmed` and a hand-off to the backend, never an
   assumed shape.
4. **Backend work is not tracked here.** Something the backend must change goes to the report as a
   hand-off item (and, if it is a product question, to `ba-writer`) — never as a backend task list
   inside this repo's spec.
5. **The design follows this repo's architecture (`AGENTS.md`).** `domain` is pure Kotlin and owns
   repository interfaces and models; `data` implements them; every network call goes through
   `NetworkClient.safeCall` (`Docs/Specs/Networking/`); tokens and secrets live in `core/storage`
   secure storage (`Docs/Specs/Storage/`); structured data goes to Room; DI is manual (`di/`, no
   Hilt/Koin); threads switch via injected `DispatcherProvider`, never `Dispatchers.IO`; durations use
   elapsed time. A design that breaks one of these is wrong, not a variant.
6. **One-way cascade: `requirements.md` → `design.md` → `tasks.md`.** A requirement changes first,
   then its design, then its tasks. Never edit `tasks.md` from a requirement change without updating
   `design.md` first.
7. **Never mark a task `[x]` unless it is verified in the code** (read the file). Writing the spec is
   not implementing it.
8. **Unanswered questions are recorded, never skipped or guessed**, inline where they bite:
   ```
   > ⚠️ Not confirmed: <the question, in one sentence> — <what it blocks>.
   ```
   (`⚠️ Not confirmed (product): …` when it is a product decision — rule 1.) When answered, move it to
   "Open questions (resolved)" with date and resolution, and check what it changes downstream.
9. **Every update bumps `Version`/`Last updated` in all three files and adds a `changelog.md`
   entry.** See `references/versioning_and_changelog.md`.
10. **A Step-by-step Flow in `design.md` is mandatory when the flow makes 2+ sequential backend calls
    or side effects** (login → save tokens securely → navigate; 401 → refresh → retry). See
    `references/step_by_step_flow.md`.
11. **Questions are short, concrete, one topic each, with a suggested default.** See
    `references/gathering_questions.md`.

## Reference map

| Reference | Covers |
|---|---|
| `references/upstream_product_spec.md` | Layer 0/1/2 rules shared by all four platform spec skills: finding the product spec, the header, ID tags, what a platform spec may not say, hand-back to `ba-writer`, test vectors |
| `references/modes.md` | Prospective vs Retroactive, and which Android files to read in retroactive mode |
| `references/gathering_questions.md` | The Android-specific questions left after reading the product spec |
| `references/ears_notation.md` | EARS phrasing for Android criteria, with ID tags |
| `references/step_by_step_flow.md` | The per-step shape for flows from ViewModel through use case and repository to the backend |
| `references/versioning_and_changelog.md` | Version bump rule, header format, changelog entries |

## Workflow

### 0. Read the product spec and the contract

Per `references/upstream_product_spec.md`: open innerlamp-be `docs/product/<feature>/product.md`
(usually `../innerlamp-be`), note its version, the IDs whose Android column is not `n/a`, §3.6 rows
naming Android, open `⚠️` items naming Android, and `rules/*.json` vectors for BRs the app computes
locally (e.g. lunar-date rules in `domain/calendar/`). Open innerlamp-be `docs/specs/api/openapi.yaml`
for the operations the feature calls. No product spec and a new feature → stop and send the user to
`ba-writer`.

### 1. Gather the Android decisions

Ask only what the product spec does not answer (`references/gathering_questions.md`): mode, route
and entry point, auth (public / needs a session), online vs offline-first, which operations and
their contract status, what is stored locally (Room / prefs / secure), platform features
(notifications, photo picker, biometrics, permissions), and whether 2+ sequential calls happen.

### 2. Populate the files

Copy `assets/templates/*` to `Docs/Specs/<Feature>/` (replace `{{Feature}}` / `{{feature}}`) and
fill them:

| Template | Key sections |
|---|---|
| `requirements.md.template` | Header (product spec / contract / implements), User Story, EARS criteria with tags, Data & API (operations cited), Error → UI mapping, Out of scope, Pending, Open questions |
| `design.md.template` | Overview, Layers & components (with status column), Navigation flow, Step-by-step Flow, Local storage, Deliberately stubbed |
| `tasks.md.template` | Phases from the Layers table: domain → UI (`il-ui-builder`) → data (`il-data-builder`, contract-gated) → tests → docs |
| `changelog.md.template` | `v1.0 (initial)` |

**Retroactive**: read the code first (`references/modes.md`) and mark `[x]` only what you verified.
**Prospective**: every task `[ ]`; unconfirmed operations are "stubbed until the contract exists"
(fake repository in `il-ui-builder`, real one later in `il-data-builder`).

### 3. Updating an existing spec

1. Find where the change originates (behavior → `requirements.md`; technical detail →
   `design.md`). If the product spec changed, start from its changelog's affected IDs.
2. Cascade requirements → design → tasks, and say explicitly which files did not need to change.
3. Bump the version in all three headers and add a changelog entry.
4. Resolving a `⚠️` moves it to "Open questions (resolved)" and states what it changed downstream.
5. List hand-offs: product questions → `ba-writer`; contract changes → backend; shipped behavior →
   `il-okf-writer`.

### 4. Verify before reporting done

- Header present (or `Product spec: none yet (legacy)`); every criterion EARS-phrased and tagged;
  every product ID in scope for Android appears on at least one criterion.
- No product rule restated or decided, none imported from `Docs/*_assumptions.md`; no schema or error
  list copied; no backend task list.
- Design keeps `domain` pure, network through `safeCall`, secrets in secure storage, DI manual.
- Every task traces to a Layers row or a Step; no `[x]` without reading the code.
- Every computational BR the app implements has a task to run its vector file.
- Versions match across the three files; the changelog has the new entry, newest first.
- Report lists hand-offs and any `⚠️` still open.
