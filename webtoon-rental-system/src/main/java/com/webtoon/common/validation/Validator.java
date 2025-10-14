package com.webtoon.common.validation;

/**
 * 입력값 검증 유틸리티 클래스
 * 모든 검증 로직을 중앙화하여 일관성 보장
 */
public class Validator {

    /**
     * ID 검증 (5-20자)
     * @param username 사용자 ID
     * @throws ValidationException 검증 실패 시
     */
    public static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("ID를 입력해주세요.");
        }

        String trimmed = username.trim();
        if (trimmed.length() < 5 || trimmed.length() > 20) {
            throw new ValidationException("ID는 5-20자 이내여야 합니다.");
        }
    }

    /**
     * 비밀번호 검증 (4자 이상)
     * @param password 비밀번호
     * @throws ValidationException 검증 실패 시
     */
    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("비밀번호를 입력해주세요.");
        }

        if (password.length() < 4) {
            throw new ValidationException("비밀번호는 4자 이상이어야 합니다.");
        }
    }

    /**
     * 닉네임/작가명 검증 (2-10자)
     * @param name 닉네임 또는 작가명
     * @throws ValidationException 검증 실패 시
     */
    public static void validateDisplayName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("이름을 입력해주세요.");
        }

        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 10) {
            throw new ValidationException("이름은 2-10자 이내여야 합니다.");
        }
    }

    /**
     * 포인트 검증 (음수 방지)
     * @param points 포인트
     * @throws ValidationException 검증 실패 시
     */
    public static void validatePoints(int points) {
        if (points < 0) {
            throw new ValidationException("포인트는 음수가 될 수 없습니다.");
        }
    }

    /**
     * 자기소개 검증 (최대 200자)
     * @param bio 자기소개
     * @throws ValidationException 검증 실패 시
     */
    public static void validateBio(String bio) {
        if (bio != null && bio.length() > 200) {
            throw new ValidationException("자기소개는 200자 이하여야 합니다.");
        }
    }
}