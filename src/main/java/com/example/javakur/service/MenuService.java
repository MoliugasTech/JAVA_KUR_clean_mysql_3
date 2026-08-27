package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.repository.MenuItemRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class MenuService {
    private final MenuItemRepository menuItemRepository;

    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public MenuItem createMenuItem(String name, String description, BigDecimal price, Restaurant restaurant) {
        if (name == null || name.isBlank()) throw new ValidationException("Patiekalo pavadinimas negali būti tuščias");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Kaina turi būti didesnė už nulį");
        if (restaurant == null || restaurant.getId() == null) throw new ValidationException("Išsaugotas restoranas yra privalomas");
        return menuItemRepository.save(new MenuItem(name.trim(), description, price, restaurant));
    }

    public MenuItem updateMenuItem(MenuItem item, String name, String description,
                                   BigDecimal price, boolean available) {
        if (item == null || item.getId() == null) {
            throw new ValidationException("Išsaugotas meniu elementas yra privalomas");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Patiekalo pavadinimas negali būti tuščias");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Kaina turi būti didesnė už nulį");
        }
        item.setName(name.trim());
        item.setDescription(description == null ? "" : description.trim());
        item.setPrice(price);
        item.setAvailable(available);
        return menuItemRepository.update(item);
    }

    public MenuItem updatePrice(MenuItem item, BigDecimal newPrice) {
        if (item == null || item.getId() == null) throw new ValidationException("Išsaugotas meniu elementas yra privalomas");
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Kaina turi būti didesnė už nulį");
        item.setPrice(newPrice);
        return menuItemRepository.update(item);
    }

    public MenuItem updateAvailability(MenuItem item, boolean available) {
        if (item == null || item.getId() == null) {
            throw new ValidationException("Išsaugotas meniu elementas yra privalomas");
        }
        item.setAvailable(available);
        return menuItemRepository.update(item);
    }

    public List<MenuItem> findByRestaurant(Restaurant restaurant) {
        return menuItemRepository.findByRestaurant(restaurant);
    }

    public List<MenuItem> findAvailableByRestaurant(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) {
            throw new ValidationException("Išsaugotas restoranas yra privalomas");
        }
        return menuItemRepository.findAvailableByRestaurant(restaurant);
    }

    public Optional<MenuItem> findById(Long id) {
        if (id == null) throw new ValidationException("Meniu elemento ID yra privalomas");
        return menuItemRepository.findById(id);
    }

    public void deleteMenuItem(Long id) {
        if (id == null) throw new ValidationException("Meniu elemento ID yra privalomas");
        menuItemRepository.deleteById(id);
    }
}
