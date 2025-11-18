package com.webtoon.service;

import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;

/**
 * 새 회차 등록 시 팔로워에게 알림 전송 (콘솔 출력 버전)
 */
public class NotificationService {

    public void notifyNewEpisode(Webtoon webtoon, Episode episode) {
        for (String userId : webtoon.getFollowerUserIds()) {
            System.out.printf(
                    "[알림] 사용자 %s → 웹툰 '%s'의 새 회차 공개: %s (회차번호 %d)%n",
                    userId, webtoon.getTitle(), episode.getTitle(), episode.getNumber()
            );
        }
    }
}
