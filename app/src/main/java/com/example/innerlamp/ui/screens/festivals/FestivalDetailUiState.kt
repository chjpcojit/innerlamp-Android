package com.example.innerlamp.ui.screens.festivals

import com.example.innerlamp.domain.calendar.CalendarCountdown
import com.example.innerlamp.domain.calendar.EventContentFormat
import com.example.innerlamp.domain.repository.CalendarLoadError
import com.example.innerlamp.domain.usecase.EventOccurrenceDetail
import java.time.LocalDate

/** Render state of the festival detail (AC14–AC18). / State render của chi tiết ngày lễ. */
sealed interface FestivalDetailUiState {
    data object Loading : FestivalDetailUiState

    /** The occurrence exists; text sections are derived per BR-CAL-14. */
    data class Found(val detail: EventOccurrenceDetail, val today: LocalDate) : FestivalDetailUiState {
        private val event get() = detail.entry.event
        private val occurrence get() = detail.entry.occurrence

        val groupNames: List<String> get() = detail.groups.map { it.name }

        /** One paragraph per line of `long_description`; empty hides the section (never the short one). */
        val paragraphs: List<String> get() = EventContentFormat.paragraphs(event.longDescription)

        /** One numbered action per line of `guidance`; empty hides the section. */
        val practices: List<String> get() = EventContentFormat.practices(event.guidance)

        val countdown: CalendarCountdown.Countdown get() = CalendarCountdown.of(today, occurrence.solarDate)
    }

    /** `(eventCode, solarDate)` is not in the year, or the date is malformed (AC18). */
    data object NotFound : FestivalDetailUiState

    data class Error(val error: CalendarLoadError) : FestivalDetailUiState
}
