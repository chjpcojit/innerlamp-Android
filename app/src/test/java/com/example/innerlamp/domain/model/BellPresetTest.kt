package com.example.innerlamp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BellPresetTest {

    @Test
    fun presets_haveFiveEntries() = assertEquals(5, BellPresets.all.size)

    @Test
    fun everyOptions_matchPrototype() = assertEquals(listOf(0, 10, 27, 108), BellEveryOptions.all)

    @Test
    fun everyLabel_zeroIsTat() = assertEquals("Tắt", BellEveryOptions.label(0))

    @Test
    fun everyLabel_otherIsNumber() = assertEquals("108", BellEveryOptions.label(108))

    @Test
    fun interval_chuongUsesGapSeconds() =
        assertEquals(12_000L, BellMode.Chuong.intervalMillis(bpm = 72, gapSeconds = 12))

    @Test
    fun interval_moUsesBpm() =
        assertEquals(500L, BellMode.Mo.intervalMillis(bpm = 120, gapSeconds = 99))

    @Test
    fun interval_moVaChuongUsesBpm() =
        assertEquals(1000L, BellMode.MoVaChuong.intervalMillis(bpm = 60, gapSeconds = 5))

    @Test
    fun preset_everyIndexPointsIntoEveryOptions() {
        BellPresets.all.forEach { preset ->
            preset.everyIndex?.let { index ->
                assert(index in BellEveryOptions.all.indices) {
                    "preset ${preset.label} có everyIndex ngoài khoảng mốc"
                }
            }
        }
    }

    @Test
    fun roll_startsSlowSpeedsUpThenSlowsToTwoLongBeats() {
        val gaps = BellRoll.gapsMillis
        assertEquals(950L, gaps.first())
        assertEquals(listOf(950L, 1100L), gaps.takeLast(2))
        assertEquals(14, gaps.count { it == 160L })
    }

    @Test
    fun rollStrike_isCappedAndScaledByGap() {
        assertEquals(260, BellRoll.strikeMillis(950))
        assertEquals(136, BellRoll.strikeMillis(160))
    }
}
