# Gathering questions

## Read the product spec before asking

Read innerlamp-be `docs/product/<feature>/product.md` first (see `upstream_product_spec.md`). Do not
ask again what it already answers (user story, business rules, scope, edge cases) — cite the ID. A
product decision it is missing is not settled here: record `⚠️ Not confirmed (product)` and hand it
back to `ba-writer`. What is left is Android-specific.

## Phrasing

Vietnamese, "anh"/"em", technical terms in English. One topic per question, concrete, with a
suggested default the user can just confirm. One line when possible.

## Questions for a new spec

1. **Feature name** — PascalCase of the product spec's slug (`memorial` → `Memorial`). Confirm, don't
   invent.
2. **Mode** — already in the app (Retroactive) or not built yet (Prospective)?
3. **Entry point** — which route/tab reaches the screen (bottom nav, Home, Settings…)? Any route
   arguments?
4. **Auth** — usable signed out, or needs a session? What happens to a guest (send to Login —
   default — or a read-only view)?
5. **Online vs offline-first** — read from the backend each time, or cache in Room and sync?
6. **Operations** — which backend operations, and is each in innerlamp-be `openapi.yaml`? Missing
   ones become `⚠️ Not confirmed`, a backend hand-off, and a fake repository meanwhile.
7. **Local storage** — what is kept on the device: Room (structured), prefs (light settings), secure
   storage (tokens/secrets)?
8. **Platform features** — notifications (WorkManager/AlarmManager, `POST_NOTIFICATIONS`), photo
   picker, biometrics, other runtime permissions?
9. **Multi-call flow** — 2+ sequential backend calls or side effects? If yes, Step-by-step Flow.

## Updating an existing spec

1. What changed — a product spec version bump (which IDs?), a contract change, or an Android-only
   change?
2. If it answers a `⚠️`: the answer, plainly.
3. Does it change an existing criterion, or add one?

## Never skip

Unanswered → inline `⚠️ Not confirmed` (or `(product)`) where it bites, never a silent default,
never asked twice in one session.
