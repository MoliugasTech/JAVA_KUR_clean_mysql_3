package com.example.javakur.gui;

import com.example.javakur.repository.MenuItemRepository;
import com.example.javakur.repository.OrderMessageRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.AdminConsoleService;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.MenuService;
import com.example.javakur.service.OrderMessageService;
import com.example.javakur.service.OrderService;
import com.example.javakur.service.OwnerConsoleService;
import com.example.javakur.service.RestaurantService;
import com.example.javakur.service.UserService;

public final class GuiContext {
    private static final UserRepository USER_REPOSITORY = new UserRepository();
    private static final RestaurantRepository RESTAURANT_REPOSITORY = new RestaurantRepository();
    private static final OrderRepository ORDER_REPOSITORY = new OrderRepository();
    private static final MenuItemRepository MENU_ITEM_REPOSITORY = new MenuItemRepository();
    private static final ReviewRepository REVIEW_REPOSITORY = new ReviewRepository();
    private static final OrderMessageRepository MESSAGE_REPOSITORY =
            new OrderMessageRepository();
    private static final AuthenticationService AUTHENTICATION_SERVICE =
            new AuthenticationService(USER_REPOSITORY);
    private static final UserService USER_SERVICE = new UserService(USER_REPOSITORY);
    private static final AdminConsoleService ADMIN_SERVICE = new AdminConsoleService(
            USER_REPOSITORY, RESTAURANT_REPOSITORY, ORDER_REPOSITORY, REVIEW_REPOSITORY,
            MENU_ITEM_REPOSITORY, MESSAGE_REPOSITORY);
    private static final OwnerConsoleService OWNER_SERVICE = new OwnerConsoleService(
            USER_SERVICE, new RestaurantService(RESTAURANT_REPOSITORY),
            new MenuService(MENU_ITEM_REPOSITORY), new OrderService(ORDER_REPOSITORY),
            RESTAURANT_REPOSITORY, ORDER_REPOSITORY,
            new OrderMessageService(MESSAGE_REPOSITORY));

    private GuiContext() {
    }

    public static AuthenticationService authenticationService() {
        return AUTHENTICATION_SERVICE;
    }

    public static UserService userService() {
        return USER_SERVICE;
    }

    public static AdminConsoleService adminService() {
        return ADMIN_SERVICE;
    }

    public static OwnerConsoleService ownerService() {
        return OWNER_SERVICE;
    }
}
