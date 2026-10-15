# Repository pattern (worked example)

The canonical shape of a feature's data layer. This is a complete example for the **session history**
feature (which already has `domain/repository/SessionRepository` + `domain/model/Session`), using both
Room and the network. Every other feature follows exactly this shape.

Code comments follow the repo's language policy (`CLAUDE.md`): bilingual, English first, then
Vietnamese.

## File layout

```
data/local/entity/SessionEntity.kt
data/local/dao/SessionDao.kt
data/local/InnerLampDatabase.kt
data/remote/dto/SessionDto.kt
data/remote/api/SessionApiService.kt
data/mapper/SessionMapper.kt
data/repository/SessionRepositoryImpl.kt
di/SessionModule.kt
```

## 1. Entity (Room)

```kotlin
package com.example.innerlamp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room record for one meditation session. Not a domain model.
 * Bản ghi Room cho một buổi thiền. Không phải model domain.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val patternId: String,
    val startedAtEpochMillis: Long,
    val durationSeconds: Int,
    val completedCycles: Int,
    val completed: Boolean,
    val synced: Boolean, // "pushed to backend" flag for offline-first / cờ "đã đẩy lên backend" cho offline-first
)
```

## 2. DAO

```kotlin
package com.example.innerlamp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.innerlamp.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startedAtEpochMillis DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE synced = 0")
    suspend fun pendingSync(): List<SessionEntity>

    @Query("UPDATE sessions SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}
```

## 3. Database

```kotlin
package com.example.innerlamp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.innerlamp.data.local.dao.SessionDao
import com.example.innerlamp.data.local.entity.SessionEntity

@Database(entities = [SessionEntity::class], version = 1, exportSchema = true)
abstract class InnerLampDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
```

## 4. DTO (network, `@Serializable`)

```kotlin
package com.example.innerlamp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    val id: String,
    val patternId: String,
    val startedAtEpochMillis: Long,
    val durationSeconds: Int,
    val completedCycles: Int,
    val completed: Boolean,
)
```

## 5. API service

```kotlin
package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.auth.Authenticated
import com.example.innerlamp.data.remote.dto.SessionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SessionApiService {
    @Authenticated
    @GET("sessions")
    suspend fun getSessions(): ApiResponse<List<SessionDto>>

    @Authenticated
    @POST("sessions")
    suspend fun createSession(@Body dto: SessionDto): ApiResponse<SessionDto>
}
```

> `ApiResponse<T>` is the backend's shared envelope (`success/data/error/details`). Read
> `Docs/Specs/Networking/design.md` before using it.

## 6. Mapper

```kotlin
package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.local.entity.SessionEntity
import com.example.innerlamp.data.remote.dto.SessionDto
import com.example.innerlamp.domain.model.Session

fun SessionEntity.toDomain(): Session = Session(
    id = id, patternId = patternId, startedAtEpochMillis = startedAtEpochMillis,
    durationSeconds = durationSeconds, completedCycles = completedCycles, completed = completed,
)

fun Session.toEntity(synced: Boolean = false): SessionEntity = SessionEntity(
    id = id, patternId = patternId, startedAtEpochMillis = startedAtEpochMillis,
    durationSeconds = durationSeconds, completedCycles = completedCycles,
    completed = completed, synced = synced,
)

fun SessionDto.toDomain(): Session = Session(
    id = id, patternId = patternId, startedAtEpochMillis = startedAtEpochMillis,
    durationSeconds = durationSeconds, completedCycles = completedCycles, completed = completed,
)

fun Session.toDto(): SessionDto = SessionDto(
    id = id, patternId = patternId, startedAtEpochMillis = startedAtEpochMillis,
    durationSeconds = durationSeconds, completedCycles = completedCycles, completed = completed,
)
```

## 7. Repository impl (offline-first + sync)

```kotlin
package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkException
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.local.dao.SessionDao
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.mapper.toDto
import com.example.innerlamp.data.mapper.toEntity
import com.example.innerlamp.data.remote.api.SessionApiService
import com.example.innerlamp.domain.model.BreathingPattern
import com.example.innerlamp.domain.model.Session
import com.example.innerlamp.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val dao: SessionDao,
    private val api: SessionApiService,
    private val networkClient: NetworkClient,
    private val dispatchers: DispatcherProvider,
) : SessionRepository {

    override fun observeHistory(): Flow<List<Session>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun recordSession(session: Session) {
        withContext(dispatchers.io) {
            dao.upsert(session.toEntity(synced = false))
        }
    }

    override suspend fun getPatterns(): Result<List<BreathingPattern>> =
        // Offline-first: bundled catalog fallback first, backend updates later.
        // Offline-first: catalog bundled fallback trước, backend cập nhật sau.
        Result.success(bundledPatterns())

    override suspend fun syncPendingSessions(): Result<Unit> {
        val pending = dao.pendingSync()
        if (pending.isEmpty()) return Result.success(Unit)
        return withContext(dispatchers.io) {
            pending.forEach { entity ->
                when (val r = networkClient.safeCall { api.createSession(entity.toDomain().toDto()) }) {
                    is NetworkResult.Success -> dao.markSynced(entity.id)
                    is NetworkResult.Failure -> return@withContext Result.failure(NetworkException(r.error))
                }
            }
            Result.success(Unit)
        }
    }

    private fun bundledPatterns(): List<BreathingPattern> = TODO("catalog bundled (offline fallback)")
}
```

Points to get right:

- `observeHistory()` returns a `Flow` **straight from the DAO**, mapped to the domain model — no network.
- `recordSession()` writes locally first (`synced = false`) and does not wait for the network.
- `syncPendingSessions()` pushes only unsynced records; a network error → `Result.failure(NetworkException(...))`.
- `domain` only sees `Session`/`Result`; `NetworkException`/`NetworkResult` appear only in `data`.
- All I/O runs on `dispatchers.io` via `withContext`; never call `Dispatchers.IO` directly.

## 8. DI wiring (manual)

```kotlin
package com.example.innerlamp.di

import com.example.innerlamp.data.local.InnerLampDatabase
import com.example.innerlamp.data.remote.api.SessionApiService
import com.example.innerlamp.data.repository.SessionRepositoryImpl
import com.example.innerlamp.domain.repository.SessionRepository
import com.example.innerlamp.domain.usecase.ObserveSessionHistoryUseCase
import com.example.innerlamp.domain.usecase.RecordSessionUseCase

object SessionModule {
    fun provideRepository(
        database: InnerLampDatabase,
        api: SessionApiService,
        networkClient: NetworkClient,
        dispatchers: DispatcherProvider,
    ): SessionRepository = SessionRepositoryImpl(
        dao = database.sessionDao(),
        api = api,
        networkClient = networkClient,
        dispatchers = dispatchers,
    )

    fun provideObserveHistory(repo: SessionRepository) = ObserveSessionHistoryUseCase(repo)
    fun provideRecordSession(repo: SessionRepository) = RecordSessionUseCase(repo)
}
```

> `NetworkClient`/`Json`/`OkHttpClient`/`Retrofit` are provided by the shared `di/NetworkModule.kt` —
> do not rebuild them in a feature module. `api` is created via `networkClient.createService(SessionApiService::class.java)`.
