package com.webtoon.repository;

import com.webtoon.common.repository.JsonRepository;
import com.webtoon.domain.Episode;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonEpisodeRepository extends JsonRepository<Episode> implements EpisodeRepository {

    @Override
    protected String getFileName() {
        return "episodes";
    }

    @Override
    protected Class<Episode> getEntityClass() {
        return Episode.class;
    }

    @Override
    protected Long getId(Episode entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Episode entity, Long id) {
        entity.setId(id);
    }

    public List<Episode> findByWebtoonId(Long webtoonId) {
        return findAll().stream()
                .filter(e -> e.getWebtoonId().equals(webtoonId))
                .sorted(Comparator.comparingInt(Episode::getNumber))
                .collect(Collectors.toList());
    }

    public Optional<Episode> findLatestByWebtoonId(Long webtoonId) {
        return findAll().stream()
                .filter(e -> e.getWebtoonId().equals(webtoonId))
                .max(Comparator.comparingInt(Episode::getNumber));
    }
}
