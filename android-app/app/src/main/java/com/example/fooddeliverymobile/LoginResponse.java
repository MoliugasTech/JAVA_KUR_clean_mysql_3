package com.example.fooddeliverymobile;

public class LoginResponse {
    private String token;
    private String expiresAt;
    private long userId;
    private String username;
    private String role;

    public String getToken() {
        return token;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
