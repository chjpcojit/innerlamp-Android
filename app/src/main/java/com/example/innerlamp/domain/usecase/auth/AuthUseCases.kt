package com.example.innerlamp.domain.usecase.auth

import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.AuthException
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.model.EmailAddress
import com.example.innerlamp.domain.model.PasswordStrength
import com.example.innerlamp.domain.model.RegistrationForm
import com.example.innerlamp.domain.model.TempleProposal
import com.example.innerlamp.domain.repository.AuthRepository
import com.example.innerlamp.domain.repository.AuthSessionStore
import com.example.innerlamp.domain.repository.RegistrationRepository

private fun <T> fail(error: AuthError): Result<T> = Result.failure(AuthException(error))

/** Stores [session]; a storage failure becomes [AuthError.SessionStoreFailed]. / Lưu phiên; lỗi lưu thành [AuthError.SessionStoreFailed]. */
private suspend fun AuthSessionStore.saveOrFail(session: AuthSession): Result<AuthSession> =
    save(session).fold(
        onSuccess = { Result.success(session) },
        onFailure = { fail(AuthError.SessionStoreFailed) },
    )

/** Sign in with email + password, then store the session. / Đăng nhập email + mật khẩu rồi lưu phiên. */
class SignInUseCase(
    private val repository: AuthRepository,
    private val store: AuthSessionStore,
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthSession> {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return fail(AuthError.InvalidEmail)
        if (password.isEmpty()) return fail(AuthError.InvalidPassword)
        return repository.signIn(trimmed, password).fold(
            onSuccess = { store.saveOrFail(it) },
            onFailure = { Result.failure(it) },
        )
    }
}

/** Calls logout (result ignored, also offline / 401) then clears the device. / Gọi logout (bỏ qua kết quả) rồi xoá dữ liệu máy. */
class SignOutUseCase(
    private val repository: AuthRepository,
    private val store: AuthSessionStore,
) {
    suspend operator fun invoke() {
        repository.signOut()
        store.clear()
    }
}

/** Sends (or resends) the password-reset code. / Gửi (hoặc gửi lại) mã đặt lại mật khẩu. */
class RequestPasswordResetUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<CodeTiming> {
        val trimmed = email.trim()
        if (!EmailAddress.isValid(trimmed)) return fail(AuthError.InvalidEmail)
        return repository.requestResetCode(trimmed)
    }
}

/** Sets the new password; a stored session is cleaned (the backend revoked every session). / Đặt mật khẩu mới; dọn phiên đang lưu. */
class ConfirmPasswordResetUseCase(
    private val repository: AuthRepository,
    private val store: AuthSessionStore,
) {
    suspend operator fun invoke(email: String, code: String, newPassword: String): Result<Unit> {
        if (newPassword.length !in PasswordStrength.MIN_LENGTH..PasswordStrength.MAX_LENGTH) {
            return fail(AuthError.InvalidPassword)
        }
        return repository.confirmReset(email.trim(), code, newPassword).onSuccess {
            if (store.hasSession()) store.clear()
        }
    }
}

/** Submits the registration form; the code is emailed. / Gửi form đăng ký; mã được gửi qua email. */
class RequestRegistrationCodeUseCase(private val repository: RegistrationRepository) {
    suspend operator fun invoke(form: RegistrationForm): Result<CodeTiming> =
        repository.requestCode(form.copy(email = form.email.trim()))
}

class ResendRegistrationCodeUseCase(private val repository: RegistrationRepository) {
    suspend operator fun invoke(email: String): Result<CodeTiming> = repository.resendCode(email.trim())
}

/**
 * Confirms the code, stores the session, then sends the held temple proposal (its result is
 * ignored). / Xác nhận mã, lưu phiên, rồi gửi đề xuất chùa nếu có (bỏ qua kết quả).
 */
class VerifyRegistrationUseCase(
    private val repository: RegistrationRepository,
    private val store: AuthSessionStore,
) {
    suspend operator fun invoke(
        email: String,
        code: String,
        proposal: TempleProposal? = null,
    ): Result<AuthSession> =
        repository.verify(email.trim(), code).fold(
            onSuccess = { session ->
                store.saveOrFail(session).onSuccess { proposal?.let { repository.proposeTemple(it) } }
            },
            onFailure = { Result.failure(it) },
        )
}
