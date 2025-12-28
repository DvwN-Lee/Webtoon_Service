package com.webtoon.common.repository;

import com.webtoon.domain.PaymentHistory;
import com.webtoon.repository.PaymentHistoryRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryPaymentHistoryRepository extends PaymentHistoryRepository {

    private final List<PaymentHistory> store = new ArrayList<>();
    private long seq = 1L;

    @Override
    public PaymentHistory save(PaymentHistory history) {
        if (history.getId() == null) {
            history.setId(seq++);
        }
        store.removeIf(h -> h.getId().equals(history.getId()));
        store.add(history);
        return history;
    }

    @Override
    public List<PaymentHistory> findByReaderId(Long readerId) {
        return store.stream()
                .filter(h -> h.getReaderId().equals(readerId))
                .toList();
    }

    @Override
    public List<PaymentHistory> findAll() {
        return new ArrayList<>(store);
    }
}
