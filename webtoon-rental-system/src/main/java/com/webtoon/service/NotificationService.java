package com.webtoon.service;

import com.webtoon.domain.Notification;
import com.webtoon.domain.Reader;
import com.webtoon.repository.NotificationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 알림 서비스 (홍승현)
 * 이슈 #6: 알림 목록 조회, 읽음 처리
 * [수정 사항 - Issue #6 피드백 반영]
 * 1. getUnreadNotifications 메서드 추가 (누락 기능 구현)
 * 2. 객체 기반 편의 메서드(Overloading) 추가 (타입 불일치 해결)
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService() {
        this.notificationRepository = new NotificationRepository();
    }
    
    // DI용 생성자 (테스트 시 사용)
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * FR-INTERACT-03: 신규 알림 생성 및 저장
     */
    public void createNotification(Long readerId, Long webtoonId, String message) {
        List<Notification> all = notificationRepository.findAll();
        // Auto Increment ID 생성
        Long nextId = all.stream()
                .map(Notification::getId)
                .filter(id -> id != null)
                .max(Long::compareTo)
                .orElse(0L) + 1;

        Notification notification = new Notification(nextId, readerId, webtoonId, message);
        notificationRepository.save(notification);
        System.out.println("💾 [System] 알림 데이터 저장 완료 (Reader ID: " + readerId + ")");
    }

    // --- ID 기반 핵심 로직 (기존 유지) ---

    /**
     * FR-INTERACT-04: 독자별 전체 알림 목록 조회 (최신순)
     */
    public List<Notification> getNotifications(Long readerId) {
        return notificationRepository.findByReaderId(readerId).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * [추가] FR-INTERACT-04: 독자별 미확인 알림 목록 조회 (최신순)
     */
    public List<Notification> getUnreadNotifications(Long readerId) {
        return notificationRepository.findUnreadByReaderId(readerId).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * FR-INTERACT-05: 개별 알림 읽음 처리
     */
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId);
        if (n != null && !n.isRead()) {
            n.markAsRead();
            notificationRepository.update(n);
        }
    }

    /**
     * FR-INTERACT-05: 전체 알림 읽음 처리
     */
    public void markAllAsRead(Long readerId) {
        List<Notification> unread = notificationRepository.findUnreadByReaderId(readerId);
        for (Notification n : unread) {
            n.markAsRead();
            notificationRepository.update(n);
        }
    }

    // --- [추가] 객체 기반 편의 메서드 (팀장님 요구사항 반영) ---

    public List<Notification> getNotifications(Reader reader) {
        if (reader == null || reader.getId() == null) return List.of();
        return getNotifications(reader.getId());
    }

    public List<Notification> getUnreadNotifications(Reader reader) {
        if (reader == null || reader.getId() == null) return List.of();
        return getUnreadNotifications(reader.getId());
    }

    public void markAsRead(Notification notification) {
        if (notification != null && notification.getId() != null) {
            markAsRead(notification.getId());
        }
    }

    public void markAllAsRead(Reader reader) {
        if (reader != null && reader.getId() != null) {
            markAllAsRead(reader.getId());
        }
    }
}