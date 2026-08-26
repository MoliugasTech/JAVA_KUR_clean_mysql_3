package com.example.fooddeliverymobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRestaurantsActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private ListView list;
    private ProgressBar progress;
    private TextView message;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_client_restaurants);
        session = new SessionManager(this);
        list = findViewById(R.id.restaurantsList);
        progress = findViewById(R.id.catalogProgress);
        message = findViewById(R.id.catalogMessage);
        findViewById(R.id.catalogBackButton).setOnClickListener(v -> finish());
        loadRestaurants();
    }

    private void loadRestaurants() {
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result =
                        RestOperations.sendGet(Constants.RESTAURANTS_URL, session.getToken());
                handler.post(() -> showResult(result));
            } catch (IOException e) {
                handler.post(() -> showMessage(R.string.error_server_connection));
            }
        });
    }

    private void showResult(RestOperations.HttpResult result) {
        if (!result.isSuccessful()) {
            showMessage(R.string.error_catalog_failed);
            return;
        }
        try {
            RestaurantInfo[] data = gson.fromJson(result.body(), RestaurantInfo[].class);
            List<RestaurantInfo> restaurants = data == null ? List.of() : Arrays.asList(data);
            if (restaurants.isEmpty()) {
                showMessage(R.string.no_restaurants);
                return;
            }
            progress.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, restaurants));
            list.setOnItemClickListener((parent, view, position, id) ->
                    openMenu(restaurants.get(position)));
        } catch (RuntimeException e) {
            showMessage(R.string.error_invalid_server_response);
        }
    }

    private void openMenu(RestaurantInfo restaurant) {
        Intent intent = new Intent(this, RestaurantMenuActivity.class);
        intent.putExtra("id", restaurant.getId());
        intent.putExtra("name", restaurant.getName());
        intent.putExtra("description", restaurant.getDescription());
        intent.putExtra("address", restaurant.getAddress());
        intent.putExtra("type", restaurant.getType());
        intent.putExtra("opening", restaurant.getOpeningTime());
        intent.putExtra("closing", restaurant.getClosingTime());
        startActivity(intent);
    }

    private void showMessage(int resource) {
        progress.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        message.setText(resource);
        message.setVisibility(View.VISIBLE);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
