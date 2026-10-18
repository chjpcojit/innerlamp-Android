package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.entry
import com.example.innerlamp.domain.calendar.event
import com.example.innerlamp.domain.calendar.occurrence
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.CalendarYearLoad
import com.example.innerlamp.domain.model.CalendarYearSource
import com.example.innerlamp.domain.repository.CalendarEventRepository
import com.example.innerlamp.domain.repository.CalendarLoadError
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarUseCasesTest {

    // 2026-12-31 18:00 UTC = 2027-01-01 01:00 in Vietnam: Vietnam year/month decide. / Giờ VN đã sang 2027.
    private val clock = Clock.fixed(Instant.parse("2026-12-31T18:00:00Z"), ZoneOffset.UTC)

    private fun year(y: Int, vararg dates: String) = CalendarYear(
        y, emptyList(),
        listOf(event("a"), event("b")),
        dates.mapIndexed { i, d -> occurrence(if (i == 0) "a" else "b", d) },
    )

    private class FakeRepo(
        val memory: MutableMap<Int, CalendarYear> = mutableMapOf(),
        val copies: Map<Int, CalendarYear> = emptyMap(),
        val loads: Map<Int, Result<CalendarYearLoad>> = emptyMap(),
    ) : CalendarEventRepository {
        val loadCalls = mutableListOf<Int>()
        override fun cachedInMemory(year: Int) = memory[year]
        override suspend fun deviceCopy(year: Int) = memory[year] ?: copies[year]
        override suspend fun load(year: Int): Result<CalendarYearLoad> {
            loadCalls += year
            return loads.getValue(year)
        }
    }

    private fun ok(y: CalendarYear, out: Boolean = false) =
        Result.success(CalendarYearLoad(y, CalendarYearSource.Network, out))

    @Test
    fun loadYear_usesVietnamYear_andRejectsOutOfRange() = runTest {
        val repo = FakeRepo(loads = mapOf(2027 to ok(year(2027))))
        val uc = LoadCalendarYearUseCase(repo, clock)
        assertEquals(2027, uc.currentYear())
        assertTrue(uc().isSuccess)
        assertTrue(uc(2026).isFailure)
        assertTrue(uc(2101).isFailure)
        assertEquals(listOf(2027), repo.loadCalls)
        assertEquals(2028, uc.nextYear(2027))
        assertNull(uc.nextYear(2100))
    }

    @Test
    fun observe_emitsDeviceCopyFirst_thenFinal_andSkipsCopyWhenLoaded() = runTest {
        val fresh = year(2027)
        val copy = year(2027)
        val repo = FakeRepo(copies = mapOf(2027 to copy), loads = mapOf(2027 to ok(fresh)))
        val uc = LoadCalendarYearUseCase(repo, clock)
        val emitted = uc.observe(2027).toList()
        assertEquals(2, emitted.size)
        assertEquals(CalendarYearSource.DeviceCopy, emitted[0].getOrThrow().source)
        assertEquals(false, emitted[0].getOrThrow().mayBeOutOfDate)
        assertEquals(CalendarYearSource.Network, emitted[1].getOrThrow().source)

        repo.memory[2027] = fresh
        assertEquals(1, uc.observe(2027).toList().size)
    }

    @Test
    fun detail_foundNotFoundAndLoadError_usingMemoryThenCopyThenLoad() = runTest {
        val y = year(2027, "2027-02-06")
        val fromCopy = GetEventOccurrenceUseCase(FakeRepo(copies = mapOf(2027 to y)))
        assertEquals("a", fromCopy("a", LocalDate.parse("2027-02-06")).getOrThrow()!!.event.code)
        assertNull(fromCopy("a", LocalDate.parse("2027-02-07")).getOrThrow())
        assertNull(fromCopy("zzz", LocalDate.parse("2027-02-06")).getOrThrow())

        val repoLoad = FakeRepo(loads = mapOf(2027 to ok(y)))
        assertEquals("a", GetEventOccurrenceUseCase(repoLoad)("a", LocalDate.parse("2027-02-06")).getOrThrow()!!.event.code)
        assertEquals(listOf(2027), repoLoad.loadCalls)

        val failing = FakeRepo(loads = mapOf(2027 to Result.failure(CalendarLoadError.Offline)))
        assertEquals(CalendarLoadError.Offline, GetEventOccurrenceUseCase(failing)("a", LocalDate.parse("2027-02-06")).exceptionOrNull())
    }

    @Test
    fun homeBlock_sixMonthsFromVietnamMonth_oneYearFailingOnlyFailsItsMonths() = runTest {
        val major = event("phat-dan", isMajor = true)
        val y2027 = CalendarYear(2027, emptyList(), listOf(major), listOf(occurrence("phat-dan", "2027-05-20")))
        val repo = FakeRepo(loads = mapOf(2027 to ok(y2027, out = true)))
        val cards = GetHomeEventBlockUseCase(repo, clock)()
        assertEquals(YearMonth.of(2027, 1), cards.first().month)
        assertEquals(6, cards.size)
        assertEquals(listOf(2027), repo.loadCalls) // Jan-Jun 2027: a single year
        val may = cards.first { it.month == YearMonth.of(2027, 5) }.outcome as HomeMonthOutcome.Loaded
        assertEquals(listOf("phat-dan"), may.shown.map { it.event.code })
        assertTrue(may.mayBeOutOfDate)
        val jan = cards.first().outcome as HomeMonthOutcome.Loaded
        assertTrue(jan.shown.isEmpty())
        assertEquals(0, jan.more)

        // Window crossing two years: Nov 2026 start. / Cửa sổ vắt qua hai năm.
        val nov = Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"), ZoneOffset.UTC)
        val repo2 = FakeRepo(loads = mapOf(2026 to ok(year(2026)), 2027 to Result.failure(CalendarLoadError.RateLimited(9))))
        val cards2 = GetHomeEventBlockUseCase(repo2, nov)()
        assertTrue(cards2[0].outcome is HomeMonthOutcome.Loaded) // Nov 2026
        assertTrue(cards2[1].outcome is HomeMonthOutcome.Loaded) // Dec 2026
        assertEquals(HomeMonthOutcome.Failed(CalendarLoadError.RateLimited(9)), cards2[2].outcome) // Jan 2027
        assertEquals(setOf(2026, 2027), repo2.loadCalls.toSet())
    }

    @Test
    fun homeBlock_fromDeviceCopy_nullUnlessEveryYearIsAvailable() = runTest {
        val nov = Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"), ZoneOffset.UTC)
        assertNull(GetHomeEventBlockUseCase(FakeRepo(copies = mapOf(2026 to year(2026))), nov).fromDeviceCopy())
        val both = FakeRepo(copies = mapOf(2026 to year(2026), 2027 to year(2027)))
        assertEquals(6, GetHomeEventBlockUseCase(both, nov).fromDeviceCopy()!!.size)
    }

    @Suppress("unused")
    private fun unused() = entry(event("x"), "2026-01-01")
}
