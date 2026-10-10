package com.example.innerlamp.domain.model

/**
 * Chế độ chuông mõ (màn `bell` trong prototype). Pure Kotlin — không phụ thuộc Android.
 *
 * @author innerlamp
 */
enum class BellMode(val label: String) {
    /** Chỉ gõ mõ. */
    Mo("Mõ"),

    /** Chỉ thỉnh chuông. */
    Chuong("Chuông"),

    /** Gõ mõ + tự động thỉnh chuông sau mỗi N tiếng mõ. */
    MoVaChuong("Mõ + chuông"),
}

/**
 * Một preset chuông mõ (Tụng kinh / Niệm Phật / ...). `bpm` áp cho chế độ gõ mõ,
 * `gapSeconds` áp cho chế độ chuông tự động, `everyIndex` trỏ vào [BellEveryOptions.all].
 *
 * @author innerlamp
 */
data class BellPreset(
    val label: String,
    val mode: BellMode,
    val bpm: Int? = null,
    val gapSeconds: Int? = null,
    val everyIndex: Int? = null,
)

/** 5 preset của prototype `Component.B_PRESETS`. */
object BellPresets {
    val all: List<BellPreset> = listOf(
        BellPreset(label = "Tụng kinh", mode = BellMode.MoVaChuong, bpm = 72, everyIndex = 2),
        BellPreset(label = "Niệm Phật", mode = BellMode.MoVaChuong, bpm = 96, everyIndex = 3),
        BellPreset(label = "Lạy sám hối", mode = BellMode.Chuong, gapSeconds = 12),
        BellPreset(label = "Ngồi thiền", mode = BellMode.Chuong, gapSeconds = 60),
        BellPreset(label = "Chỉ gõ mõ", mode = BellMode.Mo, bpm = 60),
    )
}

/** Mốc "thỉnh chuông sau mỗi N tiếng mõ" (0 = Tắt). Khớp `Component.B_EVERY`. */
object BellEveryOptions {
    val all: List<Int> = listOf(0, 10, 27, 108)

    /** Nhãn hiển thị của một mốc (0 → "Tắt"). */
    fun label(value: Int): String = if (value == 0) "Tắt" else value.toString()
}

/** Giới hạn nhịp / khoảng cách của nút − / + (prototype `bRate`). */
object BellLimits {
    const val BpmMin = 40
    const val BpmMax = 160
    const val BpmStep = 4
    const val GapMinSeconds = 5
    const val GapMaxSeconds = 120
    const val GapStepSeconds = 5
}

/**
 * Nhịp "dồn mõ" kiểu tụng kinh (prototype `B_ROLL`): khoan thai → nhanh dần → dồn →
 * chậm lại → hai tiếng ngắt, rồi kết bằng một tiếng chuông.
 */
object BellRoll {
    /** Khoảng nghỉ (ms) sau mỗi tiếng mõ trong chuỗi dồn. */
    val gapsMillis: List<Long> = buildList {
        var x = 950.0
        while (x > 170) { add(Math.round(x)); x *= 0.84 }
        repeat(14) { add(160L) }
        x = 180.0
        while (x < 720) { add(Math.round(x)); x *= 1.28 }
        add(950L)
        add(1100L)
    }

    /** Thời lượng cú đập mõ tối đa (ms) và hệ số theo khoảng nghỉ, để búa kịp trở về. */
    const val MoStrikeMaxMillis = 260
    const val MoStrikeGapRatio = 0.85

    /** Thời lượng hoạt ảnh cú gõ mõ trong chuỗi dồn ứng với khoảng nghỉ [gapMillis]. */
    fun strikeMillis(gapMillis: Long): Int =
        minOf(MoStrikeMaxMillis.toDouble(), gapMillis * MoStrikeGapRatio).toInt()
}

/**
 * Khoảng cách giữa hai nhịp, tính bằng millis. Chế độ Chuông dùng `gapSeconds`;
 * các chế độ còn lại dùng `bpm` (nhịp/phút).
 */
fun BellMode.intervalMillis(bpm: Int, gapSeconds: Int): Long =
    if (this == BellMode.Chuong) gapSeconds * 1000L else 60_000L / bpm
