package com.example.innerlamp.domain.model

/**
 * Kiểu dữ liệu của màn "Bài viết Phật pháp" (`article`).
 *
 * Pure Kotlin — không phụ thuộc Android/Compose/Room. Tầng `domain` sở hữu kiểu này;
 * tầng `data` (sau này) sẽ map DTO/entity về đây, còn `ui` chỉ render.
 */

/**
 * Toàn bộ nội dung một bài viết Phật pháp biên tập — dùng cho màn `article`
 * (khác với [Article] chỉ là phần tóm tắt hiển thị ở danh sách Trang chủ).
 *
 * [imageId] là mã ảnh minh họa (`art-1`, `art-2`, … khớp `img` của prototype);
 * app chưa có ảnh thật nên `ui` vẽ placeholder gradient.
 *
 * [paragraphs] là các đoạn thân bài, [quote]/[quoteSource] là câu trích dẫn có
 * viền vàng bên trái ở cuối bài.
 */
data class ArticleDetail(
    val id: String,
    val category: String,
    val title: String,
    val author: String,
    /** Chữ viết tắt tên tác giả (hiển thị trong avatar tròn 32dp). */
    val initials: String,
    val date: String,
    val readTime: String,
    val imageId: String,
    val paragraphs: List<String>,
    val quote: String,
    val quoteSource: String,
)
