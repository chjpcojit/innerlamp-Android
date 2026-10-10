package com.example.innerlamp.ui.screens.buddha

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.usecase.GetBuddhaDetailUseCase
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel màn "Chi tiết Phật / Bồ Tát".
 *
 * Nhận id từ route arg (`BuddhaRoute.buddhaId`) qua [SavedStateHandle]; nội dung lấy từ
 * [GetBuddhaDetailUseCase] (dữ liệu mẫu trong domain, xem TODO(il-data-builder) ở đó).
 *
 * @param clock đồng hồ hệ thống — inject để test cố định "hôm nay".
 */
class BuddhaDetailViewModel(
    savedStateHandle: SavedStateHandle,
    getDetail: GetBuddhaDetailUseCase = GetBuddhaDetailUseCase(),
    clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private val buddhaId: String = savedStateHandle.get<String>(KEY_BUDDHA_ID).orEmpty()

    private val _uiState = MutableStateFlow(
        BuddhaDetailUiState(detail = getDetail(buddhaId), today = LocalDate.now(clock)),
    )
    val uiState: StateFlow<BuddhaDetailUiState> = _uiState.asStateFlow()

    fun toggleBio() = _uiState.update { it.copy(bioExpanded = !it.bioExpanded) }

    fun toggleVows() = _uiState.update { it.copy(vowsExpanded = !it.vowsExpanded) }

    private companion object {
        /** Tên arg route — khớp property `buddhaId` của `BuddhaRoute`. */
        const val KEY_BUDDHA_ID = "buddhaId"
    }
}
