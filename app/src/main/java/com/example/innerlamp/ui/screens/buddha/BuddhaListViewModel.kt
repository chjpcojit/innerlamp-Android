package com.example.innerlamp.ui.screens.buddha

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.BuddhaCategory
import com.example.innerlamp.domain.usecase.GetBuddhaProfilesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel màn "Chư Phật & Bồ Tát".
 *
 * Danh sách lấy từ [GetBuddhaProfilesUseCase] (hiện là dữ liệu mẫu trong domain); việc lọc theo chip
 * là logic trình bày nên nằm ở [BuddhaListUiState].
 * TODO(il-data-builder): thêm trạng thái tải / rỗng / lỗi (prototype `buddhasEmpty`) khi use case
 * chuyển sang gọi API.
 */
class BuddhaListViewModel(
    getProfiles: GetBuddhaProfilesUseCase = GetBuddhaProfilesUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuddhaListUiState(profiles = getProfiles()))
    val uiState: StateFlow<BuddhaListUiState> = _uiState.asStateFlow()

    /** Chọn một chip lọc; `null` = "Tất cả". */
    fun onCategorySelected(category: BuddhaCategory?) {
        _uiState.update { if (it.category == category) it else it.copy(category = category) }
    }
}
