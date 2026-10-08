package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.TempleDetail
import com.example.innerlamp.domain.model.TempleGlyph
import com.example.innerlamp.domain.model.TempleSchedule
import com.example.innerlamp.domain.model.TempleStat
import com.example.innerlamp.domain.model.TempleTone

/**
 * Trả về nội dung một trang chùa của màn `temple`.
 *
 * Hiện dữ liệu là mẫu lấy từ prototype (`Docs/design_handoff_innerlamp/`, màn `temple`).
 * Prototype dùng chung 1 template cho mọi chùa ("hiện dùng chung 1 template, data-bind
 * theo chùa chọn" theo README) nên [templeId] hiện chưa được dùng để phân biệt nội dung.
 *
 * TODO(il-data-builder): data-bind theo [templeId] khi có tầng data (danh sách chùa +
 * thời khóa + thống kê thuộc "Data cần API" theo README). Lúc đó use case nhận
 * repository qua constructor và trả `Result<TempleDetail>` để UI xử lý được lỗi mạng.
 */
class GetTempleDetailUseCase {

    operator fun invoke(templeId: String): TempleDetail = TempleDetail(
        id = "tu-quang",
        name = "Chùa Từ Quang",
        place = "Q. Bình Thạnh, TP.HCM",
        sect = "Hệ phái Bắc tông",
        stats = listOf(
            TempleStat(value = "184", label = "Phật tử"),
            TempleStat(value = "6", label = "Thời khóa/tuần"),
            TempleStat(value = "1938", label = "Năm lập"),
        ),
        description = "Chùa mở cửa mỗi ngày 5:00 – 20:00. Quý Phật tử có thể về dự khóa " +
            "tụng sáng, khóa thiền tối và lớp Phật pháp cho thiếu nhi vào Chủ Nhật.",
        address = "128 Nguyễn Văn Đậu, P.5",
        upcoming = listOf(
            TempleSchedule(
                id = "tk-vulan",
                title = "Tụng Kinh Vu Lan",
                sub = "CN 13/9 · 8:00 · Chánh điện",
                trailing = "Đã ghi danh",
                tone = TempleTone.Gold,
                glyph = TempleGlyph.Book,
            ),
            TempleSchedule(
                id = "tk-thien",
                title = "Khóa thiền tối",
                sub = "T5 17/9 · 19:00 · Thầy Minh Đạo",
                trailing = "Ghi danh",
                tone = TempleTone.Green,
                glyph = TempleGlyph.Circle,
            ),
        ),
    )
}
