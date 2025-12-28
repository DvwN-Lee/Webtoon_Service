package com.webtoon.integration;

import com.webtoon.domain.*;
import com.webtoon.repository.*;
import com.webtoon.service.*;
import com.webtoon.util.DataInitializer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 시나리오 통합 테스트
 * - 샘플 데이터 생성 검증
 * - 주요 사용자 시나리오 검증
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ScenarioIntegrationTest {

    private static UserRepository userRepository;
    private static JsonWebtoonRepository webtoonRepository;
    private static JsonEpisodeRepository episodeRepository;
    private static NotificationRepository notificationRepository;
    private static ReaderRepository readerRepository;
    private static RentalRepository rentalRepository;
    private static PurchaseRepository purchaseRepository;
    private static PaymentHistoryRepository paymentHistoryRepository;

    private static AuthService authService;
    private static WebtoonService webtoonService;
    private static NotificationService notificationService;

    @BeforeAll
    static void setup() {
        // 테스트용 데이터 파일 삭제
        deleteTestDataFiles();

        // 리포지토리 초기화
        userRepository = new UserRepository();
        webtoonRepository = new JsonWebtoonRepository();
        episodeRepository = new JsonEpisodeRepository();
        notificationRepository = new NotificationRepository();
        readerRepository = new ReaderRepository();
        rentalRepository = new RentalRepository();
        purchaseRepository = new PurchaseRepository();
        paymentHistoryRepository = new PaymentHistoryRepository();

        // 서비스 초기화
        authService = new AuthService(userRepository, readerRepository);
        notificationService = new NotificationService(notificationRepository);
        webtoonService = new WebtoonService(webtoonRepository, episodeRepository,
                                            notificationService, userRepository);

        // 데이터 초기화
        DataInitializer dataInitializer = new DataInitializer(
            authService, userRepository, webtoonRepository,
            episodeRepository, notificationService,
            readerRepository, rentalRepository, purchaseRepository, paymentHistoryRepository
        );
        dataInitializer.initializeData();
    }

    @AfterAll
    static void cleanup() {
        deleteTestDataFiles();
    }

    private static void deleteTestDataFiles() {
        String[] files = {"users.json", "webtoons.json", "episodes.json", "notifications.json",
                         "readers.json", "rentals.json", "purchases.json", "payment_history.json"};
        for (String file : files) {
            File f = new File("src/main/resources/data/" + file);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("시나리오 1: 샘플 데이터 생성 검증")
    void scenario1_validateSampleData() {
        System.out.println("\n=== 시나리오 1: 샘플 데이터 생성 검증 ===");

        // 1. 작가 4명 검증 (Issue #22 확장)
        List<User> allUsers = userRepository.findAll();
        long authorCount = allUsers.stream()
            .filter(u -> u instanceof Author)
            .count();
        assertEquals(4, authorCount, "작가는 4명이어야 합니다");
        System.out.println("작가 4명 생성 확인");

        // 2. 웹툰 5개 검증 (Issue #22 확장)
        List<Webtoon> allWebtoons = webtoonRepository.findAll();
        assertEquals(5, allWebtoons.size(), "웹툰은 5개여야 합니다");
        System.out.println("웹툰 5개 생성 확인");

        // 3. 각 웹툰의 회차 수 검증
        Webtoon levelUp = allWebtoons.stream()
            .filter(w -> w.getTitle().equals("나 혼자만 레벨업"))
            .findFirst()
            .orElseThrow();
        assertEquals(15, levelUp.getEpisodeIds().size(), "나 혼자만 레벨업은 15화여야 합니다");
        System.out.println("✓ 나 혼자만 레벨업: 15화 확인");

        Webtoon magicSword = allWebtoons.stream()
            .filter(w -> w.getTitle().equals("마검의 계승자"))
            .findFirst()
            .orElseThrow();
        assertEquals(20, magicSword.getEpisodeIds().size(), "마검의 계승자는 20화여야 합니다");
        System.out.println("✓ 마검의 계승자: 20화 확인");

        Webtoon dungeonReset = allWebtoons.stream()
            .filter(w -> w.getTitle().equals("던전 리셋"))
            .findFirst()
            .orElseThrow();
        assertEquals(10, dungeonReset.getEpisodeIds().size(), "던전 리셋은 10화여야 합니다");
        assertEquals("COMPLETED", dungeonReset.getStatus(), "던전 리셋은 완결 상태여야 합니다");
        System.out.println("✓ 던전 리셋: 10화 (완결) 확인");

        System.out.println("✅ 시나리오 1 통과: 샘플 데이터 정상 생성\n");
    }

    @Test
    @Order(2)
    @DisplayName("시나리오 2: 독자 회원가입 및 로그인")
    void scenario2_readerRegistrationAndLogin() {
        System.out.println("\n=== 시나리오 2: 독자 회원가입 및 로그인 ===");

        // 1. 독자 회원가입
        Reader newReader = authService.registerReader("testReader", "password123", "테스트독자");
        assertNotNull(newReader);
        assertNotNull(newReader.getId());
        assertEquals("테스트독자", newReader.getNickname());
        assertEquals(1000, newReader.getPoints(), "신규 독자는 1000P를 받아야 합니다");
        System.out.println("✓ 독자 회원가입 성공: " + newReader.getNickname() + " (1000P)");

        // 2. 로그인
        User loggedInUser = authService.login("READER", "testReader", "password123");
        assertNotNull(loggedInUser);
        assertTrue(loggedInUser instanceof Reader);
        assertEquals(newReader.getId(), loggedInUser.getId());
        System.out.println("✓ 로그인 성공: " + loggedInUser.getUsername());

        System.out.println("✅ 시나리오 2 통과: 회원가입 및 로그인 정상 동작\n");
    }

    @Test
    @Order(3)
    @DisplayName("시나리오 3: 웹툰 목록 조회 및 검색")
    void scenario3_webtoonBrowsingAndSearch() {
        System.out.println("\n=== 시나리오 3: 웹툰 목록 조회 및 검색 ===");

        // 1. 전체 웹툰 목록 조회
        List<Webtoon> allWebtoons = webtoonRepository.findAll();
        assertEquals(5, allWebtoons.size());
        System.out.println("전체 웹툰 목록 조회: " + allWebtoons.size() + "개");

        // 2. 인기순 정렬
        List<Webtoon> popularWebtoons = allWebtoons.stream()
            .sorted(Comparator.comparing(Webtoon::getPopularity).reversed())
            .collect(Collectors.toList());
        assertEquals("외모지상주의", popularWebtoons.get(0).getTitle(),
            "가장 인기있는 웹툰은 '외모지상주의'이어야 합니다");
        System.out.println("인기순 정렬: 1위 = " + popularWebtoons.get(0).getTitle());

        // 3. 제목 검색
        List<Webtoon> searchResults = webtoonRepository.searchByTitle("레벨업");
        assertEquals(1, searchResults.size());
        assertEquals("나 혼자만 레벨업", searchResults.get(0).getTitle());
        System.out.println("✓ 제목 검색 '레벨업': " + searchResults.get(0).getTitle() + " 찾음");

        System.out.println("✅ 시나리오 3 통과: 웹툰 조회 및 검색 정상 동작\n");
    }

    @Test
    @Order(4)
    @DisplayName("시나리오 4: 웹툰 팔로우 및 알림")
    void scenario4_followAndNotification() {
        System.out.println("\n=== 시나리오 4: 웹툰 팔로우 및 알림 ===");

        // 1. 독자 생성
        Reader reader = authService.registerReader("follower1", "pass1234", "팔로워1");
        System.out.println("✓ 테스트 독자 생성: " + reader.getNickname());

        // 2. 웹툰 조회
        List<Webtoon> webtoons = webtoonRepository.findAll();
        Webtoon levelUp = webtoons.stream()
            .filter(w -> w.getTitle().equals("나 혼자만 레벨업"))
            .findFirst()
            .orElseThrow();

        // 3. 웹툰 팔로우
        webtoonService.followWebtoon(levelUp.getId(), reader.getId());

        Webtoon followedWebtoon = webtoonRepository.findById(levelUp.getId()).orElseThrow();
        assertTrue(followedWebtoon.getFollowerUserIds().contains(reader.getId()));
        System.out.println("✓ 웹툰 팔로우 성공: " + levelUp.getTitle());

        // 4. 새 회차 발행 (알림 발생)
        int beforeNotificationCount = notificationService.getUnreadNotifications(reader.getId()).size();
        Episode newEpisode = webtoonService.publishEpisode(
            levelUp.getId(),
            "16화. 새로운 시작",
            "테스트 내용",
            50,
            100
        );
        assertNotNull(newEpisode);
        System.out.println("✓ 새 회차 발행: " + newEpisode.getTitle());

        // 5. 알림 확인
        int afterNotificationCount = notificationService.getUnreadNotifications(reader.getId()).size();
        assertEquals(beforeNotificationCount + 1, afterNotificationCount,
            "새 회차 발행 후 알림이 1개 증가해야 합니다");
        System.out.println("✓ 알림 수신 확인: 읽지 않은 알림 " + afterNotificationCount + "개");

        System.out.println("✅ 시나리오 4 통과: 팔로우 및 알림 시스템 정상 동작\n");
    }

    @Test
    @Order(5)
    @DisplayName("시나리오 5: 작가 계정으로 웹툰 관리")
    void scenario5_authorWebtoonManagement() {
        System.out.println("\n=== 시나리오 5: 작가의 웹툰 관리 ===");

        // 1. 작가 로그인 (기존 샘플 데이터의 추공)
        User chugong = authService.login("AUTHOR", "chugong", "1234");
        assertTrue(chugong instanceof Author);
        System.out.println("✓ 작가 로그인: " + ((Author) chugong).getAuthorName());

        // 2. 내 웹툰 조회
        List<Webtoon> myWebtoons = webtoonRepository.findByAuthorId(chugong.getId());
        assertFalse(myWebtoons.isEmpty());
        System.out.println("✓ 내 웹툰 조회: " + myWebtoons.size() + "개");

        Webtoon myWebtoon = myWebtoons.get(0);
        System.out.println("  - " + myWebtoon.getTitle() + " (" + myWebtoon.getEpisodeIds().size() + "화)");

        // 3. 회차 목록 조회
        List<Episode> episodes = episodeRepository.findByWebtoonId(myWebtoon.getId());
        assertFalse(episodes.isEmpty());
        System.out.println("✓ 회차 목록 조회: " + episodes.size() + "개");
        System.out.println("  - 최신화: " + episodes.get(episodes.size() - 1).getTitle());

        System.out.println("✅ 시나리오 5 통과: 작가 웹툰 관리 기능 정상 동작\n");
    }

    @Test
    @Order(6)
    @DisplayName("시나리오 6: 회차별 에피소드 제목 검증")
    void scenario6_episodeTitleValidation() {
        System.out.println("\n=== 시나리오 6: 에피소드 제목 검증 ===");

        // 나 혼자만 레벨업의 에피소드 제목 확인
        Webtoon levelUp = webtoonRepository.findAll().stream()
            .filter(w -> w.getTitle().equals("나 혼자만 레벨업"))
            .findFirst()
            .orElseThrow();

        List<Episode> episodes = episodeRepository.findByWebtoonId(levelUp.getId());

        // 1화 제목 확인
        Episode ep1 = episodes.stream().filter(e -> e.getNumber() == 1).findFirst().orElseThrow();
        assertTrue(ep1.getTitle().contains("세계의 변화"), "1화 제목이 올바르지 않습니다");
        System.out.println("✓ 1화: " + ep1.getTitle());

        // 5화 제목 확인
        Episode ep5 = episodes.stream().filter(e -> e.getNumber() == 5).findFirst().orElseThrow();
        assertTrue(ep5.getTitle().contains("레벨업"), "5화 제목이 올바르지 않습니다");
        System.out.println("✓ 5화: " + ep5.getTitle());

        // 11화 이후는 "최종 결전" 시리즈
        Episode ep11 = episodes.stream().filter(e -> e.getNumber() == 11).findFirst().orElseThrow();
        assertTrue(ep11.getTitle().contains("최종 결전"), "11화 제목이 올바르지 않습니다");
        System.out.println("✓ 11화: " + ep11.getTitle());

        System.out.println("✅ 시나리오 6 통과: 에피소드 제목 정상\n");
    }
}
