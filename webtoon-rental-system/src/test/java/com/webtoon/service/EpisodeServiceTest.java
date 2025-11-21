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
        Episode ep3 = newEpisode(3L, webtoonId, 3, "3화", "내용3", 50, 100);
        Episode ep1 = newEpisode(1L, webtoonId, 1, "1화", "내용1", 50, 100);
        Episode ep2 = newEpisode(2L, webtoonId, 2, "2화", "내용2", 50, 100);

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
        Episode ep = newEpisode(1L, webtoonId, 1,
                "1화. 옛 제목", "옛 내용", 50, 100);
        episodeRepository.save(ep);

        // when
        episodeService.updateTitle(1L, "1화. 새 제목");
        episodeService.updateContent(1L, "새 내용");
        episodeService.updatePrices(1L, 70, 140);

        // then
        Episode updated = episodeService.findById(1L);
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
        Episode ep = newEpisode(1L, webtoonId, 1,
                "1화", "내용", 50, 100);
        episodeRepository.save(ep);
        assertNotNull(episodeRepository.findById(1L).orElse(null)); // 존재 확인

        // when
        episodeService.delete(1L);

        // then
        assertTrue(episodeRepository.findById(1L).isEmpty());
        assertTrue(episodeService.findByWebtoonId(webtoonId).isEmpty());
    }
}
