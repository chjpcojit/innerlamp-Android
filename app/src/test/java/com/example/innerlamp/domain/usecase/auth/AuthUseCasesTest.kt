package com.example.innerlamp.domain.usecase.auth

import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.AuthException
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.AuthUser
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.model.RegistrationForm
import com.example.innerlamp.domain.model.TempleProposal
import com.example.innerlamp.domain.repository.AuthRepository
import com.example.innerlamp.domain.repository.AuthSessionStore
import com.example.innerlamp.domain.repository.RegistrationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {

    private val session = AuthSession("a", "r", AuthUser(1, "a@b.vn", null, null))

    private class FakeStore(var saveFails: Boolean = false, var has: Boolean = false) : AuthSessionStore {
        var saved: AuthSession? = null
        var cleared = 0
        override suspend fun save(session: AuthSession): Result<Unit> =
            if (saveFails) Result.failure(IllegalStateException()) else Result.success(Unit.also { saved = session })

        override suspend fun updateTokens(accessToken: String, refreshToken: String) = Result.success(Unit)
        override suspend fun load() = Unit
        override suspend fun clear() {
            cleared++
        }

        override fun hasSession() = has
    }

    private class FakeAuthRepo(val signIn: Result<AuthSession>, val reset: Result<Unit> = Result.success(Unit)) :
        AuthRepository {
        var signInCalls = 0
        var signOutCalls = 0
        override suspend fun signIn(email: String, password: String): Result<AuthSession> {
            signInCalls++
            return signIn
        }

        override suspend fun signOut(): Result<Unit> {
            signOutCalls++
            return Result.failure(AuthException(AuthError.NoConnection))
        }

        override suspend fun requestResetCode(email: String) = Result.success(CodeTiming(600, 60))
        override suspend fun confirmReset(email: String, code: String, newPassword: String) = reset
    }

    private class FakeRegRepo(val verify: Result<AuthSession>) : RegistrationRepository {
        var proposed: TempleProposal? = null
        override suspend fun requestCode(form: RegistrationForm) = Result.success(CodeTiming(600, 60))
        override suspend fun resendCode(email: String) = Result.success(CodeTiming(600, 60))
        override suspend fun verify(email: String, code: String) = verify
        override suspend fun proposeTemple(proposal: TempleProposal): Result<Unit> {
            proposed = proposal
            return Result.failure(AuthException(AuthError.Generic)) // must not matter
        }
    }

    private fun errorOf(result: Result<*>) = (result.exceptionOrNull() as AuthException).error

    @Test
    fun signIn_emptyFields_doNotCallRepository() = runTest {
        val repo = FakeAuthRepo(Result.success(session))
        val useCase = SignInUseCase(repo, FakeStore())

        assertEquals(AuthError.InvalidEmail, errorOf(useCase("  ", "pw")))
        assertEquals(AuthError.InvalidPassword, errorOf(useCase("a@b.vn", "")))
        assertEquals(0, repo.signInCalls)
    }

    @Test
    fun signIn_success_storesSession() = runTest {
        val store = FakeStore()
        val result = SignInUseCase(FakeAuthRepo(Result.success(session)), store)("a@b.vn", "pw")

        assertEquals(session, result.getOrThrow())
        assertEquals(session, store.saved)
    }

    @Test
    fun signIn_storeFailure_isSessionStoreFailed() = runTest {
        val result = SignInUseCase(FakeAuthRepo(Result.success(session)), FakeStore(saveFails = true))("a@b.vn", "pw")
        assertEquals(AuthError.SessionStoreFailed, errorOf(result))
    }

    @Test
    fun signOut_clearsDeviceEvenWhenBackendFails() = runTest {
        val store = FakeStore()
        SignOutUseCase(FakeAuthRepo(Result.success(session)), store)()
        assertEquals(1, store.cleared)
    }

    @Test
    fun confirmReset_shortPassword_doesNotCallBackend_andSuccessCleansSession() = runTest {
        val store = FakeStore(has = true)
        val useCase = ConfirmPasswordResetUseCase(FakeAuthRepo(Result.success(session)), store)

        assertEquals(AuthError.InvalidPassword, errorOf(useCase("a@b.vn", "123456", "short")))
        assertEquals(0, store.cleared)

        assertTrue(useCase("a@b.vn", "123456", "longenough1").isSuccess)
        assertEquals(1, store.cleared)
    }

    @Test
    fun verifyRegistration_storesSession_andProposalFailureDoesNotBlock() = runTest {
        val store = FakeStore()
        val repo = FakeRegRepo(Result.success(session))
        val proposal = TempleProposal("Chùa A", "01", "0001", "")

        val result = VerifyRegistrationUseCase(repo, store)("a@b.vn", "123456", proposal)

        assertTrue(result.isSuccess)
        assertEquals(session, store.saved)
        assertEquals(proposal, repo.proposed)
    }

    @Test
    fun verifyRegistration_wrongCode_storesNothing() = runTest {
        val store = FakeStore()
        val repo = FakeRegRepo(Result.failure(AuthException(AuthError.OtpInvalid)))

        val result = VerifyRegistrationUseCase(repo, store)("a@b.vn", "000000")

        assertEquals(AuthError.OtpInvalid, errorOf(result))
        assertFalse(store.saved != null)
    }
}
