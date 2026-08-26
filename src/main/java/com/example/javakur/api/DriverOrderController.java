package com.example.javakur.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.example.javakur.api.ClientOrderDtos.MessageResponse;
import static com.example.javakur.api.ClientOrderDtos.SendMessageRequest;
import static com.example.javakur.api.DriverOrderDtos.OrderDetailResponse;
import static com.example.javakur.api.DriverOrderDtos.OrderSummaryResponse;
import static com.example.javakur.api.DriverOrderDtos.StatusUpdateRequest;

@RestController
@RequestMapping("/api/driver/orders")
@PreAuthorize("hasRole('DRIVER')")
public class DriverOrderController {
    private final DriverOrderRestService service;

    public DriverOrderController(DriverOrderRestService service) {
        this.service = service;
    }

    @GetMapping("/available")
    public List<OrderSummaryResponse> available(@AuthenticationPrincipal ApiPrincipal principal) {
        return service.available(principal.userId());
    }

    @GetMapping("/mine")
    public List<OrderSummaryResponse> mine(@AuthenticationPrincipal ApiPrincipal principal) {
        return service.mine(principal.userId());
    }

    @PostMapping("/{orderId}/accept")
    public OrderDetailResponse accept(@AuthenticationPrincipal ApiPrincipal principal,
                                      @PathVariable("orderId") Long orderId) {
        return service.accept(principal.userId(), orderId);
    }

    @GetMapping("/{orderId}")
    public OrderDetailResponse get(@AuthenticationPrincipal ApiPrincipal principal,
                                   @PathVariable("orderId") Long orderId) {
        return service.get(principal.userId(), orderId);
    }

    @PutMapping("/{orderId}/status")
    public OrderDetailResponse changeStatus(@AuthenticationPrincipal ApiPrincipal principal,
                                            @PathVariable("orderId") Long orderId,
                                            @Valid @RequestBody StatusUpdateRequest request) {
        return service.changeStatus(principal.userId(), orderId, request.status());
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
                        .path("/api/driver/orders/{orderId}/messages/{messageId}")
                        .buildAndExpand(orderId, message.id()).toUri())
                .body(message);
    }
}
