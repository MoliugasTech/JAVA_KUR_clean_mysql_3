package com.example.javakur.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static com.example.javakur.api.MobileReviewDtos.ReviewRequest;
import static com.example.javakur.api.MobileReviewDtos.ReviewResponse;

@RestController
public class MobileReviewController {
    private final MobileReviewService service;

    public MobileReviewController(MobileReviewService service) {
        this.service = service;
    }

    @GetMapping("/api/client/orders/{orderId}/reviews")
    @PreAuthorize("hasRole('CLIENT')")
    public List<ReviewResponse> clientReviews(
            @AuthenticationPrincipal ApiPrincipal principal,
            @PathVariable("orderId") Long orderId) {
        return service.clientReviews(principal.userId(), orderId);
    }

    @PostMapping("/api/client/orders/{orderId}/reviews/restaurant")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewResponse> reviewRestaurant(
            @AuthenticationPrincipal ApiPrincipal principal,
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody ReviewRequest request,
            UriComponentsBuilder uriBuilder) {
        return created(service.reviewRestaurant(principal.userId(), orderId, request),
                orderId, uriBuilder);
    }

    @PostMapping("/api/client/orders/{orderId}/reviews/driver")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReviewResponse> reviewDriver(
            @AuthenticationPrincipal ApiPrincipal principal,
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody ReviewRequest request,
            UriComponentsBuilder uriBuilder) {
        return created(service.reviewDriver(principal.userId(), orderId, request),
                orderId, uriBuilder);
    }

    @GetMapping("/api/driver/orders/{orderId}/reviews")
    @PreAuthorize("hasRole('DRIVER')")
    public List<ReviewResponse> driverReviews(
            @AuthenticationPrincipal ApiPrincipal principal,
            @PathVariable("orderId") Long orderId) {
        return service.driverReviews(principal.userId(), orderId);
    }

    @PostMapping("/api/driver/orders/{orderId}/reviews/client")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ReviewResponse> reviewClient(
            @AuthenticationPrincipal ApiPrincipal principal,
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody ReviewRequest request,
            UriComponentsBuilder uriBuilder) {
        return created(service.reviewClient(principal.userId(), orderId, request),
                orderId, uriBuilder);
    }

    private ResponseEntity<ReviewResponse> created(ReviewResponse review, Long orderId,
                                                   UriComponentsBuilder uriBuilder) {
        return ResponseEntity.created(uriBuilder
                        .path("/api/orders/{orderId}/reviews/{reviewId}")
                        .buildAndExpand(orderId, review.id()).toUri())
                .body(review);
    }
}
