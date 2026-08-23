package com.example.javakur;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.OrderRepository;
import com.example.javakur.service.OrderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderLifecycleServiceTest {

    private final OrderService service = new OrderService(new InMemoryOrderRepository());

    @Test
    void completesAllowedOrderLifecycle() {
        FoodOrder order = persistedOrder();
        Driver driver = persistedDriver();

        service.changeStatus(order, OrderStatus.CONFIRMED);
        service.changeStatus(order, OrderStatus.PREPARING);
        service.changeStatus(order, OrderStatus.READY_FOR_PICKUP);
        service.assignDriver(order, driver);
        service.changeStatus(order, OrderStatus.DELIVERING);
        service.changeStatus(order, OrderStatus.COMPLETED);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertSame(driver, order.getDriver());
        assertNotNull(order.getCompletedAt());
    }

    @Test
    void rejectsSkippedStatus() {
        FoodOrder order = persistedOrder();

        assertThrows(ValidationException.class,
                () -> service.changeStatus(order, OrderStatus.DELIVERING));
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void driverCanOnlyPickUpReadyOrder() {
        FoodOrder order = persistedOrder();

        assertThrows(ValidationException.class,
                () -> service.assignDriver(order, persistedDriver()));
    }

    @Test
    void orderCanOnlyBePickedUpOnce() {
        FoodOrder order = persistedOrder();
        Driver driver = persistedDriver();
        order.setStatus(OrderStatus.READY_FOR_PICKUP);

        service.assignDriver(order, driver);

        assertThrows(ValidationException.class,
                () -> service.assignDriver(order, persistedDriver()));
        assertSame(driver, order.getDriver());
    }

    @Test
    void cancelledOrderIsTerminal() {
        FoodOrder order = persistedOrder();

        service.changeStatus(order, OrderStatus.CANCELLED);

        assertThrows(ValidationException.class,
                () -> service.changeStatus(order, OrderStatus.CONFIRMED));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    private FoodOrder persistedOrder() {
        FoodOrder order = new FoodOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);
        return order;
    }

    private Driver persistedDriver() {
        Driver driver = new Driver(
                "driver", "hash", "Test", "Driver", "driver@example.com", VehicleType.CAR);
        driver.setId(2L);
        return driver;
    }

    private static class InMemoryOrderRepository extends OrderRepository {
        @Override
        public FoodOrder update(FoodOrder order) {
            return order;
        }
    }
}
