package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Admin;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.Review;
import com.example.javakur.model.User;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.repository.UserRepository;

import java.util.List;

public class AdminConsoleService {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public AdminConsoleService(UserRepository userRepository,
                               RestaurantRepository restaurantRepository,
                               OrderRepository orderRepository,
                               ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<User> listUsers(Long adminId) {
        requireAdmin(adminId);
        return userRepository.findAllOrdered();
    }

    public User setUserActive(Long adminId, Long userId, boolean active) {
        requireAdmin(adminId);
        if (adminId.equals(userId)) {
            throw new ValidationException("Administratorius negali pakeisti savo paskyros būsenos");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Naudotojas nerastas"));
        user.setActive(active);
        return userRepository.update(user);
    }

    public List<Restaurant> listRestaurants(Long adminId) {
        requireAdmin(adminId);
        return restaurantRepository.findAllWithOwner();
    }

    public List<FoodOrder> listOrders(Long adminId) {
        requireAdmin(adminId);
        return orderRepository.findAllWithParticipants();
    }

    public List<Review> listReviews(Long adminId) {
        requireAdmin(adminId);
        return reviewRepository.findAllDetailed();
    }

    private Admin requireAdmin(Long adminId) {
        User user = userRepository.findById(adminId)
                .orElseThrow(() -> new ValidationException("Administratorius nerastas"));
        if (!(user instanceof Admin admin)) {
            throw new ValidationException("Veiksmą gali atlikti tik administratorius");
        }
        return admin;
    }
}
