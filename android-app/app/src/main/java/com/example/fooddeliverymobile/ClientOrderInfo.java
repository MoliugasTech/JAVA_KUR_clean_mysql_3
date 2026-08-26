package com.example.fooddeliverymobile;

import java.math.BigDecimal;

public class ClientOrderInfo {
    private long id;
    private String status;
    private long restaurantId;
    private String restaurantName;
    private Long driverId;
    private String driverName;
    private String createdAt;
    private BigDecimal totalPrice;

    public long getId() { return id; }
    public String getStatus() { return status == null ? "" : status; }
    public String getRestaurantName() { return restaurantName == null ? "" : restaurantName; }
    public String getCreatedAt() { return createdAt == null ? "" : createdAt.replace('T', ' '); }
    public BigDecimal getTotalPrice() { return totalPrice == null ? BigDecimal.ZERO : totalPrice; }

    @Override
    public String toString() {
        return "#" + id + " · " + getRestaurantName()
                + "\n" + StatusLabels.toLithuanian(getStatus()) + " · " + getCreatedAt()
                + "\n" + getTotalPrice().toPlainString() + " €";
    }
}
