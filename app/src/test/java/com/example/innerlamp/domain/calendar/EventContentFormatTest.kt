package com.example.innerlamp.domain.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventContentFormatTest {

    @Test
    fun paragraphs_splitOnNewlinesOnly_neverOnCommaOrSemicolon() {
        val text = "Đoạn một, có phẩy; chấm phẩy.\n\n  Đoạn hai \r\nĐoạn ba"
        assertEquals(
            listOf("Đoạn một, có phẩy; chấm phẩy.", "Đoạn hai", "Đoạn ba"),
            EventContentFormat.paragraphs(text),
        )
    }

    @Test
    fun practices_dropLeadingBullets_andEmptyLines() {
        val guidance = "- Đi chùa, lễ Phật\n• Tụng kinh; niệm Phật\n*  Làm việc thiện\n\n-\nThả phóng sinh"
        assertEquals(
            listOf("Đi chùa, lễ Phật", "Tụng kinh; niệm Phật", "Làm việc thiện", "Thả phóng sinh"),
            EventContentFormat.practices(guidance),
        )
    }

    @Test
    fun emptyInput_givesEmptyList_soTheSectionIsHidden() {
        assertTrue(EventContentFormat.paragraphs("").isEmpty())
        assertTrue(EventContentFormat.paragraphs(" \n \n").isEmpty())
        assertTrue(EventContentFormat.practices("").isEmpty())
    }
}
