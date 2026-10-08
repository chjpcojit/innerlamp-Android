package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.Session
import com.example.innerlamp.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionHistoryUseCase(
    private val repository: SessionRepository,
) {
    operator fun invoke(): Flow<List<Session>> = repository.observeHistory()
}
