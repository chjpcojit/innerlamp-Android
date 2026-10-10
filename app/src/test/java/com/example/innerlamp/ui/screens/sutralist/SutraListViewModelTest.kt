package com.example.innerlamp.ui.screens.sutralist

import com.example.innerlamp.domain.model.SutraLengthFilter
import com.example.innerlamp.domain.model.SutraTagFilter
import com.example.innerlamp.domain.model.formatSutraDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SutraListViewModelTest {

    private fun viewModel() = SutraListViewModel()

    @Test
    fun initial_state_shows_all_without_filtering_suffix() {
        val s = viewModel().uiState.value
        assertEquals(11, s.visible.size)
        assertEquals("11 bản kinh", s.countLabel)
        assertEquals("11 bộ · Bắc tông & Nam tông", s.subtitle)
    }

    @Test
    fun query_matches_without_diacritics() {
        val vm = viewModel()
        vm.onQueryChange("dia tang")
        assertEquals(listOf("diatang"), vm.uiState.value.visible.map { it.id })
        assertEquals("1 bản kinh phù hợp", vm.uiState.value.countLabel)
    }

    @Test
    fun tag_filter_by_theme_and_sect() {
        val vm = viewModel()
        vm.onTagSelected(SutraTagFilter.Mantra)
        assertEquals(setOf("daibi", "tamkinh"), vm.uiState.value.visible.map { it.id }.toSet())
        vm.onTagSelected(SutraTagFilter.Nam)
        assertTrue(vm.uiState.value.visible.all { it.sect.label == "Nam tông" })
        assertEquals(4, vm.uiState.value.visible.size)
    }

    @Test
    fun length_filter_boundaries() {
        val vm = viewModel()
        vm.onLengthSelected(SutraLengthFilter.Short)
        assertTrue(vm.uiState.value.visible.all { it.minutes < 15 })
        vm.onLengthSelected(SutraLengthFilter.Medium) // gồm cả 15 và 30
        assertTrue(vm.uiState.value.visible.any { it.id == "chuyenphap" })
        vm.onLengthSelected(SutraLengthFilter.Long)
        assertTrue(vm.uiState.value.visible.all { it.minutes > 30 })
    }

    @Test
    fun no_match_gives_empty_list() {
        val vm = viewModel()
        vm.onQueryChange("zzz")
        assertTrue(vm.uiState.value.visible.isEmpty())
    }

    @Test
    fun duration_format() {
        assertEquals("~22 phút", formatSutraDuration(22))
        assertEquals("2 giờ 10 phút", formatSutraDuration(130))
        assertEquals("2 giờ", formatSutraDuration(120))
    }
}
