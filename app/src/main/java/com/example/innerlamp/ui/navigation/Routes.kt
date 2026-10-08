package com.example.innerlamp.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Tập trung các route (destination) type-safe của app. Mỗi route là một
 * `@Serializable object`/`data class` implement [IlDestination].
 *
 * Màn hình không có tham số → `object`; có tham số → `data class`
 * (xem hướng dẫn type-safe navigation của Jetpack).
 */

/** Màn splash. Dùng transition mặc định (crossfade). */
@Serializable
object SplashRoute : IlDestination

/**
 * Màn onboarding. Dùng transition mặc định (crossfade).
 *
 * Muốn custom animation thì override `transitions` với preset có sẵn:
 *
 * ```
 * override val transitions: NavTransitionSpec = NavTransitionSpec.SlideHorizontal
 * ```
 *
 * hoặc tự dựng `NavTransitionSpec(...)` (xem doc ở [NavTransitionSpec]).
 */
@Serializable
object OnboardingRoute : IlDestination

/**
 * Màn tạo tài khoản (Signup). Nhận vào từ onboarding qua "Tạo tài khoản"
 * hoặc "Bỏ qua". Dùng transition mặc định (crossfade).
 */
@Serializable
object SignupRoute : IlDestination

/**
 * Màn đăng nhập (Login). Vào từ onboarding/signup qua "Đăng nhập" / "Tôi đã có tài khoản".
 * Dùng transition mặc định (crossfade).
 */
@Serializable
object LoginRoute : IlDestination

/**
 * Trang chủ — tab gốc đầu tiên của bottom nav. Vào từ đăng nhập / "Vào xem trước".
 * Dùng transition mặc định (crossfade).
 */
@Serializable
object HomeRoute : IlDestination

/**
 * Đạo tràng — tab gốc của bottom nav. Vào từ bottom nav (tab "Đạo tràng").
 * Dùng transition mặc định (crossfade).
 */
@Serializable
object SanghaRoute : IlDestination

/**
 * Thư viện — tab gốc thứ hai của bottom nav. Vào từ bottom nav / "Thư viện" trên
 * Trang chủ. Dùng transition mặc định (crossfade).
 *
 * TODO(navigation): khi có `BottomNavShell` (spec Navigation · Phase 3) thì tab này
 * được host trong shell thay vì đăng ký riêng ở `MainActivity`.
 */
@Serializable
object LibraryRoute : IlDestination

/**
 * Sự kiện & khóa học — màn danh sách sự kiện của đạo tràng (3 tab lọc). Vào từ
 * Đạo tràng (chip "Sự kiện & khóa học") hoặc Trang chủ ("Tất cả" ở khối Sự kiện
 * Phật giáo). Dùng transition mặc định (crossfade).
 */
@Serializable
object EventsRoute : IlDestination

/**
 * Chuông mõ — màn thực hành (nhịp mõ + thỉnh chuông). Vào từ Trang chủ (thẻ "Chuông mõ").
 * Dùng transition mặc định (crossfade).
 */
@Serializable
object BellRoute : IlDestination

/**
 * Hồ sơ — tab gốc "Tôi" của bottom nav. Vào từ bottom nav (tab "Tôi").
 * Dùng transition mặc định (crossfade).
 */
@Serializable
object ProfileRoute : IlDestination

/**
 * Lịch âm — màn xem lịch âm theo tháng (carousel cuộn ngang). Vào từ Trang chủ
 * (liên kết "Lịch âm" trên dải tuần) và Nhật ký. Dùng transition mặc định (crossfade).
 */
@Serializable
object CalRoute : IlDestination

/**
 * Ảnh chùa đẹp — màn xem toàn bộ ảnh chùa (lọc theo danh mục + lightbox). Vào từ
 * Trang chủ (khối "Ảnh chùa đẹp"). Dùng transition mặc định (crossfade).
 */
@Serializable
object PhotosRoute : IlDestination

/**
 * Niệm Phật — màn tràng hạt toàn màn hình (kéo hạt đếm 108, vòng vàng). Vào từ
 * Trang chủ (khối "Tràng hạt · Niệm Phật" → "Toàn màn hình"). Dùng transition mặc
 * định (crossfade).
 */
@Serializable
object MalaRoute : IlDestination

/**
 * Cài đặt — màn cài đặt chung (chuông, hiển thị & ngôn ngữ, nội dung, riêng tư).
 * Vào từ Hồ sơ (hàng "Cài đặt"). Dùng transition mặc định (crossfade).
 */
@Serializable
object SettingsRoute : IlDestination

/**
 * Chuẩn bị thiền — màn cấu hình buổi thiền (chọn thời lượng + chuông). Vào từ
 * Trang chủ (thẻ "Thiền hơi thở buổi sáng") và nút đèn ở giữa bottom nav.
 * Dùng transition mặc định (crossfade).
 */
@Serializable
object SetupRoute : IlDestination

/**
 * Bài viết Phật pháp — màn đọc một bài biên tập. Nhận [articleId] để biết bài nào
 * đang mở; vào từ Trang chủ (khối "Bài viết Phật pháp hay") và khối "Đọc tiếp"
 * trong chính màn này (đẩy bài mới lên stack). Dùng transition mặc định (crossfade).
 *
 * Là route có tham số nên không có instance singleton — đăng ký graph bằng
 * `composable<ArticleRoute> { ... }` thay vì `ilComposable(screen)`.
 */
@Serializable
data class ArticleRoute(val articleId: String) : IlDestination

/**
 * Trang chùa — màn chi tiết một ngôi chùa (hero, thống kê, thời khóa, địa chỉ).
 * Nhận [templeId] để biết chùa nào đang mở; vào từ Trang chủ (carousel "Chùa Việt Nam"),
 * Đạo tràng, Hồ sơ và Lịch âm. Dùng transition mặc định (crossfade).
 *
 * Là route có tham số nên không có instance singleton — đăng ký graph bằng
 * `composable<TempleRoute> { ... }` thay vì `ilComposable(screen)`.
 */
@Serializable
data class TempleRoute(val templeId: String) : IlDestination

/**
 * Pháp thoại — màn chi tiết một buổi giảng (hero, trình phát, danh sách phần,
 * CTA ngồi thiền). Nhận [talkId] để biết buổi giảng nào đang mở; vào từ Thư viện
 * (thẻ pháp thoại nổi bật). Dùng transition mặc định (crossfade).
 *
 * Là route có tham số nên không có instance singleton — đăng ký graph bằng
 * `composable<TalkRoute> { ... }` thay vì `ilComposable(screen)`.
 */
@Serializable
data class TalkRoute(val talkId: String) : IlDestination

/**
 * Chi tiết bài kinh — màn giới thiệu một bài kinh (hero, tiến độ đọc, mô tả,
 * mục lục các phẩm, CTA "Tụng theo"). Nhận [sutraId] để biết bài nào đang mở;
 * vào từ Thư viện (hàng "Kinh thường tụng"). Dùng transition mặc định (crossfade).
 *
 * Là route có tham số nên không có instance singleton — đăng ký graph bằng
 * `composable<SutraRoute> { ... }` thay vì `ilComposable(screen)`.
 */
@Serializable
data class SutraRoute(val sutraId: String) : IlDestination

/**
 * Tụng kinh — màn đọc một khổ kinh song ngữ kèm thanh phát dính đáy. Nhận [sutraId]
 * để biết bài kinh nào đang tụng; vào từ "Chi tiết bài kinh" (nút "Tụng theo").
 * Dùng transition mặc định (crossfade).
 *
 * Là route có tham số nên không có instance singleton — đăng ký graph bằng
 * `composable<ReaderRoute> { ... }` thay vì `ilComposable(screen)`.
 */
@Serializable
data class ReaderRoute(val sutraId: String) : IlDestination
