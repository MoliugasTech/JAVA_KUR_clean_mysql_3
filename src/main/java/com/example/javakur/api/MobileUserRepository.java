package com.example.javakur.api;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MobileUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT c FROM Client c WHERE c.active = true ORDER BY c.surname, c.name")
    List<Client> findActiveClients();

    @Query("SELECT c FROM Client c WHERE c.id = :id AND c.active = true")
    Optional<Client> findActiveClientById(@Param("id") Long id);

    @Query("SELECT c FROM Client c WHERE c.id = :id")
    Optional<Client> findAnyClientById(@Param("id") Long id);

    @Query("SELECT d FROM Driver d WHERE d.active = true ORDER BY d.surname, d.name")
    List<Driver> findActiveDrivers();

    @Query("SELECT d FROM Driver d WHERE d.id = :id AND d.active = true")
    Optional<Driver> findActiveDriverById(@Param("id") Long id);

    @Query("SELECT d FROM Driver d WHERE d.id = :id")
    Optional<Driver> findAnyDriverById(@Param("id") Long id);
}
