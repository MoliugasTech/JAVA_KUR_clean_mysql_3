package com.example.fooddeliverymobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ClientOrderRequest {
    private final long restaurantId;
    private final List<OrderLine> items;

    private ClientOrderRequest(long restaurantId, List<OrderLine> items) {
        this.restaurantId = restaurantId;
        this.items = items;
    }

    public static ClientOrderRequest from(long restaurantId, Map<Long, Integer> quantities) {
        List<OrderLine> lines = new ArrayList<>();
        quantities.forEach((menuItemId, quantity) -> {
            if (quantity != null && quantity > 0) {
                lines.add(new OrderLine(menuItemId, quantity));
            }
        });
        return new ClientOrderRequest(restaurantId, lines);
    }

    public long getRestaurantId() {
        return restaurantId;
    }

    public List<OrderLine> getItems() {
        return List.copyOf(items);
    }

    public static final class OrderLine {
        private final long menuItemId;
        private final int quantity;

        private OrderLine(long menuItemId, int quantity) {
            this.menuItemId = menuItemId;
            this.quantity = quantity;
        }

        public long getMenuItemId() {
            return menuItemId;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
