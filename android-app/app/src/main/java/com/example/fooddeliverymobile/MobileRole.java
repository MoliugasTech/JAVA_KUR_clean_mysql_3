package com.example.fooddeliverymobile;

public enum MobileRole {
    CLIENT,
    DRIVER;

    public static MobileRole fromApi(String value) {
        if (value == null) {
            return null;
        }
        for (MobileRole role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }
}
