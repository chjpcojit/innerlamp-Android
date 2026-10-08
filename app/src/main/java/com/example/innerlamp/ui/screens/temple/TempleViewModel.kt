package com.example.innerlamp.ui.screens.temple

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.TempleDetail
import com.example.innerlamp.domain.usecase.GetTempleDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho màn "Trang chùa" (`temple`).
 *
 * - Nhận id chùa từ route arg (`TempleRoute.templeId`) qua [SavedStateHandle].
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Nội dung lấy từ [GetTempleDetailUseCase] (hiện là dữ liệu mẫu trong domain).
 *   TODO(il-data-builder): nối `TempleRepository` khi data layer có; lúc đó use case trả
 *   `Result<TempleDetail>` để UI xử lý được lỗi mạng.
 *
 * @author innerlamp
 */
class TempleViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val getDetail = GetTempleDetailUseCase()

    // id chùa từ route; không có (preview/fallback) thì dùng id mẫu.
    private val templeId: String = savedStateHandle.get<String>(KEY_TEMPLE_ID)
        ?: DEFAULT_TEMPLE_ID

    private val detail: TempleDetail = getDetail(templeId)

    private val _uiState = MutableStateFlow(TempleUiState(detail = detail, isFollowing = false))
    val uiState: StateFlow<TempleUiState> = _uiState.asStateFlow()

    /** Đổi trạng thái "Theo dõi" ↔ "Đang theo dõi". */
    fun toggleFollow() {
        _uiState.update { it.copy(isFollowing = !it.isFollowing) }
    }

    private companion object {
        /** Tên arg route — khớp property `templeId` của `TempleRoute`. */
        const val KEY_TEMPLE_ID = "templeId"

        /** Id mẫu dùng khi không có route arg (preview/fallback). */
        const val DEFAULT_TEMPLE_ID = "tu-quang"
    }
}
