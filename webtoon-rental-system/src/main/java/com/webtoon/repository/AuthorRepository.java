package com.webtoon.repository;

import com.webtoon.domain.Author;
import java.util.*;

/**
 * 작가 저장소 인터페이스
 * - ID는 String 사용 (Author#getSubjectId() 등과 정합)
 */
public interface AuthorRepository {

    Author save(Author author);

    Optional<Author> findById(String authorId);

    List<Author> findAll();

    void deleteById(String authorId);
}
