package com.webtoon.service;

import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;

import java.util.ArrayList;
import java.util.List;

/**
 * 테스트에서 알림을 '기록'하기 위한 전용 구현.
 * NotificationService를 상속해서 notify 호출 내역을 저장한다.
 */
public class TestNotificationService extends NotificationService {

    public static class Notice {
        public final String userId;
        public final String webtoonTitle;
        public final int episodeNumber;
        public final String episodeTitle;

        public Notice(String userId, String webtoonTitle, int episodeNumber, String episodeTitle) {
            this.userId = userId;
            this.webtoonTitle = webtoonTitle;
            this.episodeNumber = episodeNumber;
            this.episodeTitle = episodeTitle;
        }
    }

    private final List<Notice> notices = new ArrayList<>();

    @Override
    public void notifyNewEpisode(Webtoon webtoon, Episode episode) {
        // 원래 콘솔 출력은 건너뛰고, 기록만 남긴다.
        for (String userId : webtoon.getFollowerUserIds()) {
            notices.add(new Notice(userId, webtoon.getTitle(), episode.getNumber(), episode.getTitle()));
        }
    }

    public List<Notice> getNotices() {
        return notices;
    }

    public void clear() {
        notices.clear();
    }
}
