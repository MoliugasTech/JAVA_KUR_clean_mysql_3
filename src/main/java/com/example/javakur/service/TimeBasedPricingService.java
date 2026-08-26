package com.example.javakur.service;

import com.example.javakur.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalTime;

public class TimeBasedPricingService {
    public static final BigDecimal PEAK_MULTIPLIER = new BigDecimal("1.200");
    public static final BigDecimal OFF_PEAK_MULTIPLIER = new BigDecimal("0.900");

    private static final LocalTime LUNCH_PEAK_START = LocalTime.of(11, 0);
    private static final LocalTime LUNCH_PEAK_END = LocalTime.of(14, 0);
    private static final LocalTime DINNER_PEAK_START = LocalTime.of(18, 0);
    private static final LocalTime DINNER_PEAK_END = LocalTime.of(21, 0);

    public BigDecimal currentMultiplier() {
        return multiplierAt(LocalTime.now());
    }

    public BigDecimal multiplierAt(LocalTime time) {
        if (time == null) {
            throw new ValidationException("Kainodaros laikas yra privalomas");
        }
        boolean lunchPeak = isWithin(time, LUNCH_PEAK_START, LUNCH_PEAK_END);
        boolean dinnerPeak = isWithin(time, DINNER_PEAK_START, DINNER_PEAK_END);
        return lunchPeak || dinnerPeak ? PEAK_MULTIPLIER : OFF_PEAK_MULTIPLIER;
    }

    private boolean isWithin(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && time.isBefore(end);
    }
}
