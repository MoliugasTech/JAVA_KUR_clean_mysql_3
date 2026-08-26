package com.example.javakur.api;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.FoodOrder;
import com.example.javakur.model.Restaurant;
import com.example.javakur.model.RestaurantOwner;
import com.example.javakur.model.RestaurantType;
import com.example.javakur.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientOrderController.class)
@Import({ApiSecurityConfig.class, ApiTokenAuthenticationFilter.class,
        ApiTokenService.class, ApiSecurityErrorWriter.class})
class ClientOrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiTokenService tokenService;

    @MockBean
    private ClientOrderRestService service;

    @Test
    void clientCanCreateOrder() throws Exception {
        when(service.create(eq(7L), any(ClientOrderDtos.CreateOrderRequest.class)))
                .thenReturn(createdOrder());

        mockMvc.perform(post("/api/client/orders")
                        .header("Authorization", bearer(client(7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"restaurantId":4,
                                 "items":[{"menuItemId":8,"quantity":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/client/orders/12"))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.restaurantId").value(4))
                .andExpect(jsonPath("$.subtotal").value(9.00))
                .andExpect(jsonPath("$.totalPrice").value(9.00));
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/client/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"restaurantId":4,
                                 "items":[{"menuItemId":8,"quantity":2}]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void driverCannotCreateClientOrder() throws Exception {
        mockMvc.perform(post("/api/client/orders")
                        .header("Authorization", bearer(driver(9L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"restaurantId":4,
                                 "items":[{"menuItemId":8,"quantity":2}]}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void emptyCartIsRejectedBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/client/orders")
                        .header("Authorization", bearer(client(7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"restaurantId":4,"items":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    private FoodOrder createdOrder() {
        FoodOrder order = new FoodOrder(client(7L), restaurant(4L));
        order.setId(12L);
        order.setSubtotal(new BigDecimal("9.00"));
        order.setPriceMultiplier(new BigDecimal("1.000"));
        order.setTotalPrice(new BigDecimal("9.00"));
        return order;
    }

    private String bearer(com.example.javakur.model.User user) {
        return "Bearer " + tokenService.issue(user).value();
    }

    private Restaurant restaurant(Long id) {
        RestaurantOwner owner = new RestaurantOwner(
                "owner", "hash", "R", "Owner", "owner@example.com");
        owner.setId(3L);
        Restaurant restaurant = new Restaurant(
                "Skanu", "", "Vilnius", RestaurantType.OTHER,
                LocalTime.of(9, 0), LocalTime.of(21, 0), owner);
        restaurant.setId(id);
        return restaurant;
    }

    private Client client(Long id) {
        Client client = new Client(
                "client", "hash", "C", "Client", "client@example.com", "Vilnius");
        client.setId(id);
        return client;
    }

    private Driver driver(Long id) {
        Driver driver = new Driver(
                "driver", "hash", "D", "Driver", "driver@example.com", VehicleType.CAR);
        driver.setId(id);
        return driver;
    }
}
