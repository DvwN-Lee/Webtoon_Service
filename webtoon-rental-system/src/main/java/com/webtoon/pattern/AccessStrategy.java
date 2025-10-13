package com.webtoon.pattern;

import com.team.webtoon.domain.Reader;
import com.team.webtoon.domain.Episode;

/**
 * 작품 접근(대여/구매 등) 정책을 캡슐화하는 전략 인터페이스.
 * - execute: 실제로 대여/구매를 수행(포인트 차감, 기록 생성 등)
 * - canAccess: 해당 Reader가 지금 이 Episode를 볼 수 있는지 여부
 */
public interface AccessStrategy {
    /**
     * 정책 실행 (대여 혹은 구매)
     * @return 성공 여부
     */
    boolean execute(Reader reader, Episode episode);

    /**
     * 접근 가능 여부 판단 (대여 중 & 미만료, 또는 구매 소장)
     */
    boolean canAccess(Reader reader, Episode episode);

    /**
     * 전략 표시 이름 (e.g., "RENTAL", "PURCHASE")
     */
    String getStrategyName();
}
