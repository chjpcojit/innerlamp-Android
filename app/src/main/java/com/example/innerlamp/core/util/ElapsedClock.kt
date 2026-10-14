package com.example.innerlamp.core.util

import android.os.SystemClock

/**
 * Đồng hồ đo khoảng thời gian đã trôi qua (monotonic), không bị ảnh hưởng khi người dùng
 * đổi giờ/múi giờ. Chỉ dùng để đo thời lượng/TTL, không dùng để hiển thị ngày giờ.
 */
interface ElapsedClock {
    /** Số mili-giây kể từ một mốc cố định (chỉ có nghĩa khi so sánh hai lần đọc). */
    fun elapsedMillis(): Long
}

/** Cài đặt mặc định dựa trên [SystemClock.elapsedRealtime]. */
object SystemElapsedClock : ElapsedClock {
    override fun elapsedMillis(): Long = SystemClock.elapsedRealtime()
}
