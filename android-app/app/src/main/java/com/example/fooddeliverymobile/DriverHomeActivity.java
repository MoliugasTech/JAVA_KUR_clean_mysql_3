package com.example.fooddeliverymobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DriverHomeActivity extends BaseHomeActivity {
    @Override protected MobileRole requiredRole() { return MobileRole.DRIVER; }
    @Override protected int titleResource() { return R.string.driver_space; }
    @Override protected int descriptionResource() { return R.string.driver_home_description; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureButton(R.id.availableDeliveriesButton, DriverOrdersActivity.MODE_AVAILABLE);
        configureButton(R.id.driverOrdersButton, DriverOrdersActivity.MODE_MINE);
    }

    private void configureButton(int buttonId, String mode) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(view -> {
                Intent intent = new Intent(this, DriverOrdersActivity.class);
                intent.putExtra(DriverOrdersActivity.EXTRA_MODE, mode);
                startActivity(intent);
            });
        }
    }
}
