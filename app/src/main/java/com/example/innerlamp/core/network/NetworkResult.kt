package com.example.innerlamp.core.network

/**
 * Kiểu trả về duy nhất của lớp network: hoặc [Success] chứa dữ liệu đã
 * deserialize, hoặc [Failure] chứa [AppError].
 */
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Failure(val error: AppError) : NetworkResult<Nothing>
}
