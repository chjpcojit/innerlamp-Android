# Requirements — Calendar

Version: 1.0
Last updated: 2026-10-15
Status: in-progress — màn "Lịch lễ Phật giáo" (`ui/screens/festivals/`) và khối "Sự kiện Phật giáo" ở Trang chủ đã chạy, nhưng tính ngày từ danh mục cục bộ trong app (drift BR-CAL-01…07, BR-CAL-05); chưa gọi backend, chưa có màn chi tiết, chưa có bản offline, chưa tải năm sau, chưa lọc theo nhóm/lễ lớn từ dữ liệu
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/festivals/` (tên cũ `festivals`, product §1.5; `ui/screens/calendar/` là màn Lịch âm, không thuộc spec này)
Product spec: innerlamp-be `docs/product/calendar/product.md` @ v1.1
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ 0eb7d1f (info.version 1.0.0; `calendarEvents` vẫn là bản trước v3.0: `bearerAuth`, chưa có `is_major`/`sort_order`/`recurrence`) + thay đổi đã chốt nhưng chưa có trong contract: innerlamp-be `docs/specs/features/calendar/design.md` @ v3.0 (0eb7d1f; backend Phase 14–18)
Implements: BR-CAL-01, BR-CAL-02, BR-CAL-03, BR-CAL-04, BR-CAL-05, BR-CAL-06, BR-CAL-07 (Android: bỏ tính cục bộ, hiện ngày server trả), BR-CAL-08, BR-CAL-09, BR-CAL-10, BR-CAL-12, BR-CAL-13, BR-CAL-14, BR-CAL-15, FR-CAL-001, FR-CAL-002, FR-CAL-003, FR-CAL-004, FR-CAL-005, FR-CAL-006, FR-CAL-007, FR-CAL-008, NFR-CAL-03

> See `changelog.md` in this folder for the version history of this spec.

Spec platform (layer 2) của **Sự kiện Phật giáo** cho Android, viết retroactively từ code (đọc ngày
2026-10-15, `main` @ bebaa9c) và đối chiếu với product spec v1.1. Hành vi sản phẩm nằm ở product spec
và **không** nhắc lại ở đây; file này chỉ nói app Android hiện thực các `BR-CAL-*`/`FR-CAL-*` thế nào.

Cột Android `n/a` ở product §4.2 (không thuộc spec này): BR-CAL-11, BR-CAL-16, FR-CAL-009 (website, §3.6),
FR-CAL-010, NFR-CAL-01, NFR-CAL-02. BR-CAL-14 ghi `n/a` cho Android ở §4.2 nhưng "Applies to" của nó và
FR-CAL-005 (Android `planned`) đều cần nó — spec này hiện thực BR-CAL-14 cùng màn chi tiết và báo lệch
cho `ba-writer` (xem changelog, Hand-offs).

`Docs/home_assumptions.md` mục #11 bị thay thế bởi product spec (BR-CAL-04, BR-CAL-05) — xem "Open
questions (resolved)". Spec này không xoá file đó.

## Ràng buộc từ product spec

- **Server là nguồn duy nhất của sự kiện và ngày** (§2.5, BR-CAL-01…07, BR-CAL-05): app bỏ danh mục lễ
  cục bộ, bỏ việc tự tính ngày lễ và tự thêm Rằm / Mùng Một; mọi lần diễn ra, ngày dương, ngày âm, nhóm,
  cờ lễ lớn đến từ `GET /v1/calendar/events`. `LunarCalendar` vẫn ở lại vì Sổ giỗ (BR-MEM-10) và màn
  Lịch âm dùng.
- **Công khai** (FR-CAL-001): không cần phiên, không gắn Bearer, không gửi `X-API-Key` (backend design
  v3.0 Step 1a: app di động không gửi header này).
- **Một năm một lần tải, từ năm hiện tại tới 2100** (FR-CAL-002, FR-CAL-004).
- **Chi tiết dùng năm đã tải**, không có operation chi tiết riêng (Appendix A "Separate detail
  capability? No").
- **Bản trên máy** (FR-CAL-007, §3.5 "Device copy"): nội dung công khai, không có dữ liệu cá nhân, thay ở
  lần tải thành công kế tiếp.
- **Phụ thuộc backend** (backend design v3.0, chưa triển khai): route công khai (hôm nay còn đòi JWT —
  Android chưa có phiên thật nên mọi lần gọi sẽ nhận 401), `is_major` (BR-CAL-10), `sort_order`
  (BR-CAL-12), `recurrence` (tầng 3 của BR-CAL-15), seed có Rằm / Mùng 1 ở `sort_order` cuối và
  `guidance` một việc một dòng (product Appendix B mục 5). Chuyển Android sang API chỉ bật khi backend
  Phase 18 đã lên môi trường `dev` (tasks Phase 3).

## User Story

Là Phật tử dùng InnerLamp trên Android (khách hay đã đăng nhập), tôi muốn xem các lễ Phật giáo, ngày lễ
Việt Nam và lễ dân gian của từng năm cùng ngày dương, ngày âm và còn bao lâu, để biết năm nay Phật đản,
Vu Lan, Rằm… rơi vào ngày nào mà không phải tự tính lịch âm (product §1.3, §2.2).

## Acceptance Criteria (EARS notation)

### Truy cập và nguồn dữ liệu

1. WHEN khách hoặc người dùng đã đăng nhập mở lịch lễ, chi tiết hay Trang chủ, THE SYSTEM SHALL tải dữ
   liệu bằng `GET /v1/calendar/events?year=<năm>` **không** gắn `Authorization` và **không** gửi
   `X-API-Key`, và không bao giờ hiện lời mời đăng nhập ở feature này. (FR-CAL-001)
2. WHEN app cần sự kiện của một năm, THE SYSTEM SHALL luôn gửi `year` tường minh (không để trống), với
   năm hiện tại tính theo `Asia/Ho_Chi_Minh`. (FR-CAL-002, platform)
   > ⚠️ Not confirmed: luôn gửi `year` tường minh thay vì để server tự chọn — mặc định đã chọn: **có**,
   > để câu trả lời được `Cache-Control: public` (backend E17) và mọi màn dùng cùng một năm; merge PR =
   > duyệt.
3. WHEN app hiện một lần diễn ra, THE SYSTEM SHALL lấy ngày dương, ngày âm (kể cả cờ tháng nhuận), tên,
   nhóm và cờ lễ lớn đúng như server trả, và không tự tính, tự thêm hay tự bỏ lần diễn ra nào — kể cả
   Rằm và Mùng Một. (BR-CAL-01, BR-CAL-02, BR-CAL-03, BR-CAL-04, BR-CAL-05, BR-CAL-06, BR-CAL-07)
4. WHEN hiển thị ngày âm của một lần diễn ra, THE SYSTEM SHALL hiện ngày/tháng âm và dấu "(nhuận)" khi
   `lunar_date.is_leap_month` là `true`, cùng ngày dương, theo BR-CAL-08. (BR-CAL-08)
5. WHEN gộp nhiều năm hoặc lọc, THE SYSTEM SHALL giữ thứ tự BR-CAL-12 (ngày dương, `sort_order`, `code`)
   bằng một bộ so sánh dùng chung trong `domain/calendar/`. (BR-CAL-12)

### Lịch lễ Phật giáo (danh sách)

6. WHEN người dùng mở lịch lễ (từ "Tất cả" của khối Home, "Lịch lễ" ở tab Chùa, hoặc "Lịch lễ" ở chi
   tiết Phật/Bồ Tát), THE SYSTEM SHALL mở `FestivalsRoute`, tải năm hiện tại và hiện các lần diễn ra theo
   tháng dương 1 → 12, cuộn tới tháng hiện tại. (FR-CAL-002)
7. WHEN hiển thị một dòng của lịch lễ, THE SYSTEM SHALL hiện ngày dương và thứ, tên sự kiện, ngày âm
   (AC4), nhãn đếm ngược theo BR-CAL-13 (AC21), dấu lễ lớn khi `is_major` (BR-CAL-10) và nhãn tên của
   từng nhóm của sự kiện, theo tên và thứ tự trong `data.groups` (BR-CAL-09). (FR-CAL-002, BR-CAL-09,
   BR-CAL-10, BR-CAL-13)
8. WHEN người dùng chọn một dòng, THE SYSTEM SHALL mở chi tiết của đúng lần diễn ra đó (AC14).
   (FR-CAL-005)
9. WHEN lịch lễ hiển thị, THE SYSTEM SHALL **không** hiện "chùa tổ chức" (số chùa, chip chùa) và câu giới
   thiệu nhắc tới chùa — chưa có nguồn dữ liệu (product §1.2). (FR-CAL-002)

### Lọc

10. WHEN lịch lễ hiển thị, THE SYSTEM SHALL có các chip lọc "Tất cả", "Lễ lớn", rồi một chip cho mỗi nhóm
    trong `data.groups` theo thứ tự đó, mỗi chip kèm số lần diễn ra khớp trong các năm đã tải; **không**
    còn chip "Rằm & Mùng Một". (FR-CAL-003, BR-CAL-09, BR-CAL-10)
11. WHEN người dùng chọn một chip, THE SYSTEM SHALL chỉ hiện các lần diễn ra khớp (lễ lớn = `is_major`;
    nhóm = sự kiện có mã nhóm đó), ẩn tháng không còn dòng nào, và giữ chip khi tải thêm năm (AC12).
    (FR-CAL-003)

### Tải năm sau

12. WHEN người dùng cuộn tới cuối năm cuối cùng đã tải và năm đó nhỏ hơn 2100, THE SYSTEM SHALL tải năm
    kế tiếp và nối vào cuối danh sách, rồi:
    - on success: nối 12 tháng của năm mới, áp chip đang chọn;
    - on failure có bản trên máy của năm đó: nối bản trên máy kèm câu "có thể chưa cập nhật" (AC24);
    - on failure không có bản trên máy: giữ nguyên danh sách, hiện hàng "Thử lại" ở cuối; bấm thì tải
      lại năm đó. (FR-CAL-004, FR-CAL-007)
13. WHEN năm cuối đã tải là 2100, THE SYSTEM SHALL không tải thêm và không hiện hàng tải thêm; THE SYSTEM
    SHALL không bao giờ đề nghị năm trước năm hiện tại. (FR-CAL-004)

### Chi tiết ngày lễ

14. WHEN người dùng mở một lần diễn ra (từ lịch lễ hoặc khối Home), THE SYSTEM SHALL mở
    `FestivalDetailRoute(eventCode, solarDate)` và lấy sự kiện từ năm của `solarDate` đã tải (bộ nhớ, rồi
    bản trên máy, rồi tải năm đó nếu chưa có), rồi hiện ảnh, nhãn nhóm và dấu lễ lớn, tên, ngày âm, thẻ
    ngày dương kèm thứ và đếm ngược, mô tả và "Phật tử thường làm gì". (FR-CAL-005, BR-CAL-08,
    BR-CAL-10, BR-CAL-13)
15. WHEN hiển thị mô tả dài và lễ nghi, THE SYSTEM SHALL tách đoạn / mục theo BR-CAL-14 (chỉ theo xuống
    dòng), hiện lễ nghi thành danh sách đánh số, và ẩn mục rỗng. (BR-CAL-14)
16. WHEN `image_url` là `null` hoặc ảnh tải lỗi, THE SYSTEM SHALL hiện ảnh mặc định dùng chung.
    (BR-CAL-14)
    > ⚠️ Not confirmed: ảnh mặc định dùng chung là ảnh nào trên Android — mặc định đã chọn: dùng lại
    > placeholder gradient hiện có của Trang chủ (`PlaceholderImage`, `home_assumptions.md` #1) cho tới
    > khi có ảnh mặc định chung (product §2.7); merge PR = duyệt.
17. WHEN chi tiết hiển thị, THE SYSTEM SHALL không có mục "Kinh nên tụng", "Chùa tổ chức" hay nút "Nhắc tôi
    ngày này" (product §1.2), và không có nút sự kiện trước/sau (FR-CAL-009 chỉ website, §3.6); back quay
    về màn trước. (FR-CAL-005)
18. WHEN năm của `solarDate` đã có (tải được hoặc bản trên máy) nhưng không có lần diễn ra `eventCode`
    vào ngày đó, THE SYSTEM SHALL báo không tìm thấy sự kiện và cho quay lại. (FR-CAL-005)

### Khối "Sự kiện Phật giáo" ở Trang chủ

19. WHEN Trang chủ hiển thị, THE SYSTEM SHALL dựng khối theo BR-CAL-15 từ tháng hiện tại (ngày của máy)
    trong sáu tháng — tải năm hiện tại và, khi cửa sổ sáu tháng sang năm sau, cả năm sau — dùng chung
    repository với lịch lễ; mỗi tháng chọn dòng và "+N sự kiện khác" bằng hàm thuần
    `domain/calendar/HomeEventBlock` (tầng 3 nhận ra bằng `recurrence = lunar_monthly`, nhóm Phật giáo
    bằng mã `buddhist`); tháng không có dòng nào vẫn có thẻ với câu trống; chọn một dòng mở chi tiết
    (AC14); chọn "Tất cả" hoặc "+N sự kiện khác" mở lịch lễ ở tháng hiện tại. (FR-CAL-006, BR-CAL-10,
    BR-CAL-12, BR-CAL-15)
    > ⚠️ Not confirmed (contract): `openapi.yaml` chưa nói mã nhóm `buddhist` là ổn định (chỉ có trong ví
    > dụ và `rules/home-block.json`) — mặc định đã chọn: hằng `BUDDHIST_GROUP = "buddhist"` trong
    > `domain/calendar/`; hand-off cho backend ghi rõ trong contract. Merge PR = duyệt mặc định.

### Đếm ngược và ngày đổi

20. WHEN app quay lại foreground (ON_RESUME) sau khi ngày của máy đã đổi, THE SYSTEM SHALL tính lại đếm
    ngược ở lịch lễ và chi tiết, và dựng lại khối Home cho tháng mới. (platform)
21. WHEN hiển thị đếm ngược ở bất kỳ màn nào, THE SYSTEM SHALL dùng một hàm thuần chung
    `domain/calendar/CalendarCountdown` với "hôm nay" là ngày của máy, cho đúng nhãn của BR-CAL-13.
    (BR-CAL-13)

### Offline và lỗi tải

22. WHEN một năm được tải thành công, THE SYSTEM SHALL ghi nó vào bản trên máy (Room), thay bản cũ của năm
    đó, và xoá bản của các năm trước năm hiện tại. (FR-CAL-007, platform)
23. WHEN mở lịch lễ hoặc Trang chủ mà năm cần đã có bản trên máy, THE SYSTEM SHALL hiện bản đó ngay (chưa
    có câu "có thể chưa cập nhật") trong lúc gọi mạng, rồi thay bằng kết quả mới khi thành công.
    (NFR-CAL-03, FR-CAL-007)
    > ⚠️ Not confirmed: hiện bản trên máy trước rồi làm mới (thay vì chờ mạng) — mặc định đã chọn: **có**,
    > để đạt 0,5 s của NFR-CAL-03; câu "có thể chưa cập nhật" chỉ hiện khi lần gọi mạng thất bại (AC24);
    > merge PR = duyệt.
24. WHEN tải một năm thất bại vì bất kỳ lý do nào (không mạng, hết giờ, 5xx, 429, 401 của backend cũ, lỗi
    parse) và máy có bản của năm đó, THE SYSTEM SHALL hiện bản đó kèm câu "có thể chưa cập nhật" (Error →
    UI) — ở đầu lịch lễ, ở khối Home, hoặc ở đầu năm được nối từ bản trên máy (AC12). (FR-CAL-007)
25. WHEN tải năm hiện tại thất bại và máy chưa có bản của năm đó, THE SYSTEM SHALL hiện thông báo lỗi tải
    kèm nút "Thử lại" — câu riêng khi `AppError.NoConnection`/`Timeout`, câu chung cho lỗi khác; ở khối
    Home thông báo này thay chỗ các thẻ tháng, khối không biến mất. (FR-CAL-008)
26. WHEN người dùng bấm "Thử lại" và lần tải thành công, THE SYSTEM SHALL hiện năm hiện tại như AC6 (lịch
    lễ) hoặc dựng lại khối (Home). (FR-CAL-008)
27. WHEN cửa sổ sáu tháng của khối Home cần hai năm và chỉ một năm có dữ liệu (mạng hoặc bản trên máy),
    THE SYSTEM SHALL hiện thẻ của các tháng có dữ liệu và, ở thẻ của tháng thuộc năm không có dữ liệu,
    thông báo lỗi tải kèm "Thử lại". (FR-CAL-006, FR-CAL-008)
    > ⚠️ Not confirmed (product): FR-CAL-008 không nói khối Home làm gì khi chỉ tải được một trong hai năm
    > — mặc định Android: báo lỗi trên từng thẻ tháng thiếu dữ liệu như trên; hand back to ba-writer in
    > innerlamp-be.
28. WHEN app nhận 401 từ `GET /v1/calendar/events` (backend chưa lên v3.0), THE SYSTEM SHALL coi là lỗi tải
    như AC24/AC25, không xoá phiên, không mời đăng nhập. (FR-CAL-001, platform)

### Hiệu năng và kiểm thử

29. WHEN mở lịch lễ của năm hiện tại trên mạng 4G, THE SYSTEM SHALL hiện danh sách trong vòng 2 giây; khi
    có bản trên máy, trong vòng 0,5 giây (AC23). (NFR-CAL-03)
30. WHEN chạy unit test, THE SYSTEM SHALL qua mọi case của innerlamp-be
    `docs/product/calendar/rules/countdown.json`, `rules/ordering.json` và `rules/home-block.json`;
    `rules/event-placement.json` chỉ dành cho backend (product §4.1). (BR-CAL-12, BR-CAL-13, BR-CAL-15)

### Hiện trạng code so với từng AC (đọc ngày 2026-10-15)

| AC | Hiện trạng | Ghi chú |
|---|---|---|
| 1 | lệch | Không gọi API; khách xem được (FR-CAL-001 `done` chỉ vì dữ liệu là cục bộ) |
| 2, 3 | chưa có | `GetBuddhistEventMonthsUseCase` có 19 lễ vía cố định + tự thêm Rằm/Mùng Một mỗi ngày âm 15/1 — **drift BR-CAL-01…07, BR-CAL-05**; chỉ có sự kiện Phật giáo (**drift BR-CAL-09**) |
| 4 | lệch | `lunarLabel` = "d/m âm lịch", **không có "(nhuận)"** (Rằm/Mùng Một tháng nhuận vẫn hiện) |
| 5 | chưa có | Thứ tự theo ngày, mỗi ngày tối đa một sự kiện (lễ vía thắng Rằm/Mùng Một cùng ngày) |
| 6 | lệch | `FestivalsRoute` có, vào từ Home/Chùa/Phật ✓, cuộn tới tháng hiện tại ✓; nhưng tải sẵn **3 năm** (tháng 1 năm nay → hết năm thứ ba), không theo năm, tháng không có sự kiện bị bỏ |
| 7 | lệch | Có ngày + thứ, tên, ngày âm, đếm ngược; **không có nhãn nhóm**; ngày tô màu theo `isFestival` (mọi lễ có tên), không theo cờ lễ lớn |
| 8 | chưa có | Dòng không bấm được (chỉ chip chùa bấm được) |
| 9 | lệch | Mỗi dòng hiện "N chùa tổ chức" + chip chùa từ dữ liệu mẫu (`GetFestivalCalendarUseCase.hostsFor`) và câu giới thiệu về chùa |
| 10, 11 | lệch | Chip "Tất cả" / "Lễ lớn" / "Rằm & Mùng Một"; "Lễ lớn" thực ra lọc `isFestival` (mọi lễ có tên), `MAJOR_KEYS` (8 key) chỉ dùng để chọn chùa mẫu; không có chip nhóm, không có số đếm |
| 12, 13 | chưa có | Không tải thêm năm; không có giới hạn 2100 |
| 14–18 | chưa có | Không có màn chi tiết |
| 19 | lệch | `EventsSection` có carousel thẻ tháng, tối đa 3 dòng, "+N sự kiện khác", "Tất cả" ✓; nhưng **12 tháng của năm dương** (không phải 6 tháng từ tháng hiện tại), **không chia tầng**, dòng bấm vào mở lịch lễ (không mở chi tiết) |
| 22–28 | chưa có | Không có tải mạng, bản trên máy, lỗi tải hay thử lại |
| 20 | chưa có | "Hôm nay" chốt lúc dựng ViewModel (`home_assumptions.md` #7) |
| 21 | ✓ phần lớn | `FestivalsUiState.countdown` đúng logic BR-CAL-13; chữ "còn N ngày" viết thường (product "Còn n ngày"); chưa đối chiếu vector |
| 29 | chưa đo | |
| 30 | chưa có | `FestivalsViewModelTest`, `GetBuddhistEventMonthsUseCaseTest` test danh mục cục bộ, chưa chạy vector |

## Dữ liệu & API

Nguồn đúng là `openapi.yaml` (operation `calendarEvents`) và backend `design.md` v3.0 cho phần chưa vào
contract; không chép schema ở đây.

| Thao tác | Operation (openapi) | Auth | Lưu cục bộ | Ghi chú |
|---|---|---|---|---|
| Tải một năm | `GET /v1/calendar/events?year=YYYY` (`calendarEvents`) | public — không Bearer, không `X-API-Key` | Room: bản của năm đó (thay toàn bộ) | Hôm nay contract còn `bearerAuth` → 401; v3.0 bỏ (backend E4). `year` luôn tường minh (AC2) |
| Cờ lễ lớn, thứ tự hiển thị, loại quy tắc | `events[].is_major`, `sort_order`, `recurrence` | — | trong bản của năm | ⚠️ Not confirmed (contract): chưa có trong `openapi.yaml` @ 0eb7d1f, đã chốt ở backend design v3.0 (E13) — chặn AC5, AC7 (dấu lễ lớn), AC10–AC11 ("Lễ lớn"), AC19, AC30 (dữ liệu thật) |
| Chi tiết | — (dùng năm đã tải) | — | đọc bản của năm | Không có operation riêng (product Appendix A) |
| Ảnh sự kiện | `events[].image_url` (có thể `null`) | — | Coil cache | Seed hiện để `null` → ảnh mặc định (AC16) |

## Error → UI mapping

Product spec v1.1 không có bảng câu chữ cho feature này; câu tiếng Việt dưới là mặc định Android.

> ⚠️ Not confirmed (product): câu chữ cho bản trên máy, lỗi tải (mất mạng / lỗi khác), "Thử lại", không
> tìm thấy sự kiện và tháng trống ở khối Home — product chỉ mô tả ý nghĩa (FR-CAL-005, FR-CAL-007,
> FR-CAL-008, BR-CAL-15). Mặc định Android là các câu trong cột "User sees"; hand back to ba-writer in
> innerlamp-be để chốt câu chung cho bốn nền tảng.

| Operation | Failure case | User sees | Hiện trạng code |
|---|---|---|---|
| `GET /v1/calendar/events` (năm hiện tại) | Mọi lỗi, có bản trên máy | Bản trên máy + "Dữ liệu đã lưu trên máy, có thể chưa cập nhật." (AC24) | chưa có |
| `GET /v1/calendar/events` (năm hiện tại) | `AppError.NoConnection` / `Timeout`, không có bản | "Không có kết nối mạng. Kiểm tra mạng rồi thử lại." + "Thử lại" (AC25) | chưa có |
| `GET /v1/calendar/events` (năm hiện tại) | 5xx / `HttpError` (kể cả 429) / `Unauthorized` (backend cũ) / `ApiError` / `Serialization` / `Unknown`, không có bản | "Chưa tải được lịch lễ. Vui lòng thử lại." + "Thử lại" (AC25, AC28) | chưa có |
| `GET /v1/calendar/events` (năm kế tiếp) | Mọi lỗi, không có bản | Danh sách giữ nguyên, hàng cuối "Chưa tải được năm YYYY · Thử lại" (AC12) | chưa có |
| `GET /v1/calendar/events` (khối Home) | Như hai dòng năm hiện tại | Câu tương ứng thay chỗ các thẻ (hoặc trên thẻ tháng thiếu dữ liệu, AC27) + "Thử lại" | chưa có (lỗi hiện không thể xảy ra vì dữ liệu cục bộ) |
| (local) chi tiết | Không có lần diễn ra `eventCode` ngày `solarDate` trong năm đó | "Không tìm thấy sự kiện này." + "Quay lại" (AC18) | chưa có |
| (local) chi tiết | Năm của `solarDate` không tải được và không có bản | Như dòng lỗi tải năm hiện tại, "Thử lại" tải lại năm đó | chưa có |
| (local) khối Home | Tháng không có sự kiện nhóm Phật giáo | Thẻ tháng với "Tháng này chưa có sự kiện Phật giáo." (AC19) | chưa có |
| (local) ảnh | `image_url` `null` hoặc tải lỗi | Ảnh mặc định (AC16) | chưa có |

`400 INVALID_YEAR` không thể xảy ra (app chỉ gửi năm hiện tại → 2100); nếu có, xử lý như lỗi khác.

## Out of scope

- Màn Lịch âm (`ui/screens/calendar/` — `CalScreen`, `LunarDetailScreen`), chấm Rằm · Mùng Một ở dải tuần
  của Trang chủ, ảnh Phật theo ngày — cùng slug nhưng là product v1.x (§1.2). Các chỗ này vẫn dùng
  `LunarCalendar` cục bộ. Báo `ba-writer` xem BR-CAL-05 ("clients stop computing them") có áp cho chúng
  khi viết v1.x không.
- Sự kiện của chùa (`EventsRoute`, "Khóa tu & lễ sắp tới") — feature sangha.
- Nhắc một ngày lễ ("Nhắc tôi ngày này", nút "Nhắc tôi" ở tab Chùa) — product §1.2, sẽ dùng ADR-0001.
- Kinh nên tụng, chùa tổ chức trên chi tiết hay danh sách (§1.2).
- Sự kiện trước/sau trên chi tiết (FR-CAL-009, website, §3.6).
- Ngày nghỉ lễ / nghỉ bù (BR-CAL-11 cột Android `n/a`), sự kiện bị tắt (BR-CAL-16 — server không trả).
- Năm trước năm hiện tại (FR-CAL-004).
- Nội dung ngoài tiếng Việt (§1.2).

## Pending (not yet implemented)

- Bỏ danh mục và cách tính cục bộ; tải qua API với bản trên máy (AC1–AC5, AC22–AC28) — `tasks.md`
  Phase 1, Phase 3; chặn bởi backend v3.0 (route công khai, `is_major`, `sort_order`, `recurrence`).
- Lịch lễ theo năm + tải năm sau tới 2100, nhãn nhóm, dấu lễ lớn, bỏ chùa tổ chức (AC6–AC13) — Phase 2.
- Màn chi tiết (AC14–AC18) — Phase 2.
- Khối Home theo BR-CAL-15, dòng mở chi tiết (AC19, AC27) — Phase 1, Phase 2.
- Tính lại khi đổi ngày (AC20) — Phase 2.
- Khối "Ngày lễ & ngày chay sắp tới" ở tab Chùa đổi nguồn (⚠️ product, xem design.md) — Phase 2.
- Vector và đo hiệu năng (AC29, AC30) — Phase 4.

## Open questions (resolved)

Theo product spec v1.1 (2026-10-15):

- ~~`home_assumptions.md` #11: ngày 30 âm của tháng thiếu tính là 29; tháng nhuận không lặp lại lễ vía;
  Rằm/Mùng Một tháng nhuận vẫn là ngày trai~~ → server tính theo BR-CAL-04 (ngày 30 không có ở tháng
  thiếu trừ sự kiện "ngày cuối tháng"; lễ không lặp ở tháng nhuận) và BR-CAL-05 (Rằm / Mùng Một có cả
  tháng nhuận, chỉ đến từ danh mục). Android không còn quy tắc nào của mục này (AC3). Resolved 2026-10-15.
- ~~Danh sách lễ lớn cố định `MAJOR_KEYS` (8 key)~~ → cờ `is_major` từ dữ liệu, 10 sự kiện ban đầu
  (BR-CAL-10) → AC7, AC10, AC11. Resolved 2026-10-15.
- ~~Chỉ hiện sự kiện Phật giáo~~ → lịch lễ hiện mọi nhóm có chip lọc; khối Home chỉ nhóm Phật giáo
  (BR-CAL-09, BR-CAL-15) → AC10, AC19. Resolved 2026-10-15.
- ~~Lịch lễ cần đăng nhập?~~ → công khai (FR-CAL-001) → AC1. Resolved 2026-10-15.
- ~~Năm nào xem được~~ → năm hiện tại trở đi, từng năm, tới 2100 (FR-CAL-004) → AC12, AC13. Resolved
  2026-10-15.
- ~~Chi tiết cần operation riêng?~~ → không, dùng năm đã tải (product Appendix A) → AC14. Resolved
  2026-10-15.
- ~~Chùa tổ chức, kinh nên tụng, nhắc~~ → ngoài v1.0 (§1.2) → AC9, AC17. Resolved 2026-10-15.

Mặc định Android chọn trong spec này (merge PR = duyệt; còn ghi `⚠️` inline để thấy chỗ áp):
`year` luôn tường minh (AC2); ảnh mặc định = placeholder hiện có (AC16); mã nhóm `buddhist` (AC19);
hiện bản trên máy trước rồi làm mới (AC23); thêm các mặc định kỹ thuật ở `design.md` (Room DB riêng,
thư mục giữ `festivals`, khối tab Chùa).
