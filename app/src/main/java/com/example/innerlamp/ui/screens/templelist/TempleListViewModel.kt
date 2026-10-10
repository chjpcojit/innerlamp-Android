package com.example.innerlamp.ui.screens.templelist

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.usecase.GetTemplesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** ViewModel màn "Tất cả chùa": danh bạ từ [GetTemplesUseCase], tìm kiếm và lọc miền ở [TempleListUiState]. */
class TempleListViewModel(
    getTemples: GetTemplesUseCase = GetTemplesUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(TempleListUiState(temples = getTemples()))
    val uiState: StateFlow<TempleListUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    /** Chọn một chip miền; `null` = "Tất cả". */
    fun onRegionSelected(region: String?) = _uiState.update { it.copy(region = region) }
}
