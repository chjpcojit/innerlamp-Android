package com.example.innerlamp.ui.screens.library

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.LibraryCategory
import com.example.innerlamp.domain.usecase.GetLibraryCatalogUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel màn Thư viện.
 *
 * Catalog lấy từ [GetLibraryCatalogUseCase] (hiện là dữ liệu mẫu trong domain);
 * việc lọc theo chip là logic trình bày nên nằm ở đây, không đẩy xuống use case.
 *
 * @param getCatalog use case cung cấp danh mục kinh/pháp thoại.
 */
class LibraryViewModel(
    getCatalog: GetLibraryCatalogUseCase = GetLibraryCatalogUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState.from(getCatalog()))
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    /** Chọn một chip lọc; `null` = "Tất cả". */
    fun onCategorySelected(category: LibraryCategory?) {
        _uiState.update { state ->
            if (state.selectedCategory == category) state else state.copy(selectedCategory = category)
        }
    }
}
