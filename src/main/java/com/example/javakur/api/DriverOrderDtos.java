package com.example.javakur.api;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class DriverOrderDtos {
    private DriverOrderDtos() {
    }

    public record OrderSummaryResponse(Long id, OrderStatus status,
                                       Long restaurantId, String restaurantName,
                                       String restaurantAddress,
                                       Long clientId, String clientName,
                                       String deliveryAddress,
                                       LocalDateTime createdAt, BigDecimal totalPrice) {
        public static OrderSummaryResponse from(FoodOrder order) {
            return new OrderSummaryResponse(order.getId(), order.getStatus(),
                    order.getRestaurant().getId(), order.getRestaurant().getName(),
                    order.getRestaurant().getAddress(), order.getClient().getId(),
                    order.getClient().getName() + " " + order.getClient().getSurname(),
                    order.getClient().getDeliveryAddress(), order.getCreatedAt(),
                    order.getTotalPrice());
        }
    }

    public record OrderDetailResponse(Long id, OrderStatus status,
                                      Long restaurantId, String restaurantName,
                                      String restaurantAddress,
                                      Long clientId, String clientName,
                                      String deliveryAddress,
                                      LocalDateTime createdAt, LocalDateTime completedAt,
                                      BigDecimal totalPrice, OrderStatus nextStatus,
                                      boolean messagingAllowed,
                                      List<ClientOrderDtos.OrderItemResponse> items) {
        public static OrderDetailResponse from(FoodOrder order) {
            OrderStatus nextStatus = switch (order.getStatus()) {
                case PICKED_UP -> OrderStatus.DELIVERING;
                case DELIVERING -> OrderStatus.COMPLETED;
                default -> null;
            };
            boolean messagingAllowed = order.getStatus() != OrderStatus.COMPLETED
                    && order.getStatus() != OrderStatus.CANCELLED;
            return new OrderDetailResponse(order.getId(), order.getStatus(),
                    order.getRestaurant().getId(), order.getRestaurant().getName(),
                    order.getRestaurant().getAddress(), order.getClient().getId(),
                    order.getClient().getName() + " " + order.getClient().getSurname(),
                    order.getClient().getDeliveryAddress(), order.getCreatedAt(),
                    order.getCompletedAt(), order.getTotalPrice(), nextStatus,
                    messagingAllowed, order.getItems().stream()
                    .map(ClientOrderDtos.OrderItemResponse::from).toList());
        }
    }

    public record StatusUpdateRequest(
            @NotNull(message = "Nauja užsakymo būsena yra privaloma") OrderStatus status) {
    }
}
