package com.example.javakur;

import com.example.javakur.consoleCourseWork.MenuControl;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.User;
import com.example.javakur.model.UserRole;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.ClientConsoleService;
import com.example.javakur.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.List;
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

    @Test
    void createsOrderUsingVisibleListNumbers() {
        Client client = new Client(
                "client", PasswordUtil.hashPassword("password123"),
                "Test", "Client", "client@example.com", "Address");
        client.setId(1L);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(10L);
        restaurant.setName("Visible restaurant");
        restaurant.setType(RestaurantType.OTHER);
        MenuItem item = new MenuItem("Visible dish", "Test", new BigDecimal("7.50"), restaurant);
        item.setId(20L);

        String consoleInput = "1\nclient\npassword123\n2\n1\n1\n2\nt\n0\n0\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MenuControl menu = new MenuControl(
                new AuthenticationService(new SingleUserRepository(client)),
                new StubClientConsoleService(restaurant, item),
                new BufferedReader(new StringReader(consoleInput)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));

        menu.run();

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("1 - Visible restaurant"));
        assertTrue(output.contains("1 - Visible dish | 7.50 EUR"));
        assertTrue(output.contains("Numatoma suma: 15.00 EUR"));
        assertTrue(output.contains("Užsakymas sukurtas. ID=30, suma=15.00 EUR"));
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

    private static class StubClientConsoleService extends ClientConsoleService {
        private final Restaurant restaurant;
        private final MenuItem item;

        private StubClientConsoleService(Restaurant restaurant, MenuItem item) {
            super(null, null, null, null);
            this.restaurant = restaurant;
            this.item = item;
        }

        @Override
        public List<Restaurant> listRestaurants() {
            return List.of(restaurant);
        }

        @Override
        public List<MenuItem> listAvailableMenu(Long restaurantId) {
            return List.of(item);
        }

        @Override
        public FoodOrder createSingleItemOrder(Long clientId, Long restaurantId,
                                               Long menuItemId, int quantity) {
            FoodOrder order = new FoodOrder();
            order.setId(30L);
            order.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
            return order;
        }
    }
}
