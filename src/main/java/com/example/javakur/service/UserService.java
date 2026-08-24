package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.VehicleType;
import com.example.javakur.model.User;
import com.example.javakur.repository.UserRepository;
import com.example.javakur.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RestaurantOwner createRestaurantOwner(String username, String plainPassword, String name,
                                                  String surname, String email) {
        validateCommonUserFields(username, plainPassword, name, surname, email);
        validateUniqueCredentials(username, email);
        RestaurantOwner owner = new RestaurantOwner(
                username.trim(), PasswordUtil.hashPassword(plainPassword), name.trim(), surname.trim(), email.trim());
        return (RestaurantOwner) userRepository.save(owner);
    }

    public Client createClient(String username, String plainPassword, String name,
                               String surname, String email, String deliveryAddress) {
        validateCommonUserFields(username, plainPassword, name, surname, email);
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new ValidationException("Pristatymo adresas negali būti tuščias");
        }
        validateUniqueCredentials(username, email);

        Client client = new Client(
                username.trim(), PasswordUtil.hashPassword(plainPassword), name.trim(), surname.trim(),
                email.trim(), deliveryAddress.trim());
        return (Client) userRepository.save(client);
    }

    public Driver createDriver(String username, String plainPassword, String name,
                               String surname, String email, VehicleType vehicleType) {
        validateCommonUserFields(username, plainPassword, name, surname, email);
        if (vehicleType == null) {
            throw new ValidationException("Transporto tipas yra privalomas");
        }
        validateUniqueCredentials(username, email);

        Driver driver = new Driver(
                username.trim(), PasswordUtil.hashPassword(plainPassword), name.trim(), surname.trim(),
                email.trim(), vehicleType);
        return (Driver) userRepository.save(driver);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        if (id == null) throw new ValidationException("Naudotojo ID yra privalomas");
        return userRepository.findById(id);
    }

    private void validateCommonUserFields(String username, String password, String name, String surname, String email) {
        if (username == null || username.isBlank()) throw new ValidationException("Vartotojo vardas negali būti tuščias");
        if (password == null || password.length() < 8) throw new ValidationException("Slaptažodį turi sudaryti bent 8 simboliai");
        if (name == null || name.isBlank()) throw new ValidationException("Vardas negali būti tuščias");
        if (surname == null || surname.isBlank()) throw new ValidationException("Pavardė negali būti tuščia");
        if (email == null || email.isBlank()) throw new ValidationException("El. paštas negali būti tuščias");
    }

    private void validateUniqueCredentials(String username, String email) {
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new ValidationException("Vartotojo vardas jau naudojamas");
        }
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            throw new ValidationException("El. paštas jau naudojamas");
        }
    }
}
