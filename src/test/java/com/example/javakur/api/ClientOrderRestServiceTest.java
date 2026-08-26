package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientOrderRestServiceTest {
    @Mock
    private MobileUserRepository userRepository;

    @Mock
    private CatalogRestaurantRepository restaurantRepository;

    @Mock
    private CatalogMenuItemRepository menuItemRepository;

    @Mock
    private ClientOrderRepository orderRepository;

    @InjectMocks
    private ClientOrderRestService service;

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
        assertEquals(new BigDecimal("9.00"), order.getSubtotal());
        assertEquals(new BigDecimal("9.00"), order.getTotalPrice());
        assertEquals(1, order.getItems().size());
        assertEquals(2, order.getItems().get(0).getQuantity());
        verify(orderRepository).save(order);
    }

    @Test
    void rejectsItemFromAnotherRestaurant() {
        Client client = client(7L);
        Restaurant selectedRestaurant = restaurant(4L);
        Restaurant anotherRestaurant = restaurant(5L);
        MenuItem foreignItem = menuItem(8L, anotherRestaurant);

        when(userRepository.findActiveClientById(7L)).thenReturn(Optional.of(client));
        when(restaurantRepository.findById(4L)).thenReturn(Optional.of(selectedRestaurant));
        when(menuItemRepository.findById(8L)).thenReturn(Optional.of(foreignItem));

        assertThrows(ValidationException.class, () -> service.create(7L,
                new CreateOrderRequest(4L, List.of(new OrderItemRequest(8L, 1)))));
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
