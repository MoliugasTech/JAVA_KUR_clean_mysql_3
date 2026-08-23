package com.example.javakur.repository;

import com.example.javakur.model.OrderMessage;
import jakarta.persistence.EntityManager;

import java.util.List;

public class OrderMessageRepository extends BaseRepository<OrderMessage, Long> {
    public OrderMessageRepository() {
        super(OrderMessage.class);
    }

    public List<OrderMessage> findByOrderId(Long orderId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM OrderMessage m "
                                    + "JOIN FETCH m.sender "
                                    + "WHERE m.order.id = :orderId ORDER BY m.sentAt, m.id",
                            OrderMessage.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
