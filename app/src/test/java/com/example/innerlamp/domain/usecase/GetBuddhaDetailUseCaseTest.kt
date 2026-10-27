package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.model.BuddhaViaDay
import com.example.innerlamp.domain.model.ContentError
import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.model.DeityType
import com.example.innerlamp.domain.repository.DeityRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GetBuddhaDetailUseCaseTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private fun clockAt(date: LocalDate) = Clock.fixed(date.atTime(12, 0).atZone(zone).toInstant(), zone)

    private class FakeDeityRepository(private val profile: Result<BuddhaProfile>) : DeityRepository {
        override suspend fun getFeatured(): Result<List<DeitySummary>> = Result.success(emptyList())
        override suspend fun getAll(): Result<List<DeitySummary>> = Result.success(emptyList())
        override suspend fun getProfile(id: String): Result<BuddhaProfile> = profile
    }

    private fun profile(vararg via: BuddhaViaDay) = BuddhaProfile(
        id = "quanam", name = "Quán Thế Âm Bồ Tát", shortName = "Quán Thế Âm", sanskrit = null,
        type = DeityType.Bodhisattva, kindLabel = "Bồ Tát", realm = null, role = null, imageUrl = null,
        viaDays = via.toList(), bio = null, canonicalSource = null, vows = emptyList(), mantras = emptyList(),
        sutras = emptyList(),
    )

    @Test
    fun next_via_falls_on_the_main_lunar_day_from_today() = runTest {
        val today = LocalDate.of(2026, 10, 8)
        val useCase = GetBuddhaDetailUseCase(
            FakeDeityRepository(Result.success(profile(BuddhaViaDay(19, 2, "Đản sinh"), BuddhaViaDay(19, 6, "Thành đạo")))),
            clockAt(today),
        )
        val next = useCase("quanam").getOrThrow().nextVia
        assertNotNull(next)
        assertFalse(next!!.isBefore(today))
        val lunar = LunarCalendar.toLunar(next)
        assertEquals(19, lunar.day)
        assertEquals(2, lunar.month)
        assertFalse(lunar.isLeapMonth)
    }

    @Test
    fun no_via_day_gives_no_next_via() = runTest {
        val useCase = GetBuddhaDetailUseCase(FakeDeityRepository(Result.success(profile())), clockAt(LocalDate.of(2026, 10, 8)))
        assertNull(useCase("quanam").getOrThrow().nextVia)
    }

    @Test
    fun repository_failure_is_passed_through() = runTest {
        val useCase = GetBuddhaDetailUseCase(FakeDeityRepository(Result.failure(ContentError.NotFound)))
        assertEquals(ContentError.NotFound, useCase("x").exceptionOrNull())
    }
}
