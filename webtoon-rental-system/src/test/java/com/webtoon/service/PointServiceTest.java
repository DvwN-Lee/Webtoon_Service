package com.webtoon.service;

import com.webtoon.domain.PaymentHistory;
import com.webtoon.pattern.CreditCardPaymentStrategy;
import com.webtoon.pattern.BankTransferPaymentStrategy;
import com.webtoon.pattern.PaymentStrategy;
import com.webtoon.repository.PaymentHistoryRepository;
import com.webtoon.repository.ReaderRepository;
import com.webtoon.domain.Reader;
import com.webtoon.service.PointService;
import com.webtoon.common.repository.InMemoryPaymentHistoryRepository;

import java.time.Clock;
import java.time.ZoneId;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointServiceTest {

    private PaymentHistoryRepository paymentRepo;
    private ReaderRepository readerRepo;
    private PointService pointService;
    private Reader reader;
    private Clock baseClock;

    @BeforeEach
    void setUp() {
        paymentRepo = new InMemoryPaymentHistoryRepository();
        readerRepo = new ReaderRepository();

        // PointService는 repo + readerRepo + clock 세 개 필요
        baseClock = Clock.systemDefaultZone();
        pointService = new PointService(paymentRepo, readerRepo, baseClock);

        // Reader 실제 생성자에 맞춰 생성
        reader = new Reader("reader1", "1234", "닉");

        // ReaderRepository에 저장하여 ID 생성
        reader = readerRepo.save(reader);

        // 기본 포인트가 1000이므로 → 0P에서 테스트를 하고 싶다면 아래처럼 조정
        reader.setPoints(0);
        readerRepo.update(reader);   // 포인트 변경사항 저장
    }

    @Test
    @DisplayName("신용카드 결제: 10,000원 → 1,000P 충전")
    void creditCard_payment_charges_points() {
        PaymentStrategy card = new CreditCardPaymentStrategy();

        boolean ok = pointService.chargePoints(reader, 10_000, card);

        assertTrue(ok);
        assertEquals(1_000, reader.getPoints()); // 0P → +1000P

        List<PaymentHistory> logs = pointService.getPaymentHistory(reader.getId());
        assertFalse(logs.isEmpty());

        PaymentHistory last = logs.get(logs.size() - 1);
        assertEquals(10_000, last.getAmount());
        assertEquals(1_000, last.getPoints());
        assertEquals("신용카드", last.getPaymentMethod());
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

        assertFalse(ok, "정해진 금액 이외 입력은 거부되어야 함");
        assertEquals(0, reader.getPoints(), "포인트 변화 없어야 함");
    }
}
