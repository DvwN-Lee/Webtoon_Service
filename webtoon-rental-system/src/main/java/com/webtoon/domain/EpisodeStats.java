package com.webtoon.domain;
/**
 * 회차 단위 통계 DTO
 *
 * - 단일 Episode에 대한 통계 정보를 담기 위한 값 객체
 */
public class EpisodeStats {

    private final String episodeId;
    private final String webtoonId;
    private final int episodeNumber;
    private final int viewCount;

    public EpisodeStats(String episodeId,
                        String webtoonId,
                        int episodeNumber,
                        int viewCount) {
        this.episodeId = episodeId;
        this.webtoonId = webtoonId;
        this.episodeNumber = episodeNumber;
        this.viewCount = viewCount;
    }

    public String getEpisodeId() {
        return episodeId;
    }

    public String getWebtoonId() {
        return webtoonId;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public int getViewCount() {
        return viewCount;
    }

    @Override
    public String toString() {
        return "EpisodeStats{" +
                "episodeId='" + episodeId + '\'' +
                ", webtoonId='" + webtoonId + '\'' +
                ", episodeNumber=" + episodeNumber +
                ", viewCount=" + viewCount +
                '}';
    }
}
