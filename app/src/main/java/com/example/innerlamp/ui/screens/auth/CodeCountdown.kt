package com.example.innerlamp.ui.screens.auth

import com.example.innerlamp.core.util.ElapsedClock
import com.example.innerlamp.core.util.SystemElapsedClock
import com.example.innerlamp.domain.model.CodeTiming

/**
 * Two countdowns of an emailed code (validity and resend wait), measured with elapsed time so a
 * clock change cannot disturb them. Durations always come from the server. /
 * Hai bộ đếm của mã gửi qua email (hiệu lực và thời gian chờ gửi lại), đo bằng elapsed time.
 */
class CodeCountdown(private val clock: ElapsedClock = SystemElapsedClock) {
    private var expiresAt = 0L
    private var resendAt = 0L

    fun start(timing: CodeTiming) {
        val now = clock.elapsedMillis()
        expiresAt = now + timing.expiresInSeconds * 1000L
        resendAt = now + timing.resendAfterSeconds * 1000L
    }

    /** Restart only the resend wait (e.g. `OTP_RESEND_TOO_SOON`). / Chỉ đặt lại thời gian chờ gửi lại. */
    fun restartResend(seconds: Int) {
        resendAt = clock.elapsedMillis() + seconds * 1000L
    }

    fun expirySeconds(): Int = remaining(expiresAt)

    fun resendSeconds(): Int = remaining(resendAt)

    private fun remaining(deadline: Long): Int =
        ((deadline - clock.elapsedMillis() + 999L) / 1000L).coerceAtLeast(0L).toInt()
}
