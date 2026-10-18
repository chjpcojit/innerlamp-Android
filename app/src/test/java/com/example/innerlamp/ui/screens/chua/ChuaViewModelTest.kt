package com.example.innerlamp.ui.screens.chua

import com.example.innerlamp.domain.calendar.FakeCalendarEventRepository
import com.example.innerlamp.domain.calendar.event
import com.example.innerlamp.domain.calendar.network
import com.example.innerlamp.domain.model.CalendarGroup
import com.example.innerlamp.domain.model.CalendarOccurrence
import com.example.innerlamp.domain.model.CalendarYear
import com.example.innerlamp.domain.model.LunarDay
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.domain.usecase.GetUpcomingFastDaysUseCase
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChuaViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun year(vararg dates: String) = CalendarYear(
        2026,
        listOf(CalendarGroup("buddhist", "Phật giáo")),
        dates.indices.map { event("e$it", sortOrder = it + 1, isMajor = it == 0) },
        dates.mapIndexed { i, d -> CalendarOccurrence("e$i", LocalDate.parse(d), LunarDay(15, 8, 2026, false)) },
    )

    private val repo = FakeCalendarEventRepository(
        loads = mutableMapOf(
            2026 to network(year("2026-10-01", "2026-10-09", "2026-10-12", "2026-10-20", "2026-10-25", "2026-11-03")),
            2027 to network(CalendarYear(2027, emptyList(), emptyList(), emptyList())),
        ),
    )

    private fun vm(hour: Int = 10, fastDays: GetUpcomingFastDaysUseCase? = GetUpcomingFastDaysUseCase(repo)) =
        ChuaViewModel(getUpcomingFastDays = fastDays, clock = { LocalDateTime.of(2026, 10, 9, hour, 0) })

    @Test
    fun `mặc định có chùa tu tập và trạng thái đang mở`() {
        val s = vm(hour = 10).uiState.value
        assertEquals("Chùa Từ Quang", s.myTemple?.name)
        assertEquals("Đang mở · đến 20:00", s.statusLabel)
        // From today (9/10) on, the 4 nearest Buddhist-group days of the calendar; the past one is dropped.
        assertEquals(
            listOf("2026-10-09", "2026-10-12", "2026-10-20", "2026-10-25").map(LocalDate::parse),
            s.fastDays.map { it.date },
        )
        assertEquals(0, s.fastDays.first().daysAway)
        assertFalse(s.fastDaysLoading)
    }

    @Test
    fun `lỗi mạng thì khối ngày lễ báo lỗi và chỉ thử lại khi người dùng bấm`() {
        val failing = FakeCalendarEventRepository(loads = mutableMapOf(2026 to Result.failure(CalendarLoadError.Offline)))
        val vm = vm(fastDays = GetUpcomingFastDaysUseCase(failing))
        assertTrue(vm.uiState.value.fastDays.isEmpty())
        assertEquals(CalendarLoadError.Offline, vm.uiState.value.fastDaysError)
        assertEquals(1, failing.loadCalls.size)

        failing.loads[2026] = network(year("2026-10-09"))
        failing.loads[2027] = network(CalendarYear(2027, emptyList(), emptyList(), emptyList()))
        vm.onRetryFastDays()
        assertEquals(1, vm.uiState.value.fastDays.size)
        assertEquals(3, failing.loadCalls.size) // 2026 again + 2027 (too few days left)
    }

    @Test
    fun `ngoài giờ mở cửa thì hiện đã đóng`() {
        assertEquals("Đã đóng · mở 4:00", vm(hour = 22).uiState.value.statusLabel)
    }

    @Test
    fun `đổi chùa về trạng thái chưa có chùa và hủy đổi thì giữ chùa cũ`() {
        val vm = vm()
        vm.onChangeTemple()
        assertNull(vm.uiState.value.myTemple)
        vm.onCancelChange()
        assertEquals("Chùa Từ Quang", vm.uiState.value.myTemple?.name)
    }

    @Test
    fun `chọn chùa từ danh sách đặt làm chùa tu tập và xóa ô tìm`() {
        val vm = vm()
        vm.onChangeTemple()
        vm.onQueryChange("vinh nghiem")
        assertEquals(listOf("Chùa Vĩnh Nghiêm"), vm.uiState.value.nearby.map { it.name })
        vm.onPickTemple("tp-vinhnghiem")
        assertEquals("Chùa Vĩnh Nghiêm", vm.uiState.value.myTemple?.name)
        assertEquals("", vm.uiState.value.query)
    }

    @Test
    fun `danh sách gần bạn sắp theo khoảng cách và lấy 4 chùa`() {
        val vm = vm()
        vm.onChangeTemple()
        val near = vm.uiState.value.nearby
        assertEquals(4, near.size)
        assertEquals("Chùa Vĩnh Nghiêm", near.first().name)
    }

    @Test
    fun `đề xuất thiếu tên hoặc tỉnh báo lỗi đúng ô`() {
        val vm = vm()
        vm.onChangeTemple()
        vm.onOpenProposal()
        vm.onSubmitProposal()
        assertEquals(ChuaProposalField.Name, vm.uiState.value.proposal?.errorField)
        vm.onProposalChange { it.copy(name = "Pháp Hoa") }
        vm.onSubmitProposal()
        assertEquals(ChuaProposalField.Province, vm.uiState.value.proposal?.errorField)
    }

    @Test
    fun `đề xuất trùng chùa có sẵn bị từ chối`() {
        val vm = vm()
        vm.onChangeTemple()
        vm.onOpenProposal()
        vm.onProposalChange { it.copy(name = "chua tu quang", province = "tp ho chi minh") }
        vm.onSubmitProposal()
        assertNotNull(vm.uiState.value.proposal?.error?.takeIf { it.contains("đã có trong danh sách") })
    }

    @Test
    fun `đề xuất hợp lệ chọn ngay chùa chờ xác minh`() {
        val vm = vm()
        vm.onChangeTemple()
        vm.onOpenProposal()
        vm.onProposalChange { it.copy(name = "Pháp Hoa", province = "Đồng Nai") }
        vm.onSubmitProposal()
        val s = vm.uiState.value
        assertEquals("Chùa Pháp Hoa", s.myTemple?.name)
        assertFalse(s.isVerified)
        assertEquals("Chờ xác minh", s.statusLabel)
        assertNull(s.proposal)
        assertTrue(s.nearby.none { it.name == "Chùa Pháp Hoa" })
    }

    @Test
    fun `bật tắt nhắc ngày lễ và giữ chỗ sự kiện`() {
        val vm = vm()
        val day: LocalDate = vm.uiState.value.fastDays.first().date
        vm.onToggleDayReminder(day)
        assertTrue(day in vm.uiState.value.remindedDays)
        vm.onToggleDayReminder(day)
        assertFalse(day in vm.uiState.value.remindedDays)
        vm.onToggleEventAction("ev-khoa-tu", isReserve = true)
        assertTrue("ev-khoa-tu" in vm.uiState.value.reservedEvents)
    }
}
