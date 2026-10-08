package com.example.innerlamp.domain.model

/**
 * Một nhịp thở hoàn chỉnh (vd 4-7-8: hít 4s, giữ 7s, thở ra 8s).
 * Pure Kotlin — không phụ thuộc Android.
 */
data class BreathingPattern(
    val id: String,
    val name: String,
    val description: String,
    val phases: List<BreathingPhase>,
    val defaultDurationSeconds: Int,
) {
    /** Tổng thời gian một chu kỳ thở, tính bằng giây. */
    val cycleSeconds: Int get() = phases.sumOf { it.durationSeconds }
}

data class BreathingPhase(
    val kind: PhaseKind,
    val durationSeconds: Int,
)

enum class PhaseKind {
    Inhale,
    HoldIn,
    Exhale,
    HoldOut,
}
