package com.example.javakur.api;

import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantType;

import java.math.BigDecimal;
import java.time.LocalTime;

public final class ClientCatalogDtos {
    private ClientCatalogDtos() {
    }

    public record RestaurantResponse(Long id, String name, String description, String address,
                                     RestaurantType type, LocalTime openingTime, LocalTime closingTime) {
        public static RestaurantResponse from(Restaurant restaurant) {
            return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getDescription(),
                    restaurant.getAddress(), restaurant.getType(), restaurant.getOpeningTime(),
                    restaurant.getClosingTime());
        }
    }

    public record MenuItemResponse(Long id, String name, String description, BigDecimal price,
                                   boolean available) {
        public static MenuItemResponse from(MenuItem menuItem) {
            return new MenuItemResponse(menuItem.getId(), menuItem.getName(), menuItem.getDescription(),
                    menuItem.getPrice(), menuItem.isAvailable());
        }
    }
}
