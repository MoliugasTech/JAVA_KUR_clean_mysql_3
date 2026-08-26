package com.example.javakur.api;

import com.example.javakur.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientOrderRepository extends JpaRepository<FoodOrder, Long> {
}
