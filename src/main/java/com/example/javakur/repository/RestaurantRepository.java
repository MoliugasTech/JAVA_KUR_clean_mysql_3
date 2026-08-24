package com.example.javakur.repository;

import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class RestaurantRepository extends BaseRepository<Restaurant, Long> {
    public RestaurantRepository() {
        super(Restaurant.class);
    }

    public List<Restaurant> findByOwner(RestaurantOwner owner) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Restaurant r WHERE r.owner = :owner ORDER BY r.name", Restaurant.class)
                    .setParameter("owner", owner)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Restaurant> findOwnedById(Long restaurantId, Long ownerId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Restaurant r JOIN FETCH r.owner "
                                    + "WHERE r.id = :restaurantId AND r.owner.id = :ownerId",
                            Restaurant.class)
                    .setParameter("restaurantId", restaurantId)
                    .setParameter("ownerId", ownerId)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    public List<Restaurant> findAllWithOwner() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Restaurant r JOIN FETCH r.owner ORDER BY r.name", Restaurant.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
