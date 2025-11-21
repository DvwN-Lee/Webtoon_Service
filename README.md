# 웹툰 대여 시스템

웹툰 대여 및 구매 플랫폼 CLI 애플리케이션

## 요구사항

- Java 21 이상
- Gradle 8.x 이상

## 실행 방법

### IDE에서 직접 실행 (권장)

#### IntelliJ IDEA

1. `src/main/java/com/webtoon/cli/Main.java` 파일 열기
2. 메뉴: Run → Edit Configurations...
3. Main 클래스 설정이 없으면 생성:
   - '+' 버튼 클릭 → Application 선택
   - Name: `Main`
   - Main class: `com.webtoon.cli.Main`
   - Module: `webtoon-rental-system.main`
4. **VM options** 필드에 다음 추가:
   ```
   -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
   ```
5. **Environment variables** (선택사항, Windows에서 권장):
   ```
   JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
   ```
6. Apply → OK
7. Run 버튼 클릭 또는 `Shift + F10`

#### Eclipse

1. `Main.java` 우클릭 → Run As → Run Configurations
2. Arguments 탭 → VM arguments에 추가:
   ```
   -Dfile.encoding=UTF-8
   ```
3. Apply → Run

#### VS Code

1. `.vscode/launch.json` 파일 생성 또는 수정:
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "Main",
         "request": "launch",
         "mainClass": "com.webtoon.cli.Main",
         "vmArgs": "-Dfile.encoding=UTF-8"
       }
     ]
   }
   ```
2. F5로 실행

### 터미널에서 실행

```bash
./gradlew run
```

Windows:
```batch
gradlew.bat run
```

### 인코딩 문제 해결

**프로그램 자체가 자동으로 UTF-8 인코딩을 설정합니다.**

하지만 Windows에서 한글이 여전히 깨지는 경우:

1. **IDE 실행 시**: 위의 VM options 설정 확인
2. **터미널 실행 시**: 터미널에서 `chcp 65001` 실행 후 `gradlew.bat run` 실행

## 빌드

```bash
./gradlew clean build
```

## 테스트

```bash
./gradlew test
```

## 샘플 데이터

첫 실행 시 자동으로 샘플 데이터가 생성됩니다:

### 작가 계정
- ID: `chugong` / PW: `1234` / 작가명: 추공
- ID: `geomma` / PW: `1234` / 작가명: 검마
- ID: `ant_writer` / PW: `1234` / 작가명: 안트

### 웹툰 작품
1. 나 혼자만 레벨업 (추공, 연재중, 15화)
2. 마검의 계승자 (검마, 연재중, 20화)
3. 던전 리셋 (안트, 완결, 10화)

## 기능

### 독자 기능
- 회원가입 및 로그인
- 웹툰 검색 및 탐색
- 웹툰 팔로우
- 회차 대여 (10분, 50P)
- 회차 구매 (영구, 100P)
- 포인트 충전
- 알림 확인

### 작가 기능
- 회원가입 및 로그인
- 작품 등록
- 회차 업로드
- 작품 정보 수정
- 통계 조회