package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.User;
import com.example.javakur.model.VehicleType;
import com.example.javakur.repository.OrderRepository;

import java.util.List;

public class DriverConsoleService {
    private final UserService userService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public DriverConsoleService(UserService userService, OrderService orderService,
                                OrderRepository orderRepository) {
        this.userService = userService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    public Driver register(String username, String password, String name, String surname,
                           String email, VehicleType vehicleType) {
        return userService.createDriver(username, password, name, surname, email, vehicleType);
    }

    public List<FoodOrder> listReadyOrders() {
        return orderRepository.findReadyForPickup();
    }

    public FoodOrder pickUpOrder(Long driverId, Long orderId) {
        Driver driver = requireDriver(driverId);
        FoodOrder order = orderRepository.findReadyUnassignedById(orderId)
                .orElseThrow(() -> new ValidationException("Užsakymo nebegalima paimti pristatymui"));
        return orderService.assignDriver(order, driver);
    }

    public List<FoodOrder> listActiveDeliveries(Long driverId) {
        requireDriver(driverId);
        return orderRepository.findActiveByDriverId(driverId);
    }

    public FoodOrder changeDeliveryStatus(Long driverId, Long orderId, OrderStatus status) {
        if (status != OrderStatus.DELIVERING && status != OrderStatus.COMPLETED) {
            throw new ValidationException("Vairuotojas gali nustatyti tik DELIVERING arba COMPLETED");
        }
        FoodOrder order = orderRepository.findOwnedByDriver(orderId, driverId)
                .orElseThrow(() -> new ValidationException("Užsakymas nepriskirtas šiam vairuotojui"));
        return orderService.changeStatus(order, status);
    }

    private Driver requireDriver(Long driverId) {
        User user = userService.findById(driverId)
                .orElseThrow(() -> new ValidationException("Vairuotojas nerastas"));
        if (!(user instanceof Driver driver)) {
            throw new ValidationException("Naudotojas nėra vairuotojas");
        }
        return driver;
    }
}
