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
        // Author의 User.id를 문자열로 변환해 키로 사용
        String authorId = String.valueOf(author.getId());
        store.put(authorId, author);
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

