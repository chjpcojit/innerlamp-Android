package com.example.innerlamp.ui.screens.buddha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.DeityType
import com.example.innerlamp.domain.usecase.GetDeityCatalogUseCase
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.toContentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel màn "Chư Phật & Bồ Tát": loads the catalogue from `GET /v1/deities` once; chip filtering is
 * presentation logic in [BuddhaListUiState]. /
 * Tải danh mục từ `GET /v1/deities` một lần; lọc theo chip nằm ở [BuddhaListUiState].
 */
class BuddhaListViewModel(
    private val getCatalog: GetDeityCatalogUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuddhaListUiState())
    val uiState: StateFlow<BuddhaListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Chọn một chip lọc; `null` = "Tất cả". */
    fun onTypeSelected(type: DeityType?) {
        _uiState.update { if (it.type == type) it else it.copy(type = type) }
    }

    fun onRetry() {
        if (_uiState.value.status == ContentStatus.Loading) return
        _uiState.update { it.copy(status = ContentStatus.Loading) }
        load()
    }

    private fun load() {
        viewModelScope.launch {
            getCatalog().fold(
                onSuccess = { list -> _uiState.update { it.copy(deities = list, status = ContentStatus.Loaded) } },
                onFailure = { error -> _uiState.update { it.copy(status = error.toContentStatus()) } },
            )
        }
    }
}
