package com.example.innerlamp.ui.screens.festivals

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FestivalsViewModelTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    private fun viewModel(today: LocalDate) =
        FestivalsViewModel(Clock.fixed(today.atStartOfDay(zone).toInstant(), zone))

    @Test
    fun covers_january_of_current_year_through_december_two_years_later() {
        val state = viewModel(LocalDate.of(2026, 10, 8)).uiState.value
        val headers = state.rows.filterIsInstance<FestivalRow.Header>()
        assertEquals(2026 to 1, headers.first().let { it.year to it.month })
        assertEquals(2028 to 12, headers.last().let { it.year to it.month })
        assertEquals(36, headers.size)
    }

    @Test
    fun major_filter_keeps_only_festivals_and_fasting_filter_only_the_rest() {
        val vm = viewModel(LocalDate.of(2026, 10, 8))
        vm.onFilterSelected(FestivalFilter.Major)
        assertTrue(vm.uiState.value.rows.filterIsInstance<FestivalRow.Item>().all { it.entry.event.isFestival })
        vm.onFilterSelected(FestivalFilter.Fasting)
        assertTrue(vm.uiState.value.rows.filterIsInstance<FestivalRow.Item>().none { it.entry.event.isFestival })
    }

    @Test
    fun today_row_is_header_of_current_month() {
        val state = viewModel(LocalDate.of(2026, 10, 8)).uiState.value
        assertEquals(FestivalRow.Header(2026, 10), state.rows[state.todayRowIndex])
    }

    @Test
    fun countdown_labels() {
        val state = viewModel(LocalDate.of(2026, 10, 8)).uiState.value
        assertEquals("Hôm nay", state.countdown(LocalDate.of(2026, 10, 8)).label)
        assertEquals("Ngày mai", state.countdown(LocalDate.of(2026, 10, 9)).label)
        assertEquals("còn 5 ngày", state.countdown(LocalDate.of(2026, 10, 13)).label)
        assertTrue(state.countdown(LocalDate.of(2026, 10, 1)).isPast)
    }

    @Test
    fun every_entry_has_at_least_one_host() {
        val state = viewModel(LocalDate.of(2026, 10, 8)).uiState.value
        assertTrue(state.rows.filterIsInstance<FestivalRow.Item>().all { it.entry.hosts.isNotEmpty() })
    }
}
