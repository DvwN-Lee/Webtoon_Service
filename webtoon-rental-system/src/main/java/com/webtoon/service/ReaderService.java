package com.webtoon.service;

import com.webtoon.common.validation.ValidationException;
import com.webtoon.domain.Reader;
import com.webtoon.repository.ReaderRepository;

/**
 * 독자 관련 서비스 (홍승현)
 */
public class ReaderService {

    private final ReaderRepository readerRepository;

    public ReaderService() {
        this.readerRepository = new ReaderRepository();
    }

    /**
     * 닉네임 변경
     */
    public void updateNickname(Long readerId, String newNickname) {
        Reader reader = readerRepository.findById(readerId);
        if (reader == null) {
            throw new ValidationException("존재하지 않는 독자입니다.");
        }

        reader.updateNickname(newNickname);
        readerRepository.update(reader);
    }

    /**
     * 팔로우
     */
    public void followWebtoon(Long readerId, Long webtoonId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader != null) {
            reader.followWebtoon(webtoonId);
            readerRepository.update(reader);
        }
    }

    /**
     * 언팔로우
     */
    public void unfollowWebtoon(Long readerId, Long webtoonId) {
        Reader reader = readerRepository.findById(readerId);
        if (reader != null) {
            reader.unfollowWebtoon(webtoonId);
            readerRepository.update(reader);
        }
    }
}
