package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Admin;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.User;
import com.example.javakur.model.UserRole;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.security.AuthorizationService;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationServiceTest {
    private static final String PASSWORD = "password123";

    private final InMemoryUserRepository repository = new InMemoryUserRepository();
    private final AuthenticationService authenticationService = new AuthenticationService(repository);
    private final AuthorizationService authorizationService = new AuthorizationService();

    @BeforeEach
    void addUsers() {
        repository.add(user(new Admin("admin", hash(), "A", "A", "admin@example.com"), 1L));
        repository.add(user(new Client("client", hash(), "C", "C", "client@example.com", "Address"), 2L));
        repository.add(user(new Driver("driver", hash(), "D", "D", "driver@example.com", VehicleType.CAR), 3L));
        repository.add(user(new RestaurantOwner("owner", hash(), "O", "O", "owner@example.com"), 4L));
    }

    @Test
    void logsInEverySupportedRole() {
        assertEquals(UserRole.ADMIN, authenticationService.login("admin", PASSWORD).role());
        assertEquals(UserRole.CLIENT, authenticationService.login("client", PASSWORD).role());
        assertEquals(UserRole.DRIVER, authenticationService.login("driver", PASSWORD).role());
        assertEquals(UserRole.RESTAURANT_OWNER, authenticationService.login("owner", PASSWORD).role());
    }

    @Test
    void trimsUsernameAndReturnsSafeSessionData() {
        UserSession session = authenticationService.login("  client  ", PASSWORD);

        assertEquals(2L, session.userId());
        assertEquals("client", session.username());
        assertEquals(UserRole.CLIENT, session.role());
    }

    @Test
    void rejectsWrongOrMissingCredentialsWithSameMessage() {
        ValidationException wrongPassword = assertThrows(ValidationException.class,
                () -> authenticationService.login("client", "wrong-password"));
        ValidationException missingUser = assertThrows(ValidationException.class,
                () -> authenticationService.login("missing", PASSWORD));
        ValidationException blank = assertThrows(ValidationException.class,
                () -> authenticationService.login(" ", PASSWORD));

        assertEquals(wrongPassword.getMessage(), missingUser.getMessage());
        assertEquals(wrongPassword.getMessage(), blank.getMessage());
    }

    @Test
    void rejectsInactiveAccount() {
        User client = repository.findByUsername("client").orElseThrow();
        client.setActive(false);

        ValidationException error = assertThrows(ValidationException.class,
                () -> authenticationService.login("client", PASSWORD));

        assertEquals("Vartotojo paskyra užblokuota", error.getMessage());
    }

    @Test
    void allowsMatchingRole() {
        UserSession client = authenticationService.login("client", PASSWORD);

        authorizationService.requireRole(client, UserRole.CLIENT);
        assertTrue(authorizationService.hasRole(client, UserRole.CLIENT));
        assertFalse(authorizationService.hasRole(client, UserRole.ADMIN));
    }

    @Test
    void allowsOneOfSeveralRoles() {
        UserSession owner = authenticationService.login("owner", PASSWORD);

        authorizationService.requireRole(owner, UserRole.ADMIN, UserRole.RESTAURANT_OWNER);
    }

    @Test
    void rejectsMissingSessionOrWrongRole() {
        UserSession driver = authenticationService.login("driver", PASSWORD);

        assertThrows(ValidationException.class,
                () -> authorizationService.requireRole(driver, UserRole.ADMIN));
        assertThrows(ValidationException.class,
                () -> authorizationService.requireRole(null, UserRole.CLIENT));
    }

    private String hash() {
        return PasswordUtil.hashPassword(PASSWORD);
    }

    private <T extends User> T user(T user, Long id) {
        user.setId(id);
        return user;
    }

    private static class InMemoryUserRepository extends UserRepository {
        private final Map<String, User> users = new HashMap<>();

        void add(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }
    }
}
