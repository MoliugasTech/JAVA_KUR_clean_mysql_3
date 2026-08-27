package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Admin;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.Review;
import com.example.javakur.model.User;
import com.example.javakur.repository.MenuItemRepository;
import com.example.javakur.repository.OrderMessageRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class AdminConsoleService {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderMessageRepository messageRepository;
    private final MenuService menuService;

    public AdminConsoleService(UserRepository userRepository,
                               RestaurantRepository restaurantRepository,
                               OrderRepository orderRepository,
                               ReviewRepository reviewRepository) {
        this(userRepository, restaurantRepository, orderRepository, reviewRepository,
                new MenuItemRepository(), new OrderMessageRepository());
    }

    public AdminConsoleService(UserRepository userRepository,
                               RestaurantRepository restaurantRepository,
                               OrderRepository orderRepository,
                               ReviewRepository reviewRepository,
                               MenuItemRepository menuItemRepository,
                               OrderMessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.menuItemRepository = menuItemRepository;
        this.messageRepository = messageRepository;
        this.menuService = new MenuService(menuItemRepository);
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

    public List<FoodOrder> listOrders(Long adminId, OrderStatus status, Long restaurantId,
                                      LocalDate fromDate, LocalDate toDate,
                                      String clientQuery) {
        List<FoodOrder> orders = listOrders(adminId);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("Datos nuo negali būti vėlesnė už datą iki");
        }
        String query = clientQuery == null ? "" : clientQuery.trim().toLowerCase(Locale.ROOT);
        return orders.stream()
                .filter(order -> status == null || order.getStatus() == status)
                .filter(order -> restaurantId == null
                        || order.getRestaurant().getId().equals(restaurantId))
                .filter(order -> fromDate == null || order.getCreatedAt() != null
                        && !order.getCreatedAt().toLocalDate().isBefore(fromDate))
                .filter(order -> toDate == null || order.getCreatedAt() != null
                        && !order.getCreatedAt().toLocalDate().isAfter(toDate))
                .filter(order -> query.isEmpty()
                        || contains(order.getClient().getUsername(), query)
                        || contains(order.getClient().getName(), query)
                        || contains(order.getClient().getSurname(), query))
                .toList();
    }

    public void deleteTerminalOrder(Long adminId, Long orderId) {
        requireAdmin(adminId);
        FoodOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ValidationException("Užsakymas nerastas"));
        if (order.getStatus() != OrderStatus.COMPLETED
                && order.getStatus() != OrderStatus.CANCELLED) {
            throw new ValidationException(
                    "Šalinti galima tik užbaigtą arba atšauktą užsakymą");
        }
        orderRepository.deleteWithDependents(orderId);
    }

    public List<MenuItem> listMenu(Long adminId, Long restaurantId) {
        requireAdmin(adminId);
        return menuItemRepository.findByRestaurant(requireRestaurant(restaurantId));
    }

    public MenuItem createMenuItem(Long adminId, Long restaurantId, String name,
                                   String description, BigDecimal price) {
        requireAdmin(adminId);
        return menuService.createMenuItem(name, description, price,
                requireRestaurant(restaurantId));
    }

    public MenuItem updateMenuItem(Long adminId, Long restaurantId, Long itemId,
                                   String name, String description, BigDecimal price,
                                   boolean available) {
        requireAdmin(adminId);
        Restaurant restaurant = requireRestaurant(restaurantId);
        MenuItem item = requireMenuItem(itemId);
        if (item.getRestaurant() == null
                || !restaurant.getId().equals(item.getRestaurant().getId())) {
            throw new ValidationException("Patiekalas nepriklauso pasirinktam restoranui");
        }
        return menuService.updateMenuItem(item, name, description, price, available);
    }

    public void deleteMenuItem(Long adminId, Long restaurantId, Long itemId) {
        requireAdmin(adminId);
        Restaurant restaurant = requireRestaurant(restaurantId);
        MenuItem item = requireMenuItem(itemId);
        if (item.getRestaurant() == null
                || !restaurant.getId().equals(item.getRestaurant().getId())) {
            throw new ValidationException("Patiekalas nepriklauso pasirinktam restoranui");
        }
        menuItemRepository.deleteById(itemId);
    }

    public List<OrderMessage> listMessages(Long adminId) {
        requireAdmin(adminId);
        return messageRepository.findAllDetailed();
    }

    public OrderMessage updateMessage(Long adminId, Long messageId, String content) {
        requireAdmin(adminId);
        OrderMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ValidationException("Žinutė nerasta"));
        if (content == null || content.isBlank()) {
            throw new ValidationException("Žinutė negali būti tuščia");
        }
        String normalized = content.trim();
        if (normalized.length() > 2000) {
            throw new ValidationException("Žinutė negali viršyti 2000 simbolių");
        }
        message.setContent(normalized);
        return messageRepository.update(message);
    }

    public void deleteMessage(Long adminId, Long messageId) {
        requireAdmin(adminId);
        if (!messageRepository.existsById(messageId)) {
            throw new ValidationException("Žinutė nerasta");
        }
        messageRepository.deleteById(messageId);
    }

    public List<Review> listReviews(Long adminId) {
        requireAdmin(adminId);
        return reviewRepository.findAllDetailed();
    }

    public void deleteReview(Long adminId, Long reviewId) {
        requireAdmin(adminId);
        if (!reviewRepository.existsById(reviewId)) {
            throw new ValidationException("Atsiliepimas nerastas");
        }
        reviewRepository.deleteById(reviewId);
    }

    private Restaurant requireRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new ValidationException("Restorano ID yra privalomas");
        }
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ValidationException("Restoranas nerastas"));
    }

    private MenuItem requireMenuItem(Long itemId) {
        if (itemId == null) {
            throw new ValidationException("Patiekalo ID yra privalomas");
        }
        return menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Patiekalas nerastas"));
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
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
