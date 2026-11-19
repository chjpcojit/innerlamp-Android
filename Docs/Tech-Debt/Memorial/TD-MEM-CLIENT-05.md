# TD-MEM-CLIENT-05 — No concurrent-edit protection (last write wins)

- **Owner:** ⚔ Đạo Sư ⚔ (Tech Lead) · **Client owner:** ✦ Hóa Thần ✦ (Android)
- **Status:** open, accepted (decision D10 / contract §6.10)
- **Target:** Prod/Beta

## What is owed / Nợ gì

`PUT /v1/memorials/{id}` replaces the whole document and neither the contract nor the clients carry a
version or ETag. Two devices editing the same entry keep the later write and silently lose the earlier one.

## Trigger to pay / Điều kiện trả

A real user complaint or a product requirement for multi-device editing. The backend adds a version/ETag,
then each client sends `If-Match` and maps 409/412 to a conflict message.

## Impact if not paid

A concurrent edit can be lost without warning. The client copy is always replaced by the server answer, so
the UI never shows a stale "saved" state for the losing write; the user simply sees the other edit.
