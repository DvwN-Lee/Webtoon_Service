package com.webtoon.pattern;

import com.webtoon.domain.Rental;
import com.webtoon.domain.Reader;
import com.webtoon.domain.Episode;
import com.webtoon.repository.RentalRepository;

import java.time.Duration;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class RentalAccessStrategy implements AccessStrategy {

    private static final Duration RENTAL_DURATION = Duration.ofMinutes(10);

    private final RentalRepository rentalRepository;

    public RentalAccessStrategy(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @Override
    public Object execute(Reader reader, Episode episode, Clock clock) {

        int price = episode.getRentalPrice();

        if (!reader.usePoints(price)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        Rental rental = new Rental(
                null,
                reader.getId(),
                episode.getId(),
                price,
                now,
                now.plus(RENTAL_DURATION),
                clock
        );

        return rental;
    }

    @Override
    public boolean canAccess(Reader reader, Episode episode, Clock clock) {

        List<Rental> rentals = rentalRepository.findByReaderId(reader.getId());

        return rentals.stream()
                .filter(r -> r.getEpisodeId().equals(episode.getId()))
                .anyMatch(r -> !r.isExpired(clock));
    }

    @Override
    public String getStrategyName() {
        return "RENTAL";
    }
}