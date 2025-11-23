package com.webtoon.domain;

import com.webtoon.pattern.Observer;
import com.webtoon.pattern.Subject;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 웹툰 도메인 (팔로우 대상이므로 Subject 구현)
 */
public class Webtoon implements Subject {

    // 식별/기본 정보
    private Long id;                   // Repository에서 자동 생성
    private String title;              // 작품명
    private Long authorId;             // 작가(User/Author)의 id
    private List<String> genres = new ArrayList<>(); // ["판타지","액션"]
    private String status;             // "ONGOING" | "COMPLETED"
    private String summary;            // 한 줄 소개 (상세 화면용)

    // 관계/통계
    private List<Long> episodeIds = new ArrayList<>(); // 회차 id 목록 (번호순은 Service에서 정렬)
    private final Set<Long> followerUserIds = new HashSet<>(); // 팔로워
    private int popularity = 0;        // 정렬용(임시): 조회/대여/구매 합산 등

    // 메타
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Observer 패턴을 위한 Observer 리스트 (런타임에 주입)
    private transient List<Observer> observers = new ArrayList<>();

    // 생성자
    public Webtoon() { }

    public Webtoon(Long id, String title, Long authorId, List<String> genres,
                   String status, String summary) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        if (genres != null) this.genres = new ArrayList<>(genres);
        this.status = status;
        this.summary = summary;
    }

    // 도메인 메서드
    public void addEpisode(Long episodeId) {
        this.episodeIds.add(episodeId);
        touch();
        // notifyObservers()는 Service 레이어에서 호출 (UserRepository 필요)
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

    // Subject 구현
    @Override
    public String getSubjectId() {
        return id != null ? id.toString() : null;
    }

    @Override
    public String getSubjectName() {
        return title;
    }

    @Override
    public void attach(Long userId) {
        followerUserIds.add(userId);
    }

    @Override
    public void detach(Long userId) {
        followerUserIds.remove(userId);
    }

    @Override
    public Set<Long> getFollowerUserIds() {
        return followerUserIds;
    }



    @Override
    public void notifyObservers() {
        if (id == null) {
            return; // ID가 없으면 알림을 보낼 수 없음
        }

        String message = String.format("'%s'에 새 회차가 추가되었습니다.", title);

        // Observer 리스트를 순회하며 update() 호출
        for (Observer observer : observers) {
            if (followerUserIds.contains(observer.getUserId())) {
                observer.update(id, title, message);
            }
        }
    }

    /**
     * Observer 등록 (런타임에 Service 레이어에서 호출)
     * @param observer 등록할 Observer
     */
    public void registerObserver(Observer observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Observer 제거
     * @param observer 제거할 Observer
     */
    public void removeObserver(Observer observer) {
        if (this.observers != null) {
            this.observers.remove(observer);
        }
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; touch(); }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; touch(); }

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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "Webtoon{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authorId=" + authorId +
                ", status='" + status + '\'' +
                ", episodes=" + episodeIds.size() +
                ", followers=" + followerUserIds.size() +
                '}';
    }
}
