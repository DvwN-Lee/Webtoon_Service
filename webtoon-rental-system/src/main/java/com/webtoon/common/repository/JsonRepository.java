package com.webtoon.common.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.webtoon.common.util.LocalDateTimeAdapter;
import com.webtoon.domain.User;


import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import com.webtoon.common.util.UserTypeAdapter;

/**
 * JSON 파일 기반 Repository 추상 클래스
 * 모든 Repository가 이 클래스를 상속받아 CRUD 기능을 사용
 *
 * @param <T> 엔티티 타입
 */
public abstract class JsonRepository<T> {

    // Gson 인스턴스 (LocalDateTime 어댑터 포함)
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(User.class, new UserTypeAdapter())
            .create();

    // 동시성 제어를 위한 ReadWriteLock
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 추상 메서드 - 각 구현체가 정의해야 함
    /**
     * JSON 파일명 반환 (확장자 제외)
     * 예: "users", "webtoons"
     */
    protected abstract String getFileName();

    /**
     * 엔티티 클래스 타입 반환
     */
    protected abstract Class<T> getEntityClass();

    /**
     * 엔티티의 ID 반환
     */
    protected abstract Long getId(T entity);

    /**
     * 엔티티에 ID 설정
     */
    protected abstract void setId(T entity, Long id);

    /**
     * 파일 경로 반환
     * src/main/resources/data/{fileName}.json
     */
    private String getFilePath() {
        return "src/main/resources/data/" + getFileName() + ".json";
    }

    /**
     * 엔티티 저장 (신규 또는 업데이트)
     * ID가 null이면 자동 생성, 존재하면 업데이트
     */
    public void save(T entity) {
        lock.writeLock().lock();
        try {
            List<T> entities = loadFromFile();

            Long id = getId(entity);
            if (id == null) {
                // 신규 - ID 자동 생성
                Long nextId = getNextId();
                setId(entity, nextId);
                entities.add(entity);
            } else {
                // 업데이트 - 기존 엔티티 교체
                entities.removeIf(e -> getId(e).equals(id));
                entities.add(entity);
            }

            saveToFile(entities);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * ID로 엔티티 조회
     */
    public T findById(Long id) {
        lock.readLock().lock();
        try {
            return loadFromFile().stream()
                    .filter(e -> getId(e).equals(id))
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 전체 엔티티 조회
     */
    public List<T> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(loadFromFile());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 엔티티 업데이트
     */
    public void update(T entity) {
        save(entity); // save()가 업데이트 로직 포함
    }

    /**
     * ID로 엔티티 삭제
     */
    public void delete(Long id) {
        lock.writeLock().lock();
        try {
            List<T> entities = loadFromFile();
            entities.removeIf(e -> getId(e).equals(id));
            saveToFile(entities);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 파일에서 엔티티 로드
     */
    private List<T> loadFromFile() {
        Path path = Paths.get(getFilePath());

        // 파일이 없으면 빈 리스트 반환
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(path);
            Type listType = TypeToken.getParameterized(List.class, getEntityClass()).getType();
            List<T> entities = GSON.fromJson(json, listType);
            return entities != null ? entities : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 읽기 실패: " + getFilePath(), e);
        }
    }

    /**
     * 파일에 엔티티 저장
     */
    private void saveToFile(List<T> entities) {
        Path path = Paths.get(getFilePath());

        try {
            // 디렉토리 생성 (없을 경우)
            Files.createDirectories(path.getParent());

            String json = GSON.toJson(entities);
            Files.writeString(path, json);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 쓰기 실패: " + getFilePath(), e);
        }
    }

    /**
     * 다음 ID 생성 (Auto Increment)
     */
    private Long getNextId() {
        List<T> all = loadFromFile();
        return all.stream()
                .map(this::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }
}