package com.webtoon.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** FR-PAYMENT-01~05: 충전 금액/수단 선택, 결제 처리, 충전 내역 조회 대응 */
public class PaymentHistory {

    // 선택: 문자열 쓰되 오타 방지를 위한 상수 제공
    public static final String METHOD_CREDIT_CARD   = "신용카드";
    public static final String METHOD_BANK_TRANSFER = "계좌이체";

    private Long id;
    private Long readerId;
    private int amount;                 // 결제 금액(원)
    private int points;                 // 충전/적립된 포인트
    private String paymentMethod;       // 예: "CREDIT_CARD", "BANK_TRANSFER"
    private LocalDateTime createdAt;    // 기록 생성 시각

    protected PaymentHistory() {}

    public PaymentHistory(Long id,
                          Long readerId,
                          int amount,
                          int points,
                          String paymentMethod,
                          LocalDateTime createdAt) {
        if (amount < 0 || points < 0) throw new IllegalArgumentException("amount/points must be >= 0");
        if (paymentMethod == null || paymentMethod.isBlank()) throw new IllegalArgumentException("paymentMethod required");

        this.id = id;
        this.readerId = readerId;
        this.amount = amount;
        this.points = points;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
    }

    public static PaymentHistory ofNow(Long id, Long readerId, int amount, int points, String paymentMethod) {
        return new PaymentHistory(id, readerId, amount, points, paymentMethod, LocalDateTime.now());
    }

    // Getters
    public Long getId() { return id; }
    public Long getReaderId() { return readerId; }
    public int getAmount() { return amount; }
    public int getPoints() { return points; }
    public String getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) {
        this.id = id;
    }

    // equals/hashCode: id가 있으면 id 기준, 없으면 주요 필드로
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentHistory)) return false;
        PaymentHistory p = (PaymentHistory) o;
        return Objects.equals(id, p.id);
    }
    @Override public int hashCode() {
        return id != null ? id.hashCode()
                : Objects.hash(readerId, amount, points, paymentMethod, createdAt);
    }

    @Override
    // PaymentHistory 객체를 사람이 보기 쉽게 문자열로 바꿔주는 역할. (클래스 이름 + 해시코드(주소값).
    // → “도대체 amount가 얼마인지, 언제 결제한 건지” 전혀 알 수 없음.)
    public String toString() {
        return "PaymentHistory{" +
                "id=" + id +
                ", readerId=" + readerId +
                ", amount=" + amount +
                ", points=" + points +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}