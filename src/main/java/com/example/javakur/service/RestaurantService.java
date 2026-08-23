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
        if (name == null || name.isBlank()) throw new ValidationException("Restaurant name cannot be blank");
        if (address == null || address.isBlank()) throw new ValidationException("Restaurant address cannot be blank");
        if (type == null) throw new ValidationException("Restaurant type is required");
        if (openingTime == null || closingTime == null) throw new ValidationException("Working hours are required");
        if (owner == null || owner.getId() == null) throw new ValidationException("Persisted restaurant owner is required");

        return restaurantRepository.save(new Restaurant(name.trim(), description, address.trim(), type,
                openingTime, closingTime, owner));
    }

    public List<Restaurant> findByOwner(RestaurantOwner owner) {
        return restaurantRepository.findByOwner(owner);
    }

    public List<Restaurant> findAll() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> findById(Long id) {
        if (id == null) throw new ValidationException("Restaurant id is required");
        return restaurantRepository.findById(id);
    }
}
