package com.example.javakur;

import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.repository.MenuItemRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.MenuService;
import com.example.javakur.service.OrderService;
import com.example.javakur.service.RestaurantService;
import com.example.javakur.service.UserService;
import com.example.javakur.util.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Manual MySQL smoke test for M03 order creation. */
public class MysqlOrderSmoke {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();
        MenuItemRepository menuItemRepository = new MenuItemRepository();
        OrderRepository orderRepository = new OrderRepository();

        UserService userService = new UserService(userRepository);
        RestaurantService restaurantService = new RestaurantService(restaurantRepository);
        MenuService menuService = new MenuService(menuItemRepository);
        OrderService orderService = new OrderService(orderRepository);

        RestaurantOwner owner = null;
        Client client = null;
        Restaurant restaurant = null;
        MenuItem firstItem = null;
        MenuItem secondItem = null;
        FoodOrder order = null;
        String suffix = String.valueOf(System.currentTimeMillis());

        try {
            owner = userService.createRestaurantOwner(
                    "order_owner_" + suffix, "password123", "Order", "Owner",
                    "order_owner_" + suffix + "@example.com");

            client = userService.createClient(
                    "order_client_" + suffix, "password123", "Order", "Client",
                    "order_client_" + suffix + "@example.com", "Testų g. 2, Vilnius");

            restaurant = restaurantService.createRestaurant(
                    "Order Restaurant " + suffix, "M03 smoke test", "Testų g. 1, Vilnius",
                    RestaurantType.OTHER, LocalTime.of(9, 0), LocalTime.of(22, 0), owner);

            firstItem = menuService.createMenuItem(
                    "Soup", "M03 test item", new BigDecimal("7.50"), restaurant);
            secondItem = menuService.createMenuItem(
                    "Main dish", "M03 test item", new BigDecimal("10.49"), restaurant);

            Map<MenuItem, Integer> items = new LinkedHashMap<>();
            items.put(firstItem, 2);
            items.put(secondItem, 1);

            order = orderService.createOrder(client, restaurant, items, new BigDecimal("1.100"));
            FoodOrder storedOrder = orderService.findDetailedById(order.getId())
                    .orElseThrow(() -> new IllegalStateException("Created order was not found"));

            if (storedOrder.getItems().size() != 2
                    || storedOrder.getSubtotal().compareTo(new BigDecimal("25.49")) != 0
                    || storedOrder.getTotalPrice().compareTo(new BigDecimal("28.04")) != 0) {
                throw new IllegalStateException("Stored order totals or items are incorrect");
            }

            System.out.println("CREATE order OK, id=" + storedOrder.getId());
            System.out.println("READ order items OK, count=" + storedOrder.getItems().size());
            System.out.println("CALCULATE subtotal OK, value=" + storedOrder.getSubtotal());
            System.out.println("CALCULATE total price OK, value=" + storedOrder.getTotalPrice());
            System.out.println("=== MYSQL ORDER SMOKE TEST PASSED ===");
        } finally {
            deleteIfPresent(orderRepository, order);
            deleteIfPresent(menuItemRepository, secondItem);
            deleteIfPresent(menuItemRepository, firstItem);
            deleteIfPresent(restaurantRepository, restaurant);
            deleteIfPresent(userRepository, client);
            deleteIfPresent(userRepository, owner);
            HibernateUtil.shutdown();
        }
    }

    private static <T> void deleteIfPresent(
            com.example.javakur.repository.GenericRepository<T, Long> repository, T entity) {
        if (entity == null) return;

        Long id;
        if (entity instanceof FoodOrder value) id = value.getId();
        else if (entity instanceof MenuItem value) id = value.getId();
        else if (entity instanceof Restaurant value) id = value.getId();
        else if (entity instanceof com.example.javakur.model.User value) id = value.getId();
        else return;

        if (id != null) repository.deleteById(id);
    }
}
