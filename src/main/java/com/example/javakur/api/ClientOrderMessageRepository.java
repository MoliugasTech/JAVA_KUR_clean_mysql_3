package com.example.javakur.api;

import com.example.javakur.model.OrderMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientOrderMessageRepository extends JpaRepository<OrderMessage, Long> {
    @EntityGraph(attributePaths = "sender")
    List<OrderMessage> findByOrderIdOrderBySentAtAscIdAsc(Long orderId);
}
