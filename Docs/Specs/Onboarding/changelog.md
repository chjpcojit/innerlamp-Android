# Changelog — Onboarding Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.1 (2026-10-16)

- Aligned with the backend's move to email registration: contract is now `openapi.yaml` @ 2.1.1 (Auth tag) and
  backend feature spec onboarding v8.3; product spec cited at @ v1.6 (v1.5 only changed the §4.2 Backend column; v1.6 defines the
  `REGISTRATION_IN_PROGRESS` screen, resolved by anh Đức). Mobile never sends `X-API-Key` / `X-InnerLamp-Client-IP`.
- `requirements.md`: criterion 13 states `phone_number` is the digits as typed (no `+`/country code; the server
  normalises to E.164); criterion 14 adds that `verify-otp` for an existing email always returns `OTP_INVALID`;
  new criterion 15a (`409 REGISTRATION_IN_PROGRESS` → stay, show `details.retry_after_seconds`); criterion 21
  names the `201 AuthResult`; criterion 23 sends `{email}` only and may ignore `resend_remaining`; criterion 27
  takes the wait from `Retry-After` / `details.retry_after_seconds`. Error table gains `REGISTRATION_IN_PROGRESS`,
  `INVALID_TEMPLE` / `PHONE_TAKEN` at `verify-otp`, `EMAIL_SEND_FAILED` (no code counted).
- `design.md`: Registration flow Steps 1 and 4 handle the new cases; DTO notes; "Deliberately stubbed" says the
  backend changes are uncommitted and untested against a real server.
- `tasks.md`: matching items; manual QA only after the backend ships.
- Language policy (2026-10-15): the three files were rewritten in English in this change.
- Backend review (2026-10-16): no spec error found. Added two notes from it: refresh `429 RATE_LIMITED` is a transient error that keeps the session (Login); after `EMAIL_SEND_FAILED` a changed resubmit can hit `REGISTRATION_IN_PROGRESS` (Onboarding 15a).
- Hand-off: product — none. Backend — none. Criterion 15a is no longer "Not confirmed" (product v1.6).

## v1.0 (2026-10-14)

- First Android spec for splash, intro, registration, email code and temple proposal, written
  prospectively from the product spec innerlamp-be `docs/product/onboarding/product.md` @ v1.2
  (ADR-0003 Accepted: email + code sent by email, SMS removed) and backend onboarding spec v8.0. Docs
  only, no code change.
- `requirements.md`: header `Product spec` / `Contract` / `Architecture` / `Implements`; criteria 1–29
  tagged with product IDs; "Current drift" table of the code on 2026-10-14 (splash always goes to the intro,
  choices only in memory, no full name, place of ordination is free text, loose email/phone checks, simulated
  success, no code screen).
- `design.md`: layers table with a Status column; Step-by-step Flows "App launch" and "Registration" (Steps
  1–6); one `SignupViewModel` shared by Signup / temple picker / temple proposal / Otp; session stored through
  `AuthSessionStore` from `../Login/`.
- `tasks.md`: Phase 1–7; `[x]` only for what was read in code and matches (12 countries, strength hint,
  `onboarding_completed` flag, 3-step UI, dharma-name "Gợi ý", temple and administrative-unit repositories).
- Decisions by anh Đức (2026-10-14), applied as on iOS: terms later (keep static content, criterion 12); first
  launch → intro → set the flag → Signup, later launches → Login; the temple proposal goes through the existing
  public route.
- Hand-off: product — the "one text" part of BR-ONB-11 is later on Android (tell `ba-writer` when done).
  Backend — none.
