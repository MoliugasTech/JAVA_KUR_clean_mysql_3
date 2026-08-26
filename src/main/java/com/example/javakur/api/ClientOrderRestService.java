package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.service.OrderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.example.javakur.api.ClientOrderDtos.CreateOrderRequest;
import static com.example.javakur.api.ClientOrderDtos.OrderItemRequest;

@Service
@Transactional(readOnly = true)
public class ClientOrderRestService {
    private final MobileUserRepository userRepository;
    private final CatalogRestaurantRepository restaurantRepository;
    private final CatalogMenuItemRepository menuItemRepository;
    private final ClientOrderRepository orderRepository;

    public ClientOrderRestService(MobileUserRepository userRepository,
                                  CatalogRestaurantRepository restaurantRepository,
                                  CatalogMenuItemRepository menuItemRepository,
                                  ClientOrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public FoodOrder create(Long clientId, CreateOrderRequest request) {
        Client client = userRepository.findActiveClientById(clientId)
                .orElseThrow(() -> new ApiResourceNotFoundException("Klientas nerastas"));
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
                client, restaurant, requestedItems, BigDecimal.ONE);
        return orderRepository.save(order);
    }
}
