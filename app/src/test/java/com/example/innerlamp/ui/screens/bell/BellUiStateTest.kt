package com.example.innerlamp.ui.screens.bell

import com.example.innerlamp.domain.model.BellMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BellUiStateTest {

    @Test
    fun subtitle_defaultShowsPresetLabel() = assertEquals("Tụng kinh", BellUiState().subtitle)

    @Test
    fun subtitle_runningAppendsTrangThai() =
        assertEquals("Tụng kinh · đang giữ nhịp", BellUiState(running = true).subtitle)

    @Test
    fun subtitle_rollingAppendsDonMo() =
        assertEquals("Tụng kinh · dồn mõ", BellUiState(rolling = true).subtitle)

    @Test
    fun subtitle_customShowsModeLabel() =
        assertEquals("Mõ + chuông", BellUiState(presetIndex = -1).subtitle)

    @Test
    fun rateLabel_chuongUsesKhoangCach() =
        assertEquals("Chuông tự động · khoảng cách", BellUiState(mode = BellMode.Chuong).rateLabel)

    @Test
    fun rateLabel_moUsesNhip() =
        assertEquals("Mõ tự động · nhịp", BellUiState(mode = BellMode.Mo).rateLabel)

    @Test
    fun rateValue_moUsesBpm() = assertEquals("72 nhịp/phút", BellUiState(bpm = 72).rateValue)

    @Test
    fun rateValue_chuongUsesGap() =
        assertEquals("12 giây một tiếng", BellUiState(mode = BellMode.Chuong, gapSeconds = 12).rateValue)

    @Test
    fun padNumber_chuongShowsChuongCount() =
        assertEquals(3, BellUiState(mode = BellMode.Chuong, chuongCount = 3).padNumber)

    @Test
    fun padNumber_moShowsMoCount() =
        assertEquals(5, BellUiState(mode = BellMode.Mo, moCount = 5).padNumber)

    @Test
    fun padTag_chuongIsChuong() = assertEquals("CHUÔNG", BellUiState(mode = BellMode.Chuong).padTag)

    @Test
    fun padHint_idleMo() = assertEquals("Chạm để gõ", BellUiState().padHint)

    @Test
    fun padHint_idleChuong() =
        assertEquals("Chạm để thỉnh chuông", BellUiState(mode = BellMode.Chuong).padHint)

    @Test
    fun padHint_running() = assertEquals("Đang giữ nhịp", BellUiState(running = true).padHint)

    @Test
    fun padHint_rolling() = assertEquals("Đang dồn mõ…", BellUiState(rolling = true).padHint)

    @Test
    fun leftLabel_moIsDonMo() = assertEquals("Dồn mõ", BellUiState().leftLabel)

    @Test
    fun leftLabel_chuongIsDatLai() =
        assertEquals("Đặt lại", BellUiState(mode = BellMode.Chuong).leftLabel)

    @Test
    fun showEvery_onlyMoVaChuong() {
        assertTrue(BellUiState(mode = BellMode.MoVaChuong).showEvery)
        assertFalse(BellUiState(mode = BellMode.Mo).showEvery)
        assertFalse(BellUiState(mode = BellMode.Chuong).showEvery)
    }

    @Test
    fun everyValue_mapsIndex() = assertEquals(27, BellUiState(everyIndex = 2).everyValue)
}
