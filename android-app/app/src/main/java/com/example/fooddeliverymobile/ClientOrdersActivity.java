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

public class ClientOrdersActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_client_orders);
        session = new SessionManager(this);
        list = findViewById(R.id.ordersList);
        progress = findViewById(R.id.ordersProgress);
        message = findViewById(R.id.ordersMessage);
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
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result = RestOperations.sendGet(
                        Constants.CLIENT_ORDERS_URL, session.getToken());
                handler.post(() -> showResult(result));
            } catch (IOException exception) {
                handler.post(() -> showMessage(R.string.error_server_connection));
            }
        });
    }

    private void showResult(RestOperations.HttpResult result) {
        if (!result.isSuccessful()) {
            showMessage(R.string.error_orders_failed);
            return;
        }
        try {
            ClientOrderInfo[] data = gson.fromJson(result.body(), ClientOrderInfo[].class);
            List<ClientOrderInfo> orders = data == null ? List.of() : Arrays.asList(data);
            if (orders.isEmpty()) {
                showMessage(R.string.no_orders);
                return;
            }
            progress.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, orders));
            list.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(this, ClientOrderDetailActivity.class);
                intent.putExtra("orderId", orders.get(position).getId());
                startActivity(intent);
            });
        } catch (RuntimeException exception) {
            showMessage(R.string.error_invalid_server_response);
        }
    }

    private void showMessage(int resource) {
        progress.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        message.setText(resource);
        message.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
