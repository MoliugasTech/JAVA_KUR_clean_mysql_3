package com.example.javakur.api;

import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientCatalogService {
    private final CatalogRestaurantRepository restaurantRepository;
    private final CatalogMenuItemRepository menuItemRepository;

    public ClientCatalogService(CatalogRestaurantRepository restaurantRepository,
                                CatalogMenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public List<Restaurant> listRestaurants() {
        return restaurantRepository.findAllForCatalog();
    }

    public List<MenuItem> listAvailableMenuItems(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ApiResourceNotFoundException("Restoranas nerastas");
        }
        return menuItemRepository.findAvailableByRestaurantId(restaurantId);
    }
}
