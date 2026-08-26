package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.service.LoyaltyPointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.javakur.api.ClientOrderDtos.MessageResponse;
import static com.example.javakur.api.DriverOrderDtos.OrderDetailResponse;
import static com.example.javakur.api.DriverOrderDtos.OrderSummaryResponse;

@Service
@Transactional(readOnly = true)
public class DriverOrderRestService {
    private final MobileUserRepository userRepository;
    private final DriverOrderRepository orderRepository;
    private final ClientOrderMessageRepository messageRepository;
    private final LoyaltyPointService loyaltyPointService;

    public DriverOrderRestService(MobileUserRepository userRepository,
                                  DriverOrderRepository orderRepository,
                                  ClientOrderMessageRepository messageRepository,
                                  LoyaltyPointService loyaltyPointService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.messageRepository = messageRepository;
        this.loyaltyPointService = loyaltyPointService;
    }

    public List<OrderSummaryResponse> available(Long driverId) {
        activeDriver(driverId);
        return orderRepository
                .findByStatusAndDriverIsNullOrderByCreatedAtAsc(OrderStatus.READY_FOR_PICKUP)
                .stream().map(OrderSummaryResponse::from).toList();
    }

    public List<OrderSummaryResponse> mine(Long driverId) {
        activeDriver(driverId);
        return orderRepository.findByDriverIdOrderByCreatedAtDesc(driverId)
                .stream().map(OrderSummaryResponse::from).toList();
    }

    @Transactional
    public OrderDetailResponse accept(Long driverId, Long orderId) {
        Driver driver = activeDriver(driverId);
        FoodOrder order = orderRepository.findForUpdateById(orderId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Užsakymas nerastas"));
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP || order.getDriver() != null) {
            throw new ValidationException("Užsakymas jau paimtas arba dar neparuoštas pristatymui");
        }
        order.setDriver(driver);
        order.setStatus(OrderStatus.PICKED_UP);
        return OrderDetailResponse.from(orderRepository.save(order));
    }

    public OrderDetailResponse get(Long driverId, Long orderId) {
        activeDriver(driverId);
        return OrderDetailResponse.from(ownedOrder(driverId, orderId));
    }

    @Transactional
    public OrderDetailResponse changeStatus(Long driverId, Long orderId, OrderStatus newStatus) {
        activeDriver(driverId);
        FoodOrder order = ownedOrder(driverId, orderId);
        boolean allowed = order.getStatus() == OrderStatus.PICKED_UP
                && newStatus == OrderStatus.DELIVERING
                || order.getStatus() == OrderStatus.DELIVERING
                && newStatus == OrderStatus.COMPLETED;
        if (!allowed) {
            throw new ValidationException("Neleistinas vairuotojo būsenos pakeitimas: "
                    + order.getStatus() + " -> " + newStatus);
        }
        order.setStatus(newStatus);
        if (newStatus == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
            loyaltyPointService.awardFor(order);
        }
        return OrderDetailResponse.from(orderRepository.save(order));
    }

    public List<MessageResponse> messages(Long driverId, Long orderId) {
        activeDriver(driverId);
        ownedOrder(driverId, orderId);
        return messageRepository.findByOrderIdOrderBySentAtAscIdAsc(orderId).stream()
                .map(MessageResponse::from).toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long driverId, Long orderId, String content) {
        Driver driver = activeDriver(driverId);
        FoodOrder order = ownedOrder(driverId, orderId);
        if (order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ValidationException("Prie užbaigto užsakymo naujų žinučių siųsti negalima");
        }
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new ValidationException("Žinutė negali būti tuščia");
        }
        if (normalized.length() > 2000) {
            throw new ValidationException("Žinutė negali būti ilgesnė nei 2000 simbolių");
        }
        return MessageResponse.from(messageRepository.save(
                new OrderMessage(order, driver, normalized)));
    }

    private Driver activeDriver(Long driverId) {
        return userRepository.findActiveDriverById(driverId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Vairuotojas nerastas"));
    }

    private FoodOrder ownedOrder(Long driverId, Long orderId) {
        return orderRepository.findByIdAndDriverId(orderId, driverId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Užsakymas nerastas"));
    }
}
