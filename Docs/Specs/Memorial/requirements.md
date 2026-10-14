# Requirements — Memorial

Version: 1.0
Last updated: 2026-10-14
Status: in-progress — UI (`ui/screens/gio/`) và tính kỳ giỗ (`domain/calendar/MemorialSchedule.kt`) đã có, nhưng sổ chỉ giữ trong bộ nhớ ViewModel (seed 3 người mẫu), chưa đăng nhập, chưa gọi backend, chưa lưu offline, chưa đặt nhắc; lệch product ở tháng nhuận, khoảng năm, xác nhận xoá và công tắc "Lời nhắc" chỉ dành cho iOS
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/gio/` (tên cũ `gio`, product §1.5)
Product spec: innerlamp-be `docs/product/memorial/product.md` @ v1.0
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ 5f4ca56 (info.version 1.0.0)
Implements: BR-MEM-01, BR-MEM-02, BR-MEM-03, BR-MEM-04, BR-MEM-05, BR-MEM-06, BR-MEM-07, BR-MEM-08, BR-MEM-09, BR-MEM-10, BR-MEM-11, BR-MEM-12, FR-MEM-001, FR-MEM-002, FR-MEM-003, FR-MEM-004, FR-MEM-005, FR-MEM-006, FR-MEM-007, FR-MEM-008, FR-MEM-010, FR-MEM-011 (planned), FR-MEM-012, FR-MEM-013, NFR-MEM-01, NFR-MEM-02, NFR-MEM-03

> See `changelog.md` in this folder for the version history of this spec.

Spec này được viết retroactively từ code đang chạy (đọc ngày 2026-10-14), đối chiếu với product spec
v1.0. Đây là spec platform (layer 2) của **Sổ giỗ chạp** cho Android. Hành vi sản phẩm (sổ riêng
theo tài khoản, nội dung một mục, ngày mất, tháng nhuận, kỳ giỗ, nhắc, xoá vĩnh viễn, đọc offline)
nằm ở product spec và **không** nhắc lại ở đây; file này chỉ nói app Android hiện thực các
`BR-MEM-*`/`FR-MEM-*` đó thế nào. FR-MEM-009 (thêm vào ứng dụng Lời nhắc) là `n/a` cho Android
(product §3.6).

`Docs/gio_assumptions.md` đã bị thay thế bởi các quyết định của product spec — xem "Open questions
(resolved)" bên dưới và `changelog.md`.

## Ràng buộc từ product spec

- **Sổ thuộc tài khoản** (BR-MEM-01, §2.7): sổ chỉ đọc/ghi qua backend bằng phiên đăng nhập; dữ liệu
  seed/in-memory hiện tại bị bỏ, không migrate (§2.7, §3.5).
- **App tự tính, server chỉ lưu** (§2.7, §3.6): kỳ giỗ tới, số ngày còn lại, lần giỗ và quy đổi
  âm ↔ dương (BR-MEM-05…10) tính trên máy; nhắc là thông báo local do app tự đặt (FR-MEM-008).
- **Đọc offline, không sửa offline** (FR-MEM-010, §3.6).
- **Không có ứng dụng Lời nhắc của OS** trên Android (FR-MEM-009 chỉ iOS, §3.6).
- **Ảnh chân dung** (FR-MEM-011) là `later` ở mọi nền tảng — Android chỉ hiện chữ viết tắt.
- **Phụ thuộc:** đăng nhập Android (`ui/screens/login/`) hiện là stub (`LoginViewModel` còn
  `TODO(il-data-builder)`), chưa có spec Android cho Login/phiên. Mọi tiêu chí cần Bearer (AC1–AC3,
  AC15, AC16, AC19) bị chặn tới khi phiên đăng nhập thật tồn tại.

## User Story

Là người dùng đã đăng nhập, tôi muốn ghi người thân đã mất vào sổ giỗ của mình trên điện thoại
Android, thấy kỳ giỗ tới của từng người và được nhắc trước, để không quên ngày giỗ chạp hằng năm
(product §1.2, §2.2).

## Acceptance Criteria (EARS notation)

### Mở và xem sổ

1. WHEN người dùng đã đăng nhập bấm "Sổ giỗ" ở Trang chủ, THE SYSTEM SHALL mở `GioRoute` và tải sổ
   của tài khoản bằng `GET /v1/memorials` (Bearer), rồi:
   - on success: thay toàn bộ bản offline trên máy bằng kết quả, hiển thị sổ theo AC4–AC6 và lên
     lịch lại nhắc theo AC22;
   - on failure: theo bảng Error → UI (offline → AC27/AC28). (FR-MEM-001, BR-MEM-01)
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
   và dấu tháng nhuận theo BR-MEM-04), nhãn số ngày còn lại (nhấn mạnh theo BR-MEM-08) và dấu "Có
   nhắc" khi nhắc bật; thẻ nổi bật hiện thêm nhãn lần giỗ, ngày dương kèm thứ và tóm tắt nhắc.
   (FR-MEM-002, FR-MEM-007, BR-MEM-04, BR-MEM-08, BR-MEM-09)
6. WHEN sổ trống, THE SYSTEM SHALL hiện trạng thái trống mời thêm người thân. (FR-MEM-002)
   > ⚠️ Not confirmed (product): trạng thái trống hiện có hàng "Lấy từ gia phả" (bấm không làm gì) —
   > product §1.2 để luồng này cho `genealogy`; có ẩn hàng này tới khi genealogy có không — chặn task
   > Phase 2 "Hàng Gia phả"; hand back to ba-writer in innerlamp-be.
7. WHEN màn sổ quay lại foreground sau khi ngày của máy đã đổi, THE SYSTEM SHALL tính lại kỳ giỗ, số
   ngày còn lại và thứ tự cho ngày mới. (platform)
8. WHEN sổ có 100 mục, THE SYSTEM SHALL hiện danh sách trong vòng 2 giây trên mạng 4G, và bản xem
   trước của AC10 cập nhật ngay khi một giá trị đổi. (NFR-MEM-01)

### Thêm và sửa

9. WHEN người dùng bấm thêm người thân, THE SYSTEM SHALL mở `GioEditRoute` với giá trị khởi đầu của
   FR-MEM-003 (phần dành cho iOS không áp dụng — AC26). (FR-MEM-003, BR-MEM-11)
10. WHILE người dùng chọn lịch, ngày, tháng, năm hoặc tháng nhuận, THE SYSTEM SHALL hiện bản xem
    trước kỳ giỗ tới theo FR-MEM-006. (FR-MEM-006, BR-MEM-05, BR-MEM-06, BR-MEM-07, BR-MEM-08,
    BR-MEM-09)
11. WHEN form Thêm/sửa hiển thị, THE SYSTEM SHALL cho chọn mối quan hệ trong đúng danh sách của
    BR-MEM-02, lịch âm/dương, ngày và tháng theo miền giá trị của BR-MEM-03, năm mất từ 1900 tới năm
    hiện tại hoặc "Không rõ" (BR-MEM-03), lựa chọn tháng nhuận chỉ khi là âm lịch (BR-MEM-04), ghi chú
    và cấu hình nhắc (AC20). (BR-MEM-02, BR-MEM-03, BR-MEM-04)
12. WHEN người dùng lưu với họ tên rỗng sau khi cắt khoảng trắng, THE SYSTEM SHALL giữ form và hiện
    thông báo của FR-MEM-012 dưới ô tên, xoá thông báo ngay khi người dùng sửa; WHEN họ tên dài quá
    giới hạn của BR-MEM-02, THE SYSTEM SHALL giữ form và báo tên quá dài; cả hai trường hợp không gửi
    request. (FR-MEM-012, BR-MEM-02)
13. WHEN người dùng lưu với ghi chú dài quá giới hạn của BR-MEM-02, THE SYSTEM SHALL giữ form, báo
    ghi chú quá dài và không gửi request; WHEN ghi chú rỗng hoặc toàn khoảng trắng, THE SYSTEM SHALL
    lưu như không có ghi chú. (FR-MEM-013, BR-MEM-02)
14. WHEN người dùng lưu một ngày mất không hợp lệ theo BR-MEM-03 hoặc BR-MEM-04 (kiểm trên máy, quy
    đổi theo BR-MEM-10), THE SYSTEM SHALL giữ form, báo ngày không hợp lệ (hoặc không tồn tại, với
    tháng nhuận) và không gửi request. (BR-MEM-03, BR-MEM-04, BR-MEM-10)
15. WHEN người dùng lưu một mục mới hợp lệ, THE SYSTEM SHALL đổi ngày mất sang dạng của contract (có
    năm → ngày dương quy đổi theo BR-MEM-10, kể cả tháng nhuận; không năm → ngày/tháng/cờ nhuận theo
    lịch đã nhập), gửi cấu hình nhắc kèm múi giờ của máy, gọi `POST /v1/memorials`, rồi:
    - on success: thêm mục vào bản offline, lên lịch nhắc (AC22), quay về sổ và báo "Đã lưu vào sổ
      giỗ.";
    - on failure: ở lại form, giữ dữ liệu đã nhập, báo theo bảng Error → UI (sổ đầy, mất mạng, lỗi
      khác). (FR-MEM-003, BR-MEM-01, BR-MEM-02, BR-MEM-11, FR-MEM-010)
16. WHEN người dùng lưu thay đổi của một mục, THE SYSTEM SHALL gọi `PUT /v1/memorials/{id}` với toàn
    bộ nội dung (thay thế toàn phần), gửi lại nguyên cờ `add_to_os_reminder` đã đọc từ server, rồi:
    - on success: thay mục trong bản offline, lên lịch lại nhắc của mục (AC22), quay về sổ và báo đã
      lưu;
    - on failure (`MEMORIAL_NOT_FOUND`): báo mục không còn tồn tại, bỏ mục khỏi sổ và bản offline, huỷ
      nhắc của mục, quay về sổ;
    - on failure (khác): ở lại form, giữ dữ liệu, báo theo bảng Error → UI. (FR-MEM-004, BR-MEM-01,
      BR-MEM-11)
    > ⚠️ Not confirmed (product): Android không hiện FR-MEM-009 nhưng `PUT` thay toàn bộ — có giữ
    > nguyên `add_to_os_reminder` do iOS đặt khi sửa trên Android (đề xuất: giữ, như Appendix B mục 6
    > cho website) và mục tạo trên Android gửi `false` — chặn task Phase 3 "Mapper"; hand back to
    > ba-writer in innerlamp-be.
17. WHEN người dùng rời form bằng back mà không lưu, THE SYSTEM SHALL giữ mục như cũ và không gửi
    request. (FR-MEM-004)
18. WHILE một lần lưu hoặc xoá đang chạy, THE SYSTEM SHALL khoá nút lưu/xoá để một lần bấm chỉ tạo
    một request. (platform)

### Xoá

19. WHEN người dùng bấm "Xóa khỏi sổ giỗ", THE SYSTEM SHALL hỏi xác nhận, rồi:
    - on cancel: không đổi gì;
    - on confirm: gọi `DELETE /v1/memorials/{id}`; on success: bỏ mục khỏi bản offline, huỷ nhắc của
      mục, quay về sổ và báo "Đã xóa <tên> khỏi sổ giỗ."; on failure: giữ nguyên sổ, báo theo bảng
      Error → UI. (FR-MEM-005, BR-MEM-12, FR-MEM-010)
    > ⚠️ Not confirmed (product): khi `DELETE` trả `MEMORIAL_NOT_FOUND` (đã xoá ở máy khác) người dùng
    > thấy gì — chặn dòng tương ứng của bảng Error → UI; hand back to ba-writer in innerlamp-be.

### Nhắc

20. WHEN nhắc bật, THE SYSTEM SHALL cho chọn thời điểm và giờ nhắc chỉ trong các lựa chọn của
    BR-MEM-11 và không bao giờ gửi giá trị ngoài danh sách đó; WHEN nhắc tắt, THE SYSTEM SHALL ẩn các
    lựa chọn; cấu hình đọc từ server được hiện đúng như đã lưu ở máy khác. (FR-MEM-007, BR-MEM-11)
21. WHEN người dùng lần đầu bật nhắc (hoặc lưu một mục có nhắc) mà app chưa có quyền
    `POST_NOTIFICATIONS`, THE SYSTEM SHALL xin quyền. (platform)
    > ⚠️ Not confirmed (product): người dùng từ chối quyền thông báo (hoặc tắt thông báo của app) thì
    > thấy gì, và dấu "Có nhắc" còn hiện không — chặn task Phase 4 "Quyền thông báo"; hand back to
    > ba-writer in innerlamp-be.
22. WHEN sổ được tải thành công, hoặc một mục được lưu hay xoá, THE SYSTEM SHALL lập lại lịch nhắc
    local: mỗi mục có nhắc bật có đúng một nhắc tại thời điểm suy ra từ kỳ giỗ tới (BR-MEM-05) theo
    BR-MEM-11, tính theo giờ địa phương của máy; mục tắt nhắc, đã xoá hoặc không còn trong sổ không
    còn nhắc nào. (FR-MEM-008, BR-MEM-05, BR-MEM-11)
    > ⚠️ Not confirmed (product): khi thời điểm nhắc của kỳ giỗ tới đã qua (vd. nhắc trước 7 ngày, kỳ
    > giỗ còn 3 ngày) thì nhắc ngay, bỏ qua sang kỳ sau, hay không nhắc — chặn task Phase 1 "Planner";
    > hand back to ba-writer in innerlamp-be.
23. WHEN tới thời điểm nhắc, THE SYSTEM SHALL hiện một thông báo về ngày giỗ của người thân đó, rồi lập
    nhắc cho kỳ giỗ kế tiếp. (FR-MEM-008, BR-MEM-11)
    > ⚠️ Not confirmed (product): nội dung thông báo (tiêu đề/nội dung) và việc có hiện tên người thân
    > trên màn hình khoá hay không (NFR-MEM-02) — chặn task Phase 4 "Thông báo"; hand back to
    > ba-writer in innerlamp-be.
24. WHEN máy khởi động lại, đổi múi giờ, đổi giờ hệ thống hoặc app được cập nhật, THE SYSTEM SHALL lập
    lại lịch nhắc từ bản offline mà không cần mạng. (platform, FR-MEM-008)
25. WHEN người dùng đăng xuất, THE SYSTEM SHALL huỷ mọi nhắc sổ giỗ trên máy và xoá bản offline.
    (platform, FR-MEM-008, NFR-MEM-02)
    > ⚠️ Not confirmed (product): product chỉ nói nhắc tới "mọi máy đang đăng nhập" — xác nhận đăng
    > xuất / đổi tài khoản trên cùng máy thì dừng nhắc và bỏ bản offline — chặn task Phase 4 "Đăng
    > xuất"; hand back to ba-writer in innerlamp-be.
26. WHEN form hoặc sổ hiển thị cấu hình nhắc, THE SYSTEM SHALL không có công tắc "Thêm vào Lời nhắc
    (Reminders)" và không hiện "có trong Lời nhắc". (platform — product §3.6, FR-MEM-009 iOS only)

### Offline

27. WHEN `GET /v1/memorials` thất bại vì không tới được server (`AppError.NoConnection` /
    `AppError.Timeout`) và máy có bản offline, THE SYSTEM SHALL hiện sổ như lần tải cuối, với kỳ giỗ
    tính cho hôm nay, kèm báo đang xem offline. (FR-MEM-010, BR-MEM-05)
28. WHEN tải thất bại như AC27 mà máy chưa có bản offline, THE SYSTEM SHALL báo không tải được sổ khi
    không có kết nối và cho thử lại. (FR-MEM-010)
29. WHEN thêm, sửa hoặc xoá thất bại vì không tới được server, THE SYSTEM SHALL báo cần kết nối, không
    đổi sổ hay bản offline, và giữ dữ liệu đang nhập. (FR-MEM-010)

### Riêng tư, ảnh, lịch

30. WHEN app gửi hoặc nhận request sổ giỗ, THE SYSTEM SHALL không ghi họ tên hay ghi chú vào log
    (kể cả log HTTP của bản debug) hoặc analytics. (NFR-MEM-02)
31. WHEN lưu bản offline, THE SYSTEM SHALL giữ nó trong bộ nhớ riêng của app, không đưa vào cloud
    backup hay chuyển máy. (NFR-MEM-02, platform)
32. WHEN một mục không có ảnh, THE SYSTEM SHALL hiện chữ viết tắt của tên người thân; thêm/đổi/gỡ ảnh
    chờ FR-MEM-011 (later). (FR-MEM-011)
33. WHEN chạy unit test, THE SYSTEM SHALL qua mọi case của `rules/next-anniversary.json`,
    `rules/anniversary-ordinal.json` và `rules/death-date-conversion.json`. (NFR-MEM-03, BR-MEM-03,
    BR-MEM-04, BR-MEM-05, BR-MEM-06, BR-MEM-07, BR-MEM-08, BR-MEM-09, BR-MEM-10)

### Hiện trạng code so với từng AC (đọc ngày 2026-10-14)

| AC | Hiện trạng | Ghi chú |
|---|---|---|
| 1, 3 | chưa có | Danh sách seed từ `GetMemorialEntriesUseCase`, không gọi API, không cần phiên — **drift BR-MEM-01 / FR-MEM-001** |
| 2 | chưa có | Khách vẫn mở và sửa được sổ — **drift FR-MEM-001** |
| 4 | ✓ phần lớn | `MemorialSchedule.next` + `GioViewModel.buildItems()` (sắp theo `daysUntil`); chưa đối chiếu vector |
| 5 | lệch | Có quan hệ, ngày, nhãn ngày (≤ 7 ngày nhấn mạnh), "Có nhắc", thẻ nổi bật; **thiếu dấu tháng nhuận** (BR-MEM-04) |
| 6 | ✓ | `EmptyBook`; hàng "Lấy từ gia phả" là no-op (⚠️ AC6) |
| 7 | chưa có | "Hôm nay" chốt khi dựng ViewModel / lưu |
| 8 | chưa đo | |
| 9 | lệch | Mặc định đúng FR-MEM-003 nhưng `syncReminders = true` (công tắc iOS — AC26) |
| 10 | ✓ | `GioFormat.preview` (thứ, ngày, số ngày; âm: "Tức …", lần giỗ, câu ngày 30; dương: ngày âm năm nay) |
| 11 | lệch | Năm chỉ có hiện tại lùi 96 năm (**drift BR-MEM-03**); ngày dương luôn 1–31 bất kể tháng; **không có lựa chọn tháng nhuận** (drift BR-MEM-04) |
| 12 | lệch | Tên rỗng ✓ ("Vui lòng nhập họ và tên", xoá khi sửa bất kỳ trường); **không giới hạn 100 ký tự** |
| 13 | chưa có | Không giới hạn 1000 ký tự |
| 14 | chưa có | Không kiểm ngày tồn tại / tương lai / năm < 1900; `MemorialSchedule` kẹp 31/4 → 30/4 |
| 15 | lệch | Lưu vào list trong bộ nhớ và báo "Đã lưu vào sổ giỗ."; không gọi API |
| 16 | lệch | Thay trong bộ nhớ; không gọi API, không nhánh 404 |
| 17 | ✓ | `discardDraft()` qua back / `BackHandler` |
| 18 | chưa có | Không có trạng thái đang lưu |
| 19 | lệch | **Xoá ngay không hỏi** (drift BR-MEM-12); flash "Đã xóa <tên> khỏi sổ giỗ." ✓ |
| 20 | ✓ phần UI | Lựa chọn đúng 0/1/3/7 ngày × 06:00/07:00/12:00/19:00, ẩn khi tắt; chưa đọc từ server |
| 21–25 | chưa có | Không có lịch nhắc, thông báo, quyền, receiver |
| 26 | lệch | Có công tắc "Thêm vào Lời nhắc (Reminders)" và nhãn "· có trong Lời nhắc" — **drift §3.6** |
| 27–29 | chưa có | Không có bản offline |
| 30 | chưa có | `LoggingInterceptor` log body ở debug — sẽ lộ tên/ghi chú khi có API |
| 31 | chưa có | `innerlamp.db` hiện được cloud backup |
| 32 | ✓ | `IlAvatar(initials = initialsOf(name))` |
| 33 | chưa có | `MemorialScheduleTest` có test riêng, chưa chạy vector |

## Dữ liệu & API

Nguồn đúng là `openapi.yaml` (tag Memorials); không chép schema ở đây.

| Thao tác | Operation (openapi) | Auth | Lưu cục bộ | Ghi chú |
|---|---|---|---|---|
| Tải sổ | `GET /v1/memorials` (`memorialsList`) | Bearer (`@Authenticated`) | Room: thay toàn bộ bản offline | Trả toàn bộ (≤ 100), không phân trang; app tự sắp theo kỳ giỗ |
| Thêm | `POST /v1/memorials` (`memorialsCreate`) | Bearer | Room: thêm mục | Ngày mất hai dạng (có năm / không năm), cờ nhuận chỉ ở dạng không năm; dạng có năm khôi phục cờ nhuận khi đổi dương → âm |
| Sửa | `PUT /v1/memorials/{id}` (`memorialsUpdate`) | Bearer | Room: thay mục | Thay toàn bộ — thiếu `reminder` nghĩa là xoá nhắc |
| Xoá | `DELETE /v1/memorials/{id}` (`memorialsDelete`) | Bearer | Room: xoá mục | Xoá cứng |
| Xem một mục | `GET /v1/memorials/{id}` (`memorialsGet`) | Bearer | — | Không dùng: form mở từ dữ liệu của danh sách |
| Ảnh | — | — | — | Chưa có trong contract (FR-MEM-011 `later`) |

## Error → UI mapping

Câu chữ đã có trong product được dùng nguyên văn; các dòng ghi "(câu chữ ⚠️)" chỉ có nghĩa, chưa có
câu tiếng Việt chốt:

> ⚠️ Not confirmed (product): câu tiếng Việt cho các thông báo "sổ đầy", "ngày không hợp lệ / không
> tồn tại", "tên quá dài", "ghi chú quá dài", "không lưu được", "cần kết nối", "mục không còn tồn
> tại", "không tải được sổ khi không có kết nối" — product chỉ nêu nghĩa, prototype không có — chặn
> task Phase 2 "Thông báo lỗi"; hand back to ba-writer in innerlamp-be.

| Operation | Failure case | User sees | Hiện trạng code |
|---|---|---|---|
| `GET /v1/memorials` | `AppError.Unauthorized` (401 sau khi làm mới phiên thất bại) | Trạng thái khách, mời đăng nhập (AC2) | chưa có |
| `GET /v1/memorials` | `AppError.NoConnection` / `Timeout`, có bản offline | Sổ như lần tải cuối + báo đang xem offline (AC27) | chưa có |
| `GET /v1/memorials` | `AppError.NoConnection` / `Timeout`, không có bản offline | "Không tải được sổ khi không có kết nối" (câu chữ ⚠️) + nút thử lại (AC28) | chưa có |
| `GET /v1/memorials` | 5xx / `AppError.Unknown` / `Serialization` | Như hai dòng trên (có bản offline thì hiện bản offline) — xem ⚠️ bên dưới | chưa có |
| `POST /v1/memorials` | 409 `MEMORIAL_LIMIT_REACHED` | Ở lại form, báo sổ đã đầy (câu chữ ⚠️) | chưa có |
| `POST` / `PUT` | 400 `VALIDATION_ERROR` (lẽ ra AC12–AC14 đã chặn) | Ở lại form, báo nội dung không hợp lệ (câu chữ ⚠️); `details.field` = `full_name` → báo dưới ô tên | chưa có |
| `POST` / `PUT` / `DELETE` | `AppError.NoConnection` / `Timeout` | Ở lại, báo cần kết nối (câu chữ ⚠️), sổ không đổi (AC29) | chưa có |
| `PUT` | 404 `MEMORIAL_NOT_FOUND` | Báo mục không còn tồn tại (câu chữ ⚠️), mục biến khỏi sổ, quay về sổ (AC16) | chưa có |
| `DELETE` | 404 `MEMORIAL_NOT_FOUND` | ⚠️ AC19 (product) | chưa có |
| `POST` / `PUT` / `DELETE` | `AppError.Unauthorized` | Trạng thái khách (AC2); dữ liệu đang nhập mất | chưa có |
| `POST` / `PUT` / `DELETE` | 5xx / `AppError.Unknown` / `Serialization` | Ở lại, báo không lưu được (câu chữ ⚠️) | chưa có |
| (local) | Tên rỗng | "Vui lòng nhập họ và tên" dưới ô tên (FR-MEM-012) | ✓ |

> ⚠️ Not confirmed (product): FR-MEM-010 chỉ nói "không tới được server"; khi server trả lỗi 5xx lúc
> tải sổ thì có hiện bản offline không (đề xuất: có, như mất mạng) — chặn dòng 5xx của bảng trên;
> hand back to ba-writer in innerlamp-be.

## Out of scope

- Thêm vào ứng dụng Lời nhắc / lịch của hệ thống (FR-MEM-009 chỉ iOS, §3.6) — kể cả
  `CalendarContract`/Google Calendar.
- Push từ server; nhắc trên website (§1.2, §3.6).
- Tạo mục từ Gia phả (`genealogy`, §1.2).
- Chia sẻ sổ, tìm kiếm, nhiều ảnh, xoá mềm/khôi phục, xuất dữ liệu (§1.2).
- Đăng nhập, lưu token, làm mới phiên — thuộc feature Login/`core/network` (chưa có spec Android).
- Migrate dữ liệu seed/in-memory hiện tại (§2.7 — bỏ).

## Pending (not yet implemented)

- Sổ theo tài khoản qua backend + bản offline (AC1–AC3, AC15–AC16, AC19, AC27–AC29) — `tasks.md`
  Phase 1 (repository/use case), Phase 3 (data), chặn bởi đăng nhập Android.
- Tháng nhuận, khoảng năm 1900 → nay, kiểm ngày, giới hạn tên/ghi chú (AC11–AC14) — Phase 1, Phase 2.
- Xác nhận trước khi xoá (AC19) — Phase 2.
- Bỏ công tắc "Lời nhắc" iOS (AC26) — Phase 2.
- Nhắc local (AC21–AC25) — Phase 1 (planner), Phase 4.
- Riêng tư log/backup (AC30–AC31) — Phase 3.
- Chạy vector (AC33) — Phase 5.
- Ảnh (AC32 phần ảnh, FR-MEM-011) — Deferred.

## Open questions (resolved)

Các giả định cũ ở `Docs/gio_assumptions.md` đã được product spec v1.0 quyết định (2026-10-14); spec này
trích ID, không chép giả định:

- ~~#1 Sổ giữ trong bộ nhớ, seed 3 người mẫu~~ → sổ thuộc tài khoản, dữ liệu cũ bỏ (BR-MEM-01, §2.7),
  đọc offline (FR-MEM-010). Resolved 2026-10-14.
- ~~#2 Nhắc chưa lên lịch~~ → nhắc local trên mọi máy đang đăng nhập (FR-MEM-008, BR-MEM-11); cách
  làm ở `design.md`. Resolved 2026-10-14.
- ~~#3 "Thêm vào Lời nhắc (Reminders)" trên Android~~ → chỉ iOS (FR-MEM-009, §3.6); Android bỏ công
  tắc (AC26). Resolved 2026-10-14.
- ~~#4 Ảnh là chữ viết tắt~~ → ảnh `later` (FR-MEM-011); chữ viết tắt khi không có ảnh (AC32).
  Resolved 2026-10-14.
- ~~#5 Hàng "Lấy từ gia phả" no-op~~ → luồng thuộc `genealogy` (§1.2); việc có hiện hàng này còn mở
  (⚠️ AC6). Resolved một phần 2026-10-14.
- ~~#6 Ngày 30 tháng thiếu, 29/2 năm thường~~ → BR-MEM-06, BR-MEM-07. Resolved 2026-10-14.
- ~~#7 Không có tháng nhuận~~ → ghi được tháng nhuận, giỗ luôn ở tháng thường (BR-MEM-04, BR-MEM-05).
  Resolved 2026-10-14.
- ~~#8 Lần giỗ; năm mất lùi 96 năm~~ → BR-MEM-09; năm 1900 → năm hiện tại (BR-MEM-03). Resolved
  2026-10-14.
- ~~#9 "Hôm nay" không tự làm mới~~ → "hôm nay" là ngày của máy (BR-MEM-08); làm mới khi quay lại
  foreground là quyết định Android (AC7). Resolved 2026-10-14.
- ~~#10, #11 ViewModel dùng chung qua back stack; `DropdownMenu` thay `<select>`~~ → quyết định
  Android, ghi ở `design.md`. Resolved 2026-10-14.
