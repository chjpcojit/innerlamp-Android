package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.model.RegistrationForm
import com.example.innerlamp.domain.model.TempleProposal

/**
 * Contract for email registration. Failures are `AuthException`. /
 * Hợp đồng đăng ký bằng email. Lỗi là `AuthException`.
 */
interface RegistrationRepository {
    suspend fun requestCode(form: RegistrationForm): Result<CodeTiming>

    suspend fun resendCode(email: String): Result<CodeTiming>

    suspend fun verify(email: String, code: String): Result<AuthSession>

    suspend fun proposeTemple(proposal: TempleProposal): Result<Unit>
}
