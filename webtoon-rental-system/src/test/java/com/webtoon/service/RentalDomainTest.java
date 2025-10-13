package com.webtoon.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RentalDomainTest {

    @Test
    void rental_isExpired_and_remainingTime_works() {
        LocalDateTime now = LocalDateTime.of(2025, 10, 3, 14, 0, 0);
        LocalDateTime expires = now.plusMinutes(10);

        Rental r = new Rental(1L, 1L, 1L, now, expires);

        assertFalse(r.isExpired(now.plusMinutes(5)));
        assertTrue(r.getRemainingTime(now.plusMinutes(5)).toMinutes() <= 5);

        assertTrue(r.isExpired(now.plusMinutes(11)));
        assertEquals(Duration.ZERO, r.getRemainingTime(now.plusMinutes(11)));
    }
}