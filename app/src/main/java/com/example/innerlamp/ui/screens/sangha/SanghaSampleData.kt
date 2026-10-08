package com.example.innerlamp.ui.screens.sangha

import com.example.innerlamp.domain.model.MeditationGroup
import com.example.innerlamp.domain.model.SanghaPost
import com.example.innerlamp.domain.model.SanghaSummary
import com.example.innerlamp.domain.model.SanghaTone

/**
 * Dữ liệu mẫu cho màn Đạo tràng, lấy từ prototype `Docs/design_handoff_innerlamp/`.
 *
 * TODO(il-data-builder): thay bằng repository/API cộng đồng (bài đăng, lượt tùy hỷ,
 * số người đang ngồi thiền theo thời gian thực, đạo tràng của tôi). Giữ ở đây để UI
 * dựng được độc lập, giống `HomeSampleData`.
 */
internal object SanghaSampleData {

    /** Thẻ "Đang cùng ngồi thiền" (số người + mô tả). */
    val meditation = MeditationGroup(
        count = 312,
        label = "người trên khắp Việt Nam · phiên 20 phút",
    )

    /** Khối "Đạo tràng của tôi". */
    val mySangha = SanghaSummary(
        name = "Đạo tràng Từ Quang",
        memberCount = 184,
        place = "Q. Bình Thạnh",
        announcement = "Chủ Nhật này 8:00 — Tụng Kinh Vu Lan tại chánh điện, mời quý Phật tử về dự.",
    )

    /** Bảng tin "Chia sẻ của bạn đồng tu". */
    val posts = listOf(
        SanghaPost(
            id = "post-dh-30p",
            author = "Diệu Hạnh",
            initials = "DH",
            tone = SanghaTone.Rose,
            time = "2 giờ trước",
            meta = "41 ngày tinh tấn",
            body = "Sáng nay ngồi được 30 phút, lần đầu không nhìn đồng hồ. Cảm ơn đại chúng.",
            likes = 24,
            comments = 6,
        ),
        SanghaPost(
            id = "post-tn-vulan",
            author = "Sư cô Thích Nữ Tâm Nguyện",
            initials = "TN",
            tone = SanghaTone.Gold,
            time = "5 giờ trước",
            meta = "Chia sẻ pháp",
            body = "Mùa Vu Lan, nhiều người hỏi tôi làm sao báo hiếu khi cha mẹ đã mất. " +
                "Câu trả lời giản dị: hãy sống tử tế mỗi ngày. Mỗi lần bạn giữ được tâm " +
                "không nóng, đó đã là một nén hương dâng lên cha mẹ.",
            likes = 318,
            comments = 24,
        ),
        SanghaPost(
            id = "post-md-thienhanh",
            author = "Thầy Minh Đạo",
            initials = "MĐ",
            tone = SanghaTone.Green,
            time = "hôm qua",
            meta = "Pháp thoại",
            body = "Đi thiền không cần núi rừng. Mỗi bước chân từ bãi xe vào văn phòng, " +
                "nếu biết mình đang bước, đó đã là thiền hành. Hãy thử sáng mai: mười bước " +
                "đầu tiên, chỉ biết bước.",
            likes = 512,
            comments = 41,
        ),
    )
}
