package com.example.innerlamp.core.network.interceptor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A1 (G1): no request that carries memorial PII may reach the HTTP logger, in debug or release. Capturing
 * logcat needs a device, so this locks the decision function the interceptor branches on. /
 * A1: không request sổ giỗ nào được tới logger HTTP. Bắt logcat cần máy thật nên test khóa hàm quyết định.
 */
class LoggingInterceptorTest {

    @Test
    fun memorial_routes_are_never_logged() {
        assertFalse(LoggingInterceptor.shouldLog("/v1/memorials"))
        assertFalse(LoggingInterceptor.shouldLog("/v1/memorials/"))
        assertFalse(LoggingInterceptor.shouldLog("/v1/memorials/42"))
        assertFalse(LoggingInterceptor.shouldLog("/v1/memorials/42/photo"))
    }

    @Test
    fun auth_routes_are_never_logged() {
        assertFalse(LoggingInterceptor.shouldLog("/v1/auth/login"))
        assertFalse(LoggingInterceptor.shouldLog("/v1/auth/refresh"))
    }

    @Test
    fun other_routes_keep_their_debug_logging() {
        assertTrue(LoggingInterceptor.shouldLog("/v1/temples"))
        assertTrue(LoggingInterceptor.shouldLog("/v1/deities/top"))
        assertTrue(LoggingInterceptor.shouldLog("/v1/memorials-other"))
    }
}
