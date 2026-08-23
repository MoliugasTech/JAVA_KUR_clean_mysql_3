package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.User;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.security.UserSession;
import com.example.javakur.util.PasswordUtil;

public class AuthenticationService {
    private static final String INVALID_CREDENTIALS = "Invalid username or password";

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserSession login(String username, String plainPassword) {
        if (username == null || username.isBlank()
                || plainPassword == null || plainPassword.isBlank()) {
            throw new ValidationException(INVALID_CREDENTIALS);
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ValidationException(INVALID_CREDENTIALS));
        if (!PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            throw new ValidationException(INVALID_CREDENTIALS);
        }

        return new UserSession(user.getId(), user.getUsername(), user.getRole());
    }
}
