package com.example.innerlamp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneCountryTest {

    private val vn = PhoneCountries.byIso("VN")

    @Test
    fun nationalDigits_stripsLeadingZeroAndDialCode() {
        assertEquals("912345678", vn.nationalDigits("0912 345 678"))
        assertEquals("912345678", vn.nationalDigits("912345678"))
        assertEquals("912345678", vn.nationalDigits("+84 912 345 678"))
        assertEquals("912345678", vn.nationalDigits("0084912345678"))
    }

    @Test
    fun nationalDigits_doesNotStripDialPrefixOfShortNumber() {
        // "84" đầu số nhưng chưa vượt độ dài → coi là số quốc gia đang gõ dở.
        assertEquals("84912", vn.nationalDigits("84912"))
    }

    @Test
    fun isComplete_dependsOnCountryLength() {
        assertFalse(vn.isComplete("0912"))
        assertTrue(vn.isComplete("0912 345 678"))
        assertTrue(PhoneCountries.byIso("SG").isComplete("9123 4567"))
        assertFalse(PhoneCountries.byIso("US").isComplete("912 345 678"))
    }

    @Test
    fun formatE164_groupsByThree() {
        assertEquals("+84 912 345 678", vn.formatE164("0912 345 678"))
        assertEquals("+84", vn.formatE164(""))
    }

    @Test
    fun search_matchesNameWithoutDiacriticsDialAndIso() {
        assertEquals(listOf("VN"), PhoneCountries.search("viet").map { it.iso })
        assertEquals(listOf("VN"), PhoneCountries.search("+84").map { it.iso })
        assertEquals(listOf("DE"), PhoneCountries.search("duc").map { it.iso })
        assertEquals(listOf("US", "CA"), PhoneCountries.search("+1").map { it.iso })
        assertEquals(PhoneCountries.all, PhoneCountries.search("  "))
        assertTrue(PhoneCountries.search("zzz").isEmpty())
    }
}
