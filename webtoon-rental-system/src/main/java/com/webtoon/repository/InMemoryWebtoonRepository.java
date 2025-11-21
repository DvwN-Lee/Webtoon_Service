package com.webtoon.repository;

import com.webtoon.domain.Webtoon;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 메모리 기반 웹툰 저장소 구현
 */
public class InMemoryWebtoonRepository implements WebtoonRepository {

    private final Map<Long, Webtoon> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Webtoon save(Webtoon webtoon) {
        if (webtoon.getId() == null) {
            webtoon.setId(idGenerator.getAndIncrement());
        }
        store.put(webtoon.getId(), webtoon);
        return webtoon;
    }

    @Override
    public Optional<Webtoon> findById(Long webtoonId) {
        return Optional.ofNullable(store.get(webtoonId));
    }

    @Override
    public List<Webtoon> findByAuthorId(Long authorId) {
        return store.values().stream()
                .filter(w -> Objects.equals(w.getAuthorId(), authorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Webtoon> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long webtoonId) {
        store.remove(webtoonId);
    }

    @Override
    public List<Webtoon> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String lower = keyword.toLowerCase();
        return store.values().stream()
                .filter(w -> w.getTitle() != null &&
                        w.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }
}
