package com.webtoon.service;

import com.webtoon.domain.Author;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebtoonService 조회/검색/정렬 기능 테스트
 */
class WebtoonServiceTest {

    private WebtoonRepository webtoonRepository;
    private EpisodeRepository episodeRepository;
    private WebtoonService webtoonService;

    private String authorId1;
    private String authorId2;

    @BeforeEach
    void setUp() {
        webtoonRepository = new InMemoryWebtoonRepository();
        episodeRepository = new InMemoryEpisodeRepository();
        NotificationService notificationService = new NotificationService();
        webtoonService = new WebtoonService(webtoonRepository, episodeRepository, notificationService);

        // 테스트용 작가 ID 준비
        authorId1 = "author-001";
        authorId2 = "author-002";
    }

    @Test
    @DisplayName("모든 웹툰 목록 조회")
    void listAllWebtoons() {
        // given
        webtoonService.createWebtoon("웹툰A", authorId1);
        webtoonService.createWebtoon("웹툰B", authorId1);
        webtoonService.createWebtoon("웹툰C", authorId2);

        // when
        List<Webtoon> allWebtoons = webtoonService.listAllWebtoons();

        // then
        assertEquals(3, allWebtoons.size());
    }

    @Test
    @DisplayName("ID로 웹툰 단건 조회")
    void getWebtoonById() {
        // given: 숫자 형태의 ID로 웹툰 직접 생성
        Webtoon webtoon = new Webtoon("100", "테스트 웹툰", authorId1,
                Arrays.asList("판타지"), "ONGOING", "요약");
        webtoonRepository.save(webtoon);

        // when
        Webtoon found = webtoonService.getWebtoon(100L);

        // then
        assertNotNull(found);
        assertEquals("100", found.getId());
        assertEquals("테스트 웹툰", found.getTitle());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
    void getWebtoonById_notFound() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            webtoonService.getWebtoon(99999L);
        });
    }

    @Test
    @DisplayName("제목 키워드로 웹툰 검색")
    void searchByTitle() {
        // given
        webtoonService.createWebtoon("밤의 상점", authorId1);
        webtoonService.createWebtoon("낮의 카페", authorId1);
        webtoonService.createWebtoon("밤하늘의 별", authorId2);

        // when: "밤" 키워드로 검색
        List<Webtoon> results = webtoonService.searchByTitle("밤");

        // then: "밤의 상점", "밤하늘의 별" 2개 검색됨
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(w -> w.getTitle().equals("밤의 상점")));
        assertTrue(results.stream().anyMatch(w -> w.getTitle().equals("밤하늘의 별")));
    }

    @Test
    @DisplayName("작가 ID로 웹툰 검색")
    void searchByAuthor() {
        // given
        webtoonService.createWebtoon("웹툰A", authorId1);
        webtoonService.createWebtoon("웹툰B", authorId1);
        webtoonService.createWebtoon("웹툰C", authorId2);

        // when
        List<Webtoon> author1Webtoons = webtoonService.searchByAuthor(authorId1);
        List<Webtoon> author2Webtoons = webtoonService.searchByAuthor(authorId2);

        // then
        assertEquals(2, author1Webtoons.size());
        assertEquals(1, author2Webtoons.size());
        assertTrue(author1Webtoons.stream().allMatch(w -> w.getAuthorId().equals(authorId1)));
        assertTrue(author2Webtoons.stream().allMatch(w -> w.getAuthorId().equals(authorId2)));
    }

    @Test
    @DisplayName("인기순(popularity) 내림차순 정렬")
    void sortByPopularity() {
        // given
        Webtoon w1 = webtoonService.createWebtoon("웹툰A", authorId1);
        Webtoon w2 = webtoonService.createWebtoon("웹툰B", authorId1);
        Webtoon w3 = webtoonService.createWebtoon("웹툰C", authorId2);

        // 인기도 조정
        w1.increasePopularity(10); // 가장 인기
        w2.increasePopularity(5);
        w3.increasePopularity(15); // 최고 인기

        webtoonRepository.save(w1);
        webtoonRepository.save(w2);
        webtoonRepository.save(w3);

        // when
        List<Webtoon> sorted = webtoonService.sortByPopularity();

        // then: 15 -> 10 -> 5 순서
        assertEquals(3, sorted.size());
        assertEquals(15, sorted.get(0).getPopularity());
        assertEquals(10, sorted.get(1).getPopularity());
        assertEquals(5, sorted.get(2).getPopularity());
    }

    @Test
    @DisplayName("제목 오름차순 정렬")
    void sortByTitle() {
        // given
        webtoonService.createWebtoon("다람쥐", authorId1);
        webtoonService.createWebtoon("고양이", authorId1);
        webtoonService.createWebtoon("토끼", authorId2);

        // when
        List<Webtoon> sorted = webtoonService.sortByTitle();

        // then: 가나다순
        assertEquals(3, sorted.size());
        assertEquals("고양이", sorted.get(0).getTitle());
        assertEquals("다람쥐", sorted.get(1).getTitle());
        assertEquals("토끼", sorted.get(2).getTitle());
    }

    @Test
    @DisplayName("최신 업데이트순(updatedAt) 내림차순 정렬")
    void sortByLatest() throws InterruptedException {
        // given
        Webtoon w1 = webtoonService.createWebtoon("웹툰1", authorId1);
        Thread.sleep(10); // 시간 차이를 두기 위한 대기

        Webtoon w2 = webtoonService.createWebtoon("웹툰2", authorId1);
        Thread.sleep(10);

        Webtoon w3 = webtoonService.createWebtoon("웹툰3", authorId2);

        // when
        List<Webtoon> sorted = webtoonService.sortByLatest();

        // then: 최신 생성순으로 w3 -> w2 -> w1
        assertEquals(3, sorted.size());
        assertEquals(w3.getId(), sorted.get(0).getId());
        assertEquals(w2.getId(), sorted.get(1).getId());
        assertEquals(w1.getId(), sorted.get(2).getId());
    }

    @Test
    @DisplayName("팔로우 기능 - 여러 독자가 팔로우")
    void followWebtoon_multipleReaders() {
        // given
        Webtoon webtoon = webtoonService.createWebtoon("인기 웹툰", authorId1);

        // when
        webtoonService.followWebtoon(webtoon.getId(), "reader-001");
        webtoonService.followWebtoon(webtoon.getId(), "reader-002");
        webtoonService.followWebtoon(webtoon.getId(), "reader-003");

        // then
        Webtoon updated = webtoonRepository.findById(webtoon.getId()).orElseThrow();
        assertEquals(3, updated.getFollowerUserIds().size());
        assertTrue(updated.getFollowerUserIds().contains("reader-001"));
        assertTrue(updated.getFollowerUserIds().contains("reader-002"));
        assertTrue(updated.getFollowerUserIds().contains("reader-003"));
    }
}
