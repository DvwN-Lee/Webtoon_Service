package com.webtoon.repository;


import com.webtoon.domain.Author;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 메모리 기반 작가 저장소 구현
 */
public class InMemoryAuthorRepository implements AuthorRepository {

    // authorId(String) -> Author
    private final Map<String, Author> store = new ConcurrentHashMap<>();

    @Override
    public Author save(Author author) {
        // Author#getSubjectId() 는 String 타입(ID)이어야 함
        store.put(author.getSubjectId(), author);
        return author;
    }

    @Override
    public Optional<Author> findById(String authorId) {
        return Optional.ofNullable(store.get(authorId));
    }

    @Override
    public List<Author> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String authorId) {
        store.remove(authorId);
    }
}

