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
//
//    /**
//     * 제목 키워드 검색
//     */
//    List<Webtoon> searchByTitle(String keyword);
//}

package com.webtoon.repository;

import com.webtoon.domain.Webtoon;
import java.util.*;

/**
 * 웹툰 저장소 인터페이스
 * - ID: Long
 */
public interface WebtoonRepository {

    Webtoon save(Webtoon webtoon);

    /** ID로 단일 웹툰 조회 */
    Optional<Webtoon> findById(Long webtoonId);

    /** 특정 작가가 가진 모든 웹툰 */
    List<Webtoon> findByAuthorId(String authorId);

    /** 전체 목록 조회 (간단한 콘솔/관리용) */
    List<Webtoon> findAll();

    /** ID로 웹툰 삭제 */
    void deleteById(Long webtoonId);

    /** 제목 키워드 검색 */
    List<Webtoon> searchByTitle(String keyword);
}
