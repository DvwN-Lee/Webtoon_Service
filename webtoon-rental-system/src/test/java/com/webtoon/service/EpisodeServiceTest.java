package com.webtoon.service;

import com.webtoon.domain.Episode;
import com.webtoon.repository.EpisodeRepository;
import com.webtoon.repository.InMemoryEpisodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpisodeServiceTest {

    private EpisodeRepository episodeRepository;
    private EpisodeService episodeService;

    @BeforeEach
    void setUp() {
        episodeRepository = new InMemoryEpisodeRepository();
        episodeService = new EpisodeService(episodeRepository);
    }

    private Episode newEpisode(Long id, Long webtoonId, int number,
                               String title, String content, int rent, int buy) {
        return new Episode(id, webtoonId, number, title, content, rent, buy);
    }

    @Test
    @DisplayName("웹툰별 회차 조회 시 번호 오름차순으로 정렬된다")
    void findByWebtoonId_sortedByNumber() {
        // given
        Long webtoonId = 1L;
        Episode ep3 = newEpisode(null, webtoonId, 3, "3화", "내용3", 50, 100);
        Episode ep1 = newEpisode(null, webtoonId, 1, "1화", "내용1", 50, 100);
        Episode ep2 = newEpisode(null, webtoonId, 2, "2화", "내용2", 50, 100);

        episodeRepository.save(ep3);
        episodeRepository.save(ep1);
        episodeRepository.save(ep2);

        // when
        List<Episode> result = episodeService.findByWebtoonId(webtoonId);

        // then
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getNumber());
        assertEquals(2, result.get(1).getNumber());
        assertEquals(3, result.get(2).getNumber());
    }

    @Test
    @DisplayName("회차의 제목/내용/가격을 수정할 수 있다")
    void updateTitleContentPrices() {
        // given
        Long webtoonId = 1L;
        Episode ep = newEpisode(null, webtoonId, 1,
                "1화. 옛 제목", "옛 내용", 50, 100);
        Episode saved = episodeRepository.save(ep);
        Long episodeId = saved.getId();

        // when
        episodeService.updateTitle(episodeId, "1화. 새 제목");
        episodeService.updateContent(episodeId, "새 내용");
        episodeService.updatePrices(episodeId, 70, 140);

        // then
        Episode updated = episodeService.findById(episodeId);
        assertEquals("1화. 새 제목", updated.getTitle());
        assertEquals("새 내용", updated.getContent());
        assertEquals(70, updated.getRentPrice());
        assertEquals(140, updated.getBuyPrice());
    }

    @Test
    @DisplayName("회차를 삭제하면 저장소에서 조회되지 않는다")
    void deleteEpisodeById() {
        // given
        Long webtoonId = 1L;
        Episode ep = newEpisode(null, webtoonId, 1,
                "1화", "내용", 50, 100);
        Episode saved = episodeRepository.save(ep);
        Long episodeId = saved.getId();
        assertNotNull(episodeRepository.findById(episodeId).orElse(null)); // 존재 확인

        // when
        episodeService.delete(episodeId);

        // then
        assertTrue(episodeRepository.findById(episodeId).isEmpty());
        assertTrue(episodeService.findByWebtoonId(webtoonId).isEmpty());
    }

    @Test
    @DisplayName("사용자별 회차 상세 조회 시 조회수가 증가한다")
    void getEpisodeDetailForUser_incrementsViewCount() {
        // given
        Long webtoonId = 1L;
        Episode ep = newEpisode(null, webtoonId, 1,
                "1화. 테스트", "내용", 50, 100);
        Episode saved = episodeRepository.save(ep);
        Long episodeId = saved.getId();

        // 초기 조회수 확인
        assertEquals(0, ep.getViewCount());

        // when: 사용자가 회차 상세를 조회 (Reader 객체는 null로 전달 - 현재는 사용하지 않음)
        Episode result1 = episodeService.getEpisodeDetailForUser(ep, null);
        assertEquals(1, result1.getViewCount());

        // 다시 조회하면 조회수가 계속 증가
        Episode result2 = episodeService.getEpisodeDetailForUser(ep, null);
        assertEquals(2, result2.getViewCount());

        // then: 저장소에서 조회해도 조회수가 반영됨
        Episode stored = episodeService.findById(episodeId);
        assertEquals(2, stored.getViewCount());
    }

    @Test
    @DisplayName("Episode 엔티티 기반 삭제")
    void deleteEpisode_withEntity() {
        // given
        Long webtoonId = 1L;
        Episode ep = newEpisode(null, webtoonId, 1,
                "1화", "내용", 50, 100);
        Episode saved = episodeRepository.save(ep);
        Long episodeId = saved.getId();
        assertNotNull(episodeRepository.findById(episodeId).orElse(null));

        // when
        episodeService.deleteEpisode(saved);

        // then
        assertTrue(episodeRepository.findById(episodeId).isEmpty());
    }

    @Test
    @DisplayName("null Episode 엔티티로 삭제 시도 시 오류 없이 처리")
    void deleteEpisode_withNullEntity() {
        // when & then: 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> episodeService.deleteEpisode(null));
    }
}
