package com.webtoon.repository;

import com.webtoon.domain.Episode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 메모리 기반 회차 저장소 구현체
 * - 실제로는 DB 대신 HashMap을 사용함
 */
public class InMemoryEpisodeRepository implements EpisodeRepository {

    private final Map<Long, Episode> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Episode save(Episode episode) {
        if (episode.getId() == null) {
            episode.setId(idGenerator.getAndIncrement());
        }
        store.put(episode.getId(), episode);
        return episode;
    }

    @Override
    public Optional<Episode> findById(Long episodeId) {
        return Optional.ofNullable(store.get(episodeId));
    }

    @Override
    public List<Episode> findByWebtoonId(Long webtoonId) {
        return store.values().stream()
                .filter(e -> Objects.equals(e.getWebtoonId(), webtoonId))
                .sorted(Comparator.comparingInt(Episode::getNumber))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Episode> findLatestByWebtoonId(Long webtoonId) {
        return store.values().stream()
                .filter(e -> Objects.equals(e.getWebtoonId(), webtoonId))
                .max(Comparator.comparingInt(Episode::getNumber));
    }

    @Override
    public void deleteById(Long episodeId) {
        store.remove(episodeId);
    }
}
