package com.example.fooddeliverymobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private SessionManager session;
    private MobileRole role;
    private ProgressBar progress;
    private EditText username;
    private EditText password;
    private EditText name;
    private EditText surname;
    private EditText email;
    private EditText phone;
    private EditText deliveryAddress;
    private TextView bonusPoints;
    private Spinner vehicleType;
    private LinearLayout clientFields;
    private LinearLayout driverFields;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        session = new SessionManager(this);
        role = session.getRole();
        if (!session.isLoggedIn() || role == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_profile);
        bindViews();
        configureRoleFields();
        findViewById(R.id.profileBackButton).setOnClickListener(view -> finish());
        saveButton.setOnClickListener(view -> saveProfile());
        loadProfile();
    }

    private void bindViews() {
        progress = findViewById(R.id.profileProgress);
        username = findViewById(R.id.profileUsername);
        password = findViewById(R.id.profilePassword);
        name = findViewById(R.id.profileName);
        surname = findViewById(R.id.profileSurname);
        email = findViewById(R.id.profileEmail);
        phone = findViewById(R.id.profilePhone);
        deliveryAddress = findViewById(R.id.profileDeliveryAddress);
        bonusPoints = findViewById(R.id.profileBonusPoints);
        vehicleType = findViewById(R.id.profileVehicleType);
        clientFields = findViewById(R.id.clientProfileFields);
        driverFields = findViewById(R.id.driverProfileFields);
        saveButton = findViewById(R.id.profileSaveButton);
        vehicleType.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.vehicle_types, android.R.layout.simple_spinner_dropdown_item));
    }

    private void configureRoleFields() {
        boolean client = role == MobileRole.CLIENT;
        clientFields.setVisibility(client ? View.VISIBLE : View.GONE);
        driverFields.setVisibility(client ? View.GONE : View.VISIBLE);
    }

    private void loadProfile() {
        setLoading(true);
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result = RestOperations.sendGet(
                        Constants.profileUrl(role, session.getUserId()), session.getToken());
                handler.post(() -> showProfile(result));
            } catch (IOException exception) {
                handler.post(() -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_server_connection,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showProfile(RestOperations.HttpResult result) {
        setLoading(false);
        if (!result.isSuccessful()) {
            Toast.makeText(this, R.string.error_profile_load,
                    Toast.LENGTH_LONG).show();
            return;
        }
        try {
            ProfileInfo profile = gson.fromJson(result.body(), ProfileInfo.class);
            username.setText(profile.getUsername());
            name.setText(profile.getName());
            surname.setText(profile.getSurname());
            email.setText(profile.getEmail());
            phone.setText(profile.getPhoneNumber());
            if (role == MobileRole.CLIENT) {
                deliveryAddress.setText(profile.getDeliveryAddress());
                bonusPoints.setText(getString(
                        R.string.bonus_points_value, profile.getBonusPoints()));
            } else {
                selectVehicle(profile.getVehicleType());
            }
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.error_invalid_server_response,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void selectVehicle(String value) {
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) vehicleType.getAdapter();
        for (int index = 0; index < adapter.getCount(); index++) {
            if (value.equals(String.valueOf(adapter.getItem(index)))) {
                vehicleType.setSelection(index);
                return;
            }
        }
    }

    private void saveProfile() {
        if (!validateRequiredFields()) return;
        JsonObject json = new JsonObject();
        json.addProperty("username", value(username));
        String newPassword = value(password);
        if (!newPassword.isEmpty()) json.addProperty("password", newPassword);
        json.addProperty("name", value(name));
        json.addProperty("surname", value(surname));
        json.addProperty("email", value(email));
        json.addProperty("phoneNumber", value(phone));
        if (role == MobileRole.CLIENT) {
            json.addProperty("deliveryAddress", value(deliveryAddress));
        } else {
            json.addProperty("vehicleType", String.valueOf(vehicleType.getSelectedItem()));
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result = RestOperations.sendPut(
                        Constants.profileUrl(role, session.getUserId()),
                        gson.toJson(json), session.getToken());
                handler.post(() -> handleSaved(result));
            } catch (IOException exception) {
                handler.post(() -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.error_server_connection,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean validateRequiredFields() {
        EditText[] common = {username, name, surname, email};
        for (EditText field : common) {
            if (value(field).isEmpty()) {
                field.setError(getString(R.string.error_required_profile_fields));
                field.requestFocus();
                return false;
            }
        }
        if (role == MobileRole.CLIENT && value(deliveryAddress).isEmpty()) {
            deliveryAddress.setError(getString(R.string.error_delivery_address_required));
            deliveryAddress.requestFocus();
            return false;
        }
        String newPassword = value(password);
        if (!newPassword.isEmpty() && newPassword.length() < 8) {
            password.setError(getString(R.string.error_password_length));
            password.requestFocus();
            return false;
        }
        return true;
    }

    private void handleSaved(RestOperations.HttpResult result) {
        setLoading(false);
        if (!result.isSuccessful()) {
            Toast.makeText(this, R.string.error_profile_save,
                    Toast.LENGTH_LONG).show();
            return;
        }
        try {
            ProfileInfo profile = gson.fromJson(result.body(), ProfileInfo.class);
            session.updateUsername(profile.getUsername());
            password.setText("");
            Toast.makeText(this, R.string.profile_saved,
                    Toast.LENGTH_SHORT).show();
            showProfile(result);
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.error_invalid_server_response,
                    Toast.LENGTH_LONG).show();
        }
    }

    private String value(EditText field) {
        return field.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
