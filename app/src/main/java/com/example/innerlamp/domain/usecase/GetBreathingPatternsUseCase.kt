package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.BreathingPattern
import com.example.innerlamp.domain.repository.SessionRepository

/**
 * Trả về catalog pattern. Repository đã lo fallback bundled nên use case này
 * gần như không bao giờ fail — vẫn giữ Result để tầng UI xử lý được lỗi thật.
 */
class GetBreathingPatternsUseCase(
    private val repository: SessionRepository,
) {
    suspend operator fun invoke(): Result<List<BreathingPattern>> = repository.getPatterns()
}
