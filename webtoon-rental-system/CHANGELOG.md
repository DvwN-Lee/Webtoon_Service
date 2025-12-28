# Changelog

프로젝트의 주요 변경 사항을 기록합니다.

형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.0.0/)를 따릅니다.

---

## [Unreleased] - dm/observer-debug 브랜치

### Added (기능 추가)

#### 디자인 패턴 구현

- **Observer Pattern**: 팔로우 및 알림 시스템
  - `Observer`, `Subject` Interface 추가
  - `Webtoon` 클래스에서 `Subject` 구현 (팔로워 관리, 알림 전송)
  - `Reader` 클래스에서 `Observer` 구현 (알림 수신)
  - `NotificationService`, `NotificationRepository` 추가

- **Strategy Pattern**: 접근 전략 및 결제 전략
  - `AccessStrategy` Interface 및 구현체 (`RentalAccessStrategy`, `PurchaseAccessStrategy`)
  - `PaymentStrategy` Interface 및 구현체 (`CreditCardPaymentStrategy`, `BankTransferPaymentStrategy`)
  - `AccessService`: 대여/구매 통합 관리, 중복 구매 방지, 대여->구매 전환 시 차액 정산

- **Repository Pattern**: JSON 파일 기반 데이터 영속화
  - `JsonRepository<T>` 추상 베이스 클래스 (Generic, Auto Increment ID, ReentrantReadWriteLock)
  - `JsonWebtoonRepository`, `JsonEpisodeRepository`, `JsonReaderRepository` 구현

#### 도메인 클래스

- `Episode`: 회차 정보 (episodeNumber, title, content, rentPrice, buyPrice, viewCount)
- `Webtoon`: 웹툰 정보, Subject 패턴 구현
- `Statistics`: 통계 정보 (조회수, 에피소드 수)
- `AuthorStats`: 작가별 통계 (웹툰 ID, 조회수, 대여/구매 수, 수익)
- `EpisodeStats`: 회차별 통계 (조회수, 대여/구매 수)
- `Notification`: 알림 정보 (독자 ID, 웹툰 ID, 메시지, 읽음 여부)
- `Purchase`: 구매 기록 (독자 ID, 회차 ID, 구매가, 구매 시각)
- `Rental`: 대여 기록 (독자 ID, 회차 ID, 만료 시각)
- `PaymentHistory`: 결제 이력 (결제 금액, 결제 수단, 포인트, 결제 시각)

#### Service Layer 확장

- `EpisodeService`: 회차 업로드, 조회, 조회수 증가
- `StatisticsService`: 통계 집계 (조회수, 에피소드 수, 수익)
- `PointService`: 포인트 충전, 차감, 조회
- `ReaderService`: 독자 프로필 관리, 홈 화면 조회 (팔로우한 웹툰 목록)
- `AuthorService`: 작가 프로필 관리, 작품 목록 조회
- `WebtoonService`: 웹툰 CRUD, 검색, 정렬, 팔로우/언팔로우, 알림 발송

#### CLI Controller 확장

- `ReaderMenuController` (632줄):
  - 웹툰 탐색 메뉴 (검색, 정렬, 상세 조회)
  - 내 서재 메뉴 (대여/구매한 회차 목록)
  - 알림함 메뉴 (알림 조회, 읽음 처리)
  - 포인트 충전 메뉴 (결제 수단 선택)

- `AuthorMenuController` (354줄):
  - 내 작품 관리 메뉴 (작품 목록, 상세 조회)
  - 새 작품 등록 메뉴
  - 회차 업로드 메뉴
  - 통계 조회 메뉴 (작품별 조회수, 대여/구매 수, 수익)

#### 샘플 데이터 초기화

- `DataInitializer`: 웹툰 5개, 회차 45개, 작가 4명, 독자 3명 자동 생성
- 팔로우 관계 자동 설정
- 포인트 충전 내역 자동 생성
- 대여/구매 샘플 데이터 자동 생성

#### 개발 환경 설정

- `.vscode/settings.json`, `launch.json`, `tasks.json`: VSCode 설정 추가
- Windows UTF-8 인코딩 설정 (VM options, console.encoding)
- `.gitignore`: runtime JSON 데이터 파일 제외 (`src/main/resources/data/*.json`)

---

### Fixed (버그 수정)

- **회차 조회 시 조회수 증가 기능 누락**: `EpisodeService.incrementViewCount()` 메서드 추가 및 연동
- **팔로우/언팔로우 시 양방향 동기화 누락**:
  - `ReaderService.followWebtoon()`: Reader의 followingWebtoonIds 추가 + Webtoon.attach() 호출
  - `ReaderService.unfollowWebtoon()`: Reader의 followingWebtoonIds 제거 + Webtoon.detach() 호출
- **포인트 차감 후 Reader 영속화 누락**: `AccessService.grantAccess()` 메서드에서 `readerRepository.update()` 호출 추가
- **포인트 충전 후 Reader 영속화 누락**: `PointService.chargePoints()` 메서드에서 `readerRepository.update()` 호출 추가
- **통계 집계 오류**: `StatisticsService`에서 조회수, 에피소드 수 계산 로직 수정
- **대여->구매 전환 시 전액 차감 문제**: `AccessService`에서 차액만 차감하도록 로직 개선

---

### Changed (변경)

- **ID 타입 통합**: String -> Long (User, Webtoon, Episode 등 모든 도메인 객체)
- **통계 관련 로직 리팩토링**: `StatisticsService` 메서드 분리 및 정리
- **Repository 구현 방식 변경**: In-Memory와 JSON 기반 구현체 분리
- **UI 표기 변경**: "인기도" -> "조회수"

---

### Tests (테스트 추가)

#### 단위 테스트 (8개)

- `AuthServiceTest`: 회원가입, 로그인 테스트
- `EpisodeServiceTest`: 회차 조회, 조회수 증가 테스트
- `WebtoonServiceTest`: 웹툰 CRUD, 검색, 정렬 테스트
- `AuthorServiceTest`: 작가 프로필 관리 테스트
- `StatisticsServiceTest`: 통계 집계 테스트
- `NotificationServiceTest`: 알림 생성, 조회 테스트
- `PointServiceTest`: 포인트 충전, 차감 테스트
- `AccessServiceTest`: 대여/구매 접근 제어 테스트

#### 통합 테스트 (2개)

- `ScenarioIntegrationTest`: 주요 기능 통합 테스트 (회원가입, 로그인, 웹툰 검색, 대여)
- `FullScenarioIntegrationTest`: 전체 시나리오 통합 테스트 (645줄)
  - 작가 통계 기능 테스트
  - 언팔로우 시나리오 테스트
  - 계좌이체 결제 테스트
  - 회차 열람 및 조회수 증가 테스트
  - 포인트 부족 예외 테스트
  - 중복 구매 방지 테스트

---

### Documentation (문서화)

- `docs/ARCHITECTURE.md`: 시스템 아키텍처 및 레이어 구조 설명
- `docs/DESIGN_PATTERNS.md`: 적용된 디자인 패턴 상세 설명
- `README.md`: 아키텍처, 디자인 패턴, 패키지 구조 섹션 추가

---

## 통계

- **총 커밋 수**: 22개
- **변경 파일 수**: 81개
- **추가된 줄**: +7,683
- **삭제된 줄**: -150

---

## 주요 커밋 히스토리

| 날짜 | 커밋 | 설명 |
|------|------|------|
| 2025-11-30 | f756e55 | feat: Issue #22 DataInitializer 확장 - 샘플 데이터 생성 기능 추가 |
| 2025-11-30 | 477cdee | test: 통합 테스트 시나리오 확장 및 UI 조회수 표기 수정 |
| 2025-11-30 | 90a7383 | refactor: 통계 관련 오류 수정 |
| 2025-11-30 | 32398a5 | fix: 포인트/팔로우 초기화 및 대여 후 포인트 차감 불일치 문제 해결 |
| 2025-11-30 | bb9412a | fix: 회차 조회 시 조회수 증가 기능 추가 |
| 2025-11-30 | fbedf35 | fix: 통계 기능 연동 및 코드 정리 |
| 2025-11-30 | 3444be3 | fix: 팔로우/알림 기능 버그 수정 |
| 2025-11-23 | ec673f4 | feat: VSCode Windows UTF-8 인코딩 설정 추가 |
| 2025-11-21 | 20347ea | feat: 작가/독자 메뉴 전체 구현 및 UTF-8 인코딩 설정 |
| 2025-11-21 | ece2ada | feat: 비즈니스 로직 수동 포팅 완료 |
| 2025-11-21 | 13063b1 | fix: Observer 패턴 통합 수정 |
