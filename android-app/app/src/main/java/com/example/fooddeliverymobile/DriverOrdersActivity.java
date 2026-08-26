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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriverOrdersActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "mode";
    public static final String MODE_AVAILABLE = "available";
    public static final String MODE_MINE = "mine";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private ListView list;
    private ProgressBar progress;
    private TextView message;
    private SessionManager session;
    private boolean availableMode;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_client_orders);
        session = new SessionManager(this);
        availableMode = MODE_AVAILABLE.equals(getIntent().getStringExtra(EXTRA_MODE));
        list = findViewById(R.id.ordersList);
        progress = findViewById(R.id.ordersProgress);
        message = findViewById(R.id.ordersMessage);
        ((TextView) findViewById(R.id.ordersTitle)).setText(
                availableMode ? R.string.available_deliveries : R.string.my_deliveries);
        findViewById(R.id.ordersBackButton).setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        progress.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
        message.setVisibility(View.GONE);
        String url = availableMode ? Constants.DRIVER_AVAILABLE_ORDERS_URL
                : Constants.DRIVER_ORDERS_URL;
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result = RestOperations.sendGet(url, session.getToken());
                handler.post(() -> showResult(result));
            } catch (IOException exception) {
                handler.post(() -> showMessage(R.string.error_server_connection));
            }
        });
    }

    private void showResult(RestOperations.HttpResult result) {
        if (!result.isSuccessful()) {
            showMessage(R.string.error_driver_orders_failed);
            return;
        }
        try {
            DriverOrderInfo[] data = gson.fromJson(result.body(), DriverOrderInfo[].class);
            List<DriverOrderInfo> orders = data == null ? List.of() : Arrays.asList(data);
            if (orders.isEmpty()) {
                showMessage(availableMode ? R.string.no_available_deliveries
                        : R.string.no_driver_orders);
                return;
            }
            progress.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, orders));
            list.setOnItemClickListener((parent, view, position, id) -> {
                DriverOrderInfo order = orders.get(position);
                if (availableMode) confirmAccept(order); else openDetail(order.getId());
            });
        } catch (RuntimeException exception) {
            showMessage(R.string.error_invalid_server_response);
        }
    }

    private void confirmAccept(DriverOrderInfo order) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.accept_delivery)
                .setMessage(getString(R.string.accept_delivery_confirmation,
                        order.getRestaurantName(), order.getDeliveryAddress()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.accept, (dialog, which) -> accept(order.getId()))
                .show();
    }

    private void accept(long orderId) {
        progress.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result = RestOperations.sendPost(
                        Constants.driverAcceptUrl(orderId), "", session.getToken());
                handler.post(() -> {
                    progress.setVisibility(View.GONE);
                    if (result.isSuccessful()) {
                        Toast.makeText(this, R.string.delivery_accepted,
                                Toast.LENGTH_SHORT).show();
                        openDetail(orderId);
                    } else {
                        Toast.makeText(this, R.string.error_accept_delivery,
                                Toast.LENGTH_LONG).show();
                        loadOrders();
                    }
                });
            } catch (IOException exception) {
                handler.post(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.error_server_connection,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void openDetail(long orderId) {
        Intent intent = new Intent(this, DriverOrderDetailActivity.class);
        intent.putExtra("orderId", orderId);
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
