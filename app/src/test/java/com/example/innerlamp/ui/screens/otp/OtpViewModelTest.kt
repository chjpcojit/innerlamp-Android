package com.example.innerlamp.ui.screens.otp

import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.AuthException
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.AuthUser
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.domain.usecase.auth.ResendRegistrationCodeUseCase
import com.example.innerlamp.domain.usecase.auth.VerifyRegistrationUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OtpViewModelTest {

    private val verify = mockk<VerifyRegistrationUseCase>()
    private val resend = mockk<ResendRegistrationCodeUseCase>()
    private val session = AuthSession("a", "r", AuthUser(1, "a@b.vn", null, null))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = OtpViewModel("a@b.vn", CodeTiming(600, 60), verify, resend)

    @Test
    fun shortCode_doesNotCallBackend() {
        val vm = viewModel()
        vm.onCodeChange("123")

        vm.onConfirm()

        coVerify(exactly = 0) { verify(any(), any(), any()) }
        assertEquals("Vui lòng nhập đủ 6 chữ số.", vm.uiState.value.error)
    }

    @Test
    fun correctCode_marksVerified() {
        coEvery { verify("a@b.vn", "123456", any()) } returns Result.success(session)
        val vm = viewModel()
        vm.onCodeChange("123456")

        vm.onConfirm()

        assertTrue(vm.uiState.value.verified)
    }

    @Test
    fun wrongCode_keepsInput_andShowsError() {
        coEvery { verify(any(), any(), any()) } returns Result.failure(AuthException(AuthError.OtpInvalid))
        val vm = viewModel()
        vm.onCodeChange("000000")

        vm.onConfirm()

        assertEquals("000000", vm.uiState.value.code)
        assertEquals("Mã xác thực không đúng.", vm.uiState.value.error)
    }

    @Test
    fun registrationExpired_exitsToSignup() {
        coEvery { verify(any(), any(), any()) } returns Result.failure(AuthException(AuthError.PendingRegistrationExpired))
        val vm = viewModel()
        vm.onCodeChange("123456")

        vm.onConfirm()

        assertEquals(AuthError.PendingRegistrationExpired, vm.uiState.value.exit)
    }

    @Test
    fun otpLocked_disablesInput() {
        coEvery { verify(any(), any(), any()) } returns Result.failure(AuthException(AuthError.OtpLocked(null)))
        val vm = viewModel()
        vm.onCodeChange("123456")

        vm.onConfirm()

        assertTrue(vm.uiState.value.locked)
    }

    @Test
    fun resendBeforeWaitEnds_isIgnored() {
        val vm = viewModel()

        vm.onResend()

        coVerify(exactly = 0) { resend(any()) }
    }
}
