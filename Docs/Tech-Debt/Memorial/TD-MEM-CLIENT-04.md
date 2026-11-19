# TD-MEM-CLIENT-04 — Android WorkManager reminders can run late in Doze

- **Owner:** ✦ Hóa Thần ✦ (Android) · **Coordinator:** ⚔ Đạo Sư ⚔
- **Status:** open, accepted limitation (decision D1)
- **Target:** Prod/Beta

## What is owed / Nợ gì

The reminder is a one-time `WorkManager` job with `initialDelay`, not an exact alarm, so Doze and battery
saver can delay it (usually minutes, potentially longer). Exact alarms were avoided on purpose: they need
`SCHEDULE_EXACT_ALARM` and a Play policy justification.

## Trigger to pay / Điều kiện trả

Server push (Phase 10), or an approved product requirement for exact-time reminders plus the Play policy
review and the `SCHEDULE_EXACT_ALARM` declaration.

## Impact if not paid

A reminder may arrive after the chosen time. MEM-RMD-03 allows up to 15 minutes on an awake device; beyond
that the reminder is late. No data is lost and the next anniversary is still placed.
