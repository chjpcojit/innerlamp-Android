package com.example.innerlamp.domain.model

/**
 * Độ mạnh mật khẩu hiển thị bằng thanh 3 đoạn (prototype màn Đăng ký).
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

        /** Độ dài từ đó (kèm ≥3 nhóm ký tự) mật khẩu tính là mạnh. */
        private const val STRONG_LENGTH = 12

        /**
         * Quy tắc:
         * - rỗng → [None]; ngắn hơn [MIN_LENGTH] → [Weak]
         * - đếm nhóm ký tự: chữ thường, chữ hoa, số, ký hiệu
         * - ≥ [STRONG_LENGTH] ký tự và ≥ 3 nhóm → [Strong]
         * - ≥ 2 nhóm → [Fair]; còn lại → [Weak]
         */
        fun of(password: String): PasswordStrength {
            if (password.isEmpty()) return None
            if (password.length < MIN_LENGTH) return Weak
            val groups = listOf(
                password.any { it.isLowerCase() },
                password.any { it.isUpperCase() },
                password.any { it.isDigit() },
                password.any { !it.isLetterOrDigit() },
            ).count { it }
            return when {
                password.length >= STRONG_LENGTH && groups >= 3 -> Strong
                groups >= 2 -> Fair
                else -> Weak
            }
        }
    }
}
