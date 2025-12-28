package com.webtoon.repository;

import com.webtoon.domain.Statistics;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryStatisticsRepository implements StatisticsRepository {
    private final Map<Long, Statistics> store = new HashMap<>();

    @Override public Optional<Statistics> findByWebtoonId(Long id) {
        return Optional.ofNullable(store.get(id));
    }
    @Override public Statistics save(Statistics s) {
        store.put(s.getWebtoonId(), s);
        return s;
    }
}
