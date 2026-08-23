package com.example.javakur;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.User;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.GenericRepository;
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
import java.util.Map;

/** Manual MySQL smoke test for the M04 order lifecycle. */
public class MysqlOrderLifecycleSmoke {

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
        Driver driver = null;
        Restaurant restaurant = null;
        MenuItem menuItem = null;
        FoodOrder order = null;
        String suffix = String.valueOf(System.currentTimeMillis());

        try {
            owner = userService.createRestaurantOwner(
                    "life_owner_" + suffix, "password123", "Life", "Owner",
                    "life_owner_" + suffix + "@example.com");
            client = userService.createClient(
                    "life_client_" + suffix, "password123", "Life", "Client",
                    "life_client_" + suffix + "@example.com", "Testų g. 2, Vilnius");
            driver = userService.createDriver(
                    "life_driver_" + suffix, "password123", "Life", "Driver",
                    "life_driver_" + suffix + "@example.com", VehicleType.CAR);

            restaurant = restaurantService.createRestaurant(
                    "Lifecycle Restaurant " + suffix, "M04 smoke test", "Testų g. 1, Vilnius",
                    RestaurantType.OTHER, LocalTime.of(9, 0), LocalTime.of(22, 0), owner);
            menuItem = menuService.createMenuItem(
                    "Lifecycle dish", "M04 test item", new BigDecimal("12.50"), restaurant);

            order = orderService.createOrder(client, restaurant, Map.of(menuItem, 2), BigDecimal.ONE);
            orderService.changeStatus(order, OrderStatus.CONFIRMED);
            orderService.changeStatus(order, OrderStatus.PREPARING);
            orderService.changeStatus(order, OrderStatus.READY_FOR_PICKUP);
            orderService.assignDriver(order, driver);
            orderService.changeStatus(order, OrderStatus.DELIVERING);
            orderService.changeStatus(order, OrderStatus.COMPLETED);

            FoodOrder storedOrder = orderService.findDetailedById(order.getId())
                    .orElseThrow(() -> new IllegalStateException("Completed order was not found"));
            if (storedOrder.getStatus() != OrderStatus.COMPLETED
                    || storedOrder.getDriver() == null
                    || storedOrder.getCompletedAt() == null) {
                throw new IllegalStateException("Order lifecycle was not persisted correctly");
            }

            System.out.println("STATUS lifecycle OK, final=" + storedOrder.getStatus());
            System.out.println("DRIVER assignment OK, id=" + storedOrder.getDriver().getId());
            System.out.println("COMPLETED timestamp OK, value=" + storedOrder.getCompletedAt());
            System.out.println("=== MYSQL ORDER LIFECYCLE SMOKE TEST PASSED ===");
        } finally {
            delete(orderRepository, order);
            delete(menuItemRepository, menuItem);
            delete(restaurantRepository, restaurant);
            delete(userRepository, driver);
            delete(userRepository, client);
            delete(userRepository, owner);
            HibernateUtil.shutdown();
        }
    }

    private static void delete(GenericRepository<?, Long> repository, Object entity) {
        Long id = null;
        if (entity instanceof FoodOrder value) {
            id = value.getId();
        } else if (entity instanceof MenuItem value) {
            id = value.getId();
        } else if (entity instanceof Restaurant value) {
            id = value.getId();
        } else if (entity instanceof User value) {
            id = value.getId();
        }
        if (id != null) repository.deleteById(id);
    }
}
