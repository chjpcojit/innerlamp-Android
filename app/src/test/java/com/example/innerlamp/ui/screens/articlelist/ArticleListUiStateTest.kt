package com.example.innerlamp.ui.screens.articlelist

import com.example.innerlamp.domain.usecase.GetAllArticlesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleListUiStateTest {

    private val base = ArticleListUiState(all = GetAllArticlesUseCase()())

    @Test
    fun startsWithFirstPage() {
        assertEquals(20, base.visible.size)
        assertEquals("20 / 60 bài", base.subtitle)
        assertTrue(base.hasMore)
        assertFalse(base.isEnd)
    }

    @Test
    fun endReachedWhenAllVisible() {
        val end = base.copy(visibleCount = 60)
        assertFalse(end.hasMore)
        assertTrue(end.isEnd)
        assertFalse(end.copy(isLoadingMore = true).isEnd)
    }
}
