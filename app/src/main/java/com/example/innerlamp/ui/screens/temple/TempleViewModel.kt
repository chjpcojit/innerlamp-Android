package com.example.innerlamp.ui.screens.temple

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.usecase.GetTempleDetailUseCase
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.toContentStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel of the "Trang chùa" screen: reads `GET /v1/temples/{id}` once per open, "Thử lại" reads again. /
 * ViewModel màn "Trang chùa": mỗi lần mở đọc `GET /v1/temples/{id}` một lần, "Thử lại" đọc lại.
 *
 * @param savedStateHandle holds `templeId` (property of `TempleRoute`). / Giữ `templeId` của route.
 */
class TempleViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetail: GetTempleDetailUseCase,
) : ViewModel() {

    private val templeId: String = savedStateHandle.get<String>(KEY_TEMPLE_ID).orEmpty()

    private val _uiState = MutableStateFlow(TempleUiState())
    val uiState: StateFlow<TempleUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onRetry() {
        if (_uiState.value.status == ContentStatus.Loading) return
        _uiState.update { it.copy(status = ContentStatus.Loading) }
        load()
    }

    fun toggleIntro() = _uiState.update { it.copy(introExpanded = !it.introExpanded) }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getDetail(templeId).fold(
                onSuccess = { detail -> _uiState.update { it.copy(status = ContentStatus.Loaded, detail = detail) } },
                onFailure = { error -> _uiState.update { it.copy(status = error.toContentStatus(), detail = null) } },
            )
        }
    }

    private companion object {
        /** Route argument name — `TempleRoute.templeId`. / Tên arg route. */
        const val KEY_TEMPLE_ID = "templeId"
    }
}
