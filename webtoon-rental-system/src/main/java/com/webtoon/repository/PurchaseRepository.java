package com.webtoon.repository;

import com.webtoon.common.repository.JsonRepository;
import com.webtoon.domain.Purchase;

import java.util.List;
import java.util.stream.Collectors;

public class PurchaseRepository extends JsonRepository<Purchase> {

    @Override
    protected String getFileName() {
        return "purchases";   // → src/main/resources/data/purchases.json
    }

    @Override
    protected Class<Purchase> getEntityClass() {
        return Purchase.class;
    }

    @Override
    protected Long getId(Purchase entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Purchase entity, Long id) {
        entity.setId(id);
    }

    // 특정 독자의 구매 내역 조회
    public List<Purchase> findByReaderId(Long readerId) {
        return findAll().stream()
                .filter(p -> p.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }
}

