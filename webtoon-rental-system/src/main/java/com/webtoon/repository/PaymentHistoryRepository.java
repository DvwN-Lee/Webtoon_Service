package com.webtoon.repository;

import com.webtoon.domain.PaymentHistory;
import com.webtoon.common.repository.JsonRepository;

import java.util.List;
import java.util.stream.Collectors;

public class PaymentHistoryRepository extends JsonRepository<PaymentHistory> {

    @Override
    protected String getFileName() {
        return "payment_histories";   // ✔ 파일명만 반환!
    }

    @Override
    protected Class<PaymentHistory> getEntityClass() {
        return PaymentHistory.class;  // ✔
    }

    @Override
    protected Long getId(PaymentHistory entity) {
        return entity.getId();        // ✔
    }

    @Override
    protected void setId(PaymentHistory entity, Long id) {
        entity.setId(id);             // ✔ PaymentHistory에 setter가 필요함
    }

    // 특정 독자의 충전 내역 목록
    public List<PaymentHistory> findByReaderId(Long readerId) {
        return findAll().stream()
                .filter(h -> h.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }
}