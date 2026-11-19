package com.example.innerlamp.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A6 (G1): a reminder may open only a positive server id; anything else is ignored. /
 * A6: nhắc chỉ được mở id server dương; giá trị khác bị bỏ qua.
 */
class MemorialDeepLinkTest {

    @Test
    fun a_positive_id_is_accepted() {
        assertEquals(1L, MemorialDeepLink.parseOpenId(1L))
        assertEquals(42L, MemorialDeepLink.parseOpenId(42L))
        assertEquals(Long.MAX_VALUE, MemorialDeepLink.parseOpenId(Long.MAX_VALUE))
    }

    @Test
    fun missing_zero_and_negative_ids_are_rejected() {
        assertNull(MemorialDeepLink.parseOpenId(null))
        assertNull(MemorialDeepLink.parseOpenId(0L))
        assertNull(MemorialDeepLink.parseOpenId(-1L))
        assertNull(MemorialDeepLink.parseOpenId(Long.MIN_VALUE))
    }
}
