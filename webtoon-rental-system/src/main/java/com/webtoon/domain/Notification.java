package com.webtoon.domain;

import java.time.LocalDateTime;

/**
 * 알림 도메인 모델
 * [수정 사항 - Issue #4 피드백 반영]
 * 1. webtoonId 필드 추가 (어떤 웹툰 알림인지 식별)
 */
public class Notification {
    private Long id;
    private Long readerId;       // 수신자 ID
    private Long webtoonId;      // [추가] 관련 웹툰 ID
    private String message;      // 알림 내용
    private LocalDateTime createdAt;
    private boolean isRead;

    // Gson 역직렬화용 기본 생성자
    public Notification() {}

    // 생성자 업데이트 (webtoonId 추가)
    public Notification(Long id, Long readerId, Long webtoonId, String message) {
        this.id = id;
        this.readerId = readerId;
        this.webtoonId = webtoonId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // 읽음 처리 메서드
    public void markAsRead() {
        this.isRead = true;
    }

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }

    public Long getWebtoonId() { return webtoonId; }
    public void setWebtoonId(Long webtoonId) { this.webtoonId = webtoonId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}