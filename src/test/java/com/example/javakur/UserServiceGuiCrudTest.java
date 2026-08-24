package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Admin;
import com.example.javakur.model.Client;
import com.example.javakur.model.User;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.UserService;
import com.example.javakur.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceGuiCrudTest {
    private final FakeUserRepository repository = new FakeUserRepository();
    private final UserService service = new UserService(repository);

    private Client client;

    @BeforeEach
    void setUp() {
        repository.values.clear();
        client = new Client("client", PasswordUtil.hashPassword("password123"),
                "Jonas", "Jonaitis", "jonas@example.com", "Vilniaus g. 1");
        client.setId(1L);
        repository.values.add(client);
    }

    @Test
    void adminCanBeCreatedFromUserForm() {
        Admin admin = service.createAdmin(
                "admin2", "password123", "Asta", "Adminė", "admin2@example.com");

        assertEquals(2L, admin.getId());
        assertTrue(PasswordUtil.verifyPassword("password123", admin.getPasswordHash()));
    }

    @Test
    void commonAndClientFieldsCanBeUpdated() {
        User updated = service.updateUser(client.getId(), "client-new", "",
                "Jonas", "Naujasis", "new@example.com", "Kauno g. 2", null);

        assertEquals("client-new", updated.getUsername());
        assertEquals("Naujasis", updated.getSurname());
        assertEquals("Kauno g. 2", ((Client) updated).getDeliveryAddress());
    }

    @Test
    void updateRejectsCredentialsUsedByAnotherUser() {
        Admin other = new Admin("taken", "hash", "A", "B", "taken@example.com");
        other.setId(2L);
        repository.values.add(other);

        assertThrows(ValidationException.class, () -> service.updateUser(
                client.getId(), "taken", "", "Jonas", "Jonaitis",
                "jonas@example.com", "Vilniaus g. 1", null));
    }

    private static class FakeUserRepository extends UserRepository {
        private final List<User> values = new ArrayList<>();

        @Override
        public User save(User user) {
            user.setId((long) values.size() + 1);
            values.add(user);
            return user;
        }

        @Override
        public User update(User user) {
            return user;
        }

        @Override
        public Optional<User> findById(Long id) {
            return values.stream().filter(user -> user.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return values.stream().filter(user -> user.getUsername().equals(username)).findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return values.stream().filter(user -> user.getEmail().equals(email)).findFirst();
        }
    }
}
