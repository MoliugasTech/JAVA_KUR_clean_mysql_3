package com.example.fooddeliverymobile;

import java.math.BigDecimal;

public class MenuItemInfo {
    private long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available;

    public long getId() { return id; }
    public String getName() { return name == null ? "" : name; }
    public String getDescription() { return description == null ? "" : description; }
    public BigDecimal getPrice() { return price == null ? BigDecimal.ZERO : price; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() {
        String details = getDescription().isBlank() ? "Aprašymo nėra" : getDescription();
        return getName() + "\n" + details + "\n" + getPrice().toPlainString() + " €";
    }
}
