package com.webtoon.cli;

import com.webtoon.domain.Reader;
import com.webtoon.common.util.InputUtil;

/**
 * 독자 메뉴 컨트롤러
 * 독자 전용 기능 제공
 */
public class ReaderMenuController {

    public void showHomeScreen(Reader reader) {
        while (true) {
            System.out.println();
            InputUtil.printHeader("독자 홈 화면");
            System.out.println(reader.getDisplayName() + "님, 환영합니다!");
            System.out.println("보유 포인트: " + reader.getPoints() + "P");
            System.out.println();
            // TODO: ReaderService 연동하여 미확인 알림 개수 표시

            System.out.println("1. 프로필 조회");
            System.out.println("2. 웹툰 탐색 (미구현)");
            System.out.println("3. 내 서재 (미구현)");
            System.out.println("4. 알림함 (미구현)");
            System.out.println("5. 포인트 충전 (미구현)");
            System.out.println("0. 로그아웃");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 5);

            switch (choice) {
                case 1:
                    showProfile(reader);
                    break;
                case 2:
                    showNotImplementedMessage("웹툰 탐색", "WebtoonService");
                    break;
                case 3:
                    showNotImplementedMessage("내 서재", "ReaderService (팔로우 기능)");
                    break;
                case 4:
                    showNotImplementedMessage("알림함", "NotificationService");
                    break;
                case 5:
                    showNotImplementedMessage("포인트 충전", "PointService");
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

    public void showProfile(Reader reader) {
        System.out.println();
        InputUtil.printHeader("독자 프로필");
        System.out.println("닉네임: " + reader.getDisplayName());
        System.out.println("아이디: " + reader.getUsername());
        System.out.println("보유 포인트: " + reader.getPoints() + "P");
        System.out.println("가입일: " + reader.getCreatedAt());
        System.out.println();
        // TODO: ReaderService 연동하여 팔로우 작품 수, 대여/구매 작품 수 표시
        System.out.println("[참고] 팔로우 작품 수, 대여 작품 수 등은 추후 구현 예정입니다.");
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
