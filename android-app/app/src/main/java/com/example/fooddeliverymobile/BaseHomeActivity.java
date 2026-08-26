package com.example.fooddeliverymobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseHomeActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SessionManager sessionManager;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || sessionManager.getRole() != requiredRole()) {
            sessionManager.clear();
            openLogin();
            return;
        }

        setContentView(R.layout.activity_role_home);
        ((TextView) findViewById(R.id.homeGreeting))
                .setText(getString(R.string.greeting, sessionManager.getUsername()));
        ((TextView) findViewById(R.id.homeTitle)).setText(titleResource());
        ((TextView) findViewById(R.id.homeDescription)).setText(descriptionResource());
        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(view -> logout());
    }

    protected abstract MobileRole requiredRole();

    @StringRes
    protected abstract int titleResource();

    @StringRes
    protected abstract int descriptionResource();

    private void logout() {
        logoutButton.setEnabled(false);
        String token = sessionManager.getToken();
        executor.execute(() -> {
            try {
                RestOperations.sendPost(Constants.LOGOUT_URL, "", token);
            } catch (IOException ignored) {
                // Lokali sesija vis tiek išvaloma, kad naudotojas galėtų atsijungti.
            }
            handler.post(() -> {
                sessionManager.clear();
                openLogin();
            });
        });
    }

    private void openLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
