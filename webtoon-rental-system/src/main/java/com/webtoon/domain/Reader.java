package com.webtoon.domain;

import com.webtoon.pattern.Observer;
import java.util.ArrayList;
import java.util.List;

/**
 * 독자 도메인 모델
 * [수정 사항 - Issue #3, #5 피드백 반영]
 * 1. Observer.update(Webtoon, Episode) 메서드 구현 수정
 * 2. 객체 기반 팔로우 메서드(followWebtoon(Webtoon)) 유지
 */
public class Reader extends User implements Observer {

    private String nickname;
    private List<Long> followingWebtoonIds;
    private transient List<Notification> notifications;

    public Reader() {
        super();
        this.followingWebtoonIds = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    public Reader(String username, String password, String nickname) {
        super(username, password, 1000);
        this.nickname = nickname;
        this.followingWebtoonIds = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    // --- 팔로우 기능 (Issue #3 해결) ---
    public void followWebtoon(Long webtoonId) {
        if (webtoonId != null && !isFollowing(webtoonId)) {
            followingWebtoonIds.add(webtoonId);
        }
    }

    public void unfollowWebtoon(Long webtoonId) {
        followingWebtoonIds.remove(webtoonId);
    }

    public boolean isFollowing(Long webtoonId) {
        return followingWebtoonIds.contains(webtoonId);
    }

    public void followWebtoon(Webtoon webtoon) {
        if (isValidWebtoon(webtoon)) followWebtoon(webtoon.getId());
    }

    public void unfollowWebtoon(Webtoon webtoon) {
        if (isValidWebtoon(webtoon)) unfollowWebtoon(webtoon.getId());
    }

    public boolean isFollowing(Webtoon webtoon) {
        return isValidWebtoon(webtoon) && isFollowing(webtoon.getId());
    }

    private boolean isValidWebtoon(Webtoon webtoon) {
        return webtoon != null && webtoon.getId() != null;
    }

    // --- [수정] Observer 패턴 구현 (Issue #5 피드백 반영) ---

    @Override
    public void update(Webtoon webtoon, Episode episode) {
        // 1. 알림 메시지 포맷팅 (객체 데이터 활용)
        String message = String.format("'%s'의 새 회차 [%d화: %s]가 업로드되었습니다!",
                                     webtoon.getTitle(), episode.getNumber(), episode.getTitle());

        // 2. 콘솔 알림 출력 (CLI 요구사항)
        System.out.println("🔔 [" + nickname + "님 알림] " + message);

        // 3. 인메모리 리스트에 추가 (Runtime 확인용)
        if (this.notifications == null) {
            this.notifications = new ArrayList<>();
        }
        // ID는 DB 저장 시 부여되므로 null로 설정
        this.notifications.add(new Notification(null, this.getId(), webtoon.getId(), message));

        /*
         * [Team Leader 피드백 대응 - Repository 저장]
         * Reader는 도메인 객체이므로 Repository를 직접 의존할 수 없습니다.
         * 따라서 실제 DB(JSON) 저장은 이 update 메서드가 호출된 직후,
         * Service Layer(WebtoonService 등)에서 NotificationService를 호출하여 처리해야 합니다.
         * (통합 테스트 및 Service 연동 로직에서 이 부분이 구현됩니다.)
         */
    }

    @Override
    public Long getUserId() {
        return this.getId();
    }

    // --- Helper & Getters ---

    public int getUnreadNotificationCount() {
        if (notifications == null) return 0;
        return (int) notifications.stream().filter(n -> !n.isRead()).count();
    }

    @Override
    public String getDisplayName() { return nickname; }

    @Override
    public String getUserType() { return "READER"; }

    public void updateNickname(String nickname) { this.nickname = nickname; }

    public String getNickname() { return nickname; }

    public List<Long> getFollowingWebtoonIds() { return followingWebtoonIds; }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<Notification> getNotifications() {
        if (notifications == null) notifications = new ArrayList<>();
        return notifications;
    }
}