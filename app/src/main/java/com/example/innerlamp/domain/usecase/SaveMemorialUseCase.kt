package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.repository.MemorialRepository

/**
 * Creates or replaces an entry. A new entry (`id == 0`) is `POST`ed, everything else `PUT` with the whole
 * body. Nothing is written offline (D5). /
 * Tạo hoặc thay một mục. Mục mới (`id == 0`) gọi `POST`, còn lại `PUT` toàn bộ. Không ghi khi offline.
 */
class SaveMemorialUseCase(private val repository: MemorialRepository) {
    suspend operator fun invoke(entry: MemorialEntry): Result<MemorialEntry> =
        if (entry.isNew) repository.create(entry) else repository.update(entry)
}
