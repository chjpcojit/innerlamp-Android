package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.data.fake.FakeContentNetworkClient
import com.example.innerlamp.data.fake.ok
import com.example.innerlamp.domain.model.ContentError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentCallsTest {

    private data class Page(val items: List<Int>, val totalPages: Int)

    @Test
    fun loadAllPages_reads_every_page_up_to_total_pages() = runTest {
        val requested = mutableListOf<Int>()
        val result = FakeContentNetworkClient().loadAllPages(
            call = { page -> requested += page; ok(Page(listOf(page * 10, page * 10 + 1), totalPages = 3)) },
            items = { it.items },
            totalPages = { it.totalPages },
        )
        assertEquals(listOf(1, 2, 3), requested)
        assertEquals(listOf(10, 11, 20, 21, 30, 31), result.getOrThrow())
    }

    @Test
    fun loadAllPages_stops_on_an_empty_page_and_at_max_pages() = runTest {
        val empty = FakeContentNetworkClient().loadAllPages(
            call = { page -> ok(Page(if (page == 1) listOf(1) else emptyList(), totalPages = 9)) },
            items = { it.items },
            totalPages = { it.totalPages },
        )
        assertEquals(listOf(1), empty.getOrThrow())

        var calls = 0
        FakeContentNetworkClient().loadAllPages(
            call = { _ -> calls++; ok(Page(listOf(1), totalPages = 999)) },
            items = { it.items },
            totalPages = { it.totalPages },
            maxPages = 4,
        )
        assertEquals(4, calls)
    }

    @Test
    fun loadAllPages_fails_when_one_page_fails() = runTest {
        val client = FakeContentNetworkClient(AppError.Timeout)
        val result = client.loadAllPages(
            call = { _ -> ok(Page(listOf(1), totalPages = 2)) },
            items = { it.items },
            totalPages = { it.totalPages },
        )
        assertEquals(ContentError.Offline, result.exceptionOrNull())
    }

    @Test
    fun errors_map_to_content_errors() {
        assertEquals(ContentError.Offline, AppError.NoConnection.toContentError(null))
        assertEquals(ContentError.Offline, AppError.Timeout.toContentError("X"))
        val notFound = AppError.ApiError(code = "TEMPLE_NOT_FOUND", message = "", type = "business")
        assertEquals(ContentError.NotFound, notFound.toContentError("TEMPLE_NOT_FOUND"))
        assertEquals(ContentError.Failed, notFound.toContentError("DEITY_NOT_FOUND"))
        assertEquals(ContentError.NotFound, AppError.HttpError(404).toContentError("TEMPLE_NOT_FOUND"))
        assertEquals(ContentError.Failed, AppError.HttpError(404).toContentError(null))
        assertEquals(ContentError.Failed, AppError.HttpError(500).toContentError("TEMPLE_NOT_FOUND"))
        assertEquals(ContentError.Failed, AppError.Serialization.toContentError(null))
    }

    @Test
    fun a_mapping_exception_is_failed() = runTest {
        val result = FakeContentNetworkClient().loadContent(call = { ok("x") }, map = { error("bad payload") })
        assertEquals(ContentError.Failed, result.exceptionOrNull())
    }
}
