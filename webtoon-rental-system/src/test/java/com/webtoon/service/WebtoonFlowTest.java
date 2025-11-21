//package com.webtoon.service;
//
//import com.webtoon.domain.Author;
//import com.webtoon.domain.Episode;
//import com.webtoon.domain.Webtoon;
//import com.webtoon.repository.EpisodeRepository;
//import com.webtoon.repository.InMemoryEpisodeRepository;
//import com.webtoon.repository.InMemoryWebtoonRepository;
//import com.webtoon.repository.UserRepository;
//import com.webtoon.repository.WebtoonRepository;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class WebtoonFlowTest {
//
//    /**
//     * 실제 NotificationService를 상속해서
//     * 콘솔 출력 대신 메모리에 알림을 적재하는 테스트용 구현.
//     */
//    static class TestNotificationService extends NotificationService {
//
//        static class Notice {
//            final String userId;
//            final String webtoonTitle;
//            final String episodeTitle;
//            final int episodeNumber;
//
//            Notice(String userId, String webtoonTitle, String episodeTitle, int episodeNumber) {
//                this.userId = userId;
//                this.webtoonTitle = webtoonTitle;
//                this.episodeTitle = episodeTitle;
//                this.episodeNumber = episodeNumber;
//            }
//        }
//
//        private final List<Notice> notices = new ArrayList<>();
//
//        @Override
//        public void notifyNewEpisode(Webtoon webtoon, Episode episode) {
//            // Webtoon의 팔로워 목록을 순회하면서 알림 기록
//            for (String userId : webtoon.getFollowerUserIds()) {
//                notices.add(new Notice(
//                        userId,
//                        webtoon.getTitle(),
//                        episode.getTitle(),
//                        episode.getNumber()
//                ));
//            }
//        }
//
//        public List<Notice> getNotices() {
//            return notices;
//        }
//    }
//
//    @Test
//    void 작품생성_팔로우_회차발행_알림검증() {
//        // 🔹 username / authorName이 다른 테스트와 안 겹치도록 suffix 하나 만들어두기
////        String suffix = String.valueOf(System.nanoTime());
////        String username = "author-" + suffix;
////        String authorName = "유땅콩-" + suffix;
//        String username = "author5678";   // 5~20자 범위 OK
//        String authorName = "peanut";      // 2~10자 범위 OK
//
//        // 1) UserRepository를 테스트에서 직접 생성해서 AuthService에 "주입"
//        UserRepository userRepo = new UserRepository();
//        AuthService auth = new AuthService(userRepo);
//
//        // 2) 작가 회원가입
//        Author author = auth.registerAuthor(username, "pass1234", authorName, "소개글");
//        // Author는 Subject가 아니므로 User의 id를 문자열로 사용
//        String authorId = String.valueOf(author.getId());
//
//        // 3) 레포지토리 & 알림(테스트용) & 서비스 구성
//        EpisodeRepository episodeRepo = new InMemoryEpisodeRepository();
//        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
//        TestNotificationService notifier = new TestNotificationService();
//        WebtoonService webtoonService = new WebtoonService(webtoonRepo, episodeRepo, notifier);
//
//        // 4) 웹툰 생성
//        Webtoon toon = webtoonService.createWebtoon("밤의 상점", authorId);
//        assertNotNull(toon.getId());
//        assertEquals(authorId, toon.getAuthorId());
//
//        // 5) 팔로우 2명
//        webtoonService.followWebtoon(toon.getId(), "reader-001");
//        webtoonService.followWebtoon(toon.getId(), "reader-002");
//
//        // 저장된 객체 기준으로도 팔로워 수 확인
//        Webtoon stored = webtoonRepo.findById(toon.getId()).orElseThrow();
//        assertEquals(2, stored.getFollowerUserIds().size());
//
//        // 6) 회차 2개 발행 (번호 자동 증가)
//        Episode ep1 = webtoonService.publishEpisode(toon.getId(), "1화. 첫 손님", "내용1", 50, 100);
//        Episode ep2 = webtoonService.publishEpisode(toon.getId(), "2화. 비밀 주문서", "내용2", 50, 100);
//        assertEquals(1, ep1.getNumber());
//        assertEquals(2, ep2.getNumber());
//
//        // 7) 저장소 정합성: 해당 작품의 회차는 2개, 번호 오름차순
//        List<Episode> episodes = episodeRepo.findByWebtoonId(toon.getId());
//        assertEquals(2, episodes.size());
//        assertEquals(1, episodes.get(0).getNumber());
//        assertEquals(2, episodes.get(1).getNumber());
//
//        // 8) 알림 검증: 팔로워 2명 × 회차 2개 = 4건
//        assertEquals(4, notifier.getNotices().size());
//
//        TestNotificationService.Notice first = notifier.getNotices().get(0);
//        assertTrue(first.userId.startsWith("reader-"));
//        assertEquals("밤의 상점", first.webtoonTitle);
//        assertTrue(first.episodeNumber == 1 || first.episodeNumber == 2);
//    }
//}

package com.webtoon.service;

import com.webtoon.domain.Author;
import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.EpisodeRepository;
import com.webtoon.repository.InMemoryEpisodeRepository;
import com.webtoon.repository.InMemoryWebtoonRepository;
import com.webtoon.repository.UserRepository;
import com.webtoon.repository.WebtoonRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebtoonFlowTest {

    /**
     * 실제 NotificationService를 상속해서
     * 콘솔 출력 대신 메모리에 알림을 적재하는 테스트용 구현.
     */
    static class TestNotificationService extends NotificationService {

        static class Notice {
            final String userId;
            final String webtoonTitle;
            final String episodeTitle;
            final int episodeNumber;

            Notice(String userId, String webtoonTitle, String episodeTitle, int episodeNumber) {
                this.userId = userId;
                this.webtoonTitle = webtoonTitle;
                this.episodeTitle = episodeTitle;
                this.episodeNumber = episodeNumber;
            }
        }

        private final List<Notice> notices = new ArrayList<>();

        @Override
        public void notifyNewEpisode(Webtoon webtoon, Episode episode) {
            // Webtoon의 팔로워 목록을 순회하면서 알림 기록
            for (String userId : webtoon.getFollowerUserIds()) {
                notices.add(new Notice(
                        userId,
                        webtoon.getTitle(),
                        episode.getTitle(),
                        episode.getNumber()
                ));
            }
        }

        public List<Notice> getNotices() {
            return notices;
        }
    }

    @Test
    void 작품생성_팔로우_회차발행_알림검증() {
        // username / authorName
        String username = "author5678";   // 5~20자 범위 OK
        String authorName = "peanut";    // 2~10자 범위 OK

        // 1) UserRepository를 테스트에서 직접 생성해서 AuthService에 "주입"
        UserRepository userRepo = new UserRepository();
        AuthService auth = new AuthService(userRepo);

        // 2) 작가 회원가입
        Author author = auth.registerAuthor(username, "pass1234", authorName, "소개글");
        // Author는 Subject가 아니므로 User의 id를 문자열로 사용
        String authorId = String.valueOf(author.getId());

        // 3) 레포지토리 & 알림(테스트용) & 서비스 구성
        EpisodeRepository episodeRepo = new InMemoryEpisodeRepository();
        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
        TestNotificationService notifier = new TestNotificationService();
        WebtoonService webtoonService = new WebtoonService(webtoonRepo, episodeRepo, notifier);

        // 4) 웹툰 생성
        Webtoon toon = webtoonService.createWebtoon("밤의 상점", authorId);
        assertNotNull(toon.getId());
        assertEquals(authorId, toon.getAuthorId());

        // Long 기반 ID로 명시
        Long webtoonId = toon.getId();

        // 5) 팔로우 2명
        webtoonService.followWebtoon(webtoonId, "reader-001");
        webtoonService.followWebtoon(webtoonId, "reader-002");

        // 저장된 객체 기준으로도 팔로워 수 확인
        Webtoon stored = webtoonRepo.findById(webtoonId).orElseThrow();
        assertEquals(2, stored.getFollowerUserIds().size());

        // 6) 회차 2개 발행 (번호 자동 증가)
        Episode ep1 = webtoonService.publishEpisode(webtoonId, "1화. 첫 손님", "내용1", 50, 100);
        Episode ep2 = webtoonService.publishEpisode(webtoonId, "2화. 비밀 주문서", "내용2", 50, 100);
        assertEquals(1, ep1.getNumber());
        assertEquals(2, ep2.getNumber());

        // 7) 저장소 정합성: 해당 작품의 회차는 2개, 번호 오름차순
        List<Episode> episodes = episodeRepo.findByWebtoonId(webtoonId);
        assertEquals(2, episodes.size());
        assertEquals(1, episodes.get(0).getNumber());
        assertEquals(2, episodes.get(1).getNumber());

        // 8) 알림 검증: 팔로워 2명 × 회차 2개 = 4건
        assertEquals(4, notifier.getNotices().size());

        TestNotificationService.Notice first = notifier.getNotices().get(0);
        assertTrue(first.userId.startsWith("reader-"));
        assertEquals("밤의 상점", first.webtoonTitle);
        assertTrue(first.episodeNumber == 1 || first.episodeNumber == 2);
    }
}
