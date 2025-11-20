package com.webtoon.service;

import com.webtoon.common.validation.ValidationException;
import com.webtoon.common.validation.Validator;
import com.webtoon.domain.Reader;
import com.webtoon.repository.ReaderRepository;
import com.webtoon.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 독자 관련 서비스 (홍승현)
 * 이슈 #7: 프로필, 홈 화면, 닉네임 수정(중복 검증 포함)
 * 의존성: UserRepository(중복검증), AccessService(통계), NotificationService(알림)
 */
public class ReaderService {

    private final ReaderRepository readerRepository;
    private final UserRepository userRepository;       // 닉네임 중복 확인용
    private final AccessService accessService;         // 대여/구매 통계용 (정다민님)
    private final NotificationService notificationService; // 홈 화면 알림용

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReaderService(
            ReaderRepository readerRepository,
            UserRepository userRepository,
            AccessService accessService,
            NotificationService notificationService
    ) {
        this.readerRepository = readerRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
        this.notificationService = notificationService;
    }

    /**
     * FR-READER-02: 닉네임 변경
     * [수정] UserRepository를 이용한 중복 검증 로직 추가
     */
    public void updateNickname(Long readerId, String newNickname) {
        // 1. 형식 검증
        Validator.validateDisplayName(newNickname);

        // 2. 중복 검증 (팀장님 피드백 반영)
        if (userRepository.existsByNickname(newNickname)) {
            throw new ValidationException("이미 사용 중인 닉네임입니다.");
        }

        Reader reader = readerRepository.findById(readerId);
        if (reader == null) {
            throw new ValidationException("존재하지 않는 독자입니다.");
        }

        // 3. 업데이트 및 저장
        reader.updateNickname(newNickname);
        readerRepository.update(reader);
        System.out.println("✅ 닉네임 변경 완료: " + newNickname);
    }

    /**
     * FR-READER-03: 홈 화면 데이터 조회
     * [구현] 포인트 및 미확인 알림 개수 반환
     */
    public Map<String, Object> getHomeScreenData(Long readerId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader == null) throw new ValidationException("독자 정보 없음");

        // 최신 알림 정보를 가져와서 Reader 객체 갱신 (화면 표시용)
        reader.setNotifications(notificationService.getUnreadNotifications(readerId));

        Map<String, Object> data = new HashMap<>();
        data.put("nickname", reader.getNickname());
        data.put("points", reader.getPoints());
        data.put("unreadNotifications", reader.getUnreadNotificationCount());
        
        return data;
    }

    /**
     * FR-READER-01: 프로필 상세 조회
     * [구현] 가입일, 팔로우 수, 대여/구매 수(AccessService 연동) 반환
     */
    public Map<String, Object> getProfile(Long readerId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader == null) throw new ValidationException("독자 정보 없음");

        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", reader.getNickname());
        profile.put("points", reader.getPoints());
        profile.put("createdAt", reader.getCreatedAt().format(DATE_FORMATTER));
        profile.put("followingCount", reader.getFollowingWebtoonIds().size());

        // AccessService를 통해 실제 통계 데이터 조회
        profile.put("rentalCount", accessService.getRentals(readerId).size());
        profile.put("purchaseCount", accessService.getPurchases(readerId).size());

        return profile;
    }

    // --- 팔로우/언팔로우 (Issue #3 기능) ---
    
    public void followWebtoon(Long readerId, Long webtoonId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader != null) {
            reader.followWebtoon(webtoonId);
            readerRepository.update(reader);
        }
    }

    public void unfollowWebtoon(Long readerId, Long webtoonId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader != null) {
            reader.unfollowWebtoon(webtoonId);
            readerRepository.update(reader);
        }
    }
}