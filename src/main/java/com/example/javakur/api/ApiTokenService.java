package com.example.javakur.api;

import com.example.javakur.model.User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiTokenService {
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(8);

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, StoredToken> tokens = new ConcurrentHashMap<>();

    public IssuedToken issue(User user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        Instant expiresAt = Instant.now().plus(TOKEN_LIFETIME);
        ApiPrincipal principal = new ApiPrincipal(user.getId(), user.getUsername(), user.getRole());
        tokens.put(value, new StoredToken(principal, expiresAt));
        return new IssuedToken(value, expiresAt, principal);
    }

    public Optional<ApiPrincipal> authenticate(String value) {
        StoredToken token = tokens.get(value);
        if (token == null) {
            return Optional.empty();
        }
        if (token.expiresAt().isBefore(Instant.now())) {
            tokens.remove(value);
            return Optional.empty();
        }
        return Optional.of(token.principal());
    }

    public void revoke(String value) {
        if (value != null) {
            tokens.remove(value);
        }
    }

    public record IssuedToken(String value, Instant expiresAt, ApiPrincipal principal) {
    }

    private record StoredToken(ApiPrincipal principal, Instant expiresAt) {
    }
}
