package com.webtoon.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 사용자 추상 클래스
 * Reader와 Author의 공통 속성 및 메서드 정의
 */
public abstract class User {

    private Long id;
    private String username;      // 로그인 ID
    private String password;      // 비밀번호 (데모에서는 평문)
    private int points;           // 보유 포인트
    private LocalDateTime createdAt;

    /**
     * 기본 생성자 (Gson 역직렬화용)
     */
    protected User() {
    }

    /**
     * 사용자 생성자
     *
     * @param username 로그인 ID
     * @param password 비밀번호
     * @param initialPoints 초기 포인트 (독자: 1000, 작가: 0)
     */
    protected User(String username, String password, int initialPoints) {
        this.username = username;
        this.password = password;
        this.points = initialPoints;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 포인트 사용
     *
     * @param amount 사용할 포인트
     * @return 성공 여부 (잔액 부족 시 false)
     */
    public boolean usePoints(int amount) {
        if (points < amount) {
            return false;  // 잔액 부족
        }
        points -= amount;
        return true;
    }

    /**
     * 포인트 충전
     *
     * @param amount 충전할 포인트
     */
    public void addPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("충전 포인트는 0 이상이어야 합니다.");
        }
        points += amount;
    }

    /**
     * 비밀번호 인증
     *
     * @param password 입력된 비밀번호
     * @return 일치 여부
     */
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    // 추상 메서드 - 하위 클래스가 구현

    /**
     * 화면에 표시할 이름 반환
     * Reader: 닉네임, Author: 작가명
     */
    public abstract String getDisplayName();

    /**
     * 사용자 타입 반환
     *
     * @return "READER" 또는 "AUTHOR"
     */
    public abstract String getUserType();

    // Getter/Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", points=" + points +
                ", createdAt=" + createdAt +
                ", type=" + getUserType() +
                '}';
    }
}