package com.example.javakur.service;

import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoyaltyPointServiceTest {
    private final LoyaltyPointService service = new LoyaltyPointService();

    @Test
    void awardsOnePointForEveryCompletedEuro() {
        Client client = new Client(
                "client", "hash", "C", "Client", "client@example.com", "Vilnius");
        client.setBonusPoints(5);
        FoodOrder order = new FoodOrder();
        order.setClient(client);
        order.setTotalPrice(new BigDecimal("20.99"));

        int awarded = service.awardFor(order);

        assertEquals(20, awarded);
        assertEquals(25, client.getBonusPoints());
    }

    @Test
    void doesNotAwardPointsForZeroPrice() {
        assertEquals(0, service.calculatePoints(BigDecimal.ZERO));
    }
}
