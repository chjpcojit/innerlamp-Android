package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.ChantVerse
import com.example.innerlamp.domain.model.SutraReader

/**
 * Trả về nội dung màn "Tụng kinh" (`reader`) của một bài kinh.
 *
 * Hiện dữ liệu là mẫu lấy từ prototype (`Docs/design_handoff_innerlamp/`, màn `reader`).
 * Giống "Chi tiết bài kinh", prototype dùng chung một template nên [sutraId] chưa được
 * dùng để phân biệt nội dung.
 *
 * TODO(il-data-builder): data-bind theo [sutraId] khi có tầng data (danh mục kinh thuộc
 * "Data cần API" theo README). Lúc đó use case nhận repository qua constructor và trả
 * `Result<SutraReader>` để UI xử lý được lỗi mạng.
 */
class GetSutraReaderUseCase {

    operator fun invoke(sutraId: String): SutraReader = SutraReader(
        id = "k-kinh-phap-cu",
        title = "Kinh Pháp Cú",
        subtitle = "Phẩm Song Yếu · kệ 1–2",
        verse = ChantVerse(
            vietnamese = "Ý dẫn đầu các pháp,\n" +
                "Ý làm chủ, ý tạo;\n" +
                "Nếu với ý ô nhiễm,\n" +
                "Nói lên hay hành động,\n" +
                "Khổ não bước theo sau,\n" +
                "Như xe, chân vật kéo.",
            translation = "Mind precedes all mental states. Mind is their chief; they are all mind-wrought.",
        ),
        nextVersePreview = "Ý dẫn đầu các pháp,\n" +
            "Ý làm chủ, ý tạo;\n" +
            "Nếu với ý thanh tịnh…",
        teacher = "Thầy Minh Đạo",
        speed = "1.0×",
    )
}
