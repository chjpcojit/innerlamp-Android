# Tasks — Memorial (Android)

Version: 3.5
Last updated: 2026-11-17
Worktree `InnerLamp-memorial`, branch `feat/memorial-integration`, base `origin/main` @ `d4ebf5b`.
`[x]` means the file exists and the behaviour is implemented. Unit tests are written but only
`MemorialReminderTest` has been executed (9/9 on 2026-11-17 02:36, before its T7 case was added; the file
now has 10); every other class is compile-checked only (AGENTS.md forbids an agent running them).
Tick rule: tick an item in the same change that lands its code and its gate evidence — finished work is
never left open. Every `[ ]` ends with `[NOT RUN]` or `[DEFERRED]` and a one-phrase reason.

Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## A1 — Model + domain

- [x] `domain/model/MemorialEntry.kt` — `id: Long` (0 = new), `leapMonth`, `photoUrl` in memory only,
      `syncReminders` removed; `MemorialRelation` carries `apiCode` and the notification noun.
- [x] `domain/model/MemorialReminderChoices.kt` — four leads, four times, nearest choice with the
      earlier tie-break.
- [x] `domain/model/MemorialDateRules.kt` — name/note/date validation, nearest-reminder display, defaults.
- [x] `domain/model/MemorialError.kt` — sealed feature errors and `MemorialLoad` (Guest/Loaded/Offline/Failed).
- [x] `domain/calendar/MemorialSchedule.kt` — `upcoming`, `lunarToSolarExact`, `hasLeapMonth`; `next`
      unchanged for existing callers.

## A2 — DTO + Retrofit

- [x] `data/remote/dto/MemorialDto.kt` — response, list, request, reminder (no `timezone`, no
      `add_to_os_reminder`).
- [x] `data/remote/api/MemorialApiService.kt` — 7 operations, all `@Authenticated`, `@Multipart` with one
      part named `photo`.

## A3 — Room read copy

- [x] `core/database/memorial/MemorialCacheDatabase.kt`, `MemorialCacheEntity.kt`, `MemorialCacheDao.kt`
      — owner-scoped `(ownerId, id)` primary key, transactional replace, delivery log table.
- [x] `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml` — exclude the memorial database
      from cloud backup and device transfer.

## A4 — Repository + use cases

- [x] `data/mapper/MemorialMapper.kt` — DTO ↔ domain, cache ↔ domain, `AppError → MemorialError`.
- [x] `data/repository/MemorialRepositoryImpl.kt` — server-first load, write, delete (404 = done),
      photo upload/delete, owner-scoped copy.
- [x] `domain/repository/MemorialRepository.kt`, `domain/usecase/LoadMemorialsUseCase.kt`,
      `SaveMemorialUseCase.kt`, `DeleteMemorialUseCase.kt`; `GetMemorialEntriesUseCase.kt` deleted.
- [x] `data/repository/MemorialAccountCleaner.kt` — cancel reminders + clear the copy on sign-out.

## A7 — DI + navigation

- [x] `di/MemorialModule.kt` — database, repository, use cases, scheduler, cleaner, ViewModel factory.
- [x] `ui/navigation/Routes.kt` — `GioRoute(openId: Long?)`, `GioEditRoute` unchanged.
- [x] `MainActivity.kt` — ViewModel factory on both destinations, reminder deep link (`onCreate`,
      `onNewIntent`, `singleTop`).
- [x] `InnerLampApplication.kt` — the memorial cleaner is registered with the session store.
- [x] `app/build.gradle.kts` — `BuildConfig.MEMORIAL_PHOTO_ENABLED = false`.
- [x] `AndroidManifest.xml` — `POST_NOTIFICATIONS`, `launchMode="singleTop"`.

## A5 — ViewModel + UI

- [x] `GioViewModel.kt` — load state machine, validation, save/delete with the error table, 100-entry
      pre-check, reminder sync, photo actions, foreground refresh.
- [x] `GioUiState.kt` — status, banner, draft with field errors, confirmation flag.
- [x] `GioScreen.kt` — loading/guest/failed/empty states, offline banner, retry, login, leap label,
      initials or signed photo, "Lấy từ gia phả" removed.
- [x] `GioEditScreen.kt` — leap switch, field errors, saving lock, delete confirmation, notification hint
      with "Mở Cài đặt", photo block behind the flag, Reminders switch removed.
- [x] `GioFormat.kt` — leap marker, reminder label without the Reminders suffix.

## A6 — Local reminders

- [x] `domain/reminder/MemorialReminderPlanner.kt`, `MemorialReminderScheduler.kt`.
- [x] `core/reminder/NotificationChannels.kt`, `MemorialReminderWorker.kt`,
      `WorkManagerMemorialReminderScheduler.kt`.
- [x] Per-entry cancellation by tag, three occurrences ahead, delivery log, no name in the text.

## A8 — Tests + vectors

- [x] `app/src/test/.../domain/calendar/MemorialVectorsTest.kt` — the three product vectors unchanged.
- [x] `app/src/test/.../domain/reminder/MemorialReminderTest.kt` — choices, 16 pairs, BR-MEM-13
      catch-up/no-catch-up, copy.
- [x] `app/src/test/.../data/mapper/MemorialMapperTest.kt` — date round-trip, leap recovery, request body,
      error mapping.
- [x] `app/src/test/.../ui/screens/gio/GioViewModelTest.kt`, `GioFormatTest.kt`.
- [x] `scripts/sync-memorial-vectors.sh` + `app/src/test/resources/product/memorial/rules/*` (with
      `SOURCE`).
- [ ] Run the suite — owner/QA only (AGENTS.md). `[NOT RUN]` — only `MemorialReminderTest` 9/9 so far.

## A9 — Photos (flag)

- [x] Photo picker + upload/remove in the form, 413/415 messages.
- [x] `MemorialPhotoNormalizer` — real MIME from magic bytes, HEIC/HEIF → JPEG with EXIF rotation,
      2048 px cap, JPEG q85, 5 MiB check on the bytes sent, 32 MiB raw read cap, IO dispatcher;
      `PhotoEncodingTest`; repository no longer hardcodes `image/jpeg`/`photo.jpg`.
- [x] UI hidden unless `BuildConfig.MEMORIAL_PHOTO_ENABLED`.
- [ ] E2E after the backend deploys the routes (F1–F3), then turn the flag on. `[DEFERRED]` — photo
      routes not deployed, flag false.

## G1 security rework — Hộ Pháp, 2026-11-17

- [x] A1 — `LoggingInterceptor.shouldLog` skips `/v1/auth/` and `/v1/memorials*` (photo routes included);
      `LoggingInterceptorTest`.
- [x] A2 — `android:allowBackup="false"`; memorial and WorkManager databases excluded in both
      `backup_rules.xml` and `data_extraction_rules.xml`; `MemorialSecurityConfigTest`.
- [x] A3 — `MemorialReminderPayload` (4 keys), worker reads the device copy, falls back to "người thân";
      `MemorialReminderPayloadTest`.
- [x] A4 — `INTERNET` and `POST_NOTIFICATIONS` declared in the manifest;
      `MemorialSecurityConfigTest`.
- [x] A5 — notification `VISIBILITY_PRIVATE` (no name anywhere in the text).
- [x] A6 — `FLAG_IMMUTABLE`, `MemorialDeepLink.parseOpenId` (positive id only), component check in
      `MainActivity`; `MemorialDeepLinkTest`.
- [x] A0 — `Docs/Specs/Memorial/impact.md` §6 corrected (the two false claims removed, A1–A3 added to review
      focus and stop conditions); drift log row added.
- [ ] G1 re-sign by Hộ Pháp; logcat check on a device (owner/QA). `[NOT RUN]` — no re-sign, no device
      logcat.

## P7 QA rework — 2026-11-17

- [x] Dev base URL → `https://innerlamp-app-3f53tei42a-as.a.run.app/` (dev flavor only; staging/prod
      untouched); `/v1/memorials` answers 401 from the build machine.
- [x] A 401 on save/delete keeps the draft and its content (`draft.sessionExpired`, explicit "Đăng nhập lại");
      no cache clear, no guest screen, no navigation. `GioViewModelTest`.
- [x] `MemorialCacheDao.getById(ownerId, id)`; worker takes the owner from the session;
      `MemorialCacheDaoTest` (instrumented, not run — QA/owner).
- [ ] TC-AND-RMD-03 (QA v1 FAIL) — fixed: `MemorialNotificationText.daysUntil`
      (`core/reminder/NotificationChannels.kt:45`) counts the real days left from `anniversary_key`, the
      worker uses it (`core/reminder/MemorialReminderWorker.kt:72`) so a catch-up on the giỗ day reads
      "Hôm nay…", and the payload keeps its four keys. `MemorialReminderTest.kt:120` compiles, QA has not
      re-run it. `[NOT RUN]`
- [ ] Deferred (Q-2): picking a photo while creating a new entry — the photo flag is off and the form only
      offers the picker after the entry has a server id. `[DEFERRED]`

## Gates

- [x] `./gradlew :app:assembleDevDebug` — run, result in the delivery report.
- [x] `./gradlew :app:lintDevDebug` — run 2026-11-17 02:16; 4 known errors (Onboarding/Splash,
      TD-MEM-CLIENT-07) + 33 warnings, no baseline file added
      (`app/build/reports/lint-results-devDebug.txt`).
- [ ] `./gradlew :app:testDevDebugUnitTest` — not run by an agent; owner/QA. `[NOT RUN]` — full suite,
      one class recorded.

## Follow-ups

- Turn the photo flag on after deploy (D4 checklist).
- Cutover to server push (D1): DI provider → no-op, `cancelAll()` on first launch, then delete the local
  scheduler and TD-MEM-CLIENT-01/04.
- Reword the copy not quoted in the product excerpt (book full, validation, photo errors) if the owner
  decides.
