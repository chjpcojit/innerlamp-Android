# Requirements — Memorial

Version: 2.0
Last updated: 2026-10-14
Status: in-progress — UI (`ui/screens/gio/`) và tính kỳ giỗ (`domain/calendar/MemorialSchedule.kt`) đã có, nhưng sổ chỉ giữ trong bộ nhớ ViewModel (seed 3 người mẫu), chưa đăng nhập, chưa gọi backend, chưa lưu offline, chưa có push (FCM); còn hiện công tắc "Thêm vào Lời nhắc" (FR-MEM-009 đã bỏ) và hàng "Lấy từ gia phả"; lệch product ở tháng nhuận, khoảng năm, xác nhận xoá
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/gio/` (tên cũ `gio`, product §1.5)
Product spec: innerlamp-be `docs/product/memorial/product.md` @ v2.1
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ bf23f8b (info.version 1.0.0; phần Memorials không đổi so với 5f4ca56)
Implements: BR-MEM-01, BR-MEM-02, BR-MEM-03, BR-MEM-04, BR-MEM-05, BR-MEM-06, BR-MEM-07, BR-MEM-08, BR-MEM-09, BR-MEM-10, BR-MEM-11, BR-MEM-12, FR-MEM-001, FR-MEM-002, FR-MEM-003, FR-MEM-004, FR-MEM-005, FR-MEM-006, FR-MEM-007, FR-MEM-008, FR-MEM-010, FR-MEM-011 (planned), FR-MEM-012, FR-MEM-013, NFR-MEM-01, NFR-MEM-02, NFR-MEM-03

> See `changelog.md` in this folder for the version history of this spec.

Spec platform (layer 2) của **Sổ giỗ chạp** cho Android, viết retroactively từ code (đọc ngày
2026-10-14) và cập nhật theo product spec v2.1. v2.1 = v2.0 (innerlamp-be `main` @ bf23f8b: nhắc do
server gửi, ADR-0001, ADR-0002 — cả hai Accepted) cộng câu trả lời của product owner cho mục mở ở
Appendix A (công tắc thông báo của tài khoản chỉ tắt push, 2026-10-14). Hành vi sản phẩm nằm ở product
spec và **không** nhắc lại ở đây; file này chỉ nói app Android hiện thực các `BR-MEM-*`/`FR-MEM-*`
thế nào. FR-MEM-009 đã **Removed** ở product v2.0 — Android bỏ công tắc "Thêm vào Lời nhắc" (AC31).
BR-MEM-13 và NFR-MEM-04 chỉ áp dụng cho Backend.

`Docs/gio_assumptions.md` đã bị thay thế bởi product spec — xem "Open questions (resolved)".

## Ràng buộc từ product spec

- **Sổ thuộc tài khoản** (BR-MEM-01, §2.7): sổ chỉ đọc/ghi qua backend bằng phiên đăng nhập; dữ liệu
  seed/in-memory hiện tại bị bỏ, không migrate (§2.7, §3.5).
- **App tính để hiển thị, server tính để nhắc** (§2.7, §3.6, ADR-0002): kỳ giỗ tới, số ngày còn lại,
  lần giỗ và quy đổi âm ↔ dương (BR-MEM-05…10) tính trên máy để hiện; thời điểm nhắc (BR-MEM-11),
  nhắc trễ (BR-MEM-13) do server tính và gửi. Android **không** tự đặt nhắc local nào.
- **Nhắc tới qua hai đường** (FR-MEM-008, ADR-0001): danh sách Thông báo trong app (feature dùng chung,
  không thuộc spec này) và push qua FCM. Push chỉ hiện quan hệ, không bao giờ hiện tên (§3.1.1,
  NFR-MEM-02).
- **Múi giờ của tài khoản** (BR-MEM-11, ADR-0002): app báo múi giờ của máy làm múi giờ tài khoản; không
  còn múi giờ theo từng mục.
- **Đọc offline, không sửa offline** (FR-MEM-010, §3.6); lỗi server cũng hiện bản offline; đăng xuất xoá
  bản offline.
- **Ảnh chân dung** (FR-MEM-011) là `later` ở mọi nền tảng — Android chỉ hiện chữ viết tắt.
- **Phụ thuộc:**
  - Đăng nhập Android (`ui/screens/login/`) hiện là stub (`LoginViewModel` còn
    `TODO(il-data-builder)`), chưa có spec Android cho Login/phiên. Mọi tiêu chí cần Bearer hoặc
    sự kiện đăng nhập/đăng xuất (AC1–AC3, AC15, AC16, AC19, AC24, AC25, AC29, AC35) bị chặn tới khi
    phiên đăng nhập thật tồn tại.
  - Danh sách Thông báo (ADR-0001) — feature dùng chung, chưa có spec Android; Trang chủ có nút chuông
    (`HomeScreen` `NotificationButton`) và Hồ sơ có hàng "Thông báo" nhưng chưa mở màn nào. AC28 cần
    feature đó.
  - Firebase project (dev/prod) do product owner đăng ký (ADR-0001, Follow-ups "Infrastructure") —
    chặn AC24–AC27.

## User Story

Là người dùng đã đăng nhập, tôi muốn ghi người thân đã mất vào sổ giỗ của mình trên điện thoại
Android, thấy kỳ giỗ tới của từng người và được nhắc trước, để không quên ngày giỗ chạp hằng năm
(product §1.2, §2.2).

## Acceptance Criteria (EARS notation)

### Mở và xem sổ

1. WHEN người dùng đã đăng nhập bấm "Sổ giỗ" ở Trang chủ, THE SYSTEM SHALL mở `GioRoute` và tải sổ
   của tài khoản bằng `GET /v1/memorials` (Bearer), rồi:
   - on success: thay toàn bộ bản offline của tài khoản trên máy bằng kết quả, hiển thị sổ theo
     AC4–AC6;
   - on failure: theo bảng Error → UI (AC32/AC33). (FR-MEM-001, BR-MEM-01)
2. WHEN người dùng chưa đăng nhập mở sổ giỗ, THE SYSTEM SHALL không gọi API, không hiện mục nào (kể
   cả bản offline) và mời người dùng đăng nhập. (FR-MEM-001, BR-MEM-01)
   > ⚠️ Not confirmed: cách "mời đăng nhập" trên Android — đề xuất mặc định: `GioRoute` hiện trạng
   > thái trống có nút "Đăng nhập" mở `LoginRoute`, đăng nhập xong quay lại `GioRoute` — chặn task
   > Phase 2 "Trạng thái khách"; cần màn Login thật (Ràng buộc → Phụ thuộc).
3. WHEN một request sổ giỗ nhận 401 và làm mới phiên thất bại, THE SYSTEM SHALL xử lý như chưa đăng
   nhập (AC2). (BR-MEM-01)
4. WHEN sổ có mục, THE SYSTEM SHALL tính cho mỗi mục kỳ giỗ tới theo BR-MEM-05, BR-MEM-06, BR-MEM-07,
   số ngày còn lại theo BR-MEM-08 và lần giỗ theo BR-MEM-09, với "hôm nay" là ngày của máy và mọi
   quy đổi âm ↔ dương qua `LunarCalendar` (lịch Việt Nam UTC+7, BR-MEM-10); rồi sắp xếp và làm nổi
   bật mục gần nhất theo FR-MEM-002. (FR-MEM-002, BR-MEM-05, BR-MEM-06, BR-MEM-07, BR-MEM-08,
   BR-MEM-09, BR-MEM-10)
5. WHEN hiển thị một mục trong sổ, THE SYSTEM SHALL hiện mối quan hệ, ngày mất đúng như đã nhập (lịch
   và dấu tháng nhuận theo BR-MEM-04, dạng của §3.1.1 "Leap-month choice"), nhãn số ngày còn lại
   (nhấn mạnh theo BR-MEM-08) và dấu "Có nhắc" khi nhắc bật — kể cả khi máy không cho phép push
   (FR-MEM-007); thẻ nổi bật hiện thêm nhãn lần giỗ, ngày dương kèm thứ và tóm tắt nhắc.
   (FR-MEM-002, FR-MEM-007, BR-MEM-04, BR-MEM-08, BR-MEM-09)
6. WHEN sổ trống, THE SYSTEM SHALL hiện trạng thái trống mời thêm người thân và **không** hiện hàng
   "Lấy từ gia phả". (FR-MEM-002)
7. WHEN màn sổ quay lại foreground sau khi ngày của máy đã đổi, THE SYSTEM SHALL tính lại kỳ giỗ, số
   ngày còn lại và thứ tự cho ngày mới. (platform)
8. WHEN sổ có 100 mục, THE SYSTEM SHALL hiện danh sách trong vòng 2 giây trên mạng 4G, và bản xem
   trước của AC10 cập nhật ngay khi một giá trị đổi. (NFR-MEM-01)

### Thêm và sửa

9. WHEN người dùng bấm thêm người thân, THE SYSTEM SHALL mở `GioEditRoute` với đúng giá trị khởi đầu
   của FR-MEM-003. (FR-MEM-003, BR-MEM-11)
10. WHILE người dùng chọn lịch, ngày, tháng, năm hoặc tháng nhuận, THE SYSTEM SHALL hiện bản xem
    trước kỳ giỗ tới theo FR-MEM-006. (FR-MEM-006, BR-MEM-05, BR-MEM-06, BR-MEM-07, BR-MEM-08,
    BR-MEM-09)
11. WHEN form Thêm/sửa hiển thị, THE SYSTEM SHALL cho chọn mối quan hệ trong đúng danh sách của
    BR-MEM-02, lịch âm/dương, ngày và tháng theo miền giá trị của BR-MEM-03, năm mất từ 1900 tới năm
    hiện tại hoặc "Không rõ" (BR-MEM-03), lựa chọn "Tháng nhuận" chỉ khi là âm lịch (BR-MEM-04), ghi
    chú và cấu hình nhắc (AC20). (BR-MEM-02, BR-MEM-03, BR-MEM-04)
12. WHEN người dùng lưu với họ tên rỗng sau khi cắt khoảng trắng, THE SYSTEM SHALL giữ form và hiện
    câu §3.1.1 "Name empty" dưới ô tên, xoá thông báo ngay khi người dùng sửa; WHEN họ tên dài quá
    giới hạn của BR-MEM-02, THE SYSTEM SHALL giữ form và hiện câu §3.1.1 "Name too long"; cả hai
    trường hợp không gửi request. (FR-MEM-012, BR-MEM-02)
13. WHEN người dùng lưu với ghi chú dài quá giới hạn của BR-MEM-02, THE SYSTEM SHALL giữ form, hiện
    câu §3.1.1 "Note too long" và không gửi request; WHEN ghi chú rỗng hoặc toàn khoảng trắng, THE
    SYSTEM SHALL lưu như không có ghi chú. (FR-MEM-013, BR-MEM-02)
14. WHEN người dùng lưu một ngày mất không hợp lệ theo BR-MEM-03 hoặc BR-MEM-04 (kiểm trên máy, quy
    đổi theo BR-MEM-10), THE SYSTEM SHALL giữ form, hiện câu §3.1.1 tương ứng ("Date does not exist",
    "Leap month not in that year", "Year before 1900", "Date after today") và không gửi request.
    (BR-MEM-03, BR-MEM-04, BR-MEM-10)
15. WHEN người dùng lưu một mục mới hợp lệ, THE SYSTEM SHALL đổi ngày mất sang dạng của contract (có
    năm → ngày dương quy đổi theo BR-MEM-10, kể cả tháng nhuận; không năm → ngày/tháng/cờ nhuận theo
    lịch đã nhập), gửi cấu hình nhắc (bật/tắt, số ngày, giờ) **không** kèm múi giờ theo mục và **không**
    kèm cờ Lời nhắc của iOS, gọi `POST /v1/memorials`, rồi:
    - on success: thêm mục vào bản offline, quay về sổ và báo "Đã lưu vào sổ giỗ." (§3.1.1 "Saved");
    - on failure: ở lại form, giữ dữ liệu đã nhập, báo theo bảng Error → UI (sổ đầy, mất mạng, lỗi
      khác). (FR-MEM-003, BR-MEM-01, BR-MEM-02, BR-MEM-11, FR-MEM-010)
    > ⚠️ Not confirmed (contract): `openapi.yaml` @ bf23f8b vẫn có `reminder.timezone` (tuỳ chọn, mặc
    > định `Asia/Ho_Chi_Minh`) và `add_to_os_reminder` (tuỳ chọn, mặc định `false`), và nhận số ngày
    > 0–30, mọi `HH:mm` — Android bỏ trống hai field này và chỉ gửi lựa chọn của BR-MEM-11; khi backend
    > bỏ/thu hẹp chúng (Appendix B mục 2, 3, 6) cần xác nhận không có thay đổi bắt buộc nào khác — chặn
    > task Phase 3 "DTO/Mapper".
16. WHEN người dùng lưu thay đổi của một mục, THE SYSTEM SHALL gọi `PUT /v1/memorials/{id}` với toàn
    bộ nội dung (thay thế toàn phần, như AC15), rồi:
    - on success: thay mục trong bản offline, quay về sổ và báo "Đã lưu vào sổ giỗ.";
    - on failure (`MEMORIAL_NOT_FOUND`): bỏ mục khỏi sổ và bản offline, quay về sổ và báo "Mục này
      không còn trong sổ giỗ." (§3.1.1 "Entry no longer exists");
    - on failure (khác): ở lại form, giữ dữ liệu, báo theo bảng Error → UI. (FR-MEM-004, BR-MEM-01,
      BR-MEM-11)
17. WHEN người dùng rời form bằng back mà không lưu, THE SYSTEM SHALL giữ mục như cũ và không gửi
    request. (FR-MEM-004)
18. WHILE một lần lưu hoặc xoá đang chạy, THE SYSTEM SHALL khoá nút lưu/xoá để một lần bấm chỉ tạo
    một request. (platform)

### Xoá

19. WHEN người dùng bấm "Xóa khỏi sổ giỗ", THE SYSTEM SHALL hỏi xác nhận theo §3.1.1 "Confirm delete",
    rồi:
    - on cancel: không đổi gì;
    - on confirm: gọi `DELETE /v1/memorials/{id}`; on success: bỏ mục khỏi bản offline, quay về sổ và
      báo "Đã xóa {tên} khỏi sổ giỗ." (§3.1.1 "Deleted"); on failure (`MEMORIAL_NOT_FOUND`): bỏ mục
      khỏi sổ và bản offline, quay về sổ và báo "Mục này không còn trong sổ giỗ."; on failure (khác):
      giữ nguyên sổ, báo theo bảng Error → UI. (FR-MEM-005, BR-MEM-12, FR-MEM-010)

### Nhắc

20. WHEN nhắc bật, THE SYSTEM SHALL cho chọn thời điểm và giờ nhắc chỉ trong các lựa chọn của
    BR-MEM-11 và không bao giờ gửi giá trị ngoài danh sách đó; WHEN nhắc tắt, THE SYSTEM SHALL ẩn các
    lựa chọn; cấu hình đọc từ server được hiện đúng như đã lưu ở máy hoặc nền tảng khác. (FR-MEM-007,
    BR-MEM-11)
21. WHEN một mục đọc từ server có số ngày hoặc giờ nhắc ngoài các lựa chọn của BR-MEM-11 (tạo trước
    v2.0), THE SYSTEM SHALL hiện lựa chọn gần nhất và giữ nguyên giá trị đã lưu tới khi người dùng
    lưu mục đó (lúc lưu thì gửi lựa chọn đang hiện). (FR-MEM-007)
    > ⚠️ Not confirmed (product): giá trị cách đều hai lựa chọn (vd. 2 hoặc 5 ngày; 09:30, 15:30) thì
    > "gần nhất" là lựa chọn nào — đề xuất mặc định: lựa chọn nhỏ hơn — chặn task Phase 1
    > "Nearest choice"; hand back to ba-writer in innerlamp-be.
22. WHEN người dùng bật nhắc hoặc lưu một mục có nhắc bật trên Android 13+ (API 33) mà app chưa từng
    xin quyền `POST_NOTIFICATIONS`, THE SYSTEM SHALL xin quyền; kết quả không chặn việc lưu. (platform,
    FR-MEM-008)
23. WHILE máy không cho phép push của InnerLamp (từ chối `POST_NOTIFICATIONS`, hoặc tắt thông báo của
    app/kênh nhắc giỗ trong Cài đặt), THE SYSTEM SHALL hiện ở phần cấu hình nhắc (khi nhắc bật) câu
    §3.1.1 "Notifications not allowed on the device" với nút "Mở Cài đặt" mở trang cài đặt thông báo
    của app, vẫn hiện "Có nhắc" và vẫn lưu cấu hình nhắc như thường. (FR-MEM-008, FR-MEM-007)
24. WHEN người dùng đăng nhập thành công, và WHEN FCM cấp token mới trong lúc đang đăng nhập, THE
    SYSTEM SHALL đăng ký token FCM của máy với backend cho tài khoản đó, thử lại ở lần mở app sau nếu
    thất bại. (FR-MEM-008, ADR-0001)
    > ⚠️ Not confirmed (contract): `openapi.yaml` @ bf23f8b chưa có operation đăng ký / xoá push token
    > (Appendix B mục 8) — chặn task Phase 4 "Push token"; hand-off cho backend.
25. WHEN người dùng đăng xuất, THE SYSTEM SHALL xoá token FCM của máy khỏi backend, huỷ token FCM trên
    máy (để máy không nhận push nữa kể cả khi gọi xoá thất bại vì mất mạng) và xoá bản offline của
    sổ. (FR-MEM-008, FR-MEM-010, NFR-MEM-02)
26. WHEN một push nhắc giỗ tới máy, THE SYSTEM SHALL hiện nó trên kênh thông báo nhắc giỗ với nội dung
    server gửi — chỉ quan hệ và số ngày (§3.1.1 các dòng "Push / notification"), không bao giờ tên
    người thân, kể cả trên màn hình khoá; app không thêm tên hay ghi chú vào thông báo. (FR-MEM-008,
    NFR-MEM-02)
    > ⚠️ Not confirmed (contract): dạng payload push (notification hay data message, khoá xác định mục,
    > channel id) chưa có trong contract (Appendix B mục 7, 8) — chặn task Phase 4 "Messaging service"
    > và "Deep link"; hand-off cho backend.
27. WHEN người dùng bấm một push nhắc giỗ, THE SYSTEM SHALL mở app tại mục đó (`GioRoute` → tải sổ →
    form của mục); khi chưa đăng nhập thì xử lý như AC2; khi không tải được thì mở mục từ bản offline
    (AC32). (FR-MEM-008)
    > ⚠️ Not confirmed (product): mở một nhắc (push hoặc trong danh sách Thông báo) của mục đã bị xoá
    > thì người dùng thấy gì — đề xuất mặc định: mở sổ và báo "Mục này không còn trong sổ giỗ." —
    > chặn task Phase 2 "Mở mục từ nhắc"; hand back to ba-writer in innerlamp-be.
28. WHEN người dùng chọn một nhắc giỗ trong danh sách Thông báo (feature dùng chung, ADR-0001), THE
    SYSTEM SHALL mở mục đó như AC27. (FR-MEM-008)
29. WHEN người dùng đăng nhập thành công, và WHEN app vào foreground mà múi giờ của máy khác múi giờ đã
    báo lần trước, THE SYSTEM SHALL báo múi giờ IANA của máy cho backend làm múi giờ tài khoản, thử
    lại ở lần sau nếu thất bại. (BR-MEM-11, ADR-0002)
    > ⚠️ Not confirmed (contract): `openapi.yaml` @ bf23f8b chưa có cách báo múi giờ tài khoản
    > (Appendix B mục 3; `/v1/users/me` và `POST /v1/auth/login` không có field này) — chặn task
    > Phase 4 "Múi giờ tài khoản"; hand-off cho backend.
30. WHEN sổ được tải, hoặc một mục được lưu hay xoá, THE SYSTEM SHALL không tự đặt nhắc local nào
    (`AlarmManager`, `WorkManager`, thông báo hẹn giờ) cho sổ giỗ — nhắc do server gửi. (platform,
    ADR-0002)
31. WHEN form hoặc sổ hiển thị cấu hình nhắc, THE SYSTEM SHALL không có công tắc "Thêm vào Lời nhắc
    (Reminders)" và không hiện "có trong Lời nhắc". (platform — FR-MEM-009 Removed ở product v2.0)

Công tắc thông báo của tài khoản (ADR-0001; product v2.1 FR-MEM-008) chỉ tắt push trên mọi máy của
tài khoản, nhắc giỗ vẫn vào danh sách Thông báo; công tắc đó thuộc feature Thông báo, không thuộc spec
này. Với sổ giỗ: dấu "Có nhắc" và cấu hình nhắc không đổi theo công tắc đó (FR-MEM-007).

### Offline

32. WHEN `GET /v1/memorials` thất bại vì không tới được server (`AppError.NoConnection` /
    `AppError.Timeout`) hoặc server lỗi (5xx / `AppError.Unknown` / `AppError.Serialization`) và máy có
    bản offline của **chính tài khoản đang đăng nhập**, THE SYSTEM SHALL hiện sổ như lần tải cuối, với
    kỳ giỗ tính cho hôm nay, kèm câu §3.1.1 "Showing the offline copy". (FR-MEM-010, BR-MEM-05)
33. WHEN tải thất bại như AC32 mà máy chưa có bản offline của tài khoản đó, THE SYSTEM SHALL hiện câu
    §3.1.1 "Cannot load, no offline copy" và cho thử lại. (FR-MEM-010)
34. WHEN thêm, sửa hoặc xoá thất bại vì không tới được server, THE SYSTEM SHALL hiện câu §3.1.1
    "Connection needed to change", không đổi sổ hay bản offline, và giữ dữ liệu đang nhập.
    (FR-MEM-010)
35. WHEN một tài khoản khác đăng nhập trên máy (kể cả khi offline), THE SYSTEM SHALL không bao giờ
    hiện bản offline của tài khoản trước. (FR-MEM-010, BR-MEM-01)

### Riêng tư, ảnh, lịch

36. WHEN app gửi hoặc nhận request sổ giỗ, hoặc nhận push, THE SYSTEM SHALL không ghi họ tên, ghi chú
    hay nội dung push vào log (kể cả log HTTP của bản debug) hoặc analytics. (NFR-MEM-02)
37. WHEN lưu bản offline, THE SYSTEM SHALL giữ nó trong bộ nhớ riêng của app, không đưa vào cloud
    backup hay chuyển máy. (NFR-MEM-02, platform)
38. WHEN một mục không có ảnh, THE SYSTEM SHALL hiện chữ viết tắt của tên người thân; thêm/đổi/gỡ ảnh
    chờ FR-MEM-011 (later). (FR-MEM-011)
39. WHEN chạy unit test, THE SYSTEM SHALL qua mọi case của `rules/next-anniversary.json`,
    `rules/anniversary-ordinal.json` và `rules/death-date-conversion.json`; `rules/reminder-moment.json`
    chỉ dành cho backend (product §4.3). (NFR-MEM-03, BR-MEM-03, BR-MEM-04, BR-MEM-05, BR-MEM-06,
    BR-MEM-07, BR-MEM-08, BR-MEM-09, BR-MEM-10)

### Hiện trạng code so với từng AC (đọc ngày 2026-10-14)

Code không đổi so với lần đọc của v1.0 (branch dựa trên `main` @ fbf277f; đã kiểm lại các điểm dưới).

| AC | Hiện trạng | Ghi chú |
|---|---|---|
| 1, 3 | chưa có | Danh sách seed từ `GetMemorialEntriesUseCase`, không gọi API, không cần phiên — **drift BR-MEM-01 / FR-MEM-001** |
| 2 | chưa có | Khách vẫn mở và sửa được sổ — **drift FR-MEM-001** |
| 4 | ✓ phần lớn | `MemorialSchedule.next` + `GioViewModel.buildItems()` (sắp theo `daysUntil`); chưa đối chiếu vector |
| 5 | lệch | Có quan hệ, ngày, nhãn ngày (≤ 7 ngày nhấn mạnh), "Có nhắc", thẻ nổi bật; **thiếu dấu tháng nhuận** (BR-MEM-04) |
| 6 | lệch | `EmptyBook` có hàng "Lấy từ gia phả" (no-op) — **drift FR-MEM-002** (phải ẩn) |
| 7 | chưa có | "Hôm nay" chốt khi dựng ViewModel / lưu |
| 8 | chưa đo | |
| 9 | lệch | Mặc định đúng FR-MEM-003 nhưng có `syncReminders = true` (AC31) |
| 10 | ✓ | `GioFormat.preview` |
| 11 | lệch | Năm chỉ có hiện tại lùi 96 năm (**drift BR-MEM-03**); ngày dương luôn 1–31; **không có lựa chọn tháng nhuận** (drift BR-MEM-04) |
| 12 | lệch | Tên rỗng ✓ ("Vui lòng nhập họ và tên", xoá khi sửa bất kỳ trường); **không giới hạn 100 ký tự** |
| 13 | chưa có | Không giới hạn 1000 ký tự |
| 14 | chưa có | Không kiểm ngày tồn tại / tương lai / năm < 1900; `MemorialSchedule` kẹp 31/4 → 30/4 |
| 15 | lệch | Lưu vào list trong bộ nhớ và báo "Đã lưu vào sổ giỗ."; không gọi API |
| 16 | lệch | Thay trong bộ nhớ; không gọi API, không nhánh 404 |
| 17 | ✓ | `discardDraft()` qua back / `BackHandler` |
| 18 | chưa có | Không có trạng thái đang lưu |
| 19 | lệch | **Xoá ngay không hỏi** (drift BR-MEM-12); flash "Đã xóa <tên> khỏi sổ giỗ." ✓ |
| 20 | ✓ phần UI | Lựa chọn đúng 0/1/3/7 ngày × 06:00/07:00/12:00/19:00, ẩn khi tắt; chưa đọc từ server |
| 21 | chưa có | Chưa đọc từ server |
| 22–29 | chưa có | Không có Firebase/FCM, `POST_NOTIFICATIONS`, đăng ký token, deep link, báo múi giờ (`AndroidManifest.xml` chỉ có `USE_BIOMETRIC`) |
| 30 | ✓ | Không có lịch nhắc local nào |
| 31 | lệch | Có công tắc "Thêm vào Lời nhắc (Reminders)" (`GioEditScreen.kt`) và nhãn "· có trong Lời nhắc" (`GioFormat.kt`) — **drift FR-MEM-009 Removed** |
| 32–35 | chưa có | Không có bản offline |
| 36 | chưa có | `LoggingInterceptor` log `BODY` ở debug — sẽ lộ tên/ghi chú khi có API |
| 37 | chưa có | `innerlamp.db` hiện được cloud backup |
| 38 | ✓ | `IlAvatar(initials = initialsOf(name))` |
| 39 | chưa có | `MemorialScheduleTest` có test riêng, chưa chạy vector |

## Dữ liệu & API

Nguồn đúng là `openapi.yaml`; không chép schema ở đây.

| Thao tác | Operation (openapi) | Auth | Lưu cục bộ | Ghi chú |
|---|---|---|---|---|
| Tải sổ | `GET /v1/memorials` (`memorialsList`) | Bearer (`@Authenticated`) | Room: thay toàn bộ bản offline của tài khoản | Trả toàn bộ (≤ 100), không phân trang; app tự sắp theo kỳ giỗ |
| Thêm | `POST /v1/memorials` (`memorialsCreate`) | Bearer | Room: thêm mục | Ngày mất hai dạng (có năm / không năm); không gửi `reminder.timezone`, `add_to_os_reminder` (⚠️ AC15) |
| Sửa | `PUT /v1/memorials/{id}` (`memorialsUpdate`) | Bearer | Room: thay mục | Thay toàn bộ — thiếu `reminder` nghĩa là tắt nhắc |
| Xoá | `DELETE /v1/memorials/{id}` (`memorialsDelete`) | Bearer | Room: xoá mục | Xoá cứng |
| Xem một mục | `GET /v1/memorials/{id}` (`memorialsGet`) | Bearer | — | Không dùng: form mở từ dữ liệu của danh sách (kể cả khi mở từ nhắc, AC27) |
| Đăng ký / xoá push token | — | Bearer | — | ⚠️ Not confirmed (contract) — AC24, AC25 (Appendix B mục 8) |
| Báo múi giờ tài khoản | — | Bearer | prefs: múi giờ đã báo | ⚠️ Not confirmed (contract) — AC29 (Appendix B mục 3) |
| Payload push nhắc giỗ | — (FCM) | — | — | ⚠️ Not confirmed (contract) — AC26, AC27 |
| Danh sách Thông báo | — | Bearer | — | Feature Thông báo (ngoài spec này); ⚠️ contract (Appendix B mục 7) |
| Ảnh | — | — | — | Chưa có trong contract (FR-MEM-011 `later`) |

## Error → UI mapping

Câu chữ lấy nguyên văn từ product §3.1.1, trích theo tên dòng; không chép lại ở đây.

| Operation | Failure case | User sees | Hiện trạng code |
|---|---|---|---|
| `GET /v1/memorials` | `AppError.Unauthorized` (401 sau khi làm mới phiên thất bại) | Trạng thái khách, mời đăng nhập (AC2) | chưa có |
| `GET /v1/memorials` | `NoConnection` / `Timeout` / 5xx / `Unknown` / `Serialization`, có bản offline của tài khoản | Sổ như lần tải cuối + §3.1.1 "Showing the offline copy" (AC32) | chưa có |
| `GET /v1/memorials` | Như trên, không có bản offline | §3.1.1 "Cannot load, no offline copy" + nút thử lại (AC33) | chưa có |
| `POST /v1/memorials` | 409 `MEMORIAL_LIMIT_REACHED` | Ở lại form, §3.1.1 "Book is full" | chưa có |
| `POST` / `PUT` | 400 `VALIDATION_ERROR` (lẽ ra AC12–AC14 đã chặn) | Ở lại form, §3.1.1 "Could not save"; `details.field` = `full_name` → hiện dưới ô tên | chưa có |
| `POST` / `PUT` / `DELETE` | `NoConnection` / `Timeout` | Ở lại, §3.1.1 "Connection needed to change", sổ không đổi (AC34) | chưa có |
| `PUT` / `DELETE` | 404 `MEMORIAL_NOT_FOUND` | Mục biến khỏi sổ và bản offline, quay về sổ, §3.1.1 "Entry no longer exists" (AC16, AC19) | chưa có |
| `POST` / `PUT` / `DELETE` | `AppError.Unauthorized` | Trạng thái khách (AC2); dữ liệu đang nhập mất | chưa có |
| `POST` / `PUT` / `DELETE` | 5xx / `Unknown` / `Serialization` | Ở lại, §3.1.1 "Could not save" | chưa có |
| (local) | Tên rỗng / quá dài, ghi chú quá dài, ngày không hợp lệ | Các dòng §3.1.1 tương ứng (AC12–AC14) | tên rỗng ✓, còn lại chưa có |
| (local) | Máy không cho phép push | §3.1.1 "Notifications not allowed on the device" + "Mở Cài đặt" (AC23) | chưa có |
| Đăng ký token / báo múi giờ | Mọi lỗi | Không hiện gì; thử lại lần sau (AC24, AC29) | chưa có |

## Out of scope

- Thêm vào ứng dụng Lời nhắc / lịch của hệ thống (FR-MEM-009 Removed) — kể cả
  `CalendarContract`/Google Calendar.
- Nhắc local trên máy (ADR-0002); tính thời điểm nhắc, nhắc trễ (BR-MEM-11 phần thời điểm, BR-MEM-13,
  NFR-MEM-04 — backend).
- Màn danh sách Thông báo, số chưa đọc, đánh dấu đã đọc, công tắc thông báo của tài khoản — feature
  Thông báo dùng chung (ADR-0001), chưa có spec Android; spec này chỉ định nghĩa việc mở mục từ một
  nhắc giỗ (AC28).
- Tạo mục từ Gia phả (`genealogy`, §1.2).
- Chia sẻ sổ, tìm kiếm, nhiều ảnh, xoá mềm/khôi phục, xuất dữ liệu (§1.2).
- Đăng nhập, lưu token, làm mới phiên — thuộc feature Login/`core/network` (chưa có spec Android);
  spec này chỉ móc vào sự kiện đăng nhập/đăng xuất (AC24, AC25, AC29).
- Migrate dữ liệu seed/in-memory hiện tại (§2.7 — bỏ).

## Pending (not yet implemented)

- Sổ theo tài khoản qua backend + bản offline (AC1–AC3, AC15–AC16, AC19, AC32–AC35) — `tasks.md`
  Phase 1, Phase 3; chặn bởi đăng nhập Android.
- Tháng nhuận, khoảng năm 1900 → nay, kiểm ngày, giới hạn tên/ghi chú (AC11–AC14) — Phase 1, Phase 2.
- Xác nhận trước khi xoá (AC19) — Phase 2.
- Ẩn "Lấy từ gia phả" (AC6), bỏ công tắc "Lời nhắc" (AC31) — Phase 2.
- Lựa chọn gần nhất cho giá trị nhắc cũ (AC21) — Phase 1, Phase 2.
- Push FCM: quyền, gợi ý "Mở Cài đặt", token, hiện push, mở mục (AC22–AC28) — Phase 2, Phase 4;
  chặn bởi contract và Firebase project.
- Báo múi giờ tài khoản (AC29) — Phase 4; chặn bởi contract.
- Riêng tư log/backup (AC36–AC37) — Phase 3.
- Chạy vector (AC39) — Phase 5.
- Ảnh (AC38 phần ảnh, FR-MEM-011) — Deferred.

## Open questions (resolved)

Theo product spec v2.0/v2.1 (2026-10-14):

- ~~Thời điểm nhắc của kỳ giỗ tới đã qua~~ → không nhắc kỳ này, lập kỳ sau (BR-MEM-11) — server làm;
  Android không còn planner. Resolved 2026-10-14.
- ~~Nội dung thông báo và tên trên màn hình khoá~~ → chỉ quan hệ, không bao giờ tên (§3.1.1, FR-MEM-008,
  NFR-MEM-02) → AC26. Resolved 2026-10-14.
- ~~Từ chối quyền thông báo~~ → nhắc vẫn trong danh sách Thông báo, có gợi ý "Mở Cài đặt", "Có nhắc"
  vẫn hiện (FR-MEM-007, FR-MEM-008) → AC5, AC23. Resolved 2026-10-14.
- ~~Đăng xuất / đổi tài khoản trên cùng máy~~ → ngừng push tới máy, xoá bản offline (FR-MEM-008,
  FR-MEM-010) → AC25, AC35. Resolved 2026-10-14.
- ~~`DELETE`/`PUT` trả "không tìm thấy"~~ → bỏ khỏi sổ, báo "Mục này không còn trong sổ giỗ."
  (FR-MEM-004, FR-MEM-005) → AC16, AC19. Resolved 2026-10-14.
- ~~`add_to_os_reminder` khi sửa trên Android~~ → FR-MEM-009 Removed, contract sẽ bỏ field (Appendix B
  mục 6); Android không gửi (AC15). Resolved 2026-10-14.
- ~~Hàng "Lấy từ gia phả" ở sổ trống~~ → ẩn (FR-MEM-002) → AC6. Resolved 2026-10-14.
- ~~Lỗi 5xx khi tải sổ~~ → hiện bản offline kèm câu báo (FR-MEM-010) → AC32. Resolved 2026-10-14.
- ~~Câu tiếng Việt cho thông báo lỗi~~ → product §3.1.1. Resolved 2026-10-14.
- ~~Exact alarm hay không~~ → không còn áp dụng: Android không đặt nhắc local (ADR-0002, AC30).
  Resolved 2026-10-14.
- ~~Công tắc thông báo của tài khoản chỉ tắt push hay cả danh sách~~ → chỉ tắt push, trên mọi máy của
  tài khoản; nhắc vẫn vào danh sách Thông báo (product owner 2026-10-14, product v2.1 FR-MEM-008).
  Không đổi AC nào của Android (công tắc thuộc feature Thông báo). Resolved 2026-10-14.

Từ v1.0 (giả định cũ ở `Docs/gio_assumptions.md`, product spec v1.0):

- ~~#1 Sổ giữ trong bộ nhớ, seed 3 người mẫu~~ → sổ thuộc tài khoản, dữ liệu cũ bỏ (BR-MEM-01, §2.7),
  đọc offline (FR-MEM-010). Resolved 2026-10-14.
- ~~#2 Nhắc chưa lên lịch~~ → v1.0: nhắc local; **v2.0: server gửi** (ADR-0002, FR-MEM-008).
  Resolved 2026-10-14.
- ~~#3 "Thêm vào Lời nhắc (Reminders)" trên Android~~ → bỏ (FR-MEM-009 Removed, AC31). Resolved
  2026-10-14.
- ~~#4 Ảnh là chữ viết tắt~~ → ảnh `later` (FR-MEM-011); chữ viết tắt khi không có ảnh (AC38).
  Resolved 2026-10-14.
- ~~#5 Hàng "Lấy từ gia phả" no-op~~ → ẩn (FR-MEM-002, AC6). Resolved 2026-10-14.
- ~~#6 Ngày 30 tháng thiếu, 29/2 năm thường~~ → BR-MEM-06, BR-MEM-07. Resolved 2026-10-14.
- ~~#7 Không có tháng nhuận~~ → ghi được tháng nhuận, giỗ luôn ở tháng thường (BR-MEM-04, BR-MEM-05).
  Resolved 2026-10-14.
- ~~#8 Lần giỗ; năm mất lùi 96 năm~~ → BR-MEM-09; năm 1900 → năm hiện tại (BR-MEM-03). Resolved
  2026-10-14.
- ~~#9 "Hôm nay" không tự làm mới~~ → "hôm nay" là ngày của máy (BR-MEM-08); làm mới khi quay lại
  foreground là quyết định Android (AC7). Resolved 2026-10-14.
- ~~#10, #11 ViewModel dùng chung qua back stack; `DropdownMenu` thay `<select>`~~ → quyết định
  Android, ghi ở `design.md`. Resolved 2026-10-14.
