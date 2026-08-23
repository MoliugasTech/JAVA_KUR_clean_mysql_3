package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderItem;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.repository.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class OrderService {
    private static final int MONEY_SCALE = 2;
    private static final Map<OrderStatus, EnumSet<OrderStatus>> ALLOWED_TRANSITIONS =
            createAllowedTransitions();

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public FoodOrder createOrder(Client client, Restaurant restaurant,
                                 Map<MenuItem, Integer> requestedItems,
                                 BigDecimal priceMultiplier) {
        validateParticipants(client, restaurant);
        validateMultiplier(priceMultiplier);

        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new ValidationException("Order must contain at least one item");
        }

        FoodOrder order = new FoodOrder(client, restaurant);
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<MenuItem, Integer> entry : new LinkedHashMap<>(requestedItems).entrySet()) {
            MenuItem menuItem = entry.getKey();
            Integer quantity = entry.getValue();
            validateOrderItem(menuItem, quantity, restaurant);

            BigDecimal priceAtPurchase = menuItem.getPrice().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            order.addItem(new OrderItem(menuItem, quantity, priceAtPurchase));
            subtotal = subtotal.add(priceAtPurchase.multiply(BigDecimal.valueOf(quantity)));
        }

        subtotal = subtotal.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal normalizedMultiplier = priceMultiplier.setScale(3, RoundingMode.HALF_UP);
        BigDecimal totalPrice = subtotal.multiply(normalizedMultiplier)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        order.setSubtotal(subtotal);
        order.setPriceMultiplier(normalizedMultiplier);
        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }

    public Optional<FoodOrder> findDetailedById(Long id) {
        if (id == null) throw new ValidationException("Order id is required");
        return orderRepository.findDetailedById(id);
    }

    public FoodOrder changeStatus(FoodOrder order, OrderStatus newStatus) {
        validatePersistedOrder(order);
        if (newStatus == null) throw new ValidationException("New order status is required");

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == newStatus) {
            throw new ValidationException("Order already has status " + newStatus);
        }
        if (newStatus == OrderStatus.PICKED_UP) {
            throw new ValidationException("Use assignDriver to pick up an order");
        }

        EnumSet<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(
                currentStatus, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new ValidationException(
                    "Invalid order status transition: " + currentStatus + " -> " + newStatus);
        }

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }
        return orderRepository.update(order);
    }

    public FoodOrder assignDriver(FoodOrder order, Driver driver) {
        validatePersistedOrder(order);
        if (driver == null || driver.getId() == null) {
            throw new ValidationException("Persisted driver is required");
        }
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new ValidationException("Driver can only pick up a READY_FOR_PICKUP order");
        }
        if (order.getDriver() != null) {
            throw new ValidationException("Order already has a driver");
        }

        order.setDriver(driver);
        order.setStatus(OrderStatus.PICKED_UP);
        return orderRepository.update(order);
    }

    private void validatePersistedOrder(FoodOrder order) {
        if (order == null || order.getId() == null) {
            throw new ValidationException("Persisted order is required");
        }
    }

    private void validateParticipants(Client client, Restaurant restaurant) {
        if (client == null || client.getId() == null) {
            throw new ValidationException("Persisted client is required");
        }
        if (restaurant == null || restaurant.getId() == null) {
            throw new ValidationException("Persisted restaurant is required");
        }
    }

    private void validateMultiplier(BigDecimal priceMultiplier) {
        if (priceMultiplier == null || priceMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price multiplier must be greater than zero");
        }
    }

    private void validateOrderItem(MenuItem menuItem, Integer quantity, Restaurant restaurant) {
        if (menuItem == null || menuItem.getId() == null) {
            throw new ValidationException("Persisted menu item is required");
        }
        if (!menuItem.isAvailable()) {
            throw new ValidationException("Unavailable menu item cannot be ordered");
        }
        if (quantity == null || quantity <= 0) {
            throw new ValidationException("Order item quantity must be greater than zero");
        }
        if (menuItem.getPrice() == null || menuItem.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Menu item price must be greater than zero");
        }
        if (menuItem.getRestaurant() == null || menuItem.getRestaurant().getId() == null
                || !restaurant.getId().equals(menuItem.getRestaurant().getId())) {
            throw new ValidationException("All order items must belong to the selected restaurant");
        }
    }

    private static Map<OrderStatus, EnumSet<OrderStatus>> createAllowedTransitions() {
        Map<OrderStatus, EnumSet<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(OrderStatus.CREATED,
                EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.CONFIRMED,
                EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PREPARING,
                EnumSet.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PICKED_UP,
                EnumSet.of(OrderStatus.DELIVERING));
        transitions.put(OrderStatus.DELIVERING,
                EnumSet.of(OrderStatus.COMPLETED));
        return transitions;
    }
}
