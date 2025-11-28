package com.webtoon.common.repository;

import com.webtoon.domain.Reader;
import com.webtoon.repository.ReaderRepository;

import java.util.*;

public class InMemoryReaderRepository extends ReaderRepository {

    private final Map<Long, Reader> store = new HashMap<>();

    // 저장
    public Reader save(Reader entity) {
        if (entity.getId() == null) {
            entity.setId((long) (store.size() + 1));
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    // 업데이트
    public void update(Reader entity) {
        store.put(entity.getId(), entity);
    }

    // ID로 조회
    public Optional<Reader> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    // 전체 조회
    public List<Reader> findAll() {
        return new ArrayList<>(store.values());
    }
}


