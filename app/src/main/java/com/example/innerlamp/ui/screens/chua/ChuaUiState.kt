package com.example.innerlamp.ui.screens.chua

import com.example.innerlamp.domain.model.ChuaEvent
import com.example.innerlamp.domain.model.ChuaFastDay
import com.example.innerlamp.domain.model.ChuaNews
import com.example.innerlamp.domain.model.ChuaOnlineSession
import com.example.innerlamp.domain.model.ChuaRetreat
import com.example.innerlamp.domain.model.ChuaTemple
import com.example.innerlamp.ui.screens.templelist.foldVietnamese
import java.time.LocalDate
import java.util.Locale

/** Ô nhập của form "Đề xuất thêm chùa" đang bị lỗi (viền đỏ). */
enum class ChuaProposalField { Name, Province }

/** Form "Đề xuất thêm chùa" — chọn ngay chùa vừa đề xuất, chờ InnerLamp xác minh. */
data class ChuaProposal(
    val name: String = "",
    val address: String = "",
    val province: String = "",
    val contact: String = "",
    val error: String = "",
    val errorField: ChuaProposalField? = null,
)

/**
 * State render cho tab Chùa. Hai trạng thái theo [myTemple]:
 * - có chùa tu tập → thẻ chùa + thông báo/sự kiện/pháp thoại/công đức;
 * - chưa có chùa → tìm & chọn chùa + đạo tràng trực tuyến + khóa tu.
 * Cả hai đều có khối "Ngày lễ & ngày chay sắp tới" ([fastDays]).
 *
 * @property minuteOfDay phút trong ngày hiện tại — để tính "Đang mở / Đã đóng".
 * @property previousTemple chùa đang bị thay khi bấm "Đổi chùa" (để hủy đổi).
 */
data class ChuaUiState(
    val directory: List<ChuaTemple>,
    val myTemple: ChuaTemple?,
    val previousTemple: ChuaTemple? = null,
    val minuteOfDay: Int,
    val query: String = "",
    val proposal: ChuaProposal? = null,
    val news: List<ChuaNews>,
    val events: List<ChuaEvent>,
    val reservedEvents: Set<String> = emptySet(),
    val remindedEvents: Set<String> = emptySet(),
    val online: List<ChuaOnlineSession>,
    val retreats: List<ChuaRetreat>,
    val fastDays: List<ChuaFastDay>,
    val remindedDays: Set<LocalDate> = emptySet(),
    val peerCount: Int = 0,
) {
    /** Danh sách "Chùa gần bạn" / "Kết quả tìm kiếm" (khớp tên hoặc địa danh, bỏ dấu). */
    val nearby: List<ChuaTemple>
        get() {
            val q = query.trim().foldVietnamese()
            val pool = directory.filter { it.isVerified }
            return if (q.isEmpty()) {
                pool.filter { it.distanceKm != null }.sortedBy { it.distanceKm }.take(NEARBY_COUNT)
            } else {
                pool.filter { "${it.name} ${it.place}".foldVietnamese().contains(q) }
                    .sortedBy { it.distanceKm ?: Double.MAX_VALUE }
                    .take(SEARCH_COUNT)
            }
        }

    val isSearching: Boolean get() = query.isNotBlank()

    val listTitle: String get() = if (isSearching) "Kết quả tìm kiếm" else "Chùa gần bạn"
    val listSub: String get() = if (isSearching) "${nearby.size} chùa" else "Quận 1, TP. Hồ Chí Minh"

    val emptyMessage: String get() = "Không tìm thấy chùa nào khớp “${query.trim()}”."

    /** Thầy phụ trách pháp thoại: lấy 2 từ cuối tên trụ trì, hoặc "Chư tăng …". */
    val teacher: String
        get() {
            val t = myTemple ?: return ""
            val abbot = t.abbot
            return if (abbot != null) {
                "Thầy " + abbot.split(' ').takeLast(2).joinToString(" ")
            } else {
                "Chư tăng " + t.name.removePrefix("Chùa ")
            }
        }

    /** Trạng thái hiển thị trên thẻ chùa, vd `"Đang mở · đến 20:00"`. */
    val statusLabel: String
        get() {
            val t = myTemple ?: return ""
            return when {
                !t.isVerified -> "Chờ xác minh"
                t.isOpenAt(minuteOfDay) -> "Đang mở · đến ${formatMinute(t.closeMinute)}"
                else -> "Đã đóng · mở ${formatMinute(t.openMinute)}"
            }
        }

    val isOpenNow: Boolean get() = myTemple?.let { it.isVerified && it.isOpenAt(minuteOfDay) } ?: false

    val peersLabel: String
        get() {
            val t = myTemple ?: return ""
            val count = String.format(Locale.forLanguageTag("vi-VN"), "%,d", peerCount)
            return "$count Phật tử chọn ${t.name} làm chùa tu tập · 23 người sẽ về dự khóa tu tới"
        }

    /** Chùa đã xác minh mới có thông báo/sự kiện/pháp thoại (prototype `chVerified`). */
    val isVerified: Boolean get() = myTemple?.isVerified == true

    private fun formatMinute(m: Int): String = "${m / 60}:${(m % 60).toString().padStart(2, '0')}"

    companion object {
        private const val NEARBY_COUNT = 4
        private const val SEARCH_COUNT = 20
    }
}
