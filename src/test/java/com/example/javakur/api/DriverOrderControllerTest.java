package com.example.javakur.api;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.OrderStatus;
import com.example.javakur.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriverOrderController.class)
@Import({ApiSecurityConfig.class, ApiTokenAuthenticationFilter.class,
        ApiTokenService.class, ApiSecurityErrorWriter.class})
class DriverOrderControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ApiTokenService tokenService;
    @MockBean private DriverOrderRestService service;

    @Test
    void driverCanListAvailableAndOwnOrders() throws Exception {
        when(service.available(9L)).thenReturn(List.of(summary(OrderStatus.READY_FOR_PICKUP)));
        when(service.mine(9L)).thenReturn(List.of(summary(OrderStatus.PICKED_UP)));

        mockMvc.perform(get("/api/driver/orders/available")
                        .header("Authorization", bearer(driver(9L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deliveryAddress").value("Vilnius, Gatvė 1"));

        mockMvc.perform(get("/api/driver/orders/mine")
                        .header("Authorization", bearer(driver(9L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PICKED_UP"));
    }

    @Test
    void driverCanAcceptAndAdvanceOrder() throws Exception {
        when(service.accept(9L, 12L)).thenReturn(detail(OrderStatus.PICKED_UP));
        when(service.changeStatus(9L, 12L, OrderStatus.DELIVERING))
                .thenReturn(detail(OrderStatus.DELIVERING));

        mockMvc.perform(post("/api/driver/orders/12/accept")
                        .header("Authorization", bearer(driver(9L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED_UP"));

        mockMvc.perform(put("/api/driver/orders/12/status")
                        .header("Authorization", bearer(driver(9L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELIVERING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERING"));
    }

    @Test
    void clientCannotUseDriverEndpoints() throws Exception {
        mockMvc.perform(get("/api/driver/orders/available")
                        .header("Authorization", bearer(client(7L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/driver/orders/available"))
                .andExpect(status().isUnauthorized());
    }

    private DriverOrderDtos.OrderSummaryResponse summary(OrderStatus status) {
        return new DriverOrderDtos.OrderSummaryResponse(12L, status, 4L,
                "Skanu", "Restorano g. 2", 7L, "C Client", "Vilnius, Gatvė 1",
                LocalDateTime.of(2026, 8, 26, 18, 0), new BigDecimal("9.00"));
    }

    private DriverOrderDtos.OrderDetailResponse detail(OrderStatus status) {
        OrderStatus next = status == OrderStatus.PICKED_UP ? OrderStatus.DELIVERING
                : status == OrderStatus.DELIVERING ? OrderStatus.COMPLETED : null;
        return new DriverOrderDtos.OrderDetailResponse(12L, status, 4L, "Skanu",
                "Restorano g. 2", 7L, "C Client", "Vilnius, Gatvė 1",
                LocalDateTime.of(2026, 8, 26, 18, 0), null,
                new BigDecimal("9.00"), next, true, List.of());
    }

    private String bearer(com.example.javakur.model.User user) {
        return "Bearer " + tokenService.issue(user).value();
    }

    private Driver driver(Long id) {
        Driver driver = new Driver("driver", "hash", "D", "Driver",
                "driver@example.com", VehicleType.CAR);
        driver.setId(id);
        return driver;
    }

    private Client client(Long id) {
        Client client = new Client("client", "hash", "C", "Client",
                "client@example.com", "Vilnius");
        client.setId(id);
        return client;
    }
}
