package com.webtoon.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

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

    // 팔로워(독자) ID 집합
//    private final Set<String> followerUserIds = new HashSet<>();

    // 연재 중인 웹툰 목록^^
    private final List<Webtoon> webtoons = new ArrayList<>();

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


    // 작가는 팔로우 대상 X
//    @Override
//    public String getSubjectId() {
//        return String.valueOf(getId());               // User의 고유 ID 사용
//    }
//
//    @Override
//    public String getSubjectName() {
//        return authorName;            // 알림 표시용 이름
//    }
//
//    @Override
//    public void attach(String userId) {
//        followerUserIds.add(userId);
//    }
//
//    @Override
//    public void detach(String userId) {
//        followerUserIds.remove(userId);
//    }
//
//    @Override
//    public Set<String> getFollowerUserIds() {
//        return followerUserIds;
//    }

    // 작가의 연재 목록에 새 웹툰 등록^^
    public void createWebtoon(Webtoon webtoon) {
        if (webtoon == null) return;
        webtoons.add(webtoon);
    }

    // 작가가 가진 전체 작품 수^^
    public int getWebtoonCount() {
        return webtoons.size();
    }

    // 작가의 모든 작품을 대상으로 총 팔로워 수를 계산^^
    public int getTotalFollowers() {
        Set<String> allFollowerIds = new HashSet<>();
        for (Webtoon webtoon : webtoons) {
            if (webtoon != null) {
                allFollowerIds.addAll(webtoon.getFollowerUserIds());
            }
        }
        return allFollowerIds.size();
    }

    // 작가 연재 목록에서 특정 웹툰 제거 (웹툰 삭제 시 사용)
    public void removeWebtoon(String webtoonId) {
        webtoons.removeIf(w -> w != null && webtoonId.equals(w.getId()));
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

    public List<Webtoon> getWebtoons() {
        return webtoons;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", authorName='" + authorName + '\'' +
                ", bio='" + bio + '\'' +
                ", webtoonCount=" + webtoons.size() +
                ", totalFollowers=" + getTotalFollowers() +
                ", points=" + getPoints() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}