package com.example.innerlamp.core.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/** `Retry-After` is exposed on [AppError.ApiError] / [AppError.HttpError] (used by the 429 case). */
class NetworkErrorMapperRetryAfterTest {

    private val mapper = NetworkErrorMapper(JsonProvider.json)

    private fun http429(body: String, retryAfter: String?): HttpException {
        val raw = okhttp3.Response.Builder()
            .request(Request.Builder().url("http://localhost/").build())
            .protocol(Protocol.HTTP_1_1).code(429).message("Too Many Requests")
            .apply { retryAfter?.let { header("Retry-After", it) } }
            .build()
        return HttpException(Response.error<Any>(body.toResponseBody("application/json".toMediaType()), raw))
    }

    @Test
    fun envelopeBody_keepsHeaderValue() {
        val body = """{"success":false,"error":{"code":"RATE_LIMITED","message":"m","type":"business"}}"""
        val e = mapper.map(http429(body, "12")) as AppError.ApiError
        assertEquals("RATE_LIMITED", e.code)
        assertEquals(12, e.retryAfterSeconds)
    }

    @Test
    fun plainBody_keepsHeaderValue() {
        val e = mapper.map(http429("busy", "5")) as AppError.HttpError
        assertEquals(429, e.code)
        assertEquals(5, e.retryAfterSeconds)
    }

    @Test
    fun missingOrNonNumericHeader_isNull() {
        assertNull((mapper.map(http429("busy", null)) as AppError.HttpError).retryAfterSeconds)
        assertNull((mapper.map(http429("busy", "Wed, 21 Oct 2026 07:28:00 GMT")) as AppError.HttpError).retryAfterSeconds)
    }
}
