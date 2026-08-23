package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.User;
import com.example.javakur.repository.OrderMessageRepository;

import java.util.List;

public class OrderMessageService {
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final OrderMessageRepository messageRepository;

    public OrderMessageService(OrderMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public OrderMessage sendMessage(FoodOrder order, User sender, String content) {
        validateOrder(order);
        validateSender(order, sender);
        String normalizedContent = normalizeContent(content);
        return messageRepository.save(new OrderMessage(order, sender, normalizedContent));
    }

    public List<OrderMessage> getConversation(FoodOrder order, User viewer) {
        if (order == null || order.getId() == null) {
            throw new ValidationException("Persisted order is required");
        }
        validateSender(order, viewer);
        return messageRepository.findByOrderId(order.getId());
    }

    private void validateOrder(FoodOrder order) {
        if (order == null || order.getId() == null) {
            throw new ValidationException("Persisted order is required");
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ValidationException("Messages cannot be sent for a finished order");
        }
    }

    private void validateSender(FoodOrder order, User sender) {
        if (sender == null || sender.getId() == null) {
            throw new ValidationException("Persisted sender is required");
        }
        if (!messageRepository.isOrderParticipant(order.getId(), sender.getId())) {
            throw new ValidationException("User is not a participant of this order");
        }
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ValidationException("Message cannot be blank");
        }
        String normalized = content.trim();
        if (normalized.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException("Message cannot exceed 2000 characters");
        }
        return normalized;
    }
}
