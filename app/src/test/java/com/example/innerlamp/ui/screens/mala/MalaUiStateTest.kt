package com.example.innerlamp.ui.screens.mala

import org.junit.Assert.assertEquals
import org.junit.Test

class MalaUiStateTest {

    @Test
    fun roundsLabel_defaultIsRoundOneZero() = assertEquals("Vòng 1 · biến 0 / 108", MalaUiState().roundsLabel)

    @Test
    fun roundsLabel_reflectsRoundBeadsAndTarget() =
        assertEquals("Vòng 3 · biến 5 / 21", MalaUiState(round = 3, beads = 5, target = 21).roundsLabel)

    @Test
    fun count_atTargetStaysAtTarget() = assertEquals(54, MalaUiState(beads = 54, target = 54).count)

    @Test
    fun progress_defaultIsZero() = assertEquals(0f, MalaUiState().progress, 0f)

    @Test
    fun progress_matchesBeadsOverTarget() {
        assertEquals(0.25f, MalaUiState(beads = 27).progress, 1e-4f)
        assertEquals(0.5f, MalaUiState(beads = 27, target = 54).progress, 1e-4f)
        assertEquals(1f, MalaUiState(beads = 21, target = 21).progress, 0f)
    }

    @Test
    fun mantra_defaultsToDiDa() = assertEquals("Nam mô A Di Đà Phật", MalaUiState().mantra)
}
