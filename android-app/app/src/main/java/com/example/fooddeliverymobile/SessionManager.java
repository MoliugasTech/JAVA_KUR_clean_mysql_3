package com.example.fooddeliverymobile;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFERENCES = "food_delivery_session";
    private static final String TOKEN = "token";
    private static final String USER_ID = "user_id";
    private static final String USERNAME = "username";
    private static final String ROLE = "role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public void save(LoginResponse response, MobileRole role) {
        preferences.edit()
                .putString(TOKEN, response.getToken())
                .putLong(USER_ID, response.getUserId())
                .putString(USERNAME, response.getUsername())
                .putString(ROLE, role.name())
                .apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && getRole() != null;
    }

    public String getToken() {
        return preferences.getString(TOKEN, null);
    }

    public long getUserId() {
        return preferences.getLong(USER_ID, -1L);
    }

    public String getUsername() {
        return preferences.getString(USERNAME, "");
    }

    public MobileRole getRole() {
        return MobileRole.fromApi(preferences.getString(ROLE, null));
    }

    public void updateUsername(String username) {
        preferences.edit().putString(USERNAME, username).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
