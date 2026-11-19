package com.example.innerlamp.ui.screens.gio

import com.example.innerlamp.core.media.MemorialPhotoNormalizer
import com.example.innerlamp.core.media.NormalizedPhoto
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialError
import com.example.innerlamp.domain.model.MemorialLoad
import com.example.innerlamp.domain.model.MemorialRelation
import com.example.innerlamp.domain.reminder.MemorialReminderScheduler
import com.example.innerlamp.domain.repository.MemorialRepository
import com.example.innerlamp.domain.usecase.DeleteMemorialUseCase
import com.example.innerlamp.domain.usecase.LoadMemorialsUseCase
import com.example.innerlamp.domain.usecase.SaveMemorialUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GioViewModelTest {

    private val today = LocalDate.of(2026, 10, 14)

    private val load = mockk<LoadMemorialsUseCase>()
    private val save = mockk<SaveMemorialUseCase>()
    private val delete = mockk<DeleteMemorialUseCase>()
    private val scheduler = mockk<MemorialReminderScheduler>(relaxed = true)
    private val repository = mockk<MemorialRepository>(relaxed = true)
    private val photoNormalizer = mockk<MemorialPhotoNormalizer>(relaxed = true)

    private fun entry(id: Long, name: String, days: Int = 1) = MemorialEntry(
        id = id, name = name, relation = MemorialRelation.Father,
        calendar = MemorialCalendarType.Solar,
        day = today.plusDays(days.toLong()).dayOfMonth,
        month = today.plusDays(days.toLong()).monthValue,
        deathYear = 2020, note = "", remind = true, leadDays = 1, remindTime = LocalTime.of(7, 0),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { load.invoke() } returns MemorialLoad.Loaded(listOf(entry(2, "B", 9), entry(1, "A", 2)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = GioViewModel(
        loadMemorials = load,
        saveMemorial = save,
        deleteMemorial = delete,
        scheduler = scheduler,
        repository = repository,
        photoNormalizer = photoNormalizer,
        today = { today },
        vietnamToday = { today },
    )

    @Test
    fun initial_items_are_sorted_by_nearest_memorial() {
        val state = viewModel().uiState.value
        assertEquals(GioStatus.Ready, state.status)
        assertEquals(2, state.items.size)
        assertEquals("2 người thân", state.subtitle)
        assertEquals(state.items.map { it.occurrence.daysUntil }.sorted(), state.items.map { it.occurrence.daysUntil })
        assertEquals("A", state.items.first().entry.name)
        assertNull(state.draft)
    }

    @Test
    fun offline_load_shows_the_device_copy_with_a_banner() {
        coEvery { load.invoke() } returns MemorialLoad.Offline(listOf(entry(1, "A")), MemorialError.Offline)
        val state = viewModel().uiState.value
        assertEquals(GioStatus.Ready, state.status)
        assertEquals(1, state.items.size)
        assertEquals("Đang hiện bản lưu trên máy.", state.banner)
    }

    @Test
    fun guest_has_no_items_and_no_banner() {
        coEvery { load.invoke() } returns MemorialLoad.Guest
        val state = viewModel().uiState.value
        assertEquals(GioStatus.Guest, state.status)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun failed_load_without_a_copy_shows_the_retry_state() {
        coEvery { load.invoke() } returns MemorialLoad.Failed(MemorialError.Offline)
        val state = viewModel().uiState.value
        assertEquals(GioStatus.Failed, state.status)
        assertTrue(state.banner.contains("chưa có bản lưu"))
    }

    @Test
    fun start_new_uses_the_defaults_of_fr_mem_003() {
        val vm = viewModel()
        vm.startNew()
        val draft = vm.uiState.value.draft!!
        assertTrue(draft.isNew)
        assertEquals("", draft.entry.name)
        assertEquals(MemorialCalendarType.Lunar, draft.entry.calendar)
        assertTrue(draft.entry.remind)
        assertEquals(1, draft.entry.leadDays)
        assertEquals(LocalTime.of(7, 0), draft.entry.remindTime)
        assertFalse(draft.entry.leapMonth)
        assertNotNull(draft.preview)
    }

    @Test
    fun save_with_blank_name_shows_the_field_error_and_keeps_the_draft() {
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("   ")
        assertFalse(vm.save())
        val state = vm.uiState.value
        assertEquals("Vui lòng nhập họ và tên", state.draft!!.nameError)
        assertEquals(2, state.items.size)
    }

    @Test
    fun save_with_a_too_long_name_is_blocked_before_the_request() {
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("x".repeat(101))
        assertFalse(vm.save())
        assertEquals("Họ và tên tối đa 100 ký tự", vm.uiState.value.draft!!.nameError)
        coVerify(exactly = 0) { save.invoke(any()) }
    }

    @Test
    fun saving_a_new_entry_adds_it_and_shows_the_flash() {
        val created = entry(42, "Nguyễn Văn An", 5)
        coEvery { save.invoke(any()) } returns Result.success(created)
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("Nguyễn Văn An")
        assertTrue(vm.save())
        val state = vm.uiState.value
        assertEquals(3, state.items.size)
        assertEquals("Đã lưu vào sổ giỗ.", state.flash)
        assertNull(state.draft)
        coVerify { scheduler.sync(any()) }
    }

    @Test
    fun a_full_book_blocks_a_new_entry_before_the_request() {
        coEvery { load.invoke() } returns MemorialLoad.Loaded((1L..100L).map { entry(it, "N$it") })
        val vm = viewModel()
        vm.startNew()
        assertNull(vm.uiState.value.draft)
        assertEquals("Sổ giỗ đã đủ 100 người thân.", vm.uiState.value.flash)
    }

    @Test
    fun limit_reached_from_the_server_keeps_the_form() {
        coEvery { save.invoke(any()) } returns Result.failure(MemorialError.LimitReached)
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("A")
        vm.save()
        assertEquals("Sổ giỗ đã đủ 100 người thân.", vm.uiState.value.draft!!.formError)
        assertFalse(vm.uiState.value.draft!!.saving)
    }

    @Test
    fun a_401_while_saving_keeps_the_form_and_everything_typed() {
        coEvery { save.invoke(any()) } returns Result.failure(MemorialError.Unauthorized)
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("Nguyễn Văn An")
        vm.onNoteChange("Cúng chay, báo cậu Hai")
        vm.save()

        val state = vm.uiState.value
        val draft = state.draft!!
        assertEquals("Nguyễn Văn An", draft.entry.name)
        assertEquals("Cúng chay, báo cậu Hai", draft.entry.note)
        assertTrue(draft.sessionExpired)
        assertEquals("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", draft.formError)
        assertFalse(draft.saving)
        // The book and the device copy are untouched; only the user may leave the form.
        assertEquals(GioStatus.Ready, state.status)
        assertEquals(2, state.items.size)
        coVerify(exactly = 0) { repository.clearCache() }
        coVerify(exactly = 0) { scheduler.cancelAll() }
    }

    @Test
    fun offline_save_keeps_the_form_with_the_connection_message() {
        coEvery { save.invoke(any()) } returns Result.failure(MemorialError.Offline)
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("A")
        vm.save()
        assertEquals("Cần kết nối mạng để lưu thay đổi.", vm.uiState.value.draft!!.formError)
        assertEquals(2, vm.uiState.value.items.size)
    }

    @Test
    fun delete_asks_for_confirmation_then_removes_the_entry() {
        coEvery { delete.invoke(any(), any()) } returns Result.success(Unit)
        val vm = viewModel()
        vm.startEdit(1)
        vm.requestDelete()
        assertTrue(vm.uiState.value.confirmDelete)
        vm.confirmDelete()
        val state = vm.uiState.value
        assertEquals(1, state.items.size)
        assertTrue(state.flash.startsWith("Đã xóa"))
        assertNull(state.draft)
        coVerify { delete.invoke(1L, any()) }
    }

    @Test
    fun a_reminder_for_a_missing_entry_shows_the_gone_message() {
        val vm = viewModel()
        assertFalse(vm.openFromReminder(999))
        assertEquals("Mục này không còn trong sổ giỗ.", vm.uiState.value.flash)
    }

    @Test
    fun a_reminder_opens_the_entry_form() {
        val vm = viewModel()
        assertTrue(vm.openFromReminder(1))
        val draft = vm.uiState.value.draft!!
        assertEquals(1L, draft.entry.id)
        assertFalse(draft.isNew)
    }

    @Test
    fun uploading_a_photo_shows_it_in_the_form_and_the_book() {
        val photo = NormalizedPhoto(byteArrayOf(1, 2, 3), "image/jpeg", "photo.jpg")
        val saved = entry(1, "A").copy(photoUrl = "https://cdn.test/1.jpg")
        coEvery { photoNormalizer.normalize(any()) } returns Result.success(photo)
        coEvery { repository.uploadPhoto(1L, photo.bytes, "image/jpeg", "photo.jpg") } returns Result.success(saved)

        val vm = viewModel()
        vm.startEdit(1)
        vm.uploadPhoto(byteArrayOf(9, 9))

        val draft = vm.uiState.value.draft!!
        assertEquals("https://cdn.test/1.jpg", draft.entry.photoUrl)
        assertFalse(draft.saving)
        assertEquals("", draft.photoError)
        coVerify { repository.uploadPhoto(1L, photo.bytes, "image/jpeg", "photo.jpg") }
    }

    @Test
    fun a_413_photo_shows_the_size_message_and_keeps_the_form() {
        val photo = NormalizedPhoto(byteArrayOf(1), "image/jpeg", "photo.jpg")
        coEvery { photoNormalizer.normalize(any()) } returns Result.success(photo)
        coEvery { repository.uploadPhoto(any(), any(), any(), any()) } returns Result.failure(MemorialError.TooLarge)

        val vm = viewModel()
        vm.startEdit(1)
        vm.uploadPhoto(byteArrayOf(9))

        val draft = vm.uiState.value.draft!!
        assertEquals("Ảnh tối đa 5 MB.", draft.photoError)
        assertFalse(draft.saving)
        assertEquals(null, draft.entry.photoUrl)
    }

    @Test
    fun undecodable_bytes_never_reach_the_network() {
        coEvery { photoNormalizer.normalize(any()) } returns Result.failure(MemorialError.Validation("photo"))

        val vm = viewModel()
        vm.startEdit(1)
        vm.uploadPhoto(byteArrayOf(1, 2))

        val draft = vm.uiState.value.draft!!
        assertEquals("Không đọc được ảnh. Vui lòng chọn ảnh khác.", draft.photoError)
        coVerify(exactly = 0) { repository.uploadPhoto(any(), any(), any(), any()) }
    }

    @Test
    fun deleting_a_photo_returns_the_initials_avatar() {
        val withPhoto = entry(1, "A").copy(photoUrl = "https://cdn.test/1.jpg")
        val without = entry(1, "A").copy(photoUrl = null)
        coEvery { load.invoke() } returns MemorialLoad.Loaded(listOf(withPhoto, entry(2, "B", 9)))
        coEvery { repository.deletePhoto(1L) } returns Result.success(without)

        val vm = viewModel()
        vm.startEdit(1)
        vm.deletePhoto()

        assertEquals(null, vm.uiState.value.draft!!.entry.photoUrl)
        assertFalse(vm.uiState.value.draft!!.saving)
    }

    @Test
    fun changing_the_calendar_keeps_a_lunar_day_valid() {
        val vm = viewModel()
        vm.startNew()
        vm.onDayChange(30)
        vm.onCalendarChange(MemorialCalendarType.Solar)
        vm.onCalendarChange(MemorialCalendarType.Lunar)
        assertEquals(30, vm.uiState.value.draft!!.entry.day)
    }
}
