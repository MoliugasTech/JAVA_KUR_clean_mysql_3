package com.example.javakur.api;

import com.example.javakur.model.User;
import com.example.javakur.util.PasswordUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RestAuthenticationService {
    private static final String INVALID_CREDENTIALS =
            "Neteisingas vartotojo vardas arba slaptažodis";

    private final MobileUserRepository repository;
    private final ApiTokenService tokenService;

    public RestAuthenticationService(MobileUserRepository repository, ApiTokenService tokenService) {
        this.repository = repository;
        this.tokenService = tokenService;
    }

    public ApiTokenService.IssuedToken login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new ApiAuthenticationException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }
        User user = repository.findByUsername(username.trim())
                .orElseThrow(() ->
                        new ApiAuthenticationException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS));
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new ApiAuthenticationException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }
        if (!user.isActive()) {
            throw new ApiAuthenticationException(HttpStatus.FORBIDDEN,
                    "Vartotojo paskyra užblokuota");
        }
        return tokenService.issue(user);
    }

    public void logout(String token) {
        tokenService.revoke(token);
    }
}
