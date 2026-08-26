package com.example.javakur.api;

import com.example.javakur.model.Admin;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.MenuItem;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientCatalogController.class)
@Import({ApiSecurityConfig.class, ApiTokenAuthenticationFilter.class,
        ApiTokenService.class, ApiSecurityErrorWriter.class})
class ClientCatalogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiTokenService tokenService;

    @MockBean
    private ClientCatalogService service;

    @Test
    void clientCanListRestaurantsWithRequiredInformation() throws Exception {
        when(service.listRestaurants()).thenReturn(List.of(restaurant(4L)));

        mockMvc.perform(get("/api/catalog/restaurants")
                        .header("Authorization", bearer(client(7L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].name").value("Skanu"))
                .andExpect(jsonPath("$[0].description").value("Dienos pietūs"))
                .andExpect(jsonPath("$[0].address").value("Vilnius"))
                .andExpect(jsonPath("$[0].type").value("OTHER"))
                .andExpect(jsonPath("$[0].openingTime").value("09:00:00"))
                .andExpect(jsonPath("$[0].closingTime").value("21:00:00"))
                .andExpect(jsonPath("$[0].owner").doesNotExist());
    }

    @Test
    void clientCanListAvailableMenuItemsWithDescriptions() throws Exception {
        Restaurant restaurant = restaurant(4L);
        when(service.listAvailableMenuItems(4L)).thenReturn(List.of(menuItem(8L, restaurant)));

        mockMvc.perform(get("/api/catalog/restaurants/4/menu-items")
                        .header("Authorization", bearer(client(7L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].name").value("Sriuba"))
                .andExpect(jsonPath("$[0].description").value("Daržovių sriuba"))
                .andExpect(jsonPath("$[0].price").value(4.50))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].restaurant").doesNotExist());
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/catalog/restaurants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void driverCannotUseClientCatalog() throws Exception {
        mockMvc.perform(get("/api/catalog/restaurants")
                        .header("Authorization", bearer(driver(9L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanUseClientCatalog() throws Exception {
        when(service.listRestaurants()).thenReturn(List.of());

        mockMvc.perform(get("/api/catalog/restaurants")
                        .header("Authorization", bearer(admin(1L))))
                .andExpect(status().isOk());
    }

    private String bearer(com.example.javakur.model.User user) {
        return "Bearer " + tokenService.issue(user).value();
    }

    private Restaurant restaurant(Long id) {
        RestaurantOwner owner = new RestaurantOwner("owner", "hash", "R", "Owner", "owner@example.com");
        owner.setId(3L);
        Restaurant restaurant = new Restaurant("Skanu", "Dienos pietūs", "Vilnius",
                RestaurantType.OTHER, LocalTime.of(9, 0), LocalTime.of(21, 0), owner);
        restaurant.setId(id);
        return restaurant;
    }

    private MenuItem menuItem(Long id, Restaurant restaurant) {
        MenuItem item = new MenuItem("Sriuba", "Daržovių sriuba", new BigDecimal("4.50"), restaurant);
        item.setId(id);
        return item;
    }

    private Client client(Long id) {
        Client client = new Client("client", "hash", "C", "Client", "client@example.com", "Vilnius");
        client.setId(id);
        return client;
    }

    private Driver driver(Long id) {
        Driver driver = new Driver("driver", "hash", "D", "Driver", "driver@example.com", VehicleType.CAR);
        driver.setId(id);
        return driver;
    }

    private Admin admin(Long id) {
        Admin admin = new Admin("admin", "hash", "A", "Admin", "admin@example.com");
        admin.setId(id);
        return admin;
    }
}
