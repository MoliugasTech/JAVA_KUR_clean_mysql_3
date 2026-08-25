package com.example.fooddeliverymobile.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddeliverymobile.data.AuthGateway
import com.example.fooddeliverymobile.data.AuthRepository
import com.example.fooddeliverymobile.data.AuthSession
import com.example.fooddeliverymobile.data.UnsupportedRoleException
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val session: AuthSession? = null,
)

class LoginViewModel(private val authGateway: AuthGateway = AuthRepository()) : ViewModel() {
    var state by mutableStateOf(LoginUiState())
        private set

    fun updateUsername(value: String) {
        state = state.copy(username = value, error = null)
    }

    fun updatePassword(value: String) {
        state = state.copy(password = value, error = null)
    }

    fun login() {
        if (state.username.isBlank() || state.password.isBlank()) {
            state = state.copy(error = "Įveskite vartotojo vardą ir slaptažodį")
            return
        }
        val username = state.username
        val password = state.password
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = authGateway.login(username, password)
            val session = result.getOrNull()
            if (session != null) {
                state = state.copy(loading = false, password = "", session = session)
            } else {
                val failure = result.exceptionOrNull()
                if (failure is UnsupportedRoleException) {
                    authGateway.logout(failure.issuedToken)
                }
                state = state.copy(
                    loading = false,
                    password = "",
                    error = failure?.message ?: "Prisijungti nepavyko",
                )
            }
        }
    }

    fun logout() {
        val token = state.session?.token ?: return
        viewModelScope.launch {
            authGateway.logout(token)
            state = LoginUiState(username = state.username)
        }
    }
}
