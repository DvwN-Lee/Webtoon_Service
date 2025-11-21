package com.webtoon.domain;

public class Statistics {
    private final Long webtoonId;
    private int episodeCount;
    private long totalViews;

    public Statistics(Long webtoonId) {
        this.webtoonId = webtoonId;
    }
    public Long getWebtoonId() { return webtoonId; }
    public int getEpisodeCount() { return episodeCount; }
    public long getTotalViews() { return totalViews; }

    public void incEpisode() { episodeCount++; }
    public void decEpisode() { if (episodeCount > 0) episodeCount--; }
    public void incView()    { totalViews++; }
}
