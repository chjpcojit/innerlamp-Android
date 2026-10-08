package com.example.innerlamp.domain.model

/**
 * Cấu hình buổi thiền ở màn "Chuẩn bị thiền" (prototype `setup`).
 * Pure Kotlin — không phụ thuộc Android.
 *
 * Chỉ chứa danh mục thời lượng cố định (prototype `durVals`). Mức thời lượng và
 * cấu hình chuông do màn hình chọn rồi truyền sang phiên thiền (`session`);
 * việc lưu cấu hình để giữ giữa các lần mở màn là của `il-data-builder`.
 */
object MeditationSetup {

    /** Các mức thời lượng cố định (phút) — prototype `durVals = ['5','12','20','30','45']`. */
    val durationOptions: List<Int> = listOf(5, 12, 20, 30, 45)

    /** Mức mặc định (20 phút) — prototype `dur: 2`. */
    const val defaultDurationIndex: Int = 2

    /** Nhãn thời lượng cho nút "Bắt đầu", vd "20 phút". */
    fun durationLabel(index: Int): String = "${durationOptions[index]} phút"
}
