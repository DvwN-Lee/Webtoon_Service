package com.webtoon.repository;

import com.webtoon.domain.Episode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 메모리 기반 회차 저장소 구현체
 * - 실제로는 DB 대신 HashMap을 사용함
 */
public class InMemoryEpisodeRepository implements EpisodeRepository {

    // episodeId → Episode
    private final Map<String, Episode> store = new ConcurrentHashMap<>();

    @Override
    public Episode save(Episode episode) {
        store.put(episode.getId(), episode);
        return episode;
    }

    @Override
    public Optional<Episode> findById(String episodeId) {
        return Optional.ofNullable(store.get(episodeId));
    }

    @Override
    public List<Episode> findByWebtoonId(String webtoonId) {
        return store.values().stream()
                .filter(e -> e.getWebtoonId().equals(webtoonId))
                .sorted(Comparator.comparingInt(Episode::getNumber))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Episode> findLatestByWebtoonId(String webtoonId) {
        return store.values().stream()
                .filter(e -> e.getWebtoonId().equals(webtoonId))
                .max(Comparator.comparingInt(Episode::getNumber));
    }

    @Override
    public void deleteById(String episodeId) {
        store.remove(episodeId);
    }
}
