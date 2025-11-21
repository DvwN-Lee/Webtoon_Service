package com.webtoon.repository;

import com.webtoon.common.repository.JsonRepository;
import com.webtoon.domain.Rental;

import java.util.List;
import java.util.stream.Collectors;

public class RentalRepository extends JsonRepository<Rental> {

    @Override
    protected String getFileName() {
        return "rentals";
    }

    @Override
    protected Class<Rental> getEntityClass() {
        return Rental.class;
    }

    @Override
    protected Long getId(Rental entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Rental entity, Long id) {
        entity.setId(id);
    }

    // 특정 독자의 전체 대여 목록
    public List<Rental> findByReaderId(Long readerId) {
        return findAll().stream()
                .filter(r -> r.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }

    // 만료되지 않은 대여 목록
    public List<Rental> findActiveRentals(Long readerId) {
        return findByReaderId(readerId).stream()
                .filter(r -> !r.isExpired())
                .collect(Collectors.toList());
    }
}