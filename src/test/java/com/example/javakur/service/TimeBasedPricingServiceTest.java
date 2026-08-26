package com.example.javakur.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeBasedPricingServiceTest {
    private final TimeBasedPricingService service = new TimeBasedPricingService();

    @Test
    void appliesHigherPriceDuringLunchAndDinnerPeaks() {
        assertEquals(new BigDecimal("1.200"),
                service.multiplierAt(LocalTime.of(12, 0)));
        assertEquals(new BigDecimal("1.200"),
                service.multiplierAt(LocalTime.of(19, 30)));
    }

    @Test
    void appliesDiscountOutsidePeakHours() {
        assertEquals(new BigDecimal("0.900"),
                service.multiplierAt(LocalTime.of(9, 30)));
        assertEquals(new BigDecimal("0.900"),
                service.multiplierAt(LocalTime.of(14, 0)));
    }
}
