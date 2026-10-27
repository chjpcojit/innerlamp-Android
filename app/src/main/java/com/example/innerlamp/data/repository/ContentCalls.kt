package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.core.network.AppError
import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.domain.model.ContentError
import kotlinx.coroutines.CancellationException

/**
 * One public catalogue call: [call] through `safeCall`, then [map]; any failure becomes a [ContentError]
 * ([notFoundCode] → [ContentError.NotFound]). A mapping exception (bad payload) is [ContentError.Failed]. /
 * Một lần gọi danh mục công khai: [call] qua `safeCall` rồi [map]; mọi lỗi thành [ContentError]
 * ([notFoundCode] → [ContentError.NotFound]). Lỗi khi map (payload hỏng) là [ContentError.Failed].
 */
internal suspend fun <T : Any, R> NetworkClient.loadContent(
    notFoundCode: String? = null,
    call: suspend () -> ApiResponse<T>,
    map: (T) -> R,
): Result<R> = when (val result = safeCall(call)) {
    is NetworkResult.Success -> try {
        Result.success(map(result.data))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(ContentError.Failed)
    }
    is NetworkResult.Failure -> Result.failure(result.error.toContentError(notFoundCode))
}

/**
 * Reads pages 1…`total_pages` in turn and concatenates the items; stops early on an empty page and never
 * reads more than [maxPages] pages. Any page failing fails the whole read. /
 * Đọc lần lượt trang 1…`total_pages` rồi nối các mục; dừng sớm khi gặp trang rỗng và không đọc quá
 * [maxPages] trang. Một trang lỗi thì cả lần đọc lỗi.
 */
internal suspend fun <P : Any, I> NetworkClient.loadAllPages(
    call: suspend (page: Int) -> ApiResponse<P>,
    items: (P) -> List<I>,
    totalPages: (P) -> Int,
    maxPages: Int = MAX_PAGES,
): Result<List<I>> {
    val all = mutableListOf<I>()
    var page = 1
    while (page <= maxPages) {
        val current = loadContent(call = { call(page) }, map = { it }).getOrElse { return Result.failure(it) }
        val pageItems = items(current)
        all += pageItems
        if (pageItems.isEmpty() || page >= totalPages(current)) break
        page++
    }
    return Result.success(all)
}

/** Upper bound for [loadAllPages] (20 items per page → 1 000 items). / Trần số trang của [loadAllPages]. */
internal const val MAX_PAGES = 50

internal fun AppError.toContentError(notFoundCode: String?): ContentError = when (this) {
    AppError.NoConnection, AppError.Timeout -> ContentError.Offline
    is AppError.ApiError -> if (notFoundCode != null && code == notFoundCode) ContentError.NotFound else ContentError.Failed
    is AppError.HttpError -> if (notFoundCode != null && code == HTTP_NOT_FOUND) ContentError.NotFound else ContentError.Failed
    else -> ContentError.Failed
}

private const val HTTP_NOT_FOUND = 404
