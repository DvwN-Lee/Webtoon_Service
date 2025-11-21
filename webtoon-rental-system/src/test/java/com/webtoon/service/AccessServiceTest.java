package com.webtoon.service;

import com.webtoon.domain.Reader;
import com.webtoon.domain.Episode;
import com.webtoon.pattern.AccessStrategy;
import com.webtoon.pattern.RentalAccessStrategy;
import com.webtoon.pattern.PurchaseAccessStrategy;
import com.webtoon.common.repository.InMemoryRentalRepository;
import com.webtoon.common.repository.InMemoryPurchaseRepository;

import org.junit.jupiter.api.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AccessServiceTest {

    private InMemoryRentalRepository rentalRepo;
    private InMemoryPurchaseRepository purchaseRepo;
    private AccessService accessService;
    private Reader reader;
    private Episode ep;
    private Clock baseClock;

    @BeforeEach
    void setUp() {
        rentalRepo = new InMemoryRentalRepository();
        purchaseRepo = new InMemoryPurchaseRepository();

        baseClock = Clock.fixed(
                LocalDateTime.of(2025,10,3,14,0)
                        .atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );

        accessService = new AccessService(rentalRepo, purchaseRepo, baseClock);

        reader = new Reader("reader1", "1234", "독자A");

        ep = new Episode(1L, 1L, 15, "최종 결전 (5)",
                "내용...", 50, 100);// 반드시 마지막에!
        reader.setId(1L);
        ep.setId(1L);
    }
    @Test
    @DisplayName("대여 시 포인트 50P 차감되고 접근 가능해야 한다")
    void rent_success_deductsPoints_and_isAccessible() {
        AccessStrategy rental = new RentalAccessStrategy(rentalRepo);

        boolean ok = accessService.grantAccess(reader, ep, rental);

        assertTrue(ok);
        assertEquals(950, reader.getPoints());
        assertTrue(accessService.canAccess(reader, ep));
    }

    @Test
    @DisplayName("대여 후 10분이 지나면 접근 불가")
    void rent_expires_after_10_minutes() {
        AccessStrategy rental = new RentalAccessStrategy(rentalRepo);
        accessService.grantAccess(reader, ep, rental);

        Clock after11 = Clock.offset(baseClock, Duration.ofMinutes(11));
        AccessService expired = new AccessService(rentalRepo, purchaseRepo, after11);

        assertFalse(expired.canAccess(reader, ep));
    }

    @Test
    @DisplayName("구매 시 100P 차감되고 영구 접근 가능")
    void purchase_success_deductsPoints() {
        AccessStrategy purchase = new PurchaseAccessStrategy(purchaseRepo);

        boolean ok = accessService.grantAccess(reader, ep, purchase);

        assertTrue(ok);
        assertEquals(900, reader.getPoints());
        assertTrue(accessService.canAccess(reader, ep));
    }

    @Test
    @DisplayName("대여 중 → 구매 전환 시 차액만 차감")
    void convert_rental_to_purchase() {
        AccessStrategy rental = new RentalAccessStrategy(rentalRepo);
        accessService.grantAccess(reader, ep, rental);
        assertEquals(950, reader.getPoints());

        AccessStrategy purchase = new PurchaseAccessStrategy(purchaseRepo);
        accessService.grantAccess(reader, ep, purchase);

        assertEquals(900, reader.getPoints());
    }

    @Test
    @DisplayName("포인트 부족 시 실패")
    void insufficient_points_fail() {
        reader.addPoints(-970);

        AccessStrategy rental = new RentalAccessStrategy(rentalRepo);
        AccessStrategy purchase = new PurchaseAccessStrategy(purchaseRepo);

        assertFalse(accessService.grantAccess(reader, ep, rental));
        assertFalse(accessService.grantAccess(reader, ep, purchase));
    }
}
