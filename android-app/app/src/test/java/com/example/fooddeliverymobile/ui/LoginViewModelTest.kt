package com.example.fooddeliverymobile.ui

import com.example.fooddeliverymobile.data.AuthGateway
import com.example.fooddeliverymobile.data.AuthSession
import com.example.fooddeliverymobile.data.MobileRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun successfulClientLoginCreatesSession() = runTest(dispatcher) {
        val gateway = FakeGateway(Result.success(session(MobileRole.CLIENT)))
        val viewModel = LoginViewModel(gateway)
        viewModel.updateUsername("client")
        viewModel.updatePassword("password123")

        viewModel.login()
        advanceUntilIdle()

        assertEquals(MobileRole.CLIENT, viewModel.state.session?.role)
        assertEquals("", viewModel.state.password)
        assertFalse(viewModel.state.loading)
    }

    @Test
    fun failedLoginShowsServerMessage() = runTest(dispatcher) {
        val gateway = FakeGateway(Result.failure(IllegalStateException("Neteisingi duomenys")))
        val viewModel = LoginViewModel(gateway)
        viewModel.updateUsername("client")
        viewModel.updatePassword("wrong-password")

        viewModel.login()
        advanceUntilIdle()

        assertEquals("Neteisingi duomenys", viewModel.state.error)
        assertNull(viewModel.state.session)
    }

    @Test
    fun blankCredentialsDoNotCallServer() = runTest(dispatcher) {
        val gateway = FakeGateway(Result.success(session(MobileRole.DRIVER)))
        val viewModel = LoginViewModel(gateway)

        viewModel.login()

        assertTrue(viewModel.state.error?.isNotBlank() == true)
        assertEquals(0, gateway.loginCalls)
    }

    private fun session(role: MobileRole) = AuthSession(
        token = "token",
        expiresAt = "2026-08-25T20:00:00Z",
        userId = 22L,
        username = "mobile-user",
        role = role,
    )

    private class FakeGateway(private val loginResult: Result<AuthSession>) : AuthGateway {
        var loginCalls = 0

        override suspend fun login(username: String, password: String): Result<AuthSession> {
            loginCalls++
            return loginResult
        }

        override suspend fun logout(token: String): Result<Unit> = Result.success(Unit)
    }
}
