package com.example.innerlamp.ui.screens.login

import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.AuthException
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.AuthUser
import com.example.innerlamp.domain.usecase.auth.SignInUseCase
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
class LoginViewModelTest {

    private val signIn = mockk<SignInUseCase>()
    private val session = AuthSession("a", "r", AuthUser(1, "a@b.vn", null, null))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(email: String = "a@b.vn", password: String = "pw") =
        LoginViewModel(signIn).apply {
            onEmailChange(email)
            onPasswordChange(password)
        }

    @Test
    fun emptyPassword_showsError_andDoesNotCallUseCase() {
        val vm = viewModel(password = "")

        vm.onLoginClick()

        assertEquals("Vui lòng nhập mật khẩu.", vm.uiState.value.passwordError)
        coVerify(exactly = 0) { signIn(any(), any()) }
    }

    @Test
    fun success_setsLoggedIn() {
        coEvery { signIn("a@b.vn", "pw") } returns Result.success(session)
        val vm = viewModel()

        vm.onLoginClick()

        assertTrue(vm.uiState.value.loggedIn)
    }

    @Test
    fun invalidCredentials_keepsEmail_clearsPassword() {
        coEvery { signIn(any(), any()) } returns Result.failure(AuthException(AuthError.InvalidCredentials))
        val vm = viewModel()

        vm.onLoginClick()

        val state = vm.uiState.value
        assertEquals("a@b.vn", state.email)
        assertEquals("", state.password)
        assertEquals("Email hoặc mật khẩu không đúng.", state.passwordError)
    }

    @Test
    fun loginLocked_showsLockedBanner() {
        coEvery { signIn(any(), any()) } returns Result.failure(AuthException(AuthError.LoginLocked(null)))
        val vm = viewModel()

        vm.onLoginClick()

        assertTrue(vm.uiState.value.banner is LoginBanner.Locked)
    }

    @Test
    fun noConnection_showsNetworkBanner() {
        coEvery { signIn(any(), any()) } returns Result.failure(AuthException(AuthError.NoConnection))
        val vm = viewModel()

        vm.onLoginClick()

        assertEquals(LoginBanner.Network, vm.uiState.value.banner)
    }
}
