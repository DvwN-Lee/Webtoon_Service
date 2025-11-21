package com.webtoon.pattern;

import com.webtoon.domain.Reader;
import com.webtoon.domain.Episode;
import java.time.Clock;

public interface AccessStrategy {

    /**
     * 정책 실행
     * @return Rental or Purchase, or null
     */
    Object execute(Reader reader, Episode episode, Clock clock);

    /**
     * 접근 가능 여부
     */
    boolean canAccess(Reader reader, Episode episode, Clock clock);

    /**
     * 전략 이름
     */
    String getStrategyName();
}
