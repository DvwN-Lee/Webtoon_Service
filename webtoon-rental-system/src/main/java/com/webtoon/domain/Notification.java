package com.webtoon.domain;

import java.time.LocalDateTime;

/**
 * 알림 도메인 모델
 * (Reader에게 전송되는 알림)
 */
public class Notification {
    private Long id;
    private Long readerId;       // 수신자 ID
    private String message;      // 알림 내용
    private LocalDateTime createdAt;
    private boolean isRead;

    // Gson 역직렬화용 기본 생성자
    public Notification() {}

    public Notification(Long id, Long readerId, String message) {
        this.id = id;
        this.readerId = readerId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // 읽음 처리
    public void markAsRead() {
        this.isRead = true;
    }

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
