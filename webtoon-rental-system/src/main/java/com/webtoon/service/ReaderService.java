package com.webtoon.service;

import com.webtoon.common.validation.ValidationException;
import com.webtoon.common.validation.Validator;
import com.webtoon.domain.Reader;
import com.webtoon.repository.ReaderRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * 독자 관련 서비스 (홍승현)
 */
public class ReaderService {

    private final ReaderRepository readerRepository;
    private final NotificationService notificationService;

    public ReaderService() {
        this.readerRepository = new ReaderRepository();
        this.notificationService = new NotificationService();
    }

    // 테스트용 DI 생성자
    public ReaderService(ReaderRepository readerRepository, NotificationService notificationService) {
        this.readerRepository = readerRepository;
        this.notificationService = notificationService;
    }

    /**
     * 닉네임 변경 (중복 검증 포함)
     */
    public void updateNickname(Long readerId, String newNickname) {
        // 형식 검증
        Validator.validateDisplayName(newNickname);

        Reader reader = readerRepository.findById(readerId);
        if (reader == null) {
            throw new ValidationException("존재하지 않는 독자입니다.");
        }

        // 중복 검증
        boolean isTaken = readerRepository.findAll().stream()
                .filter(r -> !r.getId().equals(readerId))
                .anyMatch(r -> r.getNickname().equalsIgnoreCase(newNickname.trim()));

        if (isTaken) {
            throw new ValidationException("이미 사용 중인 닉네임입니다.");
        }

        reader.updateNickname(newNickname);
        readerRepository.update(reader);
    }

    /**
     * 독자 프로필 조회
     */
    public Map<String, Object> getProfile(Long readerId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader == null) {
            throw new ValidationException("존재하지 않는 독자입니다.");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", reader.getNickname());
        profile.put("points", reader.getPoints());
        profile.put("createdAt", reader.getCreatedAt());
        profile.put("followingCount", reader.getFollowingWebtoonIds().size());
        // TODO: 대여 중 작품 수, 구매 완료 작품 수는 [정다민]님의 작업 완료 후 추가
        profile.put("rentalCount", 0);
        profile.put("purchaseCount", 0);

        return profile;
    }

    /**
     * 독자 홈 화면 데이터 조회
     */
    public Map<String, Object> getHomeScreen(Long readerId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader == null) {
            throw new ValidationException("존재하지 않는 독자입니다.");
        }

        Map<String, Object> homeScreen = new HashMap<>();
        homeScreen.put("points", reader.getPoints());
        homeScreen.put("unreadNotificationCount",
            notificationService.getUnreadNotifications(readerId).size());

        return homeScreen;
    }

    /**
     * 팔로우
     */
    public void followWebtoon(Long readerId, Long webtoonId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader != null) {
            reader.followWebtoon(webtoonId);
            readerRepository.update(reader);
        }
    }

    /**
     * 언팔로우
     */
    public void unfollowWebtoon(Long readerId, Long webtoonId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader != null) {
            reader.unfollowWebtoon(webtoonId);
            readerRepository.update(reader);
        }
    }
}
