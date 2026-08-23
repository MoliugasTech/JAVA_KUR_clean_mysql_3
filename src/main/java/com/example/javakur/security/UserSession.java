package com.example.javakur.security;

import com.example.javakur.model.UserRole;

public record UserSession(Long userId, String username, UserRole role) {
    public UserSession {
        if (userId == null || username == null || role == null) {
            throw new IllegalArgumentException("Complete authenticated user data is required");
        }
    }
}
