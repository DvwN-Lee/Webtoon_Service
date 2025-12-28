package com.webtoon.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** FR-PURCHASE-01/02/03: 회차 구매, 목록 조회, 대여→구매 전환(차액 결제) 대응 */
public class Purchase {
    private Long id;
    private Long readerId;
    private Long episodeId;

    private int pricePaid;                 // 보통 100P, 전환 시 차액(50P)
    private LocalDateTime purchasedAt;

    protected Purchase() {}

    public Purchase(Long id, Long readerId, Long episodeId, int pricePaid, LocalDateTime purchasedAt) {
        this.id = id;
        this.readerId = readerId;
        this.episodeId = episodeId;
        this.pricePaid = pricePaid;
        this.purchasedAt = purchasedAt;
    }

    public static Purchase ofNow(Long id, Long readerId, Long episodeId, int pricePaid) {
        return new Purchase(id, readerId, episodeId, pricePaid, LocalDateTime.now());
    }

    // Getter
    public Long getId() { return id; }
    public Long getReaderId() { return readerId; }
    public Long getEpisodeId() { return episodeId; }
    public int getPricePaid() { return pricePaid; }
    public LocalDateTime getPurchasedAt() { return purchasedAt; }

    public void setId(Long id) {
        this.id = id;
    }

    // 동등성: 영구 소장 특성상 (reader, episode) 조합을 유일로 본다
    @Override public boolean equals(Object o) {
        if (!(o instanceof Purchase)) return false;
        Purchase p = (Purchase) o;
        return Objects.equals(readerId, p.readerId) && Objects.equals(episodeId, p.episodeId);
    }
    @Override public int hashCode() { return Objects.hash(readerId, episodeId); }

    @Override
    public String toString() {
        return "Purchase{" +
                "id=" + id +
                ", readerId=" + readerId +
                ", episodeId=" + episodeId +
                ", purchasedAt=" + purchasedAt +
                '}';
    }
}
