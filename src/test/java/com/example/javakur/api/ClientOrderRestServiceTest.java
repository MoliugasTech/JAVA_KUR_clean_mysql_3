package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.example.javakur.api.ClientOrderDtos.CreateOrderRequest;
import static com.example.javakur.api.ClientOrderDtos.OrderItemRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientOrderRestServiceTest {
    @Mock private MobileUserRepository userRepository;
    @Mock private CatalogRestaurantRepository restaurantRepository;
    @Mock private CatalogMenuItemRepository menuItemRepository;
    @Mock private ClientOrderRepository orderRepository;
    @Mock private ClientOrderMessageRepository messageRepository;
    @InjectMocks private ClientOrderRestService service;

    @Test
    void createsAndPersistsOrderForAuthenticatedClient() {
        Client client = client(7L);
        Restaurant restaurant = restaurant(4L);
        MenuItem item = menuItem(8L, restaurant);
        when(userRepository.findActiveClientById(7L)).thenReturn(Optional.of(client));
        when(restaurantRepository.findById(4L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(8L)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> {
            FoodOrder order = invocation.getArgument(0);
            order.setId(12L);
            return order;
        });

        FoodOrder order = service.create(7L,
                new CreateOrderRequest(4L, List.of(new OrderItemRequest(8L, 2))));

        assertEquals(12L, order.getId());
        assertEquals(new BigDecimal("9.00"), order.getTotalPrice());
        verify(orderRepository).save(order);
    }

    @Test
    void returnsOnlyAuthenticatedClientsOrders() {
        Client client = client(7L);
        FoodOrder order = order(12L, client, OrderStatus.CREATED);
        when(userRepository.findActiveClientById(7L)).thenReturn(Optional.of(client));
        when(orderRepository.findByClientIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(order));

        var result = service.list(7L);

        assertEquals(1, result.size());
        assertEquals(12L, result.get(0).id());
        verify(orderRepository).findByClientIdOrderByCreatedAtDesc(7L);
    }

    @Test
    void completedOrderConversationIsReadOnly() {
        Client client = client(7L);
        FoodOrder order = order(12L, client, OrderStatus.COMPLETED);
        when(userRepository.findActiveClientById(7L)).thenReturn(Optional.of(client));
        when(orderRepository.findByIdAndClientId(12L, 7L)).thenReturn(Optional.of(order));

        assertFalse(service.get(7L, 12L).messagingAllowed());
        assertThrows(ValidationException.class,
                () -> service.sendMessage(7L, 12L, "Ar gavote?"));
    }

    @Test
    void trimsAndPersistsMessageForOpenOrder() {
        Client client = client(7L);
        FoodOrder order = order(12L, client, OrderStatus.DELIVERING);
        when(userRepository.findActiveClientById(7L)).thenReturn(Optional.of(client));
        when(orderRepository.findByIdAndClientId(12L, 7L)).thenReturn(Optional.of(order));
        when(messageRepository.save(any(OrderMessage.class))).thenAnswer(invocation -> {
            OrderMessage message = invocation.getArgument(0);
            message.setId(5L);
            return message;
        });

        var result = service.sendMessage(7L, 12L, "  Laukiu prie durų  ");

        assertEquals(5L, result.id());
        assertEquals("Laukiu prie durų", result.content());
    }

    private FoodOrder order(Long id, Client client, OrderStatus status) {
        Restaurant restaurant = restaurant(4L);
        FoodOrder order = new FoodOrder(client, restaurant);
        order.setId(id);
        order.setStatus(status);
        order.setSubtotal(new BigDecimal("9.00"));
        order.setTotalPrice(new BigDecimal("9.00"));
        order.addItem(new com.example.javakur.model.OrderItem(
                menuItem(8L, restaurant), 2, new BigDecimal("4.50")));
        return order;
    }

    private Client client(Long id) {
        Client client = new Client(
                "client", "hash", "C", "Client", "client@example.com", "Vilnius");
        client.setId(id);
        return client;
    }

    private Restaurant restaurant(Long id) {
        RestaurantOwner owner = new RestaurantOwner(
                "owner" + id, "hash", "R", "Owner", "owner" + id + "@example.com");
        owner.setId(id + 100);
        Restaurant restaurant = new Restaurant(
                "Skanu " + id, "", "Vilnius", RestaurantType.OTHER,
                LocalTime.of(9, 0), LocalTime.of(21, 0), owner);
        restaurant.setId(id);
        return restaurant;
    }

    private MenuItem menuItem(Long id, Restaurant restaurant) {
        MenuItem item = new MenuItem(
                "Sriuba", "Daržovių sriuba", new BigDecimal("4.50"), restaurant);
        item.setId(id);
        return item;
    }
}
