# Changelog — LightLamp Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.0.1 (2026-10-17)

- `design.md`: the Navigation spec was updated (Navigation v1.1), so the "Navigation still says" note and the Navigation items of "Legacy to clean up" are resolved. `requirements.md`, `tasks.md`: no change needed, verified.

## v1.0 (initial)

- Android layer-2 spec of lighting a lamp and the lamp garden, written prospectively on 2026-10-17 from
  innerlamp-be product spec `docs/product/lamp/product.md` v1.0 (feature code `LMP`) and the backend
  feature spec `docs/specs/features/lamp/requirements.md` v2.0, checked against the Android code read on
  the same day. Contract cited: `openapi.yaml` info.version 2.1.1 (tag Lamps); the v2.0 changes (optional
  and nullable `recipient`, `is_mine`) are not in it yet (planned 3.0.0) and are marked
  `⚠️ Not confirmed` wherever they bite.
- Scope: the light-a-lamp screen with its confirmation, the garden, the guest introduction and the
  centre bottom-nav button. Implements BR-LMP-01…14, FR-LMP-001…014 and NFR-LMP-03; NFR-LMP-01 and
  NFR-LMP-02 are backend only.
- Current state: nothing of the lamp exists in the app; the centre button opens the meditation setup
  (`SetupRoute`), which this spec changes to Thắp đèn. All product §4.2 Android cells are `planned`
  (FR-LMP-001 currently reads "Android spec says the center button opens meditation"). Every task is `[ ]`.
- Design choices: online only, no local storage; both calls `@Authenticated` through `safeCall`; the app
  sends no page size and embeds none of the service's numbers; the input check, the remaining-time label
  and the end-date format are pure Kotlin in `domain/lamp/`; UI on a fake repository until openapi 3.0.0.
- Vectors: `lamp-remaining-label.json` and `lamp-input-validation.json` run in JVM unit tests;
  `lamp-lifetime.json` and `lamp-lighter-name.json` are backend rules the app only displays.
- Hand-offs: to `ba-writer` in innerlamp-be — where a guest lands after signing in from the lamp
  introduction (and whether it offers "Tạo tài khoản"), whether "Ẩn tên tôi" resets after "Thắp thêm đèn",
  the empty-state copy under a filter, how a client localizes the literal anonymous label, and the
  §4.2 FR-LMP-001 Android note; to the backend — the 3.0.0 contract (optional recipient, `is_mine`);
  to design — the Thắp đèn screens and the lamp artwork; `../Navigation/` and the signup legal text carry
  the superseded lamp-button / prayer-privacy lines listed in `design.md` "Legacy to clean up".
