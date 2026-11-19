package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO of `Memorial` (client contract v1, §2.1). Unknown fields are ignored by the shared [Json]; every
 * optional field has a default so a missing one never breaks decoding. /
 * DTO của `Memorial`. Field lạ được bỏ qua; field tuỳ chọn có default để thiếu cũng không hỏng decode.
 */
@Serializable
data class MemorialDto(
    val id: Long = 0,
    @SerialName("full_name") val fullName: String = "",
    val relationship: String = "",
    @SerialName("death_calendar") val deathCalendar: String = "lunar",
    @SerialName("death_date") val deathDate: String? = null,
    @SerialName("death_day") val deathDay: Int? = null,
    @SerialName("death_month") val deathMonth: Int? = null,
    @SerialName("death_leap_month") val deathLeapMonth: Boolean? = null,
    val note: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    val reminder: MemorialReminderDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

/**
 * `reminder` of the response. `timezone` and `add_to_os_reminder` are deliberately absent: unknown fields
 * are ignored and never written back (contract §0.1–0.2). /
 * `reminder` của response. Cố ý không khai `timezone`/`add_to_os_reminder`: field lạ bị bỏ qua và không
 * bao giờ được ghi lại.
 */
@Serializable
data class MemorialReminderDto(
    @SerialName("days_before") val daysBefore: Int = 0,
    val time: String = "07:00",
)

@Serializable
data class MemorialListDto(
    val items: List<MemorialDto> = emptyList(),
    val total: Int = 0,
)

/**
 * Body of `POST`/`PUT` (contract §2.3–2.4). The shared `Json` drops null fields, so exactly one date form
 * is sent, a blank note is cleared and `reminder = null` turns reminders off. /
 * Body `POST`/`PUT`. `Json` bỏ field null nên chỉ một dạng ngày được gửi, ghi chú rỗng bị xoá và
 * `reminder = null` là tắt nhắc.
 */
@Serializable
data class MemorialRequestDto(
    @SerialName("full_name") val fullName: String,
    val relationship: String,
    @SerialName("death_calendar") val deathCalendar: String,
    @SerialName("death_date") val deathDate: String? = null,
    @SerialName("death_day") val deathDay: Int? = null,
    @SerialName("death_month") val deathMonth: Int? = null,
    @SerialName("death_leap_month") val deathLeapMonth: Boolean? = null,
    val note: String? = null,
    val reminder: MemorialReminderDto? = null,
)
