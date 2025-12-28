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

이 프로젝트는 이미 VSCode 설정 파일(`.vscode/`)이 포함되어 있습니다.

1. **필수 확장 설치**:
   - Extension Pack for Java (Microsoft)
   - Gradle for Java (Microsoft)

2. **실행 방법**:
   - `webtoon-rental-system/src/main/java/com/webtoon/cli/Main.java` 파일 열기
   - 방법 1: F5 키 (디버그 실행)
   - 방법 2: Ctrl+F5 (일반 실행)
   - 방법 3: Run and Debug 패널에서 "Main" 설정 선택 후 실행

3. **Windows 사용자 추가 설정** (한글 깨짐 방지):
   - VSCode 설정(Ctrl+,) 열기
   - `Files: Encoding` 검색 → `utf8` 확인
   - 터미널에서 실행 시: 통합 터미널 설정에서 Code Page 65001(UTF-8) 사용

4. **설정 파일 위치**:
   - `.vscode/launch.json`: 실행 구성 (VM 옵션 포함)
   - `.vscode/settings.json`: 프로젝트 설정 (인코딩 등)
   - `.vscode/tasks.json`: 빌드/실행 태스크

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

1. **IntelliJ IDEA**: 위의 VM options 설정 확인
2. **VSCode**:
   - `.vscode/launch.json`에 이미 UTF-8 설정이 포함되어 있습니다
   - 그래도 문제가 있다면: VSCode 통합 터미널에서 `chcp 65001` 실행
   - 또는: 외부 터미널(Windows Terminal 권장) 사용
3. **터미널 직접 실행**:
   - Windows: `chcp 65001` 실행 후 `gradlew.bat run`
   - Mac/Linux: 문제없음 (기본적으로 UTF-8 사용)

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

## 아키텍처

이 프로젝트는 Layered Architecture를 적용하여 관심사를 명확히 분리했습니다.

### 레이어 구성

| Layer | Package | 책임 |
|-------|---------|------|
| Presentation | cli | CLI 메뉴, 사용자 입력 처리 |
| Service | service | 비즈니스 로직 |
| Domain | domain | 엔티티, 도메인 규칙 |
| Repository | repository | 데이터 영속화 |

### 패키지 구조

```
com.webtoon
├── cli/          # CLI Controller (Main, MenuController, ReaderMenuController, AuthorMenuController)
├── service/      # Service Layer (WebtoonService, EpisodeService, AuthorService 등)
├── domain/       # Domain Model (User, Reader, Author, Webtoon, Episode 등)
├── repository/   # Repository Layer (JsonRepository, InMemory 구현체)
├── pattern/      # 디자인 패턴 Interface (Observer, Strategy)
├── common/       # 공통 유틸리티 (JsonRepository 베이스 클래스, Validator)
└── util/         # 데이터 초기화 (DataInitializer)
```

상세 내용: [docs/ARCHITECTURE.md](webtoon-rental-system/docs/ARCHITECTURE.md)

---

## 적용된 디자인 패턴

### Observer Pattern

팔로우 및 알림 기능 구현에 사용되었습니다.

- **Subject**: `Webtoon` (팔로워 관리, 알림 전송)
- **Observer**: `Reader` (알림 수신)
- **동작**: 작가가 새 회차 발행 시 팔로워들에게 자동 알림

### Strategy Pattern

회차 접근 방식과 결제 수단 선택에 사용되었습니다.

- **AccessStrategy**: 대여/구매 전략
  - `RentalAccessStrategy`: 10분 대여, 50P
  - `PurchaseAccessStrategy`: 영구 구매, 100P
- **PaymentStrategy**: 결제 수단 전략
  - `CreditCardPaymentStrategy`: 신용카드
  - `BankTransferPaymentStrategy`: 계좌이체

### Repository Pattern

데이터 영속화 추상화에 사용되었습니다.

- **베이스 클래스**: `JsonRepository<T>` (Generic, Auto Increment ID, 동시성 제어)
- **구현체**: JSON 파일 기반 (`JsonWebtoonRepository` 등)
- **테스트용**: In-Memory 구현체

상세 내용: [docs/DESIGN_PATTERNS.md](webtoon-rental-system/docs/DESIGN_PATTERNS.md)

---

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

---

## 상세 문서

프로젝트의 상세 문서는 다음 링크에서 확인할 수 있습니다.

- [시스템 아키텍처](webtoon-rental-system/docs/ARCHITECTURE.md) - 레이어 구조, 패키지 구성, 데이터 흐름
- [디자인 패턴 상세](webtoon-rental-system/docs/DESIGN_PATTERNS.md) - Observer, Strategy, Repository 패턴 설명
- [변경 이력](webtoon-rental-system/CHANGELOG.md) - 버전별 변경 사항
- [시나리오 테스트 가이드](webtoon-rental-system/SCENARIO_TEST_GUIDE.md) - 통합 테스트 시나리오