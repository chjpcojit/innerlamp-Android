package com.example.innerlamp.data.remote.api

import com.example.innerlamp.core.network.ApiResponse
import com.example.innerlamp.data.remote.dto.CalendarEventsPageDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Calendar endpoint (contract 2.2.0, `calendarEvents`). Public: NOT `@Authenticated`, so no
 * `Authorization` is sent, and never an `X-API-Key` (website only; a wrong key gives 401). /
 * Endpoint lịch lễ (contract 2.2.0). Công khai: không `@Authenticated` nên không gửi `Authorization`,
 * và không bao giờ gửi `X-API-Key` (chỉ dành cho website; sai key trả 401).
 */
interface CalendarApiService {

    /** Always an explicit [year] (AC2); the response is `Cache-Control: public` only with a year. */
    @GET("v1/calendar/events")
    suspend fun getEvents(@Query("year") year: Int): ApiResponse<CalendarEventsPageDto>
}
