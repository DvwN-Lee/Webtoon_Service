package com.webtoon.cli;

import com.webtoon.repository.UserRepository;
import com.webtoon.service.AuthService;
import com.webtoon.util.DataInitializer;

public class Main {
    public static void main(String[] args) {
        printWelcomeBanner();

        // 서비스 및 리포지토리 초기화
        UserRepository userRepository = new UserRepository();
        AuthService authService = new AuthService(userRepository);

        // 데이터 초기화 (샘플 사용자 생성)
        DataInitializer dataInitializer = new DataInitializer(authService, userRepository);
        dataInitializer.initializeData();

        // CLI 메뉴 컨트롤러 실행
        MenuController menuController = new MenuController(authService);
        menuController.showStartMenu();

        printGoodbyeBanner();
    }

    private static void printWelcomeBanner() {
        System.out.println("=====================================");
        System.out.println("   웹툰 대여 서비스 v1.0");
        System.out.println("=====================================");
        System.out.println();
    }

    private static void printGoodbyeBanner() {
        System.out.println();
        System.out.println("=====================================");
        System.out.println("   프로그램을 종료합니다");
        System.out.println("   감사합니다!");
        System.out.println("=====================================");
    }
}