package com.example.javakur.api;

import com.example.javakur.model.Admin;
import com.example.javakur.model.Client;
import com.example.javakur.model.Driver;
import com.example.javakur.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MobileUserController.class)
@Import({ApiSecurityConfig.class, ApiTokenAuthenticationFilter.class,
        ApiTokenService.class, ApiSecurityErrorWriter.class})
class ApiSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiTokenService tokenService;

    @MockBean
    private MobileUserRestService service;

    @Test
    void allowsPublicClientRegistration() throws Exception {
        when(service.createClient(org.mockito.ArgumentMatchers.any())).thenReturn(client(7L));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"new_client","password":"password123",
                                 "name":"C","surname":"Client","email":"new@example.com",
                                 "phoneNumber":null,"deliveryAddress":"Vilnius"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsProtectedRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/clients/7"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Reikalingas prisijungimas"));
    }

    @Test
    void clientCanReadOwnProfile() throws Exception {
        Client client = client(7L);
        when(service.getClient(7L)).thenReturn(client);

        mockMvc.perform(get("/api/clients/7")
                        .header("Authorization", bearer(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void clientCannotReadAnotherClientProfile() throws Exception {
        mockMvc.perform(get("/api/clients/8")
                        .header("Authorization", bearer(client(7L))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Šiam veiksmui neturite teisių"));
    }

    @Test
    void driverCannotUseClientEndpoint() throws Exception {
        mockMvc.perform(get("/api/clients/7")
                        .header("Authorization", bearer(driver(7L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanListClients() throws Exception {
        when(service.listClients()).thenReturn(List.of(client(7L)));

        mockMvc.perform(get("/api/clients")
                        .header("Authorization", bearer(admin(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
    }

    @Test
    void ordinaryClientCannotListEveryClient() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .header("Authorization", bearer(client(7L))))
                .andExpect(status().isForbidden());
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

    private Admin admin(Long id) {
        Admin admin = new Admin("admin", "hash", "A", "Admin", "admin@example.com");
        admin.setId(id);
        return admin;
    }
}
