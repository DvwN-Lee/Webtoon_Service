package com.webtoon.service;

import com.webtoon.domain.*;
import com.webtoon.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsServiceTest {

    private StatisticsService statisticsService;
    private StatisticsRepository statisticsRepository;

    @BeforeEach
    void setUp() {
        statisticsRepository = new InMemoryStatisticsRepository();
        statisticsService = new StatisticsService(statisticsRepository);
    }

    @Test
    @DisplayName("새 웹툰에 에피소드를 한 번 생성하면 episodeCount가 1이 된다")
    void onEpisodeCreated_once_makes_episodeCount_1() {
        // given
        Long webtoonId = 1L;

        // when
        statisticsService.onEpisodeCreated(webtoonId);

        // then
        assertEquals(1, statisticsService.getEpisodeCount(webtoonId));
        assertEquals(0L, statisticsService.getTotalViews(webtoonId)); // 조회수는 그대로 0
    }

    @Test
    @DisplayName("같은 웹툰에 에피소드를 여러 번 생성하면 episodeCount가 누적된다")
    void onEpisodeCreated_multipleTimes_accumulates_episodeCount() {
        // given
        Long webtoonId = 1L;

        // when
        statisticsService.onEpisodeCreated(webtoonId);
        statisticsService.onEpisodeCreated(webtoonId);
        statisticsService.onEpisodeCreated(webtoonId);

        // then
        assertEquals(3, statisticsService.getEpisodeCount(webtoonId));
    }

    @Test
    @DisplayName("에피소드 생성 후 삭제하면 episodeCount가 감소한다(0보다 내려가지 않는다)")
    void onEpisodeDeleted_decreases_episodeCount() {
        // given
        Long webtoonId = 1L;

        // 3개 생성
        statisticsService.onEpisodeCreated(webtoonId);
        statisticsService.onEpisodeCreated(webtoonId);
        statisticsService.onEpisodeCreated(webtoonId);

        // when - 1개 삭제
        statisticsService.onEpisodeDeleted(webtoonId);

        // then
        assertEquals(2, statisticsService.getEpisodeCount(webtoonId));
    }

    @Test
    @DisplayName("조회수가 증가하면 totalViews가 누적된다")
    void onViewIncreased_accumulates_totalViews() {
        // given
        Long webtoonId = 1L;

        // when
        statisticsService.onViewIncreased(webtoonId);
        statisticsService.onViewIncreased(webtoonId);
        statisticsService.onViewIncreased(webtoonId);
        statisticsService.onViewIncreased(webtoonId);
        statisticsService.onViewIncreased(webtoonId);

        // then
        assertEquals(5L, statisticsService.getTotalViews(webtoonId));
        // 에피소드 수는 0(ensure로 생성만 됨)
        assertEquals(0, statisticsService.getEpisodeCount(webtoonId));
    }

    @Test
    @DisplayName("아무 이벤트가 없어도 조회 시 기본값 0/0을 반환한다")
    void getCounts_without_any_event_returns_zero() {
        // given
        Long webtoonId = 99L;

        // when & then
        assertEquals(0, statisticsService.getEpisodeCount(webtoonId));
        assertEquals(0L, statisticsService.getTotalViews(webtoonId));
    }

    @Test
    @DisplayName("서로 다른 웹툰의 통계는 서로 독립적으로 관리된다")
    void statistics_are_independent_per_webtoon() {
        // given
        Long webtoonA = 1L;
        Long webtoonB = 2L;

        // when
        statisticsService.onEpisodeCreated(webtoonA);   // A: ep 1
        statisticsService.onEpisodeCreated(webtoonA);   // A: ep 2
        statisticsService.onViewIncreased(webtoonA);    // A: view 1

        statisticsService.onEpisodeCreated(webtoonB);   // B: ep 1
        statisticsService.onViewIncreased(webtoonB);    // B: view 1
        statisticsService.onViewIncreased(webtoonB);    // B: view 2

        // then
        assertEquals(2,  statisticsService.getEpisodeCount(webtoonA));
        assertEquals(1L, statisticsService.getTotalViews(webtoonA));

        assertEquals(1,  statisticsService.getEpisodeCount(webtoonB));
        assertEquals(2L, statisticsService.getTotalViews(webtoonB));
    }

    @Test
    @DisplayName("작가 단위 통계 조회 - 여러 웹툰의 통계 합산")
    void getAuthorStats_aggregatesMultipleWebtoons() {
        // given: WebtoonRepository를 포함한 StatisticsService 생성
        StatisticsRepository statsRepo = new InMemoryStatisticsRepository();
        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
        StatisticsService service = new StatisticsService(statsRepo, webtoonRepo);

        // 작가 생성
        Author author = new Author("author123", "pass1234", "테스트작가", "소개");
        author.setId(1L);
        Long authorId = author.getId();

        // 작가의 웹툰 3개 생성
        Webtoon w1 = new Webtoon(1L, "웹툰A", authorId, Arrays.asList("판타지"), "ONGOING", "요약A");
        Webtoon w2 = new Webtoon(2L, "웹툰B", authorId, Arrays.asList("로맨스"), "ONGOING", "요약B");
        Webtoon w3 = new Webtoon(3L, "웹툰C", authorId, Arrays.asList("액션"), "COMPLETED", "요약C");

        webtoonRepo.save(w1);
        webtoonRepo.save(w2);
        webtoonRepo.save(w3);

        // Author 도메인에도 웹툰 추가
        author.createWebtoon(w1);
        author.createWebtoon(w2);
        author.createWebtoon(w3);

        // 각 웹툰별로 통계 생성
        // 웹툰A: 회차 2개, 조회수 10
        service.onEpisodeCreated(1L);
        service.onEpisodeCreated(1L);
        for (int i = 0; i < 10; i++) service.onViewIncreased(1L);

        // 웹툰B: 회차 3개, 조회수 20
        service.onEpisodeCreated(2L);
        service.onEpisodeCreated(2L);
        service.onEpisodeCreated(2L);
        for (int i = 0; i < 20; i++) service.onViewIncreased(2L);

        // 웹툰C: 회차 1개, 조회수 5
        service.onEpisodeCreated(3L);
        for (int i = 0; i < 5; i++) service.onViewIncreased(3L);

        // when
        AuthorStats stats = service.getAuthorStats(author);

        // then
        assertNotNull(stats);
        assertEquals(String.valueOf(authorId), stats.getAuthorId());
        assertEquals("테스트작가", stats.getAuthorName());
        assertEquals(3, stats.getWebtoonCount(), "작가의 웹툰 수는 3개");
        assertEquals(6, stats.getTotalEpisodeCount(), "총 회차 수는 2+3+1=6");
        assertEquals(35L, stats.getTotalViews(), "총 조회수는 10+20+5=35");
    }

    @Test
    @DisplayName("작가 단위 통계 조회 - 웹툰이 없는 작가")
    void getAuthorStats_noWebtoons() {
        // given
        StatisticsRepository statsRepo = new InMemoryStatisticsRepository();
        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
        StatisticsService service = new StatisticsService(statsRepo, webtoonRepo);

        Author author = new Author("newauthor", "pass1234", "신인작가", "소개");
        author.setId(100L);

        // when
        AuthorStats stats = service.getAuthorStats(author);

        // then
        assertNotNull(stats);
        assertEquals(0, stats.getWebtoonCount());
        assertEquals(0, stats.getTotalEpisodeCount());
        assertEquals(0L, stats.getTotalViews());
    }

    @Test
    @DisplayName("작가 통계 조회 시 null Author는 예외 발생")
    void getAuthorStats_nullAuthor() {
        // given
        StatisticsRepository statsRepo = new InMemoryStatisticsRepository();
        WebtoonRepository webtoonRepo = new InMemoryWebtoonRepository();
        StatisticsService service = new StatisticsService(statsRepo, webtoonRepo);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            service.getAuthorStats(null);
        });
    }

    @Test
    @DisplayName("회차 단위 통계 조회 - Episode 객체에서 조회수 반환")
    void getEpisodeStats_returnsViewCount() {
        // given
        Episode episode = new Episode(
                null,
                1L,
                5,
                "5화. 테스트",
                "내용",
                50,
                100
        );

        // 조회수 증가
        episode.incrementViewCount();
        episode.incrementViewCount();
        episode.incrementViewCount();

        StatisticsRepository statsRepo = new InMemoryStatisticsRepository();
        StatisticsService service = new StatisticsService(statsRepo);

        // when
        EpisodeStats stats = service.getEpisodeStats(episode);

        // then
        assertNotNull(stats);
        assertEquals(episode.getId(), stats.getEpisodeId());
        assertEquals(episode.getWebtoonId(), stats.getWebtoonId());
        assertEquals(5, stats.getEpisodeNumber());
        assertEquals(3, stats.getViewCount());
    }

    @Test
    @DisplayName("회차 통계 조회 시 null Episode는 예외 발생")
    void getEpisodeStats_nullEpisode() {
        // given
        StatisticsRepository statsRepo = new InMemoryStatisticsRepository();
        StatisticsService service = new StatisticsService(statsRepo);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            service.getEpisodeStats(null);
        });
    }
}
