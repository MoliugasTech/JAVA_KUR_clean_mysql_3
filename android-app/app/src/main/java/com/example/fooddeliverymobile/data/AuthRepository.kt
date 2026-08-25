package com.example.fooddeliverymobile.data

import com.google.gson.Gson

interface AuthGateway {
    suspend fun login(username: String, password: String): Result<AuthSession>
    suspend fun logout(token: String): Result<Unit>
}

class AuthRepository(
    private val api: AuthApi = MobileApi.auth,
    private val gson: Gson = Gson(),
) : AuthGateway {
    override suspend fun login(username: String, password: String): Result<AuthSession> = runCatching {
        val response = api.login(LoginRequest(username.trim(), password))
        if (!response.isSuccessful) {
            throw ApiRequestException(errorMessage(response.errorBody()?.string()))
        }
        val body = response.body() ?: throw ApiRequestException("Serveris grąžino tuščią atsakymą")
        val role = MobileRole.fromApi(body.role) ?: throw UnsupportedRoleException(body.token)
        AuthSession(body.token, body.expiresAt, body.userId, body.username, role)
    }

    override suspend fun logout(token: String): Result<Unit> = runCatching {
        val response = api.logout("Bearer $token")
        if (!response.isSuccessful && response.code() != 401) {
            throw ApiRequestException(errorMessage(response.errorBody()?.string()))
        }
    }

    private fun errorMessage(json: String?): String {
        if (json.isNullOrBlank()) return "Nepavyko susisiekti su serveriu"
        return runCatching { gson.fromJson(json, ApiErrorResponse::class.java).message }
            .getOrNull().orEmpty().ifBlank { "Nepavyko įvykdyti užklausos" }
    }
}

class ApiRequestException(message: String) : RuntimeException(message)

class UnsupportedRoleException(val issuedToken: String) :
    RuntimeException("Ši mobilioji programėlė skirta tik klientams ir vairuotojams")
