package com.example.javakur.consoleCourseWork;

import com.example.javakur.repository.UserRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.MenuItemRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.OrderMessageRepository;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.ClientConsoleService;
import com.example.javakur.service.MenuService;
import com.example.javakur.service.OrderService;
import com.example.javakur.service.OrderMessageService;
import com.example.javakur.service.OrderInteractionService;
import com.example.javakur.service.ReviewService;
import com.example.javakur.service.OwnerConsoleService;
import com.example.javakur.service.DriverConsoleService;
import com.example.javakur.service.RestaurantService;
import com.example.javakur.service.UserService;
import com.example.javakur.service.AdminConsoleService;
import com.example.javakur.util.HibernateUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StartNoGui {
    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();
        MenuItemRepository menuItemRepository = new MenuItemRepository();
        OrderRepository orderRepository = new OrderRepository();
        OrderMessageRepository orderMessageRepository = new OrderMessageRepository();
        ReviewRepository reviewRepository = new ReviewRepository();

        AuthenticationService authenticationService = new AuthenticationService(userRepository);
        ClientConsoleService clientConsoleService = new ClientConsoleService(
                new UserService(userRepository),
                new RestaurantService(restaurantRepository),
                new MenuService(menuItemRepository),
                new OrderService(orderRepository));
        OwnerConsoleService ownerConsoleService = new OwnerConsoleService(
                new UserService(userRepository),
                new RestaurantService(restaurantRepository),
                new MenuService(menuItemRepository),
                new OrderService(orderRepository),
                restaurantRepository,
                orderRepository);
        DriverConsoleService driverConsoleService = new DriverConsoleService(
                new UserService(userRepository),
                new OrderService(orderRepository),
                orderRepository);
        OrderInteractionService interactionService = new OrderInteractionService(
                new UserService(userRepository), orderRepository,
                new OrderMessageService(orderMessageRepository),
                new ReviewService(reviewRepository));
        AdminConsoleService adminConsoleService = new AdminConsoleService(
                userRepository, restaurantRepository, orderRepository, reviewRepository);
        MenuControl menuControl = new MenuControl(
                authenticationService,
                clientConsoleService,
                ownerConsoleService,
                driverConsoleService,
                interactionService,
                adminConsoleService,
                new BufferedReader(new InputStreamReader(System.in)),
                System.out);

        try {
            menuControl.run();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
