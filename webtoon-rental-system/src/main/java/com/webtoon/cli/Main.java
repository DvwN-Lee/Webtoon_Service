package com.webtoon.cli;

import com.webtoon.repository.*;
import com.webtoon.service.*;
import com.webtoon.util.DataInitializer;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

public class Main {
    public static void main(String[] args) {
        // 콘솔 출력 인코딩을 UTF-8로 설정 (윈도우 호환성)
        setupConsoleEncoding();

        printWelcomeBanner();

        // 리포지토리 초기화
        UserRepository userRepository = new UserRepository();
        JsonWebtoonRepository webtoonRepository = new JsonWebtoonRepository();
        JsonEpisodeRepository episodeRepository = new JsonEpisodeRepository();
        NotificationRepository notificationRepository = new NotificationRepository();
        ReaderRepository readerRepository = new ReaderRepository();
        RentalRepository rentalRepository = new RentalRepository();
        PurchaseRepository purchaseRepository = new PurchaseRepository();
        PaymentHistoryRepository paymentHistoryRepository = new PaymentHistoryRepository();
        StatisticsRepository statisticsRepository = new InMemoryStatisticsRepository();

        // 서비스 초기화
        Clock clock = Clock.systemDefaultZone();

        AuthService authService = new AuthService(userRepository, readerRepository);
        NotificationService notificationService = new NotificationService(notificationRepository);
        StatisticsService statisticsService = new StatisticsService(
            statisticsRepository,
            webtoonRepository
        );
        ReaderService readerService = new ReaderService(
            readerRepository,
            notificationService,
            rentalRepository,
            purchaseRepository
        );
        WebtoonService webtoonService = new WebtoonService(
            webtoonRepository,
            episodeRepository,
            notificationService,
            userRepository,
            statisticsService
        );
        EpisodeService episodeService = new EpisodeService(
            episodeRepository,
            statisticsService
        );
        PointService pointService = new PointService(
            paymentHistoryRepository,
            readerRepository,
            clock
        );
        AccessService accessService = new AccessService(
            rentalRepository,
            purchaseRepository,
            clock
        );
        AuthorService authorService = new AuthorService(
            userRepository,
            webtoonRepository,
            webtoonService
        );

        // 데이터 초기화 (샘플 데이터 생성)
        DataInitializer dataInitializer = new DataInitializer(
            authService,
            userRepository,
            webtoonRepository,
            episodeRepository,
            notificationService
        );
        dataInitializer.initializeData();

        // CLI 메뉴 컨트롤러 실행
        MenuController menuController = new MenuController(
            authService,
            readerService,
            webtoonService,
            episodeService,
            notificationService,
            pointService,
            accessService,
            rentalRepository,
            purchaseRepository,
            authorService,
            statisticsService,
            readerRepository
        );
        menuController.showStartMenu();

        printGoodbyeBanner();
    }

    /**
     * 콘솔 출력 인코딩을 UTF-8로 설정
     * Windows에서 한글이 깨지는 문제 방지
     */
    private static void setupConsoleEncoding() {
        try {
            // System.out을 UTF-8로 재설정
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // 설정 실패 시 경고 출력 (기본 인코딩 사용)
            System.err.println("Warning: Failed to set console encoding to UTF-8");
        }
    }

    private static void printWelcomeBanner() {
        System.out.println("============================================");
        System.out.println("  웹툰 대여 플랫폼");
        System.out.println("============================================");
        System.out.println();
    }

    private static void printGoodbyeBanner() {
        System.out.println();
        System.out.println("============================================");
        System.out.println("  프로그램을 종료합니다");
        System.out.println("  감사합니다!");
        System.out.println("============================================");
    }
}
