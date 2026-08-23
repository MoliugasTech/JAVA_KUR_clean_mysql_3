package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.Client;
import com.example.javakur.model.User;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.util.PasswordUtil;

import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RestaurantOwner createRestaurantOwner(String username, String plainPassword, String name,
                                                  String surname, String email) {
        validateCommonUserFields(username, plainPassword, name, surname, email);
        validateUniqueCredentials(username, email);
        RestaurantOwner owner = new RestaurantOwner(
                username.trim(), PasswordUtil.hashPassword(plainPassword), name.trim(), surname.trim(), email.trim());
        return (RestaurantOwner) userRepository.save(owner);
    }

    public Client createClient(String username, String plainPassword, String name,
                               String surname, String email, String deliveryAddress) {
        validateCommonUserFields(username, plainPassword, name, surname, email);
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new ValidationException("Delivery address cannot be blank");
        }
        validateUniqueCredentials(username, email);

        Client client = new Client(
                username.trim(), PasswordUtil.hashPassword(plainPassword), name.trim(), surname.trim(),
                email.trim(), deliveryAddress.trim());
        return (Client) userRepository.save(client);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private void validateCommonUserFields(String username, String password, String name, String surname, String email) {
        if (username == null || username.isBlank()) throw new ValidationException("Username cannot be blank");
        if (password == null || password.length() < 8) throw new ValidationException("Password must contain at least 8 characters");
        if (name == null || name.isBlank()) throw new ValidationException("Name cannot be blank");
        if (surname == null || surname.isBlank()) throw new ValidationException("Surname cannot be blank");
        if (email == null || email.isBlank()) throw new ValidationException("Email cannot be blank");
    }

    private void validateUniqueCredentials(String username, String email) {
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new ValidationException("Username is already in use");
        }
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            throw new ValidationException("Email is already in use");
        }
    }
}
