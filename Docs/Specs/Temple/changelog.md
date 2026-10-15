# Changelog — Temple Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.0 (initial) (2026-10-15)

- Spec đầu tiên của danh bạ chùa cho Android, viết theo kiểu hỗn hợp: hiện trạng đọc từ code ngày
  2026-10-15 (`ui/screens/home/`, `templelist/`, `temple/`, `domain/usecase/GetTemples*`,
  `TempleOption*`), phần còn lại prospective, theo product spec innerlamp-be
  `docs/product/temple/product.md` v1.1 (implements BR-TPL-01…09, FR-TPL-001…009, NFR-TPL-01, NFR-TPL-02;
  FR-TPL-005 qua `../Onboarding/`; FR-TPL-010 là Backend).
- `requirements.md`: 36 tiêu chí EARS; bảng hiện trạng (drift: dữ liệu mẫu, "Tây Nguyên", tìm/lọc trên
  máy, không phân trang, Theo dõi / Cúng dường, bố cục trang chùa cũ); Dữ liệu & API, Error → UI.
- `design.md`: online-only (không Room); `TempleRepository` mới bên cạnh `TempleOptionRepository`;
  `domain/temple/` cho BR-TPL-07, BR-TPL-09; port `LocationProvider` + `core/location/` dùng
  `LocationManager`; hai Step-by-step Flow (danh bạ: trang đầu / trang kế; trang chùa: tải, giờ, quyền vị
  trí, khoảng cách).
- `tasks.md`: Phase 0 (đã có: danh sách rút gọn, route) → Phase 1–5; chạy vector
  `temple-open-status.json`, `temple-distance.json`.
- Product spec / contract: product v1.1; contract innerlamp-be `openapi.yaml` @ 0eb7d1f (info.version
  1.0.0) — thiếu `q`/`region`/trang 12 và trường mới chi tiết.
- Hand-offs: backend — `q`, `region`, `INVALID_FILTER`, trang 12, `holiday_operating_hours`,
  `sangha_body`, `contacts`, `content_updated_month`, dạng giờ `HH:MM - HH:MM` vào `openapi.yaml`
  (Appendix B mục 1, 2, 4; backend v2.0 AC 5, 39–46); `ba-writer` — nhãn mã type/sect/heritage rank, câu
  chữ lỗi/không tìm thấy/gợi ý, lỗi khối nổi bật, ô "Kinh sách", dòng Rằm khi không có giờ Rằm.
- Reason: product spec temple v1.1 (2026-10-15) yêu cầu spec Android `Docs/Specs/Temple/`.
