package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.User;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

public class OwnerConsoleService {
    private static final EnumSet<OrderStatus> OWNER_STATUSES = EnumSet.of(
            OrderStatus.CONFIRMED, OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED);

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final OrderService orderService;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public OwnerConsoleService(UserService userService, RestaurantService restaurantService,
                               MenuService menuService, OrderService orderService,
                               RestaurantRepository restaurantRepository,
                               OrderRepository orderRepository) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.orderService = orderService;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
    }

    public RestaurantOwner register(String username, String password, String name,
                                    String surname, String email) {
        return userService.createRestaurantOwner(username, password, name, surname, email);
    }

    public Restaurant createRestaurant(Long ownerId, String name, String description, String address,
                                       RestaurantType type, LocalTime openingTime, LocalTime closingTime) {
        return restaurantService.createRestaurant(name, description, address, type,
                openingTime, closingTime, requireOwner(ownerId));
    }

    public List<Restaurant> listRestaurants(Long ownerId) {
        return restaurantService.findByOwner(requireOwner(ownerId));
    }

    public MenuItem addMenuItem(Long ownerId, Long restaurantId, String name,
                                String description, BigDecimal price) {
        Restaurant restaurant = requireOwnedRestaurant(ownerId, restaurantId);
        return menuService.createMenuItem(name, description, price, restaurant);
    }

    public List<MenuItem> listMenu(Long ownerId, Long restaurantId) {
        return menuService.findByRestaurant(requireOwnedRestaurant(ownerId, restaurantId));
    }

    public MenuItem updateMenuItem(Long ownerId, Long restaurantId, Long itemId,
                                   BigDecimal newPrice, Boolean available) {
        Restaurant restaurant = requireOwnedRestaurant(ownerId, restaurantId);
        MenuItem item = menuService.findById(itemId)
                .orElseThrow(() -> new ValidationException("Meniu elementas nerastas"));
        if (item.getRestaurant() == null || item.getRestaurant().getId() == null
                || !restaurant.getId().equals(item.getRestaurant().getId())) {
            throw new ValidationException("Meniu elementas nepriklauso šiam restoranui");
        }
        if (newPrice != null) item = menuService.updatePrice(item, newPrice);
        if (available != null) item = menuService.updateAvailability(item, available);
        return item;
    }

    public List<FoodOrder> listOrders(Long ownerId, Long restaurantId) {
        requireOwnedRestaurant(ownerId, restaurantId);
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public FoodOrder changeOrderStatus(Long ownerId, Long orderId, OrderStatus newStatus) {
        if (newStatus == null || !OWNER_STATUSES.contains(newStatus)) {
            throw new ValidationException("Restorano savininkas negali nustatyti būsenos " + newStatus);
        }
        FoodOrder order = orderRepository.findOwnedByRestaurantOwner(orderId, ownerId)
                .orElseThrow(() -> new ValidationException("Užsakymas nepriklauso šiam savininkui"));
        return orderService.changeStatus(order, newStatus);
    }

    private RestaurantOwner requireOwner(Long ownerId) {
        User user = userService.findById(ownerId)
                .orElseThrow(() -> new ValidationException("Restorano savininkas nerastas"));
        if (!(user instanceof RestaurantOwner owner)) {
            throw new ValidationException("Naudotojas nėra restorano savininkas");
        }
        return owner;
    }

    private Restaurant requireOwnedRestaurant(Long ownerId, Long restaurantId) {
        if (ownerId == null || restaurantId == null) {
            throw new ValidationException("Savininko ir restorano ID yra privalomi");
        }
        return restaurantRepository.findOwnedById(restaurantId, ownerId)
                .orElseThrow(() -> new ValidationException("Restoranas nepriklauso šiam savininkui"));
    }
}
