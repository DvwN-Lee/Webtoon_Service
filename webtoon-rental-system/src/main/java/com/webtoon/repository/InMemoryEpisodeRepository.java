package com.webtoon.repository;

import com.webtoon.domain.Episode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//public class InMemoryEpisodeRepository implements EpisodeRepository {
//
//    // episodeId → Episode
//    private final Map<Long, Episode> store = new ConcurrentHashMap<>();
//
//    @Override
//    public Episode save(Episode episode) {
//        store.put(episode.getId(), episode);
//        return episode;
//    }
//
//    @Override
//    public Optional<Episode> findById(Long episodeId) {
//        return Optional.ofNullable(store.get(episodeId));
//    }
//
//    @Override
//    public List<Episode> findByWebtoonId(Long webtoonId) {
//        return store.values().stream()
//                .filter(e -> e.getWebtoonId().equals(webtoonId))
//                .sorted(Comparator.comparingInt(Episode::getNumber))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Optional<Episode> findLatestByWebtoonId(Long webtoonId) {
//        return store.values().stream()
//                .filter(e -> e.getWebtoonId().equals(webtoonId))
//                .max(Comparator.comparingInt(Episode::getNumber));
//    }
//
//    @Override
//    public void deleteById(Long episodeId) {
//        store.remove(episodeId);
//    }
//}

/**
 * 메모리 기반 회차 저장소 구현체
 * - 실제로는 DB 대신 HashMap을 사용함
 */
public class InMemoryEpisodeRepository implements EpisodeRepository {

    // episodeId(Long) → Episode
    private final Map<Long, Episode> store = new ConcurrentHashMap<>();

    @Override
    public Episode save(Episode episode) {
        // 여기서는 id가 이미 세팅되어 있다고 가정
        Long id = episode.getId();
        if (id == null) {
            throw new IllegalStateException("Episode ID가 null입니다. 저장 전에 ID를 세팅해야 합니다.");
        }
        store.put(id, episode);
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
