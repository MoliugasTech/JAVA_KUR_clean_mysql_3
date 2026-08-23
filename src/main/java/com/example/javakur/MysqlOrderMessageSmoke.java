package com.example.javakur;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.User;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.GenericRepository;
import com.example.javakur.repository.MenuItemRepository;
import com.example.javakur.repository.OrderMessageRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.MenuService;
import com.example.javakur.service.OrderMessageService;
import com.example.javakur.service.OrderService;
import com.example.javakur.service.RestaurantService;
import com.example.javakur.service.UserService;
import com.example.javakur.util.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Manual MySQL smoke test for M06 order messages. */
public class MysqlOrderMessageSmoke {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();
        MenuItemRepository menuItemRepository = new MenuItemRepository();
        OrderRepository orderRepository = new OrderRepository();
        OrderMessageRepository messageRepository = new OrderMessageRepository();

        UserService userService = new UserService(userRepository);
        RestaurantService restaurantService = new RestaurantService(restaurantRepository);
        MenuService menuService = new MenuService(menuItemRepository);
        OrderService orderService = new OrderService(orderRepository);
        OrderMessageService messageService = new OrderMessageService(messageRepository);

        RestaurantOwner owner = null;
        Client client = null;
        Driver driver = null;
        Restaurant restaurant = null;
        MenuItem menuItem = null;
        FoodOrder order = null;
        List<OrderMessage> messages = new ArrayList<>();
        String suffix = String.valueOf(System.currentTimeMillis());

        try {
            owner = userService.createRestaurantOwner(
                    "msg_owner_" + suffix, "password123", "Message", "Owner",
                    "msg_owner_" + suffix + "@example.com");
            client = userService.createClient(
                    "msg_client_" + suffix, "password123", "Message", "Client",
                    "msg_client_" + suffix + "@example.com", "Testų g. 2, Vilnius");
            driver = userService.createDriver(
                    "msg_driver_" + suffix, "password123", "Message", "Driver",
                    "msg_driver_" + suffix + "@example.com", VehicleType.CAR);
            restaurant = restaurantService.createRestaurant(
                    "Message Restaurant " + suffix, "M06 smoke test", "Testų g. 1, Vilnius",
                    RestaurantType.OTHER, LocalTime.of(9, 0), LocalTime.of(22, 0), owner);
            menuItem = menuService.createMenuItem(
                    "Message dish", "M06 test item", new BigDecimal("8.00"), restaurant);

            order = orderService.createOrder(client, restaurant, Map.of(menuItem, 1), BigDecimal.ONE);
            orderService.changeStatus(order, OrderStatus.CONFIRMED);
            orderService.changeStatus(order, OrderStatus.PREPARING);
            orderService.changeStatus(order, OrderStatus.READY_FOR_PICKUP);
            order = orderService.assignDriver(order, driver);

            messages.add(messageService.sendMessage(order, client, "Ar jau paruošta?"));
            messages.add(messageService.sendMessage(order, owner, "Taip, galite paimti"));
            messages.add(messageService.sendMessage(order, driver, "Užsakymą paėmiau"));

            List<OrderMessage> stored = messageService.getConversation(order, client);
            if (stored.size() != 3) {
                throw new IllegalStateException("Expected 3 messages, found " + stored.size());
            }

            System.out.println("CLIENT message OK");
            System.out.println("OWNER message OK");
            System.out.println("DRIVER message OK");
            System.out.println("=== MYSQL ORDER MESSAGE SMOKE TEST PASSED ===");
        } finally {
            for (OrderMessage message : messages) delete(messageRepository, message);
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
        if (entity instanceof OrderMessage value) id = value.getId();
        else if (entity instanceof FoodOrder value) id = value.getId();
        else if (entity instanceof MenuItem value) id = value.getId();
        else if (entity instanceof Restaurant value) id = value.getId();
        else if (entity instanceof User value) id = value.getId();
        if (id != null) repository.deleteById(id);
    }
}
