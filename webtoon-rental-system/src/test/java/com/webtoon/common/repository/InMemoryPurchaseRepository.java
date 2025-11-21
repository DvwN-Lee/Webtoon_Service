package com.webtoon.common.repository;

import com.webtoon.domain.Purchase;
import com.webtoon.repository.PurchaseRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryPurchaseRepository extends PurchaseRepository {

    private final List<Purchase> store = new ArrayList<>();
    private long seq = 1L;

    @Override
    public void save(Purchase purchase) {
        if (purchase.getId() == null) {
            purchase.setId(seq++);
        }
        store.removeIf(p -> p.getId().equals(purchase.getId()));
        store.add(purchase);
    }

    @Override
    public List<Purchase> findByReaderId(Long readerId) {
        return store.stream()
                .filter(p -> p.getReaderId().equals(readerId))
                .toList();
    }

    @Override
    public List<Purchase> findAll() {
        return new ArrayList<>(store);
    }
}