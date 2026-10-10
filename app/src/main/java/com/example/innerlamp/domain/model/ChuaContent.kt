package com.example.innerlamp.domain.model

import java.time.LocalDate

/**
 * Các kiểu dữ liệu cho tab Chùa (`sangha` trong prototype).
 *
 * Chỉ chứa hình dạng thuần Kotlin — không import Android/Compose/Room.
 */

/**
 * Một ngôi chùa trong danh bạ của tab Chùa (chùa tu tập của tôi hoặc chùa gần bạn).
 *
 * Khác với [Temple] (thẻ tóm tắt ở carousel Trang chủ): có thêm trụ trì, giờ mở cửa
 * và khoảng cách để tính trạng thái "Đang mở / Đã đóng" và sắp xếp "Chùa gần bạn".
 */
data class ChuaTemple(
    val id: String,
    val name: String,
    /** Địa chỉ hiển thị, vd `"Bình Thạnh, TP. Hồ Chí Minh"`. */
    val place: String,
    /** Tỉnh/thành — dùng để chống trùng khi đề xuất chùa mới. */
    val province: String,
    /** Trụ trì; `null` = "Đang cập nhật". */
    val abbot: String? = null,
    /** Khoảng cách tới vị trí người dùng (km); `null` = chưa có tọa độ. */
    val distanceKm: Double? = null,
    /** Giờ mở cửa tính bằng phút trong ngày (`0..1440`). */
    val openMinute: Int = DEFAULT_OPEN_MINUTE,
    val closeMinute: Int = DEFAULT_CLOSE_MINUTE,
    /** `false` = chùa người dùng tự đề xuất, đang chờ InnerLamp xác minh. */
    val isVerified: Boolean = true,
) {
    /** Chùa đang mở cửa tại [minuteOfDay] (`0..1439`). */
    fun isOpenAt(minuteOfDay: Int): Boolean = minuteOfDay in openMinute until closeMinute

    companion object {
        const val DEFAULT_OPEN_MINUTE = 4 * 60
        const val DEFAULT_CLOSE_MINUTE = 20 * 60
    }
}

/** Một thông báo của chùa (khối "Thông báo từ chùa"). */
data class ChuaNews(
    /** Thời gian tương đối, vd `"Hôm nay"`. */
    val time: String,
    val text: String,
)

/** Hành động của một sự kiện chùa: nhắc hay giữ chỗ (nút pill bên phải thẻ). */
enum class ChuaEventAction { Remind, Reserve }

/** Một sự kiện/khóa tu sắp tới của chùa tu tập. */
data class ChuaEvent(
    val id: String,
    /** Ngày trong tháng, vd `"08"`. */
    val day: String,
    /** Nhãn tháng, vd `"TH10"`. */
    val month: String,
    val title: String,
    val sub: String,
    val action: ChuaEventAction,
)

/** Một buổi đạo tràng trực tuyến (thiền, tụng kinh livestream). */
data class ChuaOnlineSession(
    val id: String,
    /** Nhãn lịch, vd `"HẰNG NGÀY · 20:00"`. */
    val tag: String,
    val title: String,
    val sub: String,
    /** `true` = chấm vàng (buổi hằng ngày), `false` = chấm xám. */
    val isDaily: Boolean,
)

/** Một khóa tu sắp diễn ra ở các chùa (khối "Khóa tu sắp diễn ra", trạng thái chưa có chùa). */
data class ChuaRetreat(
    val id: String,
    /** Chùa tổ chức — để mở đúng Trang chùa. */
    val templeId: String,
    val day: String,
    val month: String,
    val title: String,
    val sub: String,
)

/**
 * Một ngày lễ/ngày chay sắp tới (khối "Ngày lễ & ngày chay sắp tới").
 *
 * [daysAway] = 0 nghĩa là hôm nay.
 */
data class ChuaFastDay(
    val date: LocalDate,
    val title: String,
    val lunarDay: Int,
    val lunarMonthName: String,
    val daysAway: Int,
    /** `true` = lễ vía (tô màu nhấn), `false` = Mùng Một/Rằm. */
    val isFestival: Boolean,
    /** Gợi ý thực hành, vd `"Gợi ý: ăn chay, tụng Kinh Phổ Môn"`. */
    val suggestion: String,
)
