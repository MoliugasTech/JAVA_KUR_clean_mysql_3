package com.example.javakur.api;

import com.example.javakur.model.UserRole;

public record ApiPrincipal(Long userId, String username, UserRole role) {
}
