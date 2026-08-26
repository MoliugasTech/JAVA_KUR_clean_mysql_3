package com.example.fooddeliverymobile;

import java.math.BigDecimal;

public class DriverOrderInfo {
    private long id;
    private String status;
    private long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private long clientId;
    private String clientName;
    private String deliveryAddress;
    private String createdAt;
    private BigDecimal totalPrice;

    public long getId() { return id; }
    public String getStatus() { return status == null ? "" : status; }
    public String getRestaurantName() { return restaurantName == null ? "" : restaurantName; }
    public String getRestaurantAddress() { return restaurantAddress == null ? "" : restaurantAddress; }
    public String getClientName() { return clientName == null ? "" : clientName; }
    public String getDeliveryAddress() { return deliveryAddress == null ? "" : deliveryAddress; }
    public String getCreatedAt() { return createdAt == null ? "" : createdAt.replace('T', ' '); }
    public BigDecimal getTotalPrice() { return totalPrice == null ? BigDecimal.ZERO : totalPrice; }

    @Override
    public String toString() {
        return "#" + id + " · " + getRestaurantName()
                + "\n" + getRestaurantAddress() + " → " + getDeliveryAddress()
                + "\n" + StatusLabels.toLithuanian(getStatus())
                + " · " + getTotalPrice().toPlainString() + " €";
    }
}
