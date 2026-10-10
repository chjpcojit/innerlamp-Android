package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.SutraCatalogItem
import com.example.innerlamp.domain.model.SutraSect

/**
 * Trả về danh mục toàn bộ bản kinh cho màn "Kinh sách" (prototype `SUTRA_CAT`).
 *
 * Hiện là dữ liệu mẫu.
 *
 * TODO(il-data-builder): chuyển sang `SutraRepository` (API + cache offline).
 */
class GetSutraCatalogUseCase {
    operator fun invoke(): List<SutraCatalogItem> = CATALOG

    private companion object {
        val CATALOG = listOf(
            SutraCatalogItem(
                id = "phapcu", name = "Kinh Pháp Cú", original = "Dhammapada · Pāli",
                translator = "HT Thích Minh Châu", minutes = 130, tags = listOf("Hằng ngày"),
                sect = SutraSect.Nam, hasAudio = true, isDownloaded = true,
                description = "Tuyển tập 423 lời dạy ngắn của Đức Phật, xếp theo 26 phẩm. Bản Việt dịch kèm nguyên văn Pāli và bản Anh ngữ để đối chiếu.",
            ),
            SutraCatalogItem(
                id = "vulan", name = "Kinh Vu Lan Báo Hiếu", original = "佛說盂蘭盆經 · Ullambana Sūtra",
                translator = "Bản Việt dịch thông dụng", minutes = 38, tags = listOf("Cầu siêu"),
                sect = SutraSect.Bac, hasAudio = true, isDownloaded = false,
                description = "Chuyện Tôn giả Mục Kiền Liên cứu mẹ, lời Phật dạy về chữ hiếu và nghi thức cúng dường chư Tăng ngày Rằm tháng Bảy.",
            ),
            SutraCatalogItem(
                id = "adida", name = "Kinh A Di Đà", original = "佛說阿彌陀經 · Sukhāvatīvyūha",
                translator = "HT Thích Trí Tịnh", minutes = 22, tags = listOf("Hằng ngày", "Cầu siêu"),
                sect = SutraSect.Bac, hasAudio = true, isDownloaded = true,
                description = "Đức Phật giới thiệu cõi Cực Lạc phương Tây và khuyên chúng sanh niệm danh hiệu Phật A Di Đà — kinh căn bản của pháp môn Tịnh Độ.",
            ),
            SutraCatalogItem(
                id = "daibi", name = "Chú Đại Bi", original = "大悲咒 · Nīlakaṇṭha Dhāraṇī",
                translator = "Phiên âm Hán – Việt", minutes = 8, tags = listOf("Trì chú", "Hằng ngày"),
                sect = SutraSect.Bac, hasAudio = true, isDownloaded = true,
                description = "Thần chú của Bồ Tát Quán Thế Âm, trích Kinh Thiên Thủ Thiên Nhãn — thường trì tụng trong khóa lễ sáng.",
            ),
            SutraCatalogItem(
                id = "tamkinh", name = "Bát Nhã Tâm Kinh", original = "般若波羅蜜多心經 · Prajñāpāramitā Hṛdaya",
                translator = "Phiên âm Hán – Việt · bản dịch nghĩa", minutes = 4, tags = listOf("Hằng ngày", "Trì chú"),
                sect = SutraSect.Bac, hasAudio = true, isDownloaded = true,
                description = "Tinh yếu của hệ Bát Nhã: ngũ uẩn đều không, vượt qua mọi khổ ách — bài kinh ngắn được tụng nhiều nhất.",
            ),
            SutraCatalogItem(
                id = "phomon", name = "Phẩm Phổ Môn", original = "觀世音菩薩普門品 · Kinh Pháp Hoa phẩm 25",
                translator = "HT Thích Trí Tịnh", minutes = 25, tags = listOf("Cầu an"),
                sect = SutraSect.Bac, hasAudio = true, isDownloaded = false,
                description = "Hạnh nguyện cứu khổ của Bồ Tát Quán Thế Âm — thường tụng trong khóa lễ cầu an.",
            ),
            SutraCatalogItem(
                id = "duocsu", name = "Kinh Dược Sư", original = "藥師琉璃光如來本願功德經",
                translator = "Bản Việt dịch thông dụng", minutes = 45, tags = listOf("Cầu an"),
                sect = SutraSect.Bac, hasAudio = false, isDownloaded = false,
                description = "Mười hai đại nguyện của Đức Phật Dược Sư — tụng cầu an, cầu cho người bệnh.",
            ),
            SutraCatalogItem(
                id = "diatang", name = "Kinh Địa Tạng Bổn Nguyện", original = "地藏菩薩本願經",
                translator = "HT Thích Trí Tịnh", minutes = 120, tags = listOf("Cầu siêu"),
                sect = SutraSect.Bac, hasAudio = false, isDownloaded = false,
                description = "Đại nguyện của Bồ Tát Địa Tạng — tụng trong mùa Vu Lan và khi cầu siêu cho người đã khuất.",
            ),
            SutraCatalogItem(
                id = "tutam", name = "Kinh Từ Bi", original = "Karaṇīya Metta Sutta · Pāli",
                translator = "HT Thích Minh Châu", minutes = 6, tags = listOf("Hằng ngày", "Cầu an"),
                sect = SutraSect.Nam, hasAudio = true, isDownloaded = false,
                description = "Lời dạy về việc rải tâm từ đến mọi loài — thường tụng trước giờ ngồi thiền tâm từ.",
            ),
            SutraCatalogItem(
                id = "hanhphuc", name = "Kinh Hạnh Phúc", original = "Maṅgala Sutta · Pāli",
                translator = "HT Thích Minh Châu", minutes = 5, tags = listOf("Cầu an", "Hằng ngày"),
                sect = SutraSect.Nam, hasAudio = true, isDownloaded = true,
                description = "Ba mươi tám điều phúc lành cao thượng — Đức Phật trả lời câu hỏi thế nào là hạnh phúc.",
            ),
            SutraCatalogItem(
                id = "chuyenphap", name = "Kinh Chuyển Pháp Luân", original = "Dhammacakkappavattana Sutta · Pāli",
                translator = "HT Thích Minh Châu", minutes = 15, tags = listOf("Hằng ngày"),
                sect = SutraSect.Nam, hasAudio = false, isDownloaded = false,
                description = "Bài pháp đầu tiên tại vườn Lộc Uyển: trung đạo, Tứ Diệu Đế và Bát Chánh Đạo.",
            ),
        )
    }
}
