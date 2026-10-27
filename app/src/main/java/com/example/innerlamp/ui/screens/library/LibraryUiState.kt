package com.example.innerlamp.ui.screens.library

import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.model.FeaturedTalk
import com.example.innerlamp.domain.model.LibraryCatalog
import com.example.innerlamp.domain.model.LibraryHub
import com.example.innerlamp.domain.model.Sutra
import com.example.innerlamp.ui.components.ContentStatus

/**
 * State của màn Thư viện (chỉ đọc — màn không có bộ lọc). The "Chư Phật & Bồ Tát" strip comes from
 * `GET /v1/deities/top` with its own [buddhasStatus]; talks and sutras are still sample data. /
 * Dải "Chư Phật & Bồ Tát" lấy từ `GET /v1/deities/top`, có trạng thái tải riêng; pháp thoại và kinh vẫn
 * là dữ liệu mẫu.
 */
data class LibraryUiState(
    val hubs: List<LibraryHub>,
    val featuredTalk: FeaturedTalk,
    val buddhas: List<DeitySummary>,
    val sutras: List<Sutra>,
    val buddhasStatus: ContentStatus = ContentStatus.Loaded,
) {
    companion object {
        fun from(
            catalog: LibraryCatalog,
            buddhas: List<DeitySummary>,
            buddhasStatus: ContentStatus = ContentStatus.Loaded,
        ): LibraryUiState =
            LibraryUiState(
                hubs = catalog.hubs,
                featuredTalk = catalog.featuredTalk,
                buddhas = buddhas,
                sutras = catalog.sutras,
                buddhasStatus = buddhasStatus,
            )
    }
}
