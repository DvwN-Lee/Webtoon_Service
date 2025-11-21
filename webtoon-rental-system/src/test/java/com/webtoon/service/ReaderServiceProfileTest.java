package com.webtoon.service;

import com.webtoon.domain.*;
import com.webtoon.repository.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReaderService의 프로필 조회 기능 테스트
 * - rentalCount와 purchaseCount가 실제 데이터를 반영하는지 검증
 */
class ReaderServiceProfileTest {

    private ReaderRepository readerRepository;
    private RentalRepository rentalRepository;
    private PurchaseRepository purchaseRepository;
    private NotificationService notificationService;
    private ReaderService readerService;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 파일 삭제
        deleteTestDataFiles();

        // 리포지토리 초기화
        readerRepository = new ReaderRepository();
        rentalRepository = new RentalRepository();
        purchaseRepository = new PurchaseRepository();
        notificationService = new NotificationService();

        // 서비스 초기화
        readerService = new ReaderService(
            readerRepository,
            notificationService,
            rentalRepository,
            purchaseRepository
        );
    }

    @AfterEach
    void tearDown() {
        deleteTestDataFiles();
    }

    private void deleteTestDataFiles() {
        String[] files = {"readers.json", "rentals.json", "purchases.json", "notifications.json"};
        for (String file : files) {
            File f = new File("src/main/resources/data/" + file);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    @Test
    @DisplayName("프로필 조회 - 대여/구매 내역이 없는 경우")
    void testGetProfile_noRentalsOrPurchases() {
        // Given: 독자 생성
        Reader reader = new Reader("testUser", "password", "테스트독자");
        readerRepository.save(reader);

        // When: 프로필 조회
        Map<String, Object> profile = readerService.getProfile(reader.getId());

        // Then: 대여/구매 카운트가 0이어야 함
        assertEquals("테스트독자", profile.get("nickname"));
        assertEquals(1000, profile.get("points"));
        assertEquals(0, profile.get("followingCount"));
        assertEquals(0, profile.get("rentalCount"));
        assertEquals(0, profile.get("purchaseCount"));

        System.out.println("✓ 대여/구매 내역 없음: rentalCount=0, purchaseCount=0");
    }

    @Test
    @DisplayName("프로필 조회 - 활성 대여가 있는 경우")
    void testGetProfile_withActiveRentals() {
        // Given: 독자 생성
        Reader reader = new Reader("testUser", "password", "테스트독자");
        readerRepository.save(reader);

        // 활성 대여 2건 추가
        Clock clock = Clock.systemDefaultZone();
        LocalDateTime now = LocalDateTime.now(clock);
        Rental rental1 = new Rental(null, reader.getId(), 1L, 50, now, now.plusMinutes(10), clock);
        Rental rental2 = new Rental(null, reader.getId(), 2L, 50, now, now.plusMinutes(20), clock);
        rentalRepository.save(rental1);
        rentalRepository.save(rental2);

        // 만료된 대여 1건 추가 (카운트에 포함되지 않아야 함)
        Rental expiredRental = new Rental(null, reader.getId(), 3L, 50, now.minusMinutes(20), now.minusMinutes(10), clock);
        rentalRepository.save(expiredRental);

        // When: 프로필 조회
        Map<String, Object> profile = readerService.getProfile(reader.getId());

        // Then: 활성 대여만 카운트되어야 함
        assertEquals(2, profile.get("rentalCount"), "활성 대여는 2건이어야 합니다");
        assertEquals(0, profile.get("purchaseCount"));

        System.out.println("✓ 활성 대여 2건 확인: rentalCount=2");
    }

    @Test
    @DisplayName("프로필 조회 - 구매 내역이 있는 경우")
    void testGetProfile_withPurchases() {
        // Given: 독자 생성
        Reader reader = new Reader("testUser", "password", "테스트독자");
        readerRepository.save(reader);

        // 구매 3건 추가
        Purchase purchase1 = Purchase.ofNow(null, reader.getId(), 1L, 100);
        Purchase purchase2 = Purchase.ofNow(null, reader.getId(), 2L, 100);
        Purchase purchase3 = Purchase.ofNow(null, reader.getId(), 3L, 100);
        purchaseRepository.save(purchase1);
        purchaseRepository.save(purchase2);
        purchaseRepository.save(purchase3);

        // When: 프로필 조회
        Map<String, Object> profile = readerService.getProfile(reader.getId());

        // Then: 구매 카운트가 정확해야 함
        assertEquals(0, profile.get("rentalCount"));
        assertEquals(3, profile.get("purchaseCount"), "구매는 3건이어야 합니다");

        System.out.println("✓ 구매 3건 확인: purchaseCount=3");
    }

    @Test
    @DisplayName("프로필 조회 - 대여와 구매가 모두 있는 경우")
    void testGetProfile_withBothRentalsAndPurchases() {
        // Given: 독자 생성
        Reader reader = new Reader("testUser", "password", "테스트독자");
        readerRepository.save(reader);

        // 팔로잉 추가
        reader.followWebtoon(101L);
        reader.followWebtoon(102L);
        readerRepository.update(reader);

        // 활성 대여 2건
        Clock clock = Clock.systemDefaultZone();
        LocalDateTime now = LocalDateTime.now(clock);
        Rental rental1 = new Rental(null, reader.getId(), 1L, 50, now, now.plusMinutes(10), clock);
        Rental rental2 = new Rental(null, reader.getId(), 2L, 50, now, now.plusMinutes(20), clock);
        rentalRepository.save(rental1);
        rentalRepository.save(rental2);

        // 구매 3건
        Purchase purchase1 = Purchase.ofNow(null, reader.getId(), 3L, 100);
        Purchase purchase2 = Purchase.ofNow(null, reader.getId(), 4L, 100);
        Purchase purchase3 = Purchase.ofNow(null, reader.getId(), 5L, 100);
        purchaseRepository.save(purchase1);
        purchaseRepository.save(purchase2);
        purchaseRepository.save(purchase3);

        // When: 프로필 조회
        Map<String, Object> profile = readerService.getProfile(reader.getId());

        // Then: 모든 카운트가 정확해야 함
        assertEquals("테스트독자", profile.get("nickname"));
        assertEquals(1000, profile.get("points"));
        assertEquals(2, profile.get("followingCount"), "팔로잉 2개");
        assertEquals(2, profile.get("rentalCount"), "활성 대여 2건");
        assertEquals(3, profile.get("purchaseCount"), "구매 3건");

        System.out.println("✓ 전체 프로필 정보:");
        System.out.println("  - 닉네임: " + profile.get("nickname"));
        System.out.println("  - 포인트: " + profile.get("points"));
        System.out.println("  - 팔로잉: " + profile.get("followingCount"));
        System.out.println("  - 대여중: " + profile.get("rentalCount"));
        System.out.println("  - 구매완료: " + profile.get("purchaseCount"));
    }
}
