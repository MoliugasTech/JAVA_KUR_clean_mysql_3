package com.example.fooddeliverymobile;

public final class Constants {
    public static final String HOME_URL = BuildConfig.API_BASE_URL;
    public static final String LOGIN_URL = HOME_URL + "api/auth/login";
    public static final String LOGOUT_URL = HOME_URL + "api/auth/logout";
    public static final String CLIENTS_URL = HOME_URL + "api/clients";
    public static final String DRIVERS_URL = HOME_URL + "api/drivers";

    private Constants() {
    }
}
