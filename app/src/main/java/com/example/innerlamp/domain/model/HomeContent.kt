package com.example.innerlamp.domain.model

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
    /** Card image (`thumbnail_url`); `null` → placeholder. / Ảnh thẻ; `null` → nền placeholder. */
    val imageUrl: String? = null,
)

/** Bài viết Phật pháp biên tập ở trang chủ. */
data class Article(
    val id: String,
    val category: String,
    val title: String,
    val author: String,
    val readTime: String,
)
