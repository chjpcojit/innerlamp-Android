# Design — Memorial (Android)

Version: 3.4
Last updated: 2026-11-17
Companion to `requirements.md` v3.4. Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Overview

Two screens share one `GioViewModel` attached to the `GioRoute` back stack entry: the book
(`GioScreen`) and the form (`GioEditScreen`). Data flows
`GioViewModel → use case → MemorialRepository (domain) → MemorialRepositoryImpl (data) → NetworkClient.safeCall`
and into a Room read copy. Anniversary maths stays pure Kotlin in `domain/calendar/`.

```
ui/screens/gio/            GioScreen, GioEditScreen, GioViewModel, GioUiState, GioFormat
domain/model/              MemorialEntry, MemorialError/MemorialLoad, MemorialDateRules, MemorialReminderChoices
domain/calendar/           LunarCalendar, MemorialSchedule (+ lunarToSolarExact, upcoming, hasLeapMonth)
domain/reminder/           MemorialReminderScheduler (port), MemorialReminderPlanner (pure)
domain/repository/         MemorialRepository, AuthSessionStore.currentUserId()
domain/usecase/            LoadMemorialsUseCase, SaveMemorialUseCase, DeleteMemorialUseCase
data/remote/{dto,api}/     MemorialDto, MemorialRequestDto, MemorialApiService (7 operations)
data/mapper/               MemorialMapper (DTO ↔ domain, cache ↔ domain, AppError → MemorialError)
data/repository/           MemorialRepositoryImpl, MemorialAccountCleaner
core/database/memorial/    MemorialCacheDatabase, MemorialCacheEntity/Dao, delivery log
core/reminder/             NotificationChannels, MemorialNotificationText, MemorialReminderWorker, scheduler
di/MemorialModule.kt       manual wiring, one ViewModel factory
```

## Load state machine

`GioStatus`: `Loading → Ready | Guest | Failed`.

- `Loading` is only shown while the list is empty, so a foreground reload never flashes the spinner.
- `Loaded` replaces the copy, sorts by `daysUntil` then name, `Ready`.
- `Offline` shows the copy with the banner and re-places reminders from it.
- `Failed` shows the retry state.
- `Guest` (no session, or 401 on read) shows the login invitation and no cached rows.
- A reminder tap sets `pendingOpenId`, applied after the load; an id that no longer exists shows
  "Mục này không còn trong sổ giỗ."

Writes set `draft.saving`, so a double tap cannot send twice. Validation runs before the request
(`MemorialDateRules`), and each field has its own message; only an unexpected server field falls back to
the general message.

## Device copy

- `innerlamp_memorial.db` (separate from `innerlamp.db`): `memorial_cache` keyed by `(ownerId, id)` plus
  `memorial_reminder_delivery` (`delivery_key`).
- Every query carries `ownerId` from `AuthSessionStore.currentUserId()`; a successful load deletes rows of
  other owners, a sign-out deletes everything.
- `photo_url` is never stored (D8); the cache holds no signed URL.
- The whole file is excluded from `backup_rules.xml` and `data_extraction_rules.xml` (cloud backup and
  device transfer).
- Destructive migration is acceptable: it is only a read copy.

## Reminders (interim, D1)

- `MemorialReminderPlanner.plan(entry, now, zone, delivered)` returns up to three reminders per entry:
  `key = "<id>:<solar date>"`, `moment = anniversary - leadDays at remindTime` in the device zone.
  A moment already passed is caught up once if the sync still falls inside the giỗ day (moment ≤ now ≤
  23:59:59 of the solar anniversary date, device zone) and the key was never delivered — this includes a
  lead-day moment that fell the previous day. After the giỗ day the period is skipped and the next one is
  planned; one notification per `entryId + kỳ giỗ`.
- `WorkManagerMemorialReminderScheduler.sync(entries)` cancels every work tagged `memorial_reminder`, then
  enqueues one `OneTimeWorkRequest` per moment with `initialDelay` (unique work per key, `REPLACE`).
  No exact alarm is used.
- `MemorialReminderWorker` skips a delivered key, refuses to notify without `POST_NOTIFICATIONS` (the key
  is not marked, so enabling the permission later can still catch up on the day), and builds the text from
  lead + relationship only (no name).
- Tapping the notification starts `MainActivity` (`singleTop`) with `EXTRA_OPEN_MEMORIAL_ID`; `onCreate` /
  `onNewIntent` set the state that navigates to `GioRoute(openId)`.
- `sync` runs after every load and save, and from `onForeground` (new day or new time zone).
- Cutover: replace `MemorialModule.provideReminderScheduler` with a no-op and call `cancelAll()` once.

## Photos (flag, D4)

`BuildConfig.MEMORIAL_PHOTO_ENABLED` is true for dev/staging and false for prod. The form shows the
picker/remove block only when the flag is true and the entry already has an id, so a new entry adds its
photo after the first save (MEM-IMG-08). The picker is the Android Photo Picker (no storage or camera
permission). `photo_url` lives in screen state only.

`MemorialPhotoNormalizer` (core/media) runs on the IO dispatcher before any upload:

- the real format comes from the magic bytes (`PhotoEncoding.sniff`), never from the filename or the
  picker's MIME hint;
- HEIC/HEIF (and AVIF) are decoded with `BitmapFactory` (API 28+), the EXIF orientation is applied, and the
  result is JPEG quality 85;
- JPEG/PNG/WebP keep their format and bytes when the long side is ≤ 2048 px; larger images are downscaled
  and re-encoded as JPEG;
- the long side is capped at 2048 px and the 5 MiB limit is checked on the bytes actually sent (one
  lower-quality pass, then `TooLarge`);
- the multipart filename extension always matches the MIME type (`photo.jpg/png/webp`); undecodable input is
  `Validation("photo")` and never reaches the network;
- the raw picker stream is capped at 32 MiB so a huge file cannot exhaust memory.

## DI and navigation

- `MemorialModule` owns the database, repository, use cases, scheduler and the ViewModel factory. The
  ViewModel is created with that factory for both destinations, with `getBackStackEntry<GioRoute>()` for
  the form.
- `GioRoute` is `data class GioRoute(val openId: Long? = null)`; the editor opens from the single signal
  `state.draft != null`, so add, edit and reminder-open share one path.
- `MainActivity` is `launchMode="singleTop"`; the reminder PendingIntent is `IMMUTABLE`.

## Errors

A 401 on a read means no session: the copy is cleared and the guest state is shown. A 401 on a write is
**not** a reason to destroy input: the draft and every field stay, `draft.sessionExpired` turns on, the form
shows "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại." and only the user's "Đăng nhập lại" tap (or a real
sign-out through the session store) leaves the form. The reminder worker resolves the entry by
`(ownerId, id)` from the session, so it can never announce another account's entry.

`AppError → MemorialError` by code/status: `MEMORIAL_NOT_FOUND`/404 → `NotFound`,
`MEMORIAL_LIMIT_REACHED`/409 → `LimitReached`, `PHOTO_TOO_LARGE`/`PAYLOAD_TOO_LARGE`/413 → `TooLarge`,
`PHOTO_UNSUPPORTED_TYPE`/415 → `UnsupportedPhoto`, `VALIDATION_ERROR` → `Validation(field)`,
`UNAUTHORIZED`/401 → `Unauthorized`, no connection/timeout → `Offline`, everything else → `Failed`.

## Testing

- JVM unit tests: `MemorialVectorsTest` (three product vector files unchanged), `MemorialMapperTest`,
  `MemorialReminderTest` (16 lead/time pairs, BR-MEM-13 catch-up/no-catch-up, copy), `GioViewModelTest`
  (load states, validation, save/delete failures, reminder open), `GioFormatTest`.
- Instrumented/E2E: not written for this change; needs a device and the owner/QA (AGENTS.md).
- Gates: `:app:assembleDevDebug` and `:app:lintDevDebug` (the 8 pre-existing
  `ViewModelConstructorInComposable` warnings are the baseline; this change adds none).

## Privacy and device data (G1 rework 2026-11-17)

- `LoggingInterceptor.shouldLog` returns false for `/v1/auth/` and `/v1/memorials` (`/v1/memorials/{id}`,
  `/v1/memorials/{id}/photo` included), so no request body or URL of the book reaches logcat — debug builds
  included (A1).
- `android:allowBackup="false"`; `backup_rules.xml` and `data_extraction_rules.xml` also exclude
  `innerlamp_memorial.db` and `androidx.work.workdb` (with `-wal`/`-shm`) from cloud backup and device
  transfer (A2).
- WorkManager input is `entry_id`, `days_before`, `time`, `anniversary_key` only; the worker reads the entry
  from the device copy and uses "người thân" when the relationship is unknown (A3).
- Reminder notifications are `VISIBILITY_PRIVATE` (lock screen shows no personal text), the PendingIntent is
  `FLAG_IMMUTABLE`, and `MemorialDeepLink.parseOpenId` accepts only a positive id (A5, A6).
- No analytics in this feature; the repository maps errors without logging payloads.
