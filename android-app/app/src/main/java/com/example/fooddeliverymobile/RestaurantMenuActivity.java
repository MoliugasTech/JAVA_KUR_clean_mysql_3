package com.example.fooddeliverymobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestaurantMenuActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private ListView list;
    private ProgressBar progress;
    private ProgressBar orderProgress;
    private TextView message;
    private TextView cartSummary;
    private Button orderButton;
    private SessionManager session;
    private MenuItemCartAdapter adapter;
    private long restaurantId;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_restaurant_menu);
        session = new SessionManager(this);
        restaurantId = getIntent().getLongExtra("id", -1L);
        if (restaurantId < 0) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.restaurantName)).setText(getIntent().getStringExtra("name"));
        ((TextView) findViewById(R.id.restaurantDescription))
                .setText(getIntent().getStringExtra("description"));
        ((TextView) findViewById(R.id.restaurantDetails)).setText(getString(
                R.string.restaurant_details,
                getIntent().getStringExtra("address"),
                getIntent().getStringExtra("type"),
                getIntent().getStringExtra("opening"),
                getIntent().getStringExtra("closing")));
        findViewById(R.id.menuBackButton).setOnClickListener(v -> finish());

        list = findViewById(R.id.menuItemsList);
        progress = findViewById(R.id.menuProgress);
        orderProgress = findViewById(R.id.orderProgress);
        message = findViewById(R.id.menuMessage);
        cartSummary = findViewById(R.id.cartSummary);
        orderButton = findViewById(R.id.orderButton);
        orderButton.setOnClickListener(view -> submitOrder());
        updateCart();
        loadMenu();
    }

    private void loadMenu() {
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result = RestOperations.sendGet(
                        Constants.menuUrl(restaurantId), session.getToken());
                handler.post(() -> showMenuResult(result));
            } catch (IOException e) {
                handler.post(() -> showMessage(R.string.error_server_connection));
            }
        });
    }

    private void showMenuResult(RestOperations.HttpResult result) {
        if (!result.isSuccessful()) {
            showMessage(R.string.error_menu_failed);
            return;
        }
        try {
            MenuItemInfo[] data = gson.fromJson(result.body(), MenuItemInfo[].class);
            List<MenuItemInfo> items = data == null ? List.of() : Arrays.asList(data);
            if (items.isEmpty()) {
                showMessage(R.string.no_menu_items);
                return;
            }
            adapter = new MenuItemCartAdapter(this, items, this::updateCart);
            list.setAdapter(adapter);
            progress.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            updateCart();
        } catch (RuntimeException e) {
            showMessage(R.string.error_invalid_server_response);
        }
    }

    private void updateCart() {
        int count = adapter == null ? 0 : adapter.getUnitCount();
        if (count == 0) {
            cartSummary.setText(R.string.cart_empty);
        } else {
            cartSummary.setText(getString(
                    R.string.cart_summary,
                    count,
                    adapter.getTotal().setScale(2, RoundingMode.HALF_UP)));
        }
        orderButton.setEnabled(count > 0 && orderProgress.getVisibility() != View.VISIBLE);
    }

    private void submitOrder() {
        if (adapter == null || adapter.getUnitCount() == 0) {
            Toast.makeText(this, R.string.error_empty_cart, Toast.LENGTH_SHORT).show();
            return;
        }

        ClientOrderRequest request =
                ClientOrderRequest.from(restaurantId, adapter.getQuantities());
        String json = gson.toJson(request);
        orderProgress.setVisibility(View.VISIBLE);
        orderButton.setEnabled(false);

        executor.execute(() -> {
            try {
                RestOperations.HttpResult result = RestOperations.sendPost(
                        Constants.CLIENT_ORDERS_URL, json, session.getToken());
                handler.post(() -> showOrderResult(result));
            } catch (IOException e) {
                handler.post(() -> {
                    orderProgress.setVisibility(View.GONE);
                    updateCart();
                    Toast.makeText(this, R.string.error_server_connection,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showOrderResult(RestOperations.HttpResult result) {
        orderProgress.setVisibility(View.GONE);
        if (!result.isSuccessful()) {
            updateCart();
            Toast.makeText(this, readServerError(result.body()),
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {
            CreatedOrderResponse order =
                    gson.fromJson(result.body(), CreatedOrderResponse.class);
            if (order == null || order.getId() <= 0) {
                throw new IllegalStateException("Missing order id");
            }
            adapter.clearCart();
            Toast.makeText(this,
                    getString(R.string.order_created, order.getId(), order.getTotalPrice()),
                    Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            updateCart();
            Toast.makeText(this, R.string.error_invalid_server_response,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String readServerError(String responseBody) {
        try {
            ApiError error = gson.fromJson(responseBody, ApiError.class);
            if (error != null && error.message != null && !error.message.isBlank()) {
                return error.message;
            }
        } catch (RuntimeException ignored) {
        }
        return getString(R.string.error_order_failed);
    }

    private void showMessage(int resource) {
        progress.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        message.setText(resource);
        message.setVisibility(View.VISIBLE);
        orderButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private static final class ApiError {
        private String message;
    }
}
