package com.example.innerlamp.core.network

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Test JVM cho [NetworkErrorMapper]: map đủ các case lỗi về [AppError].
 */
class NetworkErrorMapperTest {

    private val mapper = NetworkErrorMapper(JsonProvider.json)

    @Test
    fun unknownHost_mapsToNoConnection() {
        assertEquals(AppError.NoConnection, mapper.map(UnknownHostException("no host")))
    }

    @Test
    fun connectException_mapsToNoConnection() {
        assertEquals(AppError.NoConnection, mapper.map(ConnectException("refused")))
    }

    @Test
    fun socketTimeout_mapsToTimeout() {
        assertEquals(AppError.Timeout, mapper.map(SocketTimeoutException("timeout")))
    }

    @Test
    fun serializationException_mapsToSerialization() {
        assertEquals(AppError.Serialization, mapper.map(SerializationException("bad json")))
    }

    @Test
    fun http401_mapsToUnauthorized() {
        val e = HttpException(Response.error<Any>(401, "unauthorized".toResponseBody()))
        assertEquals(AppError.Unauthorized, mapper.map(e))
    }

    @Test
    fun httpError_withParseableEnvelope_mapsToApiError() {
        val body = """
            {"success":false,"error":{"code":"VALIDATION_FAILED","message":"email sai","type":"validation"},
             "details":{"email":"da ton tai"}}
        """.trimIndent()
        val e = HttpException(
            Response.error<Any>(422, body.toResponseBody("application/json".toMediaType())),
        )

        val result = mapper.map(e)
        assertTrue(result is AppError.ApiError)
        result as AppError.ApiError
        assertEquals("VALIDATION_FAILED", result.code)
        assertEquals("email sai", result.message)
        assertEquals("validation", result.type)
        assertEquals(JsonPrimitive("da ton tai"), result.details?.get("email"))
    }

    @Test
    fun httpError_withoutParseableBody_mapsToHttpError() {
        val e = HttpException(
            Response.error<Any>(500, "<html>oops</html>".toResponseBody("text/html".toMediaType())),
        )

        val result = mapper.map(e)
        assertTrue(result is AppError.HttpError)
        result as AppError.HttpError
        assertEquals(500, result.code)
    }

    @Test
    fun httpError_withEmptyErrorBody_mapsToHttpError() {
        val e = HttpException(
            Response.error<Any>(503, "".toResponseBody("application/json".toMediaType())),
        )

        val result = mapper.map(e)
        assertTrue(result is AppError.HttpError)
        assertEquals(503, (result as AppError.HttpError).code)
    }

    @Test
    fun genericException_mapsToUnknown() {
        val cause = IllegalStateException("boom")
        val result = mapper.map(cause)
        assertTrue(result is AppError.Unknown)
        assertTrue((result as AppError.Unknown).cause === cause)
    }
}
