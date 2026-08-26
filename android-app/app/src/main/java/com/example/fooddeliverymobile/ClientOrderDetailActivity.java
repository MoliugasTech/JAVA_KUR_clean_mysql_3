package com.example.fooddeliverymobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientOrderDetailActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private SessionManager session;
    private long orderId;
    private ProgressBar progress;
    private TextView title;
    private TextView summary;
    private TextView items;
    private TextView conversationMessage;
    private ListView messages;
    private EditText messageInput;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_client_order_detail);
        session = new SessionManager(this);
        orderId = getIntent().getLongExtra("orderId", -1L);
        if (orderId < 0) {
            finish();
            return;
        }
        progress = findViewById(R.id.orderDetailProgress);
        title = findViewById(R.id.orderDetailTitle);
        summary = findViewById(R.id.orderDetailSummary);
        items = findViewById(R.id.orderDetailItems);
        conversationMessage = findViewById(R.id.conversationMessage);
        messages = findViewById(R.id.messagesList);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendMessageButton);
        findViewById(R.id.orderDetailBackButton).setOnClickListener(view -> finish());
        sendButton.setOnClickListener(view -> sendMessage());
        loadOrder();
    }

    private void loadOrder() {
        progress.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                RestOperations.HttpResult orderResult = RestOperations.sendGet(
                        Constants.orderUrl(orderId), session.getToken());
                RestOperations.HttpResult messagesResult = RestOperations.sendGet(
                        Constants.messagesUrl(orderId), session.getToken());
                handler.post(() -> showOrder(orderResult, messagesResult));
            } catch (IOException exception) {
                handler.post(() -> showError(R.string.error_server_connection));
            }
        });
    }

    private void showOrder(RestOperations.HttpResult orderResult,
                           RestOperations.HttpResult messagesResult) {
        progress.setVisibility(View.GONE);
        if (!orderResult.isSuccessful() || !messagesResult.isSuccessful()) {
            showError(R.string.error_order_details_failed);
            return;
        }
        try {
            ClientOrderDetail order = gson.fromJson(orderResult.body(), ClientOrderDetail.class);
            title.setText(getString(R.string.order_number, order.getId()));
            String driver = order.getDriverName().isBlank()
                    ? getString(R.string.driver_not_assigned) : order.getDriverName();
            summary.setText(getString(R.string.order_detail_summary,
                    order.getRestaurantName(), StatusLabels.toLithuanian(order.getStatus()),
                    order.getCreatedAt(), driver, order.getTotalPrice().toPlainString()));
            StringBuilder itemText = new StringBuilder();
            for (OrderLineInfo item : order.getItems()) {
                if (itemText.length() > 0) itemText.append('\n');
                itemText.append(item.getQuantity()).append(" × ")
                        .append(item.getName()).append(" — ")
                        .append(item.getLineTotal().toPlainString()).append(" €");
            }
            items.setText(itemText);
            setMessagingAllowed(order.isMessagingAllowed());

            OrderMessageInfo[] data = gson.fromJson(messagesResult.body(), OrderMessageInfo[].class);
            showMessages(data == null ? List.of() : Arrays.asList(data));
        } catch (RuntimeException exception) {
            showError(R.string.error_invalid_server_response);
        }
    }

    private void showMessages(List<OrderMessageInfo> data) {
        if (data.isEmpty()) {
            messages.setVisibility(View.GONE);
            conversationMessage.setText(R.string.no_messages);
            conversationMessage.setVisibility(View.VISIBLE);
            return;
        }
        conversationMessage.setVisibility(View.GONE);
        messages.setVisibility(View.VISIBLE);
        messages.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, data));
        messages.post(() -> messages.setSelection(messages.getCount() - 1));
    }

    private void setMessagingAllowed(boolean allowed) {
        messageInput.setEnabled(allowed);
        sendButton.setEnabled(allowed);
        if (!allowed) {
            messageInput.setHint(R.string.conversation_read_only);
        }
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            messageInput.setError(getString(R.string.error_empty_message));
            return;
        }
        sendButton.setEnabled(false);
        JsonObject json = new JsonObject();
        json.addProperty("content", content);
        executor.execute(() -> {
            try {
                RestOperations.HttpResult result = RestOperations.sendPost(
                        Constants.messagesUrl(orderId), gson.toJson(json), session.getToken());
                handler.post(() -> handleMessageSent(result));
            } catch (IOException exception) {
                handler.post(() -> {
                    sendButton.setEnabled(true);
                    Toast.makeText(this, R.string.error_server_connection,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void handleMessageSent(RestOperations.HttpResult result) {
        if (!result.isSuccessful()) {
            sendButton.setEnabled(true);
            Toast.makeText(this, R.string.error_message_failed, Toast.LENGTH_LONG).show();
            return;
        }
        messageInput.setText("");
        Toast.makeText(this, R.string.message_sent, Toast.LENGTH_SHORT).show();
        loadOrder();
    }

    private void showError(int resource) {
        progress.setVisibility(View.GONE);
        Toast.makeText(this, resource, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
