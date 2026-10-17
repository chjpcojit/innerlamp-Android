# Requirements — LightLamp

Version: 1.0
Last updated: 2026-10-17
Status: not started — nothing of the lamp exists in the app: no Thắp đèn screen, no garden, no route, no domain/data code. The centre lamp button of the bottom nav is wired to the meditation setup (`SetupRoute`), which product decision 2026-10-16 changes to Thắp đèn. The contract does not yet carry the v2.0 changes (optional `recipient`, `is_mine`), so the UI is built on a fake repository first.
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/lamp/` (new; the spec folder is `LightLamp`, product §1.5), `domain/lamp/`, `data/` and `ui/navigation/` + `MainActivity.kt` (centre button)
Product spec: innerlamp-be `docs/product/lamp/product.md` @ v1.0
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ info.version 2.1.1 (tag Lamps: `POST /v1/lamps`, `GET /v1/lamps`); backend feature spec innerlamp-be `docs/specs/features/lamp/requirements.md` @ v2.0 — its contract changes (recipient optional and nullable, `is_mine` on garden items) land in `openapi.yaml` as 3.0.0 only when the backend code ships and are **not** in 2.1.1.
Implements: BR-LMP-01, BR-LMP-02, BR-LMP-03, BR-LMP-04, BR-LMP-05, BR-LMP-06, BR-LMP-07, BR-LMP-08, BR-LMP-09, BR-LMP-10, BR-LMP-11, BR-LMP-12, BR-LMP-13, BR-LMP-14, FR-LMP-001, FR-LMP-002, FR-LMP-003, FR-LMP-004, FR-LMP-005, FR-LMP-006, FR-LMP-007, FR-LMP-008, FR-LMP-009, FR-LMP-010, FR-LMP-011, FR-LMP-012, FR-LMP-013, FR-LMP-014, NFR-LMP-03
Not on Android: NFR-LMP-01, NFR-LMP-02 (backend)

> See `changelog.md` in this folder for the version history of this spec.

Platform spec (layer 2) for lighting a lamp and the lamp garden on Android, written prospectively on
2026-10-17 from the product spec v1.0 and checked against the code read on the same day. The product
behavior — the closed sets, burn durations, the end moment, the limit, the recipient and prayer
rules, the double-submission window, the lighter's name, what other users see, garden order and
counts, the remaining-time label, sign-in — lives in the product spec and is **not** restated here;
this file says how the Android app applies those `BR-LMP-*` / `FR-LMP-*`.

## Constraints from the product spec

- **Scope of this version** (product §1.2): light a free lamp and browse the garden. "Thỉnh đèn tại chùa"
  (a temple lights a lamp for a fee), payment, a "my lamps" screen, expired-lamp history, put-out /
  extend / relight / edit, moderation and a filter by intention are not built (FR-LMP-014, BR-LMP-13).
- **Online only** (product §2.4): lighting and the garden need the server; no lamp is stored on the
  device and there is no offline copy (§3.7).
- **Time is the server's** (NFR-LMP-03, BR-LMP-03): the app never computes a lighting or end moment;
  the device clock only formats the end date (device time zone) and the remaining-time label.
- **The numbers belong to the service** (BR-LMP-05, BR-LMP-08, BR-LMP-11): the app does not embed 20
  lamps, 5 seconds or 12 per page, and does not send a `limit`; it only shows what the server answers.
  The text limits of BR-LMP-06 / BR-LMP-07 and the closed sets of BR-LMP-01 / BR-LMP-02 are also
  checked on the device, only to avoid a pointless request — the server remains the judge.
- **What the app may show about a lamp**: in the garden only the lighter's name (or "Của bạn"), lamp
  type, recipient and remaining time; never intention or prayer, even on the user's own lamps
  (BR-LMP-10). The user sees their own intention and prayer only on the confirmation after lighting.
- **Privacy** (product §3.7): recipient and prayer can hold a deceased relative's name; the app never
  writes them to logs.
- **Copy** is Vietnamese (quoted below with an English gloss where it is not obvious). English copy for
  the messages and for the anonymous label is not defined yet (product Appendix A).

## User Story

As a signed-in user, I want to light a lamp with a lamp type, an intention, an optional recipient, an
optional prayer and a burn duration, and to browse the garden of lit lamps, so that my wish stays
visible for the days I chose and I see the community practising with me.

## Acceptance Criteria (EARS notation)

### Entering the feature

1. WHEN the user taps the centre lamp button of the bottom nav, THE SYSTEM SHALL open `LampRoute` (Thắp đèn) with `launchSingleTop`, on every screen that shows the bottom nav, and SHALL NOT open the meditation setup. (FR-LMP-001)
2. WHEN the user looks for meditation, THE SYSTEM SHALL keep it reachable through the existing entries to `SetupRoute` on Home, Chùa and the talk screen, and through no lamp control. (FR-LMP-001)
3. WHEN `LampRoute` or `LampGardenRoute` is composed and no session is stored (guest), THE SYSTEM SHALL show a short introduction to the lamp and a "Đăng nhập để thắp đèn" (Log in to light a lamp) button that opens `LoginRoute`, and SHALL NOT show the form, the garden or any count and SHALL NOT call the backend. (FR-LMP-013, BR-LMP-14)

> ⚠️ Not confirmed (product): where the guest lands after signing in from the lamp introduction — the Login spec sends a successful sign-in to Home, so the user would have to tap the lamp button again; and whether the introduction also offers "Tạo tài khoản" like the shared invitation of the Login spec criterion 21 (FR-LMP-013 names only the sign-in button) — blocks criterion 3; default: lamp-specific introduction with the single sign-in button, landing on Home; hand back to ba-writer in innerlamp-be.

4. WHEN a lamp request returns `401` and the session cannot be refreshed, THE SYSTEM SHALL leave it to the app-wide behavior of the Login spec (criteria 10–11: clear the account data, open `LoginRoute` with "Phiên đăng nhập đã hết, vui lòng đăng nhập lại.") and SHALL NOT keep any lamp data on screen. A `401` for a guest is never a session end (Login criterion 12). (FR-LMP-013, BR-LMP-14)

### The light-a-lamp form

5. WHEN `LampRoute` opens for a signed-in user, THE SYSTEM SHALL show an unlit lamp and the form with the oil lamp, Cầu an (peace) and 7 days preselected, "Ẩn tên tôi" (Hide my name) off, empty recipient and prayer fields, the notice "Người khác sẽ thấy tên người nhận." (Others will see the recipient's name.) under the recipient, a "Thắp đèn" (Light a lamp) button and a way to open the garden. (FR-LMP-002, BR-LMP-10)
6. WHEN the user chooses a lamp type, an intention or a burn duration, THE SYSTEM SHALL select only that choice, offering exactly the three lamp types, four intentions and the durations 1, 7, 21 and 49 days, and SHALL change the lamp artwork to the chosen lamp type. (FR-LMP-002, BR-LMP-01, BR-LMP-02)
7. THE SYSTEM SHALL show no "Thỉnh đèn tại chùa", donation, payment or price control on any lamp screen. (FR-LMP-014)
8. WHEN the user taps "Thắp đèn" with a recipient or prayer that, after trimming both ends and Unicode NFC normalization, is longer than 100 or 300 characters respectively (counted as characters, not bytes), THE SYSTEM SHALL NOT call the backend, SHALL show "Thông tin chưa hợp lệ. Vui lòng kiểm tra lại người nhận và lời nguyện." (Invalid information. Please check the recipient and the prayer.) and SHALL keep what the user typed. A blank recipient or prayer is valid. (BR-LMP-06, BR-LMP-07, FR-LMP-003)
9. WHEN the user submits a valid form, THE SYSTEM SHALL call `POST /v1/lamps` with the lamp type, intention, burn duration, the trimmed recipient (left out when blank), the trimmed prayer (left out when blank) and the hide-name choice, show the lamp igniting, then the confirmation of criterion 11. (FR-LMP-003, BR-LMP-06, BR-LMP-07)

> ⚠️ Not confirmed: not in openapi.yaml yet (planned 3.0.0, be spec v2.0) — the optional / nullable `recipient`; contract 2.1.1 still requires a recipient — blocks lighting a lamp with a blank recipient (BR-LMP-06) against the real backend; the fake repository accepts it until the contract ships.

10. WHILE a lamp is being lit, THE SYSTEM SHALL ignore further taps on "Thắp đèn" (the button shows a progress state). The backend's 5-second window turns an identical resubmission into the same lamp and answers with the existing lamp; the app treats that answer exactly like a new lamp. (BR-LMP-08, FR-LMP-005, FR-LMP-003)
11. WHEN the backend lights the lamp, THE SYSTEM SHALL show the confirmation: the lit lamp artwork, its type, intention and number of days, the recipient or "Tất cả chúng sanh" (All sentient beings) when the response has none, the prayer when there is one, and until when it burns — the end moment from the response shown as date and time in the device's time zone — with exactly two actions, "Thắp thêm đèn" (Light another lamp) and "Vào vườn đèn" (Go to the lamp garden). (FR-LMP-003, FR-LMP-014, BR-LMP-03, BR-LMP-06, BR-LMP-10)
12. WHEN the user taps "Thắp thêm đèn", THE SYSTEM SHALL open an empty form with an unlit lamp, keeping the previous lamp type, intention and burn duration selected, with the recipient and the prayer empty. (FR-LMP-003)

> ⚠️ Not confirmed (product): whether "Ẩn tên tôi" keeps its last value or goes back to off after "Thắp thêm đèn" — FR-LMP-003 names only type, intention and duration; blocks criterion 12; default: back to off (the documented form default of FR-LMP-002).

13. WHEN the backend refuses because the user already has the maximum of lit lamps, THE SYSTEM SHALL show "Bạn đã có 20 ngọn đèn đang sáng, mức tối đa cho mỗi người. Hãy thắp thêm khi có đèn tắt." (You already have 20 lit lamps, the maximum per person. Light more when some go out.), add no lamp and keep what the user typed. (FR-LMP-004, BR-LMP-05)
14. WHEN lighting fails for any other reason — the backend rejects the input, there is no connection, a timeout, a server error — THE SYSTEM SHALL keep what the user typed, add no lamp, store nothing on the device and show, for a rejected input, the invalid-input message of criterion 8, otherwise "Chưa thắp được đèn. Vui lòng thử lại." (The lamp could not be lit. Please try again.). (FR-LMP-003, product §3.7)
15. WHEN the screen is recreated (rotation, process death) before the lamp is lit, THE SYSTEM SHALL restore the choices and the typed recipient and prayer; the confirmation and the lit lamp are never persisted on the device. (Android)

### The garden

16. WHEN the user taps "Vào vườn đèn" or the garden entry of the form, THE SYSTEM SHALL open `LampGardenRoute`, show a loading state and call `GET /v1/lamps` with no filter and no cursor. The app sends no `limit`. (FR-LMP-007, BR-LMP-11)
17. WHEN the garden is loaded, THE SYSTEM SHALL show one card per lit lamp in the order received (most recently lit first), each with the lamp artwork of its type, the lamp type name, the lighter's name as received, the recipient or "Tất cả chúng sanh" when the item has none, and the remaining-time label; and SHALL show neither intention nor prayer on any card. (FR-LMP-007, BR-LMP-09, BR-LMP-10, BR-LMP-11)
18. WHEN a garden item is marked as the signed-in user's own, THE SYSTEM SHALL highlight its card and show "Của bạn" (Yours) in place of the lighter's name, anonymous lamps included, and SHALL NOT derive the mark from anything stored on the device. (FR-LMP-008, BR-LMP-10)

> ⚠️ Not confirmed: not in openapi.yaml yet (planned 3.0.0, be spec v2.0) — `is_mine` on garden items; blocks criterion 18 against the real backend; the fake repository marks some items as own until the contract ships.

19. WHEN the garden is shown, THE SYSTEM SHALL show "N ngọn đèn đang sáng" (N lamps burning) with N the total from the response, and a chip "Tất cả" (All) plus one chip per lamp type with that type's count from the response, 0 included; the numbers are taken from the response as they are, with no sample lamps and no base number added. (FR-LMP-011, BR-LMP-11)
20. WHEN the user chooses a lamp type chip, THE SYSTEM SHALL cancel the request in flight, clear the list and call `GET /v1/lamps` with that lamp type from the first page; WHEN the user chooses "Tất cả", the same without a filter. The header total and the chip counts SHALL NOT change because of the filter. The chosen chip survives recreation. (FR-LMP-009, BR-LMP-11)
21. WHEN the response says more lamps exist, THE SYSTEM SHALL show "Xem thêm đèn" (See more lamps) at the end of the list; WHEN the user taps it, THE SYSTEM SHALL call `GET /v1/lamps` with the cursor the previous response returned (an opaque value the app never builds or reads) and append the page, ignoring taps while a page is loading and dropping an item whose id is already listed; WHEN no more lamps exist, THE SYSTEM SHALL NOT show the action. (FR-LMP-010, BR-LMP-11)
22. WHEN loading the next page fails, THE SYSTEM SHALL keep the lamps already shown, show the garden error message under them with a retry and keep the cursor. (FR-LMP-012, FR-LMP-010)
23. WHEN no lit lamp matches, THE SYSTEM SHALL show "Vườn đèn còn trống" (The lamp garden is empty) with "Thắp một ngọn đèn để nó sáng ở đây." (Light a lamp so it shines here.) and a button that opens the form. (FR-LMP-012, FR-LMP-007)

> ⚠️ Not confirmed (product): the copy of the empty state when only the chosen filter is empty — FR-LMP-009 says the list is empty and the counts still show every type, FR-LMP-012 defines the empty copy only for an empty garden; blocks criterion 23 under a filter; default: the same empty state under the chips.

24. WHEN the garden cannot be loaded, THE SYSTEM SHALL show "Chưa tải được vườn đèn. Vui lòng thử lại sau." (The lamp garden could not be loaded. Please try again later.) with a retry, and SHALL keep a way to open the lighting form. (FR-LMP-012)
25. THE SYSTEM SHALL offer on the garden a way back to the lighting form. (FR-LMP-002)

### Remaining time and the end of a lamp

26. WHEN a card is drawn, THE SYSTEM SHALL compute its label from the item's end moment and the current moment: "Còn N ngày" (N days left) with N the remaining whole days rounded up when 24 hours or more remain, "Còn dưới 1 ngày" (Less than 1 day left) when less remains, and SHALL NOT show a card whose time has run out; it SHALL recompute the labels when the screen returns to the foreground. (BR-LMP-12, BR-LMP-04, FR-LMP-006)
27. THE SYSTEM SHALL NOT use the device clock to decide anything but that label and the format of the end date: which lamps are lit, the totals and the counts are always what the server last answered. (NFR-LMP-03, BR-LMP-03, FR-LMP-006)
28. THE SYSTEM SHALL offer no action to put out, edit, extend, relight or delete a lamp, and no "my lamps" screen. (BR-LMP-13)

### Cross-cutting

29. THE SYSTEM SHALL send both lamp requests as authenticated calls through `NetworkClient.safeCall` and SHALL show only app-owned wording, never the backend's `error.message`. (BR-LMP-14, Android)
30. WHEN the app logs requests or errors (including `LoggingInterceptor` in debug builds), THE SYSTEM SHALL NOT log the bodies of `/v1/lamps` calls or the recipient and prayer of any lamp. (product §3.7, Android)
31. THE SYSTEM SHALL run every case of the shared vector files `lamp-remaining-label.json` (BR-LMP-12) and `lamp-input-validation.json` (BR-LMP-01, BR-LMP-02, BR-LMP-06, BR-LMP-07) in JVM unit tests, loaded from innerlamp-be `docs/product/lamp/rules/`. `lamp-lifetime.json` and `lamp-lighter-name.json` describe what the backend decides; the app only displays the end moment and the name it receives and does not implement those rules. (BR-LMP-01, BR-LMP-02, BR-LMP-06, BR-LMP-07, BR-LMP-12)

## Data & API

| Action | Operation (openapi) | Auth | Stored locally | Notes |
|---|---|---|---|---|
| Light a lamp | `POST /v1/lamps` | Bearer (`@Authenticated`) | none | `201` for a new lamp, `200` for the identical lamp lit within the dedupe window — both mean "lit". Recipient optional: ⚠️ Not confirmed: not in openapi.yaml yet (planned 3.0.0, be spec v2.0) |
| Browse the garden | `GET /v1/lamps` (optional lamp type filter, cursor) | Bearer (`@Authenticated`) | none | Returns the page, the total and the per-type counts from one moment, a "more" flag and the next cursor. `is_mine` on items and a nullable recipient: ⚠️ Not confirmed: not in openapi.yaml yet (planned 3.0.0, be spec v2.0) |

Request/response shapes: see the contract in the header and the backend feature spec. This file does
not copy schemas.

## Error → UI mapping

| Operation | Failure case | User sees | Code status |
|---|---|---|---|
| `POST /v1/lamps` | Lamp limit reached (409) | Limit message (criterion 13); form kept | missing |
| `POST /v1/lamps` | Input rejected (400 validation) | "Thông tin chưa hợp lệ. Vui lòng kiểm tra lại người nhận và lời nguyện."; form kept | missing |
| `POST /v1/lamps` | `AppError.NoConnection` / `Timeout` / 5xx / any other | "Chưa thắp được đèn. Vui lòng thử lại."; form kept | missing |
| `POST /v1/lamps`, `GET /v1/lamps` | `401` and the refresh is rejected | Session ended (criterion 4, Login spec) | missing |
| `GET /v1/lamps` | `AppError.NoConnection` / `Timeout` / 5xx | "Chưa tải được vườn đèn. Vui lòng thử lại sau." with a retry; lighting stays available | missing |
| `GET /v1/lamps` | Rejected filter, limit or cursor (400) | Same garden error; the retry reloads the first page | missing |

## Out of scope

- "Thỉnh đèn tại chùa", payment, donation (the future `offering` feature, FR-LMP-014).
- A "my lamps" screen, expired-lamp history, notifications such as "your lamp is about to go out".
- Putting out, extending, relighting or editing a lamp; moderation; a filter by intention.
- Sharing a lit lamp (product Appendix A, unconfirmed — nothing is built until decided).
- How long expired lamps are kept and the garden response-time target (backend; NFR-LMP-01 is not on Android).
- Lamp artwork and animation as an asset task: the prototype has no lamp screens (see design.md "Deliberately stubbed").
- The shared sign-in invitation (`AuthGate`) and the session-ended behavior — owned by `../Login/`.

## Pending (not yet implemented)

Everything above: `tasks.md` Phases 1–5. The bottom-nav change of criterion 1 is `tasks.md` Phase 2 and
also removes the lamp line of `../Navigation/` (Phase 5).

## Open questions (resolved)

- ~~Does the centre button open meditation or Thắp đèn?~~ → Thắp đèn; meditation stays reachable from Home and the meditation area. Resolved 2026-10-16 (product, FR-LMP-001).
- ~~Android folder name?~~ → `Docs/Specs/LightLamp`. Resolved 2026-10-16 (product §1.5).
- ~~Can guests see the garden? Hide-name switch? Own lamps? Sample lamps? Expired lamps?~~ → Product decisions of 2026-10-16 (product Appendix A "resolved"); applied in criteria 3, 5, 18, 19.

> ⚠️ Not confirmed (product): English copy for every message and the English name of the anonymous label (product Appendix A) — blocks the English locale; the app has Vietnamese copy only until decided.

> ⚠️ Not confirmed (product, backend): how a client localizes the lighter's name when the server returns the anonymous label as literal Vietnamese text ("Vô danh") — blocks an English locale; until then the app shows the received name as it is.

> ⚠️ Not confirmed (design): the format of the end date and time on the confirmation (the product only fixes the device time zone) — blocks criterion 11's test; default: day/month/year then 24-hour time, e.g. "18/10/2026 01:00".
