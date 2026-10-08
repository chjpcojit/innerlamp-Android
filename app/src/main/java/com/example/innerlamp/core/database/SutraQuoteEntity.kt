package com.example.innerlamp.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.innerlamp.domain.model.SutraQuote

/** Bảng `SutraQuote`; mọi cột NOT NULL, `sequence` UNIQUE. */
@Entity(tableName = "SutraQuote", indices = [Index(value = ["sequence"], unique = true)])
data class SutraQuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sequence: Int,
    val sutraName: String,
    val translator: String,
    val chapterNumber: String,
    val chapterName: String,
    val verseNumber: Int,
    val content: String,
) {
    fun toDomain() = SutraQuote(sequence, sutraName, translator, chapterNumber, chapterName, verseNumber, content)

    companion object {
        fun from(q: SutraQuote) = SutraQuoteEntity(
            sequence = q.sequence,
            sutraName = q.sutraName,
            translator = q.translator,
            chapterNumber = q.chapterNumber,
            chapterName = q.chapterName,
            verseNumber = q.verseNumber,
            content = q.content,
        )
    }
}
