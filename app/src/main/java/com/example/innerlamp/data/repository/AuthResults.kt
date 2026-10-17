package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkResult
import com.example.innerlamp.data.mapper.toAuthError
import com.example.innerlamp.domain.model.AuthException

/** Unwraps a [NetworkResult] into `Result`, failing with `AuthException`. / Bóc [NetworkResult] thành `Result`, lỗi là `AuthException`. */
internal inline fun <T, R> NetworkResult<T>.toAuthResult(transform: (T) -> R): Result<R> = when (this) {
    is NetworkResult.Success -> Result.success(transform(data))
    is NetworkResult.Failure -> Result.failure(AuthException(error.toAuthError()))
}
