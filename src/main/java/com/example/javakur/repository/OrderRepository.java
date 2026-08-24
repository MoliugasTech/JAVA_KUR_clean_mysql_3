package com.example.javakur.repository;

import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
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

    public List<FoodOrder> findByParticipantId(Long userId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT o FROM FoodOrder o "
                                    + "JOIN FETCH o.client JOIN FETCH o.restaurant r "
                                    + "JOIN FETCH r.owner LEFT JOIN FETCH o.driver "
                                    + "WHERE o.client.id = :userId OR o.driver.id = :userId "
                                    + "OR r.owner.id = :userId ORDER BY o.createdAt DESC",
                            FoodOrder.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<FoodOrder> findParticipantOrder(Long orderId, Long userId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT o FROM FoodOrder o "
                                    + "JOIN FETCH o.client JOIN FETCH o.restaurant r "
                                    + "JOIN FETCH r.owner LEFT JOIN FETCH o.driver "
                                    + "WHERE o.id = :orderId AND (o.client.id = :userId "
                                    + "OR o.driver.id = :userId OR r.owner.id = :userId)",
                            FoodOrder.class)
                    .setParameter("orderId", orderId)
                    .setParameter("userId", userId)
                    .getResultStream()
                    .findFirst();
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

    public List<FoodOrder> findReadyForPickup() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM FoodOrder o JOIN FETCH o.restaurant JOIN FETCH o.client "
                                    + "WHERE o.status = :status AND o.driver IS NULL ORDER BY o.createdAt",
                            FoodOrder.class)
                    .setParameter("status", OrderStatus.READY_FOR_PICKUP)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<FoodOrder> findReadyUnassignedById(Long orderId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM FoodOrder o JOIN FETCH o.restaurant JOIN FETCH o.client "
                                    + "WHERE o.id = :orderId AND o.status = :status AND o.driver IS NULL",
                            FoodOrder.class)
                    .setParameter("orderId", orderId)
                    .setParameter("status", OrderStatus.READY_FOR_PICKUP)
                    .getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    public List<FoodOrder> findActiveByDriverId(Long driverId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM FoodOrder o JOIN FETCH o.restaurant JOIN FETCH o.client "
                                    + "WHERE o.driver.id = :driverId AND o.status IN :statuses "
                                    + "ORDER BY o.createdAt",
                            FoodOrder.class)
                    .setParameter("driverId", driverId)
                    .setParameter("statuses", List.of(OrderStatus.PICKED_UP, OrderStatus.DELIVERING))
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<FoodOrder> findOwnedByDriver(Long orderId, Long driverId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM FoodOrder o JOIN FETCH o.restaurant JOIN FETCH o.client "
                                    + "JOIN FETCH o.driver WHERE o.id = :orderId AND o.driver.id = :driverId",
                            FoodOrder.class)
                    .setParameter("orderId", orderId)
                    .setParameter("driverId", driverId)
                    .getResultStream().findFirst();
        } finally {
            em.close();
        }
    }
}
