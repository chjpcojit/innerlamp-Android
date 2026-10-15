# EARS notation for requirements.md

Every acceptance criterion names a trigger and a mandatory system response:

```
WHEN <trigger>, THE SYSTEM SHALL <behavior>. (<product ID>)
```

Forms used here (see `Docs/Specs/Networking/requirements.md`):

- `WHEN <event>, THE SYSTEM SHALL <behavior>.`
- `WHEN <event>, THE SYSTEM SHALL <behavior>, then:` with `- on success: …` / `- on failure: …`
  branches, for submit-and-wait flows.
- `WHEN <state holds>, THE SYSTEM SHALL …` for conditions ("WHEN there is no network", "WHEN the
  access token has expired").

## Examples

Bad: "The login screen handles errors."

Good:

> WHEN the user taps "Đăng nhập" (Log in) with valid input, THE SYSTEM SHALL call
> `POST /v1/auth/login`, then:
>
> - on success: save the tokens to secure storage and navigate to Home;
> - on failure: stay on the Login screen, show the message from the Error → UI table, and keep the
>   phone number. (FR-AUTH-002)

Spec prose is English; user-facing copy quoted in a criterion keeps its own language (Vietnamese
first) with an English gloss in parentheses.

## Rules

1. **One criterion = one trigger**; outcomes are branches of it.
2. **Always "THE SYSTEM SHALL"** — a testable obligation, not a description.
3. **Tag every criterion** with the product ID it implements — `(FR-MEM-002)`, `(BR-MEM-04)` — or
   `(platform)` for Android-only behavior (back navigation, permissions, notifications, process
   death, offline cache). See `upstream_product_spec.md`.
4. **Don't restate the rule** — "THE SYSTEM SHALL compute the next anniversary date per BR-MEM-03",
   not the rule's content.
5. **Name hidden steps** (token save, refresh, cache write, scheduling a notification) here; the
   _how_ goes in `design.md`'s Step-by-step Flow.
6. Out of scope / Pending / Open questions are plain prose, not EARS.
