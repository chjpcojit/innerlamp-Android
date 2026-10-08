package com.example.innerlamp.data.quote

import com.example.innerlamp.domain.model.SutraQuote
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Một phần tử của `sutra_quotes_300.json` (khoá giữ nguyên tiếng Việt không dấu như file nguồn). */
@Serializable
internal data class SutraQuoteDto(
    @SerialName("stt") val sequence: Int,
    @SerialName("kinh") val sutraName: String,
    @SerialName("dich_gia") val translator: String,
    @SerialName("pham_so") val chapterNumber: String,
    @SerialName("pham_ten") val chapterName: String,
    @SerialName("ke_so") val verseNumber: Int,
    @SerialName("noi_dung") val content: String,
) {
    fun toDomain() = SutraQuote(sequence, sutraName, translator, chapterNumber, chapterName, verseNumber, content)
}

/** Parse + chọn quote cần seed. Thuần Kotlin để test trên JVM. */
internal object SutraQuoteJson {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): List<SutraQuote> =
        json.decodeFromString<List<SutraQuoteDto>>(text).map { it.toDomain() }

    /**
     * Quote chưa có trong DB, sắp theo `sequence`. `sequence` trùng trong chính [catalog]
     * chỉ lấy bản đầu tiên.
     */
    fun newQuotes(catalog: List<SutraQuote>, existing: Set<Int>): List<SutraQuote> {
        val seen = existing.toMutableSet()
        return catalog.filter { seen.add(it.sequence) }.sortedBy { it.sequence }
    }
}
