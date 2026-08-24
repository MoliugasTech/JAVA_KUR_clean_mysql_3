package com.example.javakur.repository;

import com.example.javakur.model.User;
import com.example.javakur.model.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository<User, Long> {
    public UserRepository() {
        super(User.class);
    }

    public Optional<User> findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public Optional<User> findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            return Optional.of(em.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<User> findByRole(UserRole role) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.role = :role ORDER BY u.surname, u.name", User.class)
                    .setParameter("role", role)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<User> findAllOrdered() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT u FROM User u ORDER BY u.role, u.surname, u.name", User.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
