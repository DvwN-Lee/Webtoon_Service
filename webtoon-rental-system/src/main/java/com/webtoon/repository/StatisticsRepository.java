package com.webtoon.repository;

import com.webtoon.domain.Statistics;
import java.util.Optional;

public interface StatisticsRepository {
    Optional<Statistics> findByWebtoonId(Long webtoonId);
    Statistics save(Statistics s);
}

