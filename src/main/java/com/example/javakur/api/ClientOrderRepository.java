package com.example.javakur.api;

import com.example.javakur.model.FoodOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientOrderRepository extends JpaRepository<FoodOrder, Long> {
    @EntityGraph(attributePaths = {"restaurant", "driver"})
    List<FoodOrder> findByClientIdOrderByCreatedAtDesc(Long clientId);

    @EntityGraph(attributePaths = {
            "client", "restaurant", "driver", "items", "items.menuItem"
    })
    Optional<FoodOrder> findByIdAndClientId(Long id, Long clientId);
}
