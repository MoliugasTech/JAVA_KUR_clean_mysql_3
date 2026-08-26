package com.example.javakur.api;

import com.example.javakur.service.LoyaltyPointService;
import com.example.javakur.service.TimeBasedPricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class PricingAndLoyaltyConfiguration {

    @Bean
    TimeBasedPricingService timeBasedPricingService() {
        return new TimeBasedPricingService();
    }

    @Bean
    LoyaltyPointService loyaltyPointService() {
        return new LoyaltyPointService();
    }
}
