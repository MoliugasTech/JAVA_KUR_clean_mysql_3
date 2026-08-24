package com.example.javakur.api;

import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.example.javakur.api.MobileUserDtos.ClientCreateRequest;
import static com.example.javakur.api.MobileUserDtos.ClientUpdateRequest;
import static com.example.javakur.api.MobileUserDtos.DriverCreateRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MobileUserControllerTest {
    private MobileUserRestService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(MobileUserRestService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new MobileUserController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void listsClientsWithoutExposingPasswordHash() throws Exception {
        when(service.listClients()).thenReturn(List.of(client(4L)));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].username").value("mobile_client"))
                .andExpect(jsonPath("$[0].deliveryAddress").value("Vilnius"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void createsClientAndReturnsLocation() throws Exception {
        when(service.createClient(any(ClientCreateRequest.class))).thenReturn(client(7L));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"mobile_client","password":"password123",
                                 "name":"Asta","surname":"Astrauskaitė",
                                 "email":"client@example.com","phoneNumber":"+37060000001",
                                 "deliveryAddress":"Vilnius"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clients/7"))
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void updatesClient() throws Exception {
        Client updated = client(7L);
        updated.setDeliveryAddress("Kaunas");
        when(service.updateClient(any(Long.class), any(ClientUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/clients/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"mobile_client","password":null,
                                 "name":"Asta","surname":"Astrauskaitė",
                                 "email":"client@example.com","phoneNumber":"+37060000001",
                                 "deliveryAddress":"Kaunas"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryAddress").value("Kaunas"));
    }

    @Test
    void softDeletesClient() throws Exception {
        mockMvc.perform(delete("/api/clients/7"))
                .andExpect(status().isNoContent());

        verify(service).deleteClient(7L);
    }

    @Test
    void listsDrivers() throws Exception {
        when(service.listDrivers()).thenReturn(List.of(driver(9L)));

        mockMvc.perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].vehicleType").value("CAR"))
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void createsDriver() throws Exception {
        when(service.createDriver(any(DriverCreateRequest.class))).thenReturn(driver(12L));

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"mobile_driver","password":"password123",
                                 "name":"Darius","surname":"Dargis",
                                 "email":"driver@example.com","phoneNumber":"+37060000002",
                                 "vehicleType":"CAR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/drivers/12"))
                .andExpect(jsonPath("$.vehicleType").value("CAR"));
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"mobile_client","password":"password123",
                                 "name":"Asta","surname":"Astrauskaitė",
                                 "email":"wrong-email","phoneNumber":null,"deliveryAddress":"Vilnius"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El. pašto formatas neteisingas"));
    }

    @Test
    void returnsNotFoundErrorBody() throws Exception {
        when(service.getDriver(999L))
                .thenThrow(new ApiResourceNotFoundException("Vairuotojas nerastas"));

        mockMvc.perform(get("/api/drivers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Vairuotojas nerastas"))
                .andExpect(jsonPath("$.path").value("/api/drivers/999"));
    }

    private Client client(Long id) {
        Client client = new Client("mobile_client", "secret-hash", "Asta", "Astrauskaitė",
                "client@example.com", "Vilnius");
        client.setId(id);
        return client;
    }

    private Driver driver(Long id) {
        Driver driver = new Driver("mobile_driver", "secret-hash", "Darius", "Dargis",
                "driver@example.com", VehicleType.CAR);
        driver.setId(id);
        return driver;
    }
}
