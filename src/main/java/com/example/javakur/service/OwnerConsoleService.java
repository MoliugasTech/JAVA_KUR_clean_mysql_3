package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.User;
import com.example.javakur.repository.OrderMessageRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.RestaurantRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class OwnerConsoleService {
    private static final EnumSet<OrderStatus> OWNER_STATUSES = EnumSet.of(
            OrderStatus.CONFIRMED, OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP);

    private final UserService userService;
    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final OrderService orderService;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final OrderMessageService messageService;

    public OwnerConsoleService(UserService userService, RestaurantService restaurantService,
                               MenuService menuService, OrderService orderService,
                               RestaurantRepository restaurantRepository,
                               OrderRepository orderRepository) {
        this(userService, restaurantService, menuService, orderService,
                restaurantRepository, orderRepository,
                new OrderMessageService(new OrderMessageRepository()));
    }

    public OwnerConsoleService(UserService userService, RestaurantService restaurantService,
                               MenuService menuService, OrderService orderService,
                               RestaurantRepository restaurantRepository,
                               OrderRepository orderRepository,
                               OrderMessageService messageService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.orderService = orderService;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.messageService = messageService;
    }

    public RestaurantOwner register(String username, String password, String name,
                                    String surname, String email) {
        return userService.createRestaurantOwner(username, password, name, surname, email);
    }

    public Restaurant createRestaurant(Long ownerId, String name, String description, String address,
                                       RestaurantType type, LocalTime openingTime, LocalTime closingTime) {
        return restaurantService.createRestaurant(name, description, address, type,
                openingTime, closingTime, requireOwner(ownerId));
    }

    public Restaurant updateRestaurant(Long ownerId, Long restaurantId, String name,
                                       String description, String address, RestaurantType type,
                                       LocalTime openingTime, LocalTime closingTime) {
        Restaurant restaurant = requireOwnedRestaurant(ownerId, restaurantId);
        return restaurantService.updateRestaurant(restaurant, name, description, address,
                type, openingTime, closingTime);
    }

    public List<Restaurant> listRestaurants(Long ownerId) {
        return restaurantService.findByOwner(requireOwner(ownerId));
    }

    public MenuItem addMenuItem(Long ownerId, Long restaurantId, String name,
                                String description, BigDecimal price) {
        Restaurant restaurant = requireOwnedRestaurant(ownerId, restaurantId);
        return menuService.createMenuItem(name, description, price, restaurant);
    }

    public List<MenuItem> listMenu(Long ownerId, Long restaurantId) {
        return menuService.findByRestaurant(requireOwnedRestaurant(ownerId, restaurantId));
    }

    public MenuItem updateMenuItem(Long ownerId, Long restaurantId, Long itemId,
                                   BigDecimal newPrice, Boolean available) {
        Restaurant restaurant = requireOwnedRestaurant(ownerId, restaurantId);
        MenuItem item = menuService.findById(itemId)
                .orElseThrow(() -> new ValidationException("Meniu elementas nerastas"));
        if (item.getRestaurant() == null || item.getRestaurant().getId() == null
                || !restaurant.getId().equals(item.getRestaurant().getId())) {
            throw new ValidationException("Meniu elementas nepriklauso šiam restoranui");
        }
        if (newPrice != null) item = menuService.updatePrice(item, newPrice);
        if (available != null) item = menuService.updateAvailability(item, available);
        return item;
    }

    public MenuItem updateMenuItem(Long ownerId, Long restaurantId, Long itemId,
                                   String name, String description, BigDecimal price,
                                   boolean available) {
        Restaurant restaurant = requireOwnedRestaurant(ownerId, restaurantId);
        MenuItem item = requireOwnedMenuItem(restaurant, itemId);
        return menuService.updateMenuItem(item, name, description, price, available);
    }

    public void deleteMenuItem(Long ownerId, Long restaurantId, Long itemId) {
        Restaurant restaurant = requireOwnedRestaurant(ownerId, restaurantId);
        requireOwnedMenuItem(restaurant, itemId);
        menuService.deleteMenuItem(itemId);
    }

    public List<FoodOrder> listOrders(Long ownerId, Long restaurantId) {
        requireOwnedRestaurant(ownerId, restaurantId);
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public List<FoodOrder> listOrders(Long ownerId, Long restaurantId,
                                      OrderStatus status, LocalDate fromDate,
                                      LocalDate toDate, String clientQuery) {
        List<FoodOrder> orders = listOrders(ownerId, restaurantId);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("Datos nuo negali būti vėlesnė už datą iki");
        }
        String query = clientQuery == null ? "" : clientQuery.trim().toLowerCase(Locale.ROOT);
        return orders.stream()
                .filter(order -> status == null || order.getStatus() == status)
                .filter(order -> fromDate == null || order.getCreatedAt() != null
                        && !order.getCreatedAt().toLocalDate().isBefore(fromDate))
                .filter(order -> toDate == null || order.getCreatedAt() != null
                        && !order.getCreatedAt().toLocalDate().isAfter(toDate))
                .filter(order -> query.isEmpty() || matchesClient(order, query))
                .toList();
    }

    public FoodOrder getOrder(Long ownerId, Long orderId) {
        return requireOwnedOrder(ownerId, orderId);
    }

    public List<OrderMessage> listMessages(Long ownerId, Long orderId) {
        FoodOrder order = requireOwnedOrder(ownerId, orderId);
        return messageService.getConversation(order, requireOwner(ownerId));
    }

    public OrderMessage sendMessage(Long ownerId, Long orderId, String content) {
        FoodOrder order = requireOwnedOrder(ownerId, orderId);
        return messageService.sendMessage(order, requireOwner(ownerId), content);
    }

    public FoodOrder changeOrderStatus(Long ownerId, Long orderId, OrderStatus newStatus) {
        if (newStatus == null || !OWNER_STATUSES.contains(newStatus)) {
            throw new ValidationException("Restorano savininkas negali nustatyti būsenos " + newStatus);
        }
        FoodOrder order = orderRepository.findOwnedByRestaurantOwner(orderId, ownerId)
                .orElseThrow(() -> new ValidationException("Užsakymas nepriklauso šiam savininkui"));
        return orderService.changeStatus(order, newStatus);
    }

    public FoodOrder cancelOrder(Long ownerId, Long orderId, String reason) {
        return orderService.cancel(requireOwnedOrder(ownerId, orderId), reason);
    }

    public FoodOrder removeOrderItem(Long ownerId, Long orderId, Long orderItemId) {
        return orderService.removeItem(requireOwnedOrder(ownerId, orderId), orderItemId);
    }

    public FoodOrder setDriverAssignmentAllowed(Long ownerId, Long orderId, boolean allowed) {
        return orderService.setDriverAssignmentAllowed(
                requireOwnedOrder(ownerId, orderId), allowed);
    }

    private boolean matchesClient(FoodOrder order, String query) {
        if (order.getClient() == null) return false;
        return contains(order.getClient().getUsername(), query)
                || contains(order.getClient().getName(), query)
                || contains(order.getClient().getSurname(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private MenuItem requireOwnedMenuItem(Restaurant restaurant, Long itemId) {
        if (itemId == null) {
            throw new ValidationException("Meniu elemento ID yra privalomas");
        }
        MenuItem item = menuService.findById(itemId)
                .orElseThrow(() -> new ValidationException("Meniu elementas nerastas"));
        if (item.getRestaurant() == null || item.getRestaurant().getId() == null
                || !restaurant.getId().equals(item.getRestaurant().getId())) {
            throw new ValidationException("Meniu elementas nepriklauso šiam restoranui");
        }
        return item;
    }

    private FoodOrder requireOwnedOrder(Long ownerId, Long orderId) {
        if (ownerId == null || orderId == null) {
            throw new ValidationException("Savininko ir užsakymo ID yra privalomi");
        }
        return orderRepository.findOwnedByRestaurantOwner(orderId, ownerId)
                .orElseThrow(() -> new ValidationException(
                        "Užsakymas nepriklauso šiam savininkui"));
    }

    private RestaurantOwner requireOwner(Long ownerId) {
        User user = userService.findById(ownerId)
                .orElseThrow(() -> new ValidationException("Restorano savininkas nerastas"));
        if (!(user instanceof RestaurantOwner owner)) {
            throw new ValidationException("Naudotojas nėra restorano savininkas");
        }
        return owner;
    }

    private Restaurant requireOwnedRestaurant(Long ownerId, Long restaurantId) {
        if (ownerId == null || restaurantId == null) {
            throw new ValidationException("Savininko ir restorano ID yra privalomi");
        }
        return restaurantRepository.findOwnedById(restaurantId, ownerId)
                .orElseThrow(() -> new ValidationException("Restoranas nepriklauso šiam savininkui"));
    }
}
