package com.webtoon.service;

import com.webtoon.domain.Episode;
import com.webtoon.domain.Reader;
import com.webtoon.pattern.AccessStrategy;
import com.webtoon.pattern.RentalAccessStrategy;
import com.webtoon.pattern.PurchaseAccessStrategy;
import com.webtoon.repository.PurchaseRepository;
import com.webtoon.repository.RentalRepository;

import org.junit.jupiter.api.*;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class AccessServiceTest {

    private RentalRepository rentalRepo;
    private PurchaseRepository purchaseRepo;
    private AccessService accessService;
    private Reader reader;
    private Episode ep;
    private Clock baseClock;

    @BeforeEach
    void setUp() {
        // 실제 구현체가 JSON 파일을 쓰더라도 테스트에 문제는 없음(로컬에만 생성).
        rentalRepo = new RentalRepository();
        purchaseRepo = new PurchaseRepository();

        baseClock = Clock.fixed(LocalDateTime.of(2025, 10, 3, 14, 0, 0)
                .atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        accessService = new AccessService(rentalRepo, purchaseRepo, baseClock);

        reader = new Reader(1L, "reader1", "1234", "독자A", 1000);
        // 대여 50P, 구매 100P 가정(시나리오/요구사항과 일치)
        ep = new Episode(1L, 1L, 15, "최종 결전 (5)", "내용...", 50, 100, LocalDateTime.now());
    }

    @Test
    @DisplayName("대여 시 포인트 50P 차감되고 접근 가능해야 한다")
    void rent_success_deductsPoints_and_isAccessible() {
        AccessStrategy rental = new RentalAccessStrategy();

        boolean ok = accessService.grantAccess(reader, ep, rental);

        assertTrue(ok);
        assertEquals(950, reader.getPoints(), "대여 후 잔여 포인트가 950P 여야 함");
        assertTrue(accessService.canAccess(reader, ep), "대여 직후에는 접근 가능해야 함");
    }

    @Test
    @DisplayName("대여 후 10분이 지나면 접근 불가 (만료)")
    void rent_expires_after_10_minutes() {
        AccessStrategy rental = new RentalAccessStrategy();
        accessService.grantAccess(reader, ep, rental);

        // 기준 시간 + 11분으로 Clock 이동 (10분 데모 규격 반영)
        Clock after11 = Clock.offset(baseClock, Duration.ofMinutes(11));
        AccessService expiredService = new AccessService(rentalRepo, purchaseRepo, after11);

        assertFalse(expiredService.canAccess(reader, ep), "10분 경과 후에는 만료로 접근 불가");
    }

    @Test
    @DisplayName("구매 시 포인트 100P 차감되고 영구 접근 가능")
    void purchase_success_deductsPoints_and_isAlwaysAccessible() {
        AccessStrategy purchase = new PurchaseAccessStrategy();

        boolean ok = accessService.grantAccess(reader, ep, purchase);

        assertTrue(ok);
        assertEquals(900, reader.getPoints(), "구매 후 잔여 포인트가 900P 여야 함");
        assertTrue(accessService.canAccess(reader, ep), "구매 후에는 영구 접근 가능");
    }

    @Test
    @DisplayName("대여 중인 회차를 구매로 전환하면 차액(50P)만 추가 차감")
    void convert_rental_to_purchase_pays_only_difference() {
        // 1) 먼저 대여
        AccessStrategy rental = new RentalAccessStrategy();
        assertTrue(accessService.grantAccess(reader, ep, rental));
        assertEquals(950, reader.getPoints());

        // 2) 구매 전략으로 재요청 → 차액 50P만 추가 차감되어 900P 기대
        AccessStrategy purchase = new PurchaseAccessStrategy();
        assertTrue(accessService.grantAccess(reader, ep, purchase));

        assertEquals(900, reader.getPoints(), "대여(50P) 사용 이력이 있으면 차액 50P만 추가 차감");
        assertTrue(accessService.canAccess(reader, ep));
    }

    @Test
    @DisplayName("포인트 부족 시 대여/구매 실패")
    void insufficient_points_fail() {
        reader = new Reader(2L, "reader2", "1234", "포인트부족", 30); // 50P/100P보다 적음
        AccessStrategy rental = new RentalAccessStrategy();
        AccessStrategy purchase = new PurchaseAccessStrategy();

        assertFalse(accessService.grantAccess(reader, ep, rental), "대여 실패해야 함");
        assertFalse(accessService.grantAccess(reader, ep, purchase), "구매 실패해야 함");
    }
}