package com.example.fooddeliverymobile;

import java.math.BigDecimal;

public final class CreatedOrderResponse {
    private long id;
    private String status;
    private BigDecimal totalPrice;

    public long getId() {
        return id;
    }

    public String getStatus() {
        return status == null ? "" : status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice == null ? BigDecimal.ZERO : totalPrice;
    }
}
