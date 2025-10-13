package com.webtoon.repository;

import com.team.webtoon.domain.Rental;
import com.team.webtoon.common.repository.JsonRepository;
import java.util.List;
import java.util.stream.Collectors;

public class RentalRepository extends JsonRepository<Rental> {

    @Override
    protected String getFilePath() {
        return "src/main/resources/data/rentals.json";
    }

    @Override
    protected Class<Rental> getEntityClass() {
        return Rental.class;
    }

    // 특정 독자의 전체 대여 목록
    public List<Rental> findByReaderId(Long readerId) {
        return findAll().stream()
                .filter(r -> r.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }

    // 만료되지 않은 대여 목록 (AccessService가 사용할 수 있음)
    public List<Rental> findActiveRentals(Long readerId) {
        return findByReaderId(readerId).stream()
                .filter(r -> !r.isExpired())
                .collect(Collectors.toList());
    }
}