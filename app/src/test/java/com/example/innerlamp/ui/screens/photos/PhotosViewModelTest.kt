package com.example.innerlamp.ui.screens.photos

import com.example.innerlamp.domain.model.ContentError
import com.example.innerlamp.domain.model.HomePhotos
import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.model.PhotoCredit
import com.example.innerlamp.domain.model.TemplePhoto
import com.example.innerlamp.domain.repository.GalleryRepository
import com.example.innerlamp.domain.usecase.GetPhotoCreditUseCase
import com.example.innerlamp.domain.usecase.GetTemplePhotosUseCase
import com.example.innerlamp.ui.components.ContentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeGalleryRepository(
        var photos: Result<List<TemplePhoto>>,
        var credit: Result<PhotoCredit> = Result.success(PhotoCredit("Nguyễn A", "CC BY-SA 4.0", null)),
    ) : GalleryRepository {
        var listCalls = 0
        override suspend fun getAll(): Result<List<TemplePhoto>> = photos.also { listCalls++ }
        override suspend fun getHomePhotos(count: Int): Result<HomePhotos> = error("not used")
        override suspend fun getCredit(id: String): Result<PhotoCredit> = credit
    }

    /** 4 Chùa, 3 Tượng Phật, 3 Cảnh thiền, 2 Lễ hội. */
    private val sample: List<TemplePhoto> = listOf(
        PhotoCategory.Temple to 4,
        PhotoCategory.BuddhaStatue to 3,
        PhotoCategory.Meditation to 3,
        PhotoCategory.Festival to 2,
    ).flatMap { (category, n) -> List(n) { category } }
        .mapIndexed { i, category -> TemplePhoto("p$i", category, "Ảnh $i", "Nơi $i", 180) }

    private fun viewModel(repo: FakeGalleryRepository = FakeGalleryRepository(Result.success(sample))) =
        PhotosViewModel(GetTemplePhotosUseCase(repo), GetPhotoCreditUseCase(repo))

    @Test
    fun initial_load_shows_all_photos() {
        val s = viewModel().uiState.value
        assertEquals(ContentStatus.Loaded, s.status)
        assertEquals(null, s.selectedCategory)
        assertEquals(12, s.filteredPhotos.size)
        assertEquals("12 ảnh", s.countLabel)
    }

    @Test
    fun selecting_category_filters_and_updates_count_label() {
        val vm = viewModel()
        vm.onCategorySelected(PhotoCategory.Temple)
        val s = vm.uiState.value
        assertTrue(s.filteredPhotos.all { it.category == PhotoCategory.Temple })
        assertEquals("4 ảnh · Chùa", s.countLabel)
        vm.onCategorySelected(null)
        assertEquals("12 ảnh", vm.uiState.value.countLabel)
    }

    @Test
    fun offline_then_retry_recovers() {
        val repo = FakeGalleryRepository(Result.failure(ContentError.Offline))
        val vm = viewModel(repo)
        assertEquals(ContentStatus.Offline, vm.uiState.value.status)
        repo.photos = Result.success(sample)
        vm.onRetry()
        assertEquals(ContentStatus.Loaded, vm.uiState.value.status)
        assertEquals(2, repo.listCalls)
    }

    @Test
    fun opening_a_photo_loads_its_credit_and_closing_clears_it() {
        val vm = viewModel()
        vm.onPhotoOpened(sample[0])
        assertEquals(sample[0], vm.uiState.value.viewing)
        assertEquals("Ảnh: Nguyễn A · CC BY-SA 4.0", vm.uiState.value.creditLine)
        vm.onLightboxDismissed()
        assertNull(vm.uiState.value.viewing)
        assertNull(vm.uiState.value.creditLine)
    }

    @Test
    fun a_failed_credit_keeps_the_photo_open_without_credit() {
        val repo = FakeGalleryRepository(Result.success(sample), credit = Result.failure(ContentError.Failed))
        val vm = viewModel(repo)
        vm.onPhotoOpened(sample[1])
        assertEquals(sample[1], vm.uiState.value.viewing)
        assertNull(vm.uiState.value.creditLine)
    }
}
