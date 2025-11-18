package com.webtoon.service;

import com.webtoon.domain.Author;
import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.AuthorRepository;
import com.webtoon.repository.EpisodeRepository;
import com.webtoon.repository.InMemoryAuthorRepository;
import com.webtoon.repository.InMemoryEpisodeRepository;
import com.webtoon.repository.InMemoryWebtoonRepository;
import com.webtoon.repository.WebtoonRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebtoonFlowTest {

    @Test
    void 작품생성_팔로우_회차발행_알림검증() {
        // 1) AuthService는 기존 코드 그대로 사용 (회원가입 담당)
        AuthService auth = new AuthService();
        Author author = auth.registerAuthor("author1234", "pass1234", "유연주", "소개글");
        String authorId = author.getSubjectId();

        // 2) 레포지토리 & 알림(테스트용) & 서비스 구성
        EpisodeRepository episodeRepo = new InMemoryEpisodeRepository();
        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
        AuthorRepository authorRepo = new InMemoryAuthorRepository(); // 필요 시 확장용 (이번 테스트에선 직접 사용 X)

        TestNotificationService notifier = new TestNotificationService();
        WebtoonService webtoonService = new WebtoonService(webtoonRepo, episodeRepo, notifier);

        // 3) 웹툰 생성
        Webtoon toon = webtoonService.createWebtoon("밤의 상점", authorId);
        assertNotNull(toon.getId());
        assertEquals(authorId, toon.getAuthorId());

        // 4) 팔로우 2명
        webtoonService.followWebtoon(toon.getId(), "reader-001");
        webtoonService.followWebtoon(toon.getId(), "reader-002");
        // 저장된 객체 기준으로도 팔로워 수 확인
        Webtoon stored = webtoonRepo.findById(toon.getId()).orElseThrow();
        assertEquals(2, stored.getFollowerUserIds().size());

        // 5) 회차 2개 발행 (번호 자동 증가)
        Episode ep1 = webtoonService.publishEpisode(toon.getId(), "1화. 첫 손님", "내용1", 50, 100);
        Episode ep2 = webtoonService.publishEpisode(toon.getId(), "2화. 비밀 주문서", "내용2", 50, 100);
        assertEquals(1, ep1.getNumber());
        assertEquals(2, ep2.getNumber());

        // 6) 저장소 정합성: 해당 작품의 회차는 2개, 번호 오름차순
        List<Episode> episodes = episodeRepo.findByWebtoonId(toon.getId());
        assertEquals(2, episodes.size());
        assertTrue(episodes.get(0).getNumber() < episodes.get(1).getNumber());

        // 7) 알림 검증: 팔로워 2명 × 회차 2개 = 4건
        assertEquals(4, notifier.getNotices().size());
        TestNotificationService.Notice first = notifier.getNotices().get(0);
        assertTrue(first.userId.startsWith("reader-"));
        assertEquals("밤의 상점", first.webtoonTitle);
        assertTrue(first.episodeNumber == 1 || first.episodeNumber == 2);
    }
}
