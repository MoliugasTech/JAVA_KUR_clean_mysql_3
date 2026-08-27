package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.repository.RestaurantRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public Restaurant createRestaurant(String name, String description, String address, RestaurantType type,
                                       LocalTime openingTime, LocalTime closingTime, RestaurantOwner owner) {
        if (name == null || name.isBlank()) throw new ValidationException("Restorano pavadinimas negali būti tuščias");
        if (address == null || address.isBlank()) throw new ValidationException("Restorano adresas negali būti tuščias");
        if (type == null) throw new ValidationException("Restorano tipas yra privalomas");
        if (openingTime == null || closingTime == null) throw new ValidationException("Darbo laikas yra privalomas");
        if (owner == null || owner.getId() == null) throw new ValidationException("Išsaugotas restorano savininkas yra privalomas");

        return restaurantRepository.save(new Restaurant(name.trim(), description, address.trim(), type,
                openingTime, closingTime, owner));
    }

    public Restaurant updateRestaurant(Restaurant restaurant, String name, String description,
                                       String address, RestaurantType type,
                                       LocalTime openingTime, LocalTime closingTime) {
        if (restaurant == null || restaurant.getId() == null) {
            throw new ValidationException("Išsaugotas restoranas yra privalomas");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Restorano pavadinimas negali būti tuščias");
        }
        if (address == null || address.isBlank()) {
            throw new ValidationException("Restorano adresas negali būti tuščias");
        }
        if (type == null || openingTime == null || closingTime == null) {
            throw new ValidationException("Restorano tipas ir darbo laikas yra privalomi");
        }
        restaurant.setName(name.trim());
        restaurant.setDescription(description == null ? "" : description.trim());
        restaurant.setAddress(address.trim());
        restaurant.setType(type);
        restaurant.setOpeningTime(openingTime);
        restaurant.setClosingTime(closingTime);
        return restaurantRepository.update(restaurant);
    }

    public List<Restaurant> findByOwner(RestaurantOwner owner) {
        return restaurantRepository.findByOwner(owner);
    }

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> findById(Long id) {
        if (id == null) throw new ValidationException("Restorano ID yra privalomas");
        return restaurantRepository.findById(id);
    }
}
