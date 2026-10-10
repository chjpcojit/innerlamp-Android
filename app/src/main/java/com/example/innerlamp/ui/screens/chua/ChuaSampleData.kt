package com.example.innerlamp.ui.screens.chua

import com.example.innerlamp.domain.model.ChuaEvent
import com.example.innerlamp.domain.model.ChuaEventAction
import com.example.innerlamp.domain.model.ChuaNews
import com.example.innerlamp.domain.model.ChuaOnlineSession
import com.example.innerlamp.domain.model.ChuaRetreat
import com.example.innerlamp.domain.model.ChuaTemple
import com.example.innerlamp.domain.usecase.GetTemplesUseCase

/**
 * Dữ liệu mẫu cho tab Chùa, lấy từ prototype `Docs/innerlamp-project/` (`chuaVals`).
 *
 * TODO(il-data-builder): thay bằng repository/API chùa (danh bạ + tọa độ GPS để tính
 * "gần tôi", thông báo, sự kiện do ban hộ tự đăng, buổi đạo tràng trực tuyến). Giữ ở đây
 * để UI dựng được độc lập, giống `HomeSampleData`.
 */
internal object ChuaSampleData {

    /** Id chùa tu tập mặc định (khớp `TempleRoute("tu-quang")`). */
    const val DEFAULT_TEMPLE_ID = "tu-quang"

    /** Khoảng cách mẫu (km) từ Quận 1, TP.HCM tới từng chùa — prototype tính từ tọa độ. */
    private val distances = mapOf(
        DEFAULT_TEMPLE_ID to 4.8,
        "tp-vinhnghiem" to 2.1,
        "tp-truclam" to 233.0,
        "tp-linhung" to 760.0,
        "tp-thienmu" to 1048.0,
        "tp-baidinh" to 1135.0,
        "tp-tranquoc" to 1138.0,
        "tp-motcot" to 1140.0,
        "tp-huong" to 1160.0,
    )

    private val abbots = mapOf(DEFAULT_TEMPLE_ID to "Đại đức Thích Minh Đạo")

    /** Danh bạ chùa: Từ Quang (chùa tu tập mẫu) + danh bạ chung của Trang chủ. */
    val directory: List<ChuaTemple> by lazy {
        val tuQuang = ChuaTemple(
            id = DEFAULT_TEMPLE_ID,
            name = "Chùa Từ Quang",
            place = "Bình Thạnh, TP. Hồ Chí Minh",
            province = "TP. Hồ Chí Minh",
            abbot = abbots[DEFAULT_TEMPLE_ID],
            distanceKm = distances[DEFAULT_TEMPLE_ID],
        )
        val others = GetTemplesUseCase()().map { t ->
            ChuaTemple(
                id = t.id,
                name = t.name,
                place = t.place,
                province = t.place.substringAfterLast(", "),
                abbot = abbots[t.id],
                distanceKm = distances[t.id],
            )
        }
        listOf(tuQuang) + others
    }

    /** Số Phật tử mẫu chọn chùa làm chùa tu tập. */
    fun peerCount(temple: ChuaTemple): Int =
        if (temple.id == DEFAULT_TEMPLE_ID) 184 else 600 + (temple.name.length * 37) % 900

    val news = listOf(
        ChuaNews(
            time = "Hôm nay",
            text = "Khóa tu một ngày an lạc tháng này nhận ghi danh đến hết thứ Sáu. " +
                "Quý Phật tử ghi danh trong ứng dụng hoặc tại văn phòng chùa.",
        ),
        ChuaNews(
            time = "3 ngày trước",
            text = "Từ tuần này, khóa thiền tối thứ Năm dời sang 19:30 để Phật tử đi làm về kịp tham dự.",
        ),
    )

    val events = listOf(
        ChuaEvent(
            id = "ev-thien-toi",
            day = "08",
            month = "TH10",
            title = "Thiền tối thứ Năm",
            sub = "Hằng tuần · 19:30 · chánh điện",
            action = ChuaEventAction.Remind,
        ),
        ChuaEvent(
            id = "ev-khoa-tu",
            day = "11",
            month = "TH10",
            title = "Khóa tu một ngày an lạc",
            sub = "Chủ Nhật · 7:00–16:00 · còn 12 chỗ",
            action = ChuaEventAction.Reserve,
        ),
    )

    val online = listOf(
        ChuaOnlineSession(
            id = "on-thien-toi",
            tag = "HẰNG NGÀY · 20:00",
            title = "Thiền tối trực tuyến",
            sub = "Thiền viện Trúc Lâm · 20 phút · khoảng 300 người cùng ngồi",
            isDaily = true,
        ),
        ChuaOnlineSession(
            id = "on-pho-mon",
            tag = "RẰM & MÙNG MỘT · 19:30",
            title = "Tụng Kinh Phổ Môn qua livestream",
            sub = "Chùa Vĩnh Nghiêm · khoảng 45 phút",
            isDaily = false,
        ),
    )

    val retreats = listOf(
        ChuaRetreat(
            id = "rt-an-lac",
            templeId = DEFAULT_TEMPLE_ID,
            day = "11",
            month = "TH10",
            title = "Khóa tu một ngày an lạc",
            sub = "Chùa Từ Quang · Bình Thạnh · còn 12 chỗ",
        ),
        ChuaRetreat(
            id = "rt-ba-ngay",
            templeId = "tp-truclam",
            day = "23",
            month = "TH10",
            title = "Khóa thiền ba ngày",
            sub = "Thiền viện Trúc Lâm · Đà Lạt",
        ),
    )
}
