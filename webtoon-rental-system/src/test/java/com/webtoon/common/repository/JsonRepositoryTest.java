package com.webtoon.common.repository;

import org.junit.jupiter.api.*;
import java.io.File;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonRepository 테스트
 */
class JsonRepositoryTest {

    private TestEntityRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TestEntityRepository();
        // 테스트 파일 삭제
        File file = new File("src/main/resources/data/test_entities.json");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("엔티티 저장 및 조회")
    void testSaveAndFindById() {
        // Given
        TestEntity entity = new TestEntity("Test Name", LocalDateTime.now());

        // When
        repository.save(entity);
        TestEntity found = repository.findById(entity.getId()).orElse(null);

        // Then
        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("Test Name", found.getName());
    }

    @Test
    @DisplayName("전체 엔티티 조회")
    void testFindAll() {
        // Given
        repository.save(new TestEntity("Entity 1", LocalDateTime.now()));
        repository.save(new TestEntity("Entity 2", LocalDateTime.now()));

        // When
        var all = repository.findAll();

        // Then
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("엔티티 업데이트")
    void testUpdate() {
        // Given
        TestEntity entity = new TestEntity("Original", LocalDateTime.now());
        repository.save(entity);

        // When
        entity.setName("Updated");
        repository.update(entity);

        // Then
        TestEntity updated = repository.findById(entity.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("Updated", updated.getName());
    }

    @Test
    @DisplayName("엔티티 삭제")
    void testDelete() {
        // Given
        TestEntity entity = new TestEntity("To Delete", LocalDateTime.now());
        repository.save(entity);

        // When
        repository.deleteById(entity.getId());

        // Then
        assertFalse(repository.findById(entity.getId()).isPresent());
    }

    @Test
    @DisplayName("Auto Increment ID")
    void testAutoIncrementId() {
        // Given & When
        TestEntity entity1 = new TestEntity("Entity 1", LocalDateTime.now());
        TestEntity entity2 = new TestEntity("Entity 2", LocalDateTime.now());
        repository.save(entity1);
        repository.save(entity2);

        // Then
        assertEquals(1L, entity1.getId());
        assertEquals(2L, entity2.getId());
    }

    // 테스트용 엔티티
    static class TestEntity {
        private Long id;
        private String name;
        private LocalDateTime createdAt;

        public TestEntity(String name, LocalDateTime createdAt) {
            this.name = name;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    // 테스트용 Repository
    static class TestEntityRepository extends JsonRepository<TestEntity> {
        @Override
        protected String getFileName() {
            return "test_entities";
        }

        @Override
        protected Class<TestEntity> getEntityClass() {
            return TestEntity.class;
        }

        @Override
        protected Long getId(TestEntity entity) {
            return entity.getId();
        }

        @Override
        protected void setId(TestEntity entity, Long id) {
            entity.setId(id);
        }
    }
}