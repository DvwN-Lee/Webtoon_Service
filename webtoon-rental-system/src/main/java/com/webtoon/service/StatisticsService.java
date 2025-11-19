package com.webtoon.service;

import com.webtoon.domain.Statistics;
import com.webtoon.repository.StatisticsRepository;

public class StatisticsService {
    private final StatisticsRepository repo;

    public StatisticsService(StatisticsRepository repo) { this.repo = repo; }

    private Statistics ensure(Long webtoonId) {
        return repo.findByWebtoonId(webtoonId)
                .orElseGet(() -> repo.save(new Statistics(webtoonId)));
    }

    // 사용 메서드(최소)
    public void onEpisodeCreated(Long webtoonId) { ensure(webtoonId).incEpisode(); }
    public void onEpisodeDeleted(Long webtoonId) { ensure(webtoonId).decEpisode(); }
    public void onViewIncreased(Long webtoonId)  { ensure(webtoonId).incView(); }

    // 조회
    public int getEpisodeCount(Long webtoonId) { return ensure(webtoonId).getEpisodeCount(); }
    public long getTotalViews(Long webtoonId)  { return ensure(webtoonId).getTotalViews(); }
}
