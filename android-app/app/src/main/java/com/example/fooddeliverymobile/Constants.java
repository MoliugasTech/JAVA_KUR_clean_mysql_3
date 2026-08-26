package com.example.fooddeliverymobile;

public final class Constants {
    public static final String HOME_URL = BuildConfig.API_BASE_URL;
    public static final String LOGIN_URL = HOME_URL + "api/auth/login";
    public static final String LOGOUT_URL = HOME_URL + "api/auth/logout";
    public static final String CLIENTS_URL = HOME_URL + "api/clients";
    public static final String DRIVERS_URL = HOME_URL + "api/drivers";
    public static final String RESTAURANTS_URL = HOME_URL + "api/catalog/restaurants";
    public static final String CLIENT_ORDERS_URL = HOME_URL + "api/client/orders";

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
}
