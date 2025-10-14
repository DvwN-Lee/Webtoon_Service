package com.webtoon.domain;

import com.webtoon.pattern.Observer;
import java.util.ArrayList;
import java.util.List;

/**
 * 독자 도메인 모델
 * 팔로우 / 알림 기능 포함
 */
public class Reader extends User implements Observer {

    private String nickname;
    private List<Long> followingWebtoonIds;  // 팔로우 중인 웹툰 ID 목록
    private List<Notification> notifications; // 받은 알림 목록

    public Reader() {
        super();
        this.followingWebtoonIds = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    public Reader(String username, String password, String nickname) {
        super(username, password, 1000);
        this.nickname = nickname;
        this.followingWebtoonIds = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    @Override
    public String getDisplayName() {
        return nickname;
    }

    @Override
    public String getUserType() {
        return "READER";
    }

    /** 닉네임 수정 */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /** 팔로우 추가 */
    public void followWebtoon(Long webtoonId) {
        if (!followingWebtoonIds.contains(webtoonId)) {
            followingWebtoonIds.add(webtoonId);
        }
    }

    /** 팔로우 취소 */
    public void unfollowWebtoon(Long webtoonId) {
        followingWebtoonIds.remove(webtoonId);
    }

    /** 알림 추가 */
    public void receiveNotification(String message) {
        this.notifications.add(new Notification(null, this.getId(), message));
    }

    /** 안 읽은 알림 개수 반환 */
    public int getUnreadNotificationCount() {
        return (int) notifications.stream().filter(n -> !n.isRead()).count();
    }

    // Observer 패턴 구현
    @Override
    public void update(String message) {
        receiveNotification(message);
        System.out.println("📢 [" + nickname + "] 새 알림: " + message);
    }

    @Override
    public Long getUserId() {
        return this.getId();
    }

    // Getter / Setter
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public List<Long> getFollowingWebtoonIds() { return followingWebtoonIds; }
    public List<Notification> getNotifications() { return notifications; }

    @Override
    public String toString() {
        return "Reader{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", nickname='" + nickname + '\'' +
                ", points=" + getPoints() +
                ", following=" + followingWebtoonIds.size() +
                ", notifications=" + notifications.size() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
