package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.NetworkClient
import com.example.innerlamp.data.mapper.toDomain
import com.example.innerlamp.data.remote.api.RegistrationApiService
import com.example.innerlamp.data.remote.dto.EmailRequestDto
import com.example.innerlamp.data.remote.dto.RegisterRequestDto
import com.example.innerlamp.data.remote.dto.TempleSuggestionRequestDto
import com.example.innerlamp.data.remote.dto.VerifyOtpRequestDto
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.model.RegistrationForm
import com.example.innerlamp.domain.model.TempleProposal
import com.example.innerlamp.domain.repository.RegistrationRepository

/** [RegistrationRepository] over `register/...` and `temple-suggestions` via `safeCall`. / Cài đặt qua `safeCall`. */
class RegistrationRepositoryImpl(
    private val api: RegistrationApiService,
    private val networkClient: NetworkClient,
) : RegistrationRepository {

    override suspend fun requestCode(form: RegistrationForm): Result<CodeTiming> =
        networkClient.safeCall {
            api.requestOtp(
                RegisterRequestDto(
                    email = form.email,
                    password = form.password,
                    phoneCountry = form.phoneCountry,
                    phoneNumber = form.phoneNumber,
                    fullName = form.fullName,
                    dharmaName = form.dharmaName?.takeIf { it.isNotBlank() },
                    ordinationTempleId = form.ordinationTempleId,
                ),
            )
        }.toAuthResult { it.toDomain() }

    override suspend fun resendCode(email: String): Result<CodeTiming> =
        networkClient.safeCall { api.resendOtp(EmailRequestDto(email)) }.toAuthResult { it.toDomain() }

    override suspend fun verify(email: String, code: String): Result<AuthSession> =
        networkClient.safeCall { api.verifyOtp(VerifyOtpRequestDto(email, code)) }
            .toAuthResult { it.toDomain() }

    override suspend fun proposeTemple(proposal: TempleProposal): Result<Unit> =
        networkClient.safeCall {
            api.suggestTemple(
                TempleSuggestionRequestDto(proposal.name, proposal.provinceCode, proposal.wardCode, proposal.address),
            )
        }.toAuthResult { }
}
