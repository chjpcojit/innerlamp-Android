package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.mapper.toCredit
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.GalleryApiService
import com.example.innerlamp.domain.model.HomePhotos
import com.example.innerlamp.domain.model.PhotoCredit
import com.example.innerlamp.domain.model.TemplePhoto
import com.example.innerlamp.domain.repository.GalleryRepository

/** [GalleryRepository] over the public gallery routes; online only, no cache. / Chỉ online, không cache. */
class GalleryRepositoryImpl(
    private val api: GalleryApiService,
    private val networkClient: NetworkClient,
) : GalleryRepository {

    override suspend fun getAll(): Result<List<TemplePhoto>> =
        networkClient.loadAllPages(
            call = { page -> api.getList(page) },
            items = { it.items },
            totalPages = { it.totalPages },
        ).map { items -> items.mapIndexed { index, dto -> dto.toDomain(index) } }

    override suspend fun getHomePhotos(count: Int): Result<HomePhotos> =
        networkClient.loadContent(call = { api.getList(page = 1) }) { page ->
            HomePhotos(
                photos = page.items.take(count).mapIndexed { index, dto -> dto.toDomain(index) },
                total = maxOf(page.total, page.items.size),
            )
        }

    override suspend fun getCredit(id: String): Result<PhotoCredit> =
        networkClient.loadContent(notFoundCode = GALLERY_IMAGE_NOT_FOUND, call = { api.getDetail(id) }, map = { it.toCredit() })

    private companion object {
        const val GALLERY_IMAGE_NOT_FOUND = "GALLERY_IMAGE_NOT_FOUND"
    }
}
