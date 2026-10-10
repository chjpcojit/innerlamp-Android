package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.MemorialCalendarType
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialRelation
import java.time.LocalTime

/**
 * Danh sách người thân trong Sổ giỗ chạp.
 *
 * Hiện trả dữ liệu mẫu lấy từ prototype (`state.gio`); khi có tầng dữ liệu (Room) thì
 * thay bằng repository mà không đổi chữ ký.
 */
class GetMemorialEntriesUseCase {
    operator fun invoke(): List<MemorialEntry> = listOf(
        MemorialEntry(
            id = "g1", name = "Trần Thị Mai", relation = MemorialRelation.MaternalGrandmother,
            calendar = MemorialCalendarType.Lunar, day = 23, month = 8, deathYear = 2015,
            note = "Cúng chay. Báo cậu Hai và dì Út về từ chiều hôm trước.",
            remind = true, leadDays = 1, remindTime = LocalTime.of(7, 0), syncReminders = true,
        ),
        MemorialEntry(
            id = "g2", name = "Nguyễn Văn Hòa", relation = MemorialRelation.PaternalGrandfather,
            calendar = MemorialCalendarType.Lunar, day = 12, month = 3, deathYear = 2009,
            note = "Giỗ chính ở nhà bác Cả, Nam Định.",
            remind = true, leadDays = 7, remindTime = LocalTime.of(19, 0), syncReminders = true,
        ),
        MemorialEntry(
            id = "g3", name = "Nguyễn Văn Minh", relation = MemorialRelation.Father,
            calendar = MemorialCalendarType.Solar, day = 14, month = 12, deathYear = 2019,
            note = "",
            remind = true, leadDays = 3, remindTime = LocalTime.of(7, 0), syncReminders = false,
        ),
    )
}
