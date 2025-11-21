package com.webtoon.pattern;

import java.util.Set;

/**
 * Observer 패턴의 Subject 인터페이스
 * 팔로우 가능한 대상 (웹툰, 작가 등)
 */
public interface Subject {
    /**
     * 알림 주체 식별자 반환
     * @return 주체 ID
     */
    String getSubjectId();

    /**
     * 알림 주체 표시명 반환
     * @return 주체명 (작품명, 작가명 등)
     */
    String getSubjectName();

    /**
     * 팔로워 추가
     * @param userId 팔로우할 사용자 ID
     */
    void attach(Long userId);

    /**
     * 팔로워 제거
     * @param userId 언팔로우할 사용자 ID
     */
    void detach(Long userId);

    /**
     * 팔로워 목록 조회
     * @return 팔로워 사용자 ID 집합
     */
    Set<Long> getFollowerUserIds();

    /**
     * Observer 패턴 핵심 메서드: 주체가 팔로워들에게 알림을 보내는 역할
     */
    void notifyObservers();
}
