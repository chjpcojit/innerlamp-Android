package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.data.fake.FakeContentNetworkClient
import com.example.innerlamp.data.fake.ok
import com.example.innerlamp.data.remote.api.GalleryApiService
import com.example.innerlamp.data.remote.dto.GalleryDetailDto
import com.example.innerlamp.data.remote.dto.GalleryPageDto
import com.example.innerlamp.data.remote.dto.GallerySummaryDto
import com.example.innerlamp.domain.model.ContentError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryRepositoryImplTest {

    private class FakeGalleryApi(private val pages: List<List<GallerySummaryDto>>, private val total: Int) : GalleryApiService {
        val requested = mutableListOf<Int>()

        override suspend fun getList(page: Int, type: String?): ApiResponse<GalleryPageDto> {
            requested += page
            return ok(GalleryPageDto(items = pages[page - 1], page = page, total = total, totalPages = pages.size))
        }

        override suspend fun getDetail(id: String): ApiResponse<GalleryDetailDto> = ok(GalleryDetailDto(id = id, author = "A"))
    }

    private fun photos(vararg ids: String) = ids.map { GallerySummaryDto(id = it, type = "CHUA") }

    @Test
    fun getAll_concatenates_pages_with_stable_positions() = runTest {
        val api = FakeGalleryApi(listOf(photos("a", "b"), photos("c")), total = 3)
        val all = GalleryRepositoryImpl(api, FakeContentNetworkClient()).getAll().getOrThrow()
        assertEquals(listOf(1, 2), api.requested)
        assertEquals(listOf("a", "b", "c"), all.map { it.id })
    }

    @Test
    fun home_photos_take_the_first_ones_and_count_the_rest() = runTest {
        val api = FakeGalleryApi(listOf(photos("a", "b", "c", "d")), total = 12)
        val home = GalleryRepositoryImpl(api, FakeContentNetworkClient()).getHomePhotos(3).getOrThrow()
        assertEquals(listOf("a", "b", "c"), home.photos.map { it.id })
        assertEquals(9, home.more)
    }

    @Test
    fun credit_not_found_is_not_found() = runTest {
        val notFound = AppError.ApiError(code = "GALLERY_IMAGE_NOT_FOUND", message = "", type = "business")
        val repo = GalleryRepositoryImpl(FakeGalleryApi(emptyList(), 0), FakeContentNetworkClient(notFound))
        assertEquals(ContentError.NotFound, repo.getCredit("x").exceptionOrNull())
    }
}
