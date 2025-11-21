package com.webtoon.service;

import com.webtoon.domain.Notification;
import com.webtoon.domain.Reader;
import com.webtoon.repository.NotificationRepository;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NotificationService 단위 테스트
 * 알림 생성, 조회, 읽음 처리 로직 검증
 */
class NotificationServiceTest {

    private NotificationService notificationService;
    private NotificationRepository notificationRepository;

    private static final String DATA_FILE = "src/main/resources/data/notifications.json";

    @BeforeEach
    void setUp() {
        notificationRepository = new NotificationRepository();
        notificationService = new NotificationService(notificationRepository);

        // 테스트 전 데이터 파일 초기화
        File f = new File(DATA_FILE);
        if (f.exists()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    @Test
    @DisplayName("알림 생성 및 저장 테스트")
    void createNotificationTest() {
        // Given
        Long readerId = 1L;
        Long webtoonId = 100L;

        // When
        notificationService.createNotification(readerId, webtoonId, "새 웹툰이 업로드되었습니다!");

        // Then
        List<Notification> all = notificationRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("새 웹툰이 업로드되었습니다!", all.get(0).getMessage());
        assertEquals(readerId, all.get(0).getReaderId());
        assertEquals(webtoonId, all.get(0).getWebtoonId());
        assertFalse(all.get(0).isRead());
    }

    @Test
    @DisplayName("알림 읽음 처리 테스트")
    void markAsReadTest() {
        // Given
        Long readerId = 2L;
        Long webtoonId = 200L;
        notificationService.createNotification(readerId, webtoonId, "읽기 테스트 알림");

        Notification n = notificationRepository.findAll().get(0);
        assertFalse(n.isRead());

        // When
        notificationService.markAsRead(n.getId());

        // Then
        Notification updated = notificationRepository.findById(n.getId());
        assertTrue(updated.isRead());
    }

    @Test
    @DisplayName("여러 알림을 최신순으로 정렬하여 조회")
    void getNotificationsSortedTest() {
        // Given
        Long readerId = 3L;
        Long webtoonId = 300L;
        notificationService.createNotification(readerId, webtoonId, "첫 번째 알림");
        notificationService.createNotification(readerId, webtoonId, "두 번째 알림");

        // When
        List<Notification> sortedList = notificationService.getNotifications(readerId);

        // Then
        assertEquals(2, sortedList.size());
        assertEquals("두 번째 알림", sortedList.get(0).getMessage()); // 최신순 정렬 확인
    }

    @Test
    @DisplayName("모든 알림 일괄 읽음 처리 테스트")
    void markAllAsReadTest() {
        // Given
        Long readerId = 4L;
        Long webtoonId = 400L;
        notificationService.createNotification(readerId, webtoonId, "1번 알림");
        notificationService.createNotification(readerId, webtoonId, "2번 알림");

        List<Notification> before = notificationRepository.findUnreadByReaderId(readerId);
        assertEquals(2, before.size());

        // When
        notificationService.markAllAsRead(readerId);

        // Then
        List<Notification> after = notificationRepository.findUnreadByReaderId(readerId);
        assertEquals(0, after.size());
    }
}
