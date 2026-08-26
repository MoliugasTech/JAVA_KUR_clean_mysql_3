package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoyaltyPointService {

    public int awardFor(FoodOrder order) {
        if (order == null || order.getClient() == null) {
            throw new ValidationException("Užsakymas ir klientas yra privalomi bonuso taškams skirti");
        }
        int points = calculatePoints(order.getTotalPrice());
        Client client = order.getClient();
        client.addBonusPoints(points);
        return points;
    }

    public int calculatePoints(BigDecimal totalPrice) {
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return totalPrice.setScale(0, RoundingMode.DOWN).intValueExact();
    }
}
