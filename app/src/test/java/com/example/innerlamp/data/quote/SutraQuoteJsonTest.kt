package com.example.innerlamp.data.quote

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SutraQuoteJsonTest {

    private fun item(stt: Int, text: String = "x") = """
        {"stt": $stt, "kinh": "K", "dich_gia": "D", "pham_so": "II", "pham_ten": "P", "ke_so": 7, "noi_dung": "$text"}
    """.trimIndent()

    @Test
    fun parse_maps_all_fields() {
        val q = SutraQuoteJson.parse("[${item(3, "nội dung")}]").single()
        assertEquals(3, q.sequence)
        assertEquals("K", q.sutraName)
        assertEquals("D", q.translator)
        assertEquals("II", q.chapterNumber)
        assertEquals("P", q.chapterName)
        assertEquals(7, q.verseNumber)
        assertEquals("nội dung", q.content)
    }

    @Test
    fun new_quotes_skips_existing_sequences_and_never_touches_old_rows() {
        val catalog = SutraQuoteJson.parse("[${item(1)},${item(2)},${item(3)}]")
        assertEquals(listOf(2, 3), SutraQuoteJson.newQuotes(catalog, setOf(1)).map { it.sequence })
        assertTrue(SutraQuoteJson.newQuotes(catalog, setOf(1, 2, 3)).isEmpty())
    }

    @Test
    fun new_quotes_keeps_first_of_duplicate_sequences_in_file() {
        val catalog = SutraQuoteJson.parse("[${item(1, "đầu")},${item(1, "sau")},${item(2)}]")
        val fresh = SutraQuoteJson.newQuotes(catalog, emptySet())
        assertEquals(listOf(1, 2), fresh.map { it.sequence })
        assertEquals("đầu", fresh.first().content)
    }

    @Test
    fun bundled_asset_has_300_unique_quotes() {
        val text = File("src/main/assets/sutra_quotes_300.json").readText()
        val quotes = SutraQuoteJson.parse(text)
        assertEquals(300, quotes.size)
        assertEquals((1..300).toList(), quotes.map { it.sequence }.sorted())
        assertTrue(quotes.all { it.content.isNotBlank() && it.chapterNumber.isNotBlank() })
    }
}
