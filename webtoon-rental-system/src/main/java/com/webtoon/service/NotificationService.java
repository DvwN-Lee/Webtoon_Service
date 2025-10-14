package com.webtoon.service;

import com.webtoon.domain.Notification;
import com.webtoon.repository.NotificationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Notification 서비스 로직
 * 알림 생성 / 읽음 처리 / 조회 담당
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService() {
        this.notificationRepository = new NotificationRepository();
    }

    // 테스트용 DI 생성자
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** 신규 알림 생성 */
    public void createNotification(Long readerId, String message) {
        List<Notification> all = notificationRepository.findAll();
        Long nextId = all.stream()
                .map(Notification::getId)
                .filter(id -> id != null)
                .max(Long::compareTo)
                .orElse(0L) + 1;

        Notification newNotification = new Notification(nextId, readerId, message);
        notificationRepository.save(newNotification);
    }

    /** 독자별 전체 알림 조회 (최신순 정렬) */
    public List<Notification> getNotifications(Long readerId) {
        return notificationRepository.findByReaderId(readerId).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** 알림 단건 읽음 처리 */
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId);
        if (n != null && !n.isRead()) {
            n.markAsRead();
            notificationRepository.update(n);
        }
    }

    /** 독자별 모든 알림 일괄 읽음 처리 */
    public void markAllAsRead(Long readerId) {
        List<Notification> unread = notificationRepository.findUnreadByReaderId(readerId);
        for (Notification n : unread) {
            n.markAsRead();
            notificationRepository.update(n);
        }
    }
}
