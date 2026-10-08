package com.example.innerlamp.ui.screens.sangha

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel cho màn Đạo tràng (`sangha`).
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Dữ liệu hiện lấy từ [SanghaSampleData] vì chưa có repository cộng đồng.
 *   TODO(il-data-builder): nối vào use case `GetSanghaFeed` khi data layer có.
 *
 * @author innerlamp
 */
class SanghaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        SanghaUiState(
            meditation = SanghaSampleData.meditation,
            mySangha = SanghaSampleData.mySangha,
            posts = SanghaSampleData.posts,
        ),
    )
    val uiState: StateFlow<SanghaUiState> = _uiState.asStateFlow()
}
