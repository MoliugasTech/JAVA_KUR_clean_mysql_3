package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.service.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {

    @Test
    void createsOrderAndCalculatesPrices() {
        TestData data = createTestData();
        OrderService service = new OrderService(new InMemoryOrderRepository());

        Map<MenuItem, Integer> items = new LinkedHashMap<>();
        items.put(data.firstItem(), 2);
        items.put(data.secondItem(), 1);

        FoodOrder order = service.createOrder(
                data.client(), data.restaurant(), items, new BigDecimal("1.100"));

        assertEquals(new BigDecimal("25.49"), order.getSubtotal());
        assertEquals(new BigDecimal("1.100"), order.getPriceMultiplier());
        assertEquals(new BigDecimal("28.04"), order.getTotalPrice());
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("7.50"), order.getItems().get(0).getPriceAtPurchase());
    }

    @Test
    void rejectsEmptyOrder() {
        TestData data = createTestData();
        OrderService service = new OrderService(new InMemoryOrderRepository());

        assertThrows(ValidationException.class, () -> service.createOrder(
                data.client(), data.restaurant(), Map.of(), BigDecimal.ONE));
    }

    @Test
    void rejectsMenuItemFromAnotherRestaurant() {
        TestData data = createTestData();
        OrderService service = new OrderService(new InMemoryOrderRepository());

        Restaurant anotherRestaurant = createRestaurant(99L, data.owner(), "Other Restaurant");
        MenuItem foreignItem = new MenuItem(
                "Foreign dish", "", new BigDecimal("5.00"), anotherRestaurant);
        foreignItem.setId(100L);

        assertThrows(ValidationException.class, () -> service.createOrder(
                data.client(), data.restaurant(), Map.of(foreignItem, 1), BigDecimal.ONE));
    }

    private TestData createTestData() {
        RestaurantOwner owner = new RestaurantOwner(
                "owner", "hash", "Test", "Owner", "owner@example.com");
        owner.setId(1L);

        Restaurant restaurant = createRestaurant(2L, owner, "Test Restaurant");

        Client client = new Client(
                "client", "hash", "Test", "Client", "client@example.com", "Testų g. 1");
        client.setId(3L);

        MenuItem firstItem = new MenuItem(
                "First dish", "", new BigDecimal("7.50"), restaurant);
        firstItem.setId(4L);

        MenuItem secondItem = new MenuItem(
                "Second dish", "", new BigDecimal("10.49"), restaurant);
        secondItem.setId(5L);

        return new TestData(owner, restaurant, client, firstItem, secondItem);
    }

    private Restaurant createRestaurant(Long id, RestaurantOwner owner, String name) {
        Restaurant restaurant = new Restaurant(
                name, "", "Testų g. 1", RestaurantType.OTHER,
                LocalTime.of(9, 0), LocalTime.of(22, 0), owner);
        restaurant.setId(id);
        return restaurant;
    }

    private record TestData(RestaurantOwner owner, Restaurant restaurant, Client client,
                            MenuItem firstItem, MenuItem secondItem) {
    }

    private static class InMemoryOrderRepository extends OrderRepository {
        @Override
        public FoodOrder save(FoodOrder order) {
            order.setId(10L);
            return order;
        }
    }
}
