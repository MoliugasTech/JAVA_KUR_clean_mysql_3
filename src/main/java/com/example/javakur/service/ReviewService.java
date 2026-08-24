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
        requireSameUser(order.getClient(), author, "Tik užsakymo klientas gali įvertinti restoraną");
        return createReview(order, author, ReviewType.RESTAURANT,
                order.getRestaurant(), null, rating, comment);
    }

    public Review reviewDriver(FoodOrder order, Client author, byte rating, String comment) {
        validateCompletedOrder(order);
        requireSameUser(order.getClient(), author, "Tik užsakymo klientas gali įvertinti vairuotoją");
        if (order.getDriver() == null || order.getDriver().getId() == null) {
            throw new ValidationException("Užbaigtas užsakymas neturi priskirto vairuotojo");
        }
        return createReview(order, author, ReviewType.DRIVER,
                null, order.getDriver(), rating, comment);
    }

    public Review reviewClient(FoodOrder order, Driver author, byte rating, String comment) {
        validateCompletedOrder(order);
        requireSameUser(order.getDriver(), author, "Tik priskirtas vairuotojas gali įvertinti klientą");
        return createReview(order, author, ReviewType.CLIENT,
                null, order.getClient(), rating, comment);
    }

    public List<Review> findByOrderId(Long orderId) {
        if (orderId == null) throw new ValidationException("Užsakymo ID yra privalomas");
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
            throw new ValidationException("Išsaugotas užsakymas yra privalomas");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ValidationException("Vertinti galima tik užbaigtus užsakymus");
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
            throw new ValidationException("Įvertinimas turi būti nuo 1 iki 5");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) return null;
        String normalized = comment.trim();
        if (normalized.length() > MAX_COMMENT_LENGTH) {
            throw new ValidationException("Atsiliepimo komentaras negali viršyti 2000 simbolių");
        }
        return normalized;
    }
}
