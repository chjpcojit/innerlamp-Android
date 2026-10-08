package com.example.innerlamp.domain.model

import java.time.LocalDate

/** Sự kiện Phật giáo trong tháng (lễ vía hoặc ngày trai giới Mùng Một/Rằm). */
data class BuddhistEvent(
    val date: LocalDate,
    val title: String,
    /** Ngày âm dạng "15/7 âm lịch". */
    val lunarLabel: String,
    /** `true` = lễ vía/đại lễ (tô màu nhấn), `false` = chỉ là ngày trai giới. */
    val isFestival: Boolean,
)

/** Các sự kiện của một tháng dương lịch. */
data class EventMonth(
    val year: Int,
    val month: Int,
    val events: List<BuddhistEvent>,
)

/** Câu kinh hiển thị trên thẻ lịch âm ở trang chủ. */
data class Verse(
    val lines: List<String>,
    val source: String,
)

/** Chùa Việt Nam trong carousel "Chùa Việt Nam". */
data class Temple(
    val id: String,
    val name: String,
    val place: String,
    val region: String,
    val note: String,
)

/** Bài viết Phật pháp biên tập ở trang chủ. */
data class Article(
    val id: String,
    val category: String,
    val title: String,
    val author: String,
    val readTime: String,
)

/** Một vị Phật/Bồ Tát trong khối "Thư viện Phật & Bồ Tát" ở trang chủ. */
data class BuddhaFigure(
    val id: String,
    /** Nhãn pill trên ảnh: "Phật", "Bồ Tát", "Phật tương lai". */
    val kind: String,
    /** Tên ngắn hiển thị dưới ảnh, vd "Phật Thích Ca". */
    val shortName: String,
    /** Ngày vía âm lịch dạng "dd/mm". */
    val viaLabel: String,
)
