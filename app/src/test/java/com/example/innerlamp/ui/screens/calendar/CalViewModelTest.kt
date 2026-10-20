package com.example.innerlamp.ui.screens.calendar

import com.example.innerlamp.domain.model.CalendarDayImage
import com.example.innerlamp.domain.repository.CalendarImageError
import com.example.innerlamp.domain.repository.CalendarImageRepository
import com.example.innerlamp.domain.usecase.GetCalendarDayImageUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-10-20T03:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 10, 20)

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun image(date: LocalDate) =
        CalendarDayImage(date, "1", "https://img/$date.jpg", "Jane", "CC BY 4.0", "https://cc/by", null)

    private class FakeRepo(var respond: suspend (LocalDate) -> Result<CalendarDayImage>) : CalendarImageRepository {
        val requested = mutableListOf<LocalDate>()
        override suspend fun load(date: LocalDate): Result<CalendarDayImage> {
            requested += date
            return respond(date)
        }
    }

    private fun vm(repo: FakeRepo) =
        CalViewModel(clock = clock, getDayImage = GetCalendarDayImageUseCase(repo))

    @Test
    fun init_loadsImageOfToday() = runTest(dispatcher) {
        val repo = FakeRepo { Result.success(image(it)) }
        val vm = vm(repo)
        assertEquals(DayImageUiState.Loading, vm.uiState.value.dayImage)
        advanceUntilIdle()
        assertEquals(DayImageUiState.Loaded(image(today)), vm.uiState.value.dayImage)
        assertEquals(listOf(today), repo.requested)
    }

    @Test
    fun selectingDate_loadsThatDay_andSameDateIsIgnored() = runTest(dispatcher) {
        val repo = FakeRepo { Result.success(image(it)) }
        val vm = vm(repo)
        advanceUntilIdle()
        val other = today.plusDays(3)
        vm.onDateSelected(other)
        vm.onDateSelected(other)
        advanceUntilIdle()
        assertEquals(DayImageUiState.Loaded(image(other)), vm.uiState.value.dayImage)
        assertEquals(listOf(today, other), repo.requested)
    }

    @Test
    fun staleResponse_doesNotOverwriteNewerSelection() = runTest(dispatcher) {
        val slow = CompletableDeferred<Result<CalendarDayImage>>()
        val repo = FakeRepo { date -> if (date == today) slow.await() else Result.success(image(date)) }
        val vm = vm(repo)
        advanceUntilIdle()
        val other = today.plusDays(1)
        vm.onDateSelected(other)
        advanceUntilIdle()
        slow.complete(Result.success(image(today)))
        advanceUntilIdle()
        assertEquals(DayImageUiState.Loaded(image(other)), vm.uiState.value.dayImage)
    }

    @Test
    fun errors_mapToStates_andRetryReloads() = runTest(dispatcher) {
        var failure: CalendarImageError? = CalendarImageError.Offline
        val repo = FakeRepo { date -> failure?.let { Result.failure(it) } ?: Result.success(image(date)) }
        val vm = vm(repo)
        advanceUntilIdle()
        assertEquals(DayImageUiState.Offline, vm.uiState.value.dayImage)

        failure = CalendarImageError.Failed
        vm.retryImage()
        advanceUntilIdle()
        assertEquals(DayImageUiState.Failed, vm.uiState.value.dayImage)

        failure = null
        vm.retryImage()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.dayImage is DayImageUiState.Loaded)
        assertEquals(3, repo.requested.size)
    }

    @Test
    fun invalidDate_isFailed() = runTest(dispatcher) {
        val repo = FakeRepo { Result.success(image(it)) }
        val vm = vm(repo)
        advanceUntilIdle()
        vm.onDateSelected(LocalDate.of(2101, 1, 1))
        advanceUntilIdle()
        assertEquals(DayImageUiState.Failed, vm.uiState.value.dayImage)
    }
}
