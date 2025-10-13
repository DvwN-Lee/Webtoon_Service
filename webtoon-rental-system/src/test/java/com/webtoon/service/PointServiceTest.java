package com.webtoon.service;

import com.webtoon.domain.PaymentHistory;
import com.webtoon.domain.Reader;
import com.webtoon.pattern.CreditCardPaymentStrategy;
import com.webtoon.pattern.BankTransferPaymentStrategy;
import com.webtoon.pattern.PaymentStrategy;
import com.webtoon.repository.PaymentHistoryRepository;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointServiceTest {

    private PaymentHistoryRepository paymentRepo;
    private PointService pointService;
    private Reader reader;

    @BeforeEach
    void setUp() {
        paymentRepo = new PaymentHistoryRepository(); // JSON 파일을 쓰는 실제 구현체여도 무방
        pointService = new PointService(paymentRepo);
        reader = new Reader(1L, "reader1", "1234", "닉", 0);
    }

    @Test
    @DisplayName("신용카드 결제: 10,000원 → 1,000P 충전")
    void creditCard_payment_charges_points() {
        PaymentStrategy card = new CreditCardPaymentStrategy();

        boolean ok = pointService.chargePoints(reader, 10_000, card);

        assertTrue(ok);
        assertEquals(1_000, reader.getPoints());

        List<PaymentHistory> logs = pointService.getPaymentHistory(reader.getId());
        assertFalse(logs.isEmpty());
        assertEquals(10_000, logs.get(logs.size()-1).getAmount());
        assertEquals(1_000, logs.get(logs.size()-1).getPoints());
        assertEquals("신용카드", logs.get(logs.size()-1).getPaymentMethod());
    }

    @Test
    @DisplayName("계좌이체: 5,000원 → 500P 충전")
    void bank_transfer_payment_charges_points() {
        PaymentStrategy transfer = new BankTransferPaymentStrategy();

        boolean ok = pointService.chargePoints(reader, 5_000, transfer);

        assertTrue(ok);
        assertEquals(500, reader.getPoints());
    }

    @Test
    @DisplayName("허용되지 않은 금액은 실패해야 한다 (데모 정책)")
    void invalid_amount_should_fail() {
        PaymentStrategy card = new CreditCardPaymentStrategy();

        boolean ok = pointService.chargePoints(reader, 3333, card);

        assertFalse(ok, "정해진 금액 이외 입력은 거부되어야 함(10원=1P 환산 정책/유효 금액 제약).");
    }
}