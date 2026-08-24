package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderMessage;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.Review;
import com.example.javakur.model.ReviewType;
import com.example.javakur.model.User;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.OrderMessageRepository;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.OrderInteractionService;
import com.example.javakur.service.OrderMessageService;
import com.example.javakur.service.ReviewService;
import com.example.javakur.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderInteractionServiceTest {
    private final FakeUserRepository users = new FakeUserRepository();
    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final FakeMessageRepository messages = new FakeMessageRepository();
    private final FakeReviewRepository reviews = new FakeReviewRepository();
    private final OrderInteractionService service = new OrderInteractionService(
            new UserService(users), orders, new OrderMessageService(messages),
            new ReviewService(reviews));

    private Client client;
    private Driver driver;
    private RestaurantOwner owner;
    private FoodOrder order;

    @BeforeEach
    void setUp() {
        client = new Client("client", "hash", "C", "C", "c@example.com", "Address");
        client.setId(1L);
        driver = new Driver("driver", "hash", "D", "D", "d@example.com", VehicleType.CAR);
        driver.setId(2L);
        owner = new RestaurantOwner("owner", "hash", "O", "O", "o@example.com");
        owner.setId(3L);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(4L);
        restaurant.setOwner(owner);
        order = new FoodOrder(client, restaurant);
        order.setId(5L);
        order.setDriver(driver);
        order.setStatus(OrderStatus.PICKED_UP);
        users.values = List.of(client, driver, owner);
        orders.order = order;
        messages.order = order;
    }

    @Test
    void participantCanSendAndReadConversation() {
        service.sendMessage(client.getId(), order.getId(), "Kur užsakymas?");
        service.sendMessage(driver.getId(), order.getId(), "Jau važiuoju");

        List<OrderMessage> conversation = service.getConversation(owner.getId(), order.getId());

        assertEquals(2, conversation.size());
        assertEquals("Jau važiuoju", conversation.get(1).getContent());
    }

    @Test
    void rejectsUserWhoIsNotOrderParticipant() {
        Client outsider = new Client("other", "hash", "X", "X", "x@example.com", "X");
        outsider.setId(99L);
        users.values = List.of(client, driver, owner, outsider);

        assertThrows(ValidationException.class,
                () -> service.sendMessage(outsider.getId(), order.getId(), "Hello"));
    }

    @Test
    void clientAndDriverCanCreateTheirAllowedReviews() {
        order.setStatus(OrderStatus.COMPLETED);

        service.reviewRestaurant(client.getId(), order.getId(), (byte) 5, "Puiku");
        service.reviewDriver(client.getId(), order.getId(), (byte) 4, null);
        service.reviewClient(driver.getId(), order.getId(), (byte) 5, "Malonus klientas");

        assertEquals(3, service.getReviews(owner.getId(), order.getId()).size());
    }

    @Test
    void roleCannotReviewUsingAnotherRolesAction() {
        order.setStatus(OrderStatus.COMPLETED);

        assertThrows(ValidationException.class,
                () -> service.reviewRestaurant(driver.getId(), order.getId(), (byte) 5, null));
        assertThrows(ValidationException.class,
                () -> service.reviewClient(client.getId(), order.getId(), (byte) 5, null));
    }

    private static boolean participant(FoodOrder order, Long userId) {
        return userId.equals(order.getClient().getId())
                || userId.equals(order.getDriver().getId())
                || userId.equals(order.getRestaurant().getOwner().getId());
    }

    private static class FakeUserRepository extends UserRepository {
        private List<User> values = List.of();

        @Override
        public Optional<User> findById(Long id) {
            return values.stream().filter(user -> user.getId().equals(id)).findFirst();
        }
    }

    private static class FakeOrderRepository extends OrderRepository {
        private FoodOrder order;

        @Override
        public List<FoodOrder> findByParticipantId(Long userId) {
            return order != null && participant(order, userId) ? List.of(order) : List.of();
        }

        @Override
        public Optional<FoodOrder> findParticipantOrder(Long orderId, Long userId) {
            return order != null && order.getId().equals(orderId) && participant(order, userId)
                    ? Optional.of(order) : Optional.empty();
        }
    }

    private static class FakeMessageRepository extends OrderMessageRepository {
        private final List<OrderMessage> values = new ArrayList<>();
        private FoodOrder order;

        @Override
        public OrderMessage save(OrderMessage message) {
            message.setId((long) values.size() + 1);
            values.add(message);
            return message;
        }

        @Override
        public List<OrderMessage> findByOrderId(Long orderId) {
            return values.stream().filter(value -> value.getOrder().getId().equals(orderId)).toList();
        }

        @Override
        public boolean isOrderParticipant(Long orderId, Long userId) {
            return order != null && order.getId().equals(orderId) && participant(order, userId);
        }
    }

    private static class FakeReviewRepository extends ReviewRepository {
        private final List<Review> values = new ArrayList<>();

        @Override
        public Review save(Review review) {
            review.setId((long) values.size() + 1);
            values.add(review);
            return review;
        }

        @Override
        public boolean existsByOrderAuthorAndType(Long orderId, Long authorId, ReviewType type) {
            return values.stream().anyMatch(review -> review.getOrder().getId().equals(orderId)
                    && review.getAuthor().getId().equals(authorId) && review.getType() == type);
        }

        @Override
        public List<Review> findByOrderId(Long orderId) {
            return values.stream().filter(value -> value.getOrder().getId().equals(orderId)).toList();
        }
    }
}
