# Changelog — Deity Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.0 (initial) (2026-10-15)

- Spec đầu tiên của danh mục Chư Phật & Bồ Tát cho Android, viết theo kiểu hỗn hợp: hiện trạng đọc từ code
  ngày 2026-10-15 (`ui/screens/buddha/`, `ui/screens/home/`, `ui/screens/library/`, `ui/screens/mala/`,
  `domain/model/BuddhaProfile.kt`, `domain/usecase/GetBuddha*`, `ui/navigation/Routes.kt`,
  `MainActivity.kt`, `Docs/home_assumptions.md` #1, #13 — #11 đã gỡ trước đây), phần còn lại
  prospective, theo product spec innerlamp-be `docs/product/deity/product.md` v1.0 (implements
  BR-DTY-01…08, FR-DTY-001…010, NFR-DTY-01, NFR-DTY-02; FR-DTY-011 là Backend).
- `requirements.md`: 31 tiêu chí EARS; bảng hiện trạng (drift: 9 hồ sơ mẫu, "Phật tương lai", không Chư
  Thiên, lọc trên máy, không phân trang, vía quét âm lịch trên máy, không Hình tượng, mala không nhận câu
  niệm, chia sẻ no-op, "Báo thông tin sai"); Dữ liệu & API, Error → UI.
- `design.md`: online-only (không Room); `DeityRepository` + bốn use case; model `Deity*` thay
  `BuddhaProfile`/`BuddhaFigure`; dùng `CalendarCountdown`/`LunarDay` của `../Calendar/`; `MalaRoute` nhận
  `mantra`; hai Step-by-step Flow (danh mục: trang đầu / trang kế; trang một vị: chi tiết ‖ kinh liên
  quan, hành động, đổi ngày khi resume).
- `tasks.md`: Phase 0 (đã có: route, điều hướng) → Phase 1–5; vector đếm ngược qua `../Calendar/`;
  `deity-order.json`, `deity-next-via.json` không chạy trên Android.
- Product spec / contract: product v1.0; contract innerlamp-be `openapi.yaml` @ a8a016b (info.version
  1.0.0) cho phần đã có; trang 12, `date`, `INVALID_DATE`, `next_via`, `via_events` theo backend deity
  `requirements.md` v3.0 (contract not yet in openapi.yaml, backend Phase 9).
- Hand-offs: backend — đưa deity v3.0 vào `openapi.yaml`; `ba-writer` — câu trống khối Thư viện, câu không
  tìm thấy / lỗi trang một vị, ngày vía tháng nhuận, ngày vía không có lần diễn ra, nhãn loại lạ, chia sẻ
  khi không có chú ngắn, link trong văn bản chia sẻ (Appendix A mục 3), thứ tự kinh liên quan (FR-DTY-007
  vs `deitiesSutras`), dòng phụ của kinh, lỗi riêng của kinh liên quan.
- Reason: product spec deity v1.0 (2026-10-15) yêu cầu spec Android `Docs/Specs/Deity/`.
