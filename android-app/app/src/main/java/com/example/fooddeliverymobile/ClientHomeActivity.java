package com.example.fooddeliverymobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ClientHomeActivity extends BaseHomeActivity {
    @Override protected MobileRole requiredRole() { return MobileRole.CLIENT; }
    @Override protected int titleResource() { return R.string.client_space; }
    @Override protected int descriptionResource() { return R.string.client_home_description; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button = findViewById(R.id.restaurantsButton);
        if (button != null) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(view ->
                    startActivity(new Intent(this, ClientRestaurantsActivity.class)));
        }
    }
}
