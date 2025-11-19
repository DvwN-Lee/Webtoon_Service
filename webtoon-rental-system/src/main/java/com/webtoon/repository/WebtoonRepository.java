//package com.webtoon.repository;
//
//import com.webtoon.domain.Webtoon;
//import java.util.*;
//
///**
// * 웹툰 저장소 인터페이스
// * - ID: String (예: UUID)
// */
//public interface WebtoonRepository {
//
//    Webtoon save(Webtoon webtoon);
//
//    Optional<Webtoon> findById(String webtoonId);
//
//    /**
//     * 특정 작가가 가진 모든 웹툰
//     */
//    List<Webtoon> findByAuthorId(String authorId);
//
//    /**
//     * 전체 목록 조회 (간단한 콘솔/관리용)
//     */
//    List<Webtoon> findAll();
//
//    void deleteById(String webtoonId);
//}

//package com.webtoon.repository;
//
//import com.webtoon.domain.Author;
//import com.webtoon.domain.Webtoon;
//
//import java.util.List;
//import java.util.Optional;
//
///**
// * 웹툰 저장소 인터페이스
// * - ID: Long
// */
//public interface WebtoonRepository {
//
//    Webtoon save(Webtoon webtoon);
//
//    Optional<Webtoon> findById(Long webtoonId);
//
//    /** 특정 작가의 모든 웹툰 */
//    List<Webtoon> findByAuthor(Author author);
//
//    /** 제목 키워드 검색 (부분 일치) */
//    List<Webtoon> searchByTitle(String keyword);
//
//    /** 전체 목록 조회 */
//    List<Webtoon> findAll();
//
//    void deleteById(Long webtoonId);
//}

package com.webtoon.repository;

import com.webtoon.domain.Webtoon;
import java.util.*;

/**
 * 웹툰 저장소 인터페이스
 * - ID: String (예: UUID)
 */
public interface WebtoonRepository {

    Webtoon save(Webtoon webtoon);

    Optional<Webtoon> findById(String webtoonId);

    /**
     * 특정 작가가 가진 모든 웹툰
     */
    List<Webtoon> findByAuthorId(String authorId);

    /**
     * 전체 목록 조회 (간단한 콘솔/관리용)
     */
    List<Webtoon> findAll();

    void deleteById(String webtoonId);

    /**
     * 제목 키워드 검색
     */
    List<Webtoon> searchByTitle(String keyword);
}
