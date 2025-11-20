package com.webtoon.domain;

import com.webtoon.pattern.Observer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Reader의 Observer 패턴 구현 테스트 (Issue #5)
 * 팀원 코드를 수정하지 않고, Mock 객체를 사용하여 Reader의 반응을 테스트함
 */
class ReaderObserverTest {

    @Test
    @DisplayName("Reader가 Observer 구현체로서 update 호출 시 알림을 내부 목록에 추가하는지 검증")
    void testUpdateAddsNotification() {
        // Given
        Reader reader = new Reader("testUser", "pw", "Tester");
        reader.setId(1L);

        // Webtoon, Episode는 유연주님 파트이므로 Mock으로 대체하여 테스트 격리
        Webtoon mockWebtoon = mock(Webtoon.class);
        Episode mockEpisode = mock(Episode.class);

        when(mockWebtoon.getTitle()).thenReturn("테스트 웹툰");
        when(mockEpisode.getNumber()).thenReturn(99);
        when(mockEpisode.getTitle()).thenReturn("충격적인 결말");

        // When: Observer의 update 메서드 호출 (Subject가 호출했다고 가정)
        reader.update(mockWebtoon, mockEpisode);

        // Then: Reader 내부에 알림이 생성되었는지 확인
        assertFalse(reader.getNotifications().isEmpty());
        Notification n = reader.getNotifications().get(0);
        
        assertTrue(n.getMessage().contains("테스트 웹툰"));
        assertTrue(n.getMessage().contains("99화"));
        assertEquals(reader.getId(), n.getReaderId());
        
        // 홈 화면용 카운트 확인
        assertEquals(1, reader.getUnreadNotificationCount());
    }
}