package com.example.javakur.security;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.UserRole;

import java.util.Arrays;

public class AuthorizationService {

    public boolean hasRole(UserSession session, UserRole role) {
        return session != null && role != null && session.role() == role;
    }

    public void requireRole(UserSession session, UserRole... allowedRoles) {
        if (session == null) {
            throw new ValidationException("Authentication is required");
        }
        if (allowedRoles == null || allowedRoles.length == 0) {
            throw new ValidationException("At least one allowed role is required");
        }
        boolean allowed = Arrays.stream(allowedRoles)
                .anyMatch(role -> role == session.role());
        if (!allowed) {
            throw new ValidationException("Access denied for role " + session.role());
        }
    }
}
