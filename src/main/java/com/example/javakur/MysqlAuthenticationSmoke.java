package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.UserRole;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.security.AuthorizationService;
import com.example.javakur.security.UserSession;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.UserService;
import com.example.javakur.util.HibernateUtil;

/** Manual MySQL smoke test for M07 authentication. */
public class MysqlAuthenticationSmoke {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        AuthenticationService authenticationService = new AuthenticationService(userRepository);
        AuthorizationService authorizationService = new AuthorizationService();
        Client client = null;
        String suffix = String.valueOf(System.currentTimeMillis());
        String username = "auth_client_" + suffix;
        String password = "password123";

        try {
            client = userService.createClient(
                    username, password, "Auth", "Client",
                    username + "@example.com", "Testų g. 1, Vilnius");

            UserSession session = authenticationService.login(username, password);
            authorizationService.requireRole(session, UserRole.CLIENT);
            if (!client.getId().equals(session.userId())) {
                throw new IllegalStateException("Authenticated user ID does not match");
            }

            boolean wrongPasswordRejected = false;
            try {
                authenticationService.login(username, "incorrect-password");
            } catch (ValidationException expected) {
                wrongPasswordRejected = true;
            }
            if (!wrongPasswordRejected) {
                throw new IllegalStateException("Wrong password was accepted");
            }

            System.out.println("BCrypt login OK, user=" + session.username());
            System.out.println("ROLE authorization OK, role=" + session.role());
            System.out.println("WRONG password rejected OK");
            System.out.println("=== MYSQL AUTHENTICATION SMOKE TEST PASSED ===");
        } finally {
            if (client != null && client.getId() != null) {
                userRepository.deleteById(client.getId());
            }
            HibernateUtil.shutdown();
        }
    }
}
