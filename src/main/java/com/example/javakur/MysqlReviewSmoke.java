package com.example.javakur;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.Review;
import com.example.javakur.model.User;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.GenericRepository;
import com.example.javakur.repository.MenuItemRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.MenuService;
import com.example.javakur.service.OrderService;
import com.example.javakur.service.RestaurantService;
import com.example.javakur.service.ReviewService;
import com.example.javakur.service.UserService;
import com.example.javakur.util.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Manual MySQL smoke test for M05 reviews. */
public class MysqlReviewSmoke {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();
        MenuItemRepository menuItemRepository = new MenuItemRepository();
        OrderRepository orderRepository = new OrderRepository();
        ReviewRepository reviewRepository = new ReviewRepository();

        UserService userService = new UserService(userRepository);
        RestaurantService restaurantService = new RestaurantService(restaurantRepository);
        MenuService menuService = new MenuService(menuItemRepository);
        OrderService orderService = new OrderService(orderRepository);
        ReviewService reviewService = new ReviewService(reviewRepository);

        RestaurantOwner owner = null;
        Client client = null;
        Driver driver = null;
        Restaurant restaurant = null;
        MenuItem menuItem = null;
        FoodOrder order = null;
        List<Review> reviews = new ArrayList<>();
        String suffix = String.valueOf(System.currentTimeMillis());

        try {
            owner = userService.createRestaurantOwner(
                    "review_owner_" + suffix, "password123", "Review", "Owner",
                    "review_owner_" + suffix + "@example.com");
            client = userService.createClient(
                    "review_client_" + suffix, "password123", "Review", "Client",
                    "review_client_" + suffix + "@example.com", "Testų g. 2, Vilnius");
            driver = userService.createDriver(
                    "review_driver_" + suffix, "password123", "Review", "Driver",
                    "review_driver_" + suffix + "@example.com", VehicleType.CAR);
            restaurant = restaurantService.createRestaurant(
                    "Review Restaurant " + suffix, "M05 smoke test", "Testų g. 1, Vilnius",
                    RestaurantType.OTHER, LocalTime.of(9, 0), LocalTime.of(22, 0), owner);
            menuItem = menuService.createMenuItem(
                    "Review dish", "M05 test item", new BigDecimal("10.00"), restaurant);

            order = orderService.createOrder(client, restaurant, Map.of(menuItem, 1), BigDecimal.ONE);
            orderService.changeStatus(order, OrderStatus.CONFIRMED);
            orderService.changeStatus(order, OrderStatus.PREPARING);
            orderService.changeStatus(order, OrderStatus.READY_FOR_PICKUP);
            orderService.assignDriver(order, driver);
            orderService.changeStatus(order, OrderStatus.DELIVERING);
            order = orderService.changeStatus(order, OrderStatus.COMPLETED);

            reviews.add(reviewService.reviewRestaurant(order, client, (byte) 5, "Puikus maistas"));
            reviews.add(reviewService.reviewDriver(order, client, (byte) 5, "Greitas pristatymas"));
            reviews.add(reviewService.reviewClient(order, driver, (byte) 5, "Sklandus perdavimas"));

            List<Review> stored = reviewService.findByOrderId(order.getId());
            if (stored.size() != 3) {
                throw new IllegalStateException("Expected 3 reviews, found " + stored.size());
            }

            System.out.println("RESTAURANT review OK");
            System.out.println("DRIVER review OK");
            System.out.println("CLIENT review OK");
            System.out.println("=== MYSQL REVIEW SMOKE TEST PASSED ===");
        } finally {
            for (Review review : reviews) delete(reviewRepository, review);
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
        if (entity instanceof Review value) id = value.getId();
        else if (entity instanceof FoodOrder value) id = value.getId();
        else if (entity instanceof MenuItem value) id = value.getId();
        else if (entity instanceof Restaurant value) id = value.getId();
        else if (entity instanceof User value) id = value.getId();
        if (id != null) repository.deleteById(id);
    }
}
