# TD-MEM-CLIENT-03 — iOS schedules at most 60 pending reminders

- **Owner:** ✦ Nguyên Anh ✦ (iOS) · **Coordinator:** ⚔ Đạo Sư ⚔
- **Status:** open, platform limit
- **Target:** Prod/Beta

## What is owed / Nợ gì

iOS keeps 64 pending notifications per app; the interim scheduler places at most 60 (20 entries × 3
anniversaries) and re-places the rest when the app becomes active. A book with more than 20 reminded
entries therefore has entries without a pre-placed reminder.

## Trigger to pay / Điều kiện trả

Server push (Phase 10) or a documented "top N nearest" policy accepted by the product owner.

## Impact if not paid

Distant anniversaries may be missing a reminder if the app is not opened for months. No crash, no
duplicate; it is a coverage limit, not a data-loss risk.
