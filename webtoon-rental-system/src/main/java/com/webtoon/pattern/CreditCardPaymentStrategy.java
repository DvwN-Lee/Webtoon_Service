package com.webtoon.pattern;

/**
 * 데모에서는 성공으로 가정.
 * 실제라면 카드번호/유효기간/CVC/3D-Secure 등 검증 로직이 들어감.
 */
public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean processPayment(int amount) {
        // TODO: 실제 결제 모듈 연동 대신 데모에선 항상 성공
        return true;
    }

    @Override
    public String getPaymentMethodName() {
        return "신용카드";
    }
}
