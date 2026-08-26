package com.example.javakur.api;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverOrderRepository extends JpaRepository<FoodOrder, Long> {
    @EntityGraph(attributePaths = {"client", "restaurant"})
    List<FoodOrder> findByStatusAndDriverIsNullOrderByCreatedAtAsc(OrderStatus status);

    @EntityGraph(attributePaths = {"client", "restaurant"})
    List<FoodOrder> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    @EntityGraph(attributePaths = {
            "client", "restaurant", "driver", "items", "items.menuItem"
    })
    Optional<FoodOrder> findByIdAndDriverId(Long id, Long driverId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM FoodOrder o WHERE o.id = :id")
    Optional<FoodOrder> findForUpdateById(@Param("id") Long id);
}
