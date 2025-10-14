package com.webtoon.pattern;

/**
 * 옵저버 패턴 인터페이스
 * Webtoon → Reader 알림 전달 시 사용
 */
public interface Observer {
    void update(String message);
    Long getUserId();
}
