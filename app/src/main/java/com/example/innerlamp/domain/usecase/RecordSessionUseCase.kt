package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.Session
import com.example.innerlamp.domain.repository.SessionRepository

class RecordSessionUseCase(
    private val repository: SessionRepository,
) {
    suspend operator fun invoke(session: Session): Result<Unit> = runCatching {
        repository.recordSession(session)
    }
}
