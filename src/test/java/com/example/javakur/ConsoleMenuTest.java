package com.example.javakur;

import com.example.javakur.consoleCourseWork.MenuControl;
import com.example.javakur.model.Client;
import com.example.javakur.model.User;
import com.example.javakur.model.UserRole;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleMenuTest {

    @Test
    void showsClientMenuAfterSuccessfulLogin() {
        Client client = new Client(
                "client", PasswordUtil.hashPassword("password123"),
                "Test", "Client", "client@example.com", "Address");
        client.setId(1L);

        String consoleInput = "1\nclient\npassword123\n0\n0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MenuControl menu = new MenuControl(
                new AuthenticationService(new SingleUserRepository(client)),
                new BufferedReader(new StringReader(consoleInput)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));

        menu.run();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Prisijungta: client [CLIENT]"));
        assertTrue(output.contains("KLIENTO MENIU"));
        assertTrue(output.contains("Sukurti užsakymą"));
        assertTrue(output.contains("Atsijungta."));
        assertTrue(output.contains("Programa baigta."));
    }

    @Test
    void staysInStartMenuAfterFailedLogin() {
        Client client = new Client(
                "client", PasswordUtil.hashPassword("password123"),
                "Test", "Client", "client@example.com", "Address");
        client.setId(1L);

        String consoleInput = "1\nclient\nwrong-password\n0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MenuControl menu = new MenuControl(
                new AuthenticationService(new SingleUserRepository(client)),
                new BufferedReader(new StringReader(consoleInput)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));

        menu.run();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Prisijungti nepavyko: Invalid username or password"));
        assertTrue(output.contains("Programa baigta."));
    }

    @Test
    void definesMenuForEveryRole() {
        for (UserRole role : UserRole.values()) {
            assertEquals(4, MenuControl.menuOptionsFor(role).size());
        }
    }

    private static class SingleUserRepository extends UserRepository {
        private final User user;

        private SingleUserRepository(User user) {
            this.user = user;
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return user.getUsername().equals(username) ? Optional.of(user) : Optional.empty();
        }
    }
}
