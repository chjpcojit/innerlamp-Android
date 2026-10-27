package com.example.innerlamp.ui.screens.buddha

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.usecase.GetBuddhaDetailUseCase
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.toContentStatus
import java.time.Clock
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel màn "Chi tiết Phật / Bồ Tát": id from `BuddhaRoute.buddhaId`, content from
 * `GET /v1/deities/{id}` via [GetBuddhaDetailUseCase]; "Thử lại" reads again. /
 * Id lấy từ route, nội dung từ `GET /v1/deities/{id}`; "Thử lại" đọc lại.
 *
 * @param clock đồng hồ hệ thống — inject để test cố định "hôm nay".
 */
class BuddhaDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetail: GetBuddhaDetailUseCase,
    clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private val buddhaId: String = savedStateHandle.get<String>(KEY_BUDDHA_ID).orEmpty()

    private val _uiState = MutableStateFlow(BuddhaDetailUiState(today = LocalDate.now(clock)))
    val uiState: StateFlow<BuddhaDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun toggleBio() = _uiState.update { it.copy(bioExpanded = !it.bioExpanded) }

    fun toggleVows() = _uiState.update { it.copy(vowsExpanded = !it.vowsExpanded) }

    fun onRetry() {
        if (_uiState.value.status == ContentStatus.Loading) return
        _uiState.update { it.copy(status = ContentStatus.Loading) }
        load()
    }

    private fun load() {
        viewModelScope.launch {
            getDetail(buddhaId).fold(
                onSuccess = { detail -> _uiState.update { it.copy(status = ContentStatus.Loaded, detail = detail) } },
                onFailure = { error -> _uiState.update { it.copy(status = error.toContentStatus(), detail = null) } },
            )
        }
    }

    private companion object {
        /** Tên arg route — khớp property `buddhaId` của `BuddhaRoute`. */
        const val KEY_BUDDHA_ID = "buddhaId"
    }
}
