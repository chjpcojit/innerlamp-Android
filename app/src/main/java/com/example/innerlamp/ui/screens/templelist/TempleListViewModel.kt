package com.example.innerlamp.ui.screens.templelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.usecase.GetTempleDirectoryUseCase
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.toContentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel of "Tất cả chùa": loads the directory once, search and region filter in [TempleListUiState]. /
 * ViewModel màn "Tất cả chùa": tải danh bạ một lần, tìm kiếm và lọc miền ở [TempleListUiState].
 */
class TempleListViewModel(
    private val getDirectory: GetTempleDirectoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TempleListUiState())
    val uiState: StateFlow<TempleListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    /** Chọn một chip miền; `null` = "Tất cả". */
    fun onRegionSelected(region: String?) = _uiState.update { it.copy(region = region) }

    /** "Thử lại" after a failed load. / "Thử lại" sau khi tải lỗi. */
    fun onRetry() {
        if (_uiState.value.status == ContentStatus.Loading) return
        _uiState.update { it.copy(status = ContentStatus.Loading) }
        load()
    }

    private fun load() {
        viewModelScope.launch {
            getDirectory().fold(
                onSuccess = { temples -> _uiState.update { it.copy(temples = temples, status = ContentStatus.Loaded) } },
                onFailure = { error -> _uiState.update { it.copy(status = error.toContentStatus()) } },
            )
        }
    }
}
