package com.webtoon.domain;

import java.time.LocalDateTime;

/**
 * 회차 도메인 (콘솔 데모용 텍스트 콘텐츠 포함)
 */
public class Episode {

    // 식별
    private Long id;          // UUID
    private Long webtoonId;   // 소속 작품 ID

    // 기본 정보
    private int number;         // 1..N (정렬 기준)
    private String title;       // 회차 제목
    private String content;     // 콘솔로 출력할 본문 텍스트

    // 가격
    private int rentPrice;      // 기본 50P
    private int buyPrice;       // 기본 100P

    // 메타
    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== 생성자 =====
    public Episode() { }

    public Episode(Long id, Long webtoonId, int number, String title,
                   String content, Integer rentPrice, Integer buyPrice) {
        this.id = id;
        this.webtoonId = webtoonId;
        this.number = number;
        this.title = title;
        this.content = content;
        this.rentPrice = rentPrice != null ? rentPrice : 50;
        this.buyPrice = buyPrice != null ? buyPrice : 100;
        validatePrices();
    }

    // ===== 도메인 메서드 =====
    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }

    public void updatePrices(int newRentPrice, int newBuyPrice) {
        this.rentPrice = newRentPrice;
        this.buyPrice = newBuyPrice;
        validatePrices();
    }

    private void validatePrices() {
        if (rentPrice < 0 || buyPrice < 0) {
            throw new IllegalArgumentException("가격은 음수가 될 수 없습니다.");
        }
        if (buyPrice < rentPrice) {
            // 대여→구매 전환(차액 결제) 로직과 충돌하지 않도록 최소 제약
            throw new IllegalArgumentException("구매 가격은 대여 가격보다 작을 수 없습니다.");
        }
    }

    // ===== Getter / Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWebtoonId() { return webtoonId; }
    public void setWebtoonId(Long webtoonId) { this.webtoonId = webtoonId; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRentalPrice() { return rentPrice; }
    public void setRentalPrice(int rentalPrice) {
        this.rentPrice = rentalPrice;
        validatePrices();
    }

    public int getPurchasePrice() { return buyPrice; }
    public void setPurchasePrice(int purchasePrice) {
        this.buyPrice = purchasePrice;
        validatePrices();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Episode{" +
                "id='" + id + '\'' +
                ", webtoonId='" + webtoonId + '\'' +
                ", number=" + number +
                ", title='" + title + '\'' +
                ", rentPrice=" + rentPrice +
                ", buyPrice=" + buyPrice +
                ", createdAt=" + createdAt +
                '}';
    }
}