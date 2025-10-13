package com.webtoon.domain;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** FR-RENTAL-01/02/03: 회차 대여, 만료 처리(데모 10분), 대여 목록 조회 대응 */
public class Rental {
    public enum Status { ACTIVE, EXPIRED }

    private static final Duration RENTAL_DURATION = Duration.ofMinutes(10); // 데모 정책

    private Long id;
    private Long readerId;
    private Long episodeId;

    private int pricePaid;                // 보통 50P
    private LocalDateTime rentedAt;       // 생성 시 now(clock)
    private LocalDateTime expiresAt;      // now(clock) + 10분
    private Status status = Status.ACTIVE;

    protected Rental() {}

    public Rental(Long id, Long readerId, Long episodeId, int pricePaid,
                  LocalDateTime rentedAt, LocalDateTime expiresAt, Clock clock) {
        this.id = id;
        this.readerId = readerId;
        this.episodeId = episodeId;
        this.pricePaid = pricePaid;
        this.rentedAt = rentedAt;
        this.expiresAt = expiresAt;
        this.status = LocalDateTime.now(clock).isAfter(expiresAt) ? Status.EXPIRED : Status.ACTIVE;
    }

    /** 팩토리: 데모 정책(대여기간 10분) 적용 */
    public static Rental ofNow(Long id, Long readerId, Long episodeId, int pricePaid, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new Rental(id, readerId, episodeId, pricePaid, now, now.plus(RENTAL_DURATION), clock);
    }

    /** 만료 검사 및 상태 갱신 (서비스에서 주기적으로/접근 시 호출) */
    public boolean refreshExpiry(Clock clock) {
        if (status == Status.EXPIRED) return false;
        if (LocalDateTime.now(clock).isAfter(expiresAt)) {
            status = Status.EXPIRED;
            return true;
        }
        return false;
    }

    /** === UML 명세 메서드들 (Clock 기반) === */
    public boolean isExpired(Clock clock) {
        refreshExpiry(clock);
        return status == Status.EXPIRED;
    }

    public Duration getRemainingTime(Clock clock) {
        refreshExpiry(clock);
        LocalDateTime now = LocalDateTime.now(clock);
        Duration d = Duration.between(now, expiresAt);
        return d.isNegative() ? Duration.ZERO : d; // 음수 방지
    }

    /** 호환용(선택): Clock 미지정 시 시스템 시계 사용 */
    public boolean isExpired() { return isExpired(Clock.systemDefaultZone()); }
    public Duration getRemainingTime() { return getRemainingTime(Clock.systemDefaultZone()); }

    public boolean isActive(Clock clock) { return !isExpired(clock); }
    public long remainingSeconds(Clock clock) { return getRemainingTime(clock).getSeconds(); }

    // Getters
    public Long getId() { return id; }
    public Long getReaderId() { return readerId; }
    public Long getEpisodeId() { return episodeId; }
    public int getPricePaid() { return pricePaid; }
    public LocalDateTime getRentedAt() { return rentedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public Status getStatus(Clock clock) { refreshExpiry(clock); return status; }

    // 동등성: 하나의 대여는 (reader, episode, rentedAt)로 유니크하다고 가정
    @Override public boolean equals(Object o) {
        if (!(o instanceof Rental)) return false;
        Rental r = (Rental) o;
        return Objects.equals(readerId, r.readerId)
                && Objects.equals(episodeId, r.episodeId)
                && Objects.equals(rentedAt, r.rentedAt);
    }
    @Override public int hashCode() { return Objects.hash(readerId, episodeId, rentedAt); }

    @Override
    public String toString() {
        return "Rental{" +
                "id=" + id +
                ", readerId=" + readerId +
                ", episodeId=" + episodeId +   // ← 오타 수정
                ", rentedAt=" + rentedAt +
                ", expiresAt=" + expiresAt +
                ", status=" + status +
                '}';
    }
}