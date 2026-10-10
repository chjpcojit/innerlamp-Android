package com.example.innerlamp.ui.screens.sutralist

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.SutraLengthFilter
import com.example.innerlamp.domain.model.SutraTagFilter
import com.example.innerlamp.domain.usecase.GetSutraCatalogUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** ViewModel màn "Kinh sách": giữ danh mục và các bộ lọc. */
class SutraListViewModel(
    getSutraCatalog: GetSutraCatalogUseCase = GetSutraCatalogUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SutraListUiState(catalog = getSutraCatalog()))
    val uiState: StateFlow<SutraListUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    fun onTagSelected(tag: SutraTagFilter) = _uiState.update { it.copy(tag = tag) }

    fun onLengthSelected(length: SutraLengthFilter) = _uiState.update { it.copy(length = length) }
}
