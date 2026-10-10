package com.example.innerlamp.ui.screens.library

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.usecase.GetBuddhaProfilesUseCase
import com.example.innerlamp.domain.usecase.GetLibraryCatalogUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel màn Thư viện: ghép catalog kinh/pháp thoại với danh sách chư Phật & Bồ Tát
 * (dữ liệu mẫu trong domain).
 */
class LibraryViewModel(
    getCatalog: GetLibraryCatalogUseCase = GetLibraryCatalogUseCase(),
    getBuddhas: GetBuddhaProfilesUseCase = GetBuddhaProfilesUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState.from(getCatalog(), getBuddhas()))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
}
