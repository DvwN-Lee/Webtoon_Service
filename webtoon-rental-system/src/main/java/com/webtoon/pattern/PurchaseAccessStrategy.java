package com.webtoon.pattern;

import com.webtoon.domain.Purchase;
import com.webtoon.domain.Reader;
import com.webtoon.domain.Episode;
import com.webtoon.repository.PurchaseRepository;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.List;

public class PurchaseAccessStrategy implements AccessStrategy {

    private final PurchaseRepository purchaseRepository;

    public PurchaseAccessStrategy(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Object execute(Reader reader, Episode episode, Clock clock) {

        int price = episode.getBuyPrice();

        if (!reader.usePoints(price)) {
            return null;
        }

        return new Purchase(
                null,
                reader.getId(),
                episode.getId(),
                price,
                LocalDateTime.now(clock)
        );
    }

    @Override
    public boolean canAccess(Reader reader, Episode episode, Clock clock) {

        List<Purchase> list = purchaseRepository.findByReaderId(reader.getId());

        return list.stream()
                .anyMatch(p -> p.getEpisodeId().equals(episode.getId()));
    }

    @Override
    public String getStrategyName() {
        return "PURCHASE";
    }
}
