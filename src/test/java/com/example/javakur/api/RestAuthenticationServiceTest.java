package com.example.javakur.api;

import com.example.javakur.model.Client;
import com.example.javakur.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationServiceTest {
    private static final String PASSWORD = "password123";

    @Mock
    private MobileUserRepository repository;

    private final ApiTokenService tokenService = new ApiTokenService();

    @InjectMocks
    private RestAuthenticationService service;

    @Test
    void logsInWithHashedPasswordAndIssuesToken() {
        service = new RestAuthenticationService(repository, tokenService);
        Client client = client(true);
        when(repository.findByUsername("client")).thenReturn(Optional.of(client));

        ApiTokenService.IssuedToken token = service.login(" client ", PASSWORD);

        assertEquals(5L, token.principal().userId());
        assertEquals("client", token.principal().username());
        assertTrue(tokenService.authenticate(token.value()).isPresent());
    }

    @Test
    void rejectsWrongPasswordWithoutRevealingUserDetails() {
        service = new RestAuthenticationService(repository, tokenService);
        when(repository.findByUsername("client")).thenReturn(Optional.of(client(true)));

        ApiAuthenticationException exception = assertThrows(ApiAuthenticationException.class,
                () -> service.login("client", "wrong-password"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Neteisingas vartotojo vardas arba slaptažodis", exception.getMessage());
    }

    @Test
    void rejectsInactiveAccount() {
        service = new RestAuthenticationService(repository, tokenService);
        when(repository.findByUsername("client")).thenReturn(Optional.of(client(false)));

        ApiAuthenticationException exception = assertThrows(ApiAuthenticationException.class,
                () -> service.login("client", PASSWORD));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Vartotojo paskyra užblokuota", exception.getMessage());
    }

    @Test
    void logoutRevokesToken() {
        service = new RestAuthenticationService(repository, tokenService);
        when(repository.findByUsername("client")).thenReturn(Optional.of(client(true)));
        ApiTokenService.IssuedToken token = service.login("client", PASSWORD);

        service.logout(token.value());

        assertFalse(tokenService.authenticate(token.value()).isPresent());
    }

    private Client client(boolean active) {
        Client client = new Client("client", PasswordUtil.hashPassword(PASSWORD),
                "C", "Client", "client@example.com", "Vilnius");
        client.setId(5L);
        client.setActive(active);
        return client;
    }
}
