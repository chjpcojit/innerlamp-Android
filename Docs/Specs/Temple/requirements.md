# Requirements — Temple

Version: 1.1
Last updated: 2026-10-15
Status: planned (drift) — khối "Chùa Việt Nam" ở Trang chủ, màn "Tất cả chùa" (`templelist`) và "Trang chùa" (`temple`) đã có UI nhưng chạy trên dữ liệu mẫu (8 chùa, có miền "Tây Nguyên"), tìm/lọc trên máy, không phân trang; Trang chùa là bố cục cũ (Theo dõi, Cúng dường, thống kê, thời khóa) và không gọi backend. Chỉ phần danh sách rút gọn (`TempleOptionRepository`) đã nối backend.
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/temple/` (trang chùa) và `ui/screens/templelist/` (danh bạ); khối nổi bật nằm ở `ui/screens/home/` (tên thư mục code giữ nguyên, product §1.5)
Product spec: innerlamp-be `docs/product/temple/product.md` @ v1.1
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ 0eb7d1f (info.version 1.0.0) — **chưa** có `q`/`region` và trang 12 ở `GET /v1/temples`, chưa có bốn nhóm trường mới ở `GET /v1/temples/{id}` (product Appendix B; backend spec `temple` v2.0 Phase 11–13 chưa làm)
Implements: BR-TPL-01, BR-TPL-02, BR-TPL-03, BR-TPL-04, BR-TPL-05, BR-TPL-06, BR-TPL-07, BR-TPL-08, BR-TPL-09, FR-TPL-001, FR-TPL-002, FR-TPL-003, FR-TPL-004, FR-TPL-005 (qua `../Onboarding/`), FR-TPL-006, FR-TPL-007, FR-TPL-008, FR-TPL-009, NFR-TPL-01, NFR-TPL-02 — FR-TPL-010 là Backend (Android `n/a` trong product §4.2)

> See `changelog.md` in this folder for the version history of this spec.

Spec platform (layer 2) của **danh bạ chùa** cho Android. Viết theo kiểu hỗn hợp: phần hiện trạng đọc
từ code ngày 2026-10-15 (branch `claude/project-thread-lr6x54` dựa trên `main` @ bebaa9c), phần còn lại
là prospective. Hành vi sản phẩm (thứ tự, cỡ trang, ba miền, cách khớp tìm kiếm, trường thiếu, khoảng
cách, giờ mở cửa) nằm ở product spec và **không** nhắc lại ở đây; file này chỉ nói app Android hiện thực
các `BR-TPL-*`/`FR-TPL-*` thế nào.

## Ràng buộc từ product spec

- **Công khai, chỉ đọc** (§2.3, §2.7): không cần đăng nhập; không có Bearer, không `X-API-Key` (giống
  `TempleApiService` hiện có).
- **Server quyết định thứ tự, tìm kiếm và lọc** (BR-TPL-01, BR-TPL-05, §4.1): app hiện đúng thứ tự nhận
  được, không sắp lại, không lọc trên máy. Vector `temple-order.json` và `temple-search-match.json` chỉ
  chạy ở backend.
- **App tự tính** khoảng cách (BR-TPL-07) và trạng thái mở cửa (BR-TPL-09) — chạy vector
  `temple-distance.json`, `temple-open-status.json`.
- **Không tải cả danh bạ một lần** (NFR-TPL-02): mọi danh sách đi theo trang của server.
- **Dữ liệu mẫu bị bỏ** (§2.1 "Android shows sample data"): mọi chùa đến từ backend; id dạng
  `temple_<region>_<n>` của contract thay cho id mẫu `tp-…`, `tu-quang`.
- **Phụ thuộc:** contract cho tìm/lọc/trang 12 và trường mới (Appendix B mục 1, 2, 4) — xem
  "Dữ liệu & API"; bộ chọn chùa khi đăng ký thuộc `../Onboarding/` (FR-TPL-005, BR-ONB-10).

## User Story

Là người dùng (đã hoặc chưa đăng nhập), tôi muốn xem các chùa nổi bật ở Trang chủ, tìm và lọc toàn bộ
danh bạ chùa Việt Nam theo tên và miền, mở trang một chùa để đọc hồ sơ, biết chùa đang mở cửa không và
được chỉ đường tới đó (product §1.2, §2.2).

## Acceptance Criteria (EARS notation)

### Chùa nổi bật ở Trang chủ

1. WHEN Trang chủ mở, THE SYSTEM SHALL tải `GET /v1/temples/top` (public) và hiện các thẻ chùa trong
   khối "Chùa Việt Nam" đúng thứ tự nhận được (không sắp lại, không cắt bớt), kèm "Xem tất cả" mở
   `TempleListRoute`. (FR-TPL-001, BR-TPL-01, BR-TPL-02)
2. WHEN `top` trả danh sách rỗng, THE SYSTEM SHALL hiện trạng thái trống của khối, không hiện lỗi.
   (FR-TPL-001, BR-TPL-02)
3. WHEN `top` thất bại, THE SYSTEM SHALL hiện thẻ rỗng của khối kèm "Thử lại" (gọi lại `top`), không
   chặn các khối khác của Trang chủ. (platform)
   > ⚠️ Not confirmed (product): FR-TPL-001 không có kịch bản lỗi cho khối nổi bật — câu chữ khi lỗi (và
   > có phân biệt mất mạng như FR-TPL-002 không) — chặn task Phase 2 "Khối nổi bật"; hand back to
   > ba-writer in innerlamp-be.
4. WHEN hiện một thẻ chùa (Trang chủ hoặc danh bạ), THE SYSTEM SHALL hiện ảnh (`thumbnail_url`; không có
   → ảnh giữ chỗ), tên, loại, tỉnh, cấp di tích và hệ phái theo §3.1.1 "Temple card". (FR-TPL-001,
   FR-TPL-002, BR-TPL-06)
   > ⚠️ Not confirmed (product): nhãn tiếng Việt hiển thị cho mã `type`, `sect`, `heritage_rank` của
   > contract (vd. `DI_SAN_THE_GIOI`, `BẮC_TÔNG`) và cách hiện một mã lạ (contract coi `type`/`sect` là
   > enum mở) — chặn task Phase 2 "Nhãn hiển thị"; hand back to ba-writer in innerlamp-be.

### Danh bạ "Tất cả chùa"

5. WHEN người dùng mở `TempleListRoute`, THE SYSTEM SHALL gọi `GET /v1/temples` trang 1 (không `q`,
   không `region`), hiện các chùa đúng thứ tự nhận được và số chùa tìm thấy lấy từ `total` của
   response. (FR-TPL-002, BR-TPL-01, BR-TPL-03)
6. WHEN người dùng cuộn tới gần cuối danh sách và `page < total_pages`, THE SYSTEM SHALL tải trang kế
   tiếp và nối vào cuối danh sách, mỗi lúc chỉ một request; THE SYSTEM SHALL dừng khi
   `page >= total_pages` hoặc trang trả về rỗng, và đọc cỡ trang từ `page_size` của response, không
   giả định số cố định. (FR-TPL-002, BR-TPL-03, NFR-TPL-02, §3.6)
7. WHEN tải trang đầu thất bại, THE SYSTEM SHALL hiện trạng thái lỗi có "Thử lại", với câu khác nhau
   khi máy mất mạng (`AppError.NoConnection` / `AppError.Timeout`) và khi lỗi khác. (FR-TPL-002)
   > ⚠️ Not confirmed (product): câu chữ tiếng Việt cho lỗi tải (mất mạng / lỗi khác) ở danh bạ và trang
   > chùa, câu "không tìm thấy chùa" của FR-TPL-006 và câu gợi ý của "Không tìm thấy chùa" (FR-TPL-003)
   > — product chỉ mô tả ý, không cho câu — chặn task Phase 2 "Trạng thái lỗi/rỗng"; hand back to
   > ba-writer in innerlamp-be.
8. WHEN tải một trang kế tiếp thất bại, THE SYSTEM SHALL giữ các chùa đã tải và hiện "Thử lại" ở cuối
   danh sách để tải lại đúng trang đó. (platform)
9. WHEN người dùng gõ vào ô tìm kiếm, THE SYSTEM SHALL chờ người dùng ngừng gõ (debounce) rồi cắt
   khoảng trắng hai đầu, cắt còn 100 ký tự, gọi `GET /v1/temples` trang 1 với `q` (và `region` đang
   chọn), thay danh sách bằng kết quả và hiện số chùa khớp; THE SYSTEM SHALL bỏ kết quả của request cũ
   về muộn và không lọc gì trên máy. (FR-TPL-003, BR-TPL-05, NFR-TPL-01)
10. WHEN tìm kiếm (có hoặc không lọc miền) không khớp chùa nào, THE SYSTEM SHALL hiện "Không tìm thấy
    chùa" kèm gợi ý đổi từ khoá hoặc miền (⚠️ câu gợi ý — AC7). (FR-TPL-003)
11. WHEN người dùng xoá ô tìm kiếm (nút "Xóa" hoặc xoá hết chữ), THE SYSTEM SHALL gọi lại trang 1
    **không** có tham số `q` (giữ miền đang chọn); chuỗi chỉ toàn khoảng trắng được coi như rỗng.
    (FR-TPL-003, BR-TPL-05)
12. WHEN người dùng chọn một chip miền, THE SYSTEM SHALL hiện đúng bốn chip cố định "Tất cả · Miền Bắc ·
    Miền Trung · Miền Nam" (không suy ra từ dữ liệu), chỉ một chip được chọn, gọi trang 1 với `region`
    `north` / `central` / `south` (bỏ hẳn tham số khi "Tất cả") kèm từ khoá đang có. (FR-TPL-004,
    BR-TPL-04, BR-TPL-05)
13. WHEN backend trả `400 INVALID_FILTER` (lẽ ra AC9, AC11, AC12 đã chặn), THE SYSTEM SHALL hiện trạng
    thái lỗi chung có "Thử lại". (platform)
14. WHEN app bị hệ thống huỷ và mở lại ở danh bạ (process death), THE SYSTEM SHALL giữ từ khoá và miền
    đang chọn rồi tải lại trang 1 theo chúng. (platform)
15. WHEN người dùng bấm một thẻ chùa ở Trang chủ hoặc danh bạ, THE SYSTEM SHALL mở `TempleRoute(id)`
    với `id` của contract; back quay lại đúng vị trí cuộn và các trang đã tải. (FR-TPL-006, platform)

### Danh sách rút gọn (bộ chọn chùa)

16. WHEN một feature khác cần chọn chùa, THE SYSTEM SHALL dùng `TempleOptionRepository`
    (`GET /v1/temples/options`, tìm theo tên — §3.6) như `../Onboarding/requirements.md` tiêu chí 17 mô
    tả; spec này không định nghĩa lại bộ chọn, chỉ giữ repository dùng chung. (FR-TPL-005, BR-TPL-01,
    BR-TPL-05)

### Trang chùa

17. WHEN `TempleRoute(id)` mở (từ Trang chủ, danh bạ, tab Chùa / chùa của tôi, Lịch âm, Lịch lễ), THE
    SYSTEM SHALL hiện trạng thái đang tải và gọi `GET /v1/temples/{id}` không cần đăng nhập, rồi:
    - on success: hiện trang theo AC18–AC25;
    - on failure (`404 TEMPLE_NOT_FOUND`): hiện trạng thái "không tìm thấy chùa" có nút quay lại
      (⚠️ câu — AC7);
    - on failure (khác): hiện lỗi có "Thử lại", câu khác nhau khi mất mạng như AC7. (FR-TPL-006,
      BR-TPL-06)
18. WHEN trang chùa hiển thị, THE SYSTEM SHALL hiện các phần theo đúng thứ tự của FR-TPL-007 (ảnh bìa +
    cấp di tích, hệ phái, tên, "Còn gọi: …", nơi chốn; trạng thái mở cửa + giờ + khoảng cách; lưới niên
    đại · cấp di tích · hệ phái · miền; giới thiệu; trụ trì · tổ khai sơn · giáo hội; kiến trúc & di
    sản; tiện ích; lưu ý khi viếng; sự kiện & khóa học; vị trí & liên hệ; chân trang), mỗi giá trị thiếu
    xử lý theo BR-TPL-06 ("Đang cập nhật" / "Không có" / ảnh giữ chỗ / ẩn "Còn gọi" khi `aliases` rỗng).
    Chuỗi rỗng `""` của contract là "thiếu". (FR-TPL-007, BR-TPL-06)
19. WHEN phần giới thiệu dài hơn 5 dòng, THE SYSTEM SHALL thu gọn còn 5 dòng với "Đọc thêm", bấm thì mở
    hết và đổi thành "Thu gọn". (FR-TPL-007)
20. WHEN có lưu ý khi viếng, THE SYSTEM SHALL đánh số "01, 02, …" theo thứ tự nhận được. (FR-TPL-007)
21. WHEN hiện phần tiện ích, THE SYSTEM SHALL hiện bãi đỗ ô tô, giữ xe máy, cơm chay, khóa tu theo bốn
    cờ của `amenities` — cờ `false` hiện "Không có" (BR-TPL-06). (FR-TPL-007, BR-TPL-06)
    > ⚠️ Not confirmed (product): ô "Kinh sách" khi chưa có dữ liệu — FR-TPL-007 nói hiện "when known"
    > (tức ẩn), BR-TPL-06 nói các trường "later" hiện "Đang cập nhật" / "Không có" — chặn task Phase 2
    > "Tiện ích"; hand back to ba-writer in innerlamp-be. Mặc định đề xuất: ẩn ô tới khi contract có dữ
    > liệu.
22. WHEN hiện phần kiến trúc & di sản, THE SYSTEM SHALL hiện `architecture_heritage`; kiểu kiến trúc, vật
    liệu, điểm nổi bật và bộ ảnh chưa có trong contract nên hiện theo BR-TPL-06 (fields later).
    (FR-TPL-007, BR-TPL-06)
23. WHEN hiện vị trí & liên hệ, THE SYSTEM SHALL hiện `location.full_address`, miền, toạ độ, Hotline,
    Fanpage, Website, Email; liên hệ thiếu hiện "Đang cập nhật" và không bấm được. (FR-TPL-007,
    FR-TPL-008, BR-TPL-06)
24. WHEN `content_updated_month` có giá trị, THE SYSTEM SHALL hiện chân trang "Cập nhật MM/YYYY"; WHEN
    `null`, THE SYSTEM SHALL ẩn dòng đó; chân trang **không** có "Báo thông tin sai". (FR-TPL-007,
    BR-TPL-08)
25. WHEN trang chùa hiển thị, THE SYSTEM SHALL chỉ có hành động "Chỉ đường" ở hàng nút — không có "Theo
    dõi" (ẩn, không phải vô hiệu), không có "Cúng dường", không có link báo sai. (BR-TPL-08)
26. WHEN hiện phần "Sự kiện & khóa học", THE SYSTEM SHALL luôn hiện trạng thái trống "Chùa chưa đăng sự
    kiện nào." không kèm gợi ý theo dõi và không có "Xem tất cả"; app không gọi
    `GET /v1/temples/{id}/events` ở spec này (nội dung thuộc `sangha`). (FR-TPL-009, BR-TPL-08)

### Giờ mở cửa

27. WHEN trang chùa hiển thị và chùa có giờ mở cửa, THE SYSTEM SHALL tính trạng thái theo BR-TPL-09 với
    "bây giờ" lấy từ `Clock` được inject rồi đổi sang giờ Việt Nam (`Asia/Ho_Chi_Minh`), ngày âm của
    ngày Việt Nam đó qua `LunarCalendar` (múi +7, không lấy múi giờ máy), giờ Rằm & Mùng Một từ
    `holiday_operating_hours`; hiện dòng trạng thái, hai dòng giờ và nhãn "Hôm nay" theo BR-TPL-09;
    WHEN chùa không có giờ mở cửa, THE SYSTEM SHALL ẩn trạng thái và hiện giờ "Đang cập nhật".
    (BR-TPL-09, BR-TPL-06)
    > ⚠️ Not confirmed (product): dòng "Rằm & Mùng Một" hiện gì khi chùa có giờ thường nhưng không có giờ
    > Rằm (vector "Rằm without holiday hours uses normal hours" chỉ nói giờ áp dụng; BR-TPL-06 sẽ cho
    > "Đang cập nhật", backend AC46 coi `""` là "như ngày thường") — chặn task Phase 2 "Thẻ giờ"; hand
    > back to ba-writer in innerlamp-be.
28. WHILE trang chùa đang hiện, THE SYSTEM SHALL tính lại trạng thái mở cửa khi màn quay lại foreground
    (`ON_RESUME`) và mỗi khi sang phút mới. (platform)
29. WHEN `operating_hours` hoặc `holiday_operating_hours` không đọc được theo dạng `HH:MM - HH:MM`, THE
    SYSTEM SHALL coi như không có khung giờ đó để tính (ẩn trạng thái nếu là giờ thường) và hiện nguyên
    văn chuỗi ở dòng giờ. (platform)
    > ⚠️ Not confirmed (contract): dạng `HH:MM - HH:MM` mới là cam kết của backend spec v2.0 AC46 (chưa
    > implement); `openapi.yaml` @ 0eb7d1f chỉ có ví dụ `"07:00 - 18:00"` cho chuỗi tự do — chặn task
    > Phase 3 "Mapper giờ"; hand-off cho backend.

### Khoảng cách

30. WHEN trang chùa hiển thị và app đang có quyền vị trí (chính xác hoặc gần đúng), THE SYSTEM SHALL lấy
    vị trí hiện tại của máy, tính khoảng cách tới toạ độ chùa và hiện dòng "Cách bạn …" theo BR-TPL-07;
    WHEN không có quyền, không lấy được vị trí, hoặc quá thời gian chờ, THE SYSTEM SHALL ẩn dòng đó và
    không chặn phần còn lại của trang. (BR-TPL-07)
31. WHEN người dùng mở trang chùa lần đầu mà app chưa từng xin quyền vị trí, THE SYSTEM SHALL xin
    `ACCESS_COARSE_LOCATION` + `ACCESS_FINE_LOCATION` một lần bằng hộp thoại hệ thống; bị từ chối thì
    không xin lại và ẩn dòng khoảng cách. (BR-TPL-07, platform)
    > ⚠️ Not confirmed: thời điểm xin quyền vị trí trên Android (mặc định đề xuất: lần đầu mở trang chùa,
    > một lần; phương án khác: một nút "Hiện khoảng cách" — thêm UI ngoài prototype nên cần product) —
    > chặn task Phase 2 "Quyền vị trí".
32. WHEN app dùng vị trí của người dùng, THE SYSTEM SHALL chỉ dùng trong bộ nhớ để tính khoảng cách — không
    lưu, không gửi lên backend, không ghi log hay analytics. (BR-TPL-07, product §3.5, §3.7)

### Chỉ đường & liên hệ

33. WHEN người dùng bấm "Chỉ đường" (hàng nút hoặc phần vị trí), THE SYSTEM SHALL mở ứng dụng bản đồ mặc
    định với điểm đến là toạ độ chùa (intent `geo:`); WHEN máy không có app nào nhận intent đó, THE
    SYSTEM SHALL mở Google Maps chỉ đường trên trình duyệt. (FR-TPL-008, §3.6)
34. WHEN người dùng bấm một liên hệ có giá trị, THE SYSTEM SHALL mở trình quay số với số Hotline
    (`ACTION_DIAL`, không xin `CALL_PHONE`), mở Fanpage/Website trong trình duyệt, hoặc mở ứng dụng mail
    với địa chỉ Email (`mailto:`); WHEN liên hệ không có giá trị, THE SYSTEM SHALL không làm gì; WHEN máy
    không có app nhận intent, THE SYSTEM SHALL không crash. (FR-TPL-008, BR-TPL-06)

### Vector

35. WHEN chạy unit test, THE SYSTEM SHALL qua mọi case của `rules/temple-open-status.json` (BR-TPL-09) và
    `rules/temple-distance.json` (BR-TPL-07); `temple-order.json` và `temple-search-match.json` chỉ
    dành cho backend (product §4.1). (BR-TPL-07, BR-TPL-09)
36. WHEN đo trên mạng 4G, THE SYSTEM SHALL hiện trang 1 của một lần tìm hoặc đổi miền trong vòng 2 giây
    kể từ lúc gửi request. (NFR-TPL-01)

### Hiện trạng code so với từng AC (đọc ngày 2026-10-15)

| AC | Hiện trạng | Ghi chú |
|---|---|---|
| 1, 2 | lệch | `HomeSampleData.load()` (trễ giả 900 ms) lấy 8 chùa mẫu từ `GetTemplesUseCase`; có skeleton và thẻ rỗng "Chưa có chùa nào" + "Xem tất cả" |
| 3 | lệch | Lỗi và rỗng chung một thẻ rỗng (`Docs/home_assumptions.md` #13); `HomeViewModel.onReload()` có sẵn |
| 4 | lệch | Thẻ Trang chủ hiện miền, tên, nơi chốn, `note` (chú thích mẫu, không có trong contract); thẻ danh bạ hiện tên, "nơi · miền"; không ảnh (gradient giữ chỗ, #1) |
| 5, 6 | chưa có | `TempleListViewModel` lấy toàn bộ 8 chùa mẫu một lần, không phân trang — **drift BR-TPL-03 / NFR-TPL-02**; phụ đề "N chùa trong danh bạ" là tổng mẫu, không phải số tìm thấy |
| 7, 8, 13 | chưa có | Không có trạng thái lỗi |
| 9–11 | lệch | `TempleListUiState.filtered` lọc **trên máy**, khớp tên **và nơi chốn** (bỏ dấu qua `foldVietnamese`) — **drift BR-TPL-05** (server, tên + tên khác, không địa chỉ); câu rỗng "Không tìm thấy chùa nào khớp “q” ở …" |
| 12 | lệch | Chip miền suy từ dữ liệu, có "Tây Nguyên" — **drift BR-TPL-04** |
| 14 | chưa có | State không qua `SavedStateHandle` |
| 15 | ✓ phần điều hướng | `onTemple(id)` → `TempleRoute(id)`; id là id mẫu |
| 16 | ✓ repository | `TempleOptionRepositoryImpl` gọi `GET /v1/temples/options` qua `safeCall`, bỏ `q` rỗng, cắt 100 ký tự; bộ chọn chưa có (Onboarding) |
| 17 | chưa có | `GetTempleDetailUseCase` trả một chùa mẫu "Chùa Từ Quang" cho mọi id; không tải, không lỗi, không 404 |
| 18–24 | lệch | Bố cục cũ: hero, thống kê 3 cột (Phật tử / Thời khóa / Năm lập), giới thiệu, "Thời khóa sắp tới" (ghi danh), địa chỉ — thiếu gần hết các phần của FR-TPL-007 |
| 25 | lệch | Có "Theo dõi" (bật/tắt cục bộ) và "Cúng dường" — **drift BR-TPL-08** |
| 26 | lệch | "Thời khóa sắp tới" có dữ liệu mẫu và nhãn ghi danh (nội dung `sangha`) thay cho trạng thái trống |
| 27–29 | chưa có | Không tính trạng thái mở cửa |
| 30–32 | chưa có | Không có quyền/vị trí (`AndroidManifest.xml` chỉ có `USE_BIOMETRIC`) |
| 33, 34 | chưa có | `onDirections = {}` (no-op) trong `MainActivity.kt`; không có liên hệ |
| 35 | chưa có | Không có test vector |
| 36 | chưa đo | |

## Dữ liệu & API

Nguồn đúng là `openapi.yaml`; không chép schema ở đây. Mọi operation public (không `@Authenticated`).

| Thao tác | Operation (openapi) | Auth | Lưu cục bộ | Ghi chú |
|---|---|---|---|---|
| Chùa nổi bật | `GET /v1/temples/top` (`templesTop`) | public | không | Có trong contract |
| Danh bạ theo trang | `GET /v1/temples?page=` (`templesList`) | public | không | Có trong contract nhưng trang 20 |
| Tìm + lọc miền | `GET /v1/temples?page=&q=&region=` | public | không | ⚠️ Not confirmed (contract): `q` (tên + tên khác), `region`, `400 INVALID_FILTER` (`details.field` `q`/`region`) và trang 12 chưa có trong `openapi.yaml` @ 0eb7d1f (product Appendix B mục 1, 2; backend v2.0 AC 5, 39–44) — chặn task Phase 3 "Tìm/lọc"; hand-off cho backend |
| Trang chùa | `GET /v1/temples/{id}` (`templesGet`) | public | không | Có trong contract (phần cũ) |
| Trường mới ở trang chùa | `GET /v1/temples/{id}` | public | không | ⚠️ Not confirmed (contract): `holiday_operating_hours`, `sangha_body`, `contacts{hotline, fanpage, website, email}`, `content_updated_month` và dạng giờ `HH:MM - HH:MM` chưa có trong `openapi.yaml` @ 0eb7d1f (Appendix B mục 4; backend v2.0 AC 45–46) — chặn task Phase 3 "DTO chi tiết"; khi chưa có, các phần đó hiện theo BR-TPL-06 |
| Danh sách rút gọn | `GET /v1/temples/options` (`templesOptions`) | public | không | Đã nối (`TempleOptionRepositoryImpl`); dùng bởi `../Onboarding/` |
| Sự kiện của chùa | `GET /v1/temples/{id}/events` | Bearer | — | **Không dùng** ở spec này (FR-TPL-009: nội dung từ `sangha`) |
| Ảnh | `thumbnail_url` / `image_url` | public | cache ảnh của Coil | Dùng nguyên URL server trả |
| Vị trí người dùng | — (máy) | — | không lưu | BR-TPL-07, product §3.5 |

## Error → UI mapping

Câu chữ còn chờ product (⚠️ AC7); bảng chỉ nói người dùng thấy gì.

| Operation | Failure case | User sees | Hiện trạng code |
|---|---|---|---|
| `GET /v1/temples/top` | Mọi lỗi (`NoConnection`, `Timeout`, 5xx, `Unknown`, `Serialization`) | Thẻ rỗng của khối + "Thử lại" (AC3; ⚠️ product) | lệch (thẻ rỗng chung, không gọi API) |
| `GET /v1/temples` trang 1 | `NoConnection` / `Timeout` | Trạng thái lỗi câu "mất mạng" + "Thử lại" (AC7) | chưa có |
| `GET /v1/temples` trang 1 | 5xx / `Unknown` / `Serialization` / `400 INVALID_FILTER` / `400 INVALID_PAGE` | Trạng thái lỗi câu chung + "Thử lại" (AC7, AC13) | chưa có |
| `GET /v1/temples` trang ≥ 2 | Mọi lỗi | Giữ danh sách, "Thử lại" ở cuối (AC8) | chưa có |
| `GET /v1/temples` | 200, `total = 0` | "Không tìm thấy chùa" + gợi ý (AC10); không có từ khoá và miền → trạng thái trống của danh bạ | lệch (lọc trên máy) |
| `GET /v1/temples/{id}` | `404 TEMPLE_NOT_FOUND` | "Không tìm thấy chùa" + quay lại (AC17) | chưa có |
| `GET /v1/temples/{id}` | `NoConnection` / `Timeout` / lỗi khác | Lỗi + "Thử lại", câu theo AC7 (AC17) | chưa có |
| (máy) | Không có quyền / không lấy được vị trí | Ẩn dòng "Cách bạn …" (AC30) | chưa có |
| (máy) | Không có app bản đồ | Mở Google Maps trên trình duyệt (AC33) | chưa có |
| (máy) | Không có app nhận liên hệ | Không làm gì, không crash (AC34) | chưa có |

## Out of scope

- Theo dõi chùa, cúng dường, báo thông tin sai, đề xuất thêm chùa (`temple-suggestion`), bản đồ chùa, tìm
  "gần tôi", sắp theo khoảng cách, đánh giá (product §1.2).
- Chùa của tôi / đạo tràng, thời khóa, sự kiện của chùa, ghi danh (`sangha`) — gồm tab Chùa
  (`ui/screens/chua/`), màn `EventsRoute`, và nội dung "Sự kiện & khóa học" khi `sangha` cung cấp.
- Bộ chọn chùa khi đăng ký (`../Onboarding/`); spec này chỉ giữ `TempleOptionRepository` dùng chung.
- Bản đọc offline (product §3.6 cho phép, Android **không** làm ở v1.0 — xem `design.md` "Local storage").
- Màn "Ảnh chùa đẹp" (`ui/screens/photos/`, `GetTemplePhotosUseCase`) — không thuộc product `temple`.
- Lịch lễ Phật giáo (`ui/screens/festivals/`), Lịch âm (`ui/screens/calendar/`), Hồ sơ: chỉ là điểm mở
  `TempleRoute`; việc đổi id mẫu của chúng sang id thật thuộc spec của các feature đó.
- Thứ tự, chùa nổi bật, tìm kiếm, lọc — server tính (BR-TPL-01, BR-TPL-02, BR-TPL-05).

## Pending (not yet implemented)

- Domain: model theo contract, repository, use case, quy tắc giờ mở cửa và khoảng cách — `tasks.md`
  Phase 1.
- Khối nổi bật, danh bạ có phân trang + tìm/lọc trên server, trang chùa theo FR-TPL-007, bỏ Theo dõi /
  Cúng dường / thời khóa, chỉ đường, liên hệ, quyền vị trí (AC1–AC34) — Phase 2.
- Data layer cho `top`, danh sách, chi tiết (AC1, AC5–AC13, AC17) — Phase 3; tìm/lọc và trường mới chặn
  bởi contract.
- Vector (AC35), đo NFR (AC36) — Phase 4.
- Dọn dữ liệu mẫu — Phase 5 (các dòng `Docs/home_assumptions.md` về chùa đã gỡ ở v1.1).

## Open questions (resolved)

Theo product spec v1.1 (2026-10-15) — các điểm này thay cho hành vi hiện có của code Android:

- ~~Miền "Tây Nguyên" trong dữ liệu mẫu~~ → chỉ ba miền, chùa Tây Nguyên thuộc Miền Trung (BR-TPL-04) →
  AC12. Resolved 2026-10-15.
- ~~Tìm trên máy theo tên + địa danh~~ → tìm trên server theo tên + tên khác, không địa chỉ (BR-TPL-05)
  → AC9–AC11. Resolved 2026-10-15.
- ~~Nút "Theo dõi" bật/tắt cục bộ, nút "Cúng dường"~~ → ẩn cả hai (BR-TPL-08) → AC25. Resolved 2026-10-15.
- ~~"Báo thông tin sai" ở chân trang (prototype)~~ → không làm (BR-TPL-08) → AC24. Resolved 2026-10-15.
- ~~Khoảng cách từ điểm cố định Quận 1 (prototype)~~ → chỉ khi người dùng cho phép vị trí (BR-TPL-07) →
  AC30–AC32. Resolved 2026-10-15.
- ~~Giờ mở cửa theo múi giờ máy~~ → luôn giờ Việt Nam, giờ Rằm & Mùng Một theo âm lịch (BR-TPL-09) →
  AC27. Resolved 2026-10-15.
- ~~Ảnh, chùa trong carousel, nguồn dữ liệu Trang chủ (`Docs/home_assumptions.md` #1, #3, #13 — phần
  chùa)~~ → dữ liệu từ backend (FR-TPL-001), ảnh theo URL contract, ảnh giữ chỗ khi không có (BR-TPL-06);
  thẻ lỗi của khối chùa: AC3 (⚠️ product). Phần chùa của các dòng đó sẽ xoá ở Phase 5. Resolved
  2026-10-15.
