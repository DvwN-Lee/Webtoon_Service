package com.webtoon.repository;

import com.webtoon.common.repository.JsonRepository;
import com.webtoon.domain.Notification;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Notification Repository
 * 알림 JSON 파일 관리 및 조회 기능
 */
public class NotificationRepository extends JsonRepository<Notification> {

    @Override
    protected String getFileName() {
        return "notifications"; // 실제 저장 파일: notifications.json
    }

    @Override
    protected Class<Notification> getEntityClass() {
        return Notification.class;
    }

    @Override
    protected Long getId(Notification entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Notification entity, Long id) {
        entity.setId(id);
    }

    // === 추가 기능 ===

    /** 특정 독자(readerId)의 알림만 조회 */
    public List<Notification> findByReaderId(Long readerId) {
        return findAll().stream()
                .filter(n -> n.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }

    /** 특정 독자의 미확인 알림만 조회 */
    public List<Notification> findUnreadByReaderId(Long readerId) {
        return findAll().stream()
                .filter(n -> n.getReaderId().equals(readerId) && !n.isRead())
                .collect(Collectors.toList());
    }
}