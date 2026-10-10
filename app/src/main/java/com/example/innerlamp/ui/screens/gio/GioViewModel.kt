package com.example.innerlamp.ui.screens.gio

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.calendar.MemorialSchedule
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import com.example.innerlamp.domain.usecase.GetMemorialEntriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * ViewModel dùng chung cho "Sổ giỗ chạp" và "Thêm/sửa ngày giỗ" — hai màn nằm trên cùng
 * một back stack entry của `GioRoute` nên cùng thấy một danh sách và một bản nháp.
 *
 * - Danh sách người thân hiện giữ trong bộ nhớ (seed từ [GetMemorialEntriesUseCase]); chưa lưu
 *   bền — xem `Docs/gio_assumptions.md`.
 * - Thông báo [GioUiState.flash] được màn hình tự ẩn sau vài giây qua [dismissFlash].
 *
 * @param today nguồn "hôm nay" (tiêm vào để test, mặc định là ngày hệ thống).
 */
class GioViewModel(
    getEntries: GetMemorialEntriesUseCase = GetMemorialEntriesUseCase(),
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private var entries: List<MemorialEntry> = getEntries()

    private val _uiState = MutableStateFlow(GioUiState(items = buildItems()))
    val uiState: StateFlow<GioUiState> = _uiState.asStateFlow()

    /** Mở form thêm mới với giá trị mặc định (khớp prototype). */
    fun startNew() {
        val entry = MemorialEntry(
            id = UUID.randomUUID().toString(),
            name = "",
            relation = MemorialRelation.PaternalGrandfather,
            calendar = MemorialCalendarType.Lunar,
            day = 1,
            month = 1,
            deathYear = null,
            note = "",
            remind = true,
            leadDays = 1,
            remindTime = LocalTime.of(7, 0),
            syncReminders = true,
        )
        openDraft(entry, isNew = true)
    }

    /** Mở form sửa cho người có [id]; bỏ qua nếu không còn trong sổ. */
    fun startEdit(id: String) {
        entries.firstOrNull { it.id == id }?.let { openDraft(it, isNew = false) }
    }

    /** Bỏ bản nháp (back khỏi form mà không lưu). */
    fun discardDraft() {
        _uiState.update { it.copy(draft = null) }
    }

    fun onNameChange(name: String) = edit(refreshPreview = false) { it.copy(name = name) }

    fun onRelationChange(relation: MemorialRelation) = edit(refreshPreview = false) { it.copy(relation = relation) }

    /** Đổi âm/dương; ngày được kẹp lại cho hợp lệ (âm ≤ 30, dương ≤ 31). */
    fun onCalendarChange(calendar: MemorialCalendarType) = edit(refreshPreview = true) {
        val maxDay = if (calendar == MemorialCalendarType.Lunar) 30 else 31
        it.copy(calendar = calendar, day = minOf(it.day, maxDay))
    }

    fun onDayChange(day: Int) = edit(refreshPreview = true) { it.copy(day = day) }

    fun onMonthChange(month: Int) = edit(refreshPreview = true) { it.copy(month = month) }

    /** [year] = `null` ↔ "Không rõ". */
    fun onDeathYearChange(year: Int?) = edit(refreshPreview = true) { it.copy(deathYear = year) }

    fun onNoteChange(note: String) = edit(refreshPreview = false) { it.copy(note = note) }

    fun onRemindToggle() = edit(refreshPreview = false) { it.copy(remind = !it.remind) }

    fun onLeadDaysChange(days: Int) = edit(refreshPreview = false) { it.copy(leadDays = days) }

    fun onRemindTimeChange(time: LocalTime) = edit(refreshPreview = false) { it.copy(remindTime = time) }

    fun onSyncToggle() = edit(refreshPreview = false) { it.copy(syncReminders = !it.syncReminders) }

    /**
     * Lưu bản nháp vào sổ. Trả `true` nếu đã lưu (màn hình quay lại danh sách); `false`
     * nếu tên trống — khi đó bật lỗi dưới ô tên và giữ nguyên form.
     */
    fun save(): Boolean {
        val draft = _uiState.value.draft ?: return false
        val entry = draft.entry.copy(name = draft.entry.name.trim())
        if (entry.name.isEmpty()) {
            _uiState.update { it.copy(draft = draft.copy(showNameError = true)) }
            return false
        }
        entries = if (draft.isNew) entries + entry else entries.map { if (it.id == entry.id) entry else it }
        publish(flash = "Đã lưu vào sổ giỗ.")
        return true
    }

    /** Xóa người đang sửa khỏi sổ. Không làm gì với bản nháp mới (chưa có trong sổ). */
    fun delete() {
        val draft = _uiState.value.draft ?: return
        if (draft.isNew) return
        entries = entries.filterNot { it.id == draft.entry.id }
        val label = draft.entry.name.ifBlank { "người thân" }
        publish(flash = "Đã xóa $label khỏi sổ giỗ.")
    }

    fun dismissFlash() {
        _uiState.update { if (it.flash.isEmpty()) it else it.copy(flash = "") }
    }

    private fun openDraft(entry: MemorialEntry, isNew: Boolean) {
        val draft = GioDraft(entry = entry, isNew = isNew, preview = previewOf(entry))
        _uiState.update { it.copy(draft = draft, flash = "") }
    }

    private fun edit(refreshPreview: Boolean, transform: (MemorialEntry) -> MemorialEntry) {
        _uiState.update { state ->
            val draft = state.draft ?: return@update state
            val entry = transform(draft.entry)
            state.copy(
                draft = draft.copy(
                    entry = entry,
                    // Xóa lỗi tên ngay khi người dùng sửa bất kỳ trường nào (khớp prototype).
                    showNameError = false,
                    preview = if (refreshPreview) previewOf(entry) else draft.preview,
                ),
            )
        }
    }

    private fun publish(flash: String) {
        _uiState.update { GioUiState(items = buildItems(), flash = flash, draft = null) }
    }

    private fun previewOf(entry: MemorialEntry): GioPreview = GioFormat.preview(entry, today())

    private fun buildItems(): List<GioItem> {
        val now = today()
        return entries
            .map { GioItem(it, MemorialSchedule.next(it, now)) }
            .sortedBy { it.occurrence.daysUntil }
    }
}
