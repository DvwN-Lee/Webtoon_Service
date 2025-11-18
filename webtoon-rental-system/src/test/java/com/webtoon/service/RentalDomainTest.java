package com.webtoon.service;

import org.junit.jupiter.api.Test;
import com.webtoon.domain.Rental;
import java.time.Clock;
import java.time.ZoneId;
import java.time.Duration;
import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;

class RentalDomainTest {

    @Test
    void rental_isExpired_and_remainingTime_works() {
        LocalDateTime base = LocalDateTime.of(2025, 10, 3, 14, 0, 0);
        Clock clock = Clock.fixed(base.atZone(java.time.ZoneId.systemDefault()).toInstant(),
                java.time.ZoneId.systemDefault());

        Rental r = Rental.ofNow(1L, 1L, 1L, 50, clock);

        // +5분 later
        Clock clock5 = Clock.fixed(base.plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        assertFalse(r.isExpired(clock5));
        assertTrue(r.getRemainingTime(clock5).toMinutes() <= 5);

        // +11분 later
        Clock clock11 = Clock.fixed(base.plusMinutes(11).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        assertTrue(r.isExpired(clock11));
        assertEquals(Duration.ZERO, r.getRemainingTime(clock11));
    }
}