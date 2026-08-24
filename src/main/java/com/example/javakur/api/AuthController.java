package com.example.javakur.api;

import com.example.javakur.model.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RestAuthenticationService authenticationService;

    public AuthController(RestAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        ApiTokenService.IssuedToken token =
                authenticationService.login(request.username(), request.password());
        ApiPrincipal principal = token.principal();
        return new LoginResponse(token.value(), "Bearer", token.expiresAt(),
                principal.userId(), principal.username(), principal.role());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authenticationService.logout((String) authentication.getCredentials());
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(
            @NotBlank(message = "Vartotojo vardas yra privalomas") String username,
            @NotBlank(message = "Slaptažodis yra privalomas") String password) {
    }

    public record LoginResponse(String token, String tokenType, Instant expiresAt,
                                Long userId, String username, UserRole role) {
    }
}
