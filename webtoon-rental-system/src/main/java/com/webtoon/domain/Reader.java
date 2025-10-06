package com.webtoon.domain;

/**
 * 독자 도메인 모델
 *
 * TODO: 홍승현 - 다음 필드 및 메서드 추가 필요
 * - List<Webtoon> followingWebtoons: 팔로우 중인 작품
 * - List<Notification> notifications: 받은 알림
 * - followWebtoon(Webtoon): 작품 팔로우
 * - unfollowWebtoon(Webtoon): 팔로우 취소
 * - getUnreadNotificationCount(): 미확인 알림 개수
 * - Observer 인터페이스 구현
 */
public class Reader extends User {

    private String nickname;  // 닉네임

    // 기본 생성자 (Gson용)
    public Reader() {
        super();
    }

    /**
     * 독자 생성자
     *
     * @param username 로그인 ID
     * @param password 비밀번호
     * @param nickname 닉네임
     */
    public Reader(String username, String password, String nickname) {
        super(username, password, 1000);  // 초기 포인트 1000P
        this.nickname = nickname;
    }

    @Override
    public String getDisplayName() {
        return nickname;
    }

    @Override
    public String getUserType() {
        return "READER";
    }

    /**
     * 닉네임 수정
     *
     * @param nickname 새 닉네임
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // Getter/Setter

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "Reader{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", nickname='" + nickname + '\'' +
                ", points=" + getPoints() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}