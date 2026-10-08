package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.BreathingPattern
import com.example.innerlamp.domain.model.Session
import kotlinx.coroutines.flow.Flow

/**
 * Hợp đồng do tầng `domain` sở hữu, tầng `data` implement.
 * Domain không biết Room/Retrofit tồn tại.
 */
interface SessionRepository {

    fun observeHistory(): Flow<List<Session>>

    suspend fun recordSession(session: Session)

    /**
     * Lấy danh sách pattern. Fallback về catalog bundled khi backend chưa có
     * hoặc mất mạng — app phải dùng được offline.
     */
    suspend fun getPatterns(): Result<List<BreathingPattern>>

    /** Đẩy các session chưa sync lên backend. No-op nếu chưa cấu hình base URL. */
    suspend fun syncPendingSessions(): Result<Unit>
}
