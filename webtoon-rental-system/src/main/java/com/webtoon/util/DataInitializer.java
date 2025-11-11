package com.webtoon.util;

import com.webtoon.domain.Author;
import com.webtoon.domain.Reader;
import com.webtoon.repository.UserRepository;
import com.webtoon.service.AuthService;

import java.io.File;

public class DataInitializer {

    private final AuthService authService;
    private final UserRepository userRepository; // users.json 파일 경로 확인용

    public DataInitializer(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    public void initializeData() {
        System.out.println("데이터 초기화를 시작합니다...");

        // users.json 파일 경로 확인
        String usersFilePath = "src/main/resources/data/users.json";
        File usersFile = new File(usersFilePath);

        if (usersFile.exists() && usersFile.length() > 0) {
            System.out.println("users.json 파일이 이미 존재하여 데이터 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("users.json 파일이 없거나 비어있으므로 샘플 데이터를 생성합니다.");

        try {
            // 독자 2명 생성
            authService.registerReader("reader1", "1234", "독자1");
            authService.registerReader("reader2", "1234", "독자2");

            // 작가 2명 생성
            authService.registerAuthor("author1", "1234", "작가1", "안녕하세요, 작가1입니다.");
            authService.registerAuthor("author2", "1234", "작가2", "안녕하세요, 작가2입니다.");

            System.out.println("[완료] 샘플 사용자 데이터 생성이 완료되었습니다.");
        } catch (Exception e) {
            System.err.println("데이터 초기화 중 오류 발생: " + e.getMessage());
        }
    }
}
