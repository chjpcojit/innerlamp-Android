# Trang chủ (Home) — giả định & việc còn mở

Nguồn thiết kế: `Docs/InnerLamp icon consistency/InnerLamp Prototype.dc.html` (màn `home`, bản mới có link "Sổ giỗ" và trạng thái rỗng). Code: `ui/screens/home/`, `domain/calendar/`.

| # | Giả định | Lý do | Cách xác thực / bước tiếp |
|---|---|---|---|
| 1 | Ảnh (tượng Phật, thumbnail bài viết) là gradient placeholder. Ảnh chùa: xem `Docs/Specs/Temple/` | Prototype dùng `<image-slot>`, chưa có ảnh thật và app chưa có thư viện tải ảnh | Thêm Coil + asset/API, thay `PlaceholderImage` và nền hero |
| 2 | Số liệu thực hành (12/20 phút, chuỗi 42 ngày, 7 thanh tuần) và 27 hạt ban đầu là dữ liệu mẫu từ prototype | Chưa có repository thực hành | Nối Room/`SessionRepository`; tràng hạt nên ghi local-first |
| 3 | Bài viết, câu kinh lấy từ `HomeSampleData`. Khối chùa nổi bật: xem `Docs/Specs/Temple/` (FR-TPL-001) | Chưa có backend | Thay bằng repository khi có API |
| 4 | Tên trong lời chào mặc định "quý Phật tử"; avatar chữ "A" | Chưa có nguồn pháp danh (prefs/auth) | Đọc từ hồ sơ người dùng, truyền vào `HomeUiState.create(displayName=…)` |
| 6 | Bottom nav (Thư viện, Đèn, Đạo tràng, Tôi) và mọi callback đích của Home là no-op | Các route này chưa tồn tại | Nối vào `HomeCallbacks` khi có route |
| 7 | Ngày "hôm nay" chốt lúc tạo ViewModel, không tự cập nhật qua nửa đêm (riêng khối Sự kiện Phật giáo: theo `Docs/Specs/Calendar/`, tính lại khi quay lại màn) | Đủ cho bản đầu | Thêm quan sát đổi ngày nếu app hay mở qua đêm |
| 8 | Âm lịch dùng múi giờ cố định GMT+7 | Thuật toán Hồ Ngọc Đức cho lịch Việt Nam; port từ prototype | Test đối chiếu Tết 2024/2025/2026, tháng nhuận 2023 |
| 9 | Carousel sự kiện đồng bộ chiều cao theo thẻ cao nhất đã đo | `LazyRow` không stretch như flex | Thẻ chưa cuộn tới có thể làm chiều cao tăng nhẹ khi xuất hiện |
| 10 | `IlBottomNav` giữ nguyên đệm đáy 30dp (không cộng thêm inset thanh điều hướng) | Component dùng chung, đã có từ trước | Kiểm tra trên máy dùng nút 3 phím |
| 12 | Link "Sổ giỗ" trên dải tuần là no-op (`HomeCallbacks.onGio`) | Màn Sổ giỗ chạp (`gio`) có trong prototype mới nhưng app chưa có route | Thêm `GioRoute` + màn hình, nối vào `MainActivity` |
| 13 | 2 khối Phật/bài viết (khối chùa: xem `Docs/Specs/Temple/`): skeleton khi tải → dữ liệu; rỗng hoặc lỗi → thẻ rỗng. Lỗi mạng và "không có dữ liệu" hiện chung một thẻ rỗng. Nguồn hiện là `HomeSampleData.load()` giả lập trễ 900ms | Chưa có API; prototype chỉ có cờ `homeEmpty` | Thay `HomeSampleData::load` (truyền vào `HomeViewModel.contentLoader` ở `HomeScreen`) bằng repository thật; cân nhắc tách thông điệp lỗi mạng với rỗng |
| 14 | Thẻ Tràng hạt trên Home chỉ là lối tắt vào màn Niệm Phật (cả thẻ + "Toàn màn hình" đều `onMala`); bỏ lần hạt và "Bắt đầu lại" tại Home. Số hạt hiển thị (27) vẫn là dữ liệu mẫu, **không** đồng bộ với `MalaViewModel` | Prototype mới (`goMalaKeep`): beads dùng chung giữa Home và Niệm Phật; Android chưa có repository thực hành để chia sẻ số hạt | Khi có repository thực hành, cấp số hạt cho cả `HomeViewModel` và `MalaViewModel`; phân biệt "giữ số hạt" (thẻ) với "về 0" ("Toàn màn hình") |
| 15 | "Tìm chùa" ở Home: prototype mới mở route `findtp` (màn riêng, reset form), trước đó là `sangha`; `HomeCallbacks.onFindTemple` vẫn no-op | App chưa có route/màn `findtp` | Thêm `FindTempleRoute` + màn hình, nối vào `MainActivity` |
