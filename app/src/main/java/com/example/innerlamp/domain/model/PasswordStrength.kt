package com.example.innerlamp.domain.model

/**
 * Độ mạnh mật khẩu hiển thị bằng thanh 3 đoạn (prototype màn Đăng ký / Đặt mật khẩu mới).
 * Thuần domain — không phụ thuộc Android/Compose.
 *
 * @param segments số đoạn được tô (0–3); [None] = chưa nhập.
 */
enum class PasswordStrength(val segments: Int, val label: String) {
    None(0, ""),
    Weak(1, "Yếu"),
    Fair(2, "Khá tốt"),
    Strong(3, "Mạnh");

    companion object {
        /** Độ dài tối thiểu để mật khẩu được chấp nhận. */
        const val MIN_LENGTH = 8

        /** Độ dài tối đa backend chấp nhận. */
        const val MAX_LENGTH = 64

        /** Độ dài cộng thêm một điểm. */
        private const val LONG_LENGTH = 12

        /**
         * Quy tắc khớp `pwStrength` của prototype — cộng 1 điểm cho mỗi tiêu chí:
         * ≥ [MIN_LENGTH] ký tự, ≥ [LONG_LENGTH] ký tự, có cả chữ thường lẫn chữ hoa, có số, có ký hiệu.
         * - rỗng → [None]
         * - ≤ 1 điểm → [Weak]; ≤ 3 điểm → [Fair]; còn lại → [Strong]
         */
        fun of(password: String): PasswordStrength {
            if (password.isEmpty()) return None
            val score = listOf(
                password.length >= MIN_LENGTH,
                password.length >= LONG_LENGTH,
                password.any { it.isLowerCase() } && password.any { it.isUpperCase() },
                password.any { it.isDigit() },
                password.any { !it.isLetterOrDigit() },
            ).count { it }
            return when {
                score <= 1 -> Weak
                score <= 3 -> Fair
                else -> Strong
            }
        }
    }
}
