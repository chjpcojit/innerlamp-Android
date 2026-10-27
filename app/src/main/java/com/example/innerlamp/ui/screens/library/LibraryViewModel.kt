package com.example.innerlamp.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.usecase.GetFeaturedDeitiesUseCase
import com.example.innerlamp.domain.usecase.GetLibraryCatalogUseCase
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.toContentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel màn Thư viện: ghép catalog kinh/pháp thoại (dữ liệu mẫu) với chư Phật & Bồ Tát nổi bật từ
 * `GET /v1/deities/top`. / Joins the sample talk/sutra catalog with the featured deities from the API.
 */
class LibraryViewModel(
    getCatalog: GetLibraryCatalogUseCase = GetLibraryCatalogUseCase(),
    private val getFeaturedDeities: GetFeaturedDeitiesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LibraryUiState.from(getCatalog(), buddhas = emptyList(), buddhasStatus = ContentStatus.Loading),
    )
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadBuddhas()
    }

    /** "Thử lại" on the deity strip. / "Thử lại" ở dải chư Phật. */
    fun onRetryBuddhas() {
        if (_uiState.value.buddhasStatus == ContentStatus.Loading) return
        _uiState.update { it.copy(buddhasStatus = ContentStatus.Loading) }
        loadBuddhas()
    }

    private fun loadBuddhas() {
        viewModelScope.launch {
            getFeaturedDeities().fold(
                onSuccess = { list -> _uiState.update { it.copy(buddhas = list, buddhasStatus = ContentStatus.Loaded) } },
                onFailure = { error -> _uiState.update { it.copy(buddhas = emptyList(), buddhasStatus = error.toContentStatus()) } },
            )
        }
    }
}
