package com.webtoon.pattern;

import java.util.Set;

public interface Subject {
    String getSubjectId();     // 알림 주체 식별자 (작가 ID or 작품 ID)
    String getSubjectName();   // 알림 주체 표시명 (작가명 or 작품명)

    void attach(Long userId);        // 팔로우
    void detach(Long userId);        // 언팔로우
    Set<Long> getFollowerUserIds();  // 팔로워 조회
}
