package com.example.innerlamp.ui.screens.home

import com.example.innerlamp.domain.model.Article
import com.example.innerlamp.domain.model.Verse

/**
 * Sample data of the home blocks that have no API wiring yet (articles, fallback verses). Temples,
 * deities and photos are loaded by [HomeContentLoader]. /
 * Dữ liệu mẫu của các khối trang chủ chưa nối API (bài viết, câu kinh dự phòng). Chùa, chư Phật và ảnh
 * do [HomeContentLoader] tải.
 *
 * TODO(il-data-builder): bài viết → `/v1/articles/top`.
 */
internal object HomeSampleData {

    val verses = listOf(
        Verse(
            lines = listOf("Tâm dẫn đầu các pháp,", "Tâm làm chủ, tâm tạo.", "Nếu tâm được thanh tịnh,", "Nói làm đều an vui."),
            source = "Kinh Pháp Cú — phẩm Song Yếu",
        ),
        Verse(
            lines = listOf("Không làm các điều ác,", "Siêng làm những việc lành,", "Giữ tâm ý trong sạch,", "Ấy lời chư Phật dạy."),
            source = "Kinh Pháp Cú",
        ),
        Verse(
            lines = listOf("Hơi thở vào, biết vào,", "Hơi thở ra, biết ra.", "Thân và tâm một nhịp,", "Giây phút này an nhiên."),
            source = "Kinh Quán Niệm Hơi Thở",
        ),
        Verse(
            lines = listOf("Như sen mọc giữa nước,", "Chẳng nhiễm chút bùn nhơ.", "Người trí sống giữa đời,", "Tâm vẫn thường thanh tịnh."),
            source = "Kinh Tương Ưng Bộ",
        ),
    )

    val articles = listOf(
        Article("art-1", "Giáo lý căn bản", "Tứ Diệu Đế: bốn sự thật cho người mới bắt đầu", "Thầy Minh Đạo", "6 phút đọc"),
        Article("art-2", "Thực hành", "Chánh niệm trong từng việc nhỏ: rửa chén cũng là thiền", "Sư cô Tâm Nguyện", "4 phút đọc"),
        Article("art-3", "Lễ hội", "Ý nghĩa mùa Vu Lan và bông hồng cài áo", "Ban biên tập InnerLamp", "5 phút đọc"),
        Article("art-4", "Tâm lý & đời sống", "Buông bỏ không phải là từ bỏ", "Thầy Minh Đạo", "5 phút đọc"),
        Article("art-5", "Lịch sử", "Vua Trần Nhân Tông và thiền phái Trúc Lâm", "Ban biên tập InnerLamp", "7 phút đọc"),
    )
}
