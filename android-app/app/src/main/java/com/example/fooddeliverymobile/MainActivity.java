package com.example.fooddeliverymobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    private EditText usernameField;
    private EditText passwordField;
    private TextView errorText;
    private ProgressBar progressBar;
    private Button loginButton;
    private Button registrationButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openRoleWindow(sessionManager.getRole());
            return;
        }

        setContentView(R.layout.activity_main);
        usernameField = findViewById(R.id.loginField);
        passwordField = findViewById(R.id.passwordField);
        errorText = findViewById(R.id.loginErrorText);
        progressBar = findViewById(R.id.loginProgress);
        loginButton = findViewById(R.id.loginButton);
        registrationButton = findViewById(R.id.registrationButton);

        loginButton.setOnClickListener(this::validateUser);
        registrationButton.setOnClickListener(this::loadRegistrationWindow);
    }

    public void validateUser(View view) {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_enter_credentials));
            return;
        }

        setLoading(true);
        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("password", password);

        executor.execute(() -> {
            try {
                RestOperations.HttpResult result =
                        RestOperations.sendPost(Constants.LOGIN_URL, gson.toJson(json));
                handleLoginResult(result);
            } catch (IOException exception) {
                handler.post(() -> {
                    setLoading(false);
                    showError(getString(R.string.error_server_connection));
                });
            }
        });
    }

    private void handleLoginResult(RestOperations.HttpResult result) {
        if (!result.isSuccessful()) {
            handler.post(() -> {
                setLoading(false);
                showError(errorMessage(result.body()));
            });
            return;
        }

        LoginResponse response;
        try {
            response = gson.fromJson(result.body(), LoginResponse.class);
        } catch (RuntimeException exception) {
            handler.post(() -> {
                setLoading(false);
                showError(getString(R.string.error_invalid_server_response));
            });
            return;
        }

        MobileRole role = response == null ? null : MobileRole.fromApi(response.getRole());
        if (role == null || response.getToken() == null) {
            if (response != null && response.getToken() != null) {
                try {
                    RestOperations.sendPost(Constants.LOGOUT_URL, "", response.getToken());
                } catch (IOException ignored) {
                    // Tokenas vis tiek nebus saugomas telefone.
                }
            }
            handler.post(() -> {
                setLoading(false);
                showError(getString(R.string.error_mobile_roles_only));
            });
            return;
        }

        handler.post(() -> {
            sessionManager.save(response, role);
            passwordField.setText("");
            setLoading(false);
            openRoleWindow(role);
        });
    }

    public void loadRegistrationWindow(View view) {
        startActivity(new Intent(this, RegistrationActivity.class));
    }

    private void openRoleWindow(MobileRole role) {
        Class<?> destination = role == MobileRole.CLIENT
                ? ClientHomeActivity.class
                : DriverHomeActivity.class;
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        registrationButton.setEnabled(!loading);
        usernameField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        if (loading) {
            errorText.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private String errorMessage(String body) {
        if (body == null || body.isBlank()) {
            return getString(R.string.error_login_failed);
        }
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json != null && json.has("message")) {
                String message = json.get("message").getAsString();
                if (!message.isBlank()) {
                    return message;
                }
            }
        } catch (RuntimeException ignored) {
            // Žemiau grąžinamas bendras naudotojui suprantamas tekstas.
        }
        return getString(R.string.error_login_failed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
