package com.webtoon.service;

import com.webtoon.common.validation.ValidationException;
import com.webtoon.domain.Author;
import com.webtoon.domain.Reader;
import com.webtoon.domain.User;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthService 통합 테스트
 */
class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();

        // 테스트 파일 삭제
        File file = new File("src/main/resources/data/users.json");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    @DisplayName("FR-ACCOUNT-01: 독자 회원가입 성공")
    void testRegisterReader_Success() {
        // Given
        String username = "reader123";
        String password = "pass1234";
        String nickname = "독자A";

        // When
        Reader reader = authService.registerReader(username, password, nickname);

        // Then
        assertNotNull(reader);
        assertNotNull(reader.getId());
        assertEquals(username, reader.getUsername());
        assertEquals(nickname, reader.getNickname());
        assertEquals(1000, reader.getPoints());  // 초기 포인트
        assertEquals("READER", reader.getUserType());
    }

    @Test
    @DisplayName("FR-ACCOUNT-01: 독자 회원가입 - ID 중복")
    void testRegisterReader_DuplicateUsername() {
        // Given
        authService.registerReader("reader123", "pass1234", "독자A");

        // When & Then
        assertThrows(ValidationException.class, () -> {
            authService.registerReader("reader123", "pass5678", "독자B");
        });
    }

    @Test
    @DisplayName("FR-ACCOUNT-01: 독자 회원가입 - 닉네임 중복")
    void testRegisterReader_DuplicateNickname() {
        // Given
        authService.registerReader("reader123", "pass1234", "독자A");

        // When & Then
        assertThrows(ValidationException.class, () -> {
            authService.registerReader("reader456", "pass5678", "독자A");
        });
    }

    @Test
    @DisplayName("FR-ACCOUNT-01: 독자 회원가입 - 입력 검증 실패")
    void testRegisterReader_ValidationFailed() {
        // ID 너무 짧음
        assertThrows(ValidationException.class, () -> {
            authService.registerReader("abc", "pass1234", "독자A");
        });

        // 비밀번호 너무 짧음
        assertThrows(ValidationException.class, () -> {
            authService.registerReader("reader123", "123", "독자A");
        });

        // 닉네임 너무 짧음
        assertThrows(ValidationException.class, () -> {
            authService.registerReader("reader123", "pass1234", "A");
        });
    }

    @Test
    @DisplayName("FR-ACCOUNT-02: 작가 회원가입 성공")
    void testRegisterAuthor_Success() {
        // Given
        String username = "author123";
        String password = "pass1234";
        String authorName = "작가갑";
        String bio = "판타지 전문 작가입니다.";

        // When
        Author author = authService.registerAuthor(username, password, authorName, bio);

        // Then
        assertNotNull(author);
        assertEquals(username, author.getUsername());
        assertEquals(authorName, author.getAuthorName());
        assertEquals(bio, author.getBio());
        assertEquals(0, author.getPoints());  // 작가 초기 포인트 0
        assertEquals("AUTHOR", author.getUserType());
    }

    @Test
    @DisplayName("FR-ACCOUNT-02: 작가 회원가입 - 작가명 중복")
    void testRegisterAuthor_DuplicateAuthorName() {
        // Given
        authService.registerAuthor("author123", "pass1234", "작가갑", null);

        // When & Then
        assertThrows(ValidationException.class, () -> {
            authService.registerAuthor("author456", "pass5678", "작가갑", null);
        });
    }

    @Test
    @DisplayName("FR-ACCOUNT-03: 로그인 성공 - 독자")
    void testLogin_Reader_Success() {
        // Given
        authService.registerReader("reader123", "pass1234", "독자A");

        // When
        User user = authService.login("READER", "reader123", "pass1234");

        // Then
        assertNotNull(user);
        assertTrue(user instanceof Reader);
        assertEquals("독자A", user.getDisplayName());
        assertTrue(authService.isLoggedIn());
        assertEquals(user, authService.getCurrentUser());
    }

    @Test
    @DisplayName("FR-ACCOUNT-03: 로그인 실패 - 존재하지 않는 ID")
    void testLogin_UserNotFound() {
        // When & Then
        assertThrows(ValidationException.class, () -> {
            authService.login("READER", "nonexist", "pass1234");
        });
    }

    @Test
    @DisplayName("FR-ACCOUNT-03: 로그인 실패 - 비밀번호 불일치")
    void testLogin_WrongPassword() {
        // Given
        authService.registerReader("reader123", "pass1234", "독자A");

        // When & Then
        assertThrows(ValidationException.class, () -> {
            authService.login("READER", "reader123", "wrongpass");
        });
    }

    @Test
    @DisplayName("FR-ACCOUNT-03: 로그인 실패 - 계정 유형 불일치")
    void testLogin_WrongUserType() {
        // Given
        authService.registerReader("reader123", "pass1234", "독자A");

        // When & Then
        assertThrows(ValidationException.class, () -> {
            authService.login("AUTHOR", "reader123", "pass1234");
        });
    }

    @Test
    @DisplayName("FR-ACCOUNT-04: 로그아웃")
    void testLogout() {
        // Given
        authService.registerReader("reader123", "pass1234", "독자A");
        authService.login("READER", "reader123", "pass1234");
        assertTrue(authService.isLoggedIn());

        // When
        authService.logout();

        // Then
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }

    @Test
    @DisplayName("포인트 사용 테스트")
    void testUsePoints() {
        // Given
        Reader reader = authService.registerReader("reader123", "pass1234", "독자A");
        assertEquals(1000, reader.getPoints());

        // When
        boolean success1 = reader.usePoints(500);
        boolean success2 = reader.usePoints(600);  // 잔액 부족

        // Then
        assertTrue(success1);
        assertFalse(success2);
        assertEquals(500, reader.getPoints());
    }

    @Test
    @DisplayName("포인트 충전 테스트")
    void testAddPoints() {
        // Given
        Reader reader = authService.registerReader("reader123", "pass1234", "독자A");

        // When
        reader.addPoints(500);

        // Then
        assertEquals(1500, reader.getPoints());
    }
}