# Changelog — Onboarding Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.0 (2026-10-14)

- Spec đầu tiên của Android cho splash, giới thiệu, đăng ký, mã xác thực qua email và đề xuất chùa,
  viết prospective theo product spec innerlamp-be `docs/product/onboarding/product.md` @ v1.2
  (ADR-0003 Accepted: email + mã gửi qua email, bỏ SMS) và spec backend onboarding v8.0. Chỉ tài liệu,
  không đổi code.
- `requirements.md`: header `Product spec` / `Contract` / `Architecture` / `Implements`; tiêu chí 1–29
  gắn ID product; bảng "Lệch hiện tại" ghi code ngày 2026-10-14 (splash luôn vào giới thiệu, lựa chọn
  chỉ trong bộ nhớ, không có họ tên, nơi xuất gia là chữ tự do, kiểm email/số điện thoại lỏng, giả lập
  thành công, chưa có màn nhập mã).
- `design.md`: bảng layers có cột Trạng thái; Step-by-step Flow "Mở app" và "Đăng ký" (Step 1–6);
  `SignupViewModel` dùng chung cho Signup / bộ chọn chùa / đề xuất chùa / Otp; lưu phiên qua
  `AuthSessionStore` của `../Login/`.
- `tasks.md`: Phase 1–7; `[x]` chỉ cho phần đã đọc trong code và khớp (12 quốc gia, gợi ý độ mạnh, cờ
  `onboarding_completed`, UI 3 bước, "Gợi ý" pháp danh, repository chùa và đơn vị hành chính).
- Quyết định của anh Đức (2026-10-14) áp dụng như iOS: điều khoản để sau (giữ nội dung tĩnh, tiêu chí 12);
  lần đầu → giới thiệu → bật cờ → Signup, các lần sau → Login; đề xuất chùa gửi qua route public đã có.
- Hand-off: product — phần "một văn bản" của BR-ONB-11 để sau trên Android (báo `ba-writer` khi làm).
  Backend — không có.
