package com.example.javakur.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.javakur.api.ClientCatalogDtos.MenuItemResponse;
import static com.example.javakur.api.ClientCatalogDtos.RestaurantResponse;

@RestController
@RequestMapping("/api/catalog")
public class ClientCatalogController {
    private final ClientCatalogService service;

    public ClientCatalogController(ClientCatalogService service) {
        this.service = service;
    }

    @GetMapping("/restaurants")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public List<RestaurantResponse> listRestaurants() {
        return service.listRestaurants().stream().map(RestaurantResponse::from).toList();
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public List<MenuItemResponse> listMenuItems(@PathVariable("restaurantId") Long restaurantId) {
        return service.listAvailableMenuItems(restaurantId).stream().map(MenuItemResponse::from).toList();
    }
}
