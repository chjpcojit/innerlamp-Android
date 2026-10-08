package com.example.innerlamp.ui.screens.talk

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TalkViewModelTest {

    private fun viewModel() = TalkViewModel(SavedStateHandle())

    @Test
    fun initial_state_loads_talk_and_is_playing() {
        val s = viewModel().uiState.value
        assertEquals("tl-hieu-la-dao", s.talk.id)
        assertEquals("Hiếu là đạo,\nđạo cũng là hiếu", s.talk.title)
        assertEquals("Thầy Minh Đạo", s.talk.speaker)
        assertEquals(3, s.talk.chapters.size)
        assertEquals("00:00", s.talk.chapters.first().timestamp)
        assertTrue(s.isPlaying)
    }

    @Test
    fun toggle_play_flips_is_playing() {
        val vm = viewModel()
        assertTrue(vm.uiState.value.isPlaying)
        vm.togglePlay()
        assertFalse(vm.uiState.value.isPlaying)
        vm.togglePlay()
        assertTrue(vm.uiState.value.isPlaying)
    }
}
