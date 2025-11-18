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

    Optional<Episode> findById(Long episodeId);

    List<Episode> findByWebtoonId(Long webtoonId);

    Optional<Episode> findLatestByWebtoonId(Long webtoonId);

    void deleteById(Long episodeId);
}
