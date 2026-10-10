package com.example.innerlamp.domain.model

/**
 * Quy tắc tràng hạt 108 — màn "Niệm Phật" (`mala`) trong prototype.
 * Pure Kotlin, không phụ thuộc Android.
 *
 * Tràng hạt là trạng thái thực hành **local-first** (README "State Management"): hiện
 * chỉ giữ trong ViewModel, chưa lưu Room hay đồng bộ — việc đó thuộc `il-data-builder`.
 */
object Mala {
    /** Số hạt một tràng (chuỗi 108). */
    const val BEADS_PER_ROUND = 108

    /** Các mốc "biến" người dùng chọn cho một vòng niệm (prototype `mlTargets`). */
    val TARGETS: List<Int> = listOf(21, 54, 108)

    /** Cứ 27 hạt là một hạt mẫu (hạt lớn màu vàng đánh dấu 1/4 tràng). */
    const val MOTHER_BEAD_EVERY = 27

    /** Một lần niệm: +1; chạm 108 thì vòng về hạt 1 của tràng mới. */
    fun advance(beads: Int): Int = if (beads >= BEADS_PER_ROUND) 1 else beads + 1

    /** Số biến hiển thị trong vòng hiện tại (1..[target]); 0 khi chưa niệm. */
    fun count(beads: Int, target: Int): Int =
        if (beads > 0 && beads % target == 0) target else beads % target

    /** Tiến độ 0..1 của tràng hiện tại (dùng cho vòng conic-gold / thanh tiến trình). */
    fun progress(beads: Int): Float = (beads.toFloat() / BEADS_PER_ROUND).coerceIn(0f, 1f)

    /** Hạt ở vị trí [position] trong chuỗi có phải hạt mẫu không (mỗi 27 hạt). */
    fun isMotherBead(position: Int): Boolean =
        ((position % MOTHER_BEAD_EVERY) + MOTHER_BEAD_EVERY) % MOTHER_BEAD_EVERY == 0
}
