//package com.webtoon.service;
//
//import com.webtoon.domain.Author;
//import com.webtoon.domain.Episode;
//import com.webtoon.domain.Webtoon;
//import com.webtoon.repository.*;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class EpisodeServiceTest {
//
//    @Test
//    void 회차_조회_수정_삭제_흐름() {
//        EpisodeRepository episodeRepo = new InMemoryEpisodeRepository();
//        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
//        AuthorRepository authorRepo = new InMemoryAuthorRepository();
//
//        TestNotificationService notifier = new TestNotificationService();
//        WebtoonService webtoonService = new WebtoonService(webtoonRepo, episodeRepo, notifier);
//        EpisodeService episodeService = new EpisodeService(episodeRepo);
//
//        AuthService auth = new AuthService();
//        String suf1 = String.format("%04d", (int)(Math.random() * 10000));
//        Author author = auth.registerAuthor(
//                "authorX_" + suf1,
//                "pass1234",
//                "작가" + suf1,   // 2~10자 충족
//                "소개"
//        );
//        String authorId = author.getSubjectId();
//
//        Webtoon toon = webtoonService.createWebtoon("테스트 웹툰_" + suf1, authorId);
//
//        webtoonService.followWebtoon(toon.getId(), "reader-01");
//
//        Episode ep = webtoonService.publishEpisode(
//                toon.getId(), "1화. 시작", "본문1", 50, 100
//        );
//
//        Episode found = episodeService.findById(ep.getId());
//        assertEquals("1화. 시작", found.getTitle());
//        assertEquals("본문1", found.getContent());
//        assertEquals(50, found.getRentPrice());
//        assertEquals(100, found.getBuyPrice());
//
//        episodeService.updateTitle(ep.getId(), "1화. 수정제목");
//        episodeService.updateContent(ep.getId(), "본문1-수정");
//        episodeService.updatePrices(ep.getId(), 60, 120);
//
//        Episode updated = episodeService.findById(ep.getId());
//        assertEquals("1화. 수정제목", updated.getTitle());
//        assertEquals("본문1-수정", updated.getContent());
//        assertEquals(60, updated.getRentPrice());
//
//        List<Episode> list = episodeService.findByWebtoonId(toon.getId());
//        assertEquals(1, list.size());
//        assertEquals(updated.getId(), list.get(0).getId());
//
//        episodeService.delete(ep.getId());
//        assertTrue(episodeRepo.findById(ep.getId()).isEmpty());
//        assertEquals(0, episodeRepo.findByWebtoonId(toon.getId()).size());
//    }
//
//    @Test
//    void 가격_유효성_검증_실패() {
//        EpisodeRepository episodeRepo = new InMemoryEpisodeRepository();
//        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
//
//        TestNotificationService notifier = new TestNotificationService();
//        WebtoonService webtoonService = new WebtoonService(webtoonRepo, episodeRepo, notifier);
//        EpisodeService episodeService = new EpisodeService(episodeRepo);
//
//        AuthService auth = new AuthService();
//        String suf2 = String.format("%04d", (int)(Math.random() * 10000));
//        Author author = auth.registerAuthor(
//                "authorY_" + suf2,
//                "pass1234",
//                "작가" + suf2,   // 2~10자 충족
//                "소개"
//        );
//        String authorId = author.getSubjectId();
//
//        Webtoon toon = webtoonService.createWebtoon("검증 웹툰_" + suf2, authorId);
//        Episode ep = webtoonService.publishEpisode(toon.getId(), "1화", "본문", 50, 100);
//
//        assertThrows(IllegalArgumentException.class,
//                () -> episodeService.updatePrices(ep.getId(), -1, 100));
//
//        assertThrows(IllegalArgumentException.class,
//                () -> episodeService.updatePrices(ep.getId(), 80, 60));
//    }
//}
