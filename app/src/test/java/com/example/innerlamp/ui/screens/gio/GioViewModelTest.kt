package com.example.innerlamp.ui.screens.gio

import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialRelation
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GioViewModelTest {

    private val today = LocalDate.of(2026, 10, 9)

    private fun viewModel() = GioViewModel(today = { today })

    @Test
    fun initial_items_are_sorted_by_nearest_memorial() {
        val s = viewModel().uiState.value
        assertEquals(3, s.items.size)
        assertEquals("3 người thân", s.subtitle)
        assertEquals(s.items.map { it.occurrence.daysUntil }.sorted(), s.items.map { it.occurrence.daysUntil })
        assertEquals(s.items.first(), s.nextUp)
        assertNull(s.draft)
    }

    @Test
    fun start_new_opens_default_draft_with_preview() {
        val vm = viewModel()
        vm.startNew()
        val draft = vm.uiState.value.draft!!
        assertTrue(draft.isNew)
        assertEquals("", draft.entry.name)
        assertEquals(MemorialCalendarType.Lunar, draft.entry.calendar)
        assertNotNull(draft.preview)
    }

    @Test
    fun save_with_blank_name_shows_error_and_keeps_draft() {
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("   ")
        assertFalse(vm.save())
        val s = vm.uiState.value
        assertTrue(s.draft!!.showNameError)
        assertEquals(3, s.items.size)
    }

    @Test
    fun editing_any_field_clears_name_error() {
        val vm = viewModel()
        vm.startNew()
        vm.save()
        vm.onNameChange("Nguyễn Văn An")
        assertFalse(vm.uiState.value.draft!!.showNameError)
    }

    @Test
    fun save_new_entry_adds_it_trims_name_and_flashes() {
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("  Lê Văn An ")
        vm.onRelationChange(MemorialRelation.Father)
        assertTrue(vm.save())
        val s = vm.uiState.value
        assertEquals(4, s.items.size)
        assertEquals("Lê Văn An", s.items.first { it.entry.relation == MemorialRelation.Father && it.entry.name.startsWith("Lê") }.entry.name)
        assertEquals("Đã lưu vào sổ giỗ.", s.flash)
        assertNull(s.draft)
    }

    @Test
    fun edit_existing_entry_replaces_instead_of_adding() {
        val vm = viewModel()
        val id = vm.uiState.value.items.first().entry.id
        vm.startEdit(id)
        assertFalse(vm.uiState.value.draft!!.isNew)
        vm.onNoteChange("Ghi chú mới")
        assertTrue(vm.save())
        val s = vm.uiState.value
        assertEquals(3, s.items.size)
        assertEquals("Ghi chú mới", s.items.first { it.entry.id == id }.entry.note)
    }

    @Test
    fun delete_removes_entry_and_flashes_name() {
        val vm = viewModel()
        val target = vm.uiState.value.items.first().entry
        vm.startEdit(target.id)
        vm.delete()
        val s = vm.uiState.value
        assertEquals(2, s.items.size)
        assertTrue(s.items.none { it.entry.id == target.id })
        assertEquals("Đã xóa ${target.name} khỏi sổ giỗ.", s.flash)
    }

    @Test
    fun delete_does_nothing_for_unsaved_draft() {
        val vm = viewModel()
        vm.startNew()
        vm.delete()
        assertEquals(3, vm.uiState.value.items.size)
        assertNotNull(vm.uiState.value.draft)
    }

    @Test
    fun deleting_every_entry_gives_empty_book() {
        val vm = viewModel()
        repeat(3) {
            vm.startEdit(vm.uiState.value.items.first().entry.id)
            vm.delete()
        }
        val s = vm.uiState.value
        assertTrue(s.items.isEmpty())
        assertNull(s.nextUp)
        assertEquals("Chưa có ai", s.subtitle)
    }

    @Test
    fun switching_calendar_clamps_day_and_refreshes_preview() {
        val vm = viewModel()
        vm.startNew()
        vm.onCalendarChange(MemorialCalendarType.Solar)
        vm.onDayChange(31)
        vm.onMonthChange(12)
        val solarPreview = vm.uiState.value.draft!!.preview
        vm.onCalendarChange(MemorialCalendarType.Lunar)
        val draft = vm.uiState.value.draft!!
        assertEquals(30, draft.entry.day)
        assertTrue(draft.preview != solarPreview)
        assertTrue(draft.preview!!.detail.startsWith("Tức 30 tháng Chạp âm lịch"))
    }

    @Test
    fun discard_draft_and_dismiss_flash() {
        val vm = viewModel()
        vm.startNew()
        vm.onNameChange("Lê An")
        vm.save()
        assertEquals("Đã lưu vào sổ giỗ.", vm.uiState.value.flash)
        vm.dismissFlash()
        assertEquals("", vm.uiState.value.flash)

        vm.startNew()
        vm.discardDraft()
        assertNull(vm.uiState.value.draft)
    }

    @Test
    fun toggles_flip_reminder_flags() {
        val vm = viewModel()
        vm.startNew()
        assertTrue(vm.uiState.value.draft!!.entry.remind)
        vm.onRemindToggle()
        assertFalse(vm.uiState.value.draft!!.entry.remind)
        vm.onSyncToggle()
        assertFalse(vm.uiState.value.draft!!.entry.syncReminders)
    }
}
