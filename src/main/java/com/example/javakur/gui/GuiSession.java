package com.example.javakur.gui;

import com.example.javakur.security.UserSession;

public final class GuiSession {
    private static UserSession current;

    private GuiSession() {
    }

    public static UserSession requireCurrent() {
        if (current == null) {
            throw new IllegalStateException("Naudotojas neprisijungęs");
        }
        return current;
    }

    static void set(UserSession session) {
        current = session;
    }

    static void clear() {
        current = null;
    }
}
