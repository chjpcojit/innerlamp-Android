package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.domain.model.BuddhaCategory
import com.example.innerlamp.ui.screens.home.HomeSampleData
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetBuddhaDetailUseCaseTest {

    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    private fun clockAt(date: LocalDate) = Clock.fixed(date.atTime(12, 0).atZone(zone).toInstant(), zone)

    @Test
    fun profilesHaveUniqueIdsAndBothCategories() {
        val profiles = GetBuddhaProfilesUseCase()()
        assertEquals(9, profiles.size)
        assertEquals(profiles.size, profiles.map { it.id }.toSet().size)
        assertEquals(4, profiles.count { it.category == BuddhaCategory.Buddha })
        assertEquals(5, profiles.count { it.category == BuddhaCategory.Bodhisattva })
    }

    @Test
    fun everyProfileHasContentForEachSection() {
        GetBuddhaProfilesUseCase()().forEach { p ->
            assertTrue("${p.id} vows", p.vows.isNotEmpty())
            assertTrue("${p.id} mantras", p.mantras.isNotEmpty())
            assertTrue("${p.id} sutras", p.sutras.isNotEmpty())
        }
    }

    @Test
    fun homeCarouselMatchesProfiles() {
        val profiles = GetBuddhaProfilesUseCase()().associateBy { it.id }
        HomeSampleData.buddhas.forEach { figure ->
            val profile = profiles.getValue(figure.id)
            assertEquals(figure.viaLabel, profile.viaLabel)
            assertEquals(figure.kind, profile.kindLabel)
        }
    }

    @Test
    fun nextViaFallsOnTheViaLunarDayWithinSearchWindow() {
        val today = LocalDate.of(2026, 10, 8)
        val useCase = GetBuddhaDetailUseCase(clock = clockAt(today))
        GetBuddhaProfilesUseCase()().forEach { p ->
            val next = useCase(p.id).nextVia
            assertNotNull("${p.id} nextVia", next)
            assertTrue(!next!!.isBefore(today))
            val lunar = LunarCalendar.toLunar(next)
            assertEquals(p.viaDay, lunar.day)
            assertEquals(p.viaMonth, lunar.month)
        }
    }

    @Test
    fun unknownIdFallsBackToFirstProfile() {
        val detail = GetBuddhaDetailUseCase(clock = clockAt(LocalDate.of(2026, 10, 8)))("khong-co")
        assertEquals("thichca", detail.profile.id)
    }
}
