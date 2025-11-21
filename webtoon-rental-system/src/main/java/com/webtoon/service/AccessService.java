package com.webtoon.service;

import com.webtoon.domain.Rental;
import com.webtoon.domain.Purchase;
import com.webtoon.domain.Reader;
import com.webtoon.domain.Episode;
import com.webtoon.pattern.AccessStrategy;
import com.webtoon.pattern.PurchaseAccessStrategy;
import com.webtoon.repository.RentalRepository;
import com.webtoon.repository.PurchaseRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class AccessService {

    private final RentalRepository rentalRepository;
    private final PurchaseRepository purchaseRepository;
    private final Clock clock;

    /**
     * FR-EPISODE-02 (회차 열람),
     * FR-RENTAL-01  (회차 대여),
     * FR-PURCHASE-01 (회차 구매)
     *
     * 전략 패턴을 사용하여 대여/구매 수행을 위임하는 서비스.
     * Clock을 주입하여 테스트에서 시간 기반 기능(대여 만료 등)을
     * 안정적으로 검사할 수 있도록 설계됨.
     */
    public AccessService(RentalRepository rentalRepository,
                         PurchaseRepository purchaseRepository,
                         Clock clock) {
        this.rentalRepository = rentalRepository;
        this.purchaseRepository = purchaseRepository;
        this.clock = clock;
    }

    /**
     * 회차 대여/구매 기능
     *
     * - RentalAccessStrategy : 일반 대여
     * - PurchaseAccessStrategy : 일반 구매 + “대여 중 → 구매 전환”까지 처리
     *
     * 동작 요약
     * 1) PurchaseAccessStrategy 가 아닌 경우
     *    - 이미 접근 가능하면(대여/구매) 추가 과금 없이 true
     * 2) PurchaseAccessStrategy 인 경우
     *    - 이미 구매한 회차면 true (재구매 X)
     *    - 대여 중이면 “구매가 - 대여가” 만큼만 포인트 추가 차감 후 구매 기록 생성
     *    - 그 외에는 전략에 위임(일반 구매 – 전액 차감)
     */
    public boolean grantAccess(Reader reader, Episode episode, AccessStrategy strategy) {

        Long readerId = reader.getId();
        Long epId = episode.getId();

        // 1) PurchaseAccessStrategy 가 아닌 경우 : 기존처럼 “이미 접근 가능하면” 스킵
        if (!(strategy instanceof PurchaseAccessStrategy)) {
            if (canAccess(reader, episode)) {
                return true;
            }
        } else {
            // 2) PurchaseAccessStrategy 인 경우

            // 2-1) 이미 구매한 회차라면 재구매 불필요
            boolean alreadyPurchased = purchaseRepository.findByReaderId(readerId).stream()
                    .anyMatch(p -> p.getEpisodeId().equals(epId));
            if (alreadyPurchased) {
                return true;
            }

            // 2-2) “대여 중 & 미만료” 상태라면 → 차액만 차감 (FR-PURCHASE-03)
            boolean hasActiveRental = rentalRepository.findByReaderId(readerId).stream()
                    .filter(r -> r.getEpisodeId().equals(epId))
                    .anyMatch(r -> !r.isExpired(clock));

            if (hasActiveRental) {
                int rentalPrice = episode.getRentalPrice();
                int purchasePrice = episode.getPurchasePrice();
                int diff = purchasePrice - rentalPrice;

                // 구매가가 더 싸거나 같으면 추가 차감 없이 소장 처리만
                if (diff > 0) {
                    if (!reader.usePoints(diff)) {
                        return false;   // 포인트 부족
                    }
                }

                // 실제 구매 기록은 full price 로 남겨도 되고, diff 로 남겨도 되지만
                // 여기서는 "구매가"를 기록하는 것으로 처리
                Purchase purchase = new Purchase(
                        null,
                        readerId,
                        epId,
                        purchasePrice,
                        LocalDateTime.now(clock)
                );
                purchaseRepository.save(purchase);
                return true;
            }
            // ※ 여기까지 왔다면: 아직 대여도, 구매도 안 한 상태의 "일반 첫 구매"
            //    → 아래의 strategy.execute(...) 로 진행
        }

        // 3) 그 외의 일반 케이스: 전략에 위임
        Object result = strategy.execute(reader, episode, clock);
        if (result == null) return false;

        if (result instanceof Rental rental) {
            rentalRepository.save(rental);
            return true;
        }

        if (result instanceof Purchase purchase) {
            purchaseRepository.save(purchase);
            return true;
        }

        return false;
    }

    /**
     * 전략을 이용한 접근 가능 판단(특정 전략 기반)
     */
    public boolean canAccess(Reader reader, Episode episode, AccessStrategy strategy) {
        return strategy.canAccess(reader, episode, clock);
    }

    /**
     * 기본 접근 가능 여부 판단
     * - 이미 구매한 회차인지
     * - 대여 중이고 만료되지 않았는지
     */
    public boolean canAccess(Reader reader, Episode episode) {
        Long readerId = reader.getId();
        Long epId = episode.getId();

        // 1) 구매했으면 무조건 접근 가능
        boolean purchased = purchaseRepository.findByReaderId(readerId).stream()
                .anyMatch(p -> p.getEpisodeId().equals(epId));
        if (purchased) return true;

        // 2) 대여 중이며 미만료
        return rentalRepository.findByReaderId(readerId).stream()
                .filter(r -> r.getEpisodeId().equals(epId))
                .anyMatch(r -> !r.isExpired(clock));
    }

    /**
     * FR-RENTAL-03: 대여 목록 조회
     */
    public List<Rental> getRentals(Long readerId) {
        return rentalRepository.findByReaderId(readerId).stream()
                .filter(r -> !r.isExpired(clock))
                .toList();
    }

    /**
     * FR-PURCHASE-02: 구매 목록 조회
     */
    public List<Purchase> getPurchases(Long readerId) {
        return purchaseRepository.findByReaderId(readerId);
    }
}
