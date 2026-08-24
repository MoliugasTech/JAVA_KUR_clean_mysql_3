package com.example.javakur.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static com.example.javakur.api.MobileUserDtos.ClientCreateRequest;
import static com.example.javakur.api.MobileUserDtos.ClientResponse;
import static com.example.javakur.api.MobileUserDtos.ClientUpdateRequest;
import static com.example.javakur.api.MobileUserDtos.DriverCreateRequest;
import static com.example.javakur.api.MobileUserDtos.DriverResponse;
import static com.example.javakur.api.MobileUserDtos.DriverUpdateRequest;

@RestController
@RequestMapping("/api")
public class MobileUserController {
    private final MobileUserRestService service;

    public MobileUserController(MobileUserRestService service) {
        this.service = service;
    }

    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClientResponse> listClients() {
        return service.listClients().stream().map(ClientResponse::from).toList();
    }

    @GetMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and #id == principal.userId())")
    public ClientResponse getClient(@P("id") @PathVariable("id") Long id) {
        return ClientResponse.from(service.getClient(id));
    }

    @PostMapping("/clients")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientCreateRequest request) {
        ClientResponse response = ClientResponse.from(service.createClient(request));
        return ResponseEntity.created(URI.create("/api/clients/" + response.id())).body(response);
    }

    @PutMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and #id == principal.userId())")
    public ClientResponse updateClient(@P("id") @PathVariable("id") Long id,
                                       @Valid @RequestBody ClientUpdateRequest request) {
        return ClientResponse.from(service.updateClient(id, request));
    }

    @DeleteMapping("/clients/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENT') and #id == principal.userId())")
    public ResponseEntity<Void> deleteClient(@P("id") @PathVariable("id") Long id) {
        service.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DriverResponse> listDrivers() {
        return service.listDrivers().stream().map(DriverResponse::from).toList();
    }

    @GetMapping("/drivers/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and #id == principal.userId())")
    public DriverResponse getDriver(@P("id") @PathVariable("id") Long id) {
        return DriverResponse.from(service.getDriver(id));
    }

    @PostMapping("/drivers")
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody DriverCreateRequest request) {
        DriverResponse response = DriverResponse.from(service.createDriver(request));
        return ResponseEntity.created(URI.create("/api/drivers/" + response.id())).body(response);
    }

    @PutMapping("/drivers/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and #id == principal.userId())")
    public DriverResponse updateDriver(@P("id") @PathVariable("id") Long id,
                                       @Valid @RequestBody DriverUpdateRequest request) {
        return DriverResponse.from(service.updateDriver(id, request));
    }

    @DeleteMapping("/drivers/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DRIVER') and #id == principal.userId())")
    public ResponseEntity<Void> deleteDriver(@P("id") @PathVariable("id") Long id) {
        service.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}
