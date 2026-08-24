package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.VehicleType;
import com.example.javakur.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.javakur.api.MobileUserDtos.ClientCreateRequest;
import static com.example.javakur.api.MobileUserDtos.DriverUpdateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileUserRestServiceTest {
    @Mock
    private MobileUserRepository repository;

    @InjectMocks
    private MobileUserRestService service;

    @Test
    void createsClientWithHashedPassword() {
        when(repository.findByUsername("mobile_client")).thenReturn(Optional.empty());
        when(repository.findByEmail("client@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            client.setId(41L);
            return client;
        });

        Client created = service.createClient(new ClientCreateRequest(
                " mobile_client ", "password123", "Asta", "Astrauskaitė",
                " client@example.com ", "+37060000001", "Vilnius"));

        assertEquals(41L, created.getId());
        assertEquals("mobile_client", created.getUsername());
        assertEquals("client@example.com", created.getEmail());
        assertTrue(PasswordUtil.verifyPassword("password123", created.getPasswordHash()));
        assertFalse(created.getPasswordHash().contains("password123"));
    }

    @Test
    void updatesDriverWithoutReplacingPasswordWhenItIsBlank() {
        Driver driver = driver(8L);
        String originalHash = driver.getPasswordHash();
        when(repository.findActiveDriverById(8L)).thenReturn(Optional.of(driver));
        when(repository.findByUsername("driver_new")).thenReturn(Optional.empty());
        when(repository.findByEmail("driver.new@example.com")).thenReturn(Optional.empty());
        when(repository.save(driver)).thenReturn(driver);

        Driver updated = service.updateDriver(8L, new DriverUpdateRequest(
                "driver_new", "", "Darius", "Dargis", "driver.new@example.com",
                "+37060000002", VehicleType.BICYCLE));

        assertEquals("driver_new", updated.getUsername());
        assertEquals(VehicleType.BICYCLE, updated.getVehicleType());
        assertEquals(originalHash, updated.getPasswordHash());
    }

    @Test
    void softDeletesClient() {
        Client client = client(11L);
        when(repository.findAnyClientById(11L)).thenReturn(Optional.of(client));

        service.deleteClient(11L);

        assertFalse(client.isActive());
        verify(repository).save(client);
    }

    @Test
    void rejectsDuplicateUsername() {
        Client existing = client(2L);
        when(repository.findByUsername("mobile_client")).thenReturn(Optional.of(existing));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.createClient(new ClientCreateRequest(
                        "mobile_client", "password123", "Asta", "Astrauskaitė",
                        "other@example.com", null, "Vilnius")));

        assertEquals("Vartotojo vardas jau naudojamas", exception.getMessage());
    }

    @Test
    void reportsMissingDriver() {
        when(repository.findActiveDriverById(999L)).thenReturn(Optional.empty());

        ApiResourceNotFoundException exception = assertThrows(ApiResourceNotFoundException.class,
                () -> service.getDriver(999L));

        assertEquals("Vairuotojas nerastas", exception.getMessage());
    }

    private Client client(Long id) {
        Client client = new Client("mobile_client", "hash", "Asta", "Astrauskaitė",
                "client@example.com", "Vilnius");
        client.setId(id);
        return client;
    }

    private Driver driver(Long id) {
        Driver driver = new Driver("driver", "original-hash", "Darius", "Dargis",
                "driver@example.com", VehicleType.CAR);
        driver.setId(id);
        return driver;
    }
}
