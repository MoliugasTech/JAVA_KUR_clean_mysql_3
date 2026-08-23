package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.User;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.service.DriverConsoleService;
import com.example.javakur.service.OrderService;
import com.example.javakur.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DriverConsoleServiceTest {
    private final FakeUserRepository users = new FakeUserRepository();
    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final DriverConsoleService service = new DriverConsoleService(
            new UserService(users), new OrderService(orders), orders);

    private Driver driver;
    private FoodOrder order;

    @BeforeEach
    void setUp() {
        driver = new Driver("driver", "hash", "D", "D", "d@example.com", VehicleType.CAR);
        driver.setId(1L);
        users.user = driver;
        order = new FoodOrder();
        order.setId(2L);
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        orders.order = order;
    }

    @Test
    void listsAndPicksUpReadyOrder() {
        assertEquals(List.of(order), service.listReadyOrders());

        FoodOrder pickedUp = service.pickUpOrder(driver.getId(), order.getId());

        assertEquals(OrderStatus.PICKED_UP, pickedUp.getStatus());
        assertSame(driver, pickedUp.getDriver());
    }

    @Test
    void rejectsSecondPickup() {
        service.pickUpOrder(driver.getId(), order.getId());

        assertThrows(ValidationException.class,
                () -> service.pickUpOrder(driver.getId(), order.getId()));
    }

    @Test
    void driverCompletesOnlyOwnDeliveryInSequence() {
        service.pickUpOrder(driver.getId(), order.getId());

        service.changeDeliveryStatus(driver.getId(), order.getId(), OrderStatus.DELIVERING);
        FoodOrder completed = service.changeDeliveryStatus(
                driver.getId(), order.getId(), OrderStatus.COMPLETED);

        assertEquals(OrderStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void rejectsRestaurantSideStatus() {
        service.pickUpOrder(driver.getId(), order.getId());

        assertThrows(ValidationException.class,
                () -> service.changeDeliveryStatus(driver.getId(), order.getId(), OrderStatus.PREPARING));
    }

    private static class FakeUserRepository extends UserRepository {
        private User user;

        @Override
        public Optional<User> findById(Long id) {
            return user != null && user.getId().equals(id) ? Optional.of(user) : Optional.empty();
        }
    }

    private static class FakeOrderRepository extends OrderRepository {
        private FoodOrder order;

        @Override
        public List<FoodOrder> findReadyForPickup() {
            return isReady() ? List.of(order) : List.of();
        }

        @Override
        public Optional<FoodOrder> findReadyUnassignedById(Long orderId) {
            return isReady() && order.getId().equals(orderId) ? Optional.of(order) : Optional.empty();
        }

        @Override
        public List<FoodOrder> findActiveByDriverId(Long driverId) {
            return order.getDriver() != null && order.getDriver().getId().equals(driverId)
                    && (order.getStatus() == OrderStatus.PICKED_UP
                    || order.getStatus() == OrderStatus.DELIVERING)
                    ? List.of(order) : List.of();
        }

        @Override
        public Optional<FoodOrder> findOwnedByDriver(Long orderId, Long driverId) {
            return order.getId().equals(orderId) && order.getDriver() != null
                    && order.getDriver().getId().equals(driverId)
                    ? Optional.of(order) : Optional.empty();
        }

        @Override
        public FoodOrder update(FoodOrder order) {
            return order;
        }

        private boolean isReady() {
            return order != null && order.getStatus() == OrderStatus.READY_FOR_PICKUP
                    && order.getDriver() == null;
        }
    }
}
