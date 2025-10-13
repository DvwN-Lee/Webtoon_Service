package com.webtoon.pattern;

import com.team.webtoon.domain.Reader;
import com.team.webtoon.domain.Episode;
import com.team.webtoon.domain.Rental;
import com.team.webtoon.repository.RentalRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 회차 대여(임시 접근) 전략.
 * 데모/테스트를 위해 만료시간은 "10분"으로 설정. (요구사항/시나리오)
 */
public class RentalAccessStrategy implements AccessStrategy {

    // 데모 규격: 10분 대여 (시나리오에 명시됨)
    private static final Duration RENTAL_DURATION = Duration.ofMinutes(10);

    // AccessService에서 쓰기 위한 공개 getter
    public static Duration defaultDuration() {
        return RENTAL_DURATION;
    }

    private final RentalRepository rentalRepository;

    public RentalAccessStrategy(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @Override
    public boolean execute(Reader reader, Episode episode) {
        int price = episode.getRentalPrice();
        // 1) 포인트 검증 및 차감
        if (!reader.usePoints(price)) return false;

        // 2) Rental 생성 후 저장
        LocalDateTime now = LocalDateTime.now();
        Rental rental = new Rental(
                null,
                reader.getId(),
                episode.getId(),
                now,
                now.plus(RENTAL_DURATION)
        );
        rentalRepository.save(rental);
        return true;
    }

    @Override
    public boolean canAccess(Reader reader, Episode episode) {
        // 활성 대여 중 & 미만료 인지 확인
        List<Rental> actives = rentalRepository.findActiveRentals(reader.getId());
        return actives.stream()
                .filter(r -> r.getEpisodeId().equals(episode.getId()))
                .anyMatch(r -> !r.isExpired());
    }

    @Override
    public String getStrategyName() {
        return "RENTAL";
    }
}
