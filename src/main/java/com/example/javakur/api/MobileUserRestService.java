package com.example.javakur.api;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.User;
import com.example.javakur.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.javakur.api.MobileUserDtos.ClientCreateRequest;
import static com.example.javakur.api.MobileUserDtos.ClientUpdateRequest;
import static com.example.javakur.api.MobileUserDtos.DriverCreateRequest;
import static com.example.javakur.api.MobileUserDtos.DriverUpdateRequest;

@Service
@Transactional(readOnly = true)
public class MobileUserRestService {
    private final MobileUserRepository repository;

    public MobileUserRestService(MobileUserRepository repository) {
        this.repository = repository;
    }

    public List<Client> listClients() {
        return repository.findActiveClients();
    }

    public Client getClient(Long id) {
        return repository.findActiveClientById(id)
                .orElseThrow(() -> new ApiResourceNotFoundException("Klientas nerastas"));
    }

    @Transactional
    public Client createClient(ClientCreateRequest request) {
        requireUnique(request.username(), request.email(), null);
        Client client = new Client(trim(request.username()), PasswordUtil.hashPassword(request.password()),
                trim(request.name()), trim(request.surname()), trim(request.email()),
                trim(request.deliveryAddress()));
        client.setPhoneNumber(trim(request.phoneNumber()));
        return (Client) repository.save(client);
    }

    @Transactional
    public Client updateClient(Long id, ClientUpdateRequest request) {
        Client client = getClient(id);
        requireUnique(request.username(), request.email(), id);
        updateCommon(client, request.username(), request.password(), request.name(),
                request.surname(), request.email(), request.phoneNumber());
        client.setDeliveryAddress(trim(request.deliveryAddress()));
        return (Client) repository.save(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = repository.findAnyClientById(id)
                .orElseThrow(() -> new ApiResourceNotFoundException("Klientas nerastas"));
        client.setActive(false);
        repository.save(client);
    }

    public List<Driver> listDrivers() {
        return repository.findActiveDrivers();
    }

    public Driver getDriver(Long id) {
        return repository.findActiveDriverById(id)
                .orElseThrow(() -> new ApiResourceNotFoundException("Vairuotojas nerastas"));
    }

    @Transactional
    public Driver createDriver(DriverCreateRequest request) {
        requireUnique(request.username(), request.email(), null);
        Driver driver = new Driver(trim(request.username()), PasswordUtil.hashPassword(request.password()),
                trim(request.name()), trim(request.surname()), trim(request.email()),
                request.vehicleType());
        driver.setPhoneNumber(trim(request.phoneNumber()));
        return (Driver) repository.save(driver);
    }

    @Transactional
    public Driver updateDriver(Long id, DriverUpdateRequest request) {
        Driver driver = getDriver(id);
        requireUnique(request.username(), request.email(), id);
        updateCommon(driver, request.username(), request.password(), request.name(),
                request.surname(), request.email(), request.phoneNumber());
        driver.setVehicleType(request.vehicleType());
        return (Driver) repository.save(driver);
    }

    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = repository.findAnyDriverById(id)
                .orElseThrow(() -> new ApiResourceNotFoundException("Vairuotojas nerastas"));
        driver.setActive(false);
        repository.save(driver);
    }

    private void updateCommon(User user, String username, String password, String name,
                              String surname, String email, String phoneNumber) {
        user.setUsername(trim(username));
        user.setName(trim(name));
        user.setSurname(trim(surname));
        user.setEmail(trim(email));
        user.setPhoneNumber(trim(phoneNumber));
        if (password != null && !password.isBlank()) {
            user.setPasswordHash(PasswordUtil.hashPassword(password));
        }
    }

    private void requireUnique(String username, String email, Long excludedId) {
        repository.findByUsername(trim(username))
                .filter(user -> !user.getId().equals(excludedId))
                .ifPresent(user -> {
                    throw new ValidationException("Vartotojo vardas jau naudojamas");
                });
        repository.findByEmail(trim(email))
                .filter(user -> !user.getId().equals(excludedId))
                .ifPresent(user -> {
                    throw new ValidationException("El. paštas jau naudojamas");
                });
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
