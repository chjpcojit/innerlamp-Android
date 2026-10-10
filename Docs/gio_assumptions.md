# Sổ giỗ chạp (Gio) — giả định & việc còn mở

Nguồn thiết kế: `Docs/innerlamp-project/project/InnerLamp Prototype.dc.html` (màn `gio`, `gioEmpty`, `gioEdit`; logic `gioNext`/`gioVals`). Code: `ui/screens/gio/`, `domain/calendar/MemorialSchedule.kt`, `domain/model/MemorialEntry.kt`, `domain/usecase/GetMemorialEntriesUseCase.kt`.

| # | Giả định | Lý do | Cách xác thực / bước tiếp |
|---|---|---|---|
| 1 | Danh sách giỗ giữ trong bộ nhớ ViewModel, seed 3 người mẫu từ prototype; thoát app là mất thay đổi | Chưa có tầng dữ liệu cho Sổ giỗ (skill UI dừng ở ranh giới domain) | Thêm `MemorialRepository` + Room entity/DAO + migration; thay `GetMemorialEntriesUseCase` |
| 2 | Lời nhắc chưa được lên lịch: các công tắc "Nhắc trước ngày giỗ", thời điểm, giờ chỉ lưu vào `MemorialEntry` | Chưa có hạ tầng thông báo (WorkManager/AlarmManager, quyền `POST_NOTIFICATIONS`) | Lên lịch theo `MemorialSchedule.next` và lên lịch lại sau mỗi kỳ (ngày dương đổi hằng năm) |
| 3 | "Thêm vào Lời nhắc (Reminders)" chỉ lưu cờ `syncReminders`; thông báo sau khi lưu luôn là "Đã lưu vào sổ giỗ." | Prototype lấy theo app Lời nhắc của iOS; Android không có tương đương chuẩn | Quyết định: bỏ công tắc, hay đồng bộ sang Google Calendar/`CalendarContract` |
| 4 | Ảnh chân dung là avatar chữ viết tắt (họ + tên, vd "TM"); không chọn/kéo ảnh | Prototype dùng `<image-slot>`; app chưa có bộ chọn ảnh/lưu trữ ảnh | Thêm Photo Picker + lưu URI/file nội bộ vào entity |
| 5 | Hàng "Lấy từ gia phả" (khi sổ trống) là no-op | App chưa có màn Gia phả (`IL-GiaPha` mới ở prototype) | Nối `GioCallbacks.onGiaPha` khi có `GiaPhaRoute`; prototype có luồng "Nhắc giỗ" từ gia phả sang Sổ giỗ |
| 6 | Ngày giỗ 30 âm của tháng thiếu rơi vào ngày 29 (cuối tháng); giỗ dương 29/2 năm không nhuận rơi vào 28/2 | Khớp prototype và quy ước `LunarCalendar.matchesLunarDay` | Có test `MemorialScheduleTest` |
| 7 | Chỉ quy đổi sang tháng âm thường (không nhuận); người mất vào tháng nhuận được ghi như tháng thường cùng số | Prototype không có lựa chọn tháng nhuận | Thêm cờ "tháng nhuận" nếu người dùng cần |
| 8 | Số lần giỗ = (năm âm/dương của kỳ giỗ) − năm mất; năm mất nhập theo lịch đã chọn, chọn từ năm nay lùi 96 năm | Khớp `gioNext` của prototype | — |
| 9 | "Hôm nay" chốt khi tạo/ghi sổ, không tự làm mới qua nửa đêm khi app mở sẵn | Cùng giả định #7 của Home | Làm mới `items` khi màn quay lại foreground nếu cần |
| 10 | Form Thêm/sửa dùng chung `GioViewModel` với màn danh sách qua back stack entry của `GioRoute`; sau khi process bị hệ thống thu hồi, form tự đóng (không còn bản nháp) | Tránh truyền nguyên đối tượng qua route | Chuyển sang lưu bản nháp vào `SavedStateHandle` nếu cần giữ |
| 11 | Chọn ngày/tháng/năm dùng `DropdownMenu` thay cho `<select>` | Tương đương gần nhất trong Material3 | Đổi sang bottom sheet/picker nếu cần cuộn mượt hơn với 97 năm |

## Ghi chú build
- Dự án có product flavor (`dev`/`staging`/`prod`): task unit test là `testDevDebugUnitTest`, không phải `testDebugUnitTest` như `CLAUDE.md` ghi.
- `lintDevDebug` có 8 lỗi `ViewModelConstructorInComposable` sẵn có ở Login/Onboarding/Signup/Splash; không có lỗi nào thuộc Sổ giỗ.
