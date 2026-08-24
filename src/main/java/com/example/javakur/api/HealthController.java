package com.example.javakur.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthController {
    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("UP", "food-delivery-api");
    }

    public record HealthResponse(String status, String service) {
    }
}
