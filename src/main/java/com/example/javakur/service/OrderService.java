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
import java.util.List;
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
        return orderRepository.save(
                OrderFactory.create(client, restaurant, requestedItems, priceMultiplier));
    }

    public Optional<FoodOrder> findDetailedById(Long id) {
        if (id == null) throw new ValidationException("Užsakymo ID yra privalomas");
        return orderRepository.findDetailedById(id);
    }

    public List<FoodOrder> findByClientId(Long clientId) {
        if (clientId == null) throw new ValidationException("Kliento ID yra privalomas");
        return orderRepository.findByClientId(clientId);
    }

    public FoodOrder changeStatus(FoodOrder order, OrderStatus newStatus) {
        validatePersistedOrder(order);
        if (newStatus == null) throw new ValidationException("Nauja užsakymo būsena yra privaloma");

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == newStatus) {
            throw new ValidationException("Užsakymas jau turi būseną " + newStatus);
        }
        if (newStatus == OrderStatus.PICKED_UP) {
            throw new ValidationException("Užsakymui paimti naudokite vairuotojo priskyrimą");
        }

        EnumSet<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(
                currentStatus, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new ValidationException(
                    "Neleistinas užsakymo būsenos pakeitimas: " + currentStatus + " -> " + newStatus);
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
            throw new ValidationException("Išsaugotas vairuotojas yra privalomas");
        }
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new ValidationException("Vairuotojas gali paimti tik READY_FOR_PICKUP užsakymą");
        }
        if (order.getDriver() != null) {
            throw new ValidationException("Užsakymas jau turi priskirtą vairuotoją");
        }

        order.setDriver(driver);
        order.setStatus(OrderStatus.PICKED_UP);
        return orderRepository.update(order);
    }

    private void validatePersistedOrder(FoodOrder order) {
        if (order == null || order.getId() == null) {
            throw new ValidationException("Išsaugotas užsakymas yra privalomas");
        }
    }

    private void validateParticipants(Client client, Restaurant restaurant) {
        if (client == null || client.getId() == null) {
            throw new ValidationException("Išsaugotas klientas yra privalomas");
        }
        if (restaurant == null || restaurant.getId() == null) {
            throw new ValidationException("Išsaugotas restoranas yra privalomas");
        }
    }

    private void validateMultiplier(BigDecimal priceMultiplier) {
        if (priceMultiplier == null || priceMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Kainos daugiklis turi būti didesnis už nulį");
        }
    }

    private void validateOrderItem(MenuItem menuItem, Integer quantity, Restaurant restaurant) {
        if (menuItem == null || menuItem.getId() == null) {
            throw new ValidationException("Išsaugotas meniu elementas yra privalomas");
        }
        if (!menuItem.isAvailable()) {
            throw new ValidationException("Neprieinamo patiekalo užsakyti negalima");
        }
        if (quantity == null || quantity <= 0) {
            throw new ValidationException("Patiekalo kiekis turi būti didesnis už nulį");
        }
        if (menuItem.getPrice() == null || menuItem.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Patiekalo kaina turi būti didesnė už nulį");
        }
        if (menuItem.getRestaurant() == null || menuItem.getRestaurant().getId() == null
                || !restaurant.getId().equals(menuItem.getRestaurant().getId())) {
            throw new ValidationException("Visi patiekalai turi priklausyti pasirinktam restoranui");
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
