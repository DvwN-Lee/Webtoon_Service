package com.webtoon.repository;

import com.team.webtoon.domain.Purchase;
import com.team.webtoon.common.repository.JsonRepository;
import java.util.List;
import java.util.stream.Collectors;

public class PurchaseRepository extends JsonRepository<Purchase> {

    @Override
    protected String getFilePath() {
        return "src/main/resources/data/purchases.json";
    }

    @Override
    protected Class<Purchase> getEntityClass() {
        return Purchase.class;
    }

    // 특정 독자의 구매 목록
    public List<Purchase> findByReaderId(Long readerId) {
        return findAll().stream()
                .filter(p -> p.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }
}
