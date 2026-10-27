package com.example.innerlamp.ui.screens.home

import com.example.innerlamp.di.DeityModule
import com.example.innerlamp.di.GalleryModule
import com.example.innerlamp.di.TempleModule
import com.example.innerlamp.domain.usecase.GetFeaturedDeitiesUseCase
import com.example.innerlamp.domain.usecase.GetFeaturedTemplesUseCase
import com.example.innerlamp.domain.usecase.GetHomePhotosUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Loads the home content blocks in parallel. Each block fails on its own: a failed temple / deity call
 * gives an empty list (the block shows its empty card), a failed gallery call gives `null` photos. /
 * Tải song song các khối nội dung trang chủ. Mỗi khối lỗi độc lập: chùa / chư Phật lỗi → danh sách
 * rỗng (khối hiện thẻ rỗng), ảnh lỗi → `null`.
 */
internal class HomeContentLoader(
    private val getTemples: GetFeaturedTemplesUseCase = TempleModule.provideGetFeaturedTemplesUseCase(),
    private val getDeities: GetFeaturedDeitiesUseCase = DeityModule.provideGetFeaturedDeitiesUseCase(),
    private val getPhotos: GetHomePhotosUseCase = GalleryModule.provideGetHomePhotosUseCase(),
) {
    suspend fun load(): HomeSectionsContent = coroutineScope {
        val temples = async { getTemples() }
        val deities = async { getDeities() }
        val photos = async { getPhotos() }
        HomeSectionsContent(
            temples = temples.await().getOrDefault(emptyList()),
            buddhas = deities.await().getOrDefault(emptyList()),
            articles = HomeSampleData.articles,
            photos = photos.await().getOrNull(),
        )
    }
}
