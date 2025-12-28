package com.webtoon.pattern;

/**
 * 옵저버 패턴 인터페이스
 * Webtoon → Reader 알림 전달 시 사용
 */
public interface Observer {
    /**
     * 웹툰 업데이트 알림 수신
     * @param webtoonId 웹툰 ID
     * @param webtoonTitle 웹툰 제목
     * @param message 알림 메시지
     */
    void update(Long webtoonId, String webtoonTitle, String message);

    /**
     * Observer의 사용자 ID 반환
     * @return 사용자 ID
     */
    Long getUserId();
}
