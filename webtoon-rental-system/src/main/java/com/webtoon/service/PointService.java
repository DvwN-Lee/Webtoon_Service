package com.webtoon.service;

import com.webtoon.domain.PaymentHistory;
import com.webtoon.pattern.PaymentStrategy;
import com.webtoon.repository.PaymentHistoryRepository;
import com.webtoon.repository.ReaderRepository;
import com.webtoon.domain.Reader;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class PointService {

    // 허용 충전 금액(원)
    private static final Set<Integer> ALLOWED_AMOUNTS =
            Set.of(1_000, 5_000, 10_000, 50_000);

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
     * 포인트 충전 (FR-PAYMENT-01~04)
     * 1) 금액 검증 → 2) 결제전략 실행 → 3) 포인트 환산/적립 → 4) 이력 저장 → 5) Reader DB 저장
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

        // 3) 항상 최신 Reader를 다시 조회
        Reader latestReader = readerRepository.findById(reader.getId())
                .orElseThrow(() -> new IllegalArgumentException("Reader not found"));

        // 4) 포인트 환산 및 적립
        int points = amount / WON_PER_POINT;
        latestReader.addPoints(points);

        // 5) 결제(충전) 내역 저장
        LocalDateTime now = LocalDateTime.now(clock);

        PaymentHistory history = new PaymentHistory(
                null,                       // id는 Repository에서 생성
                reader.getId(),
                amount,                      // 결제금액
                points,                      // 충전된 포인트
                strategy.getPaymentMethodName(),
                now                          // Clock 기반 시간
        );

        paymentHistoryRepository.save(history);

        // 7) Reader의 포인트 변경사항을 DB에 저장
        // 최신 Reader를 저장해야 팔로우 데이터가 덮어씌워지지 않음
        readerRepository.update(latestReader);

        //  파라미터로 받은 reader도 최신값 동기화
        reader.setPoints(latestReader.getPoints());

        return true;
    }

    /** 충전 내역 조회 (FR-PAYMENT-05) */
    public List<PaymentHistory> getPaymentHistory(Long readerId) {
        return paymentHistoryRepository.findByReaderId(readerId);
    }
}
