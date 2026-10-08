package com.example.innerlamp.core.network

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import retrofit2.HttpException

/**
 * Map exception thô (mạng / serialization / HTTP) → [AppError], không để lộ kiểu
 * `retrofit2.*`/`okhttp3.*`/`java.io.*` ra ngoài lớp network.
 */
class NetworkErrorMapper(private val json: Json) {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is UnknownHostException -> AppError.NoConnection
        is ConnectException -> AppError.NoConnection
        is SocketTimeoutException -> AppError.Timeout
        is HttpException -> mapHttpException(throwable)
        is SerializationException -> AppError.Serialization
        else -> AppError.Unknown(throwable)
    }

    private fun mapHttpException(e: HttpException): AppError {
        if (e.code() == 401) return AppError.Unauthorized

        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val envelope = body?.let(::parseEnvelope)
        val detail = envelope?.error
        return if (detail != null) {
            AppError.ApiError(
                code = detail.code,
                message = detail.message,
                type = detail.type,
                details = envelope.details,
            )
        } else {
            AppError.HttpError(e.code(), e.message())
        }
    }

    private fun parseEnvelope(body: String): ApiResponse<JsonElement>? = runCatching {
        json.decodeFromString<ApiResponse<JsonElement>>(body)
    }.getOrNull()
}
