package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.MemorialLoad
import com.example.innerlamp.domain.reminder.MemorialReminderScheduler
import com.example.innerlamp.domain.repository.MemorialRepository

/**
 * Loads the book and keeps the device reminders in step with it. Called when the screen opens, when it
 * returns to the foreground and after a retry. /
 * Tải sổ và đồng bộ nhắc trên máy theo sổ đó. Gọi khi mở màn, khi quay lại foreground và khi thử lại.
 */
class LoadMemorialsUseCase(
    private val repository: MemorialRepository,
    private val scheduler: MemorialReminderScheduler,
) {
    suspend operator fun invoke(): MemorialLoad {
        val load = repository.load()
        when (load) {
            is MemorialLoad.Loaded -> scheduler.sync(load.entries)
            is MemorialLoad.Offline -> scheduler.sync(load.entries)
            MemorialLoad.Guest, is MemorialLoad.Failed -> Unit
        }
        return load
    }
}
