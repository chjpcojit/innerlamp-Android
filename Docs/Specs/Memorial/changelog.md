# Changelog — Memorial Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v2.1 (2026-10-14)

- Bước 4 của đợt viết lại tài liệu: xoá `Docs/gio_assumptions.md` (product spec v2.1 đã thay thế); hai ghi chú build chuyển sang `AGENTS.md` và `CLAUDE.md`. `tasks.md` Phase 6: tick mục này.
- `requirements.md` AC21 và AC27: hai `⚠️ Not confirmed (product)` đã có câu trả lời ở product v2.1 (FR-MEM-007 quy tắc "gần nhất": cách đều thì chọn nhắc sớm hơn, khác đề xuất cũ "lựa chọn nhỏ hơn"; FR-MEM-008 mục đã xoá → mở sổ và báo "Mục này không còn trong sổ giỗ."). `design.md`/`tasks.md` không đổi ngoài version.

## v2.0 (2026-10-14)

- `requirements.md`: header lên product spec @ v2.1 (= v2.0 trên innerlamp-be `main` @ bf23f8b + câu
  trả lời Appendix A của product owner ngày 2026-10-14, sẽ ghi ở v2.1 FR-MEM-008) và contract @ bf23f8b
  (phần Memorials không đổi so với 5f4ca56). AC viết lại và đánh số lại (33 → 39):
  - Bỏ nhắc local (AC22–AC25 cũ: lập lịch, tới giờ, khởi động lại, huỷ khi đăng xuất) — ADR-0002;
    thêm AC30 "không đặt nhắc local nào".
  - FR-MEM-009 Removed: AC31 (bỏ công tắc "Thêm vào Lời nhắc"); AC15 không gửi `add_to_os_reminder`
    và múi giờ theo mục.
  - Push FCM (ADR-0001): xin `POST_NOTIFICATIONS` (AC22), gợi ý "Mở Cài đặt" và "Có nhắc" vẫn hiện
    (AC5, AC23), đăng ký token sau đăng nhập / khi token mới (AC24), xoá token + bản offline khi đăng
    xuất (AC25), nội dung push chỉ quan hệ (AC26), mở mục từ push (AC27) và từ danh sách Thông báo
    (AC28, feature dùng chung, phụ thuộc).
  - Múi giờ máy làm múi giờ tài khoản (AC29, BR-MEM-11).
  - Lựa chọn gần nhất cho giá trị nhắc cũ (AC21, FR-MEM-007).
  - Sổ trống ẩn "Lấy từ gia phả" (AC6); 404 khi sửa/xoá → bỏ mục + "Mục này không còn trong sổ giỗ."
    (AC16, AC19); 5xx khi tải → bản offline + câu báo (AC32); bản offline gắn tài khoản, không bao giờ
    hiện bản của tài khoản khác (AC35); câu chữ lấy từ product §3.1.1 (trích theo tên dòng).
  - Giữ AC36 (không log body/ push payload, kể cả log HTTP debug) và AC37 (DB offline ngoài cloud
    backup) cùng task.
  - Bảng hiện trạng, Dữ liệu & API (thêm dòng push token / múi giờ / payload / danh sách Thông báo,
    đều ⚠️ contract), Error → UI, Out of scope, Pending, Open questions (resolved) cập nhật.
- `design.md`: bỏ `MemorialReminderPlanner`, port `MemorialReminderScheduler`, `core/notification`
  (AlarmManager, receivers, `RECEIVE_BOOT_COMPLETED`, exact alarm ⚠️) và Step-by-step Flow "Nhắc tới
  giờ"; thêm `core/push` (`FirebaseMessagingService`, kênh, permission checker), use case/repository
  push token và múi giờ, `MemorialReminderChoices`, `GioRoute(openId)` + deep link, `accountId` trong
  Room, hook đăng nhập/đăng xuất, Firebase build; hai Step-by-step Flow mới ("Đăng nhập / đăng xuất:
  token FCM và múi giờ", "Nhận và mở push"); flow "Mở sổ" bỏ bước lập nhắc và đọc offline cả khi 5xx.
- `tasks.md`: Phase 4 đổi từ "Nhắc local" sang "Push FCM và múi giờ tài khoản"; Phase 2 thêm ẩn gia
  phả, gợi ý quyền, mở mục từ nhắc, lựa chọn gần nhất; Phase 5 không chạy `reminder-moment.json`
  (product §4.3 — backend), kiểm `product_spec_version` theo `SOURCE`; `[x]` kiểm lại trong code ngày
  2026-10-14 (giá trị khởi đầu không còn `[x]` vì còn `syncReminders`; thêm `[x]` AC30).
- Product spec / contract: product v2.0 → v2.1 (MAJOR từ v1.0). IDs ảnh hưởng: FR-MEM-001, 002, 004,
  005, 007, 008, 009 (Removed), 010; BR-MEM-05…07, 10, 11 (giờ áp cả Backend), BR-MEM-13 và NFR-MEM-04
  (mới, chỉ Backend — Android `n/a`). Contract @ bf23f8b.
- Hand-offs:
  - `ba-writer`: (1) "lựa chọn gần nhất" khi giá trị cách đều hai lựa chọn (2/5 ngày, 09:30) — AC21;
    (2) mở một nhắc (push / danh sách) của mục đã bị xoá thì thấy gì — AC27; (3) ghi câu trả lời
    công tắc tài khoản vào product v2.1 (đã quyết, chỉ cần ghi); (4) cột Android §4.2 giữ `drift` cho
    FR-MEM-002 (hàng gia phả) và FR-MEM-009 (công tắc) tới khi code sửa.
  - Backend (contract): đăng ký / xoá push token (Appendix B mục 8); báo múi giờ tài khoản (mục 3);
    payload push nhắc giỗ — loại message, khoá id mục, channel Android (mục 7, 8); danh sách Thông báo
    (mục 7); bỏ `reminder.timezone` / `add_to_os_reminder` và thu hẹp lựa chọn nhắc (mục 2, 3, 6) —
    Android đã không gửi các field đó.
  - Feature Login (Android, chưa có spec): phiên, account id, hook đăng nhập/đăng xuất.
  - Feature Thông báo (Android, chưa có spec): danh sách, công tắc tài khoản, mở mục (AC28).
  - Hạ tầng: Firebase project + `google-services.json` theo flavor (product owner, ADR-0001).
- Resolved: mọi `⚠️ Not confirmed (product)` của v1.0 (câu chữ lỗi, nội dung/màn hình khoá, từ chối
  quyền, thời điểm đã qua, đăng xuất, 404 khi xoá, `add_to_os_reminder`, hàng gia phả, 5xx) và
  ⚠️ exact alarm (không còn áp dụng); công tắc thông báo tài khoản (chỉ tắt push — product owner
  2026-10-14). Còn mở (Android): cách mời khách đăng nhập (⚠️ AC2).
- Reason: product spec memorial v2.0 (nhắc do server gửi, ADR-0001/0002 Accepted) yêu cầu các client
  spec đang mở chuyển sang v2.0 (product `changelog.md` v2.0, "Platforms to follow up"); câu trả lời
  của product owner cho mục mở Appendix A (v2.1).

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
