package com.webtoon.pattern;

/**
 * 결제 수단을 전략으로 추상화.
 * 데모 규격: processPayment는 항상 true를 반환(성공)하도록 구현체에서 처리.
 */
public interface PaymentStrategy {
    /**
     * @param amount 실제 결제 금액(원)
     * @return 결제 성공 여부
     */
    boolean processPayment(int amount);

    /**
     * 예: "신용카드", "계좌이체"
     */
    String getPaymentMethodName();
}
