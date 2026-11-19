package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.repository.MemorialRepository
import com.example.innerlamp.domain.reminder.MemorialReminderScheduler

/**
 * Deletes an entry and cancels its pending reminders. A 404 counts as deleted (client contract §3.5). /
 * Xoá một mục và huỷ nhắc đang chờ. 404 coi như đã xoá.
 */
class DeleteMemorialUseCase(
    private val repository: MemorialRepository,
    private val scheduler: MemorialReminderScheduler,
) {
    suspend operator fun invoke(entryId: Long, remaining: List<MemorialEntry>): Result<Unit> =
        repository.delete(entryId).onSuccess { scheduler.sync(remaining) }
}
