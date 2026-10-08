package com.example.innerlamp.ui.screens.sutra

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.SutraDetail
import com.example.innerlamp.domain.usecase.GetSutraDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel cho màn "Chi tiết bài kinh" (`detail`).
 *
 * - Nhận id bài kinh từ route arg (`SutraRoute.sutraId`) qua [SavedStateHandle].
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Nội dung lấy từ [GetSutraDetailUseCase] (hiện là dữ liệu mẫu trong domain).
 *   TODO(il-data-builder): nối `SutraRepository` khi data layer có; lúc đó use case trả
 *   `Result<SutraDetail>` để UI xử lý được lỗi mạng.
 *
 * @author innerlamp
 */
class SutraViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val getDetail = GetSutraDetailUseCase()

    // id bài kinh từ route; không có (preview/fallback) thì dùng id mẫu.
    private val sutraId: String = savedStateHandle.get<String>(KEY_SUTRA_ID)
        ?: DEFAULT_SUTRA_ID

    private val detail: SutraDetail = getDetail(sutraId)

    private val _uiState = MutableStateFlow(SutraUiState.initial(detail))
    val uiState: StateFlow<SutraUiState> = _uiState.asStateFlow()

    private companion object {
        /** Tên arg route — khớp property `sutraId` của `SutraRoute`. */
        const val KEY_SUTRA_ID = "sutraId"

        /** Id mẫu dùng khi không có route arg (preview/fallback). */
        const val DEFAULT_SUTRA_ID = "k-kinh-phap-cu"
    }
}
