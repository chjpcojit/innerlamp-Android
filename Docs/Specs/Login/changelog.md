# Changelog — Login Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.2 (2026-10-16)

- Aligned with the backend's move to email auth: contract is now `openapi.yaml` @ 2.1.1 (Auth tag) and
  backend feature spec login v2.5; product spec cited at @ v1.4 (v1.4 only changed the §4.2 Backend
  column, no requirement). Mobile never sends `X-API-Key` / `X-InnerLamp-Client-IP`.
- `requirements.md`: criterion 3 names the device fields (`device_model` required, `platform` optional but always sent, `device_name`
  omitted); criterion 4 notes an unverified email gets the same `INVALID_CREDENTIALS`; criterion 10 adds the
  per-request session check (old access token → `401 UNAUTHORIZED` at once after sign-out / password change /
  re-sign-in on the same device) and that the refresh response has no `user`; new criterion 12a (`503
  SERVICE_UNAVAILABLE` keeps the session); criterion 13 ignores `401` on logout; criteria 15–18 add the ~1.5 s
  reset-request latency, `confirm` returning `200 {success: true}` with no token, revoking every session and
  lifting `LOGIN_LOCKED`, and `OTP_EXPIRED` for an unknown email; criterion 22 takes the wait from
  `Retry-After` / `details.retry_after_seconds`. Error table gains `503` and `VALIDATION_ERROR`.
- `design.md`: server-side session check paragraph; refresh keeps the stored user; DTO notes; Sign-in,
  Renewal and Forgot-password flows updated; "Deliberately stubbed" now says the backend changes are
  uncommitted and untested against a real server.
- `tasks.md`: matching items (no `device_name`, no `X-API-Key`, 503 handling, refresh DTO without `user`, reset
  loading state, manual QA only after the backend ships).
- Language policy (2026-10-15): the three files were rewritten in English in this change.
- Backend review (2026-10-16): no spec error found; added refresh `429 RATE_LIMITED` as a transient error that keeps the session (criterion 10, Renewal Step 2).
- Hand-off: product — none. Backend — none (the `device.platform` inconsistency was fixed in openapi 2.1.1; `device_name` need not be sent).

## v1.1 (2026-10-15)

- Old-doc cleanup: dropped row #5 "Vào xem trước" from `Docs/home_assumptions.md` (guests now follow
  criteria 20–21); `Docs/Specs/Networking/` v2.4 and `Docs/Specs/Storage/` v1.2 point `AuthTokenStorage`
  to this spec. `tasks.md` Phase 7 ticks that item. `requirements.md`/`design.md` unchanged apart from the version.

## v1.0 (2026-10-14)

- First Android spec for sign-in, session, sign-out, forgot password and guest mode, written
  prospectively from the product spec innerlamp-be `docs/product/login/product.md` @ v1.2 (ADR-0003
  Accepted) and backend login spec v2.2. Docs only, no code change.
- Also the `AuthTokenStorage` spec that `Docs/Specs/Networking/` and `Docs/Specs/Storage/` deferred:
  tokens in `SecureStorage` `DEVICE_UNLOCKED`, `SessionTokenCache` replacing `InMemoryAuthTokenProvider`,
  single-flight refresh through an OkHttp `Authenticator`.
- `requirements.md`: criteria 1–23 tagged with product IDs; "Current drift" table (phone-number Login and
  simulated, no session storage / refresh / sign-out / forgot password, "Vào xem trước" as signed in).
- `design.md`: shared `AuthSessionStore` + `AccountDataCleaner`; Step-by-step Flows for sign-in, renewal and
  end of session, sign-out, forgot password.
- `tasks.md`: Phase 1–7; `[x]` only for `SecureStorage` and the Bearer part of `AuthInterceptor`.
- Decisions by anh Đức (2026-10-14), applied as on iOS: an ended session shows "Phiên đăng nhập đã hết, vui
  lòng đăng nhập lại." and needs new UI (settle the design first). Claude's default, reviewed in the PR: the
  "Đăng xuất" button at the bottom of the Profile screen.
- Hand-off: product — none. Backend — none.
- Follow-up outside the folder: `Docs/Specs/Networking/` and `Docs/Specs/Storage/` still list
  `AuthTokenStorage` as "not created" — change them to point to `Docs/Specs/Login/` in the old-doc cleanup.
