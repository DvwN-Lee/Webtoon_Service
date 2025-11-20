package com.webtoon.repository;

import com.webtoon.common.repository.JsonRepository;
import com.webtoon.domain.Reader;

/**
 * Reader 전용 Repository
 * 독자 정보 JSON 파일 관리
 */
public class ReaderRepository extends JsonRepository<Reader> {

    @Override
    protected String getFileName() {
        return "readers"; // 실제 저장 파일: readers.json
    }

    @Override
    protected Class<Reader> getEntityClass() {
        return Reader.class;
    }

    @Override
    protected Long getId(Reader entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Reader entity, Long id) {
        entity.setId(id);
    }
}