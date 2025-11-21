package com.webtoon.util;

import com.webtoon.domain.Author;
import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.*;
import com.webtoon.service.AuthService;
import com.webtoon.service.WebtoonService;
import com.webtoon.service.NotificationService;

import java.io.File;

public class DataInitializer {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JsonWebtoonRepository webtoonRepository;
    private final JsonEpisodeRepository episodeRepository;
    private final NotificationService notificationService;

    public DataInitializer(AuthService authService,
                          UserRepository userRepository,
                          JsonWebtoonRepository webtoonRepository,
                          JsonEpisodeRepository episodeRepository,
                          NotificationService notificationService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.webtoonRepository = webtoonRepository;
        this.episodeRepository = episodeRepository;
        this.notificationService = notificationService;
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

            System.out.println("✓ 웹툰 작품 생성 완료 (3개)");

            // 4. 회차 생성
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

            System.out.println("✓ 회차 생성 완료 (나 혼자만 레벨업: 15화, 마검의 계승자: 20화, 던전 리셋: 10화)");
            System.out.println();
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("[샘플 데이터 생성 완료]");
            System.out.println();
            System.out.println("[작가 계정]");
            System.out.println("1. ID: chugong     / PW: 1234 / 작가명: 추공");
            System.out.println("2. ID: geomma      / PW: 1234 / 작가명: 검마");
            System.out.println("3. ID: ant_writer  / PW: 1234 / 작가명: 안트");
            System.out.println();
            System.out.println("[웹툰 작품]");
            System.out.println("1. 나 혼자만 레벨업 (추공, 연재중, 15화)");
            System.out.println("2. 마검의 계승자 (검마, 연재중, 20화)");
            System.out.println("3. 던전 리셋 (안트, 완결, 10화)");
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
}
