package com.webtoon.cli;

import com.webtoon.domain.Author;
import com.webtoon.common.util.InputUtil;

/**
 * 작가 메뉴 컨트롤러
 * 작가 전용 기능 제공
 */
public class AuthorMenuController {

    public void showHomeScreen(Author author) {
        while (true) {
            System.out.println();
            InputUtil.printHeader("작가 홈 화면");
            System.out.println(author.getDisplayName() + " 작가님, 환영합니다!");
            System.out.println("보유 포인트: " + author.getPoints() + "P");
            System.out.println();
            // TODO: AuthorService 연동하여 작품 통계 표시 (연재 작품 수, 총 팔로워 등)

            System.out.println("1. 프로필 조회");
            System.out.println("2. 내 작품 관리 (미구현)");
            System.out.println("3. 새 작품 등록 (미구현)");
            System.out.println("4. 통계 보기 (미구현)");
            System.out.println("0. 로그아웃");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 4);

            switch (choice) {
                case 1:
                    showProfile(author);
                    break;
                case 2:
                    showNotImplementedMessage("내 작품 관리", "AuthorService, WebtoonService");
                    break;
                case 3:
                    showNotImplementedMessage("새 작품 등록", "AuthorService, WebtoonService");
                    break;
                case 4:
                    showNotImplementedMessage("통계 보기", "StatisticsService");
                    break;
                case 0:
                    if (InputUtil.confirm("로그아웃 하시겠습니까?")) {
                        System.out.println("\n로그아웃되었습니다.");
                        InputUtil.pause();
                        return;
                    }
                    break;
            }
        }
    }

    public void showProfile(Author author) {
        System.out.println();
        InputUtil.printHeader("작가 프로필");
        System.out.println("작가명: " + author.getDisplayName());
        System.out.println("아이디: " + author.getUsername());
        System.out.println("자기소개: " + (author.getBio() != null ? author.getBio() : "(없음)"));
        System.out.println("보유 포인트: " + author.getPoints() + "P");
        System.out.println("가입일: " + author.getCreatedAt());
        System.out.println();
        // TODO: AuthorService 연동하여 연재 작품 수, 총 회차 수, 총 팔로워 수 표시
        System.out.println("[참고] 연재 작품 수, 총 팔로워 수 등은 추후 구현 예정입니다.");
        InputUtil.printSeparator();
        InputUtil.pause();
    }

    private void showNotImplementedMessage(String featureName, String requiredService) {
        System.out.println();
        InputUtil.printHeader("안내");
        System.out.println("[" + featureName + "] 기능은 현재 미구현 상태입니다.");
        System.out.println("필요한 서비스: " + requiredService);
        System.out.println();
        System.out.println("이 기능은 팀원들의 서비스 구현 후 활성화됩니다.");
        InputUtil.printSeparator();
        InputUtil.pause();
    }
}
