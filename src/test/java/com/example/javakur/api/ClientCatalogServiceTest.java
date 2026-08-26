package com.example.javakur.api;

import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientCatalogServiceTest {
    @Mock
    private CatalogRestaurantRepository restaurantRepository;

    @Mock
    private CatalogMenuItemRepository menuItemRepository;

    @InjectMocks
    private ClientCatalogService service;

    @Test
    void listsRestaurantsFromCatalogRepository() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(4L);
        when(restaurantRepository.findAllForCatalog()).thenReturn(List.of(restaurant));

        List<Restaurant> result = service.listRestaurants();

        assertEquals(List.of(restaurant), result);
    }

    @Test
    void listsOnlyAvailableItemsForExistingRestaurant() {
        MenuItem item = new MenuItem();
        item.setId(8L);
        when(restaurantRepository.existsById(4L)).thenReturn(true);
        when(menuItemRepository.findAvailableByRestaurantId(4L)).thenReturn(List.of(item));

        List<MenuItem> result = service.listAvailableMenuItems(4L);

        assertEquals(List.of(item), result);
    }

    @Test
    void reportsMissingRestaurantBeforeLoadingMenu() {
        when(restaurantRepository.existsById(999L)).thenReturn(false);

        ApiResourceNotFoundException exception = assertThrows(ApiResourceNotFoundException.class,
                () -> service.listAvailableMenuItems(999L));

        assertEquals("Restoranas nerastas", exception.getMessage());
    }
}
