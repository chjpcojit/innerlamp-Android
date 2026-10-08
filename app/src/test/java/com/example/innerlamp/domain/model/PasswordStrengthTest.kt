package com.example.innerlamp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordStrengthTest {

    @Test
    fun empty_isNone() = assertEquals(PasswordStrength.None, PasswordStrength.of(""))

    @Test
    fun shorterThanMinimum_isWeakEvenWhenMixed() =
        assertEquals(PasswordStrength.Weak, PasswordStrength.of("aB3$"))

    @Test
    fun minimumLengthSingleGroup_isWeak() =
        assertEquals(PasswordStrength.Weak, PasswordStrength.of("abcdefgh"))

    @Test
    fun minimumLengthTwoGroups_isFair() =
        assertEquals(PasswordStrength.Fair, PasswordStrength.of("matkhau1"))

    @Test
    fun longWithThreeGroups_isStrong() =
        assertEquals(PasswordStrength.Strong, PasswordStrength.of("MatKhau2026xx"))

    @Test
    fun longButTwoGroups_isFair() =
        assertEquals(PasswordStrength.Fair, PasswordStrength.of("matkhaudailam12"))

    @Test
    fun segmentsMatchLevel() {
        assertEquals(0, PasswordStrength.None.segments)
        assertEquals(1, PasswordStrength.Weak.segments)
        assertEquals(2, PasswordStrength.Fair.segments)
        assertEquals(3, PasswordStrength.Strong.segments)
    }
}
