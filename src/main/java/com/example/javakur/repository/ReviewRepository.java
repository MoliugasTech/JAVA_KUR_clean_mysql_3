package com.example.javakur.repository;

import com.example.javakur.model.Review;
import com.example.javakur.model.ReviewType;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ReviewRepository extends BaseRepository<Review, Long> {
    public ReviewRepository() {
        super(Review.class);
    }

    public boolean existsByOrderAuthorAndType(Long orderId, Long authorId, ReviewType type) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(r) FROM Review r "
                                    + "WHERE r.order.id = :orderId AND r.author.id = :authorId "
                                    + "AND r.type = :type",
                            Long.class)
                    .setParameter("orderId", orderId)
                    .setParameter("authorId", authorId)
                    .setParameter("type", type)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public List<Review> findByOrderId(Long orderId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Review r "
                                    + "LEFT JOIN FETCH r.restaurant "
                                    + "LEFT JOIN FETCH r.reviewedUser "
                                    + "WHERE r.order.id = :orderId ORDER BY r.createdAt",
                            Review.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
