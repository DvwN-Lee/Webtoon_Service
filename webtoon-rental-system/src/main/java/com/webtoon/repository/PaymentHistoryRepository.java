package com.webtoon.repository;

import com.team.webtoon.domain.PaymentHistory;
import com.team.webtoon.common.repository.JsonRepository;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentHistoryRepository extends JsonRepository<PaymentHistory> {

    @Override
    protected String getFilePath() {
        return "src/main/resources/data/payment_histories.json";
    }

    @Override
    protected Class<PaymentHistory> getEntityClass() {
        return PaymentHistory.class;
    }

    // 특정 독자의 충전 내역 목록
    public List<PaymentHistory> findByReaderId(Long readerId) {
        return findAll().stream()
                .filter(h -> h.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }
}