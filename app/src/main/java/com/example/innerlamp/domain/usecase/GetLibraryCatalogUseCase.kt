package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.FeaturedTalk
import com.example.innerlamp.domain.model.LibraryCatalog
import com.example.innerlamp.domain.model.LibraryCategory
import com.example.innerlamp.domain.model.Sutra
import com.example.innerlamp.domain.model.SutraTone

/**
 * Trả về catalog của màn Thư viện: các mục lọc, thẻ pháp thoại nổi bật và
 * danh sách "Kinh thường tụng".
 *
 * Hiện dữ liệu là mẫu lấy từ prototype (`Docs/design_handoff_innerlamp/`, màn `lib`).
 *
 * TODO(il-data-builder): chuyển sang delegate cho `LibraryRepository` khi có tầng
 * data/API (danh mục kinh/pháp thoại thuộc "Data cần API" theo README). Lúc đó use
 * case nhận repository qua constructor và trả `Result<LibraryCatalog>` để UI xử lý
 * được lỗi mạng.
 */
class GetLibraryCatalogUseCase {

    operator fun invoke(): LibraryCatalog = LibraryCatalog(
        categories = listOf(
            LibraryCategory.Sutra,
            LibraryCategory.Meditation,
            LibraryCategory.Talk,
        ),
        featuredTalk = FeaturedTalk(
            id = "tl-hieu-la-dao",
            kicker = "PHÁP THOẠI MÙA VU LAN",
            title = "Hiếu là đạo,\nđạo cũng là hiếu",
            durationMinutes = 46,
        ),
        sutras = listOf(
            Sutra(
                id = "k-kinh-phap-cu",
                name = "Kinh Pháp Cú",
                sub = "423 kệ · song ngữ",
                time = "2g 10p",
                tone = SutraTone.Gold,
            ),
            Sutra(
                id = "k-vu-lan",
                name = "Kinh Vu Lan",
                sub = "Tụng rằm tháng Bảy",
                time = "38 phút",
                tone = SutraTone.Rose,
            ),
            Sutra(
                id = "k-a-di-da",
                name = "Kinh A Di Đà",
                sub = "Có phiên âm",
                time = "26 phút",
                tone = SutraTone.Green,
            ),
        ),
    )
}
