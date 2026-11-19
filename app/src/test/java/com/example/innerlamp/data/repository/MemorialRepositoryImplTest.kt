package com.example.innerlamp.data.repository

import com.example.innerlamp.core.database.memorial.MemorialCacheDao
import com.example.innerlamp.core.database.memorial.MemorialCacheEntity
import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.remote.api.MemorialApiService
import com.example.innerlamp.data.remote.dto.MemorialDto
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialError
import com.example.innerlamp.domain.model.MemorialLoad
import com.example.innerlamp.domain.model.MemorialRelation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Repository behaviour with the network faked: the photo route outcomes (200/413/415) and the write/read
 * fallbacks. No HTTP server is involved; the app's own routes are covered by the contract tests later. /
 * Hành vi repository với mạng giả: kết quả route ảnh (200/413/415) và các nhánh đọc/ghi. Không dựng server.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MemorialRepositoryImplTest {

    private val dao = mockk<MemorialCacheDao>(relaxed = true)
    private val api = mockk<MemorialApiService>(relaxed = true)
    private val dispatchers = object : DispatcherProvider {
        override val io: CoroutineDispatcher = UnconfinedTestDispatcher()
        override val default: CoroutineDispatcher = UnconfinedTestDispatcher()
        override val main: CoroutineDispatcher = UnconfinedTestDispatcher()
    }

    private class FakeClient(var next: NetworkResult<Any>) : NetworkClient {
        override fun <T : Any> createService(clazz: Class<T>): T = error("not used")
        @Suppress("UNCHECKED_CAST")
        override suspend fun <T : Any> safeCall(block: suspend () -> ApiResponse<T>): NetworkResult<T> =
            next as NetworkResult<T>
    }

    private fun repository(client: FakeClient, owner: Long? = 1L) = MemorialRepositoryImpl(
        api = api,
        networkClient = client,
        dao = dao,
        currentOwnerId = { owner },
        dispatchers = dispatchers,
    )

    private fun dto(id: Long = 3, photo: String? = null) = MemorialDto(
        id = id,
        fullName = "Trần Thị Mai",
        relationship = "MOTHER",
        deathCalendar = "lunar",
        deathDate = "2026-11-23",
        reminder = null,
        photoUrl = photo,
    )

    private fun entry() = MemorialEntry(
        id = 0L, name = "Trần Thị Mai", relation = MemorialRelation.Mother,
        calendar = com.example.innerlamp.domain.model.MemorialCalendarType.Lunar,
        day = 15, month = 10, deathYear = 2026, note = "",
    )

    @Test
    fun a_guest_never_reads_the_device_copy() = runTest {
        val load = repository(FakeClient(NetworkResult.Success(Unit)), owner = null).load()
        assertEquals(MemorialLoad.Guest, load)
    }

    @Test
    fun a_failed_load_with_rows_returns_the_device_copy() = runTest {
        coEvery { dao.entries(1L) } returns listOf(
            MemorialCacheEntity(
                ownerId = 1L, id = 3L, name = "Trần Thị Mai", relationCode = "MOTHER",
                calendarCode = "lunar", day = 15, month = 10, deathYear = 2026, leapMonth = false,
                note = "", remind = false, leadDays = 1, remindTime = "07:00",
            ),
        )
        val load = repository(FakeClient(NetworkResult.Failure(AppError.NoConnection))).load()
        assertTrue(load is MemorialLoad.Offline)
        assertEquals(1, (load as MemorialLoad.Offline).entries.size)
        assertEquals(15, load.entries.first().day)
    }

    @Test
    fun a_failed_load_without_rows_fails() = runTest {
        coEvery { dao.entries(1L) } returns emptyList()
        val load = repository(FakeClient(NetworkResult.Failure(AppError.NoConnection))).load()
        assertEquals(MemorialLoad.Failed(MemorialError.Offline), load)
    }

    @Test
    fun a_404_on_delete_drops_the_row_and_reports_it_was_already_gone() = runTest {
        val client = FakeClient(
            NetworkResult.Failure(AppError.ApiError("MEMORIAL_NOT_FOUND", "", "")),
        )
        assertEquals(MemorialError.NotFound, repository(client).delete(3L).exceptionOrNull())
        coVerify { dao.deleteById(1L, 3L) }
    }

    @Test
    fun a_photo_upload_returns_the_new_signed_url() = runTest {
        val client = FakeClient(NetworkResult.Success(dto(photo = "https://cdn.test/1.jpg")))
        val result = repository(client).uploadPhoto(3L, byteArrayOf(1, 2, 3), "image/jpeg", "photo.jpg")
        assertEquals("https://cdn.test/1.jpg", result.getOrNull()?.photoUrl)
        coVerify { dao.upsert(any()) }
    }

    @Test
    fun a_413_photo_maps_to_too_large() = runTest {
        val client = FakeClient(NetworkResult.Failure(AppError.ApiError("PHOTO_TOO_LARGE", "", "")))
        val result = repository(client).uploadPhoto(3L, byteArrayOf(1), "image/jpeg", "photo.jpg")
        assertEquals(MemorialError.TooLarge, result.exceptionOrNull())
    }

    @Test
    fun a_415_photo_maps_to_unsupported() = runTest {
        val client = FakeClient(NetworkResult.Failure(AppError.HttpError(415)))
        val result = repository(client).uploadPhoto(3L, byteArrayOf(1), "image/jpeg", "photo.jpg")
        assertEquals(MemorialError.UnsupportedPhoto, result.exceptionOrNull())
    }

    @Test
    fun a_photo_over_five_mib_is_rejected_before_the_call() = runTest {
        val client = FakeClient(NetworkResult.Success(dto()))
        val result = repository(client).uploadPhoto(3L, ByteArray(5 * 1024 * 1024 + 1), "image/jpeg", "photo.jpg")
        assertEquals(MemorialError.TooLarge, result.exceptionOrNull())
        coVerify(exactly = 0) { dao.upsert(any()) }
    }

    @Test
    fun a_created_entry_is_written_to_the_device_copy() = runTest {
        val client = FakeClient(NetworkResult.Success(dto()))
        val result = repository(client).create(entry())
        assertTrue(result.isSuccess)
        assertEquals(3L, result.getOrNull()?.id)
        coVerify { dao.upsert(any()) }
    }
}
