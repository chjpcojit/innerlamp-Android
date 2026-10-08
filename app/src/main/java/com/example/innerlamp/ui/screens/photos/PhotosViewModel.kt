package com.example.innerlamp.ui.screens.photos

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.usecase.GetTemplePhotosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho màn "Ảnh chùa đẹp" (`photos`).
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Danh sách ảnh lấy từ [GetTemplePhotosUseCase] (hiện là dữ liệu mẫu trong
 *   domain); việc lọc theo chip là logic trình bày nên nằm ở đây, không đẩy
 *   xuống use case.
 *
 * @param getPhotos use case cung cấp danh sách ảnh chùa.
 */
class PhotosViewModel(
    getPhotos: GetTemplePhotosUseCase = GetTemplePhotosUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotosUiState.from(getPhotos()))
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    /** Chọn một chip lọc; `null` = "Tất cả". */
    fun onCategorySelected(category: PhotoCategory?) {
        _uiState.update { state ->
            if (state.selectedCategory == category) state else state.copy(selectedCategory = category)
        }
    }
}
