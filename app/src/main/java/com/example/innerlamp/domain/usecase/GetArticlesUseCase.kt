package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.ArticleDetail

/**
 * Trả về toàn bộ bài viết Phật pháp biên tập (5 bài) của màn `article`.
 *
 * Hiện dữ liệu là mẫu lấy từ prototype (`Docs/design_handoff_innerlamp/`, mảng
 * `Component.ARTICLES`).
 *
 * TODO(il-data-builder): chuyển sang delegate cho `ArticleRepository` khi có tầng
 * data/API (bài viết biên tập thuộc "Data cần API" theo README). Lúc đó use case
 * nhận repository qua constructor và trả `Result<List<ArticleDetail>>` để UI xử lý
 * được lỗi mạng.
 */
class GetArticlesUseCase {

    operator fun invoke(): List<ArticleDetail> = listOf(
        ArticleDetail(
            id = "art-1",
            category = "Giáo lý căn bản",
            title = "Tứ Diệu Đế: bốn sự thật cho người mới bắt đầu",
            author = "Thầy Minh Đạo",
            initials = "MĐ",
            date = "18 tháng 9",
            readTime = "6 phút đọc",
            imageId = "art-1",
            paragraphs = listOf(
                "Bài pháp đầu tiên Đức Phật giảng sau khi thành đạo, tại vườn Lộc Uyển, không nói về thần thông hay cõi xa xôi. Ngài nói về khổ — điều ai trong chúng ta cũng từng nếm trải.",
                "Khổ đế là nhìn thẳng vào sự thật rằng đời sống có bất toại nguyện. Tập đế chỉ ra nguyên nhân: lòng tham muốn, bám víu. Diệt đế khẳng định khổ có thể chấm dứt. Đạo đế là con đường thực tập dẫn đến sự chấm dứt ấy — Bát Chánh Đạo.",
                "Người mới học thường thấy Tứ Diệu Đế bi quan. Thật ra đó là một phác đồ chữa bệnh: nhận ra bệnh, tìm ra nguyên nhân, tin rằng bệnh chữa được, và bắt đầu uống thuốc.",
                "Bạn có thể bắt đầu ngay hôm nay: khi một phiền não khởi lên, dừng lại một hơi thở và hỏi — mình đang bám víu vào điều gì?",
            ),
            quote = "Này các Tỳ-kheo, xưa cũng như nay, Ta chỉ nói về khổ và sự diệt khổ.",
            quoteSource = "Kinh Trung Bộ",
        ),
        ArticleDetail(
            id = "art-2",
            category = "Thực hành",
            title = "Chánh niệm trong từng việc nhỏ: rửa chén cũng là thiền",
            author = "Sư cô Tâm Nguyện",
            initials = "TN",
            date = "15 tháng 9",
            readTime = "4 phút đọc",
            imageId = "art-2",
            paragraphs = listOf(
                "Nhiều người nghĩ thiền phải ngồi yên trong chánh điện. Nhưng chánh niệm đơn giản là biết rõ việc mình đang làm, ngay khi đang làm.",
                "Khi rửa chén, hãy cảm nhận nước ấm trên tay, nghe tiếng bát chạm nhau, thấy bọt xà phòng tan dần. Đừng rửa chén cho nhanh để đi làm việc khác — hãy rửa chén để rửa chén.",
                "Bắt đầu với một việc mỗi ngày: đánh răng, pha trà, hay mười bước chân từ cửa ra xe. Khi tâm chạy đi, nhẹ nhàng đưa nó về, không trách móc.",
            ),
            quote = "Đi như thể bàn chân bạn đang hôn mặt đất.",
            quoteSource = "Lời dạy trong truyền thống thiền Việt Nam",
        ),
        ArticleDetail(
            id = "art-3",
            category = "Lễ hội",
            title = "Ý nghĩa mùa Vu Lan và bông hồng cài áo",
            author = "Ban biên tập InnerLamp",
            initials = "IL",
            date = "9 tháng 9",
            readTime = "5 phút đọc",
            imageId = "art-3",
            paragraphs = listOf(
                "Vu Lan bắt nguồn từ câu chuyện Tôn giả Mục Kiền Liên cứu mẹ. Nhờ sức chú nguyện của chư Tăng sau mùa an cư, mẹ Ngài được thoát khổ. Từ đó, rằm tháng Bảy trở thành mùa báo hiếu.",
                "Tục cài hoa hồng trên áo nhắc ta trân quý cha mẹ khi còn có thể: hồng đỏ cho người còn mẹ, hồng trắng cho người đã mất mẹ.",
                "Báo hiếu không chỉ là lễ lạt. Một cuộc gọi về nhà, một bữa cơm cùng cha mẹ, hay sống tử tế để cha mẹ an lòng — đều là hiếu hạnh.",
            ),
            quote = "Hiếu là tâm Phật, hiếu là hạnh Phật.",
            quoteSource = "Kinh Phạm Võng",
        ),
        ArticleDetail(
            id = "art-4",
            category = "Tâm lý & đời sống",
            title = "Buông bỏ không phải là từ bỏ",
            author = "Thầy Minh Đạo",
            initials = "MĐ",
            date = "3 tháng 9",
            readTime = "5 phút đọc",
            imageId = "art-4",
            paragraphs = listOf(
                "Buông bỏ thường bị hiểu nhầm là thờ ơ, không còn quan tâm. Thật ra, buông bỏ là thôi nắm chặt — vẫn yêu thương, vẫn làm việc, nhưng không để kết quả trói buộc mình.",
                "Hãy hình dung bạn nắm một nắm cát: càng siết, cát càng rơi. Mở bàn tay ra, cát vẫn nằm yên trong lòng bàn tay.",
                "Khi lo lắng về một việc chưa xảy ra, thử tự hỏi: ngay lúc này, mình có thể làm gì? Làm điều đó, rồi để phần còn lại cho nhân duyên.",
            ),
            quote = "Hận thù không bao giờ dứt được bằng hận thù; chỉ có tình thương mới dứt được hận thù.",
            quoteSource = "Kinh Pháp Cú · kệ 5",
        ),
        ArticleDetail(
            id = "art-5",
            category = "Lịch sử",
            title = "Vua Trần Nhân Tông và thiền phái Trúc Lâm",
            author = "Ban biên tập InnerLamp",
            initials = "IL",
            date = "28 tháng 8",
            readTime = "7 phút đọc",
            imageId = "art-5",
            paragraphs = listOf(
                "Sau hai lần lãnh đạo quân dân đánh thắng quân Nguyên, vua Trần Nhân Tông nhường ngôi và lên núi Yên Tử tu hành, lập nên thiền phái Trúc Lâm — dòng thiền mang đậm bản sắc Việt.",
                "Tinh thần Trúc Lâm là “cư trần lạc đạo”: sống giữa đời mà vẫn vui với đạo. Không cần xa lánh gia đình, xã hội; ngay trong công việc hằng ngày vẫn có thể giữ tâm an.",
                "Ngày nay, các thiền viện Trúc Lâm khắp cả nước vẫn tiếp nối truyền thống này, mở cửa cho mọi người đến học thiền.",
            ),
            quote = "Ở đời vui đạo hãy tùy duyên.",
            quoteSource = "Cư trần lạc đạo phú · Trần Nhân Tông",
        ),
    )
}
