package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ClientConsoleService {
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final OrderService orderService;

    public ClientConsoleService(UserService userService, RestaurantService restaurantService,
                                MenuService menuService, OrderService orderService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.orderService = orderService;
    }

    public Client register(String username, String password, String name, String surname,
                           String email, String address) {
        return userService.createClient(username, password, name, surname, email, address);
    }

    public List<Restaurant> listRestaurants() {
        return restaurantService.findAll();
    }

    public List<MenuItem> listAvailableMenu(Long restaurantId) {
        Restaurant restaurant = requireRestaurant(restaurantId);
        return menuService.findAvailableByRestaurant(restaurant);
    }

    public FoodOrder createSingleItemOrder(Long clientId, Long restaurantId,
                                           Long menuItemId, int quantity) {
        Client client = requireClient(clientId);
        Restaurant restaurant = requireRestaurant(restaurantId);
        MenuItem menuItem = menuService.findById(menuItemId)
                .orElseThrow(() -> new ValidationException("Meniu elementas nerastas"));
        return orderService.createOrder(
                client, restaurant, Map.of(menuItem, quantity), BigDecimal.ONE);
    }

    public FoodOrder checkout(Long clientId, OrderCart cart) {
        if (cart == null || cart.isEmpty()) {
            throw new ValidationException("Krepšelyje turi būti bent vienas patiekalas");
        }
        Client client = requireClient(clientId);
        Restaurant restaurant = requireRestaurant(cart.getRestaurant().getId());
        return orderService.createOrder(client, restaurant, cart.getItems(), BigDecimal.ONE);
    }

    public List<FoodOrder> listClientOrders(Long clientId) {
        requireClient(clientId);
        return orderService.findByClientId(clientId);
    }

    private Client requireClient(Long clientId) {
        User user = userService.findById(clientId)
                .orElseThrow(() -> new ValidationException("Klientas nerastas"));
        if (!(user instanceof Client client)) {
            throw new ValidationException("Naudotojas nėra klientas");
        }
        return client;
    }

    private Restaurant requireRestaurant(Long restaurantId) {
        return restaurantService.findById(restaurantId)
                .orElseThrow(() -> new ValidationException("Restoranas nerastas"));
    }
}
