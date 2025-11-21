package com.webtoon.domain;

import com.webtoon.pattern.Subject;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 웹툰 도메인 (팔로우 대상이므로 Subject 구현)
 */
public class Webtoon implements Subject {

    // 식별/기본 정보
    private Long id;                 // 웹툰 ID
    private String title;              // 작품명
    private String authorId;           // 작가(User/Author)의 id
    private List<String> genres = new ArrayList<>(); // ["판타지","액션"]
    private String status;             // "ONGOING" | "COMPLETED"
    private String summary;            // 한 줄 소개 (상세 화면용)

    // 관계/통계(최소)
    private List<Long> episodeIds = new ArrayList<>(); // 회차 id 목록 (번호순은 Service에서 정렬)
    private final Set<String> followerUserIds = new HashSet<>(); // 팔로워
    private int popularity = 0;        // 정렬용(임시): 조회/대여/구매 합산 등

    // 메타
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ====== 생성자 ======
    public Webtoon() { }

    public Webtoon(Long id, String title, String authorId, List<String> genres,
                   String status, String summary) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        if (genres != null) this.genres = new ArrayList<>(genres);
        this.status = status;
        this.summary = summary;
    }

    // ====== 도메인 메서드 ======
    public void addEpisode(Long episodeId) {
        this.episodeIds.add(episodeId);
        touch();
        // 회차 추가 시 팔로워에게 알림
        notifyObservers();
    }

    public int getEpisodeCount() {
        return episodeIds.size();
    }

    /**
     * 최신 회차 ID 조회
     * @return 최신 회차 ID (없으면 null)
     */
    public Long getLatestEpisode() {
        if (episodeIds.isEmpty()) {
            return null;
        }
        return episodeIds.get(episodeIds.size() - 1);
    }

    public void increasePopularity(int delta) {
        this.popularity += delta;
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // ====== Subject 구현 ======
    @Override
    public String getSubjectId() {
        return String.valueOf(id);
    }

    @Override
    public String getSubjectName() { return title; }

    @Override
    public void attach(String userId)   { followerUserIds.add(userId); }

    @Override
    public void detach(String userId)   { followerUserIds.remove(userId); }

    @Override
    public Set<String> getFollowerUserIds() { return followerUserIds; }

    @Override
    public void notifyObservers() {
        // 과제 요구사항에 맞춰 콘솔 출력 방식으로 Observer 패턴 역할만 수행
        for (String userId : followerUserIds) {
            System.out.printf(
                    "[알림] 사용자 %s → 웹툰 '%s'에 새 회차가 추가되었습니다.%n",
                    userId, title
            );
        }
    }

    // ====== Getter / Setter ======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; touch(); }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; touch(); }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; touch(); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; touch(); }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; touch(); }

    public List<Long> getEpisodeIds() { return episodeIds; }
    public void setEpisodeIds(List<Long> episodeIds) { this.episodeIds = episodeIds; touch(); }

    public int getPopularity() { return popularity; }
    public void setPopularity(int popularity) { this.popularity = popularity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "Webtoon{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authorId='" + authorId + '\'' +
                ", status='" + status + '\'' +
                ", episodes=" + episodeIds.size() +
                ", followers=" + followerUserIds.size() +
                '}';
    }
}
