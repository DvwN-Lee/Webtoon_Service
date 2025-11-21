//package com.webtoon.repository;
//
//import com.webtoon.domain.Webtoon;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
///**
// * 메모리 기반 웹툰 저장소 구현
// */
//public class InMemoryWebtoonRepository implements WebtoonRepository {
//
//    // webtoonId -> Webtoon
//    private final Map<Long, Webtoon> store = new ConcurrentHashMap<>();
//
//    @Override
//    public Webtoon save(Webtoon webtoon) {
//        store.put(webtoon.getId(), webtoon);
//        return webtoon;
//    }
//
//    @Override
//    public Optional<Webtoon> findById(Long webtoonId) {
//        return Optional.ofNullable(store.get(webtoonId));
//    }
//
//    @Override
//    public List<Webtoon> findByAuthorId(String authorId) {
//        return store.values().stream()
//                .filter(w -> Objects.equals(w.getAuthorId(), authorId))
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<Webtoon> findAll() {
//        return new ArrayList<>(store.values());
//    }
//
//    @Override
//    public void deleteById(String webtoonId) {
//        store.remove(webtoonId);
//    }
//
//    @Override
//    public List<Webtoon> searchByTitle(String keyword) {
//        if (keyword == null || keyword.isBlank()) {
//            return findAll();
//        }
//        String lower = keyword.toLowerCase();
//        return store.values().stream()
//                .filter(w -> w.getTitle() != null &&
//                        w.getTitle().toLowerCase().contains(lower))
//                .collect(Collectors.toList());
//    }
//}

package com.webtoon.repository;

import com.webtoon.domain.Webtoon;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 메모리 기반 웹툰 저장소 구현
 */
public class InMemoryWebtoonRepository implements WebtoonRepository {

    // webtoonId(Long) -> Webtoon
    private final Map<Long, Webtoon> store = new ConcurrentHashMap<>();

    @Override
    public Webtoon save(Webtoon webtoon) {
        Long id = webtoon.getId();
        if (id == null) {
            throw new IllegalStateException("Webtoon ID가 null입니다. 저장 전에 ID를 설정해야 합니다.");
        }
        store.put(id, webtoon);
        return webtoon;
    }

    @Override
    public Optional<Webtoon> findById(Long webtoonId) {
        return Optional.ofNullable(store.get(webtoonId));
    }

    @Override
    public List<Webtoon> findByAuthorId(String authorId) {
        // 작가의 authorId는 String 그대로 유지 가능
        return store.values().stream()
                .filter(w -> Objects.equals(w.getAuthorId(), authorId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Webtoon> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long webtoonId) {
        store.remove(webtoonId);
    }

    @Override
    public List<Webtoon> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String lower = keyword.toLowerCase();
        return store.values().stream()
                .filter(w -> w.getTitle() != null &&
                        w.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }
}
