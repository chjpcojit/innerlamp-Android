package com.example.innerlamp.domain.model

/**
 * Một buổi thiền đã hoàn thành (hoặc bị bỏ dở).
 *
 * [startedAtEpochMillis] dùng wall clock để hiển thị lịch sử; mọi phép đo
 * *thời lượng* phải dùng elapsed time (xem `core/util/ElapsedClock`), không
 * dùng wall clock vì user đổi giờ/timezone sẽ làm sai kết quả.
 */
data class Session(
    val id: String,
    val patternId: String,
    val startedAtEpochMillis: Long,
    val durationSeconds: Int,
    val completedCycles: Int,
    val completed: Boolean,
)
