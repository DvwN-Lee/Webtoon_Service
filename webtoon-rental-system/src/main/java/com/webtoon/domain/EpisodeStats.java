package com.webtoon.domain;

/**
 * 회차 단위 통계 DTO
 *
 * 단일 Episode에 대한 통계 정보를 담기 위한 값 객체
 */
public class EpisodeStats {

    private final Long episodeId;
    private final Long webtoonId;
    private final int episodeNumber;
    private final long viewCount;

    public EpisodeStats(Long episodeId,
                        Long webtoonId,
                        int episodeNumber,
                        long viewCount) {
        this.episodeId = episodeId;
        this.webtoonId = webtoonId;
        this.episodeNumber = episodeNumber;
        this.viewCount = viewCount;
    }

    public Long getEpisodeId() {
        return episodeId;
    }

    public Long getWebtoonId() {
        return webtoonId;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public long getViewCount() {
        return viewCount;
    }

    @Override
    public String toString() {
        return "EpisodeStats{" +
                "episodeId=" + episodeId +
                ", webtoonId=" + webtoonId +
                ", episodeNumber=" + episodeNumber +
                ", viewCount=" + viewCount +
                '}';
    }
}
