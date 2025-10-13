package com.webtoon.pattern;

import com.team.webtoon.domain.Reader;
import com.team.webtoon.domain.Episode;
import com.team.webtoon.domain.Purchase;
import com.team.webtoon.repository.RentalRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 회차 구매(영구 소장) 전략.
 */
public class PurchaseAccessStrategy implements AccessStrategy {

    private final PurchaseRepository purchaseRepository;

    public PurchaseAccessStrategy(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public boolean execute(Reader reader, Episode episode) {
        int price = episode.getPurchasePrice();
        // 1) 포인트 검증 및 차감
        if (!reader.usePoints(price)) return false;

        // 2) Purchase 생성 후 저장 (영구 소장)
        Purchase purchase = new Purchase(
                null,
                reader.getId(),
                episode.getId(),
                LocalDateTime.now()
        );
        purchaseRepository.save(purchase);
        return true;
    }

    @Override
    public boolean canAccess(Reader reader, Episode episode) {
        // 구매 내역이 존재하면 항상 접근 가능
        List<Purchase> purchases = purchaseRepository.findByReaderId(reader.getId());
        return purchases.stream().anyMatch(p -> p.getEpisodeId().equals(episode.getId()));
    }

    @Override
    public String getStrategyName() {
        return "PURCHASE";
    }
}
