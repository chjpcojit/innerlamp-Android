# Changelog — Calendar Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.0 (initial) (2026-10-15)

- Spec đầu tiên, viết retroactively từ code đang chạy (`main` @ bebaa9c, đọc ngày 2026-10-15) và đối chiếu
  với product spec innerlamp-be `docs/product/calendar/product.md` v1.1. Đã có: màn "Lịch lễ Phật giáo"
  (`ui/screens/festivals/`, `FestivalsRoute`, vào từ Home / tab Chùa / chi tiết Phật) và khối "Sự kiện
  Phật giáo" ở Trang chủ, cả hai trên danh mục cục bộ (`GetBuddhistEventMonthsUseCase`,
  `GetFestivalCalendarUseCase` với `MAJOR_KEYS` và chùa tổ chức mẫu). Chưa có: API, bản trên máy, chi
  tiết, tải năm sau, chip nhóm, cờ lễ lớn từ dữ liệu, chia tầng khối Home, vector.
- `requirements.md`: header product v1.1 + contract `openapi.yaml` @ 0eb7d1f (bản `calendarEvents` trước
  v3.0) + backend `design.md` v3.0 cho field chưa vào contract; 30 AC (truy cập, lịch lễ, lọc, năm sau,
  chi tiết, khối Home, đếm ngược, offline/lỗi, hiệu năng, vector); bảng hiện trạng; Dữ liệu & API; Error →
  UI với câu mặc định; Out of scope; Pending; Open questions (resolved).
- `design.md`: domain mới (`CalendarEvent`, `CalendarCountdown`, `CalendarOrdering`, `HomeEventBlock`,
  `EventContentFormat`, repository + ba use case), màn `FestivalDetailScreen` + `FestivalDetailRoute`,
  data (`CalendarApiService` công khai, DTO, mapper, Room `innerlamp_calendar.db`, repository impl,
  `di/CalendarModule.kt`), Step-by-step Flow "Tải một năm", xoá danh mục cục bộ sau Phase 3.
- `tasks.md`: Phase 1 Domain, 2 UI, 3 Data (chờ backend v3.0), 4 Tests (vector `countdown`, `ordering`,
  `home-block` qua bản chép có `SOURCE`), 5 Documentation; `[x]` cho lối vào, truy cập không đăng nhập,
  cuộn tới tháng hiện tại, bố cục carousel Home, contract hiện có.
- Product spec / contract: product v1.1 (IDs Android: BR-CAL-01…10, 12, 13, 14, 15; FR-CAL-001…008;
  NFR-CAL-03). Contract @ 0eb7d1f; field `is_major`, `sort_order`, `recurrence` và route công khai theo
  backend design v3.0 (chưa triển khai).
- Mặc định Android đã chọn (merge PR = duyệt; ghi `⚠️` inline): luôn gửi `year` tường minh (AC2); ảnh mặc
  định = placeholder hiện có (AC16); mã nhóm `buddhist` (AC19); hiện bản trên máy trước rồi làm mới (AC23);
  Room DB riêng một dòng JSON mỗi năm, giữ năm hiện tại trở đi, không HTTP cache; giữ thư mục
  `ui/screens/festivals/`, chi tiết là `FestivalDetail*`.
- Hand-offs:
  - `ba-writer`: (1) §4.2 BR-CAL-14 cột Android ghi `n/a` nhưng FR-CAL-005 (Android `planned`) cần nó →
    nên là `planned`; (2) câu chữ cho bản trên máy, lỗi tải (mất mạng / khác), thử lại, không tìm thấy,
    tháng trống ở Home; (3) khối Home khi chỉ tải được một trong hai năm (AC27); (4) khối "Ngày lễ & ngày
    chay sắp tới" ở tab Chùa (prototype `chuaVals`) không có trong product spec nhưng dùng danh mục cục
    bộ; (5) FR-CAL-006 dẫn BR-CAL-13 nhưng §3.1.1 không nói khối Home có đếm ngược; (6) BR-CAL-05 "clients
    stop computing" có áp cho chấm Rằm · Mùng Một ở dải tuần Home / màn Lịch âm (v1.x) không; (7) ghi chú
    §4.2 cho Android: "Lễ lớn" của Android hiện lọc mọi lễ có tên (`isFestival`), `MAJOR_KEYS` chỉ dùng
    chọn chùa mẫu; Android đã có màn danh sách (chỉ thiếu chi tiết); BR-CAL-08 Android thiếu "(nhuận)" →
    `drift`, không phải `done`; lịch lễ Android còn hiện chùa tổ chức mẫu.
  - Backend (contract): cập nhật `openapi.yaml` cho v3.0 (bỏ `bearerAuth`, `is_major`, `sort_order`,
    `recurrence`, 429) — Phase 18; ghi rõ mã nhóm `buddhist` là ổn định (AC19).
- Reason: product spec calendar v1.1 (2026-10-15) yêu cầu Android bỏ danh mục cục bộ và đọc từ backend;
  bước 4 của quy trình tài liệu (`spec-writer`).
