package com.example.innerlamp.domain.model

import java.time.LocalDate

/**
 * Types of the "Chi tiết Phật / Bồ Tát" (`buddha`) screen, mapped from `GET /v1/deities/{id}` (contract
 * 3.0.0). Pure Kotlin. /
 * Các kiểu của màn "Chi tiết Phật / Bồ Tát", map từ `GET /v1/deities/{id}`. Thuần Kotlin.
 */

/** Một câu danh hiệu / tâm chú kèm lời chú thích (khối "Thần chú & danh hiệu"). */
data class BuddhaMantra(
    /** Nhãn in hoa (vd "TÂM CHÚ", "ĐÀ LA NI"). */
    val label: String,
    /** Câu niệm bằng tiếng Việt / phiên âm. */
    val text: String,
    /** Nguyên văn Sanskrit (nếu có). */
    val sanskrit: String? = null,
    /** Lời chú thích / đoạn trích. */
    val note: String,
)

/** Tham chiếu tới một bài kinh liên quan để mở sang màn Chi tiết bài kinh. */
data class BuddhaSutraRef(
    /** Id bài kinh trong danh mục kinh (khớp `SutraRoute.sutraId`). */
    val sutraId: String,
    val title: String,
    val sub: String,
)

/** One lunar commemoration day (`lunar_memorial_days[]`). / Một ngày vía âm lịch. */
data class BuddhaViaDay(
    val day: Int,
    val month: Int,
    val name: String,
) {
    /** "dd/MM", e.g. "19/02". */
    val label: String get() = "%02d/%02d".format(day, month)
}

/** Full profile of one deity. / Hồ sơ đầy đủ của một vị Phật / Bồ Tát. */
data class BuddhaProfile(
    val id: String,
    /** Honorific title (`title_honorific`, else `name_vi`). / Danh hiệu tôn xưng. */
    val name: String,
    /** `name_vi` — header title while scrolling. / Tên tiếng Việt. */
    val shortName: String,
    val sanskrit: String?,
    val type: DeityType,
    /** Hero pill ("Phật", "Bồ Tát", "Thiên"); `null` when the type is unknown. / Nhãn pill. */
    val kindLabel: String?,
    /** `pure_land`. / Cõi / nơi trú. */
    val realm: String?,
    /** `core_virtue`. / Hạnh nguyện cốt lõi. */
    val role: String?,
    val imageUrl: String?,
    /** Lunar days in contract order; the first one is the main via. / Ngày vía, vị đầu là ngày chính. */
    val viaDays: List<BuddhaViaDay>,
    /** `biography_summary`. */
    val bio: String?,
    /** `canonical_source`. / Kinh điển gốc. */
    val canonicalSource: String?,
    val vows: List<String>,
    val mantras: List<BuddhaMantra>,
    val sutras: List<BuddhaSutraRef>,
) {
    val mainVia: BuddhaViaDay? get() = viaDays.firstOrNull()
}

/** Nội dung màn Chi tiết: hồ sơ + lần vía kế tiếp tính từ ngày hiện tại. */
data class BuddhaDetail(
    val profile: BuddhaProfile,
    /** Ngày dương lịch của lần vía kế tiếp; `null` nếu không có ngày vía / không tìm thấy trong 400 ngày tới. */
    val nextVia: LocalDate?,
)
