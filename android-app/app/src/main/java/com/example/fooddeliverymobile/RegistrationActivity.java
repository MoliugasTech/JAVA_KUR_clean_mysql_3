package com.example.fooddeliverymobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrationActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    private EditText usernameField;
    private EditText passwordField;
    private EditText nameField;
    private EditText surnameField;
    private EditText emailField;
    private EditText phoneField;
    private EditText deliveryAddressField;
    private Spinner vehicleTypeSpinner;
    private RadioGroup roleGroup;
    private TextView errorText;
    private ProgressBar progressBar;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usernameField = findViewById(R.id.regUsernameField);
        passwordField = findViewById(R.id.regPasswordField);
        nameField = findViewById(R.id.regNameField);
        surnameField = findViewById(R.id.regSurnameField);
        emailField = findViewById(R.id.regEmailField);
        phoneField = findViewById(R.id.regPhoneField);
        deliveryAddressField = findViewById(R.id.regDeliveryAddressField);
        vehicleTypeSpinner = findViewById(R.id.regVehicleTypeSpinner);
        roleGroup = findViewById(R.id.regRoleGroup);
        errorText = findViewById(R.id.registrationErrorText);
        progressBar = findViewById(R.id.registrationProgress);
        createButton = findViewById(R.id.createAccountButton);

        ArrayAdapter<CharSequence> vehicleAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.vehicle_types,
                android.R.layout.simple_spinner_item);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleTypeSpinner.setAdapter(vehicleAdapter);

        roleGroup.setOnCheckedChangeListener((group, checkedId) -> updateRoleFields());
        createButton.setOnClickListener(this::createAccount);
        findViewById(R.id.backToLoginButton).setOnClickListener(view -> finish());
        updateRoleFields();
    }

    public void createAccount(View view) {
        boolean driver = roleGroup.getCheckedRadioButtonId() == R.id.driverRadio;
        String username = text(usernameField);
        String password = text(passwordField);
        String name = text(nameField);
        String surname = text(surnameField);
        String email = text(emailField);
        String phone = text(phoneField);

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()
                || surname.isEmpty() || email.isEmpty()) {
            showError(getString(R.string.error_required_registration_fields));
            return;
        }
        if (!driver && text(deliveryAddressField).isEmpty()) {
            showError(getString(R.string.error_delivery_address_required));
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("password", password);
        json.addProperty("name", name);
        json.addProperty("surname", surname);
        json.addProperty("email", email);
        if (!phone.isEmpty()) {
            json.addProperty("phoneNumber", phone);
        }

        String url;
        if (driver) {
            url = Constants.DRIVERS_URL;
            json.addProperty("vehicleType", vehicleTypeSpinner.getSelectedItem().toString());
        } else {
            url = Constants.CLIENTS_URL;
            json.addProperty("deliveryAddress", text(deliveryAddressField));
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result =
                        RestOperations.sendPost(url, gson.toJson(json));
                handler.post(() -> handleRegistrationResult(result));
            } catch (IOException exception) {
                handler.post(() -> {
                    setLoading(false);
                    showError(getString(R.string.error_server_connection));
                });
            }
        });
    }

    private void handleRegistrationResult(RestOperations.HttpResult result) {
        setLoading(false);
        if (!result.isSuccessful()) {
            showError(errorMessage(result.body()));
            return;
        }
        Toast.makeText(this, R.string.registration_success, Toast.LENGTH_LONG).show();
        finish();
    }

    private void updateRoleFields() {
        boolean driver = roleGroup.getCheckedRadioButtonId() == R.id.driverRadio;
        deliveryAddressField.setVisibility(driver ? View.GONE : View.VISIBLE);
        vehicleTypeSpinner.setVisibility(driver ? View.VISIBLE : View.GONE);
    }

    private String text(EditText field) {
        return field.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        createButton.setEnabled(!loading);
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
            return getString(R.string.error_registration_failed);
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
            // Žemiau grąžinamas bendras tekstas.
        }
        return getString(R.string.error_registration_failed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
