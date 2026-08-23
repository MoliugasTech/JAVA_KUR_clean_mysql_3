package com.example.javakur.repository;

import com.example.javakur.model.FoodOrder;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class OrderRepository extends BaseRepository<FoodOrder, Long> {
    public OrderRepository() {
        super(FoodOrder.class);
    }

    public Optional<FoodOrder> findDetailedById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT o FROM FoodOrder o "
                                    + "LEFT JOIN FETCH o.items i "
                                    + "LEFT JOIN FETCH i.menuItem "
                                    + "WHERE o.id = :id",
                            FoodOrder.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    public List<FoodOrder> findByClientId(Long clientId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM FoodOrder o "
                                    + "JOIN FETCH o.restaurant "
                                    + "WHERE o.client.id = :clientId ORDER BY o.createdAt DESC",
                            FoodOrder.class)
                    .setParameter("clientId", clientId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<FoodOrder> findByRestaurantId(Long restaurantId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM FoodOrder o "
                                    + "JOIN FETCH o.client JOIN FETCH o.restaurant "
                                    + "WHERE o.restaurant.id = :restaurantId ORDER BY o.createdAt DESC",
                            FoodOrder.class)
                    .setParameter("restaurantId", restaurantId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<FoodOrder> findOwnedByRestaurantOwner(Long orderId, Long ownerId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM FoodOrder o "
                                    + "JOIN FETCH o.restaurant r JOIN FETCH r.owner "
                                    + "JOIN FETCH o.client LEFT JOIN FETCH o.driver "
                                    + "WHERE o.id = :orderId AND r.owner.id = :ownerId",
                            FoodOrder.class)
                    .setParameter("orderId", orderId)
                    .setParameter("ownerId", ownerId)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
}
