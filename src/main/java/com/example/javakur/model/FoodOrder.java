package com.example.javakur.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "food_orders")
@Getter
@Setter
@NoArgsConstructor
public class FoodOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "driver_assignment_allowed", nullable = false)
    private boolean driverAssignmentAllowed = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "price_multiplier", nullable = false, precision = 6, scale = 3)
    private BigDecimal priceMultiplier = BigDecimal.ONE;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public FoodOrder(Client client, Restaurant restaurant) {
        this.client = client;
        this.restaurant = restaurant;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void ensureCreatedAt() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        if (item == null) throw new IllegalArgumentException("Order item cannot be null");
        if (item.getMenuItem() == null || item.getMenuItem().getRestaurant() == null) {
            throw new IllegalArgumentException("Menu item must belong to a restaurant");
        }
        if (restaurant == null || restaurant.getId() == null
                || !restaurant.getId().equals(item.getMenuItem().getRestaurant().getId())) {
            throw new IllegalArgumentException("All order items must belong to the order restaurant");
        }
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
