package com.example.innerlamp.ui.screens.talk

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.TalkDetail
import com.example.innerlamp.domain.usecase.GetTalkDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho màn "Pháp thoại" (`talk`).
 *
 * - Nhận id buổi giảng từ route arg (`TalkRoute.talkId`) qua [SavedStateHandle].
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Nội dung lấy từ [GetTalkDetailUseCase] (hiện là dữ liệu mẫu trong domain).
 *   TODO(il-data-builder): nối `TalkRepository` khi data layer có; lúc đó use case trả
 *   `Result<TalkDetail>` để UI xử lý được lỗi mạng.
 *
 * @author innerlamp
 */
class TalkViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val getDetail = GetTalkDetailUseCase()

    // id buổi giảng từ route; không có (preview/fallback) thì dùng id mẫu.
    private val talkId: String = savedStateHandle.get<String>(KEY_TALK_ID)
        ?: DEFAULT_TALK_ID

    private val detail: TalkDetail = getDetail(talkId)

    private val _uiState = MutableStateFlow(TalkUiState.initial(detail))
    val uiState: StateFlow<TalkUiState> = _uiState.asStateFlow()

    /** Đổi trạng thái phát ↔ tạm dừng (prototype `togglePlay`). */
    fun togglePlay() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    private companion object {
        /** Tên arg route — khớp property `talkId` của `TalkRoute`. */
        const val KEY_TALK_ID = "talkId"

        /** Id mẫu dùng khi không có route arg (preview/fallback). */
        const val DEFAULT_TALK_ID = "tl-hieu-la-dao"
    }
}
