package com.webtoon.service;

import com.webtoon.domain.Author;
import com.webtoon.domain.Episode;
import com.webtoon.domain.Reader;
import com.webtoon.domain.Webtoon;
import com.webtoon.domain.Notification;
import com.webtoon.repository.EpisodeRepository;
import com.webtoon.repository.InMemoryEpisodeRepository;
import com.webtoon.repository.InMemoryWebtoonRepository;
import com.webtoon.repository.NotificationRepository;
import com.webtoon.repository.UserRepository;
import com.webtoon.repository.WebtoonRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebtoonFlowTest {

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

        // 4) 독자 2명 생성 (고유한 username)
        String reader1Username = "reader" + suffix + "1";
        String reader2Username = "reader" + suffix + "2";
        Reader reader1 = auth.registerReader(reader1Username, "pass1234", "독자" + suffix + "1");
        Reader reader2 = auth.registerReader(reader2Username, "pass1234", "독자" + suffix + "2");
        Long reader1Id = reader1.getId();
        Long reader2Id = reader2.getId();

        // 5) 레포지토리 & 서비스 구성
        EpisodeRepository episodeRepo = new InMemoryEpisodeRepository();
        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
        NotificationRepository notificationRepo = new NotificationRepository();
        NotificationService notifier = new NotificationService(notificationRepo);
        WebtoonService webtoonService = new WebtoonService(webtoonRepo, episodeRepo, notifier, userRepo);

        // 6) 웹툰 생성
        Webtoon toon = webtoonService.createWebtoon("밤의 상점", authorId);
        assertNotNull(toon.getId());
        assertEquals(authorId, Long.valueOf(toon.getAuthorId()));

        // 7) 팔로우 2명 (내부에서 자동으로 Observer 등록됨)
        webtoonService.followWebtoon(toon.getId(), reader1Id);
        webtoonService.followWebtoon(toon.getId(), reader2Id);

        // 저장된 객체 기준으로도 팔로워 수 확인
        Webtoon stored = webtoonRepo.findById(toon.getId()).orElseThrow();
        assertEquals(2, stored.getFollowerUserIds().size());
        assertTrue(stored.getFollowerUserIds().contains(reader1Id));
        assertTrue(stored.getFollowerUserIds().contains(reader2Id));

        // 9) 회차 2개 발행 (번호 자동 증가)
        Episode ep1 = webtoonService.publishEpisode(toon.getId(), "1화. 첫 손님", "내용1", 50, 100);
        Episode ep2 = webtoonService.publishEpisode(toon.getId(), "2화. 비밀 주문서", "내용2", 50, 100);
        assertEquals(1, ep1.getNumber());
        assertEquals(2, ep2.getNumber());

        // 10) 저장소 정합성: 해당 작품의 회차는 2개, 번호 오름차순
        List<Episode> episodes = episodeRepo.findByWebtoonId(toon.getId());
        assertEquals(2, episodes.size());
        assertEquals(1, episodes.get(0).getNumber());
        assertEquals(2, episodes.get(1).getNumber());

        // 11) Webtoon에 회차 ID가 올바르게 추가되었는지 확인
        Webtoon updated = webtoonRepo.findById(toon.getId()).orElseThrow();
        assertEquals(2, updated.getEpisodeIds().size());
        assertTrue(updated.getEpisodeIds().contains(ep1.getId()));
        assertTrue(updated.getEpisodeIds().contains(ep2.getId()));

        // 12) Observer 패턴 알림 검증 (NotificationRepository에 저장된 알림 확인)
        // 팔로워 2명 × 회차 2개 = 4개의 알림이 생성되어야 함
        List<Notification> reader1Notifications = notificationRepo.findByReaderId(reader1Id);
        List<Notification> reader2Notifications = notificationRepo.findByReaderId(reader2Id);

        // reader1은 2개의 알림을 받아야 함 (1화, 2화)
        assertEquals(2, reader1Notifications.size(),
                "reader1은 2개의 알림을 받아야 합니다.");
        assertTrue(reader1Notifications.stream()
                .anyMatch(n -> n.getMessage().contains("밤의 상점")),
                "알림 메시지에 웹툰 제목이 포함되어야 합니다.");

        // reader2도 2개의 알림을 받아야 함 (1화, 2화)
        assertEquals(2, reader2Notifications.size(),
                "reader2는 2개의 알림을 받아야 합니다.");
        assertTrue(reader2Notifications.stream()
                .anyMatch(n -> n.getMessage().contains("밤의 상점")),
                "알림 메시지에 웹툰 제목이 포함되어야 합니다.");

        // 총 4개의 알림이 생성되었는지 확인
        List<Notification> allNotifications = notificationRepo.findAll();
        long webtoonNotificationCount = allNotifications.stream()
                .filter(n -> n.getWebtoonId().equals(toon.getId()))
                .count();
        assertEquals(4, webtoonNotificationCount,
                "팔로워 2명 × 회차 2개 = 4개의 알림이 생성되어야 합니다.");
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
