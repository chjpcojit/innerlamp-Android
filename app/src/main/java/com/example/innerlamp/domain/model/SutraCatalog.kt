package com.example.innerlamp.domain.model

import java.text.Normalizer

/**
 * Các kiểu dữ liệu của màn "Kinh sách" (`sutras`) — danh mục toàn bộ bản kinh có thể lọc.
 *
 * Pure Kotlin — không phụ thuộc Android/Compose.
 */

/** Tông phái của bản kinh. */
enum class SutraSect(val label: String) {
    Bac("Bắc tông"),
    Nam("Nam tông"),
}

/** Một bản kinh trong danh mục. */
data class SutraCatalogItem(
    val id: String,
    val name: String,
    /** Tên gốc / nguyên văn (vd "佛說阿彌陀經 · Sukhāvatīvyūha"). */
    val original: String,
    /** Người dịch / bản dịch (vd "HT Thích Trí Tịnh"). */
    val translator: String,
    /** Thời lượng tụng, tính bằng phút. */
    val minutes: Int,
    /** Nhãn chủ đề (vd "Hằng ngày", "Cầu an", "Cầu siêu", "Trì chú"). */
    val tags: List<String>,
    val sect: SutraSect,
    val hasAudio: Boolean,
    val isDownloaded: Boolean,
    val description: String,
)

/** Bộ lọc chủ đề / tông phái ở hàng chip thứ nhất (prototype `TAGS`). */
enum class SutraTagFilter(val label: String) {
    All("Tất cả"),
    Daily("Hằng ngày"),
    Peace("Cầu an"),
    Memorial("Cầu siêu"),
    Mantra("Trì chú"),
    Bac("Bắc tông"),
    Nam("Nam tông");

    fun matches(item: SutraCatalogItem): Boolean = when (this) {
        All -> true
        Daily, Peace, Memorial, Mantra -> label in item.tags
        Bac -> item.sect == SutraSect.Bac
        Nam -> item.sect == SutraSect.Nam
    }
}

/** Bộ lọc độ dài ở hàng chip thứ hai (prototype `LENS`). */
enum class SutraLengthFilter(val label: String) {
    Any("Mọi độ dài"),
    Short("Dưới 15 phút"),
    Medium("15 – 30 phút"),
    Long("Trên 30 phút");

    fun matches(minutes: Int): Boolean = when (this) {
        Any -> true
        Short -> minutes < 15
        Medium -> minutes in 15..30
        Long -> minutes > 30
    }
}

/** Bỏ dấu tiếng Việt + hạ chữ thường, để tìm "có dấu hay không dấu". */
fun String.stripVietnamese(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace('đ', 'd')
        .replace('Đ', 'D')
        .lowercase()

/** Thời lượng hiển thị: "~22 phút", "2 giờ 10 phút" (prototype `dur`). */
fun formatSutraDuration(minutes: Int): String =
    if (minutes >= 60) {
        val h = minutes / 60
        val m = minutes % 60
        if (m == 0) "$h giờ" else "$h giờ $m phút"
    } else {
        "~$minutes phút"
    }
