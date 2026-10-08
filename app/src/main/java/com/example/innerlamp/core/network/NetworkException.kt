package com.example.innerlamp.core.network

/**
 * Cầu nối để tầng `data` bọc [AppError] vào `Result.failure` mà tầng `domain`
 * vẫn chỉ thấy `Throwable` — không bao giờ phải import `core/network`.
 */
class NetworkException(val error: AppError) : Exception(error.toString())
