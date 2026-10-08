package com.example.innerlamp.ui.screens.mala

import org.junit.Assert.assertEquals
import org.junit.Test

class MalaUiStateTest {

    @Test
    fun roundsLabel_defaultIsOneTrang() = assertEquals("1 tràng", MalaUiState().roundsLabel)

    @Test
    fun roundsLabel_reflectsRound() = assertEquals("3 tràng", MalaUiState(round = 3).roundsLabel)

    @Test
    fun progress_defaultIsZero() = assertEquals(0f, MalaUiState().progress, 0f)

    @Test
    fun progress_matchesBeads() = assertEquals(0.25f, MalaUiState(beads = 27).progress, 1e-4f)
}
