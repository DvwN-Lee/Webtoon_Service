package com.webtoon.service;

import com.team.webtoon.domain.Reader;
import com.team.webtoon.domain.PaymentHistory;
import com.team.webtoon.pattern.PaymentStrategy;
import com.team.webtoon.repository.PaymentHistoryRepository;
import com.team.webtoon.repository.ReaderRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class PointService {

    // 허용 충전 금액(원)
    private static final Set<Integer> ALLOWED_AMOUNTS = Set.of(1_000, 5_000, 10_000, 50_000);
    // 10원 = 1포인트
    private static final int WON_PER_POINT = 10;

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final ReaderRepository readerRepository;
    private final Clock clock;

    public PointService(PaymentHistoryRepository paymentHistoryRepository,
                        ReaderRepository readerRepository,
                        Clock clock) {
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.readerRepository = readerRepository;
        this.clock = clock;
    }

    /**
     * 포인트 충전 (FR-PAYMENT-01~04, NFR-DATA-02,03)
     * 1) 금액 검증 → 2) 결제전략 실행 → 3) 포인트 환산/적립 → 4) 이력 저장
     * 결제 전략을 이용해 포인트 충전 수행.
     * @param reader   충전할 독자
     * @param amount 결제 금액(원) — 1,000 / 5,000 / 10,000 / 50,000 허용
     * @param strategy 결제 수단 전략
     * @return 충전 성공 여부
     */
    public boolean chargePoints(Reader reader, int amount, PaymentStrategy strategy) {
        // 1) 금액 검증
        if (!ALLOWED_AMOUNTS.contains(amount)) {
            return false;
        }

        // 2) 결제 시도(전략)
        boolean paid = strategy.processPayment(amount);
        if (!paid) {
            return false;
        }

        // 3) 포인트 환산 및 적립
        int points = amount / WON_PER_POINT; // 정수 나눗셈(소수점 버림)
        reader.addPoints(points);
        readerRepository.update(reader); // Reader 저장 (JsonRepository 기반이라 가정)

        // 4) 결제(충전) 내역 저장
        PaymentHistory history = new PaymentHistory(
                null,                       // id는 Repository에서 채번/생성
                reader.getId(),
                amount,                         // 결제금액(원)
                points,                         // 충전 포인트
                strategy.getPaymentMethodName(),// "신용카드"/"계좌이체" 등
                LocalDateTime.now()             // Clock 기반 시간

        );
        paymentHistoryRepository.save(history);

        return true;
    }

    /** 충전 내역 조회 (FR-PAYMENT-05) */
    public List<PaymentHistory> getPaymentHistory(Long readerId) {
        return paymentHistoryRepository.findByReaderId(readerId);
    }
}
