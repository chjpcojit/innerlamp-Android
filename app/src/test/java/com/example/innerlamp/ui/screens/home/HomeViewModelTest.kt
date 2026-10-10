package com.example.innerlamp.ui.screens.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    /** 25/9/2026, 09:00 giờ Việt Nam. */
    private val clock = Clock.fixed(
        LocalDate.of(2026, 9, 25).atTime(9, 0).toInstant(ZoneOffset.ofHours(7)),
        ZoneId.of("Asia/Ho_Chi_Minh"),
    )

    private fun viewModel() = HomeViewModel(clock = clock)

    @Test
    fun initial_state_uses_today_and_morning_greeting() {
        val s = viewModel().uiState.value
        assertEquals(LocalDate.of(2026, 9, 25), s.today)
        assertEquals(s.today, s.selectedDate)
        assertEquals("Chào buổi sáng, quý Phật tử", s.greetingLine)
        // 25/9/2026 = Rằm tháng Tám âm lịch (Trung thu).
        assertEquals(15, s.selectedLunar.day)
        assertEquals(8, s.selectedLunar.month)
        assertEquals(12, s.eventMonths.size)
    }

    @Test
    fun week_pages_are_five_monday_first_weeks_centered_on_current_week() {
        val pages = viewModel().uiState.value.weekPages
        assertEquals(5, pages.size)
        assertTrue(pages.all { it.days.size == 7 })
        // 25/9/2026 là thứ Sáu → tuần giữa bắt đầu thứ Hai 21/9.
        assertEquals(LocalDate.of(2026, 9, 21), pages[2].days.first().date)
        assertEquals(LocalDate.of(2026, 9, 27), pages[2].days.last().date)
        assertTrue(pages[2].days.any { it.date == LocalDate.of(2026, 9, 25) })
    }

    @Test
    fun greeting_changes_with_hour() {
        assertEquals("Chào buổi sáng", HomeUiState.greetingFor(6))
        assertEquals("Chào buổi trưa", HomeUiState.greetingFor(12))
        assertEquals("Chào buổi chiều", HomeUiState.greetingFor(15))
        assertEquals("Chào buổi tối", HomeUiState.greetingFor(20))
    }

    @Test
    fun selecting_a_date_updates_lunar_card_and_verse() {
        val vm = viewModel()
        val eve = LocalDate.of(2026, 9, 24) // 14/8 âm 2026
        vm.onDateSelected(eve)
        val s = vm.uiState.value
        assertEquals(eve, s.selectedDate)
        assertEquals(14, s.selectedLunar.day)
        assertEquals(8, s.selectedLunar.month)
        assertEquals(HomeUiState.verseFor(s.selectedLunar), s.verse)
    }

    // ── Skeleton → dữ liệu / rỗng ──────────────────────────────────────────

    private val dispatcher = StandardTestDispatcher()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun loadingViewModel(loader: suspend () -> HomeSectionsContent): HomeViewModel {
        Dispatchers.setMain(dispatcher)
        return HomeViewModel(clock = clock, contentLoader = loader)
    }

    @Test
    fun content_shows_skeleton_until_loader_returns_then_shows_data() {
        val gate = CompletableDeferred<HomeSectionsContent>()
        val vm = loadingViewModel { gate.await() }
        dispatcher.scheduler.runCurrent()

        val loading = vm.uiState.value
        assertTrue(loading.isContentLoading)
        assertTrue(loading.temples.isEmpty() && loading.buddhas.isEmpty() && loading.articles.isEmpty())

        gate.complete(HomeSectionsContent(HomeSampleData.temples, HomeSampleData.buddhas, HomeSampleData.articles))
        dispatcher.scheduler.runCurrent()

        val loaded = vm.uiState.value
        assertFalse(loaded.isContentLoading)
        assertEquals(HomeSampleData.temples, loaded.temples)
        assertEquals(HomeSampleData.articles, loaded.articles)
    }

    @Test
    fun content_is_empty_when_loader_returns_no_data() {
        val vm = loadingViewModel { HomeSectionsContent(emptyList(), emptyList(), emptyList()) }
        dispatcher.scheduler.runCurrent()
        val s = vm.uiState.value
        assertFalse(s.isContentLoading)
        assertTrue(s.temples.isEmpty() && s.buddhas.isEmpty() && s.articles.isEmpty())
    }

    @Test
    fun content_is_empty_when_loader_fails_and_reload_recovers() {
        var fail = true
        val vm = loadingViewModel {
            if (fail) error("network") else HomeSectionsContent(HomeSampleData.temples, HomeSampleData.buddhas, HomeSampleData.articles)
        }
        dispatcher.scheduler.runCurrent()
        assertFalse(vm.uiState.value.isContentLoading)
        assertTrue(vm.uiState.value.temples.isEmpty())

        fail = false
        vm.onReload()
        assertTrue(vm.uiState.value.isContentLoading)
        dispatcher.scheduler.runCurrent()
        assertFalse(vm.uiState.value.isContentLoading)
        assertEquals(HomeSampleData.temples, vm.uiState.value.temples)
    }
}
