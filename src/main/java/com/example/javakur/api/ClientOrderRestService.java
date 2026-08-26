package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.service.OrderFactory;
import com.example.javakur.service.TimeBasedPricingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.javakur.api.ClientOrderDtos.CreateOrderRequest;
import static com.example.javakur.api.ClientOrderDtos.MessageResponse;
import static com.example.javakur.api.ClientOrderDtos.OrderDetailResponse;
import static com.example.javakur.api.ClientOrderDtos.OrderItemRequest;
import static com.example.javakur.api.ClientOrderDtos.OrderSummaryResponse;

@Service
@Transactional(readOnly = true)
public class ClientOrderRestService {
    private final MobileUserRepository userRepository;
    private final CatalogRestaurantRepository restaurantRepository;
    private final CatalogMenuItemRepository menuItemRepository;
    private final ClientOrderRepository orderRepository;
    private final ClientOrderMessageRepository messageRepository;
    private final TimeBasedPricingService pricingService;

    public ClientOrderRestService(MobileUserRepository userRepository,
                                  CatalogRestaurantRepository restaurantRepository,
                                  CatalogMenuItemRepository menuItemRepository,
                                  ClientOrderRepository orderRepository,
                                  ClientOrderMessageRepository messageRepository,
                                  TimeBasedPricingService pricingService) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.messageRepository = messageRepository;
        this.pricingService = pricingService;
    }

    @Transactional
    public FoodOrder create(Long clientId, CreateOrderRequest request) {
        Client client = activeClient(clientId);
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new ApiResourceNotFoundException("Restoranas nerastas"));

        Map<MenuItem, Integer> requestedItems = new LinkedHashMap<>();
        for (OrderItemRequest requested : request.items()) {
            MenuItem item = menuItemRepository.findById(requested.menuItemId())
                    .orElseThrow(() -> new ApiResourceNotFoundException("Patiekalas nerastas"));
            if (requestedItems.keySet().stream()
                    .anyMatch(existing -> existing.getId().equals(item.getId()))) {
                throw new ValidationException("Tas pats patiekalas krepšelyje pakartotas");
            }
            requestedItems.put(item, requested.quantity());
        }

        FoodOrder order = OrderFactory.create(
                client, restaurant, requestedItems, pricingService.currentMultiplier());
        return orderRepository.save(order);
    }

    public List<OrderSummaryResponse> list(Long clientId) {
        activeClient(clientId);
        return orderRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(OrderSummaryResponse::from)
                .toList();
    }

    public OrderDetailResponse get(Long clientId, Long orderId) {
        activeClient(clientId);
        return OrderDetailResponse.from(ownedOrder(clientId, orderId));
    }

    public List<MessageResponse> messages(Long clientId, Long orderId) {
        activeClient(clientId);
        ownedOrder(clientId, orderId);
        return messageRepository.findByOrderIdOrderBySentAtAscIdAsc(orderId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional
    public MessageResponse sendMessage(Long clientId, Long orderId, String content) {
        Client client = activeClient(clientId);
        FoodOrder order = ownedOrder(clientId, orderId);
        if (order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ValidationException("Prie užbaigto užsakymo naujų žinučių siųsti negalima");
        }
        String normalized = content == null ? "" : content.trim();
        if (normalized.isEmpty()) {
            throw new ValidationException("Žinutė negali būti tuščia");
        }
        OrderMessage message = new OrderMessage(order, client, normalized);
        return MessageResponse.from(messageRepository.save(message));
    }

    private Client activeClient(Long clientId) {
        return userRepository.findActiveClientById(clientId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Klientas nerastas"));
    }

    private FoodOrder ownedOrder(Long clientId, Long orderId) {
        return orderRepository.findByIdAndClientId(orderId, clientId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Užsakymas nerastas"));
    }
}
