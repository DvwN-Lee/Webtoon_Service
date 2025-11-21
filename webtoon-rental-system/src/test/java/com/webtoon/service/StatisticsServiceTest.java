package com.webtoon.service;

import com.webtoon.repository.InMemoryStatisticsRepository;
import com.webtoon.repository.StatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
