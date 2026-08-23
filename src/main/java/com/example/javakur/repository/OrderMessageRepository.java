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

    public boolean isOrderParticipant(Long orderId, Long userId) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(o) FROM FoodOrder o "
                                    + "WHERE o.id = :orderId AND ("
                                    + "o.client.id = :userId OR o.driver.id = :userId "
                                    + "OR o.restaurant.owner.id = :userId)",
                            Long.class)
                    .setParameter("orderId", orderId)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}
