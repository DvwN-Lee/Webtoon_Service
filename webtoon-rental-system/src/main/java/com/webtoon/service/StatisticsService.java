//package com.webtoon.service;
//
//import com.webtoon.domain.Statistics;
//import com.webtoon.repository.StatisticsRepository;
//
//public class StatisticsService {
//    private final StatisticsRepository repo;
//
//    public StatisticsService(StatisticsRepository repo) { this.repo = repo; }
//
//    private Statistics ensure(Long webtoonId) {
//        return repo.findByWebtoonId(webtoonId)
//                .orElseGet(() -> repo.save(new Statistics(webtoonId)));
//    }
//
//    // 사용 메서드(최소)
//    public void onEpisodeCreated(Long webtoonId) { ensure(webtoonId).incEpisode(); }
//    public void onEpisodeDeleted(Long webtoonId) { ensure(webtoonId).decEpisode(); }
//    public void onViewIncreased(Long webtoonId)  { ensure(webtoonId).incView(); }
//
//    // 조회
//    public int getEpisodeCount(Long webtoonId) { return ensure(webtoonId).getEpisodeCount(); }
//    public long getTotalViews(Long webtoonId)  { return ensure(webtoonId).getTotalViews(); }
//}

package com.webtoon.service;

import com.webtoon.domain.*;
import com.webtoon.repository.StatisticsRepository;
import com.webtoon.repository.WebtoonRepository;

import java.util.List;

/**
 * 통계 서비스
 * - 웹툰별 회차 수 / 조회수 관리
 * - 작가 단위/회차 단위 통계 조회
 */
public class StatisticsService {

    private final StatisticsRepository repo;
    private final WebtoonRepository webtoonRepository; // 작가 단위 집계를 위해 추가

    /**
     * 기존 테스트 코드 호환용 생성자
     * - 웹툰 저장소가 필요 없는 경우 사용
     */
    public StatisticsService(StatisticsRepository repo) {
        this(repo, null);
    }

    /**
     * 작가 단위 통계까지 사용하는 경우
     */
    public StatisticsService(StatisticsRepository repo, WebtoonRepository webtoonRepository) {
        this.repo = repo;
        this.webtoonRepository = webtoonRepository;
    }

    // ================= 기본 웹툰 단위 통계 관리 =================

    private Statistics ensure(Long webtoonId) {
        return repo.findByWebtoonId(webtoonId)
                .orElseGet(() -> repo.save(new Statistics(webtoonId)));
    }

    /** 회차 생성 시 회차 수 +1 */
    public void onEpisodeCreated(Long webtoonId) {
        ensure(webtoonId).incEpisode();
    }

    /** 회차 삭제 시 회차 수 -1 (0 아래로는 내려가지 않음) */
    public void onEpisodeDeleted(Long webtoonId) {
        ensure(webtoonId).decEpisode();
    }

    /** 조회 발생 시 조회수 +1 */
    public void onViewIncreased(Long webtoonId) {
        ensure(webtoonId).incView();
    }

    /** 웹툰별 총 회차 수 */
    public int getEpisodeCount(Long webtoonId) {
        return ensure(webtoonId).getEpisodeCount();
    }

    /** 웹툰별 총 조회수 */
    public long getTotalViews(Long webtoonId) {
        return ensure(webtoonId).getTotalViews();
    }

    // ================= Issue #14: 확장 기능 =================

    /**
     * 작가 단위 통계 조회
     *
     * - 파라미터: Author
     * - 반환값: AuthorStats
     *   - webtoonCount: 이 작가의 작품 수
     *   - totalEpisodeCount: 모든 작품의 회차 수 합
     *   - totalViews: 모든 작품의 조회수 합
     */
    public AuthorStats getAuthorStats(Author author) {
        if (author == null) {
            throw new IllegalArgumentException("작가 정보가 null입니다.");
        }
        if (webtoonRepository == null) {
            throw new IllegalStateException("Author 통계를 사용하려면 WebtoonRepository가 필요합니다.");
        }

        // Author.id → Webtoon.authorId 매핑
        Long authorIdKey = author.getId();

        // 해당 작가의 모든 웹툰
        List<Webtoon> webtoons = webtoonRepository.findByAuthorId(authorIdKey);

        int webtoonCount = webtoons.size();
        int totalEpisodeCount = 0;
        long totalViews = 0L;

        for (Webtoon webtoon : webtoons) {
            if (webtoon == null || webtoon.getId() == null) continue;

            // 웹툰 ID로 통계 조회 (이제 타입 변환 불필요)
            Statistics s = repo.findByWebtoonId(webtoon.getId()).orElse(null);
            if (s != null) {
                totalEpisodeCount += s.getEpisodeCount();
                totalViews += s.getTotalViews();
            }
        }

        return new AuthorStats(
                String.valueOf(author.getId()),
                author.getAuthorName(),
                webtoonCount,
                totalEpisodeCount,
                totalViews
        );
    }

    /**
     * 회차 단위 통계 조회
     *
     * - Episode 도메인 자체에 viewCount가 있으므로
     *   이를 기반으로 EpisodeStats를 만들어 반환
     */
    public EpisodeStats getEpisodeStats(Episode episode) {
        if (episode == null) {
            throw new IllegalArgumentException("회차 정보가 null입니다.");
        }

        return new EpisodeStats(
                episode.getId(),
                episode.getWebtoonId(),
                episode.getNumber(),
                (long) episode.getViewCount()
        );
    }
}
