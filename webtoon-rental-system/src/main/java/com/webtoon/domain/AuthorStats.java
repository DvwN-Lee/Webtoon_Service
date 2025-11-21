package com.webtoon.domain;

/**
 * 작가 단위 통계 DTO
 *
 * 특정 Author에 대한 통계 정보를 담기 위한 값 객체
 */
public class AuthorStats {

    private final Long authorId;
    private final String authorName;
    private final int webtoonCount;
    private final int totalEpisodeCount;
    private final long totalViews;

    public AuthorStats(Long authorId,
                       String authorName,
                       int webtoonCount,
                       int totalEpisodeCount,
                       long totalViews) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.webtoonCount = webtoonCount;
        this.totalEpisodeCount = totalEpisodeCount;
        this.totalViews = totalViews;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public int getWebtoonCount() {
        return webtoonCount;
    }

    public int getTotalEpisodeCount() {
        return totalEpisodeCount;
    }

    public long getTotalViews() {
        return totalViews;
    }

    @Override
    public String toString() {
        return "AuthorStats{" +
                "authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                ", webtoonCount=" + webtoonCount +
                ", totalEpisodeCount=" + totalEpisodeCount +
                ", totalViews=" + totalViews +
                '}';
    }
}
