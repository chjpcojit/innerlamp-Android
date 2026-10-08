package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.SutraQuote
import com.example.innerlamp.domain.repository.SutraQuoteRepository
import java.time.LocalDate
import kotlin.random.Random
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SutraQuoteUseCaseTest {

    private fun quote(seq: Int) = SutraQuote(seq, "Kinh", "Dịch giả", "I", "Phẩm", seq, "câu $seq")

    /** Repo giả: seed nạp [catalog] vào DB lần đầu (idempotent theo sequence). */
    private class FakeRepo(private val catalog: List<SutraQuote>, initial: List<SutraQuote> = emptyList()) :
        SutraQuoteRepository {
        val rows = initial.toMutableList()
        var seedCalls = 0

        override suspend fun seedIfNeeded() {
            seedCalls++
            val have = rows.map { it.sequence }.toSet()
            rows += catalog.filter { it.sequence !in have }
        }

        override suspend fun count() = rows.size
        override suspend fun quoteAt(index: Int) = rows.sortedBy { it.sequence }.getOrNull(index)
        override suspend fun getAll() = rows.sortedBy { it.sequence }
    }

    private val ten = (1..10).map(::quote)

    @Test
    fun quote_for_date_uses_day_of_year_modulo_total() = runTest {
        val useCase = SutraQuoteUseCase(FakeRepo(ten, ten))
        // Ngày 1/1 → dayOfYear 1 → index 1 % 10 = 1 → sequence 2.
        assertEquals(2, useCase.quote(LocalDate.of(2026, 1, 1))!!.sequence)
        // 31/12/2026 → dayOfYear 365 → index 5 → sequence 6.
        assertEquals(6, useCase.quote(LocalDate.of(2026, 12, 31))!!.sequence)
        // Năm nhuận 31/12/2028 → dayOfYear 366 → index 6 → sequence 7.
        assertEquals(7, useCase.quote(LocalDate.of(2028, 12, 31))!!.sequence)
    }

    @Test
    fun quote_for_date_is_stable_within_a_day() = runTest {
        val useCase = SutraQuoteUseCase(FakeRepo(ten, ten))
        val d = LocalDate.of(2026, 9, 30)
        assertEquals(useCase.quote(d), useCase.quote(d))
    }

    @Test
    fun empty_db_seeds_before_reading() = runTest {
        val repo = FakeRepo(ten)
        val useCase = SutraQuoteUseCase(repo)
        assertEquals(2, useCase.quote(LocalDate.of(2026, 1, 1))!!.sequence)
        assertEquals(1, repo.seedCalls)
    }

    @Test
    fun empty_db_that_stays_empty_returns_null_and_empty() = runTest {
        val useCase = SutraQuoteUseCase(FakeRepo(emptyList()))
        assertNull(useCase.quote(LocalDate.of(2026, 1, 1)))
        assertNull(useCase.randomQuote())
        assertTrue(useCase.randomQuotes(3).isEmpty())
    }

    @Test
    fun populated_db_is_not_reseeded() = runTest {
        val repo = FakeRepo(ten, ten)
        SutraQuoteUseCase(repo).randomQuote()
        assertEquals(0, repo.seedCalls)
    }

    @Test
    fun random_quote_picks_index_from_random_source() = runTest {
        val useCase = SutraQuoteUseCase(FakeRepo(ten, ten), Random(42))
        val expected = Random(42).nextInt(0, 10) + 1
        assertEquals(expected, useCase.randomQuote()!!.sequence)
    }

    @Test
    fun random_quote_is_uniform_enough_and_not_cached() = runTest {
        val useCase = SutraQuoteUseCase(FakeRepo(ten, ten), Random(7))
        val seen = (1..200).map { useCase.randomQuote()!!.sequence }.toSet()
        assertEquals(ten.map { it.sequence }.toSet(), seen)
    }

    @Test
    fun random_quotes_are_distinct_and_capped_by_catalog() = runTest {
        val useCase = SutraQuoteUseCase(FakeRepo(ten, ten), Random(1))
        val picked = useCase.randomQuotes(4)
        assertEquals(4, picked.size)
        assertEquals(4, picked.map { it.sequence }.toSet().size)
        assertEquals(10, useCase.randomQuotes(50).size)
    }

    @Test
    fun random_quotes_non_positive_count_is_empty() = runTest {
        val useCase = SutraQuoteUseCase(FakeRepo(ten, ten))
        assertTrue(useCase.randomQuotes(0).isEmpty())
        assertTrue(useCase.randomQuotes(-3).isEmpty())
    }
}
