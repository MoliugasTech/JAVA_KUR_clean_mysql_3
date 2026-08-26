package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.Review;
import com.example.javakur.model.ReviewType;
import com.example.javakur.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileReviewServiceTest {
    @Mock private MobileUserRepository userRepository;
    @Mock private ClientOrderRepository clientOrderRepository;
    @Mock private DriverOrderRepository driverOrderRepository;
    @Mock private MobileReviewRepository reviewRepository;
    @InjectMocks private MobileReviewService service;

    @Test
    void clientCanReviewRestaurantAndDriverAfterCompletion() {
        Client client = client(7L);
        Driver driver = driver(9L);
        FoodOrder order = completedOrder(client, driver);
        when(userRepository.findActiveClientById(7L)).thenReturn(Optional.of(client));
        when(clientOrderRepository.findByIdAndClientId(12L, 7L))
                .thenReturn(Optional.of(order));
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new MobileReviewDtos.ReviewRequest(5, "Puiku");
        assertEquals(ReviewType.RESTAURANT,
                service.reviewRestaurant(7L, 12L, request).type());
        assertEquals(ReviewType.DRIVER,
                service.reviewDriver(7L, 12L, request).type());
    }

    @Test
    void assignedDriverCanReviewClient() {
        Client client = client(7L);
        Driver driver = driver(9L);
        FoodOrder order = completedOrder(client, driver);
        when(userRepository.findActiveDriverById(9L)).thenReturn(Optional.of(driver));
        when(driverOrderRepository.findByIdAndDriverId(12L, 9L))
                .thenReturn(Optional.of(order));
        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.reviewClient(9L, 12L,
                new MobileReviewDtos.ReviewRequest(4, "Tvarkingas klientas"));

        assertEquals(ReviewType.CLIENT, result.type());
        assertEquals(4, result.rating());
    }

    @Test
    void unfinishedOrderCannotBeReviewed() {
        Client client = client(7L);
        FoodOrder order = completedOrder(client, driver(9L));
        order.setStatus(OrderStatus.DELIVERING);
        when(userRepository.findActiveClientById(7L)).thenReturn(Optional.of(client));
        when(clientOrderRepository.findByIdAndClientId(12L, 7L))
                .thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> service.reviewRestaurant(
                7L, 12L, new MobileReviewDtos.ReviewRequest(5, "")));
    }

    @Test
    void duplicateReviewTypeIsRejected() {
        Client client = client(7L);
        FoodOrder order = completedOrder(client, driver(9L));
        when(userRepository.findActiveClientById(7L)).thenReturn(Optional.of(client));
        when(clientOrderRepository.findByIdAndClientId(12L, 7L))
                .thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderIdAndAuthorIdAndType(
                12L, 7L, ReviewType.RESTAURANT)).thenReturn(true);

        assertThrows(ValidationException.class, () -> service.reviewRestaurant(
                7L, 12L, new MobileReviewDtos.ReviewRequest(5, "Dar kartą")));
    }

    private FoodOrder completedOrder(Client client, Driver driver) {
        RestaurantOwner owner = new RestaurantOwner(
                "owner", "hash", "R", "Owner", "owner@example.com");
        owner.setId(3L);
        Restaurant restaurant = new Restaurant("Skanu", "", "Restorano g. 2",
                RestaurantType.OTHER, LocalTime.of(9, 0), LocalTime.of(21, 0), owner);
        restaurant.setId(4L);
        FoodOrder order = new FoodOrder(client, restaurant);
        order.setId(12L);
        order.setDriver(driver);
        order.setStatus(OrderStatus.COMPLETED);
        return order;
    }

    private Client client(Long id) {
        Client client = new Client("client", "hash", "C", "Client",
                "client@example.com", "Vilnius");
        client.setId(id);
        return client;
    }

    private Driver driver(Long id) {
        Driver driver = new Driver("driver", "hash", "D", "Driver",
                "driver@example.com", VehicleType.CAR);
        driver.setId(id);
        return driver;
    }
}
