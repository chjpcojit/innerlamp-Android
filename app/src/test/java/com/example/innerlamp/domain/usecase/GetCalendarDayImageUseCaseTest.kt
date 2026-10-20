package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.CalendarDayImage
import com.example.innerlamp.domain.repository.CalendarImageError
import com.example.innerlamp.domain.repository.CalendarImageRepository
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCalendarDayImageUseCaseTest {

    private class FakeRepo : CalendarImageRepository {
        val requested = mutableListOf<LocalDate>()
        override suspend fun load(date: LocalDate): Result<CalendarDayImage> {
            requested += date
            return Result.success(
                CalendarDayImage(date, "1", "https://img/x.jpg", "A", "CC BY 4.0", null, null),
            )
        }
    }

    private val repo = FakeRepo()
    private val useCase = GetCalendarDayImageUseCase(repo)

    @Test
    fun inRange_delegatesToRepository() = runTest {
        val date = LocalDate.of(2026, 10, 20)
        assertEquals(date, useCase(date).getOrThrow().date)
        assertEquals(listOf(date), repo.requested)
    }

    @Test
    fun boundaryYears_areAccepted() = runTest {
        assertTrue(useCase(LocalDate.of(1900, 1, 1)).isSuccess)
        assertTrue(useCase(LocalDate.of(2100, 12, 31)).isSuccess)
    }

    @Test
    fun outsideRange_failsLocallyWithoutCall() = runTest {
        val low = useCase(LocalDate.of(1899, 12, 31))
        val high = useCase(LocalDate.of(2101, 1, 1))
        assertEquals(CalendarImageError.InvalidDate, low.exceptionOrNull())
        assertEquals(CalendarImageError.InvalidDate, high.exceptionOrNull())
        assertTrue(repo.requested.isEmpty())
    }

    @Test
    fun attribution_flag() {
        val withAuthor = CalendarDayImage(LocalDate.now(), "1", "u", "A", "", null, null)
        val none = CalendarDayImage(LocalDate.now(), "1", "u", "", " ", null, null)
        assertTrue(withAuthor.hasAttribution)
        assertEquals(false, none.hasAttribution)
    }
}
