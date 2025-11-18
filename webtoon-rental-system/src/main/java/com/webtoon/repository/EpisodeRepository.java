package com.webtoon.repository;

import com.webtoon.domain.Episode;
import java.util.*;

/**
 * 회차 저장소 인터페이스
 * - ID: String (UUID 등)
 * - 정렬 규칙: findByWebtoonId()는 회차 number 오름차순 반환 권장
 */
public interface EpisodeRepository {

    Episode save(Episode episode);

    Optional<Episode> findById(String episodeId);

    List<Episode> findByWebtoonId(String webtoonId);

    Optional<Episode> findLatestByWebtoonId(String webtoonId);

    void deleteById(String episodeId);
}
