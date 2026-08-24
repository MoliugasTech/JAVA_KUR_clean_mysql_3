package com.example.javakur.gui;

import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.AdminConsoleService;
import com.example.javakur.service.AuthenticationService;
import com.example.javakur.service.UserService;

public final class GuiContext {
    private static final UserRepository USER_REPOSITORY = new UserRepository();
    private static final AuthenticationService AUTHENTICATION_SERVICE =
            new AuthenticationService(USER_REPOSITORY);
    private static final UserService USER_SERVICE = new UserService(USER_REPOSITORY);
    private static final AdminConsoleService ADMIN_SERVICE = new AdminConsoleService(
            USER_REPOSITORY, new RestaurantRepository(), new OrderRepository(), new ReviewRepository());

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
}
