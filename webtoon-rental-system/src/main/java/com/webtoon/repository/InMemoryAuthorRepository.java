package com.webtoon.repository;


import com.webtoon.domain.Author;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 메모리 기반 작가 저장소 구현
 */
public class InMemoryAuthorRepository implements AuthorRepository {

    private final Map<Long, Author> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Author save(Author author) {
        if (author.getId() == null) {
            author.setId(idGenerator.getAndIncrement());
        }
        store.put(author.getId(), author);
        return author;
    }

    @Override
    public Optional<Author> findById(Long authorId) {
        return Optional.ofNullable(store.get(authorId));
    }

    @Override
    public List<Author> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long authorId) {
        store.remove(authorId);
    }
}

