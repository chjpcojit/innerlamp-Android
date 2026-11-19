# Requirements — Memorial (Android)

Version: 3.4
Last updated: 2026-11-17
Status: implemented on `feat/memorial-integration` (worktree `InnerLamp-memorial`); gates `assembleDevDebug`
and `lintDevDebug` run, unit tests written but not executed (AGENTS.md forbids agents running tests).

- Product spec: innerlamp-be `docs/product/memorial/product.md` @ v2.1
- Client contract: [`client-contract.md`](client-contract.md) — pinned 2026-11-16, openapi working tree
  `info.version 3.1.0` (HEAD `e5d4c9c` = 3.0.0, 5 CRUD routes deployed)
- AC source: `ac-memorial-clients-v1.md` v1 (AC IDs `MEM-API-*`, `MEM-IMG-*`, `MEM-RMD-*`, `MEM-AND-01`)
- Impact & risk: `impact.md` (P1c)
- Decisions: `technical-decisions.md` D1–D10 (local reminders D1, local-only web D2, minimal reminder
  payload D3, photo flag D4, server-first D5, one date module D6, id/date types D7, no `photo_url`
  persistence D8, no PII logs D9, scope D10)
- Tech debt: `Docs/Tech-Debt/Memorial/TD-MEM-CLIENT-01…05`

> Legacy AC numbers 1–39 of v2.1 stay valid as history; this version traces behaviour with the new
> `MEM-*` IDs and keeps the old number in brackets where it still maps 1:1.

## Constraints

- **The book belongs to the account.** Every read/write goes through the backend with the session's
  Bearer token; the device copy is scoped by `account.user_id` and never shown to another account
  (BR-MEM-01, AC35).
- **Server-first, read-only cache.** Create/update/delete require the network. The Room copy is replaced
  after every successful read or write and is only used to read when a read fails (D5, FR-MEM-010).
- **Dates are computed on the device** for display (next anniversary, days left, ordinal, lunar ↔ solar);
  `LunarCalendar` + `MemorialSchedule` remain the only conversion module (D6, BR-MEM-05…10).
- **Reminders are local, interim** (D1 deviation from ADR-0002): WorkManager one-time work per
  anniversary, channel `memorial_reminders`, `POST_NOTIFICATIONS` asked once on API 33+. The payload sent
  to the server is only `{days_before, time}` (D3). When server push ships, the DI provider changes to a
  no-op scheduler and `cancelAll()` runs once (D1 cutover).
- **Photos are live on dev/staging and flag-gated on prod**: `BuildConfig.MEMORIAL_PHOTO_ENABLED` is true
  for the dev and staging flavors (the routes are deployed) and false for prod until it is deployed there
  (D4). With the flag off the UI is hidden and no photo route is called. A new entry takes its photo after
  the first save, when it has a server id (MEM-IMG-08, two steps).
- **Privacy (G1 rework 2026-11-17)**: no name, note, `photo_url` or token in logs/analytics —
  `LoggingInterceptor` never logs `/v1/memorials*` (A1). `android:allowBackup="false"`, and the memorial
  and WorkManager databases are excluded from both cloud backup and device transfer (A2). WorkManager input
  carries no relationship or name; the worker reads the device copy and falls back to "người thân" (A3).
  Reminder notifications are `VISIBILITY_PRIVATE` and their PendingIntent is immutable with a validated
  positive id (A5, A6).

## Acceptance criteria (Android)

| AC | Behaviour | Where |
|---|---|---|
| MEM-API-01 [1] | Opening the book issues exactly one `GET /v1/memorials`; on success the device copy is replaced and the list re-sorted by days left. | `MemorialRepositoryImpl.load`, `GioViewModel.refresh` |
| MEM-API-03 [new] | While the first load runs the screen shows "Đang tải sổ giỗ…", never the empty state. | `GioContent` (`GioStatus.Loading`) |
| MEM-API-04 [6] | Empty book: illustration + three benefits; no "Lấy từ gia phả" row. | `EmptyBook` |
| MEM-API-05 [15] | `POST` sends the mapped date form, reminder `{days_before, time}`, never `timezone`/`add_to_os_reminder`; a double tap cannot send twice (`saving` lock). | `MemorialRequestDto`, `GioViewModel.save` |
| MEM-API-06 [16] | `PUT` replaces the whole document; 404 removes the entry from the list and copy, closes the form with "Mục này không còn trong sổ giỗ." | `MemorialRepositoryImpl.write`, `handleSaveFailure` |
| MEM-API-07 [19] | Delete asks for confirmation, then `DELETE`; a 404 counts as deleted. | `GioEditScreen` dialog, `confirmDelete` |
| MEM-API-08 [new] | An entry of another account is a plain 404 → "mục không còn", never a different message. | `MemorialError.NotFound` |
| MEM-API-09 [new] | A book with 100 entries blocks "Thêm" before the request; a server 409 shows "Sổ giỗ đã đủ 100 người thân." and keeps the form. | `MAX_ENTRIES`, `LimitReached` |
| MEM-API-10 [new] | 401 on load clears the account copy and shows the guest state. 401 during a save or delete **keeps the form and everything typed**, shows "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại." with an explicit "Đăng nhập lại" action; nothing is cleared until the user taps it or a real sign-out occurs. | `handleSaveFailure`, `confirmDelete`, `SessionExpiredNotice` |
| MEM-API-11 [32] | Load failure with a device copy: the copy is shown, with "Đang hiện bản lưu trên máy." and reminders re-placed from it. | `MemorialLoad.Offline` |
| MEM-API-12 [34] | Write failure on network: form kept, "Cần kết nối mạng để lưu thay đổi.", book and copy unchanged. | `MemorialError.Offline` |
| MEM-API-13 [33] | Load failure with no copy: "Không tải được sổ giỗ" + retry. | `GioStatus.Failed` |
| MEM-API-14 [new] | The device copy is updated after every successful create/update/delete and photo change. | repository writes |
| MEM-API-15 [new] | Date round-trip: with a year the date is sent as an exact solar date (lunar including the leap month) and read back through `toLunar`; without a year day/month/leap are kept unchanged. | `toRequestDto`, `toDomain` |
| MEM-API-16 [15] | No field outside the contract is sent; unknown response fields are ignored (shared `Json`). | `JsonProvider`, DTO defaults |
| MEM-AND-01 [new] | Photo picker only (no camera permission), channel `memorial_reminders`, reminders survive reboot (WorkManager). | `GioEditScreen`, `NotificationChannels` |
| MEM-RMD-01 [20] | Reminder on/off and the four leads/times are sent to the server; saving success re-places the device reminders. | `ReminderCard`, `GioViewModel.save` |
| MEM-RMD-02 [new, BR-MEM-13] | For each of the 4 lead choices × 4 times, exactly one reminder per giỗ at the expected local moment. A moment already passed is caught up **immediately** while the giỗ day is still current (until 23:59:59 local, including a lead-day moment that fell the day before); once the giỗ day has ended the period is skipped and the next one planned. Exactly once per `entryId + kỳ giỗ`, key `"<entryId>:<solar anniversary date>"`. | `MemorialReminderPlanner`, `MemorialReminderTest` |
| MEM-RMD-03 [26] | Notification text is built from lead + relationship only ("Hôm nay là giỗ cha"), never the name; tapping opens the entry form, a deleted entry opens the book with the gone message. | `MemorialNotificationText`, `MainActivity` deep link |
| MEM-RMD-04 [new] | Turning reminders off or deleting an entry cancels its pending work; sign-out cancels all. | `scheduler.sync`, `cancelAll` |
| MEM-RMD-05 [new] | Changing date/time/lead or the name re-places reminders from the saved entry. | `sync` after save |
| MEM-RMD-06 [new] | Sign-out / account switch cancels every reminder and clears the copy before the next account loads. | `MemorialAccountCleaner` |
| MEM-RMD-07 [22, 23] | `POST_NOTIFICATIONS` is asked once; denial does not block saving, "Có nhắc" stays, and the form shows the "Mở Cài đặt" hint. | `GioEditScreen` permission block |
| MEM-IMG-01 [38] | No photo → initials avatar. | `MemorialAvatar` |
| MEM-IMG-03 [new] | Upload builds exactly one part named `photo`; the MIME type and filename come from the real bytes (magic-byte sniffing). HEIC/HEIF becomes JPEG, EXIF rotation is applied, the long side is capped at 2048 px, and the 5 MiB limit is checked on the bytes sent. | `MemorialPhotoNormalizer`, `PhotoEncoding`, `uploadPhoto` |
| MEM-IMG-04/05 [new] | 415 → "Ảnh phải là JPEG, PNG hoặc WebP."; 413 → "Ảnh tối đa 5 MB." | `photoMessage` |
| MEM-IMG-06 [new] | Deleting a photo returns the initials avatar; a photo-less entry is not an error. | `deletePhoto` |
| MEM-IMG-09 [new] | `photo_url` is never written to Room or backup (D8). | `MemorialCacheEntity` |
| Legacy 2/3 [AC2, AC3] | Guest: no request, no cached rows, "Đăng nhập" button. | `GioStatus.Guest` |
| Legacy 4/5 [AC4, AC5] | Sorting by days left; row shows relationship, date with leap marker, days label, "Có nhắc"; featured card adds ordinal, solar date, reminder summary. | `GioScreen`, `GioFormat` |
| Legacy 7 [AC7] | Returning to the foreground recomputes the list for the new day and re-places reminders. | `onForeground` |
| Legacy 9/11 [AC9, AC11] | New entry defaults (lunar, remind on, 1 day, 07:00, no leap); relationship chips; lunar/solar; day/month ranges; year 1900…now; leap switch only for lunar. | `MemorialDateRules.newEntry`, `GioEditContent` |
| Legacy 12/13/14 [AC12–14] | Name 1–100 trimmed, note ≤ 1000, date must exist (including leap), year ≥ 1900, not after today; each shows its own field message and blocks the request. | `MemorialDateRules`, `validationErrors` |
| Legacy 17/18 [AC17, AC18] | Back keeps the entry unchanged; save/delete/photo are locked while a request runs. | `discardDraft`, `saving` |
| Legacy 21 [AC21] | Stored lead/time outside the four choices are shown as the nearest one without rewriting until save. | `withNearestReminder` |
| Legacy 30 [AC30] | No local reminder is placed while the interim flag is off; with it on (this build) reminders are local by design (D1). | `MemorialModule` |
| Legacy 31 [AC31] | No "Thêm vào Lời nhắc (Reminders)" switch or "có trong Lời nhắc" label. | `GioEditScreen`, `GioFormat` |
| Legacy 35 [AC35] | The device copy is keyed by owner; another account never sees it. | cache DAO `ownerId` |
| Legacy 36/37 [AC36, AC37] | No PII in logs (`/v1/memorials*` skipped by the logger); `allowBackup=false` plus memorial/WorkManager DB exclusions in both backup rule files. | `LoggingInterceptor`, `AndroidManifest.xml`, `backup_rules.xml`, `data_extraction_rules.xml` |
| Legacy 39 [AC39] | The three product vector files run unchanged from `app/src/test/resources/product/memorial/rules/`. | `MemorialVectorsTest` |

### BR-MEM-13 — reminder catch-up vectors (T1–T6)

Giỗ 23/11/2026, `days_before = 1`, `time = 07:00`, device/account zone Asia/Ho_Chi_Minh (UTC+07):
the reminder moment is 22/11/2026 07:00. "Key" is `"<entryId>:<solar anniversary date>"`.

| # | Given | When | Then |
|---|---|---|---|
| T1 | Key not delivered | Sync 22/11/2026 08:00 | One catch-up, `fireDate ≈ now`, key `"<id>:2026-11-23"`, body from relationship only |
| T2 | Key already in `deliveredKeys` | Sync 22/11/2026 09:00 | No catch-up, no re-schedule for this period |
| T3 | Key not delivered | Sync 23/11/2026 09:00 (moment passed) | One catch-up — still inside the giỗ day |
| T4 | Key not delivered | Sync 24/11/2026 00:30 | No catch-up for 2026; plan the 2027 period |
| T5 | `remind = false` | Sync | No plan at all |
| T6 | Key not delivered, device zone changed | Sync after the zone change | Moment recomputed in the new zone; one entry per key, no duplicate |

After the giỗ day ends the period is dropped, never fired late. The planner condition is
`key !in delivered` over occurrences `>= today`; the old `occurrence.date == today` condition is not valid.

`MemorialReminderTest` covers T1–T5 today; T6 runs through the foreground re-sync when the zone changes and
has no separate unit case yet.

## Data & API

| Action | Operation | Local copy | Notes |
|---|---|---|---|
| Load | `GET /v1/memorials` | replace the owner's rows | one request, no pagination; dev base URL `https://innerlamp-app-3f53tei42a-as.a.run.app/` |
| Create | `POST /v1/memorials` | upsert the returned entry | 409 = book full |
| Update | `PUT /v1/memorials/{id}` | upsert; 404 deletes the row | whole document |
| Delete | `DELETE /v1/memorials/{id}` | delete the row | 404 = done |
| Upload photo | `PUT /v1/memorials/{id}/photo` (multipart `photo`) | upsert; URL not stored | flag off by default |
| Delete photo | `DELETE /v1/memorials/{id}/photo` | upsert | 200 even without a photo |
| Get one | `GET /v1/memorials/{id}` | — | declared, not used by the screens |

## Error → UI

| Failure | User sees | Keeps |
|---|---|---|
| 401 on load | guest state ("Đăng nhập") | — |
| 401 on save/delete | "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại." + "Đăng nhập lại" | form and every field stay until the user acts |
| 404 on update/delete | "Mục này không còn trong sổ giỗ.", form closes | remaining entries |
| 409 | "Sổ giỗ đã đủ 100 người thân." | form data |
| 400 `VALIDATION_ERROR` | field message for `full_name`, `note`, `death_*`; otherwise "Chưa lưu được. Vui lòng thử lại." | form data |
| No connection / timeout on write | "Cần kết nối mạng để lưu thay đổi." | form data, book |
| No connection / 5xx on read | device copy + "Đang hiện bản lưu trên máy."; no copy → "Không tải được sổ giỗ" + retry | — |
| 413 / 415 photo | "Ảnh tối đa 5 MB." / "Ảnh phải là JPEG, PNG hoặc WebP." | form data |

Copy not quoted in the attached product/AC excerpts (book full, validation sentences, photo errors) uses
plain Vietnamese written for this integration; the product owner may reword them without code changes.

## Out of scope

- FCM push, push-token registration, account time zone reporting, the notification inbox (Phase 8–10).
- Web local reminders (D2); iOS 60-pending cap (TD-MEM-CLIENT-03).
- Offline writes / queues / conflict resolution (D5, TD-MEM-CLIENT-05).
- Creating entries from the genealogy tree, sharing, search, multiple photos.

## Known limits (recorded, not bugs)

1. No sync: a change on the web or another device is not reflected in this device's reminders until the
   app is opened again.
2. Several devices each notify; the one-per-`entryId + kỳ giỗ` guarantee is per device while reminders are
   local (D1).
3. WorkManager may run late under Doze (TD-MEM-CLIENT-04).
4. Reinstalling or clearing data loses the schedule until the next load.
5. Travel across time zones re-places reminders for the new local zone (by design).
6. Photo UI stays hidden until the routes are deployed and the flag is turned on (D4).

## Pending

- Run `./gradlew :app:testDevDebugUnitTest` (owner/QA — AGENTS.md forbids agents running tests).
- Device E2E: CRUD, offline read, reminder firing, opening from a notification, leaving a review.
- Photo E2E after the backend deploys the two routes and the flag is turned on.
