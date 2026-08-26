package com.example.fooddeliverymobile;

import java.math.BigDecimal;
import java.util.List;

public class DriverOrderDetail {
    private long id;
    private String status;
    private String restaurantName;
    private String restaurantAddress;
    private String clientName;
    private String deliveryAddress;
    private String createdAt;
    private String completedAt;
    private BigDecimal totalPrice;
    private String nextStatus;
    private boolean messagingAllowed;
    private List<OrderLineInfo> items;

    public long getId() { return id; }
    public String getStatus() { return status == null ? "" : status; }
    public String getRestaurantName() { return restaurantName == null ? "" : restaurantName; }
    public String getRestaurantAddress() { return restaurantAddress == null ? "" : restaurantAddress; }
    public String getClientName() { return clientName == null ? "" : clientName; }
    public String getDeliveryAddress() { return deliveryAddress == null ? "" : deliveryAddress; }
    public String getCreatedAt() { return createdAt == null ? "" : createdAt.replace('T', ' '); }
    public BigDecimal getTotalPrice() { return totalPrice == null ? BigDecimal.ZERO : totalPrice; }
    public String getNextStatus() { return nextStatus; }
    public boolean isMessagingAllowed() { return messagingAllowed; }
    public List<OrderLineInfo> getItems() { return items == null ? List.of() : items; }
}
