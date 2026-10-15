# Requirements — Deity

Version: 1.0
Last updated: 2026-10-15
Status: planned (drift) — màn "Chư Phật & Bồ Tát" (`buddhas`), "Chi tiết Phật / Bồ Tát" (`buddha`), khối Trang chủ và Thư viện đã có UI nhưng chạy trên 9 hồ sơ mẫu cứng trong domain; lọc trên máy, không phân trang, không có loại Chư Thiên, không có Hình tượng, vía tính trên máy (quét âm lịch 400 ngày), không truyền thần chú sang Niệm Phật, "Chia sẻ" no-op, chân trang còn "Báo thông tin sai". Chưa có repository/API.
Feature folder: `app/src/main/java/com/example/innerlamp/ui/screens/buddha/` (tên thư mục code giữ nguyên, product §1.5); khối nổi bật ở `ui/screens/home/` và `ui/screens/library/`
Product spec: innerlamp-be `docs/product/deity/product.md` @ v1.0
Contract: innerlamp-be `docs/specs/api/openapi.yaml` @ a8a016b (info.version 1.0.0) — tag Deities (`deitiesTop`, `deitiesList`, `deitiesGet`) và `deitiesSutras`; phần v3.0 (trang 12, `next_via`, `via_events`, tham số `date`, `400 INVALID_DATE`) theo innerlamp-be `docs/specs/features/deity/requirements.md` @ v3.0 (2026-10-15) — **contract not yet in openapi.yaml** (backend `tasks.md` Phase 9)
Implements: BR-DTY-01, BR-DTY-02, BR-DTY-03, BR-DTY-04, BR-DTY-05, BR-DTY-06, BR-DTY-07, BR-DTY-08, FR-DTY-001, FR-DTY-002, FR-DTY-003, FR-DTY-004, FR-DTY-005, FR-DTY-006, FR-DTY-007, FR-DTY-008, FR-DTY-009, FR-DTY-010, NFR-DTY-01, NFR-DTY-02; liên quan BR-CAL-08, BR-CAL-12, BR-CAL-13 (calendar @ v1.2) — FR-DTY-011 là Backend (Android `n/a` trong product §4.2)

> See `changelog.md` in this folder for the version history of this spec.

Spec platform (layer 2) của **danh mục Chư Phật & Bồ Tát** cho Android. Viết kiểu hỗn hợp: hiện trạng đọc
từ code ngày 2026-10-15 (branch `docs/deity-specs`, dựa trên `main` @ ec7d77e), phần còn lại prospective.
Quy tắc sản phẩm (loại và nhãn, thứ tự, cỡ trang, trường thiếu, nguồn ngày vía, lần vía kế tiếp, đại
nguyện thu gọn) nằm ở product spec và **không** nhắc lại ở đây; file này chỉ nói app Android hiện thực
các `BR-DTY-*`/`FR-DTY-*` thế nào.

## Ràng buộc từ product spec

- **Công khai, chỉ đọc** (§2.3, §2.7): không cần đăng nhập; không Bearer (giống `TempleApiService`).
- **Server quyết định thứ tự, lọc và trang** (BR-DTY-02, BR-DTY-03, NFR-DTY-01): app hiện đúng thứ tự
  nhận được, không sắp lại, không lọc trên máy, không tải cả danh mục một lần. Vector `deity-order.json`
  chỉ chạy ở backend (product §4.1).
- **Ngày vía và lần vía kế tiếp do server trả** (BR-DTY-05, BR-DTY-06; backend deity v3.0 chọn "server
  tính"): app **không** đổi âm ↔ dương, không quét lịch, không giữ danh sách ngày vía riêng; app chỉ gửi
  ngày của máy làm "hôm nay" và tự tính **đếm ngược** (BR-CAL-13). Vì app không tự chọn lần vía kế tiếp,
  `deity-next-via.json` không phải vector của Android (product BR-DTY-06 "every platform that picks it");
  phần đếm ngược chạy vector `calendar/rules/countdown.json` qua `CalendarCountdown` của `../Calendar/`.
- **Không dữ liệu mẫu** (FR-DTY-010, §2.1 "Android shows nine sample entries"): mọi vị đến từ backend; id
  của contract (vd `bodhisattva_avalokiteshvara`) thay cho id mẫu (`quanam`, `thichca`, …).
- **Phụ thuộc:** `../Calendar/` (`CalendarCountdown`, `LunarDay`, màn Lịch lễ `FestivalsRoute`); màn Niệm
  Phật (`ui/screens/mala/`, chưa có spec Android); màn bài kinh (`SutraRoute`, chưa có product spec
  `sutra` lẫn spec Android).

## User Story

Là người dùng (đã hoặc chưa đăng nhập), tôi muốn xem các vị Phật, Bồ Tát và Chư Thiên nổi bật ở Trang chủ
và Thư viện, duyệt toàn bộ danh mục theo loại, và mở trang một vị để đọc tiểu sử, đại nguyện, hình tượng,
thần chú, biết ngày vía sắp tới, trì niệm với tràng hạt, chia sẻ và mở kinh liên quan (product §1.2,
§2.2).

## Acceptance Criteria (EARS notation)

### Vị nổi bật ở Trang chủ và Thư viện

1. WHEN Trang chủ mở, THE SYSTEM SHALL gọi `GET /v1/deities/top?date=<ngày máy>` (public) và hiện thẻ các
   vị trong khối "Thư viện Phật & Bồ Tát" đúng thứ tự nhận được, hiện **tất cả** mục nhận được trong
   carousel cuộn ngang (product §3.6: số thẻ do layout; Android cuộn được nên không cắt), kèm "Xem tất
   cả" mở `BuddhaListRoute`. (FR-DTY-001, BR-DTY-02)
2. WHEN Thư viện mở, THE SYSTEM SHALL hiện khối "Chư Phật & Bồ Tát" từ cùng nguồn `top` (cùng thứ tự,
   dải cuộn ngang, "Xem tất cả" mở `BuddhaListRoute`), và ô "Phật & Bồ Tát" trong lưới hub mở
   `BuddhaListRoute`. (FR-DTY-001, BR-DTY-02)
3. WHEN người dùng bấm một thẻ ở Trang chủ, Thư viện hoặc danh mục, THE SYSTEM SHALL mở
   `BuddhaRoute(buddhaId)` với `id` của contract; back quay lại đúng vị trí cuộn và các trang đã tải.
   (FR-DTY-001, FR-DTY-004, platform)
4. WHEN `top` trả rỗng hoặc thất bại, THE SYSTEM SHALL hiện trạng thái trống riêng của khối (không thẻ
   nào, không thẻ mẫu) kèm "Thử lại" gọi lại `top`, không chặn các khối khác của màn. (FR-DTY-010)
   - Trang chủ: giữ thẻ rỗng hiện có ("Thư viện đang trống" + "Thử lại", `HomeEmptyCard`).
   > ⚠️ Not confirmed (product): câu chữ trạng thái trống của khối "Chư Phật & Bồ Tát" ở **Thư viện**
   > (product chỉ nói "their own empty state"; prototype không có biến thể trống cho Thư viện) — chặn task
   > Phase 2 "Khối Thư viện"; hand back to ba-writer in innerlamp-be. Mặc định đề xuất: dùng cùng câu với
   > thẻ rỗng của Trang chủ.

### Thẻ một vị (Trang chủ, Thư viện, danh mục)

5. WHEN hiện một thẻ, THE SYSTEM SHALL hiện ảnh `thumbnail_url` (Coil; `""`/lỗi tải → ảnh giữ chỗ), nhãn
   loại theo BR-DTY-01 ("Phật" / "Bồ Tát" / "Chư Thiên" cho `BUDDHA` / `BODHISATTVA` / `DEVA`), `name_vi`,
   `core_virtue` (thiếu → bỏ dòng, BR-DTY-04; chỉ thẻ danh mục — thẻ Trang chủ/Thư viện theo prototype
   không có dòng này) và "Vía dd/MM âm lịch" lấy từ ngày âm của `next_via`; `next_via = null` → không
   có dòng vía. (FR-DTY-002, BR-DTY-01, BR-DTY-04, BR-DTY-06)
   > ⚠️ Not confirmed (product): thẻ và ô ngày vía hiện thế nào khi `next_via` rơi vào **tháng nhuận**
   > (BR-CAL-08 thêm "nhuận" sau tháng; định dạng "Vía dd/MM âm lịch" của BR-DTY-06 không có chỗ) — chặn
   > task Phase 2 "Định dạng ngày vía"; hand back to ba-writer in innerlamp-be. Mặc định đề xuất:
   > "Vía dd/MM nhuận âm lịch".
6. WHEN `type` không phải một trong ba giá trị đã biết (contract coi là enum mở), THE SYSTEM SHALL vẫn
   hiện vị đó như một mục thường và không hiện nhãn loại. (BR-DTY-01, platform)
   > ⚠️ Not confirmed (product): nhãn cho một loại lạ (ẩn nhãn hay nhãn chung) — không chặn (hiện chỉ có
   > ba loại); hand back to ba-writer in innerlamp-be.

### Danh mục "Chư Phật & Bồ Tát"

7. WHEN người dùng mở `BuddhaListRoute`, THE SYSTEM SHALL gọi `GET /v1/deities?page=1&date=<ngày máy>`
   (không `type`), hiện các vị đúng thứ tự nhận được và phụ đề "{N} vị · tiểu sử, đại nguyện, thần chú"
   với N = `total` của response. (FR-DTY-002, BR-DTY-02, BR-DTY-03)
8. WHEN người dùng cuộn tới gần cuối danh sách và `page < total_pages`, THE SYSTEM SHALL tải trang kế tiếp
   và nối vào cuối, mỗi lúc chỉ một request; THE SYSTEM SHALL dừng khi `page >= total_pages` hoặc trang
   trả về rỗng, và đọc cỡ trang từ `page_size` của response, không giả định 12. (FR-DTY-002, BR-DTY-03,
   NFR-DTY-01, §3.6)
9. WHEN người dùng chọn chip "Chư Phật" hoặc "Bồ Tát", THE SYSTEM SHALL hiện đúng ba chip cố định "Tất cả ·
   Chư Phật · Bồ Tát" (không có chip "Chư Thiên"), chỉ một chip được chọn, gọi lại trang 1 với
   `type=BUDDHA` / `type=BODHISATTVA` (bỏ hẳn tham số khi "Tất cả"), thay danh sách và cập nhật N; THE
   SYSTEM SHALL bỏ kết quả của request cũ về muộn và không lọc gì trên máy. (FR-DTY-003, BR-DTY-01,
   BR-DTY-03, NFR-DTY-02)
10. WHEN tải trang 1 thất bại (mọi lỗi) **hoặc** trang 1 của "Tất cả" trả `total = 0`, THE SYSTEM SHALL
    hiện trạng thái trống của prototype `buddhasEmpty` — "Chưa có thông tin chư Phật", câu gợi ý kiểm tra
    kết nối, "Thử lại" (gọi lại trang 1 với chip đang chọn) và "Xem lịch lễ vía" (mở `FestivalsRoute`) —
    phụ đề header đổi thành "Chưa có dữ liệu", ẩn chip lọc. (FR-DTY-010)
11. WHEN một chip loại trả `total = 0` (không lỗi), THE SYSTEM SHALL hiện cùng trạng thái trống của AC10
    nhưng giữ dãy chip để người dùng đổi lại. (FR-DTY-010, platform)
12. WHEN tải một trang kế tiếp thất bại, THE SYSTEM SHALL giữ các vị đã tải và hiện "Thử lại" ở cuối danh
    sách để tải lại đúng trang đó. (platform)
13. WHEN app bị hệ thống huỷ và mở lại ở danh mục (process death), THE SYSTEM SHALL giữ chip đang chọn rồi
    tải lại trang 1 theo nó. (platform)

### Trang một vị

14. WHEN `BuddhaRoute(buddhaId)` mở, THE SYSTEM SHALL hiện trạng thái đang tải và gọi
    `GET /v1/deities/{id}?date=<ngày máy>` không cần đăng nhập, rồi:
    - on success: hiện trang theo AC15–AC24;
    - on failure (`404 DEITY_NOT_FOUND`): hiện trạng thái "không tìm thấy" có nút quay lại;
    - on failure (khác, kể cả `400 INVALID_DATE`): hiện lỗi có "Thử lại". (FR-DTY-004, BR-DTY-04)
    > ⚠️ Not confirmed (product): câu chữ của trạng thái "không tìm thấy" và của lỗi tải trang một vị
    > (product chỉ mô tả ý; prototype không có biến thể lỗi cho `buddha`) — chặn task Phase 2 "Trạng thái
    > trang một vị"; hand back to ba-writer in innerlamp-be.
15. WHEN trang một vị hiển thị, THE SYSTEM SHALL hiện các phần đúng thứ tự FR-DTY-005: bìa `image_url`
    (giữ chỗ khi `""`/lỗi) với nhãn loại và `pure_land`; `name_vi`, `title_honorific`, `name_sanskrit`,
    `name_chinese`; thẻ vía (AC17); `core_virtue` và dòng `canonical_source`; "Tiểu sử"; "Đại nguyện";
    "Hình tượng"; "Thần chú & danh hiệu" + hành động (AC20–AC22); "Kinh liên quan" (AC23); chân trang
    (AC24). Mỗi trường/phần thiếu ẩn theo BR-DTY-04; chuỗi rỗng `""` và mảng rỗng của contract là
    "thiếu". Tiêu đề header khi cuộn là `name_vi`. (FR-DTY-005, BR-DTY-04)
16. WHEN tiểu sử dài hơn chiều cao thu gọn (6 dòng, prototype), THE SYSTEM SHALL thu gọn với "Đọc thêm",
    bấm thì mở hết và đổi thành "Thu gọn"; WHEN tiểu sử vừa 6 dòng, THE SYSTEM SHALL không hiện nút.
    (FR-DTY-005)
17. WHEN trang một vị hiển thị và `next_via` khác `null`, THE SYSTEM SHALL hiện thẻ vía: tên sự kiện, ô
    ngày âm (ngày + "THÁNG GIÊNG" / "THÁNG n" / "THÁNG CHẠP" theo FR-DTY-006), ngày dương kèm thứ, và
    đếm ngược theo BR-CAL-13 tính từ ngày máy tới `solar_date` qua `CalendarCountdown` ("Hôm nay" /
    "Ngày mai" / "Còn n ngày"), kèm "Lịch lễ" mở `FestivalsRoute`; WHEN `next_via = null`, THE SYSTEM
    SHALL ẩn thẻ vía. (FR-DTY-006, BR-DTY-05, BR-DTY-06, BR-DTY-04, BR-CAL-13)
18. WHEN thẻ vía hiển thị, THE SYSTEM SHALL liệt kê mọi ngày vía của vị theo `via_events` đúng thứ tự nhận
    được, mỗi mục có tên và (nếu có `next_occurrence`) ngày âm + ngày dương; app không dùng
    `lunar_memorial_days` (deprecated, backend deity v3.0 AC30). (FR-DTY-006, BR-DTY-05, BR-CAL-08,
    BR-CAL-12)
    > ⚠️ Not confirmed (product): một ngày vía **không có** lần diễn ra trong cửa sổ năm nay + năm sau
    > (`next_occurrence = null`, vd. sự kiện tháng nhuận) có hiện trong danh sách không, và hiện gì — chặn
    > task Phase 2 "Danh sách ngày vía"; hand back to ba-writer in innerlamp-be. Mặc định đề xuất: hiện
    > tên, không ngày.
19. WHEN số đại nguyện lớn hơn 5, THE SYSTEM SHALL hiện 5 đại nguyện đầu, đánh số từ 1 theo thứ tự nhận
    được, và "Xem đủ N đại nguyện" mở hết rồi đổi thành "Thu gọn"; WHEN ≤ 5, THE SYSTEM SHALL hiện hết, không
    nút; WHEN `great_vows` rỗng, THE SYSTEM SHALL ẩn phần. (BR-DTY-07, BR-DTY-04)
20. WHEN hiện "Hình tượng", THE SYSTEM SHALL hiện tư thế, toà ngồi / linh thú, pháp khí (mỗi mục một dòng)
    và ý nghĩa biểu tượng của `iconography`, ẩn từng dòng thiếu và ẩn cả phần khi cả bốn đều thiếu.
    (FR-DTY-005, BR-DTY-04)
21. WHEN hiện "Thần chú & danh hiệu", THE SYSTEM SHALL hiện tối đa ba thẻ theo thứ tự "Danh hiệu"
    (`title_honorific`), "Thần chú" (chú ngắn Hán Việt rồi Sanskrit), "Đà-la-ni" (tên + đoạn trích), mỗi thẻ
    thiếu nội dung thì ẩn, và ẩn cả phần khi không thẻ nào có nội dung; thẻ không còn dòng chú thích
    `note` của dữ liệu mẫu (contract không có). (FR-DTY-005, BR-DTY-04)
22. WHEN phần "Thần chú & danh hiệu" hiển thị, THE SYSTEM SHALL hiện **một** hàng hành động "Trì niệm với
    tràng hạt" + "Chia sẻ" ngay sau các thẻ (đặt trong thẻ "Thần chú" theo kiểu nút của prototype), thay
    cho hai nút lặp lại trên từng thẻ hiện nay; "Trì niệm với tràng hạt" chỉ hiện khi có chú ngắn Hán
    Việt. (FR-DTY-005, FR-DTY-008, FR-DTY-009, BR-DTY-04)
    > ⚠️ Not confirmed (product): "Chia sẻ" có hiện (và chia sẻ gì) khi vị **không có** chú ngắn — FR-DTY-009
    > chỉ nói chia sẻ tên + chú ngắn — chặn task Phase 2 "Chia sẻ"; hand back to ba-writer in innerlamp-be.
    > Mặc định đề xuất: vẫn hiện, chỉ chia sẻ tên.

### Hành động thực hành

23. WHEN người dùng bấm "Trì niệm với tràng hạt", THE SYSTEM SHALL mở màn Niệm Phật với câu niệm là chú
    ngắn Hán Việt của vị và số hạt 0 (không giữ số hạt cũ). (FR-DTY-008)
24. WHEN người dùng bấm "Chia sẻ", THE SYSTEM SHALL mở bảng chia sẻ của hệ thống (`Intent.ACTION_SEND`,
    `text/plain`, qua `Intent.createChooser`) với văn bản gồm `name_vi`, chú ngắn Hán Việt và (nếu có)
    Sanskrit, mỗi phần một dòng; không tự mở link; WHEN không app nào nhận, THE SYSTEM SHALL không crash.
    (FR-DTY-009, BR-DTY-04)
    > ⚠️ Not confirmed (product): văn bản chia sẻ có kèm link trang web của vị không (product Appendix A
    > mục 3) — chặn nội dung chia sẻ cuối cùng (task Phase 2 "Chia sẻ"); hand back to ba-writer in
    > innerlamp-be. Tới khi có câu trả lời: không có link.

### Kinh liên quan và chân trang

25. WHEN trang một vị tải xong, THE SYSTEM SHALL gọi `GET /v1/deities/{id}/sutras` (song song với chi
    tiết, tải hết các trang theo `total_pages`) và hiện "Kinh liên quan" theo thứ tự nhận được; bấm một
    mục mở `SutraRoute(sutraId)` với `id` của contract; WHEN không có kinh liên kết (`total = 0`), THE
    SYSTEM SHALL ẩn phần; app không còn khớp kinh theo tên và không còn kinh dự phòng "Pháp Cú".
    (FR-DTY-007, BR-DTY-04)
    > ⚠️ Not confirmed (product): thứ tự — FR-DTY-007 nói "in the sutra library's order", còn
    > `deitiesSutras` xếp theo vai (`SUBJECT`, `SPEAKER`, `MENTIONED`) rồi thứ tự biên tập — không chặn
    > Android (app hiện đúng thứ tự server trả); hand back to ba-writer in innerlamp-be.
    > ⚠️ Not confirmed (product): mỗi dòng kinh hiện gì ngoài tên tiếng Việt (prototype có dòng phụ kiểu
    > "26 phẩm · 423 kệ", contract không có trường đó) — chặn task Phase 2 "Dòng kinh"; hand back to
    > ba-writer in innerlamp-be. Mặc định đề xuất: tên + `collection`.
26. WHEN `GET /v1/deities/{id}/sutras` thất bại trong khi chi tiết thành công, THE SYSTEM SHALL ẩn phần
    "Kinh liên quan" và không chặn phần còn lại của trang. (FR-DTY-007, platform)
    > ⚠️ Not confirmed (product): lỗi riêng của "Kinh liên quan" có cần báo người dùng/"Thử lại" không —
    > không chặn; hand back to ba-writer in innerlamp-be.
27. WHEN trang một vị hiển thị, THE SYSTEM SHALL hiện chân trang chỉ với "Nội dung do Ban biên tập tổng
    hợp từ kinh điển" — không có "Báo thông tin sai", không yêu thích, không theo dõi. (BR-DTY-08)

### Ngày của máy

28. WHEN app gọi một trong ba operation deity, THE SYSTEM SHALL gửi `date` = ngày hiện tại của máy
    (`LocalDate.now(clock)`, múi giờ máy) dạng `YYYY-MM-DD`, và dùng **cùng** ngày đó làm "hôm nay" cho
    đếm ngược. (BR-DTY-06, BR-CAL-13)
29. WHEN trang một vị quay lại foreground (`ON_RESUME`) và ngày của máy đã khác ngày dùng cho lần tải,
    THE SYSTEM SHALL tải lại chi tiết với ngày mới (để `next_via` và đếm ngược không thành "Đã qua").
    (BR-DTY-06, platform)

### Hiệu năng và vector

30. WHEN đo trên mạng 4G, THE SYSTEM SHALL hiện trang 1 của một lần đổi chip trong vòng 2 giây kể từ lúc
    gửi request. (NFR-DTY-02)
31. WHEN chạy unit test, THE SYSTEM SHALL qua mọi case của innerlamp-be
    `docs/product/calendar/rules/countdown.json` (BR-CAL-13) qua `CalendarCountdown` (dùng chung với
    `../Calendar/`, task ở đó) và test riêng việc trang một vị dùng `CalendarCountdown` với ngày đã gửi;
    `deity-order.json` và `deity-next-via.json` không chạy trên Android (server chọn thứ tự và lần vía).
    (BR-DTY-06, BR-CAL-13)

### Hiện trạng code so với từng AC (đọc ngày 2026-10-15)

| AC | Hiện trạng | Ghi chú |
|---|---|---|
| 1 | lệch | `HomeSampleData.load()` (trễ giả 900 ms) trả 9 `BuddhaFigure` mẫu; có skeleton + carousel; id mẫu |
| 2 | lệch | `LibraryViewModel` lấy đồng bộ 9 `BuddhaProfile` từ `GetBuddhaProfilesUseCase`; ô hub `LibraryHub.Buddhas` → `BuddhaListRoute` ✓ |
| 3 | ✓ phần điều hướng | `onBuddha(id)` → `BuddhaRoute(id)` ở Home, Library, danh mục; id là id mẫu |
| 4 | lệch | Home: thẻ rỗng có, nhưng lỗi và rỗng chung với 2 khối khác (`Docs/home_assumptions.md` #13); Library: không có trạng thái trống |
| 5 | lệch | Không ảnh (gradient giữ chỗ, `home_assumptions.md` #1); nhãn "Phật tương lai" (Di Lặc) — **drift BR-DTY-01**; vía là ngày âm cứng trong mẫu — **drift BR-DTY-05**; dòng phụ là `role` mẫu |
| 6 | chưa có | `BuddhaCategory` chỉ có `Buddha`, `Bodhisattva`; không có Chư Thiên — **drift BR-DTY-01** |
| 7, 8 | chưa có | Không phân trang; phụ đề đếm cả 9 mẫu — **drift BR-DTY-03 / NFR-DTY-01** |
| 9 | lệch | Ba chip đúng, nhưng `BuddhaListUiState.filtered` lọc **trên máy**; phụ đề không theo chip |
| 10–12 | chưa có | Không có trạng thái trống/lỗi (`TODO(il-data-builder)` trong `BuddhaListViewModel`) |
| 13 | chưa có | Chip không qua `SavedStateHandle` |
| 14 | chưa có | `GetBuddhaDetailUseCase` trả vị đầu tiên khi id không khớp; không tải, không lỗi, không 404 |
| 15 | lệch | Thiếu `title_honorific`, tên chữ Hán, `canonical_source`; header dùng `shortName` (contract không có); có `vowIntro` mẫu |
| 16 | lệch | "Đọc thêm" luôn hiện kể cả khi tiểu sử ngắn |
| 17 | lệch | Một ngày vía cứng + quét âm lịch 400 ngày trên máy (`GetBuddhaDetailUseCase.nextVia`) — **drift BR-DTY-05/06**; đếm ngược có (logic trong `BuddhaDetailUiState.nextViaLabel`); "Lịch lễ" → `FestivalsRoute` ✓ |
| 18 | lệch | Ngày vía khác chỉ là chuỗi `extraVia` mẫu |
| 19 | ✓ phần thu gọn | 5 + "Xem đủ …" + "Thu gọn" có; nhãn dùng `vowUnit` mẫu ("lời nguyện", "hạnh nguyện"), nhãn đếm "· trích" — sửa theo AC19 |
| 20 | chưa có | Không có phần Hình tượng — **drift FR-DTY-005** |
| 21, 22 | lệch | Thẻ mẫu "DANH HIỆU"/"TÂM CHÚ" kèm `note`; hai nút trên **mỗi** thẻ |
| 23 | lệch | `onChant` mở `MalaRoute` không truyền câu niệm (`MalaUiState.mantra` mặc định "Nam mô A Di Đà Phật") — **drift FR-DTY-008** |
| 24 | chưa có | `onShare = {}` (no-op) trong `MainActivity.kt` — **drift FR-DTY-009** |
| 25, 26 | lệch | Kinh liên quan là `BuddhaSutraRef` mẫu, có kinh dự phòng `phapcu` — **drift FR-DTY-007** |
| 27 | lệch | Chân trang "… · Báo thông tin sai" — **drift BR-DTY-08** |
| 28, 29 | chưa có | `Clock` inject có ở ViewModel/use case nhưng không gọi API |
| 30, 31 | chưa có | `GetBuddhaDetailUseCaseTest` test quét âm lịch — sẽ bỏ |

## Dữ liệu & API

Nguồn đúng là `openapi.yaml` (và backend deity `requirements.md` v3.0 cho phần chưa vào contract); không
chép schema ở đây. Mọi operation public (không `@Authenticated`).

| Thao tác | Operation (openapi) | Auth | Lưu cục bộ | Ghi chú |
|---|---|---|---|---|
| Vị nổi bật | `GET /v1/deities/top?date=` (`deitiesTop`) | public | không | Có trong contract (tối đa 10); `date` và `next_via` trên mục: backend deity v3.0 AC 2, 24–26, 29 — ⚠️ contract not yet in openapi.yaml |
| Danh mục theo trang + lọc loại | `GET /v1/deities?page=&type=&date=` (`deitiesList`) | public | không | `page`, `type` có trong contract (trang 20 hiện tại); trang 12, `date`, `next_via`: backend deity v3.0 AC 5, 22, 24–26 — ⚠️ contract not yet in openapi.yaml |
| Trang một vị | `GET /v1/deities/{id}?date=` (`deitiesGet`) | public | không | Phần hồ sơ có trong contract; `next_via`, `via_events`, `date`: backend deity v3.0 AC 8, 24–29 — ⚠️ contract not yet in openapi.yaml. **Không** dùng `lunar_memorial_days` |
| Kinh liên quan | `GET /v1/deities/{id}/sutras?page=` (`deitiesSutras`) | public | không | Có trong contract; không gửi `role` |
| Ảnh | `thumbnail_url` / `image_url` | public | cache ảnh của Coil | Dùng nguyên URL server trả; `""` → giữ chỗ |
| Câu niệm sang Niệm Phật | — (điều hướng trong app) | — | không | Route arg, AC23 |
| Chia sẻ | — (`ACTION_SEND`) | — | không | AC24 |

## Error → UI mapping

| Operation | Failure case | User sees | Hiện trạng code |
|---|---|---|---|
| `GET /v1/deities/top` | Mọi lỗi (`NoConnection`, `Timeout`, 5xx, `Unknown`, `Serialization`, `400 INVALID_DATE`) hoặc 200 rỗng | Trạng thái trống của khối + "Thử lại" (AC4; Library ⚠️ câu) | lệch (Home: thẻ rỗng chung; Library: không có) |
| `GET /v1/deities` trang 1 | Mọi lỗi, hoặc `total = 0` ở "Tất cả" | `buddhasEmpty`: "Chưa có thông tin chư Phật" + gợi ý kiểm tra kết nối + "Thử lại" + "Xem lịch lễ vía" (AC10) | chưa có |
| `GET /v1/deities` trang 1 | 200, `total = 0` ở chip loại | Trạng thái trống AC10, giữ chip (AC11) | chưa có |
| `GET /v1/deities` trang ≥ 2 | Mọi lỗi | Giữ danh sách, "Thử lại" ở cuối (AC12) | chưa có |
| `GET /v1/deities/{id}` | `404 DEITY_NOT_FOUND` | "Không tìm thấy" + quay lại (AC14, ⚠️ câu) | chưa có (rơi về vị đầu tiên) |
| `GET /v1/deities/{id}` | `NoConnection` / `Timeout` / 5xx / `400 INVALID_DATE` / khác | Lỗi + "Thử lại" (AC14, ⚠️ câu) | chưa có |
| `GET /v1/deities/{id}/sutras` | Mọi lỗi | Ẩn "Kinh liên quan", phần còn lại vẫn hiện (AC26) | chưa có |
| (máy) | Không app nào nhận `ACTION_SEND` | Không làm gì, không crash (AC24) | chưa có |

## Out of scope

- Tìm kiếm trong danh mục, "Báo thông tin sai", yêu thích, theo dõi, nhắc ngày vía, màn chia sẻ ba thẻ,
  sửa danh mục (product §1.2).
- Nạp nội dung danh mục (FR-DTY-011 — Backend).
- Lịch lễ (`../Calendar/`): spec này chỉ mở `FestivalsRoute` và dùng lại `CalendarCountdown`.
- Màn bài kinh (`SutraRoute`): spec này chỉ mở nó với `id` của backend. Màn đó hiện chạy trên danh mục
  kinh mẫu (`GetSutraDetailUseCase`, id kiểu `phapcu`/`k-kinh-phap-cu`), nên id backend (vd `pho-mon`)
  chưa mở đúng bài cho tới khi feature kinh có spec và data layer — ngoài spec này.
- Màn Niệm Phật ngoài việc nhận câu niệm (lưu chuỗi hạt, đồng bộ — `MalaViewModel` TODO).
- Bản đọc offline (product §3.6 cho phép, Android **không** làm ở v1.0 — xem `design.md` "Local storage").
- Chip "Chư Thiên" (product Appendix A mục 5 — chưa quyết, không chặn).

## Pending (not yet implemented)

- Domain: model theo contract, repository, use case; bỏ dữ liệu mẫu và quét âm lịch — `tasks.md` Phase 1.
- Khối Trang chủ/Thư viện, danh mục có phân trang + lọc trên server + trạng thái trống, trang một vị theo
  FR-DTY-005 (Hình tượng, thẻ vía từ server, một hàng hành động, bỏ "Báo thông tin sai"), truyền câu niệm
  sang Niệm Phật, chia sẻ thật (AC1–AC29) — Phase 2.
- Data layer cho bốn operation (AC1, AC7–AC14, AC25) — Phase 3; trang 12, `date`, `next_via`,
  `via_events` chờ backend deity v3.0 vào `openapi.yaml`.
- Vector đếm ngược, đo NFR (AC30, AC31) — Phase 4.
- Dọn `Docs/home_assumptions.md` (phần Phật ở #1, #13) — Phase 5.

## Open questions (resolved)

Theo product spec v1.0 (2026-10-15) và backend deity spec v3.0 — các điểm này thay cho hành vi hiện có
của code Android:

- ~~Nhãn "Phật tương lai" (Di Lặc), không có loại Chư Thiên~~ → ba loại, nhãn "Phật" / "Bồ Tát" / "Chư
  Thiên", không chip Chư Thiên (BR-DTY-01) → AC5, AC6, AC9. Resolved 2026-10-15.
- ~~Lọc trên máy, tải cả danh mục~~ → server lọc theo `type`, trang 12, nạp trang kế ở cuối danh sách
  (BR-DTY-03, FR-DTY-002, FR-DTY-003, §3.6) → AC7–AC9. Resolved 2026-10-15.
- ~~Ngày vía cứng + quét âm lịch 400 ngày trên máy~~ → ngày vía là sự kiện lịch lễ, lần vía kế tiếp do
  server tính với `date` của máy; app chỉ đếm ngược (BR-DTY-05, BR-DTY-06; backend deity v3.0) → AC5,
  AC17, AC18, AC28. Resolved 2026-10-15.
- ~~Kinh liên quan khớp theo tên, dự phòng Pháp Cú~~ → `GET /v1/deities/{id}/sutras` (FR-DTY-007) → AC25.
  Resolved 2026-10-15.
- ~~"Báo thông tin sai" ở chân trang~~ → bỏ (BR-DTY-08) → AC27. Resolved 2026-10-15.
- ~~"Trì niệm" không truyền câu niệm, "Chia sẻ" no-op~~ → truyền chú ngắn, số hạt 0; chia sẻ qua bảng
  chia sẻ hệ thống (FR-DTY-008, FR-DTY-009) → AC23, AC24. Resolved 2026-10-15.
- ~~Hình tượng chưa có~~ → giữ phần Hình tượng (FR-DTY-005) → AC20. Resolved 2026-10-15.
- ~~Dữ liệu mẫu khi lỗi / rỗng~~ → trạng thái trống `buddhasEmpty`, không nội dung giả (FR-DTY-010) →
  AC4, AC10. Resolved 2026-10-15.
- ~~Lịch âm & quy tắc ngày 30 / tháng nhuận cho ngày vía (`Docs/home_assumptions.md` #11, đã gỡ trước
  đây bởi `../Calendar/`)~~ → thuộc lịch lễ, server tính (BR-CAL-04, BR-CAL-07). Resolved 2026-10-15.
