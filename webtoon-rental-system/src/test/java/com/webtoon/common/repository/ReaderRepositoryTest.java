package com.webtoon.common.repository;

import com.webtoon.domain.Reader;
import com.webtoon.repository.ReaderRepository;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReaderRepository 단위 테스트
 * 주의: 테스트 파일 경로는 src/main/resources/data/readers.json 입니다.
 * 이 테스트는 실행 전마다 해당 파일을 삭제(초기화)합니다.
 */
class ReaderRepositoryTest {

    private ReaderRepository repository;
    private static final String DATA_FILE = "src/main/resources/data/readers.json";

    @BeforeEach
    void setUp() {
        repository = new ReaderRepository();
        // 테스트 시작 전에 JSON 파일 초기화
        File f = new File(DATA_FILE);
        if (f.exists()) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

    @Test
    @DisplayName("Reader 저장 후 ID로 조회")
    void saveAndFindById() {
        Reader r = new Reader("reader1", "pass1234", "독자A");

        repository.save(r);
        Reader found = repository.findById(r.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("reader1", found.getUsername());
        assertEquals("독자A", found.getNickname());
        assertNotNull(found.getId());
    }

    @Test
    @DisplayName("Reader 전체 조회")
    void findAll() {
        repository.save(new Reader("u1", "p1", "A"));
        repository.save(new Reader("u2", "p2", "B"));

        List<Reader> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Reader 업데이트(닉네임 변경)")
    void update() {
        Reader r = new Reader("reader2", "pass", "원래닉");
        repository.save(r);

        r.updateNickname("바뀐닉");
        repository.update(r);

        Reader updated = repository.findById(r.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals("바뀐닉", updated.getNickname());
    }

    @Test
    @DisplayName("Reader 삭제")
    void deleteById() {
        Reader r = new Reader("reader3", "pass", "삭제대상");
        repository.save(r);
        Long id = r.getId();

        repository.deleteById(id);

        assertFalse(repository.findById(id).isPresent());
    }

    @Test
    @DisplayName("팔로우 추가/취소 동작")
    void followAndUnfollow() {
        Reader r = new Reader("followUser", "pw", "팔로워");
        repository.save(r);

        // 팔로우
        r.followWebtoon(101L);
        r.followWebtoon(102L);
        repository.update(r);

        Reader afterFollow = repository.findById(r.getId()).orElse(null);
        assertNotNull(afterFollow);
        assertTrue(afterFollow.getFollowingWebtoonIds().contains(101L));
        assertTrue(afterFollow.getFollowingWebtoonIds().contains(102L));

        // 언팔로우
        afterFollow.unfollowWebtoon(101L);
        repository.update(afterFollow);

        Reader afterUnfollow = repository.findById(r.getId()).orElse(null);
        assertNotNull(afterUnfollow);
        assertFalse(afterUnfollow.getFollowingWebtoonIds().contains(101L));
        assertTrue(afterUnfollow.getFollowingWebtoonIds().contains(102L));
    }
}
