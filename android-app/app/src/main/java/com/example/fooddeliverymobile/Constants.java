package com.example.fooddeliverymobile;

public final class Constants {
    public static final String HOME_URL = BuildConfig.API_BASE_URL;
    public static final String LOGIN_URL = HOME_URL + "api/auth/login";
    public static final String LOGOUT_URL = HOME_URL + "api/auth/logout";
    public static final String CLIENTS_URL = HOME_URL + "api/clients";
    public static final String DRIVERS_URL = HOME_URL + "api/drivers";
    public static final String RESTAURANTS_URL = HOME_URL + "api/catalog/restaurants";
    public static final String CLIENT_ORDERS_URL = HOME_URL + "api/client/orders";
    public static final String DRIVER_ORDERS_BASE_URL = HOME_URL + "api/driver/orders";
    public static final String DRIVER_AVAILABLE_ORDERS_URL =
            DRIVER_ORDERS_BASE_URL + "/available";
    public static final String DRIVER_ORDERS_URL = DRIVER_ORDERS_BASE_URL + "/mine";

    private Constants() {}

    public static String menuUrl(long restaurantId) {
        return RESTAURANTS_URL + "/" + restaurantId + "/menu-items";
    }

    public static String orderUrl(long orderId) {
        return CLIENT_ORDERS_URL + "/" + orderId;
    }

    public static String messagesUrl(long orderId) {
        return orderUrl(orderId) + "/messages";
    }

    public static String clientReviewsUrl(long orderId) {
        return orderUrl(orderId) + "/reviews";
    }

    public static String clientReviewUrl(long orderId, String target) {
        return clientReviewsUrl(orderId) + "/" + target;
    }

    public static String driverOrderUrl(long orderId) {
        return DRIVER_ORDERS_BASE_URL + "/" + orderId;
    }

    public static String driverAcceptUrl(long orderId) {
        return driverOrderUrl(orderId) + "/accept";
    }

    public static String driverStatusUrl(long orderId) {
        return driverOrderUrl(orderId) + "/status";
    }

    public static String driverMessagesUrl(long orderId) {
        return driverOrderUrl(orderId) + "/messages";
    }

    public static String driverReviewsUrl(long orderId) {
        return driverOrderUrl(orderId) + "/reviews";
    }

    public static String driverReviewUrl(long orderId) {
        return driverReviewsUrl(orderId) + "/client";
    }
}
