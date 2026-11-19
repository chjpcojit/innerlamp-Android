package com.example.innerlamp.data.repository

import com.example.innerlamp.core.database.memorial.MemorialCacheDao
import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.data.mapper.toCacheEntity
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.mapper.toMemorialError
import com.example.innerlamp.data.mapper.toRequestDto
import com.example.innerlamp.data.remote.api.MemorialApiService
import com.example.innerlamp.data.remote.dto.MemorialDto
import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialError
import com.example.innerlamp.domain.model.MemorialLoad
import com.example.innerlamp.domain.repository.MemorialRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * [MemorialRepository] over the seven routes. Server-first: every write needs the server; the device copy is
 * replaced after each successful read or write and is only used to read when the server fails (D5, AC32).
 * The copy is scoped to the signed-in account and never holds `photo_url` (D8, AC35). /
 * Repository trên bảy route. Server là nguồn đúng: mọi lần ghi cần server; bản trên máy được thay sau mỗi
 * lần đọc/ghi thành công và chỉ dùng để đọc khi server lỗi. Bản gắn với tài khoản, không giữ `photo_url`.
 */
class MemorialRepositoryImpl(
    private val api: MemorialApiService,
    private val networkClient: NetworkClient,
    private val dao: MemorialCacheDao,
    private val currentOwnerId: () -> Long?,
    private val dispatchers: DispatcherProvider,
) : MemorialRepository {

    override suspend fun load(): MemorialLoad {
        val owner = currentOwnerId() ?: return MemorialLoad.Guest
        return when (val result = networkClient.safeCall { api.list() }) {
            is NetworkResult.Success -> withContext(dispatchers.io) {
                try {
                    val entries = result.data.items.map { it.toDomain() }
                    dao.replaceAll(owner, entries.map { it.toCacheEntity(owner) })
                    // The copy belongs to this account only. / Bản trên máy chỉ thuộc tài khoản này.
                    dao.deleteOtherOwners(owner)
                    MemorialLoad.Loaded(entries)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    MemorialLoad.Failed(MemorialError.Failed)
                }
            }
            is NetworkResult.Failure -> {
                val error = result.error.toMemorialError()
                if (error == MemorialError.Unauthorized) {
                    withContext(dispatchers.io) { dao.deleteOwner(owner) }
                    MemorialLoad.Guest
                } else {
                    val cached = withContext(dispatchers.io) {
                        runCatching { dao.entries(owner).map { it.toDomain() } }.getOrDefault(emptyList())
                    }
                    if (cached.isEmpty()) MemorialLoad.Failed(error) else MemorialLoad.Offline(cached, error)
                }
            }
        }
    }

    override suspend fun create(entry: MemorialEntry): Result<MemorialEntry> =
        write(entry) { body -> api.create(body) }

    override suspend fun update(entry: MemorialEntry): Result<MemorialEntry> =
        write(entry) { body -> api.update(entry.id, body) }

    private suspend fun write(
        entry: MemorialEntry,
        call: suspend (com.example.innerlamp.data.remote.dto.MemorialRequestDto) -> ApiResponse<MemorialDto>,
    ): Result<MemorialEntry> {
        val owner = currentOwnerId() ?: return Result.failure(MemorialError.Unauthorized)
        val body = try {
            entry.toRequestDto()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(MemorialError.Failed)
        }
        return when (val result = networkClient.safeCall { call(body) }) {
            is NetworkResult.Success -> withContext(dispatchers.io) {
                try {
                    val saved = result.data.toDomain()
                    dao.upsert(saved.toCacheEntity(owner))
                    Result.success(saved)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(MemorialError.Failed)
                }
            }
            is NetworkResult.Failure -> {
                val error = result.error.toMemorialError()
                // A 404 on save means the entry was deleted elsewhere: drop it from the copy too.
                // 404 khi lưu = mục đã bị xoá nơi khác: bỏ khỏi bản trên máy.
                if (error == MemorialError.NotFound && !entry.isNew) {
                    withContext(dispatchers.io) { dao.deleteById(owner, entry.id) }
                }
                Result.failure(error)
            }
        }
    }

    override suspend fun delete(id: Long): Result<Unit> {
        val owner = currentOwnerId() ?: return Result.failure(MemorialError.Unauthorized)
        return when (val result = networkClient.safeCall { api.delete(id) }) {
            is NetworkResult.Success -> {
                withContext(dispatchers.io) { dao.deleteById(owner, id) }
                Result.success(Unit)
            }
            is NetworkResult.Failure -> {
                val error = result.error.toMemorialError()
                // Already gone elsewhere: drop the row and let the screen show "no longer in the book".
                // Đã bị xoá nơi khác: bỏ dòng và để màn hiện "mục không còn trong sổ".
                if (error == MemorialError.NotFound) {
                    withContext(dispatchers.io) { dao.deleteById(owner, id) }
                }
                Result.failure(error)
            }
        }
    }

    override suspend fun uploadPhoto(
        id: Long,
        bytes: ByteArray,
        mimeType: String,
        fileName: String,
    ): Result<MemorialEntry> {
        if (bytes.isEmpty() || mimeType.isBlank() || fileName.isBlank()) {
            return Result.failure(MemorialError.Validation("photo"))
        }
        if (bytes.size > MAX_PHOTO_BYTES) return Result.failure(MemorialError.TooLarge)
        val owner = currentOwnerId() ?: return Result.failure(MemorialError.Unauthorized)
        // The part name is fixed by the contract; type and filename come from the normalizer.
        // Tên part do contract quy định; MIME và tên file do bộ chuẩn hoá quyết định.
        val part = MultipartBody.Part.createFormData(
            "photo",
            fileName,
            bytes.toRequestBody(mimeType.toMediaType()),
        )
        return when (val result = networkClient.safeCall { api.uploadPhoto(id, part) }) {
            is NetworkResult.Success -> withContext(dispatchers.io) {
                try {
                    val saved = result.data.toDomain()
                    dao.upsert(saved.toCacheEntity(owner))
                    Result.success(saved)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(MemorialError.Failed)
                }
            }
            is NetworkResult.Failure -> Result.failure(result.error.toMemorialError())
        }
    }

    override suspend fun deletePhoto(id: Long): Result<MemorialEntry> {
        val owner = currentOwnerId() ?: return Result.failure(MemorialError.Unauthorized)
        return when (val result = networkClient.safeCall { api.deletePhoto(id) }) {
            is NetworkResult.Success -> withContext(dispatchers.io) {
                try {
                    val saved = result.data.toDomain()
                    dao.upsert(saved.toCacheEntity(owner))
                    Result.success(saved)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(MemorialError.Failed)
                }
            }
            is NetworkResult.Failure -> Result.failure(result.error.toMemorialError())
        }
    }

    override suspend fun clearCache() {
        withContext(dispatchers.io) { dao.clearAll() }
    }

    private companion object {
        /** 5 MiB, the server limit on the bytes actually sent (contract §3.6). / 5 MiB trên bytes thực gửi. */
        const val MAX_PHOTO_BYTES = 5 * 1024 * 1024
    }
}
