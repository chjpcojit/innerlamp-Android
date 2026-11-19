# TD-MEM-CLIENT-02 — The web client has no memorial reminder

- **Owner:** ⚔ Đạo Sư ⚔ (Tech Lead) · **Client owner:** ✦ Trúc Cơ ✦ (Web)
- **Status:** open, accepted limitation (decision D2)
- **Target:** Prod/Beta

## What is owed / Nợ gì

The website saves the reminder configuration and shows "Có nhắc", but never notifies: a closed tab cannot
be woken without Web Push (VAPID) or a service worker plus a server sender. Both belong to Phase 10.

## Trigger to pay / Điều kiện trả

The notification list/inbox API exists and the web renders it; a notification channel (Web Push or the
inbox badge) is approved as a product decision. Until then the UI must not promise "we will remind you".

## Impact if not paid

FR-MEM-008 is not met on the web. A web user only sees an anniversary by opening the book.
