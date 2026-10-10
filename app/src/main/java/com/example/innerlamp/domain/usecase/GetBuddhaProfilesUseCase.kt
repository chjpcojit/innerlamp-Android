package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.BuddhaCategory
import com.example.innerlamp.domain.model.BuddhaMantra
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.model.BuddhaSutraRef

/**
 * Trả về danh sách chư Phật & Bồ Tát cho màn `buddhas` (và hồ sơ cho màn `buddha`).
 *
 * Hiện dữ liệu là mẫu lấy 1–1 từ prototype (`Docs/innerlamp-project/`, hằng `BUDDHAS`).
 *
 * TODO(il-data-builder): data-bind từ API khi có tầng data (README: "Data cần API"). Lúc đó
 * use case nhận repository qua constructor và trả `Result<List<BuddhaProfile>>` để UI hiện được
 * trạng thái rỗng / lỗi mạng như prototype (`buddhasEmpty`).
 */
class GetBuddhaProfilesUseCase {

    operator fun invoke(): List<BuddhaProfile> = PROFILES

    private companion object {
        /**
         * Prototype gắn kinh liên quan theo tên và rơi về "Kinh Pháp Cú" khi tên không có trong
         * danh mục kinh (`openSutra(... || 'phapcu')`). Giữ nguyên hành vi đó.
         */
        const val FALLBACK_SUTRA_ID = "phapcu"

        val PROFILES: List<BuddhaProfile> = listOf(
            BuddhaProfile(
                id = "thichca",
                name = "Đức Phật Thích Ca Mâu Ni",
                shortName = "Phật Thích Ca",
                sanskrit = "Śākyamuni Buddha",
                category = BuddhaCategory.Buddha,
                kindLabel = "Phật",
                realm = "Cõi Ta Bà",
                role = "Bổn Sư — vị Phật lịch sử, người khai sáng đạo Phật.",
                viaDay = 15,
                viaMonth = 4,
                viaName = "Đại lễ Phật Đản",
                bio = "Thái tử Tất Đạt Đa (Siddhārtha Gautama) sinh tại vườn Lâm Tỳ Ni, con vua Tịnh Phạn và " +
                    "hoàng hậu Ma Da, nước Ca Tỳ La Vệ, vào khoảng thế kỷ thứ 6 trước Công nguyên. Năm 29 tuổi, " +
                    "sau khi thấy cảnh già, bệnh, chết, Ngài từ bỏ cung vàng để tìm con đường thoát khổ. Sau sáu " +
                    "năm khổ hạnh không thành, Ngài chọn con đường trung đạo, ngồi thiền dưới cội bồ đề tại Bồ Đề " +
                    "Đạo Tràng và thành đạo năm 35 tuổi. Bài pháp đầu tiên tại vườn Lộc Uyển nói về Tứ Diệu Đế. " +
                    "Suốt 45 năm, Ngài đi khắp lưu vực sông Hằng giáo hóa mọi tầng lớp, rồi nhập Niết bàn tại " +
                    "Câu Thi Na năm 80 tuổi.",
                vowIntro = "Tứ hoằng thệ nguyện — bốn lời nguyện rộng lớn mà người phát tâm Bồ Đề đều hướng theo.",
                vows = listOf(
                    "Chúng sanh vô biên thệ nguyện độ",
                    "Phiền não vô tận thệ nguyện đoạn",
                    "Pháp môn vô lượng thệ nguyện học",
                    "Phật đạo vô thượng thệ nguyện thành",
                ),
                vowUnit = "lời nguyện",
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô Bổn Sư Thích Ca Mâu Ni Phật",
                        note = "Xưng niệm để tưởng nhớ ân đức bậc Thầy đã chỉ bày con đường giác ngộ.",
                    ),
                    BuddhaMantra(
                        label = "TÂM CHÚ",
                        text = "Tadyatha Om Muni Muni Maha Muniye Soha",
                        sanskrit = "Tadyathā oṃ mune mune mahāmunaye svāhā",
                        note = "Muni nghĩa là bậc Thánh tịch lặng — tán thán Đức Phật là bậc Đại Tịch Tĩnh.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef("phapcu", "Kinh Pháp Cú", "26 phẩm · 423 kệ"),
                    BuddhaSutraRef(FALLBACK_SUTRA_ID, "Kinh Pháp Hoa", "28 phẩm · Đại thừa"),
                ),
            ),
            BuddhaProfile(
                id = "adida",
                name = "Đức Phật A Di Đà",
                shortName = "Phật A Di Đà",
                sanskrit = "Amitābha · Amitāyus",
                category = BuddhaCategory.Buddha,
                kindLabel = "Phật",
                realm = "Tây Phương Cực Lạc",
                role = "Phật Vô Lượng Quang, Vô Lượng Thọ — giáo chủ cõi Cực Lạc phương Tây.",
                viaDay = 17,
                viaMonth = 11,
                viaName = "Vía Phật A Di Đà",
                bio = "Theo Kinh Vô Lượng Thọ, vô lượng kiếp về trước có một vị quốc vương nghe Đức Phật Thế Tự " +
                    "Tại Vương thuyết pháp, phát tâm xuất gia, hiệu là Tỳ kheo Pháp Tạng. Trải qua năm kiếp tư " +
                    "duy, Ngài phát 48 đại nguyện, nguyện lập một cõi nước thanh tịnh để tiếp độ tất cả chúng " +
                    "sanh niệm danh hiệu mình. Nay Ngài đã thành Phật, cõi nước ấy là thế giới Cực Lạc ở phương " +
                    "Tây. Cùng với Bồ Tát Quán Thế Âm và Đại Thế Chí, Ngài được tôn xưng là Tây Phương Tam " +
                    "Thánh. Pháp môn Tịnh Độ — chuyên niệm danh hiệu A Di Đà — là pháp tu phổ biến bậc nhất của " +
                    "Phật tử Việt Nam.",
                vowIntro = "Khi còn là Tỳ kheo Pháp Tạng, Ngài phát 48 đại nguyện trước Đức Phật Thế Tự Tại Vương. " +
                    "Dưới đây là những nguyện thường được nhắc đến.",
                vows = listOf(
                    "Nguyện 1 — Cõi nước của Ngài không có địa ngục, ngạ quỷ, súc sanh",
                    "Nguyện 12 — Quang minh vô lượng, soi khắp mười phương",
                    "Nguyện 13 — Thọ mạng vô lượng",
                    "Nguyện 18 — Chúng sanh chí tâm tin ưa, niệm danh hiệu dù chỉ mười niệm, đều được vãng sanh",
                    "Nguyện 19 — Người phát tâm Bồ Đề, tu các công đức, lúc lâm chung được Phật và thánh chúng hiện ra tiếp dẫn",
                ),
                vowUnit = "48 đại nguyện",
                vowTotal = 48,
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô A Di Đà Phật",
                        note = "Câu niệm Phật căn bản của pháp môn Tịnh Độ — có thể trì niệm mọi lúc, mọi nơi.",
                    ),
                    BuddhaMantra(
                        label = "TÂM CHÚ",
                        text = "Om Ami Dewa Hrih",
                        sanskrit = "Oṃ amideva hrīḥ",
                        note = "Tâm chú Phật A Di Đà theo truyền thống Kim Cương Thừa.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef("adida", "Kinh A Di Đà", "Tịnh Độ tông · 22 phút"),
                    BuddhaSutraRef(FALLBACK_SUTRA_ID, "Kinh Vô Lượng Thọ", "48 đại nguyện"),
                ),
            ),
            BuddhaProfile(
                id = "duocsu",
                name = "Đức Phật Dược Sư Lưu Ly Quang",
                shortName = "Phật Dược Sư",
                sanskrit = "Bhaiṣajyaguru",
                category = BuddhaCategory.Buddha,
                kindLabel = "Phật",
                realm = "Đông Phương Tịnh Lưu Ly",
                role = "Vị Phật chữa lành thân tâm, ban an lạc và tiêu trừ tật bệnh.",
                viaDay = 30,
                viaMonth = 9,
                viaName = "Vía Phật Dược Sư",
                bio = "Theo Kinh Dược Sư, ở phương Đông có thế giới Tịnh Lưu Ly, giáo chủ là Đức Dược Sư Lưu Ly " +
                    "Quang Như Lai. Khi còn tu hạnh Bồ Tát, Ngài phát mười hai đại nguyện, nguyện cho chúng sanh " +
                    "được đầy đủ thân tướng, thoát khỏi bệnh tật, nghèo khổ và tà kiến. Hầu cận hai bên Ngài là " +
                    "Bồ Tát Nhật Quang Biến Chiếu và Nguyệt Quang Biến Chiếu. Hình tượng Ngài thường cầm bình " +
                    "thuốc hoặc tháp báu, thân màu xanh lưu ly. Phật tử Việt Nam thường tụng Kinh Dược Sư để cầu " +
                    "an, cầu cho người bệnh sớm bình phục và trong các dịp đầu năm.",
                vowIntro = "Mười hai đại nguyện Ngài phát khi còn tu hạnh Bồ Tát (Kinh Dược Sư).",
                vows = listOf(
                    "Thân Ngài sáng rực chiếu khắp vô lượng thế giới, khiến chúng sanh đều được tướng hảo như Phật",
                    "Thân như lưu ly trong suốt, soi đường cho chúng sanh trong tăm tối",
                    "Dùng trí tuệ phương tiện giúp chúng sanh đầy đủ vật dụng, không thiếu thốn",
                    "Đưa người theo tà đạo trở về chánh đạo, người tu Tiểu thừa hướng về Đại thừa",
                    "Giúp người tu hành giữ giới thanh tịnh, không phạm giới",
                    "Người thân thể khiếm khuyết, tật bệnh được lành lặn, khỏe mạnh",
                    "Người bệnh tật, nghèo khổ, không nơi nương tựa được thân tâm an lạc",
                    "Người nữ muốn chuyển thân được toại nguyện",
                    "Giúp chúng sanh thoát lưới ma và tà kiến, trở về chánh kiến",
                    "Người bị lao ngục, hình phạt được thoát khổ nạn",
                    "Người đói khát vì mưu sinh mà tạo nghiệp được no đủ, rồi được nghe pháp",
                    "Người nghèo thiếu y phục được đầy đủ y phục và vật trang nghiêm",
                ),
                vowUnit = "12 đại nguyện",
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô Dược Sư Lưu Ly Quang Như Lai",
                        note = "Thường xưng niệm khi cầu an, cầu cho người bệnh.",
                    ),
                    BuddhaMantra(
                        label = "TÂM CHÚ",
                        text = "Tadyatha Om Bhaisajye Bhaisajye Maha Bhaisajya Raja Samudgate Svaha",
                        sanskrit = "Tadyathā oṃ bhaiṣajye bhaiṣajye mahābhaiṣajya-rāja-samudgate svāhā",
                        note = "Bhaiṣajya nghĩa là thuốc — tán thán vị Đại Dược Vương chữa lành khổ đau.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef("duocsu", "Kinh Dược Sư", "Cầu an · 45 phút"),
                ),
            ),
            BuddhaProfile(
                id = "dilac",
                name = "Đức Phật Di Lặc",
                shortName = "Phật Di Lặc",
                sanskrit = "Maitreya",
                category = BuddhaCategory.Buddha,
                kindLabel = "Phật tương lai",
                realm = "Nội viện Đâu Suất",
                role = "Vị Phật tương lai sẽ hạ sanh thành Phật ở cõi Ta Bà.",
                viaDay = 1,
                viaMonth = 1,
                viaName = "Vía Đức Di Lặc — Tết Nguyên Đán",
                bio = "Di Lặc (Maitreya) nghĩa là Từ Thị — bậc có lòng từ. Hiện nay Ngài là vị Bồ Tát Nhất sanh " +
                    "bổ xứ ở nội viện cung trời Đâu Suất, được Đức Phật Thích Ca thọ ký sẽ thành Phật kế tiếp " +
                    "tại cõi này, dưới cội long hoa, thuyết pháp ba hội độ chúng sanh. Hình tượng quen thuộc ở " +
                    "Việt Nam là vị Phật bụng lớn, miệng cười hoan hỷ, bắt nguồn từ Hòa thượng Bố Đại đời Hậu " +
                    "Lương (Trung Hoa), người được xem là hóa thân của Ngài. Ngày mùng Một Tết là ngày vía Đức " +
                    "Di Lặc; người Việt đến chùa lễ Phật, cầu một năm an vui.",
                vowIntro = "Hạnh nguyện của Ngài lấy lòng từ làm gốc.",
                vows = listOf(
                    "Tu Từ tâm tam muội — lấy lòng từ đem vui cho muôn loài",
                    "Hoan hỷ, bao dung — buông xả mọi phiền não, nhìn đời bằng nụ cười",
                    "Hạ sanh thành Phật dưới cội long hoa, thuyết pháp ba hội độ chúng sanh (Long Hoa tam hội)",
                ),
                vowUnit = "hạnh nguyện",
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô Đương Lai Hạ Sanh Di Lặc Tôn Phật",
                        note = "Thường xưng niệm vào dịp đầu năm, cầu an vui và hoan hỷ.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef(FALLBACK_SUTRA_ID, "Kinh Di Lặc Hạ Sanh", "Đang cập nhật audio"),
                ),
            ),
            BuddhaProfile(
                id = "quanam",
                name = "Bồ Tát Quán Thế Âm",
                shortName = "Quán Thế Âm",
                sanskrit = "Avalokiteśvara",
                category = BuddhaCategory.Bodhisattva,
                kindLabel = "Bồ Tát",
                realm = "Tây Phương Cực Lạc",
                role = "Bồ Tát đại bi — lắng nghe tiếng kêu khổ của chúng sanh để cứu độ.",
                viaDay = 19,
                viaMonth = 2,
                viaName = "Vía Quán Thế Âm đản sanh",
                extraVia = "Còn vía thành đạo 19/6 và xuất gia 19/9 âm lịch",
                bio = "Quán Thế Âm nghĩa là quán xét âm thanh của thế gian. Theo phẩm Phổ Môn trong Kinh Pháp " +
                    "Hoa, chúng sanh gặp khổ nạn mà nhất tâm xưng danh, Ngài liền quán tiếng mà đến cứu độ, tùy " +
                    "duyên hiện ba mươi ba ứng thân. Cùng Bồ Tát Đại Thế Chí, Ngài hầu cận Đức Phật A Di Đà ở " +
                    "cõi Cực Lạc. Ở Việt Nam, Ngài là vị Bồ Tát gần gũi nhất với người dân qua nhiều hình tượng: " +
                    "Quán Âm Nam Hải đứng trên sóng, Quán Âm Thị Kính, Phật Bà nghìn mắt nghìn tay. Mỗi năm có ba " +
                    "ngày vía Ngài: đản sanh 19/2, thành đạo 19/6 và xuất gia 19/9 âm lịch.",
                vowIntro = "Mười hai đại nguyện của Bồ Tát Quán Thế Âm, thường được tụng trong nghi thức cầu an.",
                vows = listOf(
                    "Nam mô hiệu Viên Thông, danh Tự Tại, Quán Âm Như Lai quảng phát hoằng thệ nguyện",
                    "Nam mô nhất niệm tâm vô quái ngại, Quán Âm Như Lai thường cư Nam Hải nguyện",
                    "Nam mô trụ Ta Bà, U Minh giới, Quán Âm Như Lai tầm thanh cứu khổ nguyện",
                    "Nam mô hàng tà ma, trừ yêu quái, Quán Âm Như Lai năng trừ nguy hiểm nguyện",
                    "Nam mô thanh tịnh bình, thùy dương liễu, Quán Âm Như Lai cam lồ sái tâm nguyện",
                    "Nam mô đại từ bi năng hỷ xả, Quán Âm Như Lai thường hành bình đẳng nguyện",
                    "Nam mô trú dạ tuần vô tổn hoại, Quán Âm Như Lai thệ diệt tam đồ nguyện",
                    "Nam mô vọng Nam nham cần lễ bái, Quán Âm Như Lai già tỏa giải thoát nguyện",
                    "Nam mô tạo pháp thuyền du khổ hải, Quán Âm Như Lai độ tận chúng sanh nguyện",
                    "Nam mô tiền tràng phan, hậu bảo cái, Quán Âm Như Lai tiếp dẫn Tây Phương nguyện",
                    "Nam mô Vô Lượng Thọ Phật cảnh giới, Quán Âm Như Lai Di Đà thọ ký nguyện",
                    "Nam mô đoan nghiêm thân vô tỉ trạch, Quán Âm Như Lai quả tu thập nhị nguyện",
                ),
                vowUnit = "12 đại nguyện",
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô Đại Từ Đại Bi Quán Thế Âm Bồ Tát",
                        note = "Xưng niệm khi gặp khó khăn, sợ hãi, hoặc cầu bình an cho người thân.",
                    ),
                    BuddhaMantra(
                        label = "LỤC TỰ ĐẠI MINH CHÚ",
                        text = "Án Ma Ni Bát Di Hồng",
                        sanskrit = "Oṃ maṇi padme hūṃ",
                        note = "Sáu âm tiết — ngọc quý trong hoa sen — biểu trưng lòng từ bi và trí tuệ hợp nhất.",
                    ),
                    BuddhaMantra(
                        label = "CHÚ ĐẠI BI",
                        text = "Nam mô Hắc ra đát na, đa ra dạ da…",
                        note = "84 câu, trích Kinh Thiên Thủ Thiên Nhãn. Mở toàn văn trong Thư viện để tụng theo.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef("phomon", "Phẩm Phổ Môn", "Kinh Pháp Hoa · phẩm 25"),
                    BuddhaSutraRef("daibi", "Chú Đại Bi", "84 câu · 8 phút"),
                    BuddhaSutraRef("tamkinh", "Bát Nhã Tâm Kinh", "260 chữ · 4 phút"),
                ),
            ),
            BuddhaProfile(
                id = "diatang",
                name = "Bồ Tát Địa Tạng Vương",
                shortName = "Địa Tạng Vương",
                sanskrit = "Kṣitigarbha",
                category = BuddhaCategory.Bodhisattva,
                kindLabel = "Bồ Tát",
                realm = "Cõi Ta Bà & các cõi khổ",
                role = "Bồ Tát đại nguyện — cứu độ chúng sanh nơi cõi khổ.",
                viaDay = 30,
                viaMonth = 7,
                viaName = "Vía Địa Tạng Vương Bồ Tát",
                bio = "Theo Kinh Địa Tạng Bổn Nguyện, Bồ Tát Địa Tạng được Đức Phật Thích Ca phó chúc giáo hóa " +
                    "chúng sanh trong khoảng thời gian từ khi Phật nhập diệt cho đến khi Đức Di Lặc ra đời. Qua " +
                    "nhiều tiền thân, Ngài phát nguyện chưa độ hết chúng sanh tội khổ thì chưa thành Phật. Hình " +
                    "tượng Ngài là vị tỳ kheo tay cầm tích trượng mở cửa địa ngục và viên minh châu soi sáng cõi " +
                    "tối tăm. Vào mùa Vu Lan tháng Bảy, Phật tử Việt Nam thường tụng Kinh Địa Tạng hồi hướng công " +
                    "đức cho ông bà, cha mẹ nhiều đời.",
                vowIntro = "“Địa ngục vị không, thệ bất thành Phật; chúng sanh độ tận, phương chứng Bồ Đề.” — " +
                    "Bốn tiền thân phát nguyện được kể trong Kinh Địa Tạng.",
                vows = listOf(
                    "Là con trai trưởng giả, thấy tướng hảo của Đức Phật Sư Tử Phấn Tấn Cụ Túc Vạn Hạnh, nguyện độ hết chúng sanh tội khổ",
                    "Là Thánh nữ dòng Bà La Môn, vì cứu mẹ mà phát nguyện độ khắp chúng sanh chịu khổ",
                    "Là một vị vua, nguyện nếu không độ hết chúng sanh tội khổ khiến được an vui thì chưa thành Phật",
                    "Là cô Quang Mục, vì cứu mẹ mà nguyện độ hết chúng sanh nơi ác đạo rồi mới thành Phật",
                ),
                vowUnit = "tiền thân phát nguyện",
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô Đại Nguyện Địa Tạng Vương Bồ Tát",
                        note = "Thường xưng niệm khi hồi hướng cho người đã khuất.",
                    ),
                    BuddhaMantra(
                        label = "TÂM CHÚ",
                        text = "Om Ha Ha Ha Vismaye Svaha",
                        sanskrit = "Oṃ ha ha ha vismaye svāhā",
                        note = "Tâm chú Địa Tạng, trì niệm để sám hối và hồi hướng.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef("diatang", "Kinh Địa Tạng Bổn Nguyện", "13 phẩm · mùa Vu Lan"),
                    BuddhaSutraRef("vulan", "Kinh Vu Lan Báo Hiếu", "Song ngữ · 38 phút"),
                ),
            ),
            BuddhaProfile(
                id = "vanthu",
                name = "Bồ Tát Văn Thù Sư Lợi",
                shortName = "Văn Thù",
                sanskrit = "Mañjuśrī",
                category = BuddhaCategory.Bodhisattva,
                kindLabel = "Bồ Tát",
                realm = "Hầu cận Phật Thích Ca",
                role = "Bồ Tát đại trí — biểu tượng của trí tuệ.",
                viaDay = 4,
                viaMonth = 4,
                viaName = "Vía Văn Thù Bồ Tát",
                bio = "Văn Thù Sư Lợi (Mañjuśrī) nghĩa là Diệu Cát Tường. Ngài cùng Bồ Tát Phổ Hiền hầu cận hai " +
                    "bên Đức Phật Thích Ca, hợp thành Hoa Nghiêm Tam Thánh: Văn Thù biểu trưng trí, Phổ Hiền biểu " +
                    "trưng hạnh. Hình tượng Ngài tay cầm kiếm báu chặt đứt vô minh, tay kia cầm kinh Bát Nhã, " +
                    "cưỡi sư tử xanh tượng trưng uy lực của trí tuệ. Trong nhiều kinh Đại thừa, Ngài là vị thượng " +
                    "thủ thưa hỏi Phật để mở bày pháp môn cho đại chúng. Người học Phật thường cầu Ngài gia hộ " +
                    "trí tuệ sáng suốt khi học hành, thi cử.",
                vowIntro = "Hạnh nguyện của Ngài gắn với trí tuệ Bát Nhã.",
                vows = listOf(
                    "Dùng kiếm trí tuệ chặt đứt phiền não, vô minh",
                    "Là vị thượng thủ thưa hỏi Phật, mở bày pháp môn cho đại chúng",
                    "Dẫn dắt người học đạo phát tâm Bồ Đề — như chỉ đường cho Thiện Tài đồng tử đi tham học (Kinh Hoa Nghiêm)",
                ),
                vowUnit = "hạnh nguyện",
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô Đại Trí Văn Thù Sư Lợi Bồ Tát",
                        note = "Xưng niệm để cầu trí tuệ, sự sáng suốt.",
                    ),
                    BuddhaMantra(
                        label = "NGŨ TỰ CHÂN NGÔN",
                        text = "Om A Ra Pa Ca Na Dhih",
                        sanskrit = "Oṃ a ra pa ca na dhīḥ",
                        note = "Năm chữ A Ra Pa Ca Na là năm âm của chân ngôn, Dhīḥ là chủng tử trí tuệ.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef("tamkinh", "Bát Nhã Tâm Kinh", "260 chữ · 4 phút"),
                    BuddhaSutraRef(FALLBACK_SUTRA_ID, "Kinh Hoa Nghiêm", "Phẩm Nhập Pháp Giới"),
                ),
            ),
            BuddhaProfile(
                id = "phohien",
                name = "Bồ Tát Phổ Hiền",
                shortName = "Phổ Hiền",
                sanskrit = "Samantabhadra",
                category = BuddhaCategory.Bodhisattva,
                kindLabel = "Bồ Tát",
                realm = "Hầu cận Phật Thích Ca",
                role = "Bồ Tát đại hạnh — biểu tượng của thực hành.",
                viaDay = 21,
                viaMonth = 2,
                viaName = "Vía Phổ Hiền Bồ Tát",
                bio = "Phổ Hiền (Samantabhadra) nghĩa là hiền thiện cùng khắp. Ngài cùng Bồ Tát Văn Thù hầu cận " +
                    "Đức Phật Thích Ca: nếu Văn Thù là trí tuệ thì Phổ Hiền là hạnh nguyện, là sự thực hành đem " +
                    "trí tuệ vào đời sống. Hình tượng Ngài cưỡi voi trắng sáu ngà, biểu trưng sáu pháp Ba La Mật " +
                    "vững chãi. Mười hạnh nguyện của Ngài trong Kinh Hoa Nghiêm được xem là khuôn mẫu tu tập của " +
                    "người Phật tử, thường được tụng trong các khóa lễ.",
                vowIntro = "Mười đại hạnh nguyện — phẩm Phổ Hiền Hạnh Nguyện, Kinh Hoa Nghiêm.",
                vows = listOf(
                    "Lễ kính chư Phật",
                    "Xưng tán Như Lai",
                    "Rộng tu cúng dường",
                    "Sám hối nghiệp chướng",
                    "Tùy hỷ công đức",
                    "Thỉnh chuyển pháp luân",
                    "Thỉnh Phật trụ thế",
                    "Thường theo học Phật",
                    "Hằng thuận chúng sanh",
                    "Phổ giai hồi hướng",
                ),
                vowUnit = "10 hạnh nguyện",
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô Đại Hạnh Phổ Hiền Bồ Tát",
                        note = "Xưng niệm để nhắc mình đem lời Phật dạy vào việc làm hằng ngày.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef(FALLBACK_SUTRA_ID, "Kinh Hoa Nghiêm", "Phẩm Phổ Hiền Hạnh Nguyện"),
                    BuddhaSutraRef(FALLBACK_SUTRA_ID, "Kinh Pháp Hoa", "Phẩm Phổ Hiền khuyến phát"),
                ),
            ),
            BuddhaProfile(
                id = "daithechi",
                name = "Bồ Tát Đại Thế Chí",
                shortName = "Đại Thế Chí",
                sanskrit = "Mahāsthāmaprāpta",
                category = BuddhaCategory.Bodhisattva,
                kindLabel = "Bồ Tát",
                realm = "Tây Phương Cực Lạc",
                role = "Bồ Tát đại lực — dùng ánh sáng trí tuệ chiếu khắp chúng sanh.",
                viaDay = 13,
                viaMonth = 7,
                viaName = "Vía Đại Thế Chí Bồ Tát",
                bio = "Đại Thế Chí nghĩa là bậc có thế lực lớn — sức mạnh của trí tuệ. Theo Kinh Quán Vô Lượng " +
                    "Thọ, ánh sáng trí tuệ của Ngài chiếu khắp, giúp chúng sanh lìa ba đường ác. Ngài cùng Bồ " +
                    "Tát Quán Thế Âm hầu cận Đức Phật A Di Đà. Trong Kinh Lăng Nghiêm, chương Niệm Phật Viên " +
                    "Thông, Ngài dạy pháp niệm Phật: nhiếp cả sáu căn, tịnh niệm tiếp nối — được xem là kim chỉ " +
                    "nam của người tu Tịnh Độ.",
                vowIntro = "Hạnh nguyện của Ngài gắn với pháp môn niệm Phật.",
                vows = listOf(
                    "Nhiếp cả sáu căn, tịnh niệm tiếp nối (đô nhiếp lục căn, tịnh niệm tương kế)",
                    "Tiếp dẫn người niệm Phật về cõi Tịnh Độ",
                    "Dùng ánh sáng trí tuệ chiếu khắp, giúp chúng sanh lìa ba đường ác",
                ),
                vowUnit = "hạnh nguyện",
                mantras = listOf(
                    BuddhaMantra(
                        label = "DANH HIỆU",
                        text = "Nam mô Đại Thế Chí Bồ Tát",
                        note = "Thường xưng niệm cùng danh hiệu Phật A Di Đà và Quán Thế Âm.",
                    ),
                ),
                sutras = listOf(
                    BuddhaSutraRef("adida", "Kinh A Di Đà", "Tịnh Độ tông · 22 phút"),
                ),
            ),
        )
    }
}
