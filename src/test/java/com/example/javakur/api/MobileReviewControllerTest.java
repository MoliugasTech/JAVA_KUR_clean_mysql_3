package com.example.javakur.api;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.ReviewType;
import com.example.javakur.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MobileReviewController.class)
@Import({ApiSecurityConfig.class, ApiTokenAuthenticationFilter.class,
        ApiTokenService.class, ApiSecurityErrorWriter.class})
class MobileReviewControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ApiTokenService tokenService;
    @MockBean private MobileReviewService service;

    @Test
    void clientCanReviewRestaurantAndDriver() throws Exception {
        when(service.reviewRestaurant(eq(7L), eq(12L), any()))
                .thenReturn(response(ReviewType.RESTAURANT));
        when(service.reviewDriver(eq(7L), eq(12L), any()))
                .thenReturn(response(ReviewType.DRIVER));

        mockMvc.perform(post("/api/client/orders/12/reviews/restaurant")
                        .header("Authorization", bearer(client(7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Puiku\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("RESTAURANT"));

        mockMvc.perform(post("/api/client/orders/12/reviews/driver")
                        .header("Authorization", bearer(client(7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":4,\"comment\":\"Greitai\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DRIVER"));
    }

    @Test
    void driverCanReviewClient() throws Exception {
        when(service.reviewClient(eq(9L), eq(12L), any()))
                .thenReturn(response(ReviewType.CLIENT));

        mockMvc.perform(post("/api/driver/orders/12/reviews/client")
                        .header("Authorization", bearer(driver(9L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Viskas gerai\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CLIENT"));
    }

    @Test
    void ratingOutsideScaleIsRejected() throws Exception {
        mockMvc.perform(post("/api/client/orders/12/reviews/restaurant")
                        .header("Authorization", bearer(client(7L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":6,\"comment\":\"Blogas balas\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rolesCannotUseEachOthersReviewEndpoints() throws Exception {
        mockMvc.perform(get("/api/driver/orders/12/reviews")
                        .header("Authorization", bearer(client(7L))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/client/orders/12/reviews")
                        .header("Authorization", bearer(driver(9L))))
                .andExpect(status().isForbidden());
    }

    private MobileReviewDtos.ReviewResponse response(ReviewType type) {
        return new MobileReviewDtos.ReviewResponse(20L, type, 5, "Gerai",
                LocalDateTime.of(2026, 8, 26, 20, 0));
    }

    private String bearer(com.example.javakur.model.User user) {
        return "Bearer " + tokenService.issue(user).value();
    }

    private Client client(Long id) {
        Client client = new Client("client", "hash", "C", "Client",
                "client@example.com", "Vilnius");
        client.setId(id);
        return client;
    }

    private Driver driver(Long id) {
        Driver driver = new Driver("driver", "hash", "D", "Driver",
                "driver@example.com", VehicleType.CAR);
        driver.setId(id);
        return driver;
    }
}
