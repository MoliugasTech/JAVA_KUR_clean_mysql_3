package com.example.javakur.api;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ClientOrderDtos {
    private ClientOrderDtos() {
    }

    public record CreateOrderRequest(
            @NotNull(message = "Restorano ID yra privalomas") Long restaurantId,
            @NotEmpty(message = "Krepšelyje turi būti bent vienas patiekalas")
            List<@Valid OrderItemRequest> items) {
    }

    public record OrderItemRequest(
            @NotNull(message = "Patiekalo ID yra privalomas") Long menuItemId,
            @Min(value = 1, message = "Patiekalo kiekis turi būti didesnis už nulį")
            int quantity) {
    }

    public record CreatedOrderResponse(Long id, OrderStatus status, Long restaurantId,
                                       BigDecimal subtotal, BigDecimal priceMultiplier,
                                       BigDecimal totalPrice, LocalDateTime createdAt) {
        public static CreatedOrderResponse from(FoodOrder order) {
            return new CreatedOrderResponse(order.getId(), order.getStatus(),
                    order.getRestaurant().getId(), order.getSubtotal(),
                    order.getPriceMultiplier(), order.getTotalPrice(), order.getCreatedAt());
        }
    }
}
