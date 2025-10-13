package com.webtoon.service;

import com.webtoon.common.validation.ValidationException;
import com.webtoon.common.validation.Validator;
import com.webtoon.domain.Author;
import com.webtoon.domain.Reader;
import com.webtoon.domain.User;
import com.webtoon.repository.UserRepository;

/**
 * 인증 서비스
 * 회원가입, 로그인, 로그아웃 담당
 */
public class AuthService {

    private final UserRepository userRepository;
    private User currentUser;  // 현재 로그인된 사용자 (세션)

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    // 테스트용 생성자 (의존성 주입)
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * FR-ACCOUNT-01: 독자 회원가입
     *
     * @param username 로그인 ID (5-20자)
     * @param password 비밀번호 (4자 이상)
     * @param nickname 닉네임 (2-10자)
     * @return 생성된 Reader 객체
     * @throws ValidationException 검증 실패 시
     */
    public Reader registerReader(String username, String password, String nickname) {
        // 1. 입력 검증
        Validator.validateUsername(username);
        Validator.validatePassword(password);
        Validator.validateDisplayName(nickname);

        // 2. 중복 체크
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("이미 존재하는 ID입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new ValidationException("이미 존재하는 닉네임입니다.");
        }

        // 3. Reader 생성 (초기 포인트 1000P 자동 지급)
        Reader reader = new Reader(username, password, nickname);

        // 4. 저장
        userRepository.save(reader);

        System.out.println("✅ 독자 회원가입 성공: " + reader.getDisplayName() + " (초기 포인트: " + reader.getPoints() + "P)");
        return reader;
    }

    /**
     * FR-ACCOUNT-02: 작가 회원가입
     *
     * @param username 로그인 ID (5-20자)
     * @param password 비밀번호 (4자 이상)
     * @param authorName 작가명 (2-10자)
     * @param bio 자기소개 (최대 200자, null 가능)
     * @return 생성된 Author 객체
     * @throws ValidationException 검증 실패 시
     */
    public Author registerAuthor(String username, String password, String authorName, String bio) {
        // 1. 입력 검증
        Validator.validateUsername(username);
        Validator.validatePassword(password);
        Validator.validateDisplayName(authorName);
        Validator.validateBio(bio);

        // 2. 중복 체크
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("이미 존재하는 ID입니다.");
        }
        if (userRepository.existsByAuthorName(authorName)) {
            throw new ValidationException("이미 존재하는 작가명입니다.");
        }

        // 3. Author 생성
        Author author = new Author(username, password, authorName, bio);

        // 작가 생성 시 초기 웹툰 등록, 알림 구독 등이 필요하다면 여기서 훅 호출 가능
//        System.out.printf("📢 작가 Subject 생성됨: %s (id=%s)%n", author.getSubjectName(), author.getSubjectId());

        // 4. 저장
        userRepository.save(author);

        System.out.println("✅ 작가 회원가입 성공: " + author.getDisplayName());
        return author;
    }

    /**
     * FR-ACCOUNT-03: 로그인
     *
     * @param userType 사용자 유형 ("READER" 또는 "AUTHOR")
     * @param username 로그인 ID
     * @param password 비밀번호
     * @return 로그인된 User 객체
     * @throws ValidationException 로그인 실패 시
     */
    public User login(String userType, String username, String password) {
        // 1. 사용자 조회
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ValidationException("존재하지 않는 ID입니다.");
        }

        // 2. 계정 유형 확인
        if (!user.getUserType().equals(userType)) {
            throw new ValidationException("계정 유형이 일치하지 않습니다.");
        }

        // 3. 비밀번호 확인
        if (!user.authenticate(password)) {
            throw new ValidationException("비밀번호가 일치하지 않습니다.");
        }

        // 4. 세션 설정
        this.currentUser = user;

        System.out.println("✅ 로그인 성공: " + user.getDisplayName() + "님 환영합니다!");
        return user;
    }

    /**
     * FR-ACCOUNT-04: 로그아웃
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("✅ 로그아웃: " + currentUser.getDisplayName() + "님");
            this.currentUser = null;
        }
    }

    /**
     * 현재 로그인된 사용자 조회
     *
     * @return 현재 사용자 (로그인 안 됨 시 null)
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 로그인 여부 확인
     *
     * @return 로그인 여부
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}