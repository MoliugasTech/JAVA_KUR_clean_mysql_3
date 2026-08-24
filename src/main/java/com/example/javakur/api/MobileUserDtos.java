package com.example.javakur.api;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.VehicleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class MobileUserDtos {
    private MobileUserDtos() {
    }

    public record ClientCreateRequest(
            @NotBlank(message = "Vartotojo vardas yra privalomas") String username,
            @NotBlank(message = "Slaptažodis yra privalomas")
            @Size(min = 8, message = "Slaptažodį turi sudaryti bent 8 simboliai") String password,
            @NotBlank(message = "Vardas yra privalomas") String name,
            @NotBlank(message = "Pavardė yra privaloma") String surname,
            @NotBlank(message = "El. paštas yra privalomas")
            @Email(message = "El. pašto formatas neteisingas") String email,
            String phoneNumber,
            @NotBlank(message = "Pristatymo adresas yra privalomas") String deliveryAddress) {
    }

    public record ClientUpdateRequest(
            @NotBlank(message = "Vartotojo vardas yra privalomas") String username,
            @Size(min = 8, message = "Naują slaptažodį turi sudaryti bent 8 simboliai") String password,
            @NotBlank(message = "Vardas yra privalomas") String name,
            @NotBlank(message = "Pavardė yra privaloma") String surname,
            @NotBlank(message = "El. paštas yra privalomas")
            @Email(message = "El. pašto formatas neteisingas") String email,
            String phoneNumber,
            @NotBlank(message = "Pristatymo adresas yra privalomas") String deliveryAddress) {
    }

    public record DriverCreateRequest(
            @NotBlank(message = "Vartotojo vardas yra privalomas") String username,
            @NotBlank(message = "Slaptažodis yra privalomas")
            @Size(min = 8, message = "Slaptažodį turi sudaryti bent 8 simboliai") String password,
            @NotBlank(message = "Vardas yra privalomas") String name,
            @NotBlank(message = "Pavardė yra privaloma") String surname,
            @NotBlank(message = "El. paštas yra privalomas")
            @Email(message = "El. pašto formatas neteisingas") String email,
            String phoneNumber,
            @NotNull(message = "Transporto tipas yra privalomas") VehicleType vehicleType) {
    }

    public record DriverUpdateRequest(
            @NotBlank(message = "Vartotojo vardas yra privalomas") String username,
            @Size(min = 8, message = "Naują slaptažodį turi sudaryti bent 8 simboliai") String password,
            @NotBlank(message = "Vardas yra privalomas") String name,
            @NotBlank(message = "Pavardė yra privaloma") String surname,
            @NotBlank(message = "El. paštas yra privalomas")
            @Email(message = "El. pašto formatas neteisingas") String email,
            String phoneNumber,
            @NotNull(message = "Transporto tipas yra privalomas") VehicleType vehicleType) {
    }

    public record ClientResponse(Long id, String username, String name, String surname,
                                 String email, String phoneNumber, String deliveryAddress, int bonusPoints,
                                 boolean active) {
        public static ClientResponse from(Client client) {
            return new ClientResponse(client.getId(), client.getUsername(), client.getName(),
                    client.getSurname(), client.getEmail(), client.getPhoneNumber(), client.getDeliveryAddress(),
                    client.getBonusPoints(), client.isActive());
        }
    }

    public record DriverResponse(Long id, String username, String name, String surname,
                                 String email, String phoneNumber, VehicleType vehicleType, boolean active) {
        public static DriverResponse from(Driver driver) {
            return new DriverResponse(driver.getId(), driver.getUsername(), driver.getName(),
                    driver.getSurname(), driver.getEmail(), driver.getPhoneNumber(), driver.getVehicleType(),
                    driver.isActive());
        }
    }
}
