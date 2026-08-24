package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.service.OrderCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCartTest {
    private Restaurant restaurant;
    private MenuItem pizza;
    private MenuItem drink;

    @BeforeEach
    void setUp() {
        restaurant = restaurant(1L, "Test restaurant");
        pizza = item(10L, "Pizza", "9.50", restaurant);
        drink = item(11L, "Drink", "2.25", restaurant);
    }

    @Test
    void storesMultipleItemsAndCalculatesSubtotal() {
        OrderCart cart = new OrderCart(restaurant);

        cart.setQuantity(pizza, 2);
        cart.setQuantity(drink, 3);

        assertEquals(2, cart.getItems().size());
        assertEquals(2, cart.getItems().get(pizza));
        assertEquals(3, cart.getItems().get(drink));
        assertEquals(new BigDecimal("25.75"), cart.subtotal());
    }

    @Test
    void updatesQuantityInsteadOfAddingDuplicateItem() {
        OrderCart cart = new OrderCart(restaurant);
        cart.setQuantity(pizza, 1);

        cart.setQuantity(pizza, 4);

        assertEquals(1, cart.getItems().size());
        assertEquals(4, cart.getItems().get(pizza));
        assertEquals(new BigDecimal("38.00"), cart.subtotal());
    }

    @Test
    void removesItemFromCart() {
        OrderCart cart = new OrderCart(restaurant);
        cart.setQuantity(pizza, 1);

        cart.remove(pizza);

        assertTrue(cart.isEmpty());
        assertEquals(new BigDecimal("0.00"), cart.subtotal());
    }

    @Test
    void rejectsInvalidQuantityAndUnavailableItem() {
        OrderCart cart = new OrderCart(restaurant);
        drink.setAvailable(false);

        assertThrows(ValidationException.class, () -> cart.setQuantity(pizza, 0));
        assertThrows(ValidationException.class, () -> cart.setQuantity(drink, 1));
        assertTrue(cart.isEmpty());
    }

    @Test
    void rejectsItemFromAnotherRestaurant() {
        OrderCart cart = new OrderCart(restaurant);
        MenuItem foreignItem = item(12L, "Foreign dish", "5.00",
                restaurant(2L, "Other restaurant"));

        assertThrows(ValidationException.class, () -> cart.setQuantity(foreignItem, 1));
        assertFalse(cart.getItems().containsKey(foreignItem));
    }

    private static Restaurant restaurant(Long id, String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName(name);
        return restaurant;
    }

    private static MenuItem item(Long id, String name, String price, Restaurant restaurant) {
        MenuItem item = new MenuItem(name, "Test", new BigDecimal(price), restaurant);
        item.setId(id);
        return item;
    }
}
