package com.webtoon.pattern;

/**
 * 데모에서는 성공으로 가정.
 * 실제라면 은행 이체 요청/응답 및 가상계좌 매칭 등 필요.
 */
public class BankTransferPaymentStrategy implements PaymentStrategy {

    @Override
    public boolean processPayment(int amount) {
        // TODO: 실제 이체 모듈 연동 대신 데모에선 항상 성공
        return true;
    }

    @Override
    public String getPaymentMethodName() {
        return "계좌이체";
    }
}
