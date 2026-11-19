package com.example.innerlamp.core.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A3 (G1): the work input carries no relationship and no name — only these four keys. /
 * A3: input của WorkManager không có quan hệ/tên — chỉ bốn key này.
 */
class MemorialReminderPayloadTest {

    private val payload = MemorialReminderPayload(
        entryId = 42L,
        daysBefore = 3,
        time = "07:00",
        anniversaryKey = "42:2026-11-23",
    )

    @Test
    fun the_input_has_exactly_the_four_allowed_keys() {
        val data = payload.toData()
        assertEquals(MemorialReminderPayload.KEYS, data.keyValueMap.keys)
        assertEquals(4, data.keyValueMap.size)
    }

    @Test
    fun the_input_keeps_the_values_the_worker_needs() {
        val data = payload.toData()
        assertEquals(42L, data.getLong(MemorialReminderPayload.KEY_ENTRY_ID, 0L))
        assertEquals(3, data.getInt(MemorialReminderPayload.KEY_DAYS_BEFORE, 0))
        assertEquals("07:00", data.getString(MemorialReminderPayload.KEY_TIME))
        assertEquals("42:2026-11-23", data.getString(MemorialReminderPayload.KEY_ANNIVERSARY))
    }

    @Test
    fun there_is_no_field_for_a_name_or_a_relationship() {
        val data = payload.toData()
        assertNull(data.getString("relationship"))
        assertNull(data.getString("full_name"))
        assertNull(data.getString("name"))
        assertNull(data.getString("relation"))
    }
}
