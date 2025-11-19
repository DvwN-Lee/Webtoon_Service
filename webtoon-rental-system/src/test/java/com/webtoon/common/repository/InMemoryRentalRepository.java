package com.webtoon.common.repository;

import com.webtoon.domain.Rental;
import com.webtoon.repository.RentalRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryRentalRepository extends RentalRepository {

    private final List<Rental> store = new ArrayList<>();
    private long seq = 1L;

    @Override
    public void save(Rental rental) {
        if (rental.getId() == null) {
            rental.setId(seq++);
        }
        store.removeIf(r -> r.getId().equals(rental.getId()));
        store.add(rental);
    }

    @Override
    public List<Rental> findByReaderId(Long readerId) {
        return store.stream()
                .filter(r -> r.getReaderId().equals(readerId))
                .toList();
    }

    @Override
    public List<Rental> findAll() {
        return new ArrayList<>(store);
    }
}
