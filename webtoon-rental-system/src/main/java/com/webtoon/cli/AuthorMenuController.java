package com.webtoon.cli;

import com.webtoon.common.util.InputUtil;
import com.webtoon.domain.*;
import com.webtoon.service.*;

import java.util.Arrays;
import java.util.List;

/**
 * 작가 메뉴 컨트롤러
 * 작가 전용 기능 제공
 */
public class AuthorMenuController {

    private final AuthorService authorService;
    private final WebtoonService webtoonService;
    private final EpisodeService episodeService;
    private final StatisticsService statisticsService;

    public AuthorMenuController(AuthorService authorService,
                               WebtoonService webtoonService,
                               EpisodeService episodeService,
                               StatisticsService statisticsService) {
        this.authorService = authorService;
        this.webtoonService = webtoonService;
        this.episodeService = episodeService;
        this.statisticsService = statisticsService;
    }

    public void showHomeScreen(Author author) {
        while (true) {
            // 최신 작가 정보 조회
            author = authorService.getProfile(author.getId());

            System.out.println();
            InputUtil.printHeader("작가 홈 화면");
            System.out.println(author.getDisplayName() + " 작가님, 환영합니다!");
            System.out.println("보유 포인트: " + author.getPoints() + "P");
            System.out.println();

            System.out.println("1. 프로필 조회");
            System.out.println("2. 내 작품 관리");
            System.out.println("3. 새 작품 등록");
            System.out.println("4. 통계 보기");
            System.out.println("0. 로그아웃");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 4);

            switch (choice) {
                case 1:
                    showProfile(author);
                    break;
                case 2:
                    manageWebtoons(author);
                    break;
                case 3:
                    createNewWebtoon(author);
                    break;
                case 4:
                    showStatistics(author);
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

    private void showProfile(Author author) {
        System.out.println();
        InputUtil.printHeader("작가 프로필");
        System.out.println("작가명: " + author.getDisplayName());
        System.out.println("아이디: " + author.getUsername());
        System.out.println("자기소개: " + (author.getBio() != null ? author.getBio() : "(없음)"));
        System.out.println("보유 포인트: " + author.getPoints() + "P");
        System.out.println("가입일: " + author.getCreatedAt());

        // 통계 정보 표시
        AuthorStats stats = statisticsService.getAuthorStats(author);
        System.out.println();
        System.out.println("[통계]");
        System.out.println("  연재 작품 수: " + stats.getWebtoonCount() + "개");
        System.out.println("  총 회차 수: " + stats.getTotalEpisodeCount() + "화");
        System.out.println("  총 조회수: " + stats.getTotalViews() + "회");

        InputUtil.printSeparator();
        InputUtil.pause();
    }

    private void manageWebtoons(Author author) {
        while (true) {
            System.out.println();
            InputUtil.printHeader("내 작품 관리");

            List<Webtoon> webtoons = authorService.getHomeScreen(author.getId());

            if (webtoons.isEmpty()) {
                System.out.println("등록된 작품이 없습니다.");
                System.out.println();
                System.out.println("0. 뒤로가기");
                InputUtil.printSeparator();
                InputUtil.readInt("선택: ", 0, 0);
                return;
            }

            System.out.println("총 " + webtoons.size() + "개의 작품");
            System.out.println();

            for (int i = 0; i < webtoons.size(); i++) {
                Webtoon w = webtoons.get(i);
                System.out.println((i + 1) + ". " + w.getTitle());
                System.out.println("   상태: " + w.getStatus() + " | 인기도: " + w.getPopularity() + " | 회차: " + w.getEpisodeIds().size() + "화");
            }

            System.out.println();
            System.out.println("0. 뒤로가기");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("관리할 작품 번호 (0: 뒤로가기): ", 0, webtoons.size());

            if (choice == 0) {
                return;
            }

            Webtoon selectedWebtoon = webtoons.get(choice - 1);
            manageWebtoonDetail(author, selectedWebtoon);
        }
    }

    private void manageWebtoonDetail(Author author, Webtoon webtoon) {
        while (true) {
            System.out.println();
            InputUtil.printHeader("작품 상세 관리");

            System.out.println("제목: " + webtoon.getTitle());
            System.out.println("장르: " + String.join(", ", webtoon.getGenres()));
            System.out.println("상태: " + webtoon.getStatus());
            System.out.println("줄거리: " + webtoon.getSummary());
            System.out.println("인기도: " + webtoon.getPopularity());
            System.out.println("팔로워: " + webtoon.getFollowerUserIds().size() + "명");
            System.out.println("회차 수: " + webtoon.getEpisodeIds().size() + "화");
            System.out.println();

            System.out.println("1. 회차 목록 보기");
            System.out.println("2. 새 회차 업로드");
            System.out.println("3. 작품 정보 수정");
            System.out.println("0. 뒤로가기");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 3);

            switch (choice) {
                case 1:
                    showEpisodeList(author, webtoon);
                    break;
                case 2:
                    uploadNewEpisode(author, webtoon);
                    // 최신 정보 다시 로드
                    webtoon = webtoonService.getWebtoon(webtoon.getId());
                    break;
                case 3:
                    updateWebtoonInfo(author, webtoon);
                    // 최신 정보 다시 로드
                    webtoon = webtoonService.getWebtoon(webtoon.getId());
                    break;
                case 0:
                    return;
            }
        }
    }

    private void showEpisodeList(Author author, Webtoon webtoon) {
        System.out.println();
        InputUtil.printHeader("회차 목록");

        List<Episode> episodes = episodeService.findByWebtoonId(webtoon.getId());

        if (episodes.isEmpty()) {
            System.out.println("등록된 회차가 없습니다.");
        } else {
            for (Episode ep : episodes) {
                System.out.println(ep.getNumber() + "화. " + ep.getTitle());
                System.out.println("  대여: " + ep.getRentPrice() + "P | 구매: " + ep.getBuyPrice() + "P | 조회수: " + ep.getViewCount());
            }
        }

        System.out.println();
        InputUtil.printSeparator();
        InputUtil.pause();
    }

    private void uploadNewEpisode(Author author, Webtoon webtoon) {
        System.out.println();
        InputUtil.printHeader("새 회차 업로드");

        try {
            String title = InputUtil.readLine("회차 제목: ");

            String content = InputUtil.readLine("회차 내용: ");

            String rentPriceStr = InputUtil.readLine("대여가 (기본 50P): ");
            int rentPrice = rentPriceStr.isEmpty() ? 50 : Integer.parseInt(rentPriceStr);

            String buyPriceStr = InputUtil.readLine("구매가 (기본 100P): ");
            int buyPrice = buyPriceStr.isEmpty() ? 100 : Integer.parseInt(buyPriceStr);

            Episode episode = authorService.uploadEpisode(
                    author.getId(),
                    webtoon.getId(),
                    title,
                    content,
                    rentPrice,
                    buyPrice
            );

            System.out.println("\n회차가 성공적으로 업로드되었습니다!");
            System.out.println("회차 번호: " + episode.getNumber() + "화");
            InputUtil.pause();

        } catch (Exception e) {
            System.out.println("\n[오류] " + e.getMessage());
            InputUtil.pause();
        }
    }

    private void updateWebtoonInfo(Author author, Webtoon webtoon) {
        System.out.println();
        InputUtil.printHeader("작품 정보 수정");

        System.out.println("수정할 항목을 선택하세요 (Enter: 변경 안 함)");
        System.out.println();

        String newTitle = InputUtil.readLine("제목 (현재: " + webtoon.getTitle() + "): ");
        if (newTitle.isEmpty()) newTitle = null;

        String newStatus = InputUtil.readLine("상태 (현재: " + webtoon.getStatus() + ") [ONGOING/COMPLETED]: ");
        if (newStatus.isEmpty()) newStatus = null;

        String newSummary = InputUtil.readLine("줄거리 (현재: " + webtoon.getSummary() + "): ");
        if (newSummary.isEmpty()) newSummary = null;

        try {
            authorService.updateWebtoon(
                    author.getId(),
                    webtoon.getId(),
                    newTitle,
                    null, // 장르는 수정하지 않음
                    newStatus,
                    newSummary
            );

            System.out.println("\n작품 정보가 수정되었습니다.");
            InputUtil.pause();

        } catch (Exception e) {
            System.out.println("\n[오류] " + e.getMessage());
            InputUtil.pause();
        }
    }

    private void createNewWebtoon(Author author) {
        System.out.println();
        InputUtil.printHeader("새 작품 등록");

        try {
            String title = InputUtil.readLine("작품 제목: ");

            String genresStr = InputUtil.readLine("장르 (쉼표로 구분, 예: 판타지,액션): ");
            List<String> genres = genresStr.isEmpty()
                    ? Arrays.asList("기타")
                    : Arrays.asList(genresStr.split(","));

            String status = InputUtil.readLine("상태 [ONGOING/COMPLETED] (기본: ONGOING): ");
            if (status.isEmpty()) status = "ONGOING";

            String summary = InputUtil.readLine("줄거리: ");

            Webtoon webtoon = authorService.createWebtoon(
                    author.getId(),
                    title,
                    genres,
                    status,
                    summary
            );

            System.out.println("\n작품이 성공적으로 등록되었습니다!");
            System.out.println("작품 ID: " + webtoon.getId());
            InputUtil.pause();

        } catch (Exception e) {
            System.out.println("\n[오류] " + e.getMessage());
            InputUtil.pause();
        }
    }

    private void showStatistics(Author author) {
        System.out.println();
        InputUtil.printHeader("통계 보기");

        AuthorStats stats = statisticsService.getAuthorStats(author);

        System.out.println("작가명: " + stats.getAuthorName());
        System.out.println();
        System.out.println("총 작품 수: " + stats.getWebtoonCount() + "개");
        System.out.println("총 회차 수: " + stats.getTotalEpisodeCount() + "화");
        System.out.println("총 조회수: " + stats.getTotalViews() + "회");
        System.out.println();

        // 작품별 상세 통계
        List<Webtoon> webtoons = authorService.getHomeScreen(author.getId());

        if (!webtoons.isEmpty()) {
            System.out.println("[작품별 통계]");
            for (Webtoon w : webtoons) {
                int episodeCount = statisticsService.getEpisodeCount(w.getId());
                long totalViews = statisticsService.getTotalViews(w.getId());
                System.out.println("- " + w.getTitle() + ": " + episodeCount + "화, " + totalViews + "회 조회");
            }
        }

        InputUtil.printSeparator();
        InputUtil.pause();
    }
}
