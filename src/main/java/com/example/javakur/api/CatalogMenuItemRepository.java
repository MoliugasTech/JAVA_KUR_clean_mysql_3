package com.example.javakur.api;

import com.example.javakur.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CatalogMenuItemRepository extends JpaRepository<MenuItem, Long> {
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId "
            + "AND m.available = true ORDER BY m.name")
    List<MenuItem> findAvailableByRestaurantId(@Param("restaurantId") Long restaurantId);
}
