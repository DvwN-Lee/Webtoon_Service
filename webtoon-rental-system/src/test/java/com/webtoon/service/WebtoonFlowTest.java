package com.webtoon.service;

import com.webtoon.domain.Author;
import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.EpisodeRepository;
import com.webtoon.repository.InMemoryEpisodeRepository;
import com.webtoon.repository.InMemoryWebtoonRepository;
import com.webtoon.repository.UserRepository;
import com.webtoon.repository.WebtoonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebtoonFlowTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // System.out을 캡처하도록 설정
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        // 원래 System.out 복원
        System.setOut(originalOut);
    }

    @Test
    void 작품생성_팔로우_회차발행_알림검증() {
        // 1) 고유한 username/authorName 생성 (테스트 격리)
        String suffix = String.valueOf(System.currentTimeMillis() % 10000); // 4자리 숫자
        String username = "author" + suffix; // 10자 이내
        String authorName = "작가" + suffix;

        // 2) UserRepository를 테스트에서 직접 생성해서 AuthService에 "주입"
        UserRepository userRepo = new UserRepository();
        AuthService auth = new AuthService(userRepo);

        // 3) 작가 회원가입
        Author author = auth.registerAuthor(username, "pass1234", authorName, "소개글");
        Long authorId = author.getId();

        // 4) 레포지토리 & 서비스 구성
        EpisodeRepository episodeRepo = new InMemoryEpisodeRepository();
        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
        NotificationService notifier = new NotificationService();
        WebtoonService webtoonService = new WebtoonService(webtoonRepo, episodeRepo, notifier);

        // 5) 웹툰 생성
        Webtoon toon = webtoonService.createWebtoon("밤의 상점", authorId);
        assertNotNull(toon.getId());
        assertEquals(authorId, Long.valueOf(toon.getAuthorId()));

        // 6) 팔로우 2명
        webtoonService.followWebtoon(toon.getId(), 101L);
        webtoonService.followWebtoon(toon.getId(), 102L);

        // 저장된 객체 기준으로도 팔로워 수 확인
        Webtoon stored = webtoonRepo.findById(toon.getId()).orElseThrow();
        assertEquals(2, stored.getFollowerUserIds().size());
        assertTrue(stored.getFollowerUserIds().contains(101L));
        assertTrue(stored.getFollowerUserIds().contains(102L));

        // 7) 회차 2개 발행 (번호 자동 증가)
        Episode ep1 = webtoonService.publishEpisode(toon.getId(), "1화. 첫 손님", "내용1", 50, 100);
        Episode ep2 = webtoonService.publishEpisode(toon.getId(), "2화. 비밀 주문서", "내용2", 50, 100);
        assertEquals(1, ep1.getNumber());
        assertEquals(2, ep2.getNumber());

        // 8) 저장소 정합성: 해당 작품의 회차는 2개, 번호 오름차순
        List<Episode> episodes = episodeRepo.findByWebtoonId(toon.getId());
        assertEquals(2, episodes.size());
        assertEquals(1, episodes.get(0).getNumber());
        assertEquals(2, episodes.get(1).getNumber());

        // 9) Webtoon에 회차 ID가 올바르게 추가되었는지 확인
        Webtoon updated = webtoonRepo.findById(toon.getId()).orElseThrow();
        assertEquals(2, updated.getEpisodeIds().size());
        assertTrue(updated.getEpisodeIds().contains(ep1.getId()));
        assertTrue(updated.getEpisodeIds().contains(ep2.getId()));

        // 10) Observer 패턴 알림 검증 (콘솔 출력 확인)
        // 팔로워 2명 × 회차 2개 = 4개의 알림 메시지가 출력되어야 함
        String output = outputStreamCaptor.toString();

        // "101"에 대한 알림 2개 (1화, 2화)
        assertTrue(output.contains("[알림] 사용자 101 → 웹툰 '밤의 상점'"),
                "사용자 101에 대한 알림이 출력되어야 합니다.");

        // "102"에 대한 알림 2개 (1화, 2화)
        assertTrue(output.contains("[알림] 사용자 102 → 웹툰 '밤의 상점'"),
                "사용자 102에 대한 알림이 출력되어야 합니다.");

        // 총 4개의 알림 메시지가 출력되었는지 확인
        long notificationCount = output.lines()
                .filter(line -> line.contains("[알림]") && line.contains("밤의 상점"))
                .count();
        assertEquals(4, notificationCount,
                "팔로워 2명 × 회차 2개 = 4개의 알림이 출력되어야 합니다.");
    }

    @Test
    void getLatestEpisode_최신회차조회() {
        // given
        String suffix = String.valueOf(System.currentTimeMillis() % 10000); // 4자리 숫자
        String username = "author" + suffix; // 10자 이내
        String authorName = "작가" + suffix;

        UserRepository userRepo = new UserRepository();
        AuthService auth = new AuthService(userRepo);
        Author author = auth.registerAuthor(username, "pass1234", authorName, "소개글");
        Long authorId = author.getId();

        EpisodeRepository episodeRepo = new InMemoryEpisodeRepository();
        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
        NotificationService notifier = new NotificationService();
        WebtoonService webtoonService = new WebtoonService(webtoonRepo, episodeRepo, notifier);

        Webtoon toon = webtoonService.createWebtoon("테스트 웹툰", authorId);

        // when: 회차가 없을 때
        Long latestBeforePublish = toon.getLatestEpisode();

        // then: null 반환
        assertNull(latestBeforePublish, "회차가 없을 때 null을 반환해야 합니다.");

        // when: 회차 3개 발행
        Episode ep1 = webtoonService.publishEpisode(toon.getId(), "1화", "내용1", 50, 100);
        Episode ep2 = webtoonService.publishEpisode(toon.getId(), "2화", "내용2", 50, 100);
        Episode ep3 = webtoonService.publishEpisode(toon.getId(), "3화", "내용3", 50, 100);

        Webtoon updated = webtoonRepo.findById(toon.getId()).orElseThrow();
        Long latestEpisodeId = updated.getLatestEpisode();

        // then: 가장 최근에 추가된 회차 ID 반환
        assertNotNull(latestEpisodeId);
        assertEquals(ep3.getId(), latestEpisodeId, "최신 회차는 3화의 ID여야 합니다.");
    }
}
