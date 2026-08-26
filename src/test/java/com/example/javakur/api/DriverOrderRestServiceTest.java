package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.VehicleType;
import com.example.javakur.service.LoyaltyPointService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverOrderRestServiceTest {
    @Mock private MobileUserRepository userRepository;
    @Mock private DriverOrderRepository orderRepository;
    @Mock private ClientOrderMessageRepository messageRepository;
    @Mock private LoyaltyPointService loyaltyPointService;
    @InjectMocks private DriverOrderRestService service;

    @Test
    void driverSeesReadyUnassignedOrders() {
        Driver driver = driver(9L);
        FoodOrder ready = order(12L, OrderStatus.READY_FOR_PICKUP);
        when(userRepository.findActiveDriverById(9L)).thenReturn(Optional.of(driver));
        when(orderRepository
                .findByStatusAndDriverIsNullAndDriverAssignmentAllowedTrueOrderByCreatedAtAsc(
                        OrderStatus.READY_FOR_PICKUP)).thenReturn(List.of(ready));

        var result = service.available(9L);

        assertEquals(1, result.size());
        assertEquals("Vilnius, Gatvė 1", result.get(0).deliveryAddress());
    }

    @Test
    void driverCanAtomicallyAcceptReadyOrder() {
        Driver driver = driver(9L);
        FoodOrder ready = order(12L, OrderStatus.READY_FOR_PICKUP);
        when(userRepository.findActiveDriverById(9L)).thenReturn(Optional.of(driver));
        when(orderRepository.findForUpdateById(12L)).thenReturn(Optional.of(ready));
        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.accept(9L, 12L);

        assertEquals(OrderStatus.PICKED_UP, result.status());
        assertEquals(driver, ready.getDriver());
        verify(orderRepository).save(ready);
    }

    @Test
    void driverCannotAcceptOrderBlockedByRestaurant() {
        Driver driver = driver(9L);
        FoodOrder ready = order(12L, OrderStatus.READY_FOR_PICKUP);
        ready.setDriverAssignmentAllowed(false);
        when(userRepository.findActiveDriverById(9L)).thenReturn(Optional.of(driver));
        when(orderRepository.findForUpdateById(12L)).thenReturn(Optional.of(ready));

        assertThrows(ValidationException.class, () -> service.accept(9L, 12L));
        verify(orderRepository, never()).save(any(FoodOrder.class));
    }

    @Test
    void secondDriverCannotTakeAlreadyAssignedOrder() {
        Driver driver = driver(9L);
        FoodOrder order = order(12L, OrderStatus.PICKED_UP);
        order.setDriver(driver(10L));
        when(userRepository.findActiveDriverById(9L)).thenReturn(Optional.of(driver));
        when(orderRepository.findForUpdateById(12L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> service.accept(9L, 12L));
    }

    @Test
    void driverFollowsDeliveryStatusSequenceAndAwardsPointsOnlyOnCompletion() {
        Driver driver = driver(9L);
        FoodOrder order = order(12L, OrderStatus.PICKED_UP);
        order.setDriver(driver);
        when(userRepository.findActiveDriverById(9L)).thenReturn(Optional.of(driver));
        when(orderRepository.findByIdAndDriverId(12L, 9L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var delivering = service.changeStatus(9L, 12L, OrderStatus.DELIVERING);
        assertEquals(OrderStatus.DELIVERING, delivering.status());
        verify(loyaltyPointService, never()).awardFor(order);

        var completed = service.changeStatus(9L, 12L, OrderStatus.COMPLETED);
        assertEquals(OrderStatus.COMPLETED, completed.status());
        verify(loyaltyPointService).awardFor(order);
    }

    @Test
    void driverCannotSkipDeliveryStatus() {
        Driver driver = driver(9L);
        FoodOrder order = order(12L, OrderStatus.PICKED_UP);
        order.setDriver(driver);
        when(userRepository.findActiveDriverById(9L)).thenReturn(Optional.of(driver));
        when(orderRepository.findByIdAndDriverId(12L, 9L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class,
                () -> service.changeStatus(9L, 12L, OrderStatus.COMPLETED));
    }

    private FoodOrder order(Long id, OrderStatus status) {
        Client client = new Client("client", "hash", "C", "Client",
                "client@example.com", "Vilnius, Gatvė 1");
        client.setId(7L);
        RestaurantOwner owner = new RestaurantOwner(
                "owner", "hash", "R", "Owner", "owner@example.com");
        owner.setId(3L);
        Restaurant restaurant = new Restaurant("Skanu", "", "Restorano g. 2",
                RestaurantType.OTHER, LocalTime.of(9, 0), LocalTime.of(21, 0), owner);
        restaurant.setId(4L);
        FoodOrder order = new FoodOrder(client, restaurant);
        order.setId(id);
        order.setStatus(status);
        order.setTotalPrice(new BigDecimal("9.00"));
        return order;
    }

    private Driver driver(Long id) {
        Driver driver = new Driver("driver" + id, "hash", "D", "Driver",
                "driver" + id + "@example.com", VehicleType.CAR);
        driver.setId(id);
        return driver;
    }
}
