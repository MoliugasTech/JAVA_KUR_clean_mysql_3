package com.example.fooddeliverymobile;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ClientOrderRequestTest {
    @Test
    public void createsRequestOnlyFromPositiveQuantities() {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        quantities.put(11L, 2);
        quantities.put(12L, 0);

        ClientOrderRequest request = ClientOrderRequest.from(8L, quantities);

        assertEquals(8L, request.getRestaurantId());
        assertEquals(1, request.getItems().size());
        assertEquals(11L, request.getItems().get(0).getMenuItemId());
        assertEquals(2, request.getItems().get(0).getQuantity());
    }
}
