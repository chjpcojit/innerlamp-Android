package com.example.innerlamp.ui.screens.home

import com.example.innerlamp.domain.model.Article
import com.example.innerlamp.domain.model.BuddhaFigure
import com.example.innerlamp.domain.model.Temple
import com.example.innerlamp.domain.model.Verse
import kotlinx.coroutines.delay

/**
 * Dữ liệu mẫu lấy từ prototype cho các khối nội dung của trang chủ.
 *
 * TODO(il-data-builder): thay bằng repository/API khi backend có (chùa, bài viết,
 * câu kinh theo ngày, ảnh chùa). Giữ ở đây để UI dựng được độc lập.
 */
internal object HomeSampleData {

    /**
     * Giả lập lần gọi API lấy nội dung 3 khối (có độ trễ để thấy skeleton).
     *
     * TODO(il-data-builder): thay bằng repository thật; lỗi mạng thì ném exception để
     * ViewModel hiện trạng thái rỗng.
     */
    suspend fun load(): HomeSectionsContent {
        delay(LOAD_DELAY_MS)
        return HomeSectionsContent(temples = temples, buddhas = buddhas, articles = articles)
    }

    private const val LOAD_DELAY_MS = 900L

    /** Số ảnh còn lại hiển thị ở ô "+N ảnh" (prototype: 12 ảnh − 3). */
    const val PHOTO_MORE = 9

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

    val temples = listOf(
        Temple("tp-motcot", "Chùa Một Cột", "Ba Đình, Hà Nội", "Miền Bắc", "Dựng năm 1049 · thời Lý"),
        Temple("tp-tranquoc", "Chùa Trấn Quốc", "Hồ Tây, Hà Nội", "Miền Bắc", "Ngôi chùa cổ nhất Thăng Long"),
        Temple("tp-huong", "Chùa Hương", "Mỹ Đức, Hà Nội", "Miền Bắc", "Lễ hội tháng Giêng – tháng Ba"),
        Temple("tp-baidinh", "Chùa Bái Đính", "Gia Viễn, Ninh Bình", "Miền Bắc", "Quần thể chùa lớn nhất cả nước"),
        Temple("tp-thienmu", "Chùa Thiên Mụ", "Bờ sông Hương, Huế", "Miền Trung", "Tháp Phước Duyên 7 tầng"),
        Temple("tp-linhung", "Chùa Linh Ứng", "Sơn Trà, Đà Nẵng", "Miền Trung", "Tượng Quán Âm cao 67m"),
        Temple("tp-truclam", "Thiền viện Trúc Lâm", "Hồ Tuyền Lâm, Đà Lạt", "Tây Nguyên", "Thiền phái Trúc Lâm Yên Tử"),
        Temple("tp-vinhnghiem", "Chùa Vĩnh Nghiêm", "Quận 3, TP. Hồ Chí Minh", "Miền Nam", "Kiến trúc Bắc Bộ giữa Sài Gòn"),
    )

    val articles = listOf(
        Article("art-1", "Giáo lý căn bản", "Tứ Diệu Đế: bốn sự thật cho người mới bắt đầu", "Thầy Minh Đạo", "6 phút đọc"),
        Article("art-2", "Thực hành", "Chánh niệm trong từng việc nhỏ: rửa chén cũng là thiền", "Sư cô Tâm Nguyện", "4 phút đọc"),
        Article("art-3", "Lễ hội", "Ý nghĩa mùa Vu Lan và bông hồng cài áo", "Ban biên tập InnerLamp", "5 phút đọc"),
        Article("art-4", "Tâm lý & đời sống", "Buông bỏ không phải là từ bỏ", "Thầy Minh Đạo", "5 phút đọc"),
        Article("art-5", "Lịch sử", "Vua Trần Nhân Tông và thiền phái Trúc Lâm", "Ban biên tập InnerLamp", "7 phút đọc"),
    )

    /** Khối "Thư viện Phật & Bồ Tát" — 9 vị, khớp `BUDDHAS` của prototype. */
    val buddhas = listOf(
        BuddhaFigure("thichca", "Phật", "Phật Thích Ca", "15/04"),
        BuddhaFigure("adida", "Phật", "Phật A Di Đà", "17/11"),
        BuddhaFigure("duocsu", "Phật", "Phật Dược Sư", "30/09"),
        BuddhaFigure("dilac", "Phật tương lai", "Phật Di Lặc", "01/01"),
        BuddhaFigure("quanam", "Bồ Tát", "Quán Thế Âm", "19/02"),
        BuddhaFigure("diatang", "Bồ Tát", "Địa Tạng Vương", "30/07"),
        BuddhaFigure("vanthu", "Bồ Tát", "Văn Thù", "04/04"),
        BuddhaFigure("phohien", "Bồ Tát", "Phổ Hiền", "21/02"),
        BuddhaFigure("daithechi", "Bồ Tát", "Đại Thế Chí", "13/07"),
    )
}
