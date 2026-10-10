package com.example.innerlamp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MalaTest {

    @Test
    fun advance_incrementsByOne() {
        assertEquals(1, Mala.advance(0))
        assertEquals(28, Mala.advance(27))
        assertEquals(108, Mala.advance(107))
    }

    @Test
    fun advance_wrapsToOneAfter108() {
        assertEquals(1, Mala.advance(108))
    }

    @Test
    fun progress_mapsBeadsToUnitRange() {
        assertEquals(0f, Mala.progress(0), 0f)
        assertEquals(0.5f, Mala.progress(54), 1e-4f)
        assertEquals(1f, Mala.progress(108), 0f)
    }

    @Test
    fun isMotherBead_every27() {
        assertTrue(Mala.isMotherBead(27))
        assertTrue(Mala.isMotherBead(54))
        assertTrue(Mala.isMotherBead(81))
        assertTrue(Mala.isMotherBead(108))
        assertFalse(Mala.isMotherBead(1))
        assertFalse(Mala.isMotherBead(28))
    }

    @Test
    fun isMotherBead_handlesNegativePosition() {
        // Vị trí "trước" hạt hiện tại cũng dùng được (chuỗi trượt hai chiều).
        assertTrue(Mala.isMotherBead(-27))
        assertTrue(Mala.isMotherBead(0))
        assertFalse(Mala.isMotherBead(-1))
    }

    @Test
    fun count_wrapsWithinTarget() {
        assertEquals(0, Mala.count(0, 21))
        assertEquals(5, Mala.count(5, 21))
        assertEquals(21, Mala.count(21, 21))
        assertEquals(1, Mala.count(22, 21))
    }

    @Test
    fun targets_are21_54_108() = assertEquals(listOf(21, 54, 108), Mala.TARGETS)
}
