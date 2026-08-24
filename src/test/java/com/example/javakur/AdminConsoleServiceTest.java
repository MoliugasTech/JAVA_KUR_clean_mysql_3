package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Admin;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.Review;
import com.example.javakur.model.User;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.AdminConsoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminConsoleServiceTest {
    private final FakeUserRepository users = new FakeUserRepository();
    private final FakeRestaurantRepository restaurants = new FakeRestaurantRepository();
    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final FakeReviewRepository reviews = new FakeReviewRepository();
    private final AdminConsoleService service = new AdminConsoleService(
            users, restaurants, orders, reviews);

    private Admin admin;
    private Client client;

    @BeforeEach
    void setUp() {
        admin = new Admin("admin", "hash", "A", "A", "admin@example.com");
        admin.setId(1L);
        client = new Client("client", "hash", "C", "C", "client@example.com", "Address");
        client.setId(2L);
        users.values = List.of(admin, client);
    }

    @Test
    void adminCanDeactivateAnotherUser() {
        User updated = service.setUserActive(admin.getId(), client.getId(), false);

        assertSame(client, updated);
        assertFalse(client.isActive());
    }

    @Test
    void adminCannotDeactivateSelf() {
        assertThrows(ValidationException.class,
                () -> service.setUserActive(admin.getId(), admin.getId(), false));
    }

    @Test
    void nonAdminCannotUseAdministrationFunctions() {
        assertThrows(ValidationException.class, () -> service.listUsers(client.getId()));
        assertThrows(ValidationException.class, () -> service.listOrders(client.getId()));
    }

    @Test
    void adminCanReadAllSystemViews() {
        service.listUsers(admin.getId());
        service.listRestaurants(admin.getId());
        service.listOrders(admin.getId());
        service.listReviews(admin.getId());
    }

    private static class FakeUserRepository extends UserRepository {
        private List<User> values = List.of();

        @Override public Optional<User> findById(Long id) {
            return values.stream().filter(value -> value.getId().equals(id)).findFirst();
        }
        @Override public List<User> findAllOrdered() { return values; }
        @Override public User update(User user) { return user; }
    }

    private static class FakeRestaurantRepository extends RestaurantRepository {
        @Override public List<Restaurant> findAllWithOwner() { return List.of(); }
    }

    private static class FakeOrderRepository extends OrderRepository {
        @Override public List<FoodOrder> findAllWithParticipants() { return List.of(); }
    }

    private static class FakeReviewRepository extends ReviewRepository {
        @Override public List<Review> findAllDetailed() { return List.of(); }
    }
}
