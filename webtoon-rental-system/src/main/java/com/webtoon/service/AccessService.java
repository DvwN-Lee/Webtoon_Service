package com.webtoon.service;

import com.team.webtoon.domain.Reader;
import com.team.webtoon.domain.Episode;
import com.team.webtoon.domain.Rental;
import com.team.webtoon.domain.Purchase;
import com.team.webtoon.pattern.AccessStrategy;
import com.team.webtoon.repository.RentalRepository;
import com.team.webtoon.repository.PurchaseRepository;

import java.time.Clock;
// LocalDateTime.now() 대신 사용하는 ‘시간 공급자’ 역할
import java.time.LocalDateTime;
import java.util.List;

public class AccessService {

    private final RentalRepository rentalRepository;
    private final PurchaseRepository purchaseRepository;
    private final Clock clock;

    // 테스트 용이성(만료 계산 등)을 위해 Clock 주입 가능
    public AccessService(RentalRepository rentalRepository,
                         PurchaseRepository purchaseRepository,
                         Clock clock) {
        this.rentalRepository = rentalRepository;
        this.purchaseRepository = purchaseRepository;
        this.clock = clock;
    }

    /**
     * FR-EPISODE-02 (회차 열람), FR-RENTAL-01 (회차 대여), FR-PURCHASE-01 (회차 구매)
     * 전략 패턴으로 대여/구매 수행을 위임.
     */
    public boolean grantAccess(Reader reader, Episode episode, AccessStrategy strategy) {
        // 이미 접근 가능하면 추가 과금 없이 true
        if (canAccess(reader, episode)) return true;
        // 실제 대여/구매 처리는 전략에게 위임
        return strategy.execute(reader, episode);
    }

    /**
     * FR-EPISODE-02: 열람 가능 여부 판정
     * 전략 기반 접근 가능 여부(상태 확인만).
     * ex) 화면에서 '내용 보기' 버튼 활성화 판단 등에 사용.
     */
    public boolean canAccess(Reader reader, Episode episode, AccessStrategy strategy) {
        return strategy.canAccess(reader, episode);
    }

    /**
     * 전략 미지정 일반 판단:
     * - 이미 구매했거나
     * - 유효한 대여가 있으면 true
     */
    public boolean canAccess(Reader reader, Episode episode) {
        Long readerId = reader.getId();
        Long episodeId = episode.getId();

        if (isPurchased(readerId, episodeId)) return true;

        // 유효 대여가 있는지 검사
        return rentalRepository.findByReaderId(readerId).stream()
                .filter(r -> r.getEpisodeId().equals(episodeId))
                .anyMatch(r -> !r.isExpired(clock));
    }

    /**
     * 라이브러리 노출용
     * FR-RENTAL-03: 대여 중인 작품 조회
     */
    public List<Rental> getRentals(Long readerId) {
        return rentalRepository.findByReaderId(readerId).stream()
                .filter(r -> !r.isExpired(clock))      // 서비스에서 필터
                .toList();
    }

    /**
     * FR-PURCHASE-02: 구매한 작품 조회
     */
    public List<Purchase> getPurchases(Long readerId) {
        return purchaseRepository.findByReaderId(readerId);
    }

    /**
     * FR-PURCHASE-03: 대여 → 구매 전환
     * - 차액(구매가 - 대여가)을 추가 차감
     * - 구매 엔트리 생성
     * - 대여 기록은 보존, 구매 기록 추가
     */
    public boolean convertRentalToPurchase(Reader reader, Episode episode, int rentalPrice, int purchasePrice) {
        int diff = purchasePrice - rentalPrice;
        if (diff <= 0) {
            // 구매가 더 싸거나 같으면 추가 차감 없이 바로 구매 처리
            Purchase purchase = new Purchase(null, reader.getId(), episode.getId(), LocalDateTime.now(clock));
            purchaseRepository.save(purchase);
            return true;
        }

        if (!reader.usePoints(diff)) return false; // 포인트 부족

        Purchase purchase = new Purchase(null, reader.getId(), episode.getId(), LocalDateTime.now(clock));
        purchaseRepository.save(purchase);
        return true;
    }

    // ========= 내부 유틸 =========

    private boolean isPurchased(Long readerId, Long episodeId) {
        return purchaseRepository.findByReaderId(readerId).stream()
                .anyMatch(p -> p.getEpisodeId().equals(episodeId));
    }
