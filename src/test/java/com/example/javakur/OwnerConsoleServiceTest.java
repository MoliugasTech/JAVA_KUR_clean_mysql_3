package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderItem;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.User;
import com.example.javakur.repository.MenuItemRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.MenuService;
import com.example.javakur.service.OrderService;
import com.example.javakur.service.OwnerConsoleService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OwnerConsoleServiceTest {
    private final FakeUserRepository users = new FakeUserRepository();
    private final FakeRestaurantRepository restaurants = new FakeRestaurantRepository();
    private final FakeMenuRepository menu = new FakeMenuRepository();
    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final OwnerConsoleService service = new OwnerConsoleService(
            new UserService(users), new RestaurantService(restaurants),
            new MenuService(menu), new OrderService(orders), restaurants, orders);

    private RestaurantOwner owner;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        owner = new RestaurantOwner("owner", "hash", "O", "O", "o@example.com");
        owner.setId(1L);
        users.user = owner;
        restaurant = new Restaurant("Test", "Description", "Address", RestaurantType.OTHER,
                LocalTime.of(9, 0), LocalTime.of(22, 0), owner);
        restaurant.setId(2L);
        restaurants.restaurant = restaurant;
    }

    @Test
    void createsRestaurantAndMenuItemForOwner() {
        Restaurant created = service.createRestaurant(owner.getId(), "New", "D", "A",
                RestaurantType.OTHER, LocalTime.of(10, 0), LocalTime.of(20, 0));
        MenuItem item = service.addMenuItem(owner.getId(), created.getId(),
                "Pizza", "D", new BigDecimal("8.50"));

        assertEquals(owner.getId(), created.getOwner().getId());
        assertEquals(created.getId(), item.getRestaurant().getId());
    }

    @Test
    void updatesOnlyOwnedMenuItem() {
        MenuItem item = service.addMenuItem(owner.getId(), restaurant.getId(),
                "Pizza", "D", new BigDecimal("8.50"));

        MenuItem updated = service.updateMenuItem(owner.getId(), restaurant.getId(), item.getId(),
                new BigDecimal("9.50"), false);

        assertEquals(new BigDecimal("9.50"), updated.getPrice());
        assertFalse(updated.isAvailable());
    }

    @Test
    void ownerChangesOnlyRestaurantSideStatuses() {
        FoodOrder order = new FoodOrder();
        order.setId(4L);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.CREATED);
        orders.order = order;

        assertEquals(OrderStatus.CONFIRMED,
                service.changeOrderStatus(owner.getId(), order.getId(), OrderStatus.CONFIRMED).getStatus());
        assertThrows(ValidationException.class,
                () -> service.changeOrderStatus(owner.getId(), order.getId(), OrderStatus.DELIVERING));
    }

    @Test
    void ownerRejectsOrderWithReason() {
        FoodOrder order = new FoodOrder();
        order.setId(4L);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.CREATED);
        orders.order = order;

        FoodOrder rejected = service.cancelOrder(
                owner.getId(), order.getId(), "Nebėra reikalingų ingredientų");

        assertEquals(OrderStatus.CANCELLED, rejected.getStatus());
        assertEquals("Nebėra reikalingų ingredientų", rejected.getCancellationReason());
    }

    @Test
    void ownerRemovesItemAndRecalculatesPrice() {
        MenuItem first = new MenuItem("Pirma", "", new BigDecimal("5.00"), restaurant);
        first.setId(20L);
        MenuItem second = new MenuItem("Antra", "", new BigDecimal("4.00"), restaurant);
        second.setId(21L);
        OrderItem firstLine = new OrderItem(first, 1, new BigDecimal("5.00"));
        firstLine.setId(30L);
        OrderItem secondLine = new OrderItem(second, 2, new BigDecimal("4.00"));
        secondLine.setId(31L);
        FoodOrder order = new FoodOrder();
        order.setId(4L);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPriceMultiplier(new BigDecimal("1.200"));
        order.addItem(firstLine);
        order.addItem(secondLine);
        orders.order = order;

        FoodOrder updated = service.removeOrderItem(owner.getId(), order.getId(), 31L);

        assertEquals(1, updated.getItems().size());
        assertEquals(new BigDecimal("5.00"), updated.getSubtotal());
        assertEquals(new BigDecimal("6.00"), updated.getTotalPrice());
    }

    @Test
    void ownerCanBlockDriverBeforePickup() {
        FoodOrder order = new FoodOrder();
        order.setId(4L);
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PREPARING);
        orders.order = order;

        FoodOrder updated = service.setDriverAssignmentAllowed(
                owner.getId(), order.getId(), false);

        assertFalse(updated.isDriverAssignmentAllowed());
    }

    @Test
    void rejectsAnotherOwnersRestaurant() {
        assertThrows(ValidationException.class,
                () -> service.listMenu(99L, restaurant.getId()));
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
        public Restaurant save(Restaurant value) {
            value.setId(restaurant == null ? 2L : 3L);
            restaurant = value;
            return value;
        }

        @Override
        public List<Restaurant> findByOwner(RestaurantOwner owner) {
            return restaurant != null && restaurant.getOwner().getId().equals(owner.getId())
                    ? List.of(restaurant) : List.of();
        }

        @Override
        public Optional<Restaurant> findOwnedById(Long restaurantId, Long ownerId) {
            return restaurant != null && restaurant.getId().equals(restaurantId)
                    && restaurant.getOwner().getId().equals(ownerId)
                    ? Optional.of(restaurant) : Optional.empty();
        }
    }

    private static class FakeMenuRepository extends MenuItemRepository {
        private final List<MenuItem> items = new ArrayList<>();

        @Override
        public MenuItem save(MenuItem item) {
            item.setId((long) items.size() + 10);
            items.add(item);
            return item;
        }

        @Override
        public Optional<MenuItem> findById(Long id) {
            return items.stream().filter(item -> item.getId().equals(id)).findFirst();
        }

        @Override
        public List<MenuItem> findByRestaurant(Restaurant restaurant) {
            return items.stream()
                    .filter(item -> item.getRestaurant().getId().equals(restaurant.getId()))
                    .toList();
        }

        @Override
        public MenuItem update(MenuItem item) {
            return item;
        }
    }

    private static class FakeOrderRepository extends OrderRepository {
        private FoodOrder order;

        @Override
        public Optional<FoodOrder> findOwnedByRestaurantOwner(Long orderId, Long ownerId) {
            return order != null && order.getId().equals(orderId)
                    && order.getRestaurant().getOwner().getId().equals(ownerId)
                    ? Optional.of(order) : Optional.empty();
        }

        @Override
        public List<FoodOrder> findByRestaurantId(Long restaurantId) {
            return order != null && order.getRestaurant().getId().equals(restaurantId)
                    ? List.of(order) : List.of();
        }

        @Override
        public FoodOrder update(FoodOrder order) {
            return order;
        }
    }
}
