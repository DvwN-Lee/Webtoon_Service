package com.webtoon.repository;

import com.webtoon.domain.Webtoon;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 메모리 기반 웹툰 저장소 구현
 */
public class InMemoryWebtoonRepository implements WebtoonRepository {

    // webtoonId -> Webtoon
    private final Map<String, Webtoon> store = new ConcurrentHashMap<>();

    @Override
    public Webtoon save(Webtoon webtoon) {
        store.put(webtoon.getId(), webtoon);
        return webtoon;
    }

    @Override
    public Optional<Webtoon> findById(String webtoonId) {
        return Optional.ofNullable(store.get(webtoonId));
    }

    @Override
    public List<Webtoon> findByAuthorId(String authorId) {
        return store.values().stream()
                .filter(w -> Objects.equals(w.getAuthorId(), authorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Webtoon> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String webtoonId) {
        store.remove(webtoonId);
    }
}
