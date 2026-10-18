package com.example.innerlamp.domain.calendar

import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.CalendarYearLoad
import com.example.innerlamp.domain.model.CalendarYearSource
import com.example.innerlamp.domain.repository.CalendarEventRepository
import kotlinx.coroutines.CompletableDeferred

/**
 * In-memory [CalendarEventRepository] for ViewModel / use case tests: memory, device copies and a
 * scripted result per year (optionally gated). Test source only. /
 * Repository trong bộ nhớ cho test ViewModel / use case; chỉ nằm trong test.
 */
internal class FakeCalendarEventRepository(
    val memory: MutableMap<Int, CalendarYear> = mutableMapOf(),
    val copies: MutableMap<Int, CalendarYear> = mutableMapOf(),
    val loads: MutableMap<Int, Result<CalendarYearLoad>> = mutableMapOf(),
    /** When set for a year, [load] waits for it before answering. */
    val gates: MutableMap<Int, CompletableDeferred<Unit>> = mutableMapOf(),
) : CalendarEventRepository {
    val loadCalls = mutableListOf<Int>()

    override fun cachedInMemory(year: Int): CalendarYear? = memory[year]
    override suspend fun deviceCopy(year: Int): CalendarYear? = memory[year] ?: copies[year]
    override suspend fun load(year: Int): Result<CalendarYearLoad> {
        loadCalls += year
        gates[year]?.await()
        val result = loads[year] ?: error("no scripted load for $year")
        result.getOrNull()?.takeIf { !it.mayBeOutOfDate }?.let { memory[year] = it.year }
        return result
    }
}

internal fun network(year: CalendarYear, outOfDate: Boolean = false): Result<CalendarYearLoad> =
    Result.success(CalendarYearLoad(year, if (outOfDate) CalendarYearSource.DeviceCopy else CalendarYearSource.Network, outOfDate))
