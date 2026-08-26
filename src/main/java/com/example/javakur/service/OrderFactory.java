package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderItem;
import com.example.javakur.model.Restaurant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OrderFactory {
    private static final int MONEY_SCALE = 2;

    private OrderFactory() {
    }

    public static FoodOrder create(Client client, Restaurant restaurant,
                                   Map<MenuItem, Integer> requestedItems,
                                   BigDecimal priceMultiplier) {
        validateParticipants(client, restaurant);
        validateMultiplier(priceMultiplier);
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new ValidationException("Užsakyme turi būti bent vienas patiekalas");
        }

        FoodOrder order = new FoodOrder(client, restaurant);
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<MenuItem, Integer> entry : new LinkedHashMap<>(requestedItems).entrySet()) {
            MenuItem menuItem = entry.getKey();
            Integer quantity = entry.getValue();
            validateOrderItem(menuItem, quantity, restaurant);

            BigDecimal priceAtPurchase =
                    menuItem.getPrice().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            order.addItem(new OrderItem(menuItem, quantity, priceAtPurchase));
            subtotal = subtotal.add(priceAtPurchase.multiply(BigDecimal.valueOf(quantity)));
        }

        subtotal = subtotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal normalizedMultiplier = priceMultiplier.setScale(3, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal);
        order.setPriceMultiplier(normalizedMultiplier);
        order.setTotalPrice(subtotal.multiply(normalizedMultiplier)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return order;
    }

    private static void validateParticipants(Client client, Restaurant restaurant) {
        if (client == null || client.getId() == null) {
            throw new ValidationException("Išsaugotas klientas yra privalomas");
        }
        if (restaurant == null || restaurant.getId() == null) {
            throw new ValidationException("Išsaugotas restoranas yra privalomas");
        }
    }

    private static void validateMultiplier(BigDecimal multiplier) {
        if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Kainos daugiklis turi būti didesnis už nulį");
        }
    }

    private static void validateOrderItem(MenuItem item, Integer quantity,
                                          Restaurant restaurant) {
        if (item == null || item.getId() == null) {
            throw new ValidationException("Išsaugotas meniu elementas yra privalomas");
        }
        if (!item.isAvailable()) {
            throw new ValidationException("Neprieinamo patiekalo užsakyti negalima");
        }
        if (quantity == null || quantity <= 0) {
            throw new ValidationException("Patiekalo kiekis turi būti didesnis už nulį");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Patiekalo kaina turi būti didesnė už nulį");
        }
        if (item.getRestaurant() == null || item.getRestaurant().getId() == null
                || !restaurant.getId().equals(item.getRestaurant().getId())) {
            throw new ValidationException(
                    "Visi patiekalai turi priklausyti pasirinktam restoranui");
        }
    }
}
