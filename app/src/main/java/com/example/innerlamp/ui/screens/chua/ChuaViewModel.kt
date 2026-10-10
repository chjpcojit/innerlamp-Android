package com.example.innerlamp.ui.screens.chua

import androidx.lifecycle.ViewModel
import com.example.innerlamp.domain.model.ChuaTemple
import com.example.innerlamp.domain.usecase.GetUpcomingFastDaysUseCase
import com.example.innerlamp.ui.screens.templelist.foldVietnamese
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel cho tab Chùa.
 *
 * - Expose state qua `StateFlow`; screen dùng `collectAsStateWithLifecycle()`.
 * - Danh bạ, thông báo, sự kiện lấy từ [ChuaSampleData] vì chưa có repository chùa.
 *   TODO(il-data-builder): nối vào use case/repository chùa khi data layer có.
 * - [clock] tiêm vào để test được trạng thái "Đang mở / Đã đóng" và danh sách ngày lễ.
 *
 * @author innerlamp
 */
class ChuaViewModel(
    getUpcomingFastDays: GetUpcomingFastDaysUseCase = GetUpcomingFastDaysUseCase(),
    clock: () -> LocalDateTime = { LocalDateTime.now() },
) : ViewModel() {

    private val _uiState: MutableStateFlow<ChuaUiState>

    init {
        val now = clock()
        val directory = ChuaSampleData.directory
        val mine = directory.first { it.id == ChuaSampleData.DEFAULT_TEMPLE_ID }
        _uiState = MutableStateFlow(
            ChuaUiState(
                directory = directory,
                myTemple = mine,
                minuteOfDay = now.hour * 60 + now.minute,
                news = ChuaSampleData.news,
                events = ChuaSampleData.events,
                online = ChuaSampleData.online,
                retreats = ChuaSampleData.retreats,
                fastDays = getUpcomingFastDays(now.toLocalDate()),
                peerCount = ChuaSampleData.peerCount(mine),
            ),
        )
    }

    val uiState: StateFlow<ChuaUiState> = _uiState.asStateFlow()

    /** "Đổi chùa": về trạng thái chưa có chùa, nhớ chùa cũ để hủy đổi. */
    fun onChangeTemple() = _uiState.update {
        it.copy(previousTemple = it.myTemple, myTemple = null, query = "", proposal = null)
    }

    /** Hủy đổi chùa → giữ lại chùa cũ. */
    fun onCancelChange() = _uiState.update {
        val prev = it.previousTemple ?: return@update it
        it.copy(myTemple = prev, previousTemple = null, query = "", proposal = null, peerCount = ChuaSampleData.peerCount(prev))
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    /** Bấm "Chọn" ở một chùa trong danh sách → đặt làm chùa tu tập. */
    fun onPickTemple(templeId: String) = _uiState.update { state ->
        val temple = state.directory.firstOrNull { it.id == templeId } ?: return@update state
        state.withTemple(temple)
    }

    fun onToggleEventAction(eventId: String, isReserve: Boolean) = _uiState.update {
        if (isReserve) {
            it.copy(reservedEvents = it.reservedEvents.toggle(eventId))
        } else {
            it.copy(remindedEvents = it.remindedEvents.toggle(eventId))
        }
    }

    fun onToggleDayReminder(date: LocalDate) = _uiState.update {
        it.copy(remindedDays = it.remindedDays.toggle(date))
    }

    // ── Form "Đề xuất thêm chùa" ─────────────────────────────────────────────

    /** Mở form; điền sẵn ô tên bằng chuỗi đang tìm. */
    fun onOpenProposal() = _uiState.update { it.copy(proposal = ChuaProposal(name = it.query)) }

    fun onCancelProposal() = _uiState.update { it.copy(proposal = null) }

    fun onProposalChange(transform: (ChuaProposal) -> ChuaProposal) = _uiState.update { state ->
        val p = state.proposal ?: return@update state
        state.copy(proposal = transform(p).copy(error = "", errorField = null))
    }

    /** Gửi đề xuất: kiểm tra → thêm chùa "chờ xác minh" và chọn ngay làm chùa tu tập. */
    fun onSubmitProposal() = _uiState.update { state ->
        val p = state.proposal ?: return@update state
        val name = p.name.trim()
        val province = p.province.trim()
        val error: Pair<String, ChuaProposalField>? = when {
            name.isEmpty() -> "Nhập tên chùa." to ChuaProposalField.Name
            province.isEmpty() -> "Nhập tỉnh / thành phố." to ChuaProposalField.Province
            else -> state.directory.firstOrNull {
                it.name.slug() == name.slug() && it.province.slug() == province.slug()
            }?.let { "${it.name} đã có trong danh sách. Tìm và chọn ở trên." to ChuaProposalField.Name }
        }
        if (error != null) {
            return@update state.copy(proposal = p.copy(error = error.first, errorField = error.second))
        }
        val fullName = if (PREFIX.containsMatchIn(name)) name else "Chùa $name"
        val custom = ChuaTemple(
            id = "tp-u${System.nanoTime()}",
            name = fullName,
            place = listOf(p.address.trim(), province).filter { it.isNotEmpty() }.joinToString(", "),
            province = province,
            isVerified = false,
        )
        state.copy(directory = state.directory + custom).withTemple(custom)
    }

    private fun ChuaUiState.withTemple(temple: ChuaTemple) = copy(
        myTemple = temple,
        previousTemple = null,
        query = "",
        proposal = null,
        peerCount = ChuaSampleData.peerCount(temple),
    )

    /** Bỏ dấu + bỏ khoảng trắng/dấu câu để so trùng ("TP. Hồ Chí Minh" == "tp ho chi minh"). */
    private fun String.slug(): String = foldVietnamese().replace(Regex("[^a-z0-9]"), "")

    private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

    private companion object {
        val PREFIX = Regex("^(chùa|thiền viện|tịnh xá|niệm phật đường)", RegexOption.IGNORE_CASE)
    }
}
