package com.webtoon.repository;

import com.webtoon.common.repository.JsonRepository;
import com.webtoon.domain.Author;
import com.webtoon.domain.Reader;
import com.webtoon.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User Repository
 * User(Reader/Author) 영속화 담당
 * Author와 Reader 모두 관리하며, AuthorService에서도 사용 가능
 * AuthorRepository 인터페이스를 implements하지 않지만,
 * AuthorService가 이 클래스를 사용할 수 있도록 설계됨
 */
public class UserRepository extends JsonRepository<User> {

    @Override
    protected String getFileName() {
        return "users";
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected Long getId(User entity) {
        return entity.getId();
    }

    @Override
    protected void setId(User entity, Long id) {
        entity.setId(id);
    }

    // === 추가 조회 메서드 ===

    /**
     * username으로 사용자 조회
     *
     * @param username 로그인 ID
     * @return User 객체 (없으면 null)
     */
    public User findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * username 중복 체크
     *
     * @param username 확인할 ID
     * @return 존재 여부
     */
    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    /**
     * 닉네임 중복 체크 (독자만)
     *
     * @param nickname 확인할 닉네임
     * @return 존재 여부
     */
    public boolean existsByNickname(String nickname) {
        return findAll().stream()
                .filter(u -> u instanceof Reader)
                .map(u -> (Reader) u)
                .anyMatch(r -> r.getNickname().equals(nickname));
    }

    /**
     * 작가명 중복 체크 (작가만)
     *
     * @param authorName 확인할 작가명
     * @return 존재 여부
     */
    public boolean existsByAuthorName(String authorName) {
        return findAll().stream()
                .filter(u -> u instanceof Author)
                .map(u -> (Author) u)
                .anyMatch(a -> a.getAuthorName().equals(authorName));
    }

}