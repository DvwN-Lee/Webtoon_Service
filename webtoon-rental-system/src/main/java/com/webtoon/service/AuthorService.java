package com.webtoon.service;

import com.webtoon.domain.Author;
import com.webtoon.domain.Episode;
import com.webtoon.domain.Webtoon;
import com.webtoon.repository.AuthorRepository;
import com.webtoon.repository.WebtoonRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 작가(Author) 관련 유스케이스를 담당하는 서비스
 *
 * - 프로필 조회/수정
 * - 작가의 웹툰 생성/수정/삭제
 * - 작가가 자신의 작품에 회차 업로드
 * - 홈 화면용 작품 목록 조회
 */
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final WebtoonRepository webtoonRepository;
    private final WebtoonService webtoonService;

    public AuthorService(AuthorRepository authorRepository,
                         WebtoonRepository webtoonRepository,
                         WebtoonService webtoonService) {
        this.authorRepository = authorRepository;
        this.webtoonRepository = webtoonRepository;
        this.webtoonService = webtoonService;
    }

    // ================= 프로필 관련 =================

    /** 작가 프로필 조회 */
    public Author getProfile(String authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다: " + authorId));
    }

    /** 작가명 변경 */
    public void updateAuthorName(String authorId, String newName) {
        Author author = getProfile(authorId);
        author.updateAuthorName(newName);
        authorRepository.save(author);
    }

    /** 자기소개 변경 */
    public void updateBio(String authorId, String newBio) {
        Author author = getProfile(authorId);
        author.updateBio(newBio);
        authorRepository.save(author);
    }

    // ================= 웹툰 생성/수정/삭제 =================

    /**
     * 작가가 새 웹툰을 생성한다.
     * - Webtoon 도메인 인스턴스를 만들고 저장한 뒤,
     *   Author.webtoons 목록에도 추가한다.
     */
    public Webtoon createWebtoon(String authorId,
                                 String title,
                                 List<String> genres,
                                 String status,
                                 String summary) {

        Author author = getProfile(authorId);

        // 🔥 UUID → Long 변환: Long.parseLong(...) 불가, 대신 Math.abs(hash)
        Long webtoonId = Math.abs(UUID.randomUUID().getMostSignificantBits());

        Webtoon webtoon = new Webtoon(
                webtoonId,
                title,
                authorId,
                genres,
                status,
                summary
        );

        webtoonRepository.save(webtoon);

        // 작가 도메인에 연재 목록 반영
        author.createWebtoon(webtoon);
        authorRepository.save(author);

        return webtoon;
    }

    /**
     * 작가가 자신의 웹툰에 회차를 업로드한다.
     * - 실제 회차 번호 증가/저장은 WebtoonService.publishEpisode에 위임.
     */
    public Episode uploadEpisode(String authorId,
                                 Long webtoonId,
                                 String title,
                                 String content,
                                 Integer rentPrice,
                                 Integer buyPrice) {

        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));

        // 본인 작품인지 검증
        if (!authorId.equals(webtoon.getAuthorId())) {
            throw new IllegalArgumentException("해당 웹툰은 이 작가의 작품이 아닙니다.");
        }

        return webtoonService.publishEpisode(webtoonId, title, content, rentPrice, buyPrice);
    }

    /**
     * 웹툰 기본 정보 수정 (제목/장르/상태/요약)
     */
    public void updateWebtoon(String authorId,
                              Long webtoonId,
                              String newTitle,
                              List<String> newGenres,
                              String newStatus,
                              String newSummary) {

        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));

        if (!authorId.equals(webtoon.getAuthorId())) {
            throw new IllegalArgumentException("해당 웹툰은 이 작가의 작품이 아닙니다.");
        }

        if (newTitle != null) {
            webtoon.setTitle(newTitle);
        }
        if (newGenres != null) {
            webtoon.setGenres(newGenres);
        }
        if (newStatus != null) {
            webtoon.setStatus(newStatus);
        }
        if (newSummary != null) {
            webtoon.setSummary(newSummary);
        }

        webtoonRepository.save(webtoon);
    }

    /**
     * 작가의 웹툰 삭제
     * - WebtoonRepository에서 제거
     * - Author.webtoons 목록에서도 제거
     */
    public void deleteWebtoon(String authorId, Long webtoonId) {
        Author author = getProfile(authorId);

        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다: " + webtoonId));

        if (!authorId.equals(webtoon.getAuthorId())) {
            throw new IllegalArgumentException("해당 웹툰은 이 작가의 작품이 아닙니다.");
        }

        webtoonRepository.deleteById(webtoonId);
        author.removeWebtoon(webtoonId);
        authorRepository.save(author);
    }

    // ================= 홈 화면용 조회 =================

    /**
     * 작가 홈 화면용 웹툰 목록 조회
     * - 기본적으로 작가의 모든 웹툰을 인기순(popularity 내림차순)으로 정렬해서 반환
     */
    public List<Webtoon> getHomeScreen(String authorId) {
        List<Webtoon> webtoons = webtoonRepository.findByAuthorId(authorId);
        return webtoons.stream()
                .sorted(Comparator.comparingInt(Webtoon::getPopularity).reversed())
                .collect(Collectors.toList());
    }
}
