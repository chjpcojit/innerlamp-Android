package com.example.innerlamp.core.network.auth

/**
 * Đánh dấu endpoint Retrofit cần gắn header `Authorization: Bearer <token>`.
 * Mặc định không đánh dấu = endpoint public (opt-in rõ ràng).
 *
 * `@Retention(RUNTIME)` để [AuthInterceptor] đọc được annotation qua reflection.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authenticated
