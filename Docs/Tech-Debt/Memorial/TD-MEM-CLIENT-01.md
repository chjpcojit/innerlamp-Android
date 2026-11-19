# TD-MEM-CLIENT-01 — Memorial reminders run on the device (interim)

- **Owner:** ⚔ Đạo Sư ⚔ (Tech Lead) · **Client owner:** ✦ Hóa Thần ✦ (Android)
- **Status:** open, time-boxed deviation (decision D1)
- **Target:** Prod/Beta — must be closed before the feature is announced as "server reminders".

## What is owed / Nợ gì

Android places memorial reminders locally (`WorkManager`, channel `memorial_reminders`) instead of the
server computing and pushing them (ADR-0002, BR-MEM-11/13). The web has no reminder at all (TD-02).

## Trigger to pay / Điều kiện trả

All four: (1) Phase 8 contract v4.0 deployed, (2) Phase 9 server plans `next_reminder_at`, (3) Phase 10 +
notification feature deployed, (4) a client release that turns local reminders off and calls `cancelAll()`
once. Cutover order: client release first, then enable server push, to avoid two notifications per
anniversary.

## Impact if not paid

Several devices notify separately for one anniversary (BR-MEM-13 unmet); a change made on another device
does not update this device's reminders until the app is opened; Doze can delay a reminder (TD-04). No UI
or domain change is needed at cutover: only `MemorialModule.provideReminderScheduler` becomes a no-op and
the local scheduler/worker are deleted.
