package com.webtoon.service;

import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.EpisodeRepository;
import com.webtoon.repository.WebtoonRepository;

import java.util.UUID;

/**
 * 웹툰 관련 유스케이스:
 * - 웹툰 생성
 * - 작품 팔로우
 * - 회차 발행(번호 자동 증가) + 알림
 */
public class WebtoonService {

    private final WebtoonRepository webtoonRepository;
    private final EpisodeRepository episodeRepository;
    private final NotificationService notificationService;

    public WebtoonService(WebtoonRepository webtoonRepository,
                          EpisodeRepository episodeRepository,
                          NotificationService notificationService) {
        this.webtoonRepository = webtoonRepository;
        this.episodeRepository = episodeRepository;
        this.notificationService = notificationService;
    }

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
    public void followWebtoon(Long webtoonId, String userId) {
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));
        webtoon.attach(userId);
        webtoonRepository.save(webtoon); // 변경 반영
    }

    /**
     * 회차 발행:
     * - 다음 회차 번호 = 기존 최신 number + 1 (없으면 1)
     * - Episode id는 UUID 생성
     * - 저장 후 알림 발송
     */
    public Episode publishEpisode(Long webtoonId, String title, String content,
                                  Integer rentPrice, Integer buyPrice) {
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));

        int nextNumber = episodeRepository.findLatestByWebtoonId(webtoonId)
                .map(e -> e.getNumber() + 1)
                .orElse(1);

        Episode episode = new Episode(
                UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE,
                webtoonId,
                nextNumber,
                title,
                content,
                rentPrice,
                buyPrice
        );

        episodeRepository.save(episode);

        // 웹툰에 회차 id 연결
        webtoon.addEpisode(episode.getId());
        webtoonRepository.save(webtoon);

        // 팔로워 알림
        notificationService.notifyNewEpisode(webtoon, episode);
        return episode;
    }
}
