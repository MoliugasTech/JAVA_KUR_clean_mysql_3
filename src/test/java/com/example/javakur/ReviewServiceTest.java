package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.Review;
import com.example.javakur.model.ReviewType;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.ReviewRepository;
import com.example.javakur.service.ReviewService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewServiceTest {

    private final InMemoryReviewRepository repository = new InMemoryReviewRepository();
    private final ReviewService service = new ReviewService(repository);

    @Test
    void clientReviewsRestaurantAfterCompletion() {
        FoodOrder order = completedOrder();

        Review review = service.reviewRestaurant(order, order.getClient(), (byte) 5, "  Puiku  ");

        assertEquals(ReviewType.RESTAURANT, review.getType());
        assertSame(order.getRestaurant(), review.getRestaurant());
        assertEquals("Puiku", review.getComment());
    }

    @Test
    void clientReviewsAssignedDriver() {
        FoodOrder order = completedOrder();

        Review review = service.reviewDriver(order, order.getClient(), (byte) 4, null);

        assertEquals(ReviewType.DRIVER, review.getType());
        assertSame(order.getDriver(), review.getReviewedUser());
        assertNull(review.getComment());
    }

    @Test
    void assignedDriverReviewsClient() {
        FoodOrder order = completedOrder();

        Review review = service.reviewClient(order, order.getDriver(), (byte) 5, "Mandagus klientas");

        assertEquals(ReviewType.CLIENT, review.getType());
        assertSame(order.getClient(), review.getReviewedUser());
    }

    @Test
    void rejectsReviewBeforeOrderCompletion() {
        FoodOrder order = completedOrder();
        order.setStatus(OrderStatus.DELIVERING);

        assertThrows(ValidationException.class,
                () -> service.reviewRestaurant(order, order.getClient(), (byte) 5, null));
    }

    @Test
    void rejectsOutsiderReview() {
        FoodOrder order = completedOrder();
        Client outsider = client(99L, "outsider");

        assertThrows(ValidationException.class,
                () -> service.reviewRestaurant(order, outsider, (byte) 5, null));
    }

    @Test
    void rejectsRatingOutsideOneToFive() {
        FoodOrder order = completedOrder();

        assertThrows(ValidationException.class,
                () -> service.reviewDriver(order, order.getClient(), (byte) 0, null));
        assertThrows(ValidationException.class,
                () -> service.reviewDriver(order, order.getClient(), (byte) 6, null));
    }

    @Test
    void rejectsDuplicateReviewForSameTargetType() {
        FoodOrder order = completedOrder();
        service.reviewRestaurant(order, order.getClient(), (byte) 5, null);

        assertThrows(ValidationException.class,
                () -> service.reviewRestaurant(order, order.getClient(), (byte) 4, null));
    }

    private FoodOrder completedOrder() {
        Client client = client(1L, "client");
        Driver driver = new Driver(
                "driver", "hash", "Test", "Driver", "driver@example.com", VehicleType.CAR);
        driver.setId(2L);
        Restaurant restaurant = new Restaurant();
        restaurant.setId(3L);

        FoodOrder order = new FoodOrder(client, restaurant);
        order.setId(4L);
        order.setDriver(driver);
        order.setStatus(OrderStatus.COMPLETED);
        return order;
    }

    private Client client(Long id, String username) {
        Client client = new Client(
                username, "hash", "Test", "Client", username + "@example.com", "Test address");
        client.setId(id);
        return client;
    }

    private static class InMemoryReviewRepository extends ReviewRepository {
        private final List<Review> reviews = new ArrayList<>();

        @Override
        public Review save(Review review) {
            review.setId((long) reviews.size() + 1);
            reviews.add(review);
            return review;
        }

        @Override
        public boolean existsByOrderAuthorAndType(Long orderId, Long authorId, ReviewType type) {
            return reviews.stream().anyMatch(review ->
                    review.getOrder().getId().equals(orderId)
                            && review.getAuthor().getId().equals(authorId)
                            && review.getType() == type);
        }

        @Override
        public List<Review> findByOrderId(Long orderId) {
            return reviews.stream()
                    .filter(review -> review.getOrder().getId().equals(orderId))
                    .toList();
        }
    }
}
