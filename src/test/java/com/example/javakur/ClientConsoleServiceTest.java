package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.User;
import com.example.javakur.repository.MenuItemRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.ClientConsoleService;
import com.example.javakur.service.MenuService;
import com.example.javakur.service.OrderService;
import com.example.javakur.service.OrderCart;
import com.example.javakur.service.RestaurantService;
import com.example.javakur.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientConsoleServiceTest {
    private final FakeUserRepository users = new FakeUserRepository();
    private final FakeRestaurantRepository restaurants = new FakeRestaurantRepository();
    private final FakeMenuRepository menu = new FakeMenuRepository();
    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final ClientConsoleService service = new ClientConsoleService(
            new UserService(users), new RestaurantService(restaurants),
            new MenuService(menu), new OrderService(orders));

    private Client client;
    private Restaurant restaurant;
    private MenuItem item;

    @BeforeEach
    void setUp() {
        client = new Client("client", "hash", "C", "C", "c@example.com", "Address");
        client.setId(1L);
        users.user = client;

        restaurant = new Restaurant();
        restaurant.setId(2L);
        restaurant.setName("Test restaurant");
        restaurant.setType(RestaurantType.OTHER);
        restaurant.setOpeningTime(LocalTime.of(9, 0));
        restaurant.setClosingTime(LocalTime.of(22, 0));
        restaurants.restaurant = restaurant;

        item = new MenuItem("Pizza", "Test", new BigDecimal("9.50"), restaurant);
        item.setId(3L);
        menu.item = item;
    }

    @Test
    void listsRestaurantsAndAvailableMenu() {
        assertEquals(List.of(restaurant), service.listRestaurants());
        assertEquals(List.of(item), service.listAvailableMenu(restaurant.getId()));
    }

    @Test
    void createsOrderAndListsItForClient() {
        FoodOrder order = service.createSingleItemOrder(
                client.getId(), restaurant.getId(), item.getId(), 2);

        assertEquals(new BigDecimal("19.00"), order.getTotalPrice());
        assertEquals(List.of(order), service.listClientOrders(client.getId()));
    }

    @Test
    void checksOutCartWithMultipleItems() {
        MenuItem drink = new MenuItem("Drink", "Test", new BigDecimal("2.25"), restaurant);
        drink.setId(4L);
        OrderCart cart = new OrderCart(restaurant);
        cart.setQuantity(item, 2);
        cart.setQuantity(drink, 3);

        FoodOrder order = service.checkout(client.getId(), cart);

        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("25.75"), order.getSubtotal());
        assertEquals(new BigDecimal("25.75"), order.getTotalPrice());
        assertEquals(List.of(order), service.listClientOrders(client.getId()));
    }

    @Test
    void rejectsEmptyCartAtCheckout() {
        assertThrows(ValidationException.class,
                () -> service.checkout(client.getId(), new OrderCart(restaurant)));
    }

    @Test
    void rejectsUnknownClientOrRestaurant() {
        assertThrows(ValidationException.class,
                () -> service.createSingleItemOrder(99L, restaurant.getId(), item.getId(), 1));
        assertThrows(ValidationException.class,
                () -> service.listAvailableMenu(99L));
    }

    private static class FakeUserRepository extends UserRepository {
        private User user;

        @Override
        public Optional<User> findById(Long id) {
            return user != null && user.getId().equals(id) ? Optional.of(user) : Optional.empty();
        }
    }

    private static class FakeRestaurantRepository extends RestaurantRepository {
        private Restaurant restaurant;

        @Override
        public List<Restaurant> findAll() {
            return restaurant == null ? List.of() : List.of(restaurant);
        }

        @Override
        public Optional<Restaurant> findById(Long id) {
            return restaurant != null && restaurant.getId().equals(id)
                    ? Optional.of(restaurant) : Optional.empty();
        }
    }

    private static class FakeMenuRepository extends MenuItemRepository {
        private MenuItem item;

        @Override
        public Optional<MenuItem> findById(Long id) {
            return item != null && item.getId().equals(id) ? Optional.of(item) : Optional.empty();
        }

        @Override
        public List<MenuItem> findAvailableByRestaurant(Restaurant restaurant) {
            return item != null && item.getRestaurant().getId().equals(restaurant.getId())
                    ? List.of(item) : List.of();
        }
    }

    private static class FakeOrderRepository extends OrderRepository {
        private final List<FoodOrder> orders = new ArrayList<>();

        @Override
        public FoodOrder save(FoodOrder order) {
            order.setId((long) orders.size() + 1);
            orders.add(order);
            return order;
        }

        @Override
        public List<FoodOrder> findByClientId(Long clientId) {
            return orders.stream()
                    .filter(order -> order.getClient().getId().equals(clientId))
                    .toList();
        }
    }
}
