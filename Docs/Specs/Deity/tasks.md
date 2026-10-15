# Tasks — Deity

Version: 1.0
Last updated: 2026-10-15
Status: planned (drift) — chỉ route và điều hướng giữa các màn đã đúng spec; mọi việc khác `[ ]`

> See `changelog.md` in this folder for the version history of this spec.

Mỗi mục trace về một dòng của bảng **Layers & components** hoặc một Step trong `design.md`.
`[x]` chỉ khi đã kiểm tra tồn tại **và đúng như product spec v1.0 yêu cầu** trong code ngày
2026-10-15; mục có code nhưng lệch product để `[ ]` và ghi "(có code, lệch: …)".
Paths are relative to `app/src/main/java/com/example/innerlamp/`.

## Phase 0 — Đã có, giữ nguyên

- [x] `BuddhaListRoute`, `BuddhaRoute(buddhaId)` trong `ui/navigation/Routes.kt`; wiring Trang chủ ("Xem
      tất cả", thẻ), Thư viện ("Xem tất cả", thẻ, ô hub `LibraryHub.Buddhas`), danh mục → trang một vị
      trong `MainActivity.kt` (AC1–AC3, phần điều hướng; id mẫu sẽ thành id contract ở Phase 3)
- [x] "Lịch lễ" trên trang một vị → `FestivalsRoute` (`MainActivity.kt`, AC17 phần điều hướng)

## Phase 1 — Domain

- [ ] `domain/model/Deity.kt`: `DeityType` (gồm `Deva`, `Unknown`), `DeitySummary`, `DeityVia`,
      `DeityViaEvent`, `DeityIconography`, `DeityMantras`, `DeityDetail`, `DeityPage`, `RelatedSutra`
      (design.md); `LunarDay` dùng chung với `../Calendar/` (tạo nếu chưa có)
- [ ] `domain/repository/DeityRepository.kt` + `DeityNotFoundException` (AC14)
- [ ] `domain/usecase/GetFeaturedDeitiesUseCase.kt`, `GetDeityPageUseCase.kt`, `GetDeityDetailUseCase.kt`,
      `GetRelatedSutrasUseCase.kt` — `Result`, `DispatcherProvider`, `today` từ `Clock` (AC28)
- [ ] Xác nhận `domain/calendar/CalendarCountdown.kt` đã có từ `../Calendar/` Phase 1 trước khi làm thẻ
      vía (AC17) — không tạo bản riêng
- [ ] Xoá `domain/model/BuddhaProfile.kt`, `BuddhaFigure` (`domain/model/HomeContent.kt`),
      `domain/usecase/GetBuddhaProfilesUseCase.kt`, `GetBuddhaDetailUseCase.kt` khi Phase 2 không còn dùng
      (có code, lệch: 9 hồ sơ mẫu, quét âm lịch 400 ngày — drift BR-DTY-05/06)

## Phase 2 — UI (`il-ui-builder`)

- [ ] `ui/screens/buddha/DeityFormat.kt`: nhãn loại BR-DTY-01 ("Chư Thiên"; `Unknown` → không nhãn —
      AC5, AC6), "Vía dd/MM âm lịch" (⚠️ tháng nhuận), "THÁNG GIÊNG/n/CHẠP", ngày dương + thứ, nhãn đếm
      ngược từ `CalendarCountdown`, "Xem đủ N đại nguyện" / "Thu gọn", văn bản chia sẻ (AC24)
- [ ] Khối Trang chủ: `HomeViewModel` đọc `GetFeaturedDeitiesUseCase`, trạng thái riêng của khối, "Thử
      lại" chỉ tải lại khối; thẻ theo AC5 (ảnh Coil + giữ chỗ, `nameVi`, vía từ `nextVia`); bỏ `buddhas`
      khỏi `HomeSampleData` (AC1, AC4) (có code, lệch: dữ liệu mẫu, thẻ rỗng chung 3 khối, "Phật tương
      lai")
- [ ] Khối Thư viện: `LibraryViewModel`/`LibraryUiState` đọc `GetFeaturedDeitiesUseCase` async; đang tải /
      có / trống-lỗi + "Thử lại" trong `BuddhaStrip` (AC2, AC4; ⚠️ câu trống) (có code, lệch: đồng bộ từ
      dữ liệu mẫu, không trạng thái trống)
- [ ] Danh mục: viết lại `BuddhaListUiState` + `BuddhaListViewModel` — phân trang (AC7, AC8, AC12),
      chip qua server + `collectLatest` (AC9), `SavedStateHandle` (AC13) (có code, lệch: lọc trên máy,
      phụ đề đếm cả mẫu, không phân trang)
- [ ] `BuddhaListScreen`: `LazyVerticalGrid` 2 cột nạp trang kế, phụ đề N từ `total`, thẻ có
      `coreVirtue` + vía, trạng thái `buddhasEmpty` ("Chưa có thông tin chư Phật", gợi ý, "Thử lại", "Xem
      lịch lễ vía" → `FestivalsRoute`; phụ đề "Chưa có dữ liệu"; ẩn/giữ chip theo AC10/AC11), "Thử lại"
      cuối danh sách (AC5, AC7–AC12)
- [ ] Trang một vị: viết lại `BuddhaDetailUiState`/`BuddhaDetailViewModel` — tải chi tiết ‖ kinh liên
      quan (Step 1, 2), trạng thái tải / không tìm thấy / lỗi (AC14; ⚠️ câu), `onResume()` đổi ngày
      (Step 4, AC29); factory qua `di/DeityModule` (có code, lệch: dữ liệu mẫu đồng bộ, rơi về vị đầu)
- [ ] `BuddhaDetailScreen` — bìa + tên (thêm `titleHonorific`, tên chữ Hán; header dùng `nameVi`), dòng
      `coreVirtue` + `canonicalSource` (AC15); tiểu sử chỉ hiện "Đọc thêm" khi tràn (AC16); bỏ `vowIntro`
      mẫu (có code, lệch)
- [ ] Thẻ vía từ `nextVia` + đếm ngược `CalendarCountdown` + danh sách `viaEvents` (AC17, AC18; ⚠️ ngày vía
      không có lần diễn ra) (có code, lệch: một ngày vía mẫu + `extraVia`, đếm ngược tự tính)
- [ ] Đại nguyện: nhãn "Xem đủ N đại nguyện" cố định, đánh số từ 1, ẩn khi rỗng (AC19) (có code, lệch:
      nhãn theo `vowUnit` mẫu, nhãn đếm "· trích")
- [ ] Phần **Hình tượng** mới: tư thế, toà ngồi / linh thú, pháp khí, ý nghĩa; ẩn dòng/phần thiếu (AC20)
- [ ] "Thần chú & danh hiệu": ba thẻ Danh hiệu / Thần chú / Đà-la-ni, bỏ `note`, ẩn thẻ thiếu; **một** hàng
      nút trong thẻ Thần chú, "Trì niệm" chỉ khi có chú ngắn (AC21, AC22; ⚠️ Chia sẻ khi không có chú ngắn)
      (có code, lệch: hai nút trên mỗi thẻ mẫu)
- [ ] "Kinh liên quan" từ `sutras` theo thứ tự nhận được, dòng = tên + `collection` (⚠️ dòng phụ), ẩn khi
      rỗng hoặc lỗi (AC25, AC26) (có code, lệch: `BuddhaSutraRef` mẫu, dự phòng `phapcu`)
- [ ] Chân trang chỉ "Nội dung do Ban biên tập tổng hợp từ kinh điển" (AC27) (có code, lệch: "· Báo thông
      tin sai" — drift BR-DTY-08)
- [ ] Niệm Phật nhận câu niệm: `MalaRoute` → `data class MalaRoute(val mantra: String? = null)` +
      `composable<MalaRoute>`; `MalaViewModel` đọc `SavedStateHandle`, `beads = 0`; Trang chủ truyền `null`;
      `onChant` trong `MainActivity.kt` truyền chú ngắn Hán Việt (AC23) (có code, lệch: không truyền — TODO
      trong `MainActivity.kt`)
- [ ] `ui/screens/buddha/DeityShare.kt` (`ACTION_SEND` + chooser, bắt `ActivityNotFoundException`); nối
      `onShare` trong `MainActivity.kt` (AC24; ⚠️ link) (có code, lệch: no-op)
- [ ] `onSutra(id)` → `SutraRoute(id)` với id contract (AC25)

## Phase 3 — Data (`il-data-builder`, contract-gated)

- [ ] Xác nhận từng operation trong innerlamp-be `docs/specs/api/openapi.yaml` (cite, không chép schema):
      `deitiesTop`, `deitiesList` (`page`, `type`), `deitiesGet`, `deitiesSutras` có @ a8a016b; `date`,
      `INVALID_DATE`, trang 12, `next_via`, `via_events` **chưa** — theo backend deity requirements.md v3.0
      (⚠️ contract not yet in openapi.yaml, backend Phase 9 — "Deliberately stubbed")
- [ ] `data/remote/api/DeityApiService.kt`: `getTop`, `getList`, `getDetail`, `getSutras` (không
      `@Authenticated`)
- [ ] `data/remote/dto/DeityDto.kt` (phần v3.0 nullable/mặc định rỗng; không khai báo
      `lunar_memorial_days`) + DTO tối thiểu cho mục kinh liên quan
- [ ] `data/mapper/DeityMapper.kt`: `type` lạ → `Unknown`, `""` → `null`, `solar_date` → `LocalDate`,
      `DeityType` → tham số `type`
- [ ] `data/repository/DeityRepositoryImpl.kt` qua `NetworkClient.safeCall`; `date = today`;
      `DEITY_NOT_FOUND` → `DeityNotFoundException`; `getRelatedSutras` gộp mọi trang; các `AppError` khác
      theo bảng Error → UI
- [ ] `di/DeityModule.kt`: repository, use case, factory ViewModel, nguồn khối Phật cho Home/Library
- [ ] Khi backend deity v3.0 vào `openapi.yaml`: cập nhật dòng `Contract:` của `requirements.md`, bỏ
      marker ⚠️ (contract) ở `design.md`, kiểm lại AC5, AC7, AC8, AC17, AC18, AC28

## Phase 4 — Tests

- [ ] Vector đếm ngược: `CalendarProductVectorsTest` của `../Calendar/` (chạy mọi case
      `docs/product/calendar/rules/countdown.json`, BR-CAL-13, qua `scripts/sync-product-vectors.sh` dùng
      chung) phải xanh trước khi bật thẻ vía; spec này không chép thêm bản vector nào (AC31).
      `deity-order.json`, `deity-next-via.json` không chạy trên Android (server chọn, product §4.1)
- [ ] `DeityFormat`: nhãn ba loại + `Unknown`, "Vía dd/MM âm lịch", tháng Giêng/Chạp, đếm ngược hôm nay /
      ngày mai / còn n ngày, nhãn đại nguyện, văn bản chia sẻ có / không Sanskrit
- [ ] Mapper: `""` → `null`, `type` lạ, `next_via` `null`, ngày không đọc được, `via_events` rỗng
- [ ] Repository: success, 404 `DEITY_NOT_FOUND`, `NoConnection`, 5xx, `400 INVALID_DATE`; kinh liên quan
      nhiều trang, lỗi giữa chừng; `date` được gửi đúng ngày `Clock`
- [ ] `BuddhaListViewModel`: trang đầu, nạp trang kế một request, dừng ở trang cuối, đổi chip huỷ request
      cũ và về trang 1, `total = 0` ("Tất cả" / chip), lỗi trang đầu, lỗi trang sau giữ danh sách,
      `SavedStateHandle` (AC7–AC13)
- [ ] `BuddhaDetailViewModel`: có / 404 / lỗi; kinh liên quan lỗi không chặn trang; đếm ngược dùng đúng
      `today` đã gửi; resume cùng ngày không tải lại, khác ngày tải lại (AC14, AC17, AC25, AC26, AC29)
- [ ] `HomeViewModel` + `LibraryViewModel`: khối Phật có / trống / lỗi + "Thử lại" (AC1, AC2, AC4)
- [ ] `MalaViewModel`: có `mantra` → câu niệm đó, 0 hạt; không có → `DefaultMantra` (AC23)
- [ ] Bỏ `GetBuddhaDetailUseCaseTest` (test quét âm lịch — hành vi bị bỏ theo BR-DTY-05)
- [ ] Đo AC30 (NFR-DTY-02) thủ công trên máy thật, 4G
- [ ] Unit test task: `./gradlew testDevDebugUnitTest` (người dùng chạy — xem `AGENTS.md`)

## Phase 5 — Documentation

- [ ] Báo cột Android của innerlamp-be `docs/product/deity/product.md` §4.2 cho `ba-writer` (spec này
      không sửa repo backend)
- [ ] Gỡ phần Phật trong `Docs/home_assumptions.md` #1 (ảnh tượng Phật — thay bằng trỏ về spec này) và
      #13 (khối Phật); dòng #11 đã gỡ trước đây bởi `../Calendar/`
- [ ] OKF concept qua `il-okf-writer` khi feature đã kiểm end-to-end

## Deferred (not blocking Deity)

- Bản đọc offline (product §3.6 "may") — chỉ khi product owner muốn.
- Chip "Chư Thiên" nếu product quyết (Appendix A mục 5).
- Mở đúng bài kinh theo id backend — spec/data layer của feature kinh.
- Lưu chuỗi hạt Niệm Phật giữa các phiên — feature thực hành.
