package com.example.fooddeliverymobile;

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

public class RestaurantMenuActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_restaurant_menu);
        session = new SessionManager(this);
        long id = getIntent().getLongExtra("id", -1L);
        if (id < 0) { finish(); return; }

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
        message = findViewById(R.id.menuMessage);
        loadMenu(id);
    }

    private void loadMenu(long restaurantId) {
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result =
                        RestOperations.sendGet(Constants.menuUrl(restaurantId), session.getToken());
                handler.post(() -> showResult(result));
            } catch (IOException e) {
                handler.post(() -> showMessage(R.string.error_server_connection));
            }
        });
    }

    private void showResult(RestOperations.HttpResult result) {
        if (!result.isSuccessful()) { showMessage(R.string.error_menu_failed); return; }
        try {
            MenuItemInfo[] data = gson.fromJson(result.body(), MenuItemInfo[].class);
            List<MenuItemInfo> items = data == null ? List.of() : Arrays.asList(data);
            if (items.isEmpty()) { showMessage(R.string.no_menu_items); return; }
            progress.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            list.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, items));
        } catch (RuntimeException e) {
            showMessage(R.string.error_invalid_server_response);
        }
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
