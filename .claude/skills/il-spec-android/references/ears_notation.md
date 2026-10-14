# EARS notation for requirements.md

Every acceptance criterion names a trigger and a mandatory system response:

```
WHEN <trigger>, THE SYSTEM SHALL <behavior>. (<product ID>)
```

Forms used here (see `Docs/Specs/Networking/requirements.md`):

- `WHEN <event>, THE SYSTEM SHALL <behavior>.`
- `WHEN <event>, THE SYSTEM SHALL <behavior>, rồi:` with `- on success: …` / `- on failure: …`
  branches, for submit-and-wait flows.
- `WHEN <state holds>, THE SYSTEM SHALL …` for conditions ("WHEN không có mạng", "WHEN access token
  hết hạn").

## Examples

Bad: "Màn đăng nhập xử lý lỗi."

Good:

> WHEN người dùng bấm "Đăng nhập" với dữ liệu hợp lệ, THE SYSTEM SHALL gọi `POST /v1/auth/login`,
> rồi:
>
> - on success: lưu token vào secure storage, điều hướng về Home;
> - on failure: ở lại màn Login, hiển thị thông báo theo bảng Error → UI, giữ số điện thoại.
>   (FR-AUTH-002)

## Rules

1. **One criterion = one trigger**; outcomes are branches of it.
2. **Always "THE SYSTEM SHALL"** — a testable obligation, not a description.
3. **Tag every criterion** with the product ID it implements — `(FR-MEM-002)`, `(BR-MEM-04)` — or
   `(platform)` for Android-only behavior (back navigation, permissions, notifications, process
   death, offline cache). See `upstream_product_spec.md`.
4. **Don't restate the rule** — "THE SYSTEM SHALL tính ngày giỗ kế tiếp theo BR-MEM-03", not the
   rule's content.
5. **Name hidden steps** (token save, refresh, cache write, scheduling a notification) here; the
   _how_ goes in `design.md`'s Step-by-step Flow.
6. Out of scope / Pending / Open questions are plain prose, not EARS.
