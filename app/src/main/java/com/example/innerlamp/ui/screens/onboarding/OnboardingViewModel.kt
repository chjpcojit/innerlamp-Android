package com.example.innerlamp.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel màn onboarding. Chỉ giữ trạng thái 3 bước + lựa chọn của người dùng.
 *
 * TODO(il-data-builder): khi tầng data có `CompleteOnboardingUseCase` (lưu ý hướng
 * + chuông tỉnh thức + đánh dấu hoàn tất onboarding vào `UserPreferences`/DataStore),
 * hãy inject use case đó vào đây và gọi khi người dùng nhấn "Tạo tài khoản".
 * Hiện tại các lựa chọn chỉ nằm trong bộ nhớ, chưa được lưu.
 */
class OnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /** Sang bước kế tiếp; ở bước cuối thì không làm gì (màn quyết định hoàn tất). */
    fun next() {
        _uiState.update { s ->
            if (s.step < OnboardingStepCount - 1) s.copy(step = s.step + 1) else s
        }
    }

    /** Quay lại bước trước; ở bước đầu thì không làm gì. */
    fun back() {
        _uiState.update { s ->
            if (s.step > 0) s.copy(step = s.step - 1) else s
        }
    }

    /** Bật/tắt một ý hướng (bước 1, chọn nhiều). */
    fun toggleIntent(intent: PracticeIntent) {
        _uiState.update { s ->
            val next = s.selectedIntents.toMutableSet()
            if (!next.add(intent)) next.remove(intent)
            s.copy(selectedIntents = next)
        }
    }

    /** Bật/tắt một khung giờ chuông (bước 2). */
    fun toggleBell(index: Int) {
        _uiState.update { s ->
            val next = s.enabledBells.toMutableSet()
            if (!next.add(index)) next.remove(index)
            s.copy(enabledBells = next)
        }
    }
}
