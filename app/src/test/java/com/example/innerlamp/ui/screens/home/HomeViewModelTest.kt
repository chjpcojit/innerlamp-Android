package com.example.innerlamp.ui.screens.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import com.example.innerlamp.domain.calendar.FakeCalendarEventRepository
import com.example.innerlamp.domain.calendar.event
import com.example.innerlamp.domain.calendar.network
import com.example.innerlamp.domain.model.CalendarGroup
import com.example.innerlamp.domain.model.CalendarOccurrence
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.model.DeityType
import com.example.innerlamp.domain.model.HomePhotos
import com.example.innerlamp.domain.model.LunarDay
import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.domain.usecase.GetHomeEventBlockUseCase
import com.example.innerlamp.domain.usecase.HomeMonthOutcome
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val temples = listOf(Temple("t1", "Chùa Bái Đính", "Ninh Bình", "Miền Bắc", "Di tích quốc gia"))
    private val buddhas = listOf(DeitySummary("quanam", DeityType.Bodhisattva, "Quán Thế Âm", null, null, null, null, "Đại Bi", null))
    private val content = HomeSectionsContent(temples, buddhas, HomeSampleData.articles, HomePhotos(emptyList(), total = 12))

    @Test
    fun initial_state_uses_today_and_morning_greeting() {
        val s = viewModel().uiState.value
        assertEquals(LocalDate.of(2026, 9, 25), s.today)
        assertEquals(s.today, s.selectedDate)
        assertEquals("Chào buổi sáng, quý Phật tử", s.greetingLine)
        // 25/9/2026 = Rằm tháng Tám âm lịch (Trung thu).
        assertEquals(15, s.selectedLunar.day)
        assertEquals(8, s.selectedLunar.month)
        // Without the calendar use case the block stays empty (no local catalogue any more).
        assertTrue(s.eventCards.isEmpty())
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

        gate.complete(content)
        dispatcher.scheduler.runCurrent()

        val loaded = vm.uiState.value
        assertFalse(loaded.isContentLoading)
        assertEquals(temples, loaded.temples)
        assertEquals(buddhas, loaded.buddhas)
        assertEquals(HomeSampleData.articles, loaded.articles)
        assertEquals(12, loaded.photos?.more)
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
            if (fail) error("network") else content
        }
        dispatcher.scheduler.runCurrent()
        assertFalse(vm.uiState.value.isContentLoading)
        assertTrue(vm.uiState.value.temples.isEmpty())

        fail = false
        vm.onReload()
        assertTrue(vm.uiState.value.isContentLoading)
        dispatcher.scheduler.runCurrent()
        assertFalse(vm.uiState.value.isContentLoading)
        assertEquals(temples, vm.uiState.value.temples)
    }

    // ── Khối "Sự kiện Phật giáo" từ lịch lễ ────────────────────────────────

    private fun calendarYear(y: Int, vararg dates: String) = CalendarYear(
        y,
        listOf(CalendarGroup("buddhist", "Phật giáo")),
        dates.indices.map { event("e$it", sortOrder = it + 1, isMajor = it == 0) },
        dates.mapIndexed { i, d -> CalendarOccurrence("e$i", LocalDate.parse(d), LunarDay(1, 1, y, false)) },
    )

    private fun eventsViewModel(repo: FakeCalendarEventRepository): HomeViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        return HomeViewModel(clock = clock, getHomeEventBlock = GetHomeEventBlockUseCase(repo, clock))
    }

    @Test
    fun home_block_has_six_months_from_the_current_one_with_empty_month_and_no_more() {
        val repo = FakeCalendarEventRepository(
            loads = mutableMapOf(
                2026 to network(calendarYear(2026, "2026-09-28", "2026-12-02")),
                2027 to network(calendarYear(2027)),
            ),
        )
        val s = eventsViewModel(repo).uiState.value
        assertFalse(s.eventsLoading)
        assertEquals(listOf(9, 10, 11, 12, 1, 2), s.eventCards.map { it.month.monthValue })
        val sep = s.eventCards[0].outcome as HomeMonthOutcome.Loaded
        assertEquals(1, sep.shown.size)
        val oct = s.eventCards[1].outcome as HomeMonthOutcome.Loaded
        assertTrue(oct.shown.isEmpty()) // empty month card, not an error
        assertEquals(0, oct.more)
    }

    @Test
    fun a_failing_year_only_fails_its_own_months() {
        val repo = FakeCalendarEventRepository(
            loads = mutableMapOf(
                2026 to network(calendarYear(2026, "2026-09-28")),
                2027 to Result.failure(CalendarLoadError.Offline),
            ),
        )
        val cards = eventsViewModel(repo).uiState.value.eventCards
        assertTrue(cards.take(4).all { it.outcome is HomeMonthOutcome.Loaded })
        assertEquals(HomeMonthOutcome.Failed(CalendarLoadError.Offline), cards[4].outcome)
        assertEquals(HomeMonthOutcome.Failed(CalendarLoadError.Offline), cards[5].outcome)
    }

    @Test
    fun rate_limited_card_waits_for_a_user_retry() {
        val limited = CalendarLoadError.RateLimited(20)
        val repo = FakeCalendarEventRepository(
            loads = mutableMapOf(2026 to Result.failure(limited), 2027 to Result.failure(limited)),
        )
        val vm = eventsViewModel(repo)
        assertEquals(HomeMonthOutcome.Failed(limited), vm.uiState.value.eventCards.first().outcome)
        assertEquals(2, repo.loadCalls.size) // two years once, no loop

        repo.loads[2026] = network(calendarYear(2026, "2026-09-28"))
        repo.loads[2027] = network(calendarYear(2027))
        vm.onReloadEvents()
        assertTrue(vm.uiState.value.eventCards.first().outcome is HomeMonthOutcome.Loaded)
        assertEquals(4, repo.loadCalls.size)
    }

    @Test
    fun device_copy_shows_first_then_the_loaded_cards() {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeCalendarEventRepository(
            copies = mutableMapOf(2026 to calendarYear(2026, "2026-09-28"), 2027 to calendarYear(2027)),
            loads = mutableMapOf(
                2026 to network(calendarYear(2026, "2026-09-28", "2026-09-29")),
                2027 to network(calendarYear(2027)),
            ),
            gates = mutableMapOf(2026 to gate),
        )
        val vm = eventsViewModel(repo)
        val first = vm.uiState.value.eventCards.first().outcome as HomeMonthOutcome.Loaded
        assertEquals(1, first.shown.size)
        assertTrue(vm.uiState.value.eventsLoading)
        gate.complete(Unit)
        val final = vm.uiState.value.eventCards.first().outcome as HomeMonthOutcome.Loaded
        assertEquals(2, final.shown.size)
        assertFalse(vm.uiState.value.eventsLoading)
    }

    @Test
    fun resume_on_a_new_day_moves_today_and_rebuilds_the_block() {
        val repo = FakeCalendarEventRepository(
            loads = mutableMapOf(2026 to network(calendarYear(2026)), 2027 to network(calendarYear(2027))),
        )
        var now = clock
        val moving = object : Clock() {
            override fun getZone() = now.zone
            override fun withZone(zone: ZoneId) = now.withZone(zone)
            override fun instant() = now.instant()
        }
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val vm = HomeViewModel(clock = moving, getHomeEventBlock = GetHomeEventBlockUseCase(repo, moving))
        assertEquals(2, repo.loadCalls.size)
        vm.onResume() // same day: nothing
        assertEquals(2, repo.loadCalls.size)

        now = Clock.fixed(now.instant().plusSeconds(24 * 3600), now.zone)
        vm.onResume()
        assertEquals(LocalDate.of(2026, 9, 26), vm.uiState.value.today)
        assertEquals(4, repo.loadCalls.size)
    }
}
