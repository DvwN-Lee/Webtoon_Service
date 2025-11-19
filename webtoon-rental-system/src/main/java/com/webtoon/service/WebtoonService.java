//package com.webtoon.service;
//
//import com.webtoon.domain.Episode;
//import com.webtoon.domain.Webtoon;
//import com.webtoon.repository.EpisodeRepository;
//import com.webtoon.repository.WebtoonRepository;
//
//import java.util.UUID;
//
///**
// * 웹툰 관련 유스케이스:
// * - 웹툰 생성
// * - 작품 팔로우
// * - 회차 발행(번호 자동 증가) + 알림
// */
//public class WebtoonService {
//
//    private final WebtoonRepository webtoonRepository;
//    private final EpisodeRepository episodeRepository;
//    private final NotificationService notificationService;
//
//    public WebtoonService(WebtoonRepository webtoonRepository,
//                          EpisodeRepository episodeRepository,
//                          NotificationService notificationService) {
//        this.webtoonRepository = webtoonRepository;
//        this.episodeRepository = episodeRepository;
//        this.notificationService = notificationService;
//    }
//
//    /**
//     * 새 웹툰 생성 (id는 UUID로 생성)
//     */
//    public Webtoon createWebtoon(String title, String authorId) {
//        Webtoon webtoon = new Webtoon();
//        webtoon.setId(UUID.randomUUID().toString());
//        webtoon.setTitle(title);
//        webtoon.setAuthorId(authorId);
//        // status/genres/summary 등은 필요 시 외부에서 세팅
//        return webtoonRepository.save(webtoon);
//    }
//
//    /**
//     * 작품 팔로우
//     */
//    public void followWebtoon(String webtoonId, String userId) {
//        Webtoon webtoon = webtoonRepository.findById(webtoonId)
//                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));
//        webtoon.attach(userId);
//        webtoonRepository.save(webtoon); // 변경 반영
//    }
//
//    /**
//     * 회차 발행:
//     * - 다음 회차 번호 = 기존 최신 number + 1 (없으면 1)
//     * - Episode id는 UUID 생성
//     * - 저장 후 알림 발송
//     */
//    public Episode publishEpisode(String webtoonId, String title, String content,
//                                  Integer rentPrice, Integer buyPrice) {
//        Webtoon webtoon = webtoonRepository.findById(webtoonId)
//                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));
//
//        int nextNumber = episodeRepository.findLatestByWebtoonId(webtoonId)
//                .map(e -> e.getNumber() + 1)
//                .orElse(1);
//
//        Episode episode = new Episode(
//                UUID.randomUUID().toString(),
//                webtoonId,
//                nextNumber,
//                title,
//                content,
//                rentPrice,
//                buyPrice
//        );
//
//        episodeRepository.save(episode);
//
//        // 웹툰에 회차 id 연결
//        webtoon.addEpisode(episode.getId());
//        webtoonRepository.save(webtoon);
//
//        // 팔로워 알림
//        notificationService.notifyNewEpisode(webtoon, episode);
//        return episode;
//    }
//}

package com.webtoon.service;

import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.EpisodeRepository;
import com.webtoon.repository.WebtoonRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 웹툰 관련 유스케이스:
 * - 웹툰 생성
 * - 작품 팔로우
 * - 회차 발행(번호 자동 증가)
 * - 조회/검색/정렬
 */
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;
    private final EpisodeRepository episodeRepository;
    private final NotificationService notificationService; // 현재는 사용 X(과제 스펙에 따라 유지)

    public WebtoonService(WebtoonRepository webtoonRepository,
                          EpisodeRepository episodeRepository,
                          NotificationService notificationService) {
        this.webtoonRepository = webtoonRepository;
        this.episodeRepository = episodeRepository;
        this.notificationService = notificationService;
    }

    // ====== 조회/검색/정렬 기능 ======

    /** 모든 웹툰 목록 조회 */
    public List<Webtoon> listAllWebtoons() {
        return webtoonRepository.findAll();
    }

    /** ID로 단건 조회 (스펙상 Long 사용, 내부에서는 String ID와 매핑) */
    public Webtoon getWebtoon(Long id) {
        String webtoonId = String.valueOf(id);
        return webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));
    }

    /** 제목 키워드 검색 */
    public List<Webtoon> searchByTitle(String keyword) {
        return webtoonRepository.searchByTitle(keyword);
    }

    /** 작가 ID로 검색 */
    public List<Webtoon> searchByAuthor(String authorId) {
        return webtoonRepository.findByAuthorId(authorId);
    }

    /** 인기순 정렬 (popularity 내림차순) */
    public List<Webtoon> sortByPopularity() {
        return webtoonRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Webtoon::getPopularity).reversed())
                .collect(Collectors.toList());
    }

    /** 제목 오름차순 정렬 */
    public List<Webtoon> sortByTitle() {
        return webtoonRepository.findAll().stream()
                .sorted(Comparator.comparing(Webtoon::getTitle,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    /** 최신 업데이트순 정렬 (updatedAt 기준 내림차순) */
    public List<Webtoon> sortByLatest() {
        return webtoonRepository.findAll().stream()
                .sorted(Comparator.comparing(Webtoon::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    // ====== 생성/팔로우/회차 발행 ======

    /**
     * 새 웹툰 생성 (id는 UUID로 생성)
     */
    public Webtoon createWebtoon(String title, String authorId) {
        Webtoon webtoon = new Webtoon();
        webtoon.setId(UUID.randomUUID().toString());
        webtoon.setTitle(title);
        webtoon.setAuthorId(authorId);
        // status/genres/summary 등은 필요 시 외부에서 세팅
        return webtoonRepository.save(webtoon);
    }

    /**
     * 작품 팔로우
     */
    public void followWebtoon(String webtoonId, String userId) {
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));
        webtoon.attach(userId);
        webtoonRepository.save(webtoon); // 변경 반영
    }

    /**
     * 회차 발행:
     * - 다음 회차 번호 = 기존 최신 number + 1 (없으면 1)
     * - Episode id는 UUID 생성
     * - 저장 후 Webtoon.addEpisode() 내부에서 notifyObservers() 호출
     */
    public Episode publishEpisode(String webtoonId, String title, String content,
                                  Integer rentPrice, Integer buyPrice) {
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));

        int nextNumber = episodeRepository.findLatestByWebtoonId(webtoonId)
                .map(e -> e.getNumber() + 1)
                .orElse(1);

        Episode episode = new Episode(
                UUID.randomUUID().toString(),
                webtoonId,
                nextNumber,
                title,
                content,
                rentPrice,
                buyPrice
        );

        episodeRepository.save(episode);

        // 웹툰에 회차 id 연결 (+ 내부에서 notifyObservers 호출)
        webtoon.addEpisode(episode.getId());
        webtoonRepository.save(webtoon);

        // ✅ NotificationService를 직접 호출하지 않음 (Observer 패턴이 도메인 쪽에서 처리)
        return episode;
    }
}
