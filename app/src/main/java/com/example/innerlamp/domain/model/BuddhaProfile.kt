package com.example.innerlamp.domain.model

import java.time.LocalDate

/**
 * Các kiểu dữ liệu của màn "Chư Phật & Bồ Tát" (`buddhas`) và "Chi tiết Phật / Bồ Tát" (`buddha`).
 *
 * Pure Kotlin — không phụ thuộc Android/Compose/Room. Khác với [BuddhaFigure] (chỉ là thẻ tóm tắt
 * trong carousel ở Trang chủ), đây là hồ sơ đầy đủ: tiểu sử, đại nguyện, thần chú, kinh liên quan.
 */

/** Nhóm của một vị — quyết định chip lọc ở màn danh sách (prototype `cat`: 1 = Phật, 2 = Bồ Tát). */
enum class BuddhaCategory { Buddha, Bodhisattva }

/** Một câu danh hiệu / tâm chú kèm lời chú thích (khối "Thần chú & danh hiệu"). */
data class BuddhaMantra(
    /** Nhãn in hoa (vd "DANH HIỆU", "TÂM CHÚ"). */
    val label: String,
    /** Câu niệm bằng tiếng Việt / phiên âm. */
    val text: String,
    /** Nguyên văn Sanskrit (nếu có). */
    val sanskrit: String? = null,
    /** Lời chú thích cách dùng. */
    val note: String,
)

/** Tham chiếu tới một bài kinh liên quan để mở sang màn Chi tiết bài kinh. */
data class BuddhaSutraRef(
    /** Id bài kinh trong danh mục kinh (khớp `SutraRoute.sutraId`). */
    val sutraId: String,
    val title: String,
    val sub: String,
)

/** Hồ sơ đầy đủ của một vị Phật / Bồ Tát. */
data class BuddhaProfile(
    val id: String,
    /** Tên đầy đủ (vd "Đức Phật Thích Ca Mâu Ni"). */
    val name: String,
    /** Tên ngắn (vd "Phật Thích Ca") — dùng cho ô ảnh và tiêu đề header khi cuộn. */
    val shortName: String,
    /** Tên Sanskrit / Pāli. */
    val sanskrit: String,
    val category: BuddhaCategory,
    /** Nhãn pill trên ảnh: "Phật", "Phật tương lai", "Bồ Tát". */
    val kindLabel: String,
    /** Cõi / nơi trú (vd "Tây Phương Cực Lạc"). */
    val realm: String,
    /** Mô tả một câu về vai trò. */
    val role: String,
    /** Ngày vía chính (âm lịch). */
    val viaDay: Int,
    val viaMonth: Int,
    val viaName: String,
    /** Ghi chú các ngày vía còn lại (nếu có). */
    val extraVia: String? = null,
    val bio: String,
    val vowIntro: String,
    val vows: List<String>,
    /** Đơn vị đếm đại nguyện (vd "12 đại nguyện", "hạnh nguyện"). */
    val vowUnit: String,
    /** Tổng số đại nguyện khi danh sách `vows` chỉ là phần trích (vd 48 của Phật A Di Đà). */
    val vowTotal: Int? = null,
    val mantras: List<BuddhaMantra>,
    val sutras: List<BuddhaSutraRef>,
) {
    /** Ngày vía dạng "dd/MM" (vd "19/02"). */
    val viaLabel: String get() = "%02d/%02d".format(viaDay, viaMonth)
}

/** Nội dung màn Chi tiết: hồ sơ + lần vía kế tiếp tính từ ngày hiện tại. */
data class BuddhaDetail(
    val profile: BuddhaProfile,
    /** Ngày dương lịch của lần vía kế tiếp; `null` nếu không tìm thấy trong 400 ngày tới. */
    val nextVia: LocalDate?,
)
