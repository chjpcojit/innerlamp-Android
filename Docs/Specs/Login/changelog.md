# Changelog — Login Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.0 (2026-10-14)

- Spec đầu tiên của Android cho đăng nhập, phiên, đăng xuất, quên mật khẩu và khách, viết prospective
  theo product spec innerlamp-be `docs/product/login/product.md` @ v1.2 (ADR-0003 Accepted) và spec
  backend login v2.2. Chỉ tài liệu, không đổi code.
- Đây cũng là spec `AuthTokenStorage` mà `Docs/Specs/Networking/` và `Docs/Specs/Storage/` hẹn làm sau:
  token trong `SecureStorage` `DEVICE_UNLOCKED`, `SessionTokenCache` thay `InMemoryAuthTokenProvider`,
  refresh bằng OkHttp `Authenticator` single-flight.
- `requirements.md`: tiêu chí 1–23 gắn ID product; bảng "Lệch hiện tại" (Login theo số điện thoại và
  giả lập, không có lưu phiên / refresh / đăng xuất / quên mật khẩu, "Vào xem trước" như đã đăng nhập).
- `design.md`: `AuthSessionStore` + `AccountDataCleaner` dùng chung; Step-by-step Flow đăng nhập, gia hạn
  và kết thúc phiên, đăng xuất, quên mật khẩu.
- `tasks.md`: Phase 1–7; `[x]` chỉ cho `SecureStorage` và phần gắn Bearer của `AuthInterceptor`.
- Quyết định của anh Đức (2026-10-14) áp dụng như iOS: phiên hết thì báo "Phiên đăng nhập đã hết, vui lòng
  đăng nhập lại." và cần thêm UI (chốt giao diện trước khi làm). Mặc định của Claude, review cùng PR:
  nút "Đăng xuất" ở cuối màn Hồ sơ.
- Hand-off: product — không có. Backend — không có.
- Follow-up ngoài folder: `Docs/Specs/Networking/` và `Docs/Specs/Storage/` vẫn ghi `AuthTokenStorage`
  "chưa tạo" — sửa thành trỏ về `Docs/Specs/Login/` ở bước dọn tài liệu cũ.
