package com.example.javakur.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import static com.example.javakur.api.ClientOrderDtos.CreateOrderRequest;
import static com.example.javakur.api.ClientOrderDtos.CreatedOrderResponse;

@RestController
@RequestMapping("/api/client/orders")
public class ClientOrderController {
    private final ClientOrderRestService service;

    public ClientOrderController(ClientOrderRestService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CreatedOrderResponse> create(
            @AuthenticationPrincipal ApiPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request,
            UriComponentsBuilder uriBuilder) {
        var order = service.create(principal.userId(), request);
        return ResponseEntity.created(uriBuilder
                        .path("/api/client/orders/{id}")
                        .buildAndExpand(order.getId()).toUri())
                .body(CreatedOrderResponse.from(order));
    }
}
