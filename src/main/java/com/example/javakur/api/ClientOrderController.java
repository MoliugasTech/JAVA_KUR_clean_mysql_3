package com.example.javakur.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.example.javakur.api.ClientOrderDtos.CreateOrderRequest;
import static com.example.javakur.api.ClientOrderDtos.CreatedOrderResponse;
import static com.example.javakur.api.ClientOrderDtos.MessageResponse;
import static com.example.javakur.api.ClientOrderDtos.OrderDetailResponse;
import static com.example.javakur.api.ClientOrderDtos.OrderSummaryResponse;
import static com.example.javakur.api.ClientOrderDtos.SendMessageRequest;

@RestController
@RequestMapping("/api/client/orders")
@PreAuthorize("hasRole('CLIENT')")
public class ClientOrderController {
    private final ClientOrderRestService service;

    public ClientOrderController(ClientOrderRestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreatedOrderResponse> create(
            @AuthenticationPrincipal ApiPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request,
            UriComponentsBuilder uriBuilder) {
        var order = service.create(principal.userId(), request);
        return ResponseEntity.created(uriBuilder.path("/api/client/orders/{id}")
                        .buildAndExpand(order.getId()).toUri())
                .body(CreatedOrderResponse.from(order));
    }

    @GetMapping
    public List<OrderSummaryResponse> list(@AuthenticationPrincipal ApiPrincipal principal) {
        return service.list(principal.userId());
    }

    @GetMapping("/{orderId}")
    public OrderDetailResponse get(@AuthenticationPrincipal ApiPrincipal principal,
                                   @PathVariable("orderId") Long orderId) {
        return service.get(principal.userId(), orderId);
    }

    @GetMapping("/{orderId}/messages")
    public List<MessageResponse> messages(@AuthenticationPrincipal ApiPrincipal principal,
                                          @PathVariable("orderId") Long orderId) {
        return service.messages(principal.userId(), orderId);
    }

    @PostMapping("/{orderId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal ApiPrincipal principal,
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody SendMessageRequest request,
            UriComponentsBuilder uriBuilder) {
        MessageResponse message = service.sendMessage(
                principal.userId(), orderId, request.content());
        return ResponseEntity.created(uriBuilder
                        .path("/api/client/orders/{orderId}/messages/{messageId}")
                        .buildAndExpand(orderId, message.id()).toUri())
                .body(message);
    }
}
