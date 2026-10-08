package com.example.innerlamp.data.quote

import com.example.innerlamp.core.database.SutraQuoteDao
import com.example.innerlamp.core.database.SutraQuoteEntity
import com.example.innerlamp.core.util.DefaultDispatcherProvider
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.domain.model.SutraQuote
import com.example.innerlamp.domain.repository.SutraQuoteRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Cài đặt [SutraQuoteRepository] trên Room. [loadCatalog] trả nội dung JSON bundled
 * (đọc từ assets); mọi thao tác chạy trên IO.
 */
class RoomSutraQuoteRepository(
    private val dao: SutraQuoteDao,
    private val loadCatalog: suspend () -> String,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider,
) : SutraQuoteRepository {

    // Seed lúc launch và seed "DB rỗng lúc đọc" có thể chạy đồng thời.
    private val seedLock = Mutex()

    override suspend fun seedIfNeeded() = withContext(dispatchers.io) {
        seedLock.withLock {
            val catalog = SutraQuoteJson.parse(loadCatalog())
            val fresh = SutraQuoteJson.newQuotes(catalog, dao.allSequences().toSet())
            if (fresh.isNotEmpty()) dao.insertAll(fresh.map(SutraQuoteEntity::from))
        }
    }

    override suspend fun count(): Int = withContext(dispatchers.io) { dao.count() }

    override suspend fun quoteAt(index: Int): SutraQuote? = withContext(dispatchers.io) {
        if (index < 0) null else dao.atOffset(index)?.toDomain()
    }

    override suspend fun getAll(): List<SutraQuote> = withContext(dispatchers.io) {
        dao.getAll().map(SutraQuoteEntity::toDomain)
    }
}
