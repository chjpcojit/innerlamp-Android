package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.TalkChapter
import com.example.innerlamp.domain.model.TalkDetail

/**
 * Trả về nội dung một buổi pháp thoại của màn `talk`.
 *
 * Hiện dữ liệu là mẫu lấy từ prototype (`Docs/design_handoff_innerlamp/`, màn `talk`).
 * Giống trang chùa, prototype hiện dùng chung một template nên [talkId] chưa được dùng
 * để phân biệt nội dung.
 *
 * TODO(il-data-builder): data-bind theo [talkId] khi có tầng data (danh mục pháp thoại
 * thuộc "Data cần API" theo README). Lúc đó use case nhận repository qua constructor và
 * trả `Result<TalkDetail>` để UI xử lý được lỗi mạng.
 */
class GetTalkDetailUseCase {

    operator fun invoke(talkId: String): TalkDetail = TalkDetail(
        id = "tl-hieu-la-dao",
        kicker = "PHÁP THOẠI · MÙA VU LAN",
        title = "Hiếu là đạo,\nđạo cũng là hiếu",
        speaker = "Thầy Minh Đạo",
        initials = "Đ",
        durationMinutes = 46,
        listenCount = "2.4k lượt nghe",
        description = "Buổi giảng nói về hạnh hiếu trong đời sống người Phật tử tại gia: " +
            "hiếu không chỉ là nuôi dưỡng thân, mà là giúp cha mẹ an trong tâm.",
        chapters = listOf(
            TalkChapter(timestamp = "00:00", title = "Vì sao nói hiếu là gốc"),
            TalkChapter(timestamp = "11:20", title = "Câu chuyện Mục Kiền Liên"),
            TalkChapter(timestamp = "38:40", title = "Hồi hướng & hỏi đáp"),
        ),
    )
}
