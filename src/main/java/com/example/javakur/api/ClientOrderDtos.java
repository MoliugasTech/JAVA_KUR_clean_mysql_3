package com.example.javakur.api;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderItem;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    public record OrderSummaryResponse(Long id, OrderStatus status,
                                       Long restaurantId, String restaurantName,
                                       Long driverId, String driverName,
                                       LocalDateTime createdAt, BigDecimal totalPrice) {
        public static OrderSummaryResponse from(FoodOrder order) {
            Long driverId = order.getDriver() == null ? null : order.getDriver().getId();
            String driverName = order.getDriver() == null ? null
                    : order.getDriver().getName() + " " + order.getDriver().getSurname();
            return new OrderSummaryResponse(order.getId(), order.getStatus(),
                    order.getRestaurant().getId(), order.getRestaurant().getName(),
                    driverId, driverName, order.getCreatedAt(), order.getTotalPrice());
        }
    }

    public record OrderItemResponse(Long menuItemId, String name, int quantity,
                                    BigDecimal priceAtPurchase, BigDecimal lineTotal) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getMenuItem().getId(), item.getMenuItem().getName(),
                    item.getQuantity(), item.getPriceAtPurchase(),
                    item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
    }

    public record OrderDetailResponse(Long id, OrderStatus status,
                                      Long restaurantId, String restaurantName,
                                      Long driverId, String driverName,
                                      LocalDateTime createdAt, LocalDateTime completedAt,
                                      BigDecimal subtotal, BigDecimal priceMultiplier,
                                      BigDecimal totalPrice, String cancellationReason,
                                      boolean messagingAllowed, List<OrderItemResponse> items) {
        public static OrderDetailResponse from(FoodOrder order) {
            Long driverId = order.getDriver() == null ? null : order.getDriver().getId();
            String driverName = order.getDriver() == null ? null
                    : order.getDriver().getName() + " " + order.getDriver().getSurname();
            boolean messagingAllowed = order.getStatus() != OrderStatus.COMPLETED
                    && order.getStatus() != OrderStatus.CANCELLED;
            return new OrderDetailResponse(order.getId(), order.getStatus(),
                    order.getRestaurant().getId(), order.getRestaurant().getName(),
                    driverId, driverName, order.getCreatedAt(), order.getCompletedAt(),
                    order.getSubtotal(), order.getPriceMultiplier(), order.getTotalPrice(),
                    order.getCancellationReason(), messagingAllowed,
                    order.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }

    public record SendMessageRequest(
            @NotBlank(message = "Žinutė negali būti tuščia")
            @Size(max = 2000, message = "Žinutė negali būti ilgesnė nei 2000 simbolių")
            String content) {
    }

    public record MessageResponse(Long id, Long senderId, String senderName,
                                  String senderRole, String content, LocalDateTime sentAt) {
        public static MessageResponse from(OrderMessage message) {
            return new MessageResponse(message.getId(), message.getSender().getId(),
                    message.getSender().getName() + " " + message.getSender().getSurname(),
                    message.getSender().getRole().name(), message.getContent(), message.getSentAt());
        }
    }
}
