# Changelog — Memorial Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.0 (initial) (2026-10-14)

- `requirements.md`: spec đầu tiên, viết retroactively từ code đang chạy (`ui/screens/gio/`,
  `domain/calendar/MemorialSchedule.kt`, `domain/model/MemorialEntry.kt`,
  `domain/usecase/GetMemorialEntriesUseCase.kt`, đọc ngày 2026-10-14), đối chiếu với product spec
  innerlamp-be `docs/product/memorial/product.md` @ v1.0 và contract `openapi.yaml` @ 5f4ca56. Header
  `Product spec` / `Contract` / `Implements`; 33 AC EARS, mỗi AC gắn `FR-MEM-*`/`BR-MEM-*`/`NFR-MEM-*`
  hoặc `(platform)`; bảng hiện trạng từng AC; Error → UI mapping; mọi ID có cột Android khác `n/a` ở
  §4.2 đều có ít nhất một AC (FR-MEM-009 `n/a` → AC26 ẩn công tắc theo §3.6).
- `design.md`: online-first + bản offline Room (DB riêng loại khỏi backup), nhắc local bằng
  `AlarmManager` lập lại sau tải/lưu/xoá/khởi động lại; bốn Step-by-step Flow (mở sổ, lưu, xoá, nhắc).
- `tasks.md`: 6 Phase. `[x]` chỉ cho phần đã có và đúng product: lịch âm, `MemorialSchedule.next`,
  route + wiring, danh sách/thẻ nổi bật/trạng thái trống, giá trị khởi đầu, xem trước, lỗi tên rỗng,
  back không lưu, lựa chọn nhắc, flash, chữ viết tắt, contract có sẵn, test hiện có. Có task chạy mọi
  case của `rules/next-anniversary.json`, `anniversary-ordinal.json`, `death-date-conversion.json` từ
  bản chép do script làm mới (không sửa tay).
- Product spec / contract: product v1.0; contract innerlamp-be `docs/specs/api/openapi.yaml` @ 5f4ca56
  (info.version 1.0.0). Implements BR-MEM-01…12, FR-MEM-001…008, FR-MEM-010, FR-MEM-011 (planned),
  FR-MEM-012, FR-MEM-013, NFR-MEM-01…03.
- Drift code ↔ product (cột Android của §4.2 nên ghi `drift`):
  1. Sổ chỉ trong bộ nhớ ViewModel, seed 3 người mẫu, không đăng nhập, không backend — BR-MEM-01,
     FR-MEM-001, FR-MEM-003/004/005 (AC1–AC3, AC15, AC16, AC19).
  2. Không có lựa chọn tháng nhuận, không hiện dấu nhuận — BR-MEM-04 (AC5, AC11).
  3. Năm mất chỉ "năm nay lùi 96 năm" — BR-MEM-03 cần 1900 → năm hiện tại (AC11).
  4. Không kiểm ngày tồn tại / tương lai; ngày dương luôn 1–31 (31/4 bị kẹp thành 30/4) — BR-MEM-03
     (AC14).
  5. Xoá ngay không hỏi — BR-MEM-12, FR-MEM-005 (AC19).
  6. Nhắc không được đặt — FR-MEM-008 (AC21–AC25).
  7. Có công tắc "Thêm vào Lời nhắc (Reminders)" + nhãn "· có trong Lời nhắc", mặc định bật — §3.6 nói
     FR-MEM-009 chỉ iOS (AC26). Ma trận §4.2 đang ghi Android `n/a` cho FR-MEM-009, nhưng code có UI.
  8. Không giới hạn 100 ký tự tên / 1000 ký tự ghi chú — BR-MEM-02 (AC12, AC13).
  9. Không có bản offline — FR-MEM-010 (AC27–AC29).
- Tài liệu bị thay thế: `Docs/gio_assumptions.md` — giả định #1–#9 nay do product spec v1.0 quyết định
  (BR-MEM-01/03/04/05/06/07/08/09, FR-MEM-008/009/010/011, §1.2, §2.7), #10–#11 là quyết định Android
  ghi trong `design.md`; ánh xạ từng mục ở `requirements.md` "Open questions (resolved)". **Chưa xoá**
  file — task Phase 6.
- Hand-offs:
  - `ba-writer` (product, innerlamp-be): (1) câu tiếng Việt cho các thông báo lỗi chỉ có nghĩa
    (sổ đầy, ngày không hợp lệ/không tồn tại, tên/ghi chú quá dài, không lưu được, cần kết nối, mục
    không còn, không tải được khi offline); (2) nội dung thông báo nhắc và hiển thị tên trên màn hình
    khoá (NFR-MEM-02); (3) người dùng từ chối quyền thông báo thì thấy gì, "Có nhắc" còn hiện không;
    (4) thời điểm nhắc của kỳ giỗ tới đã qua thì làm gì; (5) đăng xuất / đổi tài khoản trên cùng máy:
    dừng nhắc, bỏ bản offline; (6) `DELETE` trả "không tìm thấy" (đã xoá ở máy khác) thì người dùng
    thấy gì; (7) `add_to_os_reminder` khi sửa trên Android (giữ nguyên?) và giá trị cho mục tạo trên
    Android; (8) hàng "Lấy từ gia phả" ở trạng thái trống: ẩn tới khi có `genealogy`?; (9) lỗi 5xx khi
    tải sổ có hiện bản offline như mất mạng không; (10) cập nhật cột Android §4.2 theo mục "Drift" ở
    trên (đặc biệt FR-MEM-009: Android đang có UI dù `n/a`; FR-MEM-006 có thể coi `done` trừ tháng
    nhuận).
  - Backend: không có thay đổi contract nào cần cho Android.
  - Feature Login (Android, chưa có spec): phiên thật, làm mới phiên, đăng xuất — chặn Phase 3.
- Còn mở (Android, chưa hỏi được người dùng trong phiên này): cách mời khách đăng nhập (⚠️ AC2); exact
  alarm hay không (⚠️ `design.md` "Local storage").
- Reason: product spec memorial v1.0 yêu cầu mọi nền tảng viết platform spec từ `product.md@1.0`
  (product `changelog.md` v1.0, "Platforms to follow up"); Android chưa có spec cho feature này.
