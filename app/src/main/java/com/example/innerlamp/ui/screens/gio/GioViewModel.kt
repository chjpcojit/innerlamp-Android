package com.example.innerlamp.ui.screens.gio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.core.media.MemorialPhotoNormalizer
import com.example.innerlamp.domain.calendar.MemorialSchedule
import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialDateRules
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialError
import com.example.innerlamp.domain.model.MemorialFormError
import com.example.innerlamp.domain.model.MemorialLoad
import com.example.innerlamp.domain.model.MemorialRelation
import com.example.innerlamp.domain.reminder.MemorialReminderScheduler
import com.example.innerlamp.domain.repository.MemorialRepository
import com.example.innerlamp.domain.usecase.DeleteMemorialUseCase
import com.example.innerlamp.domain.usecase.LoadMemorialsUseCase
import com.example.innerlamp.domain.usecase.SaveMemorialUseCase
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel dùng chung cho "Sổ giỗ chạp" và "Thêm/sửa ngày giỗ" — hai màn nằm trên cùng một back stack
 * entry của `GioRoute` nên cùng thấy một danh sách và một bản nháp.
 *
 * - Sổ đọc từ server theo phiên đăng nhập; máy giữ bản đọc để mở khi mất mạng (D5). Không xếp hàng ghi.
 * - Nhắc giỗ đặt trên máy qua [MemorialReminderScheduler] sau mỗi lần tải/lưu/xoá (D1).
 * - Mọi thay đổi state đi qua một `MutableStateFlow`.
 *
 * @param today nguồn "hôm nay" của máy (tiêm vào để test).
 * @param vietnamToday "hôm nay" theo giờ Việt Nam, dùng để chặn ngày mất tương lai (contract §6.4).
 */
class GioViewModel(
    private val loadMemorials: LoadMemorialsUseCase,
    private val saveMemorial: SaveMemorialUseCase,
    private val deleteMemorial: DeleteMemorialUseCase,
    private val scheduler: MemorialReminderScheduler,
    private val repository: MemorialRepository,
    private val photoNormalizer: MemorialPhotoNormalizer = MemorialPhotoNormalizer(),
    private val today: () -> LocalDate = { LocalDate.now() },
    private val vietnamToday: () -> LocalDate = { LocalDate.now(MemorialDateRules.vietnamZone) },
) : ViewModel() {

    private var entries: List<MemorialEntry> = emptyList()

    /** Id mở từ một nhắc giỗ; dùng sau khi sổ tải xong. / Entry opened from a reminder, applied after load. */
    private var pendingOpenId: Long? = null

    private var loading = false

    private val _uiState = MutableStateFlow(GioUiState())
    val uiState: StateFlow<GioUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Tải lại sổ (mở màn, thử lại, quay lại foreground khi đổi ngày). / Reloads the book. */
    fun refresh() {
        if (loading) return
        loading = true
        _uiState.update { if (it.items.isEmpty()) it.copy(status = GioStatus.Loading) else it }
        viewModelScope.launch {
            val load = try {
                loadMemorials()
            } finally {
                loading = false
            }
            when (load) {
                MemorialLoad.Guest -> _uiState.update {
                    it.copy(status = GioStatus.Guest, items = emptyList(), banner = "", draft = null)
                }
                is MemorialLoad.Loaded -> {
                    entries = load.entries
                    publish(flash = "", banner = "")
                    applyPendingOpen()
                }
                is MemorialLoad.Offline -> {
                    entries = load.entries
                    publish(flash = "", banner = OFFLINE_BANNER)
                    applyPendingOpen()
                }
                is MemorialLoad.Failed -> _uiState.update {
                    it.copy(
                        status = GioStatus.Failed,
                        items = emptyList(),
                        banner = if (load.error == MemorialError.Offline) {
                            "Không có kết nối và chưa có bản lưu trên máy."
                        } else {
                            LOAD_FAILED
                        },
                    )
                }
            }
        }
    }

    /** Recomputes the list for the current day and re-places reminders (new day or new time zone). */
    fun onForeground() {
        if (_uiState.value.status == GioStatus.Ready) {
            _uiState.update { it.copy(items = buildItems()) }
            viewModelScope.launch { scheduler.sync(entries) }
        }
    }

    /** Opens the form for a new entry with the defaults of FR-MEM-003. / Mở form thêm mới. */
    fun startNew() {
        if (entries.size >= MAX_ENTRIES) {
            _uiState.update { it.copy(flash = BOOK_FULL) }
            return
        }
        openDraft(MemorialDateRules.newEntry(), isNew = true)
    }

    /** Opens the form of [id]; ignored when the entry is no longer in the book. / Mở form sửa. */
    fun startEdit(id: Long) {
        entries.firstOrNull { it.id == id }?.let { openDraft(it, isNew = false) }
    }

    /**
     * Opens the entry behind a reminder. Called when the screen shows; when the book is still loading the
     * request is kept and applied after the load. Returns `true` when a draft was opened. /
     * Mở mục từ nhắc giỗ. Sổ chưa tải xong thì giữ lại và áp sau khi tải. Trả `true` nếu đã mở form.
     */
    fun openFromReminder(id: Long): Boolean {
        val entry = entries.firstOrNull { it.id == id }
        if (_uiState.value.status == GioStatus.Ready) {
            if (entry != null) {
                openDraft(entry, isNew = false)
                return true
            }
            _uiState.update { it.copy(flash = GONE) }
            return false
        }
        pendingOpenId = id
        return false
    }

    private fun applyPendingOpen() {
        val id = pendingOpenId ?: return
        pendingOpenId = null
        val entry = entries.firstOrNull { it.id == id }
        if (entry != null) {
            openDraft(entry, isNew = false)
        } else {
            _uiState.update { it.copy(flash = GONE) }
        }
    }

    /** Bỏ bản nháp (back khỏi form mà không lưu). */
    fun discardDraft() {
        _uiState.update { it.copy(draft = null, confirmDelete = false) }
    }

    fun onNameChange(name: String) = editDraft { it.copy(entry = it.entry.copy(name = name), nameError = "") }

    fun onRelationChange(relation: MemorialRelation) = editDraft { it.copy(entry = it.entry.copy(relation = relation)) }

    /** Đổi âm/dương; ngày được kẹp lại cho hợp lệ (âm ≤ 30, dương ≤ 31). */
    fun onCalendarChange(calendar: MemorialCalendarType) = editDraft(refreshPreview = true) {
        val maxDay = if (calendar == MemorialCalendarType.Lunar) 30 else 31
        val leap = it.entry.leapMonth && calendar == MemorialCalendarType.Lunar
        it.copy(entry = it.entry.copy(calendar = calendar, day = minOf(it.entry.day, maxDay), leapMonth = leap), dateError = "")
    }

    fun onDayChange(day: Int) = editDraft(refreshPreview = true) {
        it.copy(entry = it.entry.copy(day = day), dateError = "")
    }

    fun onMonthChange(month: Int) = editDraft(refreshPreview = true) {
        it.copy(entry = it.entry.copy(month = month), dateError = "")
    }

    /** [year] = `null` ↔ "Không rõ". */
    fun onDeathYearChange(year: Int?) = editDraft(refreshPreview = true) {
        it.copy(entry = it.entry.copy(deathYear = year), dateError = "")
    }

    /** Tháng nhuận chỉ có nghĩa với âm lịch. / The leap flag only exists for a lunar date. */
    fun onLeapMonthToggle() = editDraft(refreshPreview = true) {
        it.copy(entry = it.entry.copy(leapMonth = !it.entry.leapMonth), dateError = "")
    }

    fun onNoteChange(note: String) = editDraft { it.copy(entry = it.entry.copy(note = note), noteError = "") }

    fun onRemindToggle() = editDraft { it.copy(entry = it.entry.copy(remind = !it.entry.remind)) }

    fun onLeadDaysChange(days: Int) = editDraft { it.copy(entry = it.entry.copy(leadDays = days)) }

    fun onRemindTimeChange(time: LocalTime) = editDraft { it.copy(entry = it.entry.copy(remindTime = time)) }

    /**
     * Lưu bản nháp. Trả `true` khi đã lưu xong (màn hình quay lại); `false` khi form còn lỗi hoặc đang
     * gửi — khi đó form giữ nguyên. /
     * Saves the draft; `false` while the form is invalid or a request is running.
     */
    fun save(): Boolean {
        val draft = _uiState.value.draft ?: return false
        if (draft.saving) return false
        val entry = draft.entry.copy(name = draft.entry.name.trim())
        val (nameError, noteError, dateError) = validationErrors(entry)
        if (nameError.isNotEmpty() || noteError.isNotEmpty() || dateError.isNotEmpty()) {
            editDraft {
                it.copy(
                    entry = entry,
                    nameError = nameError,
                    noteError = noteError,
                    dateError = dateError,
                    formError = "",
                )
            }
            return false
        }
        if (entry.isNew && entries.size >= MAX_ENTRIES) {
            editDraft { it.copy(formError = BOOK_FULL) }
            return false
        }
        editDraft { it.copy(entry = entry, saving = true, formError = "") }
        viewModelScope.launch {
            saveMemorial(entry)
                .onSuccess { saved ->
                    entries = if (entries.any { it.id == saved.id }) {
                        entries.map { if (it.id == saved.id) saved else it }
                    } else {
                        entries + saved
                    }
                    scheduler.sync(entries)
                    publish(flash = SAVED, banner = _uiState.value.banner)
                }
                .onFailure { error -> handleSaveFailure(error) }
        }
        return true
    }

    private fun handleSaveFailure(error: Throwable) {
        when (val failure = error as? MemorialError) {
            MemorialError.Unauthorized -> {
                // Keep the form and everything typed: no cache clear, no guest screen, no navigation.
                // Giữ nguyên form và nội dung: không xoá cache, không đẩy về guest, không điều hướng.
                editDraft { it.copy(saving = false, sessionExpired = true, formError = SESSION_EXPIRED) }
            }
            MemorialError.NotFound -> {
                entries = entries.filterNot { it.id == _uiState.value.draft?.entry?.id }
                publish(flash = GONE, banner = _uiState.value.banner)
            }
            MemorialError.LimitReached -> editDraft { it.copy(saving = false, formError = BOOK_FULL) }
            MemorialError.Offline -> editDraft { it.copy(saving = false, formError = OFFLINE_WRITE) }
            is MemorialError.Validation -> editDraft {
                it.copy(
                    saving = false,
                    formError = "",
                    nameError = if (failure.field == "full_name") NAME_INVALID else "",
                    noteError = if (failure.field == "note") NOTE_TOO_LONG else "",
                    dateError = if (failure.field?.startsWith("death_") == true) DATE_INVALID else "",
                ).let { state ->
                    if (failure.field != null && failure.field != "full_name" &&
                        failure.field != "note" && !failure.field.startsWith("death_")
                    ) {
                        state.copy(formError = SAVE_FAILED)
                    } else {
                        state
                    }
                }
            }
            else -> editDraft { it.copy(saving = false, formError = SAVE_FAILED) }
        }
    }

    /** Hỏi xác nhận trước khi xoá (BR-MEM-12). / Asks before deleting. */
    fun requestDelete() {
        if (_uiState.value.draft?.isNew == false) _uiState.update { it.copy(confirmDelete = true) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(confirmDelete = false) }
    }

    /** Xoá mục đang sửa sau khi đã xác nhận. / Deletes the open entry after confirmation. */
    fun confirmDelete() {
        val draft = _uiState.value.draft ?: return
        if (draft.saving) return
        val id = draft.entry.id
        val remaining = entries.filterNot { it.id == id }
        _uiState.update { it.copy(confirmDelete = false) }
        editDraft { it.copy(saving = true, formError = "") }
        viewModelScope.launch {
            deleteMemorial(id, remaining)
                .onSuccess {
                    entries = remaining
                    publish(flash = "Đã xóa ${draft.entry.name} khỏi sổ giỗ.", banner = _uiState.value.banner)
                }
                .onFailure { error ->
                    when (error as? MemorialError) {
                        MemorialError.Unauthorized -> {
                            // Same rule for delete: the form and its content stay.
                            // Xoá cũng vậy: giữ nguyên form và nội dung.
                            editDraft { it.copy(saving = false, sessionExpired = true, formError = SESSION_EXPIRED) }
                        }
                        MemorialError.NotFound -> {
                            // Already gone elsewhere: the book stays correct and the reminders go too.
                            // Đã bị xoá nơi khác: sổ vẫn đúng và nhắc của mục cũng bị huỷ.
                            entries = remaining
                            viewModelScope.launch { scheduler.sync(remaining) }
                            publish(flash = GONE, banner = _uiState.value.banner)
                        }
                        MemorialError.Offline -> editDraft { it.copy(saving = false, formError = OFFLINE_WRITE) }
                        else -> editDraft { it.copy(saving = false, formError = DELETE_FAILED) }
                    }
                }
        }
    }

    /** File gốc quá lớn để đọc/an; bytes gửi đi vẫn được kiểm 5 MiB sau chuẩn hoá. / Raw file too large. */
    fun onPhotoTooLarge() {
        editDraft { it.copy(photoError = PHOTO_TOO_LARGE_TO_PROCESS) }
    }

    /**
     * Chuẩn hoá ảnh người dùng chọn (định dạng thật, HEIC → JPEG, xoay EXIF, cạnh dài ≤ 2048) rồi mới tải lên
     * với đúng MIME và tên file. Chạy ngoài main thread. / Normalises the picked photo, then uploads it with
     * the MIME type and filename that match the bytes sent.
     */
    fun uploadPhoto(raw: ByteArray) {
        val draft = _uiState.value.draft ?: return
        if (draft.isNew || draft.saving) {
            editDraft { it.copy(photoError = "Hãy lưu mục trước khi thêm ảnh.") }
            return
        }
        editDraft { it.copy(saving = true, photoError = "") }
        viewModelScope.launch {
            val photo = photoNormalizer.normalize(raw).getOrElse { error ->
                editDraft { it.copy(saving = false, photoError = photoMessage(error)) }
                return@launch
            }
            // The form was closed while the photo was being normalised: do not upload it.
            // Form đã đóng trong lúc chuẩn hoá ảnh: không tải lên nữa.
            if (_uiState.value.draft == null) return@launch
            repository.uploadPhoto(draft.entry.id, photo.bytes, photo.mimeType, photo.fileName)
                .onSuccess { saved -> applyPhoto(saved) }
                .onFailure { error -> editDraft { it.copy(saving = false, photoError = photoMessage(error)) } }
        }
    }

    /** Ảnh không đọc được từ bộ chọn (stream lỗi) — báo ngay, không gửi request. / Unreadable picked photo. */
    fun onPhotoUnreadable() {
        editDraft { it.copy(photoError = PHOTO_UNREADABLE) }
    }

    /** Gỡ ảnh chân dung (chỉ gọi khi cờ ảnh bật). / Removes the portrait photo (flag-gated). */
    fun deletePhoto() {
        val draft = _uiState.value.draft ?: return
        if (draft.isNew || draft.saving) return
        editDraft { it.copy(saving = true, photoError = "") }
        viewModelScope.launch {
            repository.deletePhoto(draft.entry.id)
                .onSuccess { saved -> applyPhoto(saved) }
                .onFailure { error -> editDraft { it.copy(saving = false, photoError = photoMessage(error)) } }
        }
    }

    private fun applyPhoto(saved: MemorialEntry) {
        entries = entries.map { if (it.id == saved.id) saved else it }
        editDraft { it.copy(entry = saved, saving = false, photoError = "") }
    }

    private fun photoMessage(error: Throwable): String = when (error as? MemorialError) {
        MemorialError.TooLarge -> PHOTO_TOO_LARGE
        MemorialError.UnsupportedPhoto -> PHOTO_UNSUPPORTED
        MemorialError.Offline -> OFFLINE_WRITE
        MemorialError.NotFound -> GONE
        is MemorialError.Validation -> PHOTO_UNREADABLE
        else -> SAVE_FAILED
    }

    fun dismissFlash() {
        _uiState.update { if (it.flash.isEmpty()) it else it.copy(flash = "") }
    }

    private fun openDraft(entry: MemorialEntry, isNew: Boolean) {
        // Stored values outside the four choices are shown as the nearest one (AC21).
        // Giá trị cũ ngoài lựa chọn hiện bằng lựa chọn gần nhất.
        val display = MemorialDateRules.withNearestReminder(entry)
        _uiState.update {
            it.copy(
                draft = GioDraft(entry = display, isNew = isNew, preview = previewOf(display)),
                confirmDelete = false,
                flash = "",
            )
        }
    }

    private fun editDraft(refreshPreview: Boolean = false, transform: (GioDraft) -> GioDraft) {
        _uiState.update { state ->
            val draft = state.draft ?: return@update state
            val updated = transform(draft)
            state.copy(
                draft = updated.copy(
                    preview = if (refreshPreview) previewOf(updated.entry) else draft.preview,
                ),
            )
        }
    }

    private fun publish(flash: String, banner: String) {
        _uiState.update {
            it.copy(
                status = GioStatus.Ready,
                items = buildItems(),
                flash = flash,
                banner = banner,
                draft = null,
                confirmDelete = false,
            )
        }
    }

    private fun previewOf(entry: MemorialEntry): GioPreview = GioFormat.preview(entry, today())

    private fun validationErrors(entry: MemorialEntry): Triple<String, String, String> {
        fun message(error: MemorialFormError?): String = when (error) {
            null -> ""
            MemorialFormError.NameEmpty -> NAME_EMPTY
            MemorialFormError.NameTooLong -> NAME_TOO_LONG
            MemorialFormError.NoteTooLong -> NOTE_TOO_LONG
            MemorialFormError.DateNotExist -> DATE_INVALID
            MemorialFormError.LeapMonthNotInYear -> LEAP_NOT_IN_YEAR
            MemorialFormError.YearBefore1900 -> YEAR_BEFORE_1900
            MemorialFormError.DateInFuture -> DATE_IN_FUTURE
        }
        return Triple(
            message(MemorialDateRules.nameError(entry.name)),
            message(MemorialDateRules.noteError(entry.note)),
            message(MemorialDateRules.dateError(entry, vietnamToday())),
        )
    }

    private fun buildItems(): List<GioItem> {
        val now = today()
        return entries
            .map { GioItem(it, MemorialSchedule.next(it, now)) }
            .sortedWith(compareBy({ it.occurrence.daysUntil }, { it.entry.name }))
    }

    private companion object {
        /** Server cap (contract §6.1). / Trần 100 mục của server. */
        const val MAX_ENTRIES = 100

        const val SAVED = "Đã lưu vào sổ giỗ."
        const val GONE = "Mục này không còn trong sổ giỗ."
        const val BOOK_FULL = "Sổ giỗ đã đủ 100 người thân."
        const val OFFLINE_WRITE = "Cần kết nối mạng để lưu thay đổi."
        const val OFFLINE_BANNER = "Đang hiện bản lưu trên máy."
        const val LOAD_FAILED = "Không tải được sổ giỗ. Vui lòng thử lại."
        const val SAVE_FAILED = "Chưa lưu được. Vui lòng thử lại."
        const val DELETE_FAILED = "Chưa xoá được. Vui lòng thử lại."
        const val NAME_EMPTY = "Vui lòng nhập họ và tên"
        const val NAME_TOO_LONG = "Họ và tên tối đa 100 ký tự"
        const val NAME_INVALID = "Họ và tên không hợp lệ"
        const val NOTE_TOO_LONG = "Ghi chú tối đa 1000 ký tự"
        const val DATE_INVALID = "Ngày mất không hợp lệ"
        const val LEAP_NOT_IN_YEAR = "Năm đó không có tháng nhuận này"
        const val YEAR_BEFORE_1900 = "Năm mất phải từ 1900"
        const val DATE_IN_FUTURE = "Ngày mất không được sau hôm nay"
        const val PHOTO_TOO_LARGE = "Ảnh tối đa 5 MB."
        const val PHOTO_TOO_LARGE_TO_PROCESS = "Ảnh quá lớn để xử lý. Vui lòng chọn ảnh khác."
        const val PHOTO_UNSUPPORTED = "Ảnh phải là JPEG, PNG hoặc WebP."
        const val PHOTO_UNREADABLE = "Không đọc được ảnh. Vui lòng chọn ảnh khác."
        const val SESSION_EXPIRED = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
    }
}
