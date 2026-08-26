package com.example.javakur.api;

import com.example.javakur.service.LoyaltyPointService;
import com.example.javakur.service.TimeBasedPricingService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PricingAndLoyaltyConfigurationTest {

    @Test
    void registersBothServicesForTheRestApplication() {
        try (var context = new AnnotationConfigApplicationContext(
                PricingAndLoyaltyConfiguration.class)) {
            assertNotNull(context.getBean(TimeBasedPricingService.class));
            assertNotNull(context.getBean(LoyaltyPointService.class));
        }
    }
}
