package com.example.innerlamp.ui.screens.festivals

import com.example.innerlamp.domain.calendar.FakeCalendarEventRepository
import com.example.innerlamp.domain.calendar.event
import com.example.innerlamp.domain.calendar.network
import com.example.innerlamp.domain.model.CalendarGroup
import com.example.innerlamp.domain.model.CalendarOccurrence
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.LunarDay
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.domain.usecase.LoadCalendarYearUseCase
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CompletableDeferred
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
class FestivalsViewModelTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private val clock = Clock.fixed(Instant.parse("2026-10-17T03:00:00Z"), zone)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private val groups = listOf(CalendarGroup("buddhist", "Phật giáo"), CalendarGroup("folk", "Dân gian"))

    private fun year(y: Int, vararg dates: Triple<String, String, Boolean>): CalendarYear {
        // (code, date, isMajor)
        val events = dates.map { (code, _, major) ->
            event(code, isMajor = major, groups = if (code.startsWith("f")) listOf("folk") else listOf("buddhist"))
        }
        val occ = dates.map { (code, date, _) -> CalendarOccurrence(code, LocalDate.parse(date), LunarDay(15, 7, y, false)) }
        return CalendarYear(y, groups, events, occ)
    }

    private val y2026 = year(
        2026,
        Triple("b1", "2026-09-10", false),
        Triple("b2", "2026-10-07", true),
        Triple("f1", "2026-10-25", false),
        Triple("b3", "2026-11-03", true),
    )
    private val y2027 = year(2027, Triple("b4", "2027-01-05", false))

    private fun vm(repo: FakeCalendarEventRepository, c: Clock = clock) =
        FestivalsViewModel(LoadCalendarYearUseCase(repo, c), c)

    private fun items(state: FestivalsUiState) = state.rows.filterIsInstance<FestivalRow.Item>()

    @Test
    fun loads_current_year_with_headers_chips_counts_and_group_labels() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to network(y2026)))
        val state = vm(repo).uiState.value
        assertEquals(FestivalsLoadState.Loaded(), state.loadState)
        assertEquals(listOf(9, 10, 11), state.rows.filterIsInstance<FestivalRow.Header>().map { it.month })
        assertEquals(
            listOf("Tất cả" to 4, "Lễ lớn" to 2, "Phật giáo" to 3, "Dân gian" to 1),
            state.chips.map { it.label to it.count },
        )
        assertEquals("Dân gian", items(state).first { it.entry.event.code == "f1" }.groupLabel)
        // The current month heading (October) is the scroll target.
        assertEquals(FestivalRow.Header(2026, 10), state.rows[state.todayRowIndex])
    }

    @Test
    fun major_and_group_filters_use_isMajor_and_group_codes() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to network(y2026)))
        val vm = vm(repo)
        vm.onFilterSelected(CalendarFilter.Major)
        assertEquals(listOf("b2", "b3"), items(vm.uiState.value).map { it.entry.event.code })
        vm.onFilterSelected(CalendarFilter.Group("folk"))
        assertEquals(listOf("f1"), items(vm.uiState.value).map { it.entry.event.code })
    }

    @Test
    fun device_copy_shows_first_then_refresh_replaces_it() {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeCalendarEventRepository(
            copies = mutableMapOf(2026 to y2026),
            loads = mutableMapOf(2026 to network(year(2026, Triple("b9", "2026-12-01", false)))),
            gates = mutableMapOf(2026 to gate),
        )
        val vm = vm(repo)
        assertEquals(FestivalsLoadState.Loaded(refreshing = true), vm.uiState.value.loadState)
        assertEquals(4, items(vm.uiState.value).size)
        gate.complete(Unit)
        assertEquals(FestivalsLoadState.Loaded(), vm.uiState.value.loadState)
        assertEquals(listOf("b9"), items(vm.uiState.value).map { it.entry.event.code })
    }

    @Test
    fun network_failure_with_device_copy_keeps_the_copy_with_out_of_date_flag() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to network(y2026, outOfDate = true)))
        val state = vm(repo).uiState.value
        assertEquals(FestivalsLoadState.Loaded(mayBeOutOfDate = true), state.loadState)
        assertEquals(4, items(state).size)
    }

    @Test
    fun offline_without_copy_is_an_error_and_retry_is_user_driven() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to Result.failure(CalendarLoadError.Offline)))
        val vm = vm(repo)
        assertEquals(FestivalsLoadState.Error(CalendarLoadError.Offline), vm.uiState.value.loadState)
        assertEquals(1, repo.loadCalls.size)

        repo.loads[2026] = network(y2026)
        vm.onRetry()
        assertEquals(FestivalsLoadState.Loaded(), vm.uiState.value.loadState)
        assertEquals(2, repo.loadCalls.size)
    }

    @Test
    fun rate_limited_shows_retry_later_and_never_retries_by_itself() {
        val error = CalendarLoadError.RateLimited(30)
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to Result.failure(error)))
        val vm = vm(repo)
        vm.onReachedEnd()
        vm.onResume()
        assertEquals(FestivalsLoadState.Error(error), vm.uiState.value.loadState)
        assertEquals(listOf(2026), repo.loadCalls)
        assertEquals("Hệ thống đang bận. Vui lòng đợi khoảng 30 giây rồi thử lại.", CalendarText.message(error))
        assertEquals(CalendarText.RATE_LIMITED, CalendarText.message(CalendarLoadError.RateLimited(null)))
    }

    @Test
    fun reaching_the_end_appends_next_year_once_and_keeps_the_chip_filter() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to network(y2026), 2027 to network(y2027)))
        val vm = vm(repo)
        vm.onFilterSelected(CalendarFilter.Group("buddhist"))
        vm.onReachedEnd()
        assertEquals(listOf(2026, 2027), repo.loadCalls)
        val state = vm.uiState.value
        assertEquals(listOf(2026, 2027), state.years.map { it.year })
        assertEquals(CalendarFilter.Group("buddhist"), state.filter)
    }

    @Test
    fun next_year_failure_keeps_the_list_and_retry_appends() {
        val repo = FakeCalendarEventRepository(
            loads = mutableMapOf(2026 to network(y2026), 2027 to Result.failure(CalendarLoadError.Failed)),
        )
        val vm = vm(repo)
        vm.onReachedEnd()
        assertEquals(NextYearState.Error(2027, CalendarLoadError.Failed), vm.uiState.value.nextYearState)
        assertEquals(4, items(vm.uiState.value).size)
        vm.onReachedEnd() // not Idle: no automatic retry
        assertEquals(2, repo.loadCalls.size)

        repo.loads[2027] = network(y2027)
        vm.onRetryNextYear()
        assertEquals(5, items(vm.uiState.value).size)
    }

    @Test
    fun stops_at_2100() {
        val late = Clock.fixed(Instant.parse("2100-06-01T03:00:00Z"), zone)
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2100 to network(year(2100, Triple("b1", "2100-06-02", false)))))
        val vm = vm(repo, late)
        vm.onReachedEnd()
        assertEquals(NextYearState.End, vm.uiState.value.nextYearState)
        assertEquals(listOf(2100), repo.loadCalls)
    }

    @Test
    fun resume_follows_a_new_day() {
        val repo = FakeCalendarEventRepository(loads = mutableMapOf(2026 to network(y2026)))
        var now = clock
        val moving = object : Clock() {
            override fun getZone() = now.zone
            override fun withZone(zone: ZoneId) = now.withZone(zone)
            override fun instant() = now.instant()
        }
        val vm = FestivalsViewModel(LoadCalendarYearUseCase(repo, moving), moving)
        assertEquals(LocalDate.of(2026, 10, 17), vm.uiState.value.today)
        now = Clock.fixed(Instant.parse("2026-10-18T03:00:00Z"), zone)
        vm.onResume()
        assertEquals(LocalDate.of(2026, 10, 18), vm.uiState.value.today)
        assertEquals(1, repo.loadCalls.size)
    }

    @Test
    fun leap_month_marker_is_on_the_lunar_date() {
        assertEquals("15/4 (nhuận)", CalendarText.lunarDate(LunarDay(15, 4, 2026, true)))
        assertEquals("Âm lịch 15/4", CalendarText.lunarLabel(LunarDay(15, 4, 2026, false)))
        assertTrue(CalendarText.message(CalendarLoadError.Offline).startsWith("Không có kết nối mạng"))
    }
}
