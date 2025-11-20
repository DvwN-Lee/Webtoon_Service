package com.webtoon.pattern;

import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;

/**
 * 옵저버 패턴 인터페이스 (NFR-MAIN-03)
 * Subject(Webtoon)의 상태 변화(신규 회차)를 Observer(Reader)에게 알림
 *
 * [수정 사항 - Issue #5 피드백 반영]
 * - update 메서드 시그니처 변경: update(String) -> update(Webtoon, Episode)
 * - 구조화된 데이터를 전달받아 Reader가 유연하게 처리 가능하도록 개선
 */
public interface Observer {

    /**
     * 알림 수신 메서드
     * Subject(Webtoon)가 notifyObservers(Episode)를 호출할 때 실행됨
     *
     * @param webtoon 업데이트된 웹툰 정보 (Subject)
     * @param episode 새로 등록된 회차 정보 (Event Data)
     */
    void update(Webtoon webtoon, Episode episode);

    /**
     * 알림을 받을 사용자(Reader)의 ID 반환
     * (NotificationService에서 DB 저장 시 식별자로 사용)
     *
     * @return 독자 ID (Long)
     */
    Long getUserId();
}