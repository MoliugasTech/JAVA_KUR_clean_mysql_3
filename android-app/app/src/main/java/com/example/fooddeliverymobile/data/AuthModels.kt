package com.example.fooddeliverymobile.data

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val token: String,
    val tokenType: String,
    val expiresAt: String,
    val userId: Long,
    val username: String,
    val role: String,
)

data class ApiErrorResponse(val message: String? = null)

data class AuthSession(
    val token: String,
    val expiresAt: String,
    val userId: Long,
    val username: String,
    val role: MobileRole,
)

enum class MobileRole {
    CLIENT,
    DRIVER;

    companion object {
        fun fromApi(value: String): MobileRole? = entries.firstOrNull { it.name == value }
    }
}
