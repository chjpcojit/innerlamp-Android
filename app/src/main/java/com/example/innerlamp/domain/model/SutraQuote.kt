package com.example.innerlamp.domain.model

/**
 * Một câu kinh (Pháp Cú) dùng làm quote. Pure Kotlin — không phụ thuộc Android/Room.
 *
 * @property sequence số thứ tự trong catalog (`stt` của JSON), duy nhất, sắp xếp tăng dần.
 * @property chapterNumber số phẩm dạng La Mã, vd "I".
 * @property verseNumber số kệ trong kinh.
 */
data class SutraQuote(
    val sequence: Int,
    val sutraName: String,
    val translator: String,
    val chapterNumber: String,
    val chapterName: String,
    val verseNumber: Int,
    val content: String,
)

/** Nguồn hiển thị dưới câu kinh, vd "Kinh Pháp Cú (Dhammapada) · Phẩm Song Yếu · kệ 5". */
val SutraQuote.sourceLabel: String get() = "$sutraName · $chapterName · kệ $verseNumber"

/** Chuyển sang [Verse] cho thẻ hero: ngắt dòng sau dấu câu (`, . ; : ! ?`). */
fun SutraQuote.toVerse(): Verse = Verse(
    lines = content.trim().split(Regex("(?<=[,.;:!?])\\s+")).filter { it.isNotBlank() },
    source = sourceLabel,
)
