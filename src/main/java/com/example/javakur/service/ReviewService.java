package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.Review;
import com.example.javakur.model.ReviewType;
import com.example.javakur.model.User;
import com.example.javakur.repository.ReviewRepository;

import java.util.List;

public class ReviewService {
    private static final int MAX_COMMENT_LENGTH = 2000;

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review reviewRestaurant(FoodOrder order, Client author, byte rating, String comment) {
        validateCompletedOrder(order);
        requireSameUser(order.getClient(), author, "Only the order client can review the restaurant");
        return createReview(order, author, ReviewType.RESTAURANT,
                order.getRestaurant(), null, rating, comment);
    }

    public Review reviewDriver(FoodOrder order, Client author, byte rating, String comment) {
        validateCompletedOrder(order);
        requireSameUser(order.getClient(), author, "Only the order client can review the driver");
        if (order.getDriver() == null || order.getDriver().getId() == null) {
            throw new ValidationException("Completed order does not have a driver");
        }
        return createReview(order, author, ReviewType.DRIVER,
                null, order.getDriver(), rating, comment);
    }

    public Review reviewClient(FoodOrder order, Driver author, byte rating, String comment) {
        validateCompletedOrder(order);
        requireSameUser(order.getDriver(), author, "Only the assigned driver can review the client");
        return createReview(order, author, ReviewType.CLIENT,
                null, order.getClient(), rating, comment);
    }

    public List<Review> findByOrderId(Long orderId) {
        if (orderId == null) throw new ValidationException("Order id is required");
        return reviewRepository.findByOrderId(orderId);
    }

    private Review createReview(FoodOrder order, User author, ReviewType type,
                                Restaurant restaurant, User reviewedUser,
                                byte rating, String comment) {
        validateRating(rating);
        String normalizedComment = normalizeComment(comment);
        if (reviewRepository.existsByOrderAuthorAndType(order.getId(), author.getId(), type)) {
            throw new ValidationException("Šis užsakymas jau įvertintas pasirinktam tipui: " + type);
        }
        return reviewRepository.save(new Review(
                order, author, type, restaurant, reviewedUser, rating, normalizedComment));
    }

    private void validateCompletedOrder(FoodOrder order) {
        if (order == null || order.getId() == null) {
            throw new ValidationException("Persisted order is required");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ValidationException("Only completed orders can be reviewed");
        }
    }

    private void requireSameUser(User expected, User actual, String message) {
        if (expected == null || expected.getId() == null || actual == null || actual.getId() == null
                || !expected.getId().equals(actual.getId())) {
            throw new ValidationException(message);
        }
    }

    private void validateRating(byte rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) return null;
        String normalized = comment.trim();
        if (normalized.length() > MAX_COMMENT_LENGTH) {
            throw new ValidationException("Review comment cannot exceed 2000 characters");
        }
        return normalized;
    }
}
