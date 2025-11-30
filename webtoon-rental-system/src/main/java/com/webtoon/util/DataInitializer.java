package com.webtoon.util;

import com.webtoon.domain.Author;
import com.webtoon.domain.Episode;
import com.webtoon.domain.Reader;
import com.webtoon.domain.Webtoon;
import com.webtoon.pattern.CreditCardPaymentStrategy;
import com.webtoon.pattern.PurchaseAccessStrategy;
import com.webtoon.pattern.RentalAccessStrategy;
import com.webtoon.repository.*;
import com.webtoon.service.*;

import java.io.File;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;

public class DataInitializer {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JsonWebtoonRepository webtoonRepository;
    private final JsonEpisodeRepository episodeRepository;
    private final NotificationService notificationService;
    private final ReaderRepository readerRepository;
    private final RentalRepository rentalRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    public DataInitializer(AuthService authService,
                          UserRepository userRepository,
                          JsonWebtoonRepository webtoonRepository,
                          JsonEpisodeRepository episodeRepository,
                          NotificationService notificationService,
                          ReaderRepository readerRepository,
                          RentalRepository rentalRepository,
                          PurchaseRepository purchaseRepository,
                          PaymentHistoryRepository paymentHistoryRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.webtoonRepository = webtoonRepository;
        this.episodeRepository = episodeRepository;
        this.notificationService = notificationService;
        this.readerRepository = readerRepository;
        this.rentalRepository = rentalRepository;
        this.purchaseRepository = purchaseRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
    }

    public void initializeData() {
        System.out.println("데이터 초기화를 시작합니다...");

        // users.json 파일 경로 확인
        String usersFilePath = "src/main/resources/data/users.json";
        File usersFile = new File(usersFilePath);

        if (usersFile.exists() && usersFile.length() > 0) {
            System.out.println("샘플 데이터가 이미 존재하여 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("샘플 데이터를 생성합니다...");

        try {
            // 1. 작가 3명 생성
            Author chugong = authService.registerAuthor("chugong", "1234", "추공",
                "판타지 액션 장르를 주로 그립니다. 독자 여러분의 많은 관심 부탁드립니다!");
            Author geomma = authService.registerAuthor("geomma", "1234", "검마",
                "판타지 장르를 주로 그립니다.");
            Author ant = authService.registerAuthor("ant_writer", "1234", "안트",
                "판타지 코미디를 그립니다.");

            System.out.println("✓ 작가 계정 생성 완료 (3명)");

            // 2. WebtoonService 생성
            WebtoonService webtoonService = new WebtoonService(
                webtoonRepository, episodeRepository, notificationService, userRepository);

            // 3. 웹툰 작품 생성
            Webtoon webtoon1 = webtoonService.createWebtoon("나 혼자만 레벨업", chugong.getId());
            webtoon1.setGenres(java.util.Arrays.asList("판타지", "액션"));
            webtoon1.setSummary("10년 전, 세계와 다른 차원을 이어주는 '게이트'가 열리고 평범한 사람들이 각성하여 헌터가 되었다. 성진우는 세계에서 가장 약한 E급 헌터였으나 던전 속 던전에서 죽을 고비를 넘기고 유일한 '플레이어'로 각성하게 되는데...");
            webtoon1.setStatus("ONGOING");
            webtoon1.setPopularity(1500);
            webtoonRepository.save(webtoon1);

            Webtoon webtoon2 = webtoonService.createWebtoon("마검의 계승자", geomma.getId());
            webtoon2.setGenres(java.util.Arrays.asList("판타지"));
            webtoon2.setSummary("마검을 들고 세상을 구하는 이야기");
            webtoon2.setStatus("ONGOING");
            webtoon2.setPopularity(1200);
            webtoonRepository.save(webtoon2);

            Webtoon webtoon3 = webtoonService.createWebtoon("던전 리셋", ant.getId());
            webtoon3.setGenres(java.util.Arrays.asList("판타지", "코미디"));
            webtoon3.setSummary("던전이 리셋되는 특별한 능력을 얻은 주인공의 이야기");
            webtoon3.setStatus("COMPLETED");
            webtoon3.setPopularity(1000);
            webtoonRepository.save(webtoon3);

            // 4. 웹툰 2개 추가 (Issue #22 요구사항)
            Author parkTaeJoon = authService.registerAuthor("park_author", "1234", "박태준",
                "학원물과 드라마 장르를 주로 그립니다.");

            Webtoon webtoon4 = webtoonService.createWebtoon("전지적 독자 시점", chugong.getId());
            webtoon4.setGenres(Arrays.asList("판타지", "액션"));
            webtoon4.setSummary("평범한 회사원 김독자는 지하철에서 읽던 소설 '멸망한 세계에서 살아남는 세 가지 방법'이 현실이 되어버린다. 소설의 유일한 독자로서 결말을 아는 그의 생존기가 시작된다.");
            webtoon4.setStatus("ONGOING");
            webtoon4.setPopularity(1800);
            webtoonRepository.save(webtoon4);

            Webtoon webtoon5 = webtoonService.createWebtoon("외모지상주의", parkTaeJoon.getId());
            webtoon5.setGenres(Arrays.asList("드라마", "액션"));
            webtoon5.setSummary("못생긴 외모 때문에 따돌림과 차별을 받던 박형석. 어느 날 완벽한 외모의 또 다른 몸을 갖게 되면서 그의 인생이 180도 바뀌게 된다.");
            webtoon5.setStatus("ONGOING");
            webtoon5.setPopularity(2000);
            webtoonRepository.save(webtoon5);

            System.out.println("작가 계정 추가 생성 완료 (박태준)");
            System.out.println("웹툰 작품 생성 완료 (5개)");

            // 5. 회차 생성
            // 나 혼자만 레벨업 - 15화
            for (int i = 1; i <= 15; i++) {
                String title = i <= 10 ? i + "화. " + getEpisodeTitle1(i) : "최종 결전 (" + (i - 10) + ")";
                webtoonService.publishEpisode(webtoon1.getId(), title,
                    "회차 " + i + " 내용입니다...", 50, 100);
            }

            // 마검의 계승자 - 20화
            for (int i = 1; i <= 20; i++) {
                webtoonService.publishEpisode(webtoon2.getId(), i + "화. " + getEpisodeTitle2(i),
                    "회차 " + i + " 내용입니다...", 50, 100);
            }

            // 던전 리셋 - 10화
            for (int i = 1; i <= 10; i++) {
                webtoonService.publishEpisode(webtoon3.getId(), i + "화. " + getEpisodeTitle3(i),
                    "회차 " + i + " 내용입니다...", 50, 100);
            }

            // 전지적 독자 시점 - 5화
            for (int i = 1; i <= 5; i++) {
                webtoonService.publishEpisode(webtoon4.getId(), i + "화. " + getEpisodeTitle4(i),
                    "회차 " + i + " 내용입니다...", 50, 100);
            }

            // 외모지상주의 - 4화
            for (int i = 1; i <= 4; i++) {
                webtoonService.publishEpisode(webtoon5.getId(), i + "화. " + getEpisodeTitle5(i),
                    "회차 " + i + " 내용입니다...", 50, 100);
            }

            System.out.println("회차 생성 완료 (나혼렙: 15화, 마검: 20화, 던전리셋: 10화, 전독시: 5화, 외모지상주의: 4화)");

            // 6. 독자 3명 생성 (Issue #22 요구사항)
            Reader reader1 = authService.registerReader("reader1", "1234", "독자일번");
            Reader reader2 = authService.registerReader("reader2", "1234", "독자이번");
            Reader reader3 = authService.registerReader("reader3", "1234", "독자삼번");
            System.out.println("독자 계정 생성 완료 (3명, 초기 포인트 각 1,000P)");

            // 7. 포인트 충전 (Issue #22 요구사항)
            Clock clock = Clock.systemDefaultZone();
            PointService pointService = new PointService(paymentHistoryRepository, readerRepository, clock);
            CreditCardPaymentStrategy creditCard = new CreditCardPaymentStrategy();

            pointService.chargePoints(reader1, 10_000, creditCard);  // +1,000P
            reader1 = readerRepository.findById(reader1.getId()).orElse(reader1);

            pointService.chargePoints(reader2, 5_000, creditCard);   // +500P
            reader2 = readerRepository.findById(reader2.getId()).orElse(reader2);

            pointService.chargePoints(reader3, 50_000, creditCard);  // +5,000P
            reader3 = readerRepository.findById(reader3.getId()).orElse(reader3);

            System.out.println("포인트 충전 완료 (reader1: 2,000P, reader2: 1,500P, reader3: 6,000P)");

            // 8. 팔로우 관계 설정 (Issue #22 요구사항) - 양방향 동기화
            ReaderService readerService = new ReaderService(readerRepository, notificationService,
                rentalRepository, purchaseRepository);

            // reader1: 나 혼자만 레벨업, 마검의 계승자 팔로우
            readerService.followWebtoon(reader1.getId(), webtoon1.getId());
            webtoonService.followWebtoon(webtoon1.getId(), reader1.getId());
            readerService.followWebtoon(reader1.getId(), webtoon2.getId());
            webtoonService.followWebtoon(webtoon2.getId(), reader1.getId());

            // reader2: 던전 리셋, 전지적 독자 시점 팔로우
            readerService.followWebtoon(reader2.getId(), webtoon3.getId());
            webtoonService.followWebtoon(webtoon3.getId(), reader2.getId());
            readerService.followWebtoon(reader2.getId(), webtoon4.getId());
            webtoonService.followWebtoon(webtoon4.getId(), reader2.getId());

            // reader3: 모든 웹툰 팔로우
            List<Webtoon> allWebtoons = Arrays.asList(webtoon1, webtoon2, webtoon3, webtoon4, webtoon5);
            for (Webtoon w : allWebtoons) {
                readerService.followWebtoon(reader3.getId(), w.getId());
                webtoonService.followWebtoon(w.getId(), reader3.getId());
            }
            System.out.println("팔로우 관계 설정 완료 (reader1: 2개, reader2: 2개, reader3: 5개)");

            // 9. 대여/구매 데이터 생성 (Issue #22 요구사항)
            AccessService accessService = new AccessService(rentalRepository, purchaseRepository,
                readerRepository, clock);
            RentalAccessStrategy rental = new RentalAccessStrategy(rentalRepository);
            PurchaseAccessStrategy purchase = new PurchaseAccessStrategy(purchaseRepository);

            // 회차 조회
            List<Episode> ep1List = episodeRepository.findByWebtoonId(webtoon1.getId());
            List<Episode> ep2List = episodeRepository.findByWebtoonId(webtoon2.getId());
            List<Episode> ep3List = episodeRepository.findByWebtoonId(webtoon3.getId());

            // reader1 최신 상태 조회
            reader1 = readerRepository.findById(reader1.getId()).orElse(reader1);
            // reader1: 나혼렙 1화 구매(100P), 2화 대여(50P)
            if (!ep1List.isEmpty()) {
                accessService.grantAccess(reader1, ep1List.get(0), purchase);
                reader1 = readerRepository.findById(reader1.getId()).orElse(reader1);
            }
            if (ep1List.size() > 1) {
                accessService.grantAccess(reader1, ep1List.get(1), rental);
                reader1 = readerRepository.findById(reader1.getId()).orElse(reader1);
            }

            // reader2 최신 상태 조회
            reader2 = readerRepository.findById(reader2.getId()).orElse(reader2);
            // reader2: 던전리셋 1화 대여(50P)
            if (!ep3List.isEmpty()) {
                accessService.grantAccess(reader2, ep3List.get(0), rental);
                reader2 = readerRepository.findById(reader2.getId()).orElse(reader2);
            }

            // reader3 최신 상태 조회
            reader3 = readerRepository.findById(reader3.getId()).orElse(reader3);
            // reader3: 나혼렙 1화 구매(100P), 마검 1화 구매(100P)
            if (!ep1List.isEmpty()) {
                accessService.grantAccess(reader3, ep1List.get(0), purchase);
                reader3 = readerRepository.findById(reader3.getId()).orElse(reader3);
            }
            if (!ep2List.isEmpty()) {
                accessService.grantAccess(reader3, ep2List.get(0), purchase);
                reader3 = readerRepository.findById(reader3.getId()).orElse(reader3);
            }

            System.out.println("대여/구매 데이터 생성 완료");
            System.out.println();
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("[샘플 데이터 생성 완료]");
            System.out.println();
            System.out.println("[작가 계정]");
            System.out.println("1. ID: chugong     / PW: 1234 / 작가명: 추공");
            System.out.println("2. ID: geomma      / PW: 1234 / 작가명: 검마");
            System.out.println("3. ID: ant_writer  / PW: 1234 / 작가명: 안트");
            System.out.println("4. ID: park_author / PW: 1234 / 작가명: 박태준");
            System.out.println();
            System.out.println("[독자 계정]");
            System.out.println("1. ID: reader1 / PW: 1234 / 닉네임: 독자일번");
            System.out.println("2. ID: reader2 / PW: 1234 / 닉네임: 독자이번");
            System.out.println("3. ID: reader3 / PW: 1234 / 닉네임: 독자삼번");
            System.out.println();
            System.out.println("[웹툰 작품]");
            System.out.println("1. 나 혼자만 레벨업 (추공, 연재중, 15화)");
            System.out.println("2. 마검의 계승자 (검마, 연재중, 20화)");
            System.out.println("3. 던전 리셋 (안트, 완결, 10화)");
            System.out.println("4. 전지적 독자 시점 (추공, 연재중, 5화)");
            System.out.println("5. 외모지상주의 (박태준, 연재중, 4화)");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println();

        } catch (Exception e) {
            System.err.println("데이터 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getEpisodeTitle1(int num) {
        String[] titles = {
            "세계의 변화", "E급 헌터", "던전 속 던전", "시스템 각성", "레벨업",
            "새로운 능력", "첫 사냥", "그림자 병사", "이중 던전", "숨겨진 힘"
        };
        return num <= titles.length ? titles[num - 1] : "에피소드 " + num;
    }

    private String getEpisodeTitle2(int num) {
        String[] titles = {
            "마검의 발견", "첫 번째 시련", "검의 선택", "어둠의 힘", "수련의 시작",
            "첫 전투", "검마의 등장", "비밀의 던전", "마검의 각성", "숨겨진 진실",
            "새로운 적", "동료들", "시련의 탑", "마검의 계승자", "결전 준비",
            "최종 결투", "승리의 대가", "새로운 여정", "마검의 각성", "숨겨진 던전"
        };
        return num <= titles.length ? titles[num - 1] : "에피소드 " + num;
    }

    private String getEpisodeTitle3(int num) {
        String[] titles = {
            "던전 입장", "리셋 발견", "혼자만의 비밀", "무한 성장", "트랩 마스터",
            "보스 공략", "숨겨진 보물", "던전의 비밀", "최종 리셋", "완결"
        };
        return num <= titles.length ? titles[num - 1] : "에피소드 " + num;
    }

    private String getEpisodeTitle4(int num) {
        String[] titles = {
            "멸망 3일 전", "시나리오의 시작", "주인공의 등장", "첫 번째 시련", "성좌들의 관심"
        };
        return num <= titles.length ? titles[num - 1] : "에피소드 " + num;
    }

    private String getEpisodeTitle5(int num) {
        String[] titles = {
            "두 개의 몸", "전학생", "복수의 시작", "새로운 친구들"
        };
        return num <= titles.length ? titles[num - 1] : "에피소드 " + num;
    }
}
