package com.webtoon.repository;

import com.webtoon.common.repository.JsonRepository;
import com.webtoon.domain.Webtoon;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonWebtoonRepository extends JsonRepository<Webtoon> implements WebtoonRepository {

    @Override
    protected String getFileName() {
        return "webtoons";
    }

    @Override
    protected Class<Webtoon> getEntityClass() {
        return Webtoon.class;
    }

    @Override
    protected Long getId(Webtoon entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Webtoon entity, Long id) {
        entity.setId(id);
    }

    public List<Webtoon> findByAuthorId(Long authorId) {
        return findAll().stream()
                .filter(w -> w.getAuthorId().equals(authorId))
                .collect(Collectors.toList());
    }

    public List<Webtoon> searchByTitle(String keyword) {
        return findAll().stream()
                .filter(w -> w.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
}
