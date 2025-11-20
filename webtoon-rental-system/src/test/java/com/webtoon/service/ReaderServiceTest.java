package com.webtoon.service;

import com.webtoon.common.validation.ValidationException;
import com.webtoon.domain.Notification;
import com.webtoon.domain.Reader;
import com.webtoon.domain.Rental;
import com.webtoon.domain.Purchase;
import com.webtoon.repository.ReaderRepository;
import com.webtoon.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReaderServiceTest {

    @Mock private ReaderRepository readerRepository;
    @Mock private UserRepository userRepository;       // 중복 검증용 Mock
    @Mock private AccessService accessService;         // 통계용 Mock
    @Mock private NotificationService notificationService; // 알림용 Mock
    
    @InjectMocks
    private ReaderService readerService;

    private Reader testReader;
    private final Long READER_ID = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testReader = new Reader("reader1", "pass1234", "독자A");
        testReader.setId(READER_ID);
        testReader.setPoints(1000);
        
        when(readerRepository.findById(READER_ID)).thenReturn(testReader);
    }

    @Test
    @DisplayName("닉네임 수정 실패 - 중복 검증 (Team Feedback 반영)")
    void testUpdateNickname_Duplicate() {
        // Given
        String duplicateNickname = "중복닉";
        when(userRepository.existsByNickname(duplicateNickname)).thenReturn(true);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            readerService.updateNickname(READER_ID, duplicateNickname);
        }, "이미 사용 중인 닉네임입니다.");

        // 업데이트가 호출되지 않았는지 확인
        verify(readerRepository, never()).update(any(Reader.class));
    }
    
    @Test
    @DisplayName("홈 화면 데이터 조회 - 알림 서비스 연동 확인")
    void testGetHomeScreenData() {
        // Given
        List<Notification> mockNotifications = List.of(new Notification(1L, READER_ID, 100L, "알림"));
        when(notificationService.getUnreadNotifications(READER_ID)).thenReturn(mockNotifications);

        // When
        Map<String, Object> data = readerService.getHomeScreenData(READER_ID);

        // Then
        assertEquals("독자A", data.get("nickname"));
        assertEquals(1000, data.get("points"));
        assertEquals(1, data.get("unreadNotifications")); 
    }

    @Test
    @DisplayName("프로필 조회 - AccessService 통계 연동 확인")
    void testGetProfile() {
        // Given
        // 정다민님의 AccessService가 가짜 리스트를 반환하도록 설정
        when(accessService.getRentals(READER_ID)).thenReturn(Arrays.asList(mock(Rental.class), mock(Rental.class))); // 2개
        when(accessService.getPurchases(READER_ID)).thenReturn(Collections.singletonList(mock(Purchase.class)));     // 1개

        // When
        Map<String, Object> profile = readerService.getProfile(READER_ID);

        // Then
        assertEquals(2, profile.get("rentalCount"));
        assertEquals(1, profile.get("purchaseCount"));
        assertEquals(0, profile.get("followingCount")); // 초기 팔로우 0
    }
}