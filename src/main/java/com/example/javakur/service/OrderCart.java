package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrderCart {
    private final Restaurant restaurant;
    private final Map<MenuItem, Integer> items = new LinkedHashMap<>();

    public OrderCart(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            throw new ValidationException("Krepšeliui reikalingas išsaugotas restoranas");
        }
        this.restaurant = restaurant;
    }

    public void setQuantity(MenuItem item, int quantity) {
        validateItem(item);
        if (quantity <= 0) throw new ValidationException("Kiekis turi būti didesnis už nulį");
        items.put(item, quantity);
    }

    public void remove(MenuItem item) {
        if (item == null || !items.containsKey(item)) {
            throw new ValidationException("Patiekalo krepšelyje nėra");
        }
        items.remove(item);
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public Map<MenuItem, Integer> getItems() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(items));
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public BigDecimal subtotal() {
        return items.entrySet().stream()
                .map(entry -> entry.getKey().getPrice()
                        .multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateItem(MenuItem item) {
        if (item == null || item.getId() == null || item.getRestaurant() == null
                || item.getRestaurant().getId() == null) {
            throw new ValidationException("Išsaugotas meniu elementas yra privalomas");
        }
        if (!restaurant.getId().equals(item.getRestaurant().getId())) {
            throw new ValidationException("Krepšelyje gali būti tik vieno restorano patiekalai");
        }
        if (!item.isAvailable()) throw new ValidationException("Neprieinamo patiekalo pridėti negalima");
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Patiekalo kaina turi būti didesnė už nulį");
        }
    }
}
