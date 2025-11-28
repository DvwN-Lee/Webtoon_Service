package com.webtoon.cli;

import com.webtoon.common.util.InputUtil;
import com.webtoon.common.validation.ValidationException;
import com.webtoon.domain.*;
import com.webtoon.pattern.*;
import com.webtoon.repository.*;
import com.webtoon.service.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReaderMenuController {

    private final ReaderService readerService;
    private final WebtoonService webtoonService;
    private final EpisodeService episodeService;
    private final NotificationService notificationService;
    private final PointService pointService;
    private final AccessService accessService;
    private final RentalRepository rentalRepository;
    private final PurchaseRepository purchaseRepository;
    private final ReaderRepository readerRepository;

    public ReaderMenuController(ReaderService readerService,
                                WebtoonService webtoonService,
                                EpisodeService episodeService,
                                NotificationService notificationService,
                                PointService pointService,
                                AccessService accessService,
                                RentalRepository rentalRepository,
                                PurchaseRepository purchaseRepository,
                                ReaderRepository readerRepository) {
        this.readerService = readerService;
        this.webtoonService = webtoonService;
        this.episodeService = episodeService;
        this.notificationService = notificationService;
        this.pointService = pointService;
        this.accessService = accessService;
        this.rentalRepository = rentalRepository;
        this.purchaseRepository = purchaseRepository;
        this.readerRepository = readerRepository;
    }

    public void showHomeScreen(Reader reader) {
        while (true) {
            // 홈 화면 데이터 조회
            Map<String, Object> homeScreen = readerService.getHomeScreen(reader.getId());
            int unreadCount = (int) homeScreen.get("unreadNotificationCount");

            System.out.println();
            InputUtil.printHeader("독자 홈 화면");
            System.out.println(reader.getDisplayName() + "님, 환영합니다!");
            System.out.println("보유 포인트: " + homeScreen.get("points") + "P");
            if (unreadCount > 0) {
                System.out.println("안 읽은 알림: " + unreadCount + "개");
            }
            System.out.println();

            System.out.println("1. 프로필 조회");
            System.out.println("2. 웹툰 탐색");
            System.out.println("3. 내 서재");
            System.out.println("4. 알림함" + (unreadCount > 0 ? " [" + unreadCount + "]" : ""));
            System.out.println("5. 포인트 충전");
            System.out.println("0. 로그아웃");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 5);

            try {
                switch (choice) {
                    case 1:
                        showProfile(reader);
                        break;
                    case 2:
                        showWebtoonExplore(reader);
                        break;
                    case 3:
                        showMyLibrary(reader);
                        break;
                    case 4:
                        showNotifications(reader);
                        break;
                    case 5:
                        chargePoints(reader);
                        break;
                    case 0:
                        if (InputUtil.confirm("로그아웃 하시겠습니까?")) {
                            System.out.println("\n로그아웃되었습니다.");
                            InputUtil.pause();
                            return;
                        }
                        break;
                }
            } catch (ValidationException e) {
                System.out.println("\n[오류] " + e.getMessage());
                InputUtil.pause();
            }
        }
    }

    private void showProfile(Reader reader) {
        System.out.println();
        InputUtil.printHeader("독자 프로필");

        Map<String, Object> profile = readerService.getProfile(reader.getId());

        System.out.println("닉네임: " + profile.get("nickname"));
        System.out.println("아이디: " + reader.getUsername());
        System.out.println("보유 포인트: " + profile.get("points") + "P");
        System.out.println("가입일: " + profile.get("createdAt"));
        System.out.println();
        System.out.println("팔로우 작품 수: " + profile.get("followingCount") + "개");
        System.out.println("대여 중인 작품: " + profile.get("rentalCount") + "개");
        System.out.println("구매 완료 작품: " + profile.get("purchaseCount") + "개");

        InputUtil.printSeparator();
        InputUtil.pause();
    }

    private void showWebtoonExplore(Reader reader) {
        while (true) {
            System.out.println();
            InputUtil.printHeader("웹툰 탐색");
            System.out.println("1. 전체 웹툰 목록");
            System.out.println("2. 인기순 정렬");
            System.out.println("3. 최신순 정렬");
            System.out.println("4. 제목으로 검색");
            System.out.println("0. 뒤로가기");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 4);

            switch (choice) {
                case 1:
                    showWebtoonList(reader, webtoonService.listAllWebtoons());
                    break;
                case 2:
                    showWebtoonList(reader, webtoonService.sortByPopularity());
                    break;
                case 3:
                    showWebtoonList(reader, webtoonService.sortByLatest());
                    break;
                case 4:
                    searchWebtoons(reader);
                    break;
                case 0:
                    return;
            }
        }
    }

    private void searchWebtoons(Reader reader) {
        System.out.println();
        InputUtil.printHeader("웹툰 검색");
        String keyword = InputUtil.readLine("검색할 제목을 입력하세요: ");

        List<Webtoon> results = webtoonService.searchByTitle(keyword);

        if (results.isEmpty()) {
            System.out.println("\n검색 결과가 없습니다.");
            InputUtil.pause();
        } else {
            showWebtoonList(reader, results);
        }
    }

    private void showWebtoonList(Reader reader, List<Webtoon> webtoons) {
        if (webtoons.isEmpty()) {
            System.out.println("\n웹툰이 없습니다.");
            InputUtil.pause();
            return;
        }

        while (true) {
            System.out.println();
            InputUtil.printHeader("웹툰 목록 (" + webtoons.size() + "개)");

            for (int i = 0; i < webtoons.size(); i++) {
                Webtoon w = webtoons.get(i);
                System.out.printf("%d. [%s] %s (인기도: %d)\n",
                        (i + 1), w.getStatus(), w.getTitle(), w.getPopularity());
            }

            System.out.println("\n0. 뒤로가기");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("상세보기할 웹툰 번호 (0: 뒤로가기): ", 0, webtoons.size());

            if (choice == 0) {
                return;
            }

            showWebtoonDetail(reader, webtoons.get(choice - 1));
        }
    }

    private void showWebtoonDetail(Reader reader, Webtoon webtoon) {
        while (true) {
            // DB에서 최신 팔로우 상태 확인
            Reader latestReader = readerRepository.findById(reader.getId())
                .orElseThrow(() -> new ValidationException("존재하지 않는 독자입니다."));

            System.out.println();
            InputUtil.printHeader("웹툰 상세 정보");
            System.out.println("제목: " + webtoon.getTitle());
            System.out.println("장르: " + String.join(", ", webtoon.getGenres()));
            System.out.println("상태: " + webtoon.getStatus());
            System.out.println("줄거리: " + webtoon.getSummary());
            System.out.println("총 회차 수: " + webtoon.getEpisodeIds().size() + "화");
            System.out.println("인기도: " + webtoon.getPopularity());

            boolean isFollowing = latestReader.getFollowingWebtoonIds().contains(webtoon.getId());
            System.out.println("팔로우 상태: " + (isFollowing ? "팔로우 중" : "미팔로우"));

            System.out.println();
            System.out.println("1. 회차 목록 보기");
            System.out.println("2. " + (isFollowing ? "언팔로우" : "팔로우"));
            System.out.println("0. 뒤로가기");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 2);

            switch (choice) {
                case 1:
                    showEpisodeList(reader, webtoon);
                    break;
                case 2:
                    if (isFollowing) {
                        // Reader에서 팔로우 정보 제거
                        readerService.unfollowWebtoon(reader.getId(), webtoon.getId());
                        reader.unfollowWebtoon(webtoon.getId());

                        // Webtoon에서도 팔로우 정보 제거 + observer 해제 + JSON save
                        webtoonService.unfollowWebtoon(webtoon.getId(), reader.getId());

                        System.out.println("\n언팔로우하였습니다.");
                    } else {
                        // Reader에 팔로우 정보 저장
                        readerService.followWebtoon(reader.getId(), webtoon.getId());
                        reader.followWebtoon(webtoon.getId());

                        // Webtoon에도 팔로우 정보 저장 + observer 등록 + JSON save
                        webtoonService.followWebtoon(webtoon.getId(), reader.getId());

                        System.out.println("\n팔로우하였습니다.");
                    }
                    InputUtil.pause();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void showEpisodeList(Reader reader, Webtoon webtoon) {

        // 항상 최신 Reader 사용
        Reader latestReader = readerRepository.findById(reader.getId())
                .orElseThrow(() -> new ValidationException("존재하지 않는 독자입니다."));

        List<Episode> episodes = episodeService.findByWebtoonId(webtoon.getId());

        if (episodes.isEmpty()) {
            System.out.println("\n등록된 회차가 없습니다.");
            InputUtil.pause();
            return;
        }

        while (true) {
            System.out.println();
            InputUtil.printHeader(webtoon.getTitle() + " - 회차 목록");

            for (Episode ep : episodes) {
                System.out.printf("%d화. %s (조회수: %d, 대여: %dP, 구매: %dP)\n",
                        ep.getNumber(), ep.getTitle(), ep.getViewCount(),
                        ep.getRentPrice(), ep.getBuyPrice());
            }

            System.out.println("\n0. 뒤로가기");
            InputUtil.printSeparator();

            int episodeNum = InputUtil.readInt("보기할 회차 번호 (0: 뒤로가기): ", 0, episodes.size());

            if (episodeNum == 0) {
                return;
            }

            Episode selectedEpisode = episodes.get(episodeNum - 1);

            // 최신 Reader를 넘겨야 포인트/팔로우가 정확함
            showEpisodeDetail(latestReader, selectedEpisode);
        }
    }


    private void showEpisodeDetail(Reader reader, Episode episode) {

        // 항상 최신 Reader 조회
        Reader latestReader = readerRepository.findById(reader.getId())
                .orElseThrow(() -> new ValidationException("존재하지 않는 독자입니다."));

        System.out.println();
        InputUtil.printHeader("회차 상세");

        boolean hasAccess = accessService.canAccess(latestReader, episode);

        System.out.println("제목: " + episode.getTitle());
        System.out.println("대여가: " + episode.getRentPrice() + "P");
        System.out.println("구매가: " + episode.getBuyPrice() + "P");
        System.out.println("조회수: " + episode.getViewCount());

        if (hasAccess) {
            // 접근 가능한 경우 조회수 증가
            episode = episodeService.getEpisodeDetailForUser(episode, latestReader);

            System.out.println("\n[조회수가 증가되었습니다: " + episode.getViewCount() + "회]");

            // 구매/대여 상태 표시
            Purchase purchase = purchaseRepository.findByReaderIdAndEpisodeId(latestReader.getId(), episode.getId());
            Rental rental = rentalRepository.findActiveRentalByReaderIdAndEpisodeId(latestReader.getId(), episode.getId());

            if (purchase != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                System.out.println("[소장 상태] 영구 소장 (구매일: " + purchase.getPurchasedAt().format(formatter) + ")");
            } else if (rental != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                long remainingMinutes = rental.remainingSeconds(java.time.Clock.systemDefaultZone()) / 60;
                long remainingSeconds = rental.remainingSeconds(java.time.Clock.systemDefaultZone()) % 60;
                System.out.println("[대여 상태] 대여 중");
                System.out.println("  - 대여일: " + rental.getRentedAt().format(formatter));
                System.out.println("  - 만료일: " + rental.getExpiresAt().format(formatter));
                System.out.println("  - 남은 시간: " + remainingMinutes + "분 " + remainingSeconds + "초");
            }

            System.out.println("\n=== 회차 내용 ===");
            System.out.println(episode.getContent());
            System.out.println("=================");

        } else {
            System.out.println("\n이 회차를 보려면 대여 또는 구매가 필요합니다.\n");
            System.out.println("1. 대여하기 (" + episode.getRentPrice() + "P, 10분간 이용)");
            System.out.println("2. 구매하기 (" + episode.getBuyPrice() + "P, 영구 소장)");
            System.out.println("0. 뒤로가기");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 2);

            try {
                boolean success = false;
                switch (choice) {
                    case 1:
                        success = accessService.grantAccess(latestReader, episode,
                                new RentalAccessStrategy(rentalRepository));

                        if (success) {
                            // DB에 반영된 최신 Reader 다시 읽기
                            latestReader = readerRepository.findById(latestReader.getId()).get();

                            System.out.println("\n대여가 완료되었습니다!");
                            System.out.println("남은 포인트: " + latestReader.getPoints() + "P");
                            InputUtil.pause();
                            showEpisodeDetail(latestReader, episode);
                        } else {
                            System.out.println("\n[오류] 포인트가 부족합니다.");
                            InputUtil.pause();
                        }
                        return;

                    case 2:
                        success = accessService.grantAccess(latestReader, episode,
                                new PurchaseAccessStrategy(purchaseRepository));

                        if (success) {
                            latestReader = readerRepository.findById(latestReader.getId()).get();

                            System.out.println("\n구매가 완료되었습니다!");
                            System.out.println("남은 포인트: " + latestReader.getPoints() + "P");
                            InputUtil.pause();
                            showEpisodeDetail(latestReader, episode);
                        } else {
                            System.out.println("\n[오류] 포인트가 부족합니다.");
                            InputUtil.pause();
                        }
                        return;

                    case 0:
                        return;
                }
            } catch (ValidationException e) {
                System.out.println("\n[오류] " + e.getMessage());
                InputUtil.pause();
            }
        }

        InputUtil.printSeparator();
        InputUtil.pause();
    }

    private void showMyLibrary(Reader reader) {
        while (true) {
            System.out.println();
            InputUtil.printHeader("내 서재");
            System.out.println("1. 팔로우한 웹툰");
            System.out.println("2. 대여 중인 회차");
            System.out.println("3. 구매한 회차");
            System.out.println("0. 뒤로가기");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 3);

            switch (choice) {
                case 1:
                    showFollowingWebtoons(reader);
                    break;
                case 2:
                    showRentedEpisodes(reader);
                    break;
                case 3:
                    showPurchasedEpisodes(reader);
                    break;
                case 0:
                    return;
            }
        }
    }

    private void showFollowingWebtoons(Reader reader) {
        // DB에서 최신 Reader 데이터를 조회하여 팔로우 목록을 가져옴
        Reader latestReader = readerRepository.findById(reader.getId())
            .orElseThrow(() -> new ValidationException("존재하지 않는 독자입니다."));

        List<Long> followingIds = latestReader.getFollowingWebtoonIds();

        if (followingIds.isEmpty()) {
            System.out.println("\n팔로우한 웹툰이 없습니다.");
            InputUtil.pause();
            return;
        }

        System.out.println();
        InputUtil.printHeader("팔로우한 웹툰 (" + followingIds.size() + "개)");

        for (Long webtoonId : followingIds) {
            try {
                Webtoon w = webtoonService.getWebtoon(webtoonId);
                System.out.printf("- [%s] %s (총 %d화)\n",
                        w.getStatus(), w.getTitle(), w.getEpisodeIds().size());
            } catch (Exception e) {
                // 웹툰이 삭제된 경우
                System.out.println("- [삭제됨] (ID: " + webtoonId + ")");
            }
        }

        InputUtil.printSeparator();
        InputUtil.pause();
    }

    private void showRentedEpisodes(Reader reader) {
        List<Rental> rentals = accessService.getRentals(reader.getId());

        if (rentals.isEmpty()) {
            System.out.println("\n대여 중인 회차가 없습니다.");
            InputUtil.pause();
            return;
        }

        System.out.println();
        InputUtil.printHeader("대여 중인 회차 (" + rentals.size() + "개)");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Rental rental : rentals) {
            try {
                Episode ep = episodeService.findById(rental.getEpisodeId());
                System.out.printf("- %s (%dP, 만료: %s)\n",
                        ep.getTitle(),
                        rental.getPricePaid(),
                        rental.getExpiresAt().format(formatter));
            } catch (Exception e) {
                System.out.println("- [회차 정보 없음] (ID: " + rental.getEpisodeId() + ")");
            }
        }

        InputUtil.printSeparator();
        InputUtil.pause();
    }

    private void showPurchasedEpisodes(Reader reader) {
        List<Purchase> purchases = accessService.getPurchases(reader.getId());

        if (purchases.isEmpty()) {
            System.out.println("\n구매한 회차가 없습니다.");
            InputUtil.pause();
            return;
        }

        System.out.println();
        InputUtil.printHeader("구매한 회차 (" + purchases.size() + "개)");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Purchase purchase : purchases) {
            try {
                Episode ep = episodeService.findById(purchase.getEpisodeId());
                System.out.printf("- %s (%dP, 구매일: %s)\n",
                        ep.getTitle(),
                        purchase.getPricePaid(),
                        purchase.getPurchasedAt().format(formatter));
            } catch (Exception e) {
                System.out.println("- [회차 정보 없음] (ID: " + purchase.getEpisodeId() + ")");
            }
        }

        InputUtil.printSeparator();
        InputUtil.pause();
    }

    private void showNotifications(Reader reader) {
        while (true) {
            List<Notification> notifications = notificationService.getNotifications(reader.getId());

            System.out.println();
            InputUtil.printHeader("알림함 (" + notifications.size() + "개)");

            if (notifications.isEmpty()) {
                System.out.println("알림이 없습니다.");
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

                for (int i = 0; i < notifications.size(); i++) {
                    Notification n = notifications.get(i);
                    String status = n.isRead() ? " " : "[NEW]";
                    System.out.printf("%d. %s %s (%s)\n",
                            (i + 1), status, n.getMessage(),
                            n.getCreatedAt().format(formatter));
                }
            }

            System.out.println();
            System.out.println("1. 모두 읽음 처리");
            System.out.println("0. 뒤로가기");
            InputUtil.printSeparator();

            int choice = InputUtil.readInt("선택: ", 0, 1);

            if (choice == 1) {
                notificationService.markAllAsRead(reader.getId());
                System.out.println("\n모든 알림을 읽음 처리했습니다.");
                InputUtil.pause();
            } else {
                return;
            }
        }
    }

    private void chargePoints(Reader reader) {

        // 1) 항상 최신 Reader를 DB에서 다시 불러오기
        Reader latestReader = readerRepository.findById(reader.getId())
                .orElseThrow(() -> new ValidationException("존재하지 않는 독자입니다."));

        System.out.println();
        InputUtil.printHeader("포인트 충전");
        System.out.println("현재 포인트: " + latestReader.getPoints() + "P");
        System.out.println();
        System.out.println("1. 신용카드 (10,000원 → 1,000P)");
        System.out.println("2. 계좌이체 (5,000원 → 500P)");
        System.out.println("0. 취소");
        InputUtil.printSeparator();

        int choice = InputUtil.readInt("선택: ", 0, 2);

        try {
            boolean success = false;

            switch (choice) {
                case 1:
                    success = pointService.chargePoints(latestReader, 10000, new CreditCardPaymentStrategy());
                    if (success) {
                        System.out.println("\n신용카드로 10,000원 결제 완료!");
                        System.out.println("1,000P가 충전되었습니다.");
                    }
                    break;

                case 2:
                    success = pointService.chargePoints(latestReader, 5000, new BankTransferPaymentStrategy());
                    if (success) {
                        System.out.println("\n계좌이체로 5,000원 결제 완료!");
                        System.out.println("500P가 충전되었습니다.");
                    }
                    break;

                case 0:
                    return;
            }

            if (success) {
                // 2) 충전 후에도 최신 Reader를 다시 읽어서 출력
                Reader updatedReader = readerRepository.findById(reader.getId())
                        .orElseThrow(() -> new ValidationException("존재하지 않는 독자입니다."));

                System.out.println("현재 포인트: " + updatedReader.getPoints() + "P");
                InputUtil.pause();
            }

        } catch (ValidationException e) {
            System.out.println("\n[오류] " + e.getMessage());
            InputUtil.pause();
        }
    }

}
