package com.webtoon.service;

import com.webtoon.domain.Episode;
import com.webtoon.domain.Notification;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.NotificationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Notification 서비스 로직
 * 알림 생성 / 읽음 처리 / 조회 담당
 * + 새 회차 등록 시 팔로워에게 알림 전송 (콘솔 출력 버전)
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

    /** 새 회차 등록 시 팔로워에게 알림 전송 (콘솔 출력) */
    public void notifyNewEpisode(Webtoon webtoon, Episode episode) {
        for (Long userId : webtoon.getFollowerUserIds()) {
            System.out.printf(
                    "[알림] 사용자 %s → 웹툰 '%s'의 새 회차 공개: %s (회차번호 %d)%n",
                    userId, webtoon.getTitle(), episode.getTitle(), episode.getNumber()
            );
        }
    }

    /** 신규 알림 생성 */
    public void createNotification(Long readerId, Long webtoonId, String message) {
        List<Notification> all = notificationRepository.findAll();
        Long nextId = all.stream()
                .map(Notification::getId)
                .filter(id -> id != null)
                .max(Long::compareTo)
                .orElse(0L) + 1;

        Notification newNotification = new Notification(nextId, readerId, webtoonId, message);
        notificationRepository.save(newNotification);
    }

    /** 독자별 전체 알림 조회 (최신순 정렬) */
    public List<Notification> getNotifications(Long readerId) {
        return notificationRepository.findByReaderId(readerId).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** 독자별 미확인 알림만 조회 (최신순 정렬) */
    public List<Notification> getUnreadNotifications(Long readerId) {
        return notificationRepository.findUnreadByReaderId(readerId).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /** 알림 단건 읽음 처리 */
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (!n.isRead()) {
                n.markAsRead();
                notificationRepository.update(n);
            }
        });
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
