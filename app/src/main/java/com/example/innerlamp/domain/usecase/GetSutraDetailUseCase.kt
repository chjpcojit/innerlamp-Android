package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.SutraBadge
import com.example.innerlamp.domain.model.SutraBadgeStyle
import com.example.innerlamp.domain.model.SutraChapter
import com.example.innerlamp.domain.model.SutraDetail

/**
 * Trả về nội dung một bài kinh của màn `detail` (Chi tiết bài kinh).
 *
 * Hiện dữ liệu là mẫu lấy từ prototype (`Docs/design_handoff_innerlamp/`, màn `detail`).
 * Giống trang chùa / pháp thoại, prototype hiện dùng chung một template nên [sutraId]
 * chưa được dùng để phân biệt nội dung.
 *
 * TODO(il-data-builder): data-bind theo [sutraId] khi có tầng data (danh mục kinh thuộc
 * "Data cần API" theo README). Lúc đó use case nhận repository qua constructor và trả
 * `Result<SutraDetail>` để UI xử lý được lỗi mạng.
 */
class GetSutraDetailUseCase {

    operator fun invoke(sutraId: String): SutraDetail = SutraDetail(
        id = "k-kinh-phap-cu",
        kicker = "KINH VĂN · SONG NGỮ",
        title = "Kinh Pháp Cú",
        meta = "26 phẩm · 423 kệ · Thầy Minh Đạo tụng · 2 giờ 10 phút",
        badges = listOf(
            SutraBadge(label = "Tụng hằng ngày", style = SutraBadgeStyle.Filled),
            SutraBadge(label = "Có phiên âm", style = SutraBadgeStyle.Outlined),
        ),
        description = "Tuyển tập 423 lời dạy ngắn của Đức Phật, xếp theo 26 phẩm. " +
            "Bản Việt dịch của Hòa thượng Thích Minh Châu, kèm nguyên văn Pāli và " +
            "bản Anh ngữ để đối chiếu.",
        chapterCount = "26 phẩm",
        chapters = listOf(
            SutraChapter(num = "I", name = "Phẩm Song Yếu", sub = "Kệ 1 – 20", time = "9 phút", isActive = true),
            SutraChapter(num = "II", name = "Phẩm Không Phóng Dật", sub = "Kệ 21 – 32", time = "7 phút"),
            SutraChapter(num = "III", name = "Phẩm Tâm", sub = "Kệ 33 – 43", time = "6 phút"),
            SutraChapter(num = "IV", name = "Phẩm Hoa", sub = "Kệ 44 – 59", time = "8 phút"),
            SutraChapter(num = "V", name = "Phẩm Người Ngu", sub = "Kệ 60 – 75", time = "8 phút"),
        ),
    )
}
