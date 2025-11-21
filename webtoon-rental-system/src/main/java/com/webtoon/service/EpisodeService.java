//package com.webtoon.service;
//
//import com.webtoon.domain.Episode;
//import com.webtoon.domain.Reader;
//import com.webtoon.repository.EpisodeRepository;
//
//import java.util.List;
//
///**
// * 회차(Episode) 관리 서비스
// * - 조회/수정/삭제 (발행은 WebtoonService가 담당)
// */
//public class EpisodeService {
//
//    private final EpisodeRepository episodeRepository;
//
//    public EpisodeService(EpisodeRepository episodeRepository) {
//        this.episodeRepository = episodeRepository;
//    }
//
//    /** 회차 단건 조회 */
//    public Episode findById(String episodeId) {
//        return episodeRepository.findById(episodeId)
//                .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다: " + episodeId));
//    }
//
//    /** 특정 작품의 모든 회차(번호 오름차순) */
//    public List<Episode> findByWebtoonId(String webtoonId) {
//        return episodeRepository.findByWebtoonId(webtoonId);
//    }
//
//    /** 제목 수정 */
//    public void updateTitle(String episodeId, String newTitle) {
//        Episode ep = findById(episodeId);
//        ep.updateTitle(newTitle);
//        episodeRepository.save(ep);
//    }
//
//    /** 본문 수정 */
//    public void updateContent(String episodeId, String newContent) {
//        Episode ep = findById(episodeId);
//        ep.updateContent(newContent);
//        episodeRepository.save(ep);
//    }
//
//    /** 가격 수정 (Episode 내부에서 유효성 검사 수행) */
//    public void updatePrices(String episodeId, int rentPrice, int buyPrice) {
//        Episode ep = findById(episodeId);
//        ep.updatePrices(rentPrice, buyPrice); // 내부에서 음수/대여>구매 조건 검증
//        episodeRepository.save(ep);
//    }
//
//    /**
//     * 사용자별 회차 상세 조회
//     * - 현재는 단순히 조회수 증가 + Episode 반환
//     * - 추후 Rental/Purchase 도메인과 연동해 "대여/구매 여부" 등을 포함한 DTO로 확장 가능
//     */
//    public Episode getEpisodeDetailForUser(Episode episode, Reader reader) {
//        if (episode == null) {
//            throw new IllegalArgumentException("회차 정보가 null입니다.");
//        }
//        // 예시: 상세 조회 시 조회수 1 증가
//        episode.incrementViewCount();
//        episodeRepository.save(episode);
//        return episode;
//    }
//
//    /**
//     * 회차 엔티티 기반 삭제
//     * - Rental/Purchase 등 연관 데이터 정리는 TODO
//     */
//    public void deleteEpisode(Episode episode) {
//        if (episode == null) return;
//        delete(episode.getId());
//        // TODO: Rental/Purchase 등 연관 데이터 처리 로직 추가 예정
//    }
//
//    /** 회차 ID 기반 삭제 (기존 메서드 유지) */
//    public void delete(String episodeId) {
//        episodeRepository.deleteById(episodeId);
//    }
//}

package com.webtoon.service;

import com.webtoon.domain.Episode;
import com.webtoon.domain.Reader;
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

    /**
     * 사용자별 회차 상세 조회
     * - 현재는 단순히 조회수 증가 + Episode 반환
     * - 추후 Rental/Purchase 도메인과 연동해 "대여/구매 여부" 등을 포함한 DTO로 확장 가능
     */
    public Episode getEpisodeDetailForUser(Episode episode, Reader reader) {
        if (episode == null) {
            throw new IllegalArgumentException("회차 정보가 null입니다.");
        }
        // 예시: 상세 조회 시 조회수 1 증가
        episode.incrementViewCount();
        episodeRepository.save(episode);
        return episode;
    }

    /**
     * 회차 엔티티 기반 삭제
     * - Rental/Purchase 등 연관 데이터 정리는 TODO
     */
    public void deleteEpisode(Episode episode) {
        if (episode == null) return;
        delete(episode.getId());   // getId()는 Long
        // TODO: Rental/Purchase 등 연관 데이터 처리 로직 추가 예정
    }

    /** 회차 ID 기반 삭제 (기존 메서드 유지) */
    public void delete(Long episodeId) {
        episodeRepository.deleteById(episodeId);
    }
}
