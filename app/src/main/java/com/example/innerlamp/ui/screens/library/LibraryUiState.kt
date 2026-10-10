package com.example.innerlamp.ui.screens.library

import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.model.FeaturedTalk
import com.example.innerlamp.domain.model.LibraryCatalog
import com.example.innerlamp.domain.model.LibraryHub
import com.example.innerlamp.domain.model.Sutra

/** State của màn Thư viện (chỉ đọc — màn không có bộ lọc). */
data class LibraryUiState(
    val hubs: List<LibraryHub>,
    val featuredTalk: FeaturedTalk,
    val buddhas: List<BuddhaProfile>,
    val sutras: List<Sutra>,
) {
    companion object {
        fun from(catalog: LibraryCatalog, buddhas: List<BuddhaProfile>): LibraryUiState =
            LibraryUiState(
                hubs = catalog.hubs,
                featuredTalk = catalog.featuredTalk,
                buddhas = buddhas,
                sutras = catalog.sutras,
            )
    }
}
