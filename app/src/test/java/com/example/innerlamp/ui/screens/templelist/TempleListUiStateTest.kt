package com.example.innerlamp.ui.screens.templelist

import com.example.innerlamp.domain.usecase.GetTemplesUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class TempleListUiStateTest {

    private val base = TempleListUiState(temples = GetTemplesUseCase()())

    @Test
    fun noFilterReturnsAll() {
        assertEquals(base.temples.size, base.filtered.size)
    }

    @Test
    fun searchIgnoresCaseAndDiacritics() {
        assertEquals(listOf("tp-motcot"), base.copy(query = "mot cot").filtered.map { it.id })
        assertEquals(listOf("tp-vinhnghiem"), base.copy(query = "  HỒ CHÍ MINH ").filtered.map { it.id })
    }

    @Test
    fun regionAndQueryCombine() {
        val bac = base.copy(region = "Miền Bắc")
        assertEquals(4, bac.filtered.size)
        assertEquals(emptyList<String>(), bac.copy(query = "huế").filtered.map { it.id })
    }

    @Test
    fun emptyMessageMentionsQueryAndRegion() {
        assertEquals("Không tìm thấy chùa nào khớp “xyz” ở Miền Nam.", base.copy(query = "xyz", region = "Miền Nam").emptyMessage)
    }
}
