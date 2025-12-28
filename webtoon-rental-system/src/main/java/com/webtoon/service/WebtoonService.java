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
import com.webtoon.domain.Reader;
import com.webtoon.domain.User;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.EpisodeRepository;
import com.webtoon.repository.UserRepository;
import com.webtoon.repository.WebtoonRepository;

import java.util.Comparator;
import java.util.List;
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
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final StatisticsService statisticsService;

    public WebtoonService(WebtoonRepository webtoonRepository,
                          EpisodeRepository episodeRepository,
                          NotificationService notificationService,
                          StatisticsService statisticsService) {
        this.webtoonRepository = webtoonRepository;
        this.episodeRepository = episodeRepository;
        this.notificationService = notificationService;
        this.userRepository = new UserRepository();
        this.statisticsService = statisticsService;
    }

    // 테스트용 DI 생성자 (모든 의존성 주입)
    public WebtoonService(WebtoonRepository webtoonRepository,
                          EpisodeRepository episodeRepository,
                          NotificationService notificationService,
                          UserRepository userRepository,
                          StatisticsService statisticsService) {
        this.webtoonRepository = webtoonRepository;
        this.episodeRepository = episodeRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.statisticsService = statisticsService;
    }

    // 기존 테스트 호환용 생성자 (StatisticsService 없이)
    public WebtoonService(WebtoonRepository webtoonRepository,
                          EpisodeRepository episodeRepository,
                          NotificationService notificationService,
                          UserRepository userRepository) {
        this(webtoonRepository, episodeRepository, notificationService, userRepository, null);
    }

    // 기존 호환용 생성자 (StatisticsService 없이)
    public WebtoonService(WebtoonRepository webtoonRepository,
                          EpisodeRepository episodeRepository,
                          NotificationService notificationService) {
        this(webtoonRepository, episodeRepository, notificationService, new UserRepository(), null);
    }

    // ====== 조회/검색/정렬 기능 ======

    /** 모든 웹툰 목록 조회 */
    public List<Webtoon> listAllWebtoons() {
        return webtoonRepository.findAll();
    }

    /** ID로 단건 조회 */
    public Webtoon getWebtoon(Long id) {
        return webtoonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + id));
    }

    /** 제목 키워드 검색 */
    public List<Webtoon> searchByTitle(String keyword) {
        return webtoonRepository.searchByTitle(keyword);
    }

    /** 작가 ID로 검색 */
    public List<Webtoon> searchByAuthor(Long authorId) {
        return webtoonRepository.findByAuthorId(authorId);
    }

//    /** 인기순 정렬 (popularity 내림차순) */
//    public List<Webtoon> sortByPopularity() {
//        return webtoonRepository.findAll().stream()
//                .sorted(Comparator.comparingInt(Webtoon::getPopularity).reversed())
//                .collect(Collectors.toList());
//    }

    /** 조회순 정렬 (총 조회수 내림차순) */
    public List<Webtoon> sortByViews() {
        List<Webtoon> all = webtoonRepository.findAll();

        // statisticsService가 주입되지 않은 테스트 환경 대비
        if (statisticsService == null) {
            return all;
        }

        return all.stream()
                .sorted(Comparator.comparingLong(
                        (Webtoon w) -> statisticsService.getTotalViews(w.getId())
                ).reversed())
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
     * 새 웹툰 생성 (id는 repository에서 자동 생성)
     */
    public Webtoon createWebtoon(String title, Long authorId) {
        Webtoon webtoon = new Webtoon();
        webtoon.setTitle(title);
        webtoon.setAuthorId(authorId);

        // 기본값 설정
        webtoon.setStatus("ONGOING");  // 기본: 연재 중
        webtoon.setGenres(java.util.Arrays.asList("기타"));  // 기본 장르
        webtoon.setSummary("준비 중입니다.");  // 기본 줄거리
        webtoon.setPopularity(0);  // 기본 인기도

        return webtoonRepository.save(webtoon);
    }

    /**
     * 작품 팔로우
     * - 팔로워 목록에 userId 추가
     * - Reader 객체를 Webtoon의 Observer로 등록 (알림 받기 위해)
     */
    public void followWebtoon(Long webtoonId, Long userId) {
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));

        // 팔로워 목록에 추가
        webtoon.attach(userId);

        // Reader 객체를 Observer로 등록
        userRepository.findById(userId).ifPresent(user -> {
            if (user instanceof Reader) {
                webtoon.registerObserver((Reader) user);
            }
        });

        webtoonRepository.save(webtoon); // 변경 반영
    }

    /**
     * 회차 발행:
     * - 다음 회차 번호 = 기존 최신 number + 1 (없으면 1)
     * - Episode id는 repository에서 자동 생성
     * - 저장 후 팔로워들에게 알림 전송
     */
    public Episode publishEpisode(Long webtoonId, String title, String content,
                                  Integer rentPrice, Integer buyPrice) {
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));

        int nextNumber = episodeRepository.findLatestByWebtoonId(webtoonId)
                .map(e -> e.getNumber() + 1)
                .orElse(1);

        Episode episode = new Episode(
                null,
                webtoonId,
                nextNumber,
                title,
                content,
                rentPrice,
                buyPrice
        );

        episodeRepository.save(episode);

        // 통계 서비스에 회차 생성 반영
        if (statisticsService != null) {
            statisticsService.onEpisodeCreated(webtoonId);
        }

        // 웹툰에 회차 id 연결
        webtoon.addEpisode(episode.getId());
        webtoonRepository.save(webtoon);

        // 팔로워들에게 알림 전송 (Observer 패턴 구현)
        notifyFollowers(webtoon, episode);

        return episode;
    }

    public void unfollowWebtoon(Long webtoonId, Long userId) {
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));

        // Webtoon의 follower 목록에서 제거
        webtoon.detach(userId);

        // 변경사항 저장 (JSON 반영)
        webtoonRepository.save(webtoon);
    }


    /**
     * 팔로워들에게 알림 전송
     * - 팔로워 ID 목록에서 Reader 객체를 조회
     * - 각 Reader의 update() 메서드 호출 (Observer 패턴)
     * - NotificationService를 통해 알림 생성 및 저장
     */
    private void notifyFollowers(Webtoon webtoon, Episode episode) {
        String message = String.format("'%s'에 새 회차가 추가되었습니다.", webtoon.getTitle());

        for (Long followerId : webtoon.getFollowerUserIds()) {
            userRepository.findById(followerId).ifPresent(user -> {
                if (user instanceof Reader) {
                    Reader reader = (Reader) user;
                    // Observer 패턴: Reader.update() 호출 (콘솔 출력)
                    reader.update(webtoon.getId(), webtoon.getTitle(), message);
                    // NotificationRepository에도 저장
                    notificationService.createNotification(followerId, webtoon.getId(), message);
                }
            });
        }
    }
}
