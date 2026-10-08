package com.example.innerlamp.ui.screens.setup

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.MeditationSetup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho màn "Chuẩn bị thiền" (`setup`).
 *
 * State thuần cục bộ (giống màn Cài đặt / Chuông mõ): chỉ giữ mức thời lượng
 * đang chọn, chưa cần repository/data layer. TODO(il-data-builder): nối vào use
 * case đọc/ghi cấu hình buổi thiền (thời lượng, chuông mở đầu/giữa giờ, nền âm
 * thanh) khi data layer có.
 */
class SetupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    /** Chọn mức thời lượng (chỉ số vào [MeditationSetup.durationOptions]). */
    fun onDurationSelect(index: Int) {
        val clamped = index.coerceIn(MeditationSetup.durationOptions.indices)
        _uiState.update { it.copy(durationIndex = clamped) }
    }
}
