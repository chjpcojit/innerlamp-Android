package com.example.innerlamp.ui.screens.festivals

import com.example.innerlamp.domain.calendar.FakeCalendarEventRepository
import com.example.innerlamp.domain.calendar.event
import com.example.innerlamp.domain.calendar.network
import com.example.innerlamp.domain.model.CalendarGroup
import com.example.innerlamp.domain.model.CalendarOccurrence
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.LunarDay
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.domain.usecase.GetEventOccurrenceUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FestivalDetailViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-17T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val year = CalendarYear(
        2026,
        listOf(CalendarGroup("buddhist", "Phật giáo")),
        listOf(
            event(
                "vu_lan",
                isMajor = true,
                longDescription = "Đoạn một, có dấu phẩy.\n\nĐoạn hai; có chấm phẩy.",
                guidance = "- Ăn chay, tụng kinh\nHồi hướng",
            ),
            event("empty_text"),
        ),
        listOf(
            CalendarOccurrence("vu_lan", LocalDate.of(2026, 10, 25), LunarDay(15, 7, 2026, true)),
            CalendarOccurrence("empty_text", LocalDate.of(2026, 10, 26), LunarDay(16, 7, 2026, false)),
        ),
    )

    private fun vm(repo: FakeCalendarEventRepository, code: String, date: String) =
        FestivalDetailViewModel(code, date, GetEventOccurrenceUseCase(repo), clock)

    @Test
    fun found_has_paragraphs_numbered_practices_group_names_and_countdown() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to network(year)))
        val state = vm(repo, "vu_lan", "2026-10-25").uiState.value as FestivalDetailUiState.Found
        assertEquals(listOf("Đoạn một, có dấu phẩy.", "Đoạn hai; có chấm phẩy."), state.paragraphs)
        assertEquals(listOf("Ăn chay, tụng kinh", "Hồi hướng"), state.practices)
        assertEquals(listOf("Phật giáo"), state.groupNames)
        assertEquals("Còn 8 ngày", CalendarText.countdown(state.countdown.label))
    }

    @Test
    fun empty_texts_are_hidden_not_replaced() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to network(year)))
        val state = vm(repo, "empty_text", "2026-10-26").uiState.value as FestivalDetailUiState.Found
        assertTrue(state.paragraphs.isEmpty())
        assertTrue(state.practices.isEmpty())
    }

    @Test
    fun unknown_pair_or_bad_date_is_not_found() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to network(year)))
        assertEquals(FestivalDetailUiState.NotFound, vm(repo, "vu_lan", "2026-10-26").uiState.value)
        assertEquals(FestivalDetailUiState.NotFound, vm(repo, "vu_lan", "not-a-date").uiState.value)
    }

    @Test
    fun load_error_then_user_retry() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to Result.failure(CalendarLoadError.Offline)))
        val vm = vm(repo, "vu_lan", "2026-10-25")
        assertEquals(FestivalDetailUiState.Error(CalendarLoadError.Offline), vm.uiState.value)
        assertEquals(1, repo.loadCalls.size)
        repo.loads[2026] = network(year)
        vm.onRetry()
        assertTrue(vm.uiState.value is FestivalDetailUiState.Found)
    }
}
