package com.example.fooddeliverymobile;

import java.math.BigDecimal;
import java.util.List;

public class ClientOrderDetail {
    private long id;
    private String status;
    private String restaurantName;
    private String driverName;
    private String createdAt;
    private String completedAt;
    private BigDecimal subtotal;
    private BigDecimal priceMultiplier;
    private BigDecimal totalPrice;
    private boolean messagingAllowed;
    private List<OrderLineInfo> items;

    public long getId() { return id; }
    public String getStatus() { return status == null ? "" : status; }
    public String getRestaurantName() { return restaurantName == null ? "" : restaurantName; }
    public String getDriverName() { return driverName == null ? "" : driverName; }
    public String getCreatedAt() { return createdAt == null ? "" : createdAt.replace('T', ' '); }
    public BigDecimal getSubtotal() { return subtotal == null ? BigDecimal.ZERO : subtotal; }
    public BigDecimal getPriceMultiplier() { return priceMultiplier == null ? BigDecimal.ONE : priceMultiplier; }
    public BigDecimal getTotalPrice() { return totalPrice == null ? BigDecimal.ZERO : totalPrice; }
    public boolean isMessagingAllowed() { return messagingAllowed; }
    public List<OrderLineInfo> getItems() { return items == null ? List.of() : items; }
}
