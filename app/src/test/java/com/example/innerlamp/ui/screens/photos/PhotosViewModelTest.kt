package com.example.innerlamp.ui.screens.photos

import com.example.innerlamp.domain.model.PhotoCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotosViewModelTest {

    private fun viewModel() = PhotosViewModel()

    @Test
    fun initial_state_shows_all_photos() {
        val s = viewModel().uiState.value
        assertEquals(null, s.selectedCategory)
        assertEquals(12, s.photos.size)
        assertEquals(12, s.filteredPhotos.size)
        assertEquals("12 ảnh", s.countLabel)
    }

    @Test
    fun selecting_category_filters_and_updates_count_label() {
        val vm = viewModel()
        vm.onCategorySelected(PhotoCategory.Temple)
        val s = vm.uiState.value
        assertEquals(PhotoCategory.Temple, s.selectedCategory)
        assertTrue(s.filteredPhotos.all { it.category == PhotoCategory.Temple })
        assertEquals(4, s.filteredPhotos.size)
        assertEquals("4 ảnh · Chùa", s.countLabel)
    }

    @Test
    fun selecting_all_clears_filter() {
        val vm = viewModel()
        vm.onCategorySelected(PhotoCategory.Festival)
        vm.onCategorySelected(null)
        val s = vm.uiState.value
        assertEquals(null, s.selectedCategory)
        assertEquals(12, s.filteredPhotos.size)
        assertEquals("12 ảnh", s.countLabel)
    }

    @Test
    fun reselecting_same_category_is_noop() {
        val vm = viewModel()
        vm.onCategorySelected(PhotoCategory.Meditation)
        vm.onCategorySelected(PhotoCategory.Meditation)
        assertEquals(PhotoCategory.Meditation, vm.uiState.value.selectedCategory)
        assertEquals(3, vm.uiState.value.filteredPhotos.size)
    }
}
