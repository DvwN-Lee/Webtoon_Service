package com.webtoon.repository;

import com.webtoon.domain.Author;
import java.util.*;

/**
 * 작가 저장소 인터페이스
 * - ID: Long (Repository에서 자동 생성)
 */
public interface AuthorRepository {

    Author save(Author author);

    Optional<Author> findById(Long authorId);

    List<Author> findAll();

    void deleteById(Long authorId);
}
