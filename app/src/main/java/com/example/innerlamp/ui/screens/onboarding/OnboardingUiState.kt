package com.example.innerlamp.ui.screens.onboarding

/**
 * Ý hướng tu tập người dùng chọn ở bước 1 của onboarding.
 * `label` là chuỗi hiển thị tiếng Việt (khớp prototype `intentLabels`).
 */
enum class PracticeIntent(val label: String) {
    Calm("Tĩnh tâm, giảm lo âu"),
    Sleep("Ngủ ngon"),
    DailyChanting("Niệm Phật hằng ngày"),
    LearnDharma("Học Phật pháp"),
    ChantSutras("Tụng kinh"),
    Mindfulness("Chánh niệm khi làm việc"),
}

/** Một khung giờ chuông tỉnh thức đề xuất ở bước 2. */
data class BellOption(val label: String, val sub: String)

val BellOptions = listOf(
    BellOption("Sáng sớm · 05:30", "Thời khóa công phu khuya"),
    BellOption("Giữa trưa · 12:00", "Một phút trở về hơi thở"),
    BellOption("Trước khi ngủ · 21:30", "Buông thư, hồi hướng"),
)

/** Số bước của onboarding. */
const val OnboardingStepCount = 3

/**
 * Trạng thái render của màn onboarding.
 *
 * - [step] 0..2: giới thiệu → chọn ý hướng → chuông tỉnh thức.
 * - [selectedIntents] / [enabledBells] giữ lựa chọn người dùng (chỉ trong bộ nhớ
 *   cho tới khi tầng data có use case lưu trữ — xem TODO trong ViewModel).
 */
data class OnboardingUiState(
    val step: Int = 0,
    val selectedIntents: Set<PracticeIntent> = emptySet(),
    val enabledBells: Set<Int> = emptySet(),
) {
    val isLastStep: Boolean get() = step == OnboardingStepCount - 1
}
