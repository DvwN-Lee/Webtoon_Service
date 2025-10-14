package com.webtoon.domain;

/**
 * 작가 도메인 모델
 *
 * TODO: 유연주 - 다음 필드 및 메서드 추가 필요
 * - List<Webtoon> webtoons: 연재 중인 작품 목록
 * - createWebtoon(Webtoon): 새 작품 등록
 * - getTotalFollowers(): 총 팔로워 수 (모든 작품 합계)
 * - getWebtoonCount(): 작품 수
 */
public class Author extends User {

    private String authorName;  // 작가명
    private String bio;         // 자기소개 (선택)

    // 기본 생성자 (Gson용)
    public Author() {
        super();
    }

    /**
     * 작가 생성자
     *
     * @param username 로그인 ID
     * @param password 비밀번호
     * @param authorName 작가명
     * @param bio 자기소개 (null 가능)
     */
    public Author(String username, String password, String authorName, String bio) {
        super(username, password, 0);  // 작가는 초기 포인트 0
        this.authorName = authorName;
        this.bio = bio;
    }

    @Override
    public String getDisplayName() {
        return authorName;
    }

    @Override
    public String getUserType() {
        return "AUTHOR";
    }

    /**
     * 작가명 수정
     *
     * @param authorName 새 작가명
     */
    public void updateAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * 자기소개 수정
     *
     * @param bio 새 자기소개
     */
    public void updateBio(String bio) {
        this.bio = bio;
    }

    // Getter/Setter

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", authorName='" + authorName + '\'' +
                ", bio='" + bio + '\'' +
                ", points=" + getPoints() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}