package com.webtoon.cli;

import com.webtoon.common.util.InputUtil;
import com.webtoon.common.validation.ValidationException;
import com.webtoon.common.validation.Validator;
import com.webtoon.domain.Author;
import com.webtoon.domain.Reader;
import com.webtoon.domain.User;
import com.webtoon.repository.*;
import com.webtoon.service.*;

import java.time.Clock;

public class MenuController {

    private final AuthService authService;
    private final ReaderService readerService;
    private final WebtoonService webtoonService;
    private final EpisodeService episodeService;
    private final NotificationService notificationService;
    private final PointService pointService;
    private final AccessService accessService;
    private final RentalRepository rentalRepository;
    private final PurchaseRepository purchaseRepository;

    private final ReaderMenuController readerMenuController;
    private final AuthorMenuController authorMenuController;

    public MenuController(AuthService authService,
                         ReaderService readerService,
                         WebtoonService webtoonService,
                         EpisodeService episodeService,
                         NotificationService notificationService,
                         PointService pointService,
                         AccessService accessService,
                         RentalRepository rentalRepository,
                         PurchaseRepository purchaseRepository,
                         AuthorService authorService,
                         StatisticsService statisticsService) {
        this.authService = authService;
        this.readerService = readerService;
        this.webtoonService = webtoonService;
        this.episodeService = episodeService;
        this.notificationService = notificationService;
        this.pointService = pointService;
        this.accessService = accessService;
        this.rentalRepository = rentalRepository;
        this.purchaseRepository = purchaseRepository;

        this.readerMenuController = new ReaderMenuController(
            readerService, webtoonService, episodeService,
            notificationService, pointService, accessService,
            rentalRepository, purchaseRepository
        );
        this.authorMenuController = new AuthorMenuController(
            authorService, webtoonService, episodeService, statisticsService
        );
    }

    public void showStartMenu() {
        while (true) {
            System.out.println();
            InputUtil.printHeader("웹툰 서비스 시작 메뉴");
            System.out.println("1. 회원가입");
            System.out.println("2. 로그인");
            System.out.println("0. 종료");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 2);

            switch (choice) {
                case 1:
                    handleRegister();
                    break;
                case 2:
                    if (handleLogin()) {
                        // 로그인 성공 시 해당 사용자 타입에 맞는 메뉴로 이동
                        User currentUser = authService.getCurrentUser();
                        if (currentUser instanceof Reader) {
                            readerMenuController.showHomeScreen((Reader) currentUser);
                        } else if (currentUser instanceof Author) {
                            authorMenuController.showHomeScreen((Author) currentUser);
                        }
                        authService.logout(); // 홈 화면에서 로그아웃 시 다시 시작 메뉴로
                    }
                    break;
                case 0:
                    return;
            }
        }
    }

    private void handleRegister() {
        System.out.println();
        InputUtil.printHeader("회원가입");
        System.out.println("1. 독자 회원가입");
        System.out.println("2. 작가 회원가입");
        System.out.println("0. 뒤로가기");
        InputUtil.printSeparator();

        int choice = InputUtil.readInt("선택: ", 0, 2);

        try {
            switch (choice) {
                case 1:
                    registerReader();
                    break;
                case 2:
                    registerAuthor();
                    break;
                case 0:
                    return;
            }
        } catch (ValidationException e) {
            System.out.println("\n[오류] " + e.getMessage());
            InputUtil.pause();
        }
    }

    private void registerReader() {
        System.out.println();
        InputUtil.printHeader("독자 회원가입");

        // 아이디 입력 및 검증
        String username = null;
        while (username == null) {
            try {
                String input = InputUtil.readLine("아이디 (5-20자): ");
                Validator.validateUsername(input);
                username = input;
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 비밀번호 입력 및 검증
        String password = null;
        while (password == null) {
            try {
                String input = InputUtil.readLine("비밀번호 (4자 이상): ");
                Validator.validatePassword(input);
                password = input;
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 닉네임 입력 및 검증
        String nickname = null;
        while (nickname == null) {
            try {
                String input = InputUtil.readLine("닉네임 (2-10자): ");
                Validator.validateDisplayName(input);
                nickname = input;
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 회원가입 시도
        try {
            authService.registerReader(username, password, nickname);
            System.out.println("\n[완료] 독자 회원가입이 완료되었습니다.");
            System.out.println("초기 포인트 1000P가 지급되었습니다.");
            InputUtil.pause();
        } catch (ValidationException e) {
            System.out.println("\n[오류] " + e.getMessage());
            InputUtil.pause();
        }
    }

    private void registerAuthor() {
        System.out.println();
        InputUtil.printHeader("작가 회원가입");

        // 아이디 입력 및 검증
        String username = null;
        while (username == null) {
            try {
                String input = InputUtil.readLine("아이디 (5-20자): ");
                Validator.validateUsername(input);
                username = input;
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 비밀번호 입력 및 검증
        String password = null;
        while (password == null) {
            try {
                String input = InputUtil.readLine("비밀번호 (4자 이상): ");
                Validator.validatePassword(input);
                password = input;
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 작가명 입력 및 검증
        String authorName = null;
        while (authorName == null) {
            try {
                String input = InputUtil.readLine("작가명 (2-10자): ");
                Validator.validateDisplayName(input);
                authorName = input;
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 자기소개 입력 및 검증 (선택사항)
        String bio = null;
        boolean bioValid = false;
        while (!bioValid) {
            try {
                String input = InputUtil.readLine("자기소개 (선택 사항, Enter로 건너뛰기): ");
                if (!input.isEmpty()) {
                    Validator.validateBio(input);
                    bio = input;
                } else {
                    bio = null; // 빈 입력은 null로 저장
                }
                bioValid = true; // 검증 통과하면 루프 종료
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 회원가입 시도
        try {
            authService.registerAuthor(username, password, authorName, bio);
            System.out.println("\n[완료] 작가 회원가입이 완료되었습니다.");
            InputUtil.pause();
        } catch (ValidationException e) {
            System.out.println("\n[오류] " + e.getMessage());
            InputUtil.pause();
        }
    }

    private boolean handleLogin() {
        System.out.println();
        InputUtil.printHeader("로그인");
        System.out.println("1. 독자 로그인");
        System.out.println("2. 작가 로그인");
        InputUtil.printSeparator();

        int userTypeChoice = InputUtil.readInt("선택: ", 1, 2);
        String userType = (userTypeChoice == 1) ? "READER" : "AUTHOR";

        System.out.println();

        // 아이디 입력 및 검증
        String username = null;
        while (username == null) {
            try {
                String input = InputUtil.readLine("아이디: ");
                Validator.validateUsername(input);
                username = input;
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 비밀번호 입력 및 검증
        String password = null;
        while (password == null) {
            try {
                String input = InputUtil.readLine("비밀번호: ");
                Validator.validatePassword(input);
                password = input;
            } catch (ValidationException e) {
                System.out.println("[오류] " + e.getMessage());
            }
        }

        // 로그인 시도
        try {
            authService.login(userType, username, password);
            User user = authService.getCurrentUser();
            System.out.println("\n[완료] " + user.getDisplayName() + "님, 환영합니다!");
            InputUtil.pause();
            return true;
        } catch (ValidationException e) {
            System.out.println("\n[오류] " + e.getMessage());
            InputUtil.pause();
            return false;
        }
    }
}
