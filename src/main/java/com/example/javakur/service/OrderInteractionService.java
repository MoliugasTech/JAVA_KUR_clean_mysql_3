package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.Review;
import com.example.javakur.model.User;
import com.example.javakur.repository.OrderRepository;

import java.util.List;

public class OrderInteractionService {
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderMessageService messageService;
    private final ReviewService reviewService;

    public OrderInteractionService(UserService userService, OrderRepository orderRepository,
                                   OrderMessageService messageService, ReviewService reviewService) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.messageService = messageService;
        this.reviewService = reviewService;
    }

    public List<FoodOrder> listParticipantOrders(Long userId) {
        requireUser(userId);
        return orderRepository.findByParticipantId(userId);
    }

    public List<OrderMessage> getConversation(Long userId, Long orderId) {
        User user = requireUser(userId);
        return messageService.getConversation(requireParticipantOrder(orderId, userId), user);
    }

    public OrderMessage sendMessage(Long userId, Long orderId, String content) {
        User user = requireUser(userId);
        return messageService.sendMessage(requireParticipantOrder(orderId, userId), user, content);
    }

    public List<Review> getReviews(Long userId, Long orderId) {
        requireUser(userId);
        FoodOrder order = requireParticipantOrder(orderId, userId);
        return reviewService.findByOrderId(order.getId());
    }

    public Review reviewRestaurant(Long clientId, Long orderId, byte rating, String comment) {
        return reviewService.reviewRestaurant(requireParticipantOrder(orderId, clientId),
                requireClient(clientId), rating, comment);
    }

    public Review reviewDriver(Long clientId, Long orderId, byte rating, String comment) {
        return reviewService.reviewDriver(requireParticipantOrder(orderId, clientId),
                requireClient(clientId), rating, comment);
    }

    public Review reviewClient(Long driverId, Long orderId, byte rating, String comment) {
        return reviewService.reviewClient(requireParticipantOrder(orderId, driverId),
                requireDriver(driverId), rating, comment);
    }

    private FoodOrder requireParticipantOrder(Long orderId, Long userId) {
        if (orderId == null) throw new ValidationException("Order id is required");
        return orderRepository.findParticipantOrder(orderId, userId)
                .orElseThrow(() -> new ValidationException("Order was not found for this user"));
    }

    private User requireUser(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new ValidationException("User was not found"));
    }

    private Client requireClient(Long userId) {
        User user = requireUser(userId);
        if (!(user instanceof Client client)) throw new ValidationException("User is not a client");
        return client;
    }

    private Driver requireDriver(Long userId) {
        User user = requireUser(userId);
        if (!(user instanceof Driver driver)) throw new ValidationException("User is not a driver");
        return driver;
    }
}
