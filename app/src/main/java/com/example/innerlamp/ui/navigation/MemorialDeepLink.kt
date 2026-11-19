package com.example.innerlamp.ui.navigation

/**
 * The one reminder deep link: the intent extra and its validation. A value from outside the app is only
 * accepted when it is a positive server id; anything else is ignored, so a forged intent can at most open the
 * book, never another account's entry (the book still resolves the id inside the signed-in account). /
 * Deep link duy nhất của nhắc giỗ: tên extra và kiểm tra. Chỉ nhận id server dương; giá trị khác bị bỏ qua,
 * nên intent giả cùng lắm mở sổ, không mở được mục của tài khoản khác.
 */
object MemorialDeepLink {

    const val EXTRA_OPEN_MEMORIAL_ID = "open_memorial_id"

    /** `null` for a missing, zero or negative id. / `null` khi thiếu id, id 0 hoặc âm. */
    fun parseOpenId(raw: Long?): Long? = raw?.takeIf { it > 0L }
}
