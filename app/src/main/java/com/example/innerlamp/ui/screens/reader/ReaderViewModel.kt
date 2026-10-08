package com.example.innerlamp.ui.screens.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.SutraReader
import com.example.innerlamp.domain.usecase.GetSutraReaderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho màn "Tụng kinh" (`reader`).
 *
 * - Nhận id bài kinh từ route arg (`ReaderRoute.sutraId`) qua [SavedStateHandle].
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Nội dung lấy từ [GetSutraReaderUseCase] (hiện là dữ liệu mẫu trong domain).
 *   TODO(il-data-builder): nối `SutraRepository` khi data layer có; lúc đó use case trả
 *   `Result<SutraReader>` để UI xử lý được lỗi mạng.
 *
 * @author innerlamp
 */
class ReaderViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val getReader = GetSutraReaderUseCase()

    // id bài kinh từ route; không có (preview/fallback) thì dùng id mẫu.
    private val sutraId: String = savedStateHandle.get<String>(KEY_SUTRA_ID)
        ?: DEFAULT_SUTRA_ID

    private val reader: SutraReader = getReader(sutraId)

    private val _uiState = MutableStateFlow(ReaderUiState.initial(reader))
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    /** Đổi trạng thái phát ↔ tạm dừng (prototype `togglePlay`). */
    fun togglePlay() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    private companion object {
        /** Tên arg route — khớp property `sutraId` của `ReaderRoute`. */
        const val KEY_SUTRA_ID = "sutraId"

        /** Id mẫu dùng khi không có route arg (preview/fallback). */
        const val DEFAULT_SUTRA_ID = "k-kinh-phap-cu"
    }
}
