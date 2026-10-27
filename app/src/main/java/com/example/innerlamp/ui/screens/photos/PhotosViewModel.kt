package com.example.innerlamp.ui.screens.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.model.TemplePhoto
import com.example.innerlamp.domain.usecase.GetPhotoCreditUseCase
import com.example.innerlamp.domain.usecase.GetTemplePhotosUseCase
import com.example.innerlamp.ui.components.ContentStatus
import com.example.innerlamp.ui.components.toContentStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel of "Ảnh đẹp": loads the gallery once, filters by chip on the device, and fetches the credit
 * of the photo opened in the lightbox (lists do not carry it). /
 * ViewModel màn "Ảnh đẹp": tải thư viện ảnh một lần, lọc theo chip trên máy, và lấy ghi công của ảnh
 * đang mở trong lightbox (danh sách không có ghi công).
 */
class PhotosViewModel(
    private val getPhotos: GetTemplePhotosUseCase,
    private val getCredit: GetPhotoCreditUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotosUiState())
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    /** Credit request of the open photo; cancelled when another photo opens. / Request ghi công đang chạy. */
    private var creditJob: Job? = null

    init {
        load()
    }

    /** Chọn một chip lọc; `null` = "Tất cả". */
    fun onCategorySelected(category: PhotoCategory?) {
        _uiState.update { state ->
            if (state.selectedCategory == category) state else state.copy(selectedCategory = category)
        }
    }

    /** "Thử lại" after a failed load. / "Thử lại" sau khi tải lỗi. */
    fun onRetry() {
        if (_uiState.value.status == ContentStatus.Loading) return
        _uiState.update { it.copy(status = ContentStatus.Loading) }
        load()
    }

    /** Opens the lightbox and fetches the credit. / Mở lightbox và lấy ghi công. */
    fun onPhotoOpened(photo: TemplePhoto) {
        creditJob?.cancel()
        _uiState.update { it.copy(viewing = photo, credit = null) }
        creditJob = viewModelScope.launch {
            // A failed credit only hides the credit line; the photo stays open. /
            // Lỗi ghi công chỉ ẩn dòng ghi công; ảnh vẫn mở.
            getCredit(photo.id).onSuccess { credit ->
                _uiState.update { if (it.viewing?.id == photo.id) it.copy(credit = credit) else it }
            }
        }
    }

    fun onLightboxDismissed() {
        creditJob?.cancel()
        _uiState.update { it.copy(viewing = null, credit = null) }
    }

    private fun load() {
        viewModelScope.launch {
            getPhotos().fold(
                onSuccess = { photos -> _uiState.update { it.copy(photos = photos, status = ContentStatus.Loaded) } },
                onFailure = { error -> _uiState.update { it.copy(status = error.toContentStatus()) } },
            )
        }
    }
}
