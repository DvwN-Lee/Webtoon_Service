package com.webtoon.service;

import com.webtoon.domain.Author;
import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthorService 동작 검증용 테스트
 */
class AuthorServiceTest {

    private AuthorRepository authorRepository;
    private WebtoonRepository webtoonRepository;
    private EpisodeRepository episodeRepository;

    private WebtoonService webtoonService;
    private AuthorService authorService;

    private String authorId; // 매 테스트마다 생성되는 작가 ID

    @BeforeEach
    void setUp() {
        // 인메모리 구현체들 준비
        authorRepository = new InMemoryAuthorRepository();
        webtoonRepository = new InMemoryWebtoonRepository();
        episodeRepository = new InMemoryEpisodeRepository();

        // WebtoonService는 AuthorService에서 회차 업로드할 때 사용
        webtoonService = new WebtoonService(webtoonRepository, episodeRepository, new NotificationService());

        // 테스트 대상 서비스
        authorService = new AuthorService(authorRepository, webtoonRepository, webtoonService);

        // 기본 작가 한 명 저장해두고 authorId 확보
        Author author = new Author("author1234", "pass1234", "원래작가명", "원래 소개");
        authorRepository.save(author);
        authorId = String.valueOf(author.getId());
    }

    @Test
    @DisplayName("작가명과 자기소개를 수정할 수 있다")
    void updateAuthorProfile() {
        // when
        authorService.updateAuthorName(authorId, "새작가명");
        authorService.updateBio(authorId, "새로운 자기소개입니다.");

        // then
        Author updated = authorService.getProfile(authorId);
        assertEquals("새작가명", updated.getAuthorName());
        assertEquals("새로운 자기소개입니다.", updated.getBio());
    }

    @Test
    @DisplayName("작가가 새 웹툰을 생성하면 Author와 Repository에 모두 반영된다")
    void createWebtoon_forAuthor() {
        // when
        Webtoon toon = authorService.createWebtoon(
                authorId,
                "밤의 상점",
                Arrays.asList("판타지", "드라마"),
                "ONGOING",
                "밤에만 여는 가게 이야기"
        );

        // then
        assertNotNull(toon.getId());
        assertEquals("밤의 상점", toon.getTitle());
        assertEquals(authorId, toon.getAuthorId());

        // 레포지토리에 저장되었는지
        Webtoon stored = webtoonRepository.findById(toon.getId()).orElseThrow();
        assertEquals("밤의 상점", stored.getTitle());

        // Author 도메인의 webtoons 목록에도 포함되는지
        Author author = authorService.getProfile(authorId);
        assertEquals(1, author.getWebtoonCount());
        assertTrue(author.getWebtoons().stream()
                .anyMatch(w -> w.getId().equals(toon.getId())));
    }

    @Test
    @DisplayName("작가가 자신의 웹툰에 회차를 업로드하면 EpisodeRepository와 Webtoon에 반영된다")
    void uploadEpisode_increasesEpisodes() {
        // given: 웹툰 하나 생성
        Webtoon toon = authorService.createWebtoon(
                authorId,
                "밤의 상점",
                Arrays.asList("판타지"),
                "ONGOING",
                "요약"
        );
        String webtoonId = toon.getId();

        // when
        Episode ep1 = authorService.uploadEpisode(
                authorId, webtoonId,
                "1화. 첫 손님", "내용1",
                50, 100
        );
        Episode ep2 = authorService.uploadEpisode(
                authorId, webtoonId,
                "2화. 비밀 주문서", "내용2",
                50, 100
        );

        // then: 회차 번호 자동 증가
        assertEquals(1, ep1.getNumber());
        assertEquals(2, ep2.getNumber());

        // 해당 웹툰의 회차 목록
        List<Episode> episodes = episodeRepository.findByWebtoonId(webtoonId);
        assertEquals(2, episodes.size());

        // Webtoon의 episodeIds에도 2개 반영
        Webtoon stored = webtoonRepository.findById(webtoonId).orElseThrow();
        assertEquals(2, stored.getEpisodeIds().size());
    }

    @DisplayName("다른 작가가 업로드를 시도하면 예외가 발생한다")
    @Test
    void uploadEpisode_invalidAuthor() {
        // given: 원래 작가의 웹툰 생성
        Webtoon toon = authorService.createWebtoon(
                authorId,
                "밤의 상점",
                null,
                "ONGOING",
                "요약"
        );

        String webtoonId = toon.getId();

        // 실제로 존재하지 않는, 틀린 authorId를 일부러 사용
        String wrongAuthorId = "WRONG-" + authorId;

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                authorService.uploadEpisode(
                        wrongAuthorId,      // 잘못된 작가 ID
                        webtoonId,
                        "1화. 잘못된 업로드",
                        "내용",
                        50,
                        100
                )
        );
    }


    @Test
    @DisplayName("웹툰 삭제 시 Repository와 Author의 목록에서 모두 제거된다")
    void deleteWebtoon_removesFromAll() {
        // given
        Webtoon t1 = authorService.createWebtoon(authorId, "밤의 상점", null, "ONGOING", "요약1");
        Webtoon t2 = authorService.createWebtoon(authorId, "낮의 카페", null, "ONGOING", "요약2");

        Author before = authorService.getProfile(authorId);
        assertEquals(2, before.getWebtoonCount());

        // when
        authorService.deleteWebtoon(authorId, t1.getId());

        // then: 레포지토리
        assertTrue(webtoonRepository.findById(t1.getId()).isEmpty());
        assertTrue(webtoonRepository.findById(t2.getId()).isPresent());

        // Author 도메인
        Author after = authorService.getProfile(authorId);
        assertEquals(1, after.getWebtoonCount());
        assertFalse(after.getWebtoons().stream()
                .anyMatch(w -> w.getId().equals(t1.getId())));
    }

    @Test
    @DisplayName("홈 화면용 작품 목록은 인기순(popularity 내림차순)으로 정렬된다")
    void getHomeScreen_sortedByPopularity() {
        // given
        Webtoon t1 = authorService.createWebtoon(authorId, "A작품", null, "ONGOING", "요약A");
        Webtoon t2 = authorService.createWebtoon(authorId, "B작품", null, "ONGOING", "요약B");
        Webtoon t3 = authorService.createWebtoon(authorId, "C작품", null, "ONGOING", "요약C");

        // 인기 수치 조정
        t1.increasePopularity(5);   // 중간
        t2.increasePopularity(10);  // 가장 인기 많음
        t3.increasePopularity(1);   // 가장 적음

        webtoonRepository.save(t1);
        webtoonRepository.save(t2);
        webtoonRepository.save(t3);

        // when
        List<Webtoon> homeList = authorService.getHomeScreen(authorId);

        // then: popularity 내림차순 B(10) -> A(5) -> C(1)
        assertEquals(3, homeList.size());
        assertEquals(t2.getId(), homeList.get(0).getId());
        assertEquals(t1.getId(), homeList.get(1).getId());
        assertEquals(t3.getId(), homeList.get(2).getId());
    }
}
