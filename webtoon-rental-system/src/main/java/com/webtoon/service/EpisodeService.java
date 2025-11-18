package com.webtoon.service;
// 회차 수정, 삭제, 상세 조회 같은 독립 기능 추가 시

import com.webtoon.domain.Episode;
import com.webtoon.repository.EpisodeRepository;

import java.util.List;

/**
 * 회차(Episode) 관리 서비스
 * - 조회/수정/삭제 (발행은 WebtoonService가 담당)
 */
public class EpisodeService {

    private final EpisodeRepository episodeRepository;

    public EpisodeService(EpisodeRepository episodeRepository) {
        this.episodeRepository = episodeRepository;
    }

    /** 회차 단건 조회 */
    public Episode findById(Long episodeId) {
        return episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다: " + episodeId));
    }

    /** 특정 작품의 모든 회차(번호 오름차순) */
    public List<Episode> findByWebtoonId(Long webtoonId) {
        return episodeRepository.findByWebtoonId(webtoonId);
    }

    /** 제목 수정 */
    public void updateTitle(Long episodeId, String newTitle) {
        Episode ep = findById(episodeId);
        ep.updateTitle(newTitle);
        episodeRepository.save(ep);
    }

    /** 본문 수정 */
    public void updateContent(Long episodeId, String newContent) {
        Episode ep = findById(episodeId);
        ep.updateContent(newContent);
        episodeRepository.save(ep);
    }

    /** 가격 수정 (Episode 내부에서 유효성 검사 수행) */
    public void updatePrices(Long episodeId, int rentPrice, int buyPrice) {
        Episode ep = findById(episodeId);
        ep.updatePrices(rentPrice, buyPrice); // 내부에서 음수/대여>구매 조건 검증
        episodeRepository.save(ep);
    }

    /** 회차 삭제 */
    public void delete(Long episodeId) {
        episodeRepository.deleteById(episodeId);
    }
}
