package com.example.innerlamp.domain.calendar

/**
 * BR-CAL-14: splits event text on line breaks only, never on commas or semicolons. An empty result
 * means the section is hidden (a long description is never replaced by the short one). /
 * BR-CAL-14: tách văn bản sự kiện chỉ theo xuống dòng, không bao giờ theo dấu phẩy / chấm phẩy. Kết
 * quả rỗng = ẩn mục (mô tả dài rỗng không thay bằng mô tả ngắn).
 */
object EventContentFormat {
    private val BULLETS = charArrayOf('-', '•', '*')

    /** One paragraph per non-empty line of `long_description`. / Mỗi dòng không rỗng = một đoạn. */
    fun paragraphs(longDescription: String): List<String> = lines(longDescription)

    /** One action per non-empty line of `guidance`, leading bullet dropped (numbered by the UI). */
    fun practices(guidance: String): List<String> =
        lines(guidance).map { it.trimStart(*BULLETS).trim() }.filter { it.isNotEmpty() }

    private fun lines(text: String): List<String> =
        text.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
}
