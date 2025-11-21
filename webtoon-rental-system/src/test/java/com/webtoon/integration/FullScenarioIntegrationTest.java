package com.webtoon.integration;

import com.webtoon.domain.*;
import com.webtoon.pattern.CreditCardPaymentStrategy;
import com.webtoon.repository.*;
import com.webtoon.service.*;
import org.junit.jupiter.api.*;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("전체 시나리오 통합 테스트 (5분 데모)")
class FullScenarioIntegrationTest {

    private static AuthService authService;
    private static WebtoonService webtoonService;
    private static EpisodeService episodeService;
    private static ReaderService readerService;
    private static NotificationService notificationService;
    private static PointService pointService;
    private static AccessService accessService;

    private static UserRepository userRepository;
    private static ReaderRepository readerRepository;
    private static JsonWebtoonRepository webtoonRepository;
    private static JsonEpisodeRepository episodeRepository;
    private static NotificationRepository notificationRepository;
    private static RentalRepository rentalRepository;
    private static PurchaseRepository purchaseRepository;
    private static PaymentHistoryRepository paymentHistoryRepository;

    private static Author chugong;
    private static Reader reader1;
    private static Webtoon levelUpWebtoon;

    @BeforeAll
    static void setUp() {
        // 기존 데이터 파일 삭제
        String dataDir = "src/main/resources/data/";
        String[] files = {"users.json", "readers.json", "webtoons.json", "episodes.json",
                         "notifications.json", "rentals.json", "purchases.json", "payment_history.json"};

        for (String file : files) {
            java.io.File f = new java.io.File(dataDir + file);
            if (f.exists()) {
                f.delete();
            }
        }

        // 리포지토리 초기화
        userRepository = new UserRepository();
        readerRepository = new ReaderRepository();
        webtoonRepository = new JsonWebtoonRepository();
        episodeRepository = new JsonEpisodeRepository();
        notificationRepository = new NotificationRepository();
        rentalRepository = new RentalRepository();
        purchaseRepository = new PurchaseRepository();
        paymentHistoryRepository = new PaymentHistoryRepository();

        Clock clock = Clock.systemDefaultZone();

        // 서비스 초기화
        authService = new AuthService(userRepository, readerRepository);
        notificationService = new NotificationService(notificationRepository);
        webtoonService = new WebtoonService(webtoonRepository, episodeRepository, notificationService, userRepository);
        episodeService = new EpisodeService(episodeRepository);
        readerService = new ReaderService(readerRepository, notificationService, rentalRepository, purchaseRepository);
        pointService = new PointService(paymentHistoryRepository, clock);
        accessService = new AccessService(rentalRepository, purchaseRepository, clock);

        System.out.println("\n========================================");
        System.out.println("  5분 데모 시나리오 테스트 시작");
        System.out.println("========================================\n");
    }

    @Test
    @Order(1)
    @DisplayName("[1분] 작가(chugong) 회원가입 및 작품 생성")
    void step1_AuthorRegistrationAndWebtoonCreation() {
        System.out.println("\n[STEP 1] 작가 회원가입 및 작품 생성");
        System.out.println("=====================================");

        // 작가 회원가입
        chugong = authService.registerAuthor("chugong", "1234", "추공", "판타지 작가입니다.");
        System.out.println("✓ 작가 회원가입 완료: " + chugong.getDisplayName());
        assertNotNull(chugong.getId());
        assertEquals("추공", chugong.getDisplayName());

        // 로그인
        authService.login("AUTHOR", "chugong", "1234");
        System.out.println("✓ 작가 로그인 완료");

        // 웹툰 생성
        levelUpWebtoon = webtoonService.createWebtoon("나 혼자만 레벨업", chugong.getId());
        System.out.println("✓ 웹툰 생성: " + levelUpWebtoon.getTitle());
        assertNotNull(levelUpWebtoon.getId());

        // 15개 회차 업로드
        for (int i = 1; i <= 15; i++) {
            webtoonService.publishEpisode(
                levelUpWebtoon.getId(),
                i + "화 - 성진우의 각성",
                "회차 " + i + " 내용입니다...",
                50, 100
            );
            System.out.println("  - " + i + "화 업로드 완료");
        }

        List<Episode> episodes = episodeService.findByWebtoonId(levelUpWebtoon.getId());
        assertEquals(15, episodes.size());
        System.out.println("✓ 총 15개 회차 업로드 완료\n");

        authService.logout();
    }

    @Test
    @Order(2)
    @DisplayName("[1분 30초] 독자(reader1) 회원가입 및 웹툰 검색")
    void step2_ReaderRegistrationAndSearch() {
        System.out.println("\n[STEP 2] 독자 회원가입 및 웹툰 검색");
        System.out.println("=====================================");

        // 독자 회원가입
        reader1 = authService.registerReader("reader1", "1234", "독자A");
        System.out.println("✓ 독자 회원가입 완료: " + reader1.getDisplayName());
        System.out.println("✓ 초기 포인트: " + reader1.getPoints() + "P");
        assertEquals(1000, reader1.getPoints());

        // 로그인
        authService.login("READER", "reader1", "1234");
        System.out.println("✓ 독자 로그인 완료");

        // 웹툰 검색
        List<Webtoon> searchResults = webtoonService.searchByTitle("레벨");
        System.out.println("✓ '레벨' 검색 결과: " + searchResults.size() + "개");
        assertEquals(1, searchResults.size());
        assertEquals("나 혼자만 레벨업", searchResults.get(0).getTitle());
        System.out.println("  - " + searchResults.get(0).getTitle() + " 발견\n");
    }

    @Test
    @Order(3)
    @DisplayName("[1분] 웹툰 팔로우 및 회차 대여")
    void step3_FollowAndRent() {
        System.out.println("\n[STEP 3] 웹툰 팔로우 및 회차 대여");
        System.out.println("=====================================");

        // 웹툰 상세 조회
        Webtoon webtoon = webtoonService.getWebtoon(levelUpWebtoon.getId());
        System.out.println("✓ 웹툰 상세 조회: " + webtoon.getTitle());
        System.out.println("  - 장르: " + String.join(", ", webtoon.getGenres()));
        System.out.println("  - 총 회차: " + webtoon.getEpisodeIds().size() + "화");

        // 팔로우
        readerService.followWebtoon(reader1.getId(), levelUpWebtoon.getId());
        // Reader 재조회 (팔로우 정보 갱신)
        reader1 = readerRepository.findById(reader1.getId()).orElseThrow();

        // Webtoon에도 팔로워 추가 (양방향 관계)
        Webtoon webtoonToUpdate = webtoonRepository.findById(levelUpWebtoon.getId()).orElseThrow();
        webtoonToUpdate.attach(reader1.getId());
        webtoonRepository.save(webtoonToUpdate);
        levelUpWebtoon = webtoonToUpdate;

        System.out.println("✓ 팔로우 완료");

        // 프로필 확인
        Map<String, Object> profile = readerService.getProfile(reader1.getId());
        assertEquals(1, profile.get("followingCount"));
        System.out.println("  - 팔로우 작품 수: " + profile.get("followingCount"));

        // 회차 목록 조회
        List<Episode> episodes = episodeService.findByWebtoonId(levelUpWebtoon.getId());
        Episode episode15 = episodes.get(14); // 15화
        System.out.println("✓ 15화 선택: " + episode15.getTitle());

        // 대여 전 포인트
        System.out.println("  - 대여 전 포인트: " + reader1.getPoints() + "P");

        // 15화 대여
        boolean rentSuccess = accessService.grantAccess(
            reader1,
            episode15,
            new com.webtoon.pattern.RentalAccessStrategy(rentalRepository)
        );
        assertTrue(rentSuccess);
        System.out.println("✓ 15화 대여 완료 (50P 차감)");
        System.out.println("  - 남은 포인트: " + reader1.getPoints() + "P");

        // 대여 목록 확인
        List<Rental> rentals = accessService.getRentals(reader1.getId());
        assertEquals(1, rentals.size());
        System.out.println("  - 대여 중인 회차: " + rentals.size() + "개\n");
    }

    @Test
    @Order(4)
    @DisplayName("[30초] 작가가 16화 업로드 → 팔로워 알림")
    void step4_NewEpisodeNotification() {
        System.out.println("\n[STEP 4] 신규 회차 업로드 및 알림");
        System.out.println("=====================================");

        // 작가 로그인
        authService.logout();
        authService.login("AUTHOR", "chugong", "1234");
        System.out.println("✓ 작가 재로그인");

        // 16화 업로드
        Episode episode16 = webtoonService.publishEpisode(
            levelUpWebtoon.getId(),
            "16화 - 그림자 군주",
            "성진우가 그림자 군주의 힘을 각성한다...",
            50, 100
        );
        System.out.println("✓ 16화 업로드 완료: " + episode16.getTitle());

        // 팔로워에게 알림 발송 확인
        authService.logout();
        authService.login("READER", "reader1", "1234");

        List<Notification> notifications = notificationService.getNotifications(reader1.getId());
        System.out.println("✓ 독자 알림 확인: " + notifications.size() + "개");
        assertTrue(notifications.size() > 0);

        // 미확인 알림 개수
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        System.out.println("  - 미확인 알림: " + unreadCount + "개");
        System.out.println("  - 최신 알림: " + notifications.get(0).getMessage() + "\n");
    }

    @Test
    @Order(5)
    @DisplayName("[30초] 독자 재로그인 및 알림 확인")
    void step5_ReloginAndCheckNotifications() {
        System.out.println("\n[STEP 5] 독자 재로그인 및 알림 확인");
        System.out.println("=====================================");

        // 로그아웃 후 재로그인
        authService.logout();
        authService.login("READER", "reader1", "1234");
        System.out.println("✓ 독자 재로그인");

        // 홈 화면 데이터 조회
        Map<String, Object> homeScreen = readerService.getHomeScreen(reader1.getId());
        int unreadCount = (int) homeScreen.get("unreadNotificationCount");
        System.out.println("✓ 홈 화면 표시");
        System.out.println("  - 보유 포인트: " + homeScreen.get("points") + "P");
        System.out.println("  - 안 읽은 알림: " + unreadCount + "개");

        // 알림함 확인
        List<Notification> notifications = notificationService.getNotifications(reader1.getId());
        System.out.println("✓ 알림함 확인");
        for (int i = 0; i < Math.min(3, notifications.size()); i++) {
            Notification n = notifications.get(i);
            String status = n.isRead() ? "[읽음]" : "[NEW]";
            System.out.println("  " + status + " " + n.getMessage());
        }

        // 16화로 바로 이동 (시뮬레이션)
        List<Episode> episodes = episodeService.findByWebtoonId(levelUpWebtoon.getId());
        Episode episode16 = episodes.stream()
            .filter(e -> e.getNumber() == 16)
            .findFirst()
            .orElseThrow();
        System.out.println("✓ 16화로 이동: " + episode16.getTitle() + "\n");
    }

    @Test
    @Order(6)
    @DisplayName("[30초] 포인트 충전 및 16화 구매")
    void step6_ChargePointsAndPurchase() {
        System.out.println("\n[STEP 6] 포인트 충전 및 회차 구매");
        System.out.println("=====================================");

        // 현재 포인트 확인
        int currentPoints = reader1.getPoints();
        System.out.println("✓ 현재 포인트: " + currentPoints + "P");
        System.out.println("  - 16화 구매 필요: 100P");

        // 포인트 부족 시뮬레이션 (현재 950P)
        if (currentPoints < 1000) {
            System.out.println("  - 포인트 부족! 충전 필요");

            // 포인트 충전 (10,000원 → 1,000P)
            boolean chargeSuccess = pointService.chargePoints(
                reader1,
                10000,
                new CreditCardPaymentStrategy()
            );
            assertTrue(chargeSuccess);
            // Reader 재조회 (포인트 변경 확인)
            reader1 = readerRepository.findById(reader1.getId()).orElseThrow();
            System.out.println("✓ 포인트 충전 완료");
            System.out.println("  - 충전액: 10,000원 (1,000P)");
            System.out.println("  - 충전 후 포인트: " + reader1.getPoints() + "P");
        }

        // 16화 구매
        List<Episode> episodes = episodeService.findByWebtoonId(levelUpWebtoon.getId());
        Episode episode16 = episodes.stream()
            .filter(e -> e.getNumber() == 16)
            .findFirst()
            .orElseThrow();

        boolean purchaseSuccess = accessService.grantAccess(
            reader1,
            episode16,
            new com.webtoon.pattern.PurchaseAccessStrategy(purchaseRepository)
        );
        assertTrue(purchaseSuccess);
        System.out.println("✓ 16화 구매 완료 (100P 차감)");
        System.out.println("  - 남은 포인트: " + reader1.getPoints() + "P");

        // 구매 목록 확인
        List<Purchase> purchases = accessService.getPurchases(reader1.getId());
        System.out.println("  - 구매 완료 회차: " + purchases.size() + "개\n");
    }

    @Test
    @Order(7)
    @DisplayName("[최종 검증] 프로필 및 통계 확인")
    void step7_FinalVerification() {
        System.out.println("\n[STEP 7] 최종 검증 - 프로필 및 통계");
        System.out.println("=====================================");

        // 독자 프로필 확인
        Map<String, Object> profile = readerService.getProfile(reader1.getId());
        System.out.println("✓ 독자 프로필:");
        System.out.println("  - 닉네임: " + profile.get("nickname"));
        System.out.println("  - 보유 포인트: " + profile.get("points") + "P");
        System.out.println("  - 팔로우 작품 수: " + profile.get("followingCount") + "개");
        System.out.println("  - 대여 중인 작품: " + profile.get("rentalCount") + "개");
        System.out.println("  - 구매 완료 작품: " + profile.get("purchaseCount") + "개");

        assertEquals(1, profile.get("followingCount"));
        assertEquals(1, profile.get("rentalCount"));
        assertEquals(1, profile.get("purchaseCount"));

        // 웹툰 정보 확인
        Webtoon webtoon = webtoonService.getWebtoon(levelUpWebtoon.getId());
        System.out.println("\n✓ 웹툰 정보:");
        System.out.println("  - 제목: " + webtoon.getTitle());
        System.out.println("  - 총 회차: " + webtoon.getEpisodeIds().size() + "화");
        assertEquals(16, webtoon.getEpisodeIds().size());

        // 알림 목록 확인
        List<Notification> notifications = notificationService.getNotifications(reader1.getId());
        System.out.println("\n✓ 알림 내역: " + notifications.size() + "개");

        // 대여 목록 확인
        List<Rental> rentals = accessService.getRentals(reader1.getId());
        System.out.println("✓ 대여 중인 회차: " + rentals.size() + "개");

        // 구매 목록 확인
        List<Purchase> purchases = accessService.getPurchases(reader1.getId());
        System.out.println("✓ 구매 완료 회차: " + purchases.size() + "개");

        System.out.println("\n========================================");
        System.out.println("  ✓ 5분 데모 시나리오 테스트 완료!");
        System.out.println("========================================\n");
    }

    @Test
    @Order(8)
    @DisplayName("[추가 검증] 대여 → 구매 전환 시나리오")
    void step8_RentalToPurchaseConversion() {
        System.out.println("\n[STEP 8] 대여 → 구매 전환 시나리오");
        System.out.println("=====================================");

        // 새로운 독자 생성
        Reader reader2 = authService.registerReader("reader2", "1234", "독자B");
        System.out.println("✓ 새 독자 생성: " + reader2.getDisplayName());
        System.out.println("  - 초기 포인트: " + reader2.getPoints() + "P");

        // 15화 대여
        List<Episode> episodes = episodeService.findByWebtoonId(levelUpWebtoon.getId());
        Episode episode15 = episodes.get(14);

        boolean rentSuccess = accessService.grantAccess(
            reader2,
            episode15,
            new com.webtoon.pattern.RentalAccessStrategy(rentalRepository)
        );
        assertTrue(rentSuccess);
        System.out.println("✓ 15화 대여 완료 (50P 차감)");
        System.out.println("  - 남은 포인트: " + reader2.getPoints() + "P");

        // 대여 → 구매 전환 (차액 50P만 차감)
        int pointsBeforeConversion = reader2.getPoints();
        boolean purchaseSuccess = accessService.grantAccess(
            reader2,
            episode15,
            new com.webtoon.pattern.PurchaseAccessStrategy(purchaseRepository)
        );
        assertTrue(purchaseSuccess);
        System.out.println("✓ 구매로 전환 완료 (차액 50P 차감)");
        System.out.println("  - 남은 포인트: " + reader2.getPoints() + "P");
        assertEquals(pointsBeforeConversion - 50, reader2.getPoints());

        // 총 비용 검증
        int totalSpent = 1000 - reader2.getPoints();
        assertEquals(100, totalSpent);
        System.out.println("  - 총 사용 포인트: " + totalSpent + "P (대여 50P + 전환 50P)\n");
    }
}
