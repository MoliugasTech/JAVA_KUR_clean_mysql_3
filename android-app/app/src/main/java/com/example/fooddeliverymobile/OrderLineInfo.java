package com.example.fooddeliverymobile;

import java.math.BigDecimal;

public class OrderLineInfo {
    private long menuItemId;
    private String name;
    private int quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal lineTotal;

    public String getName() { return name == null ? "" : name; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal == null ? BigDecimal.ZERO : lineTotal; }
}
