# 시스템 아키텍처

## 개요

웹툰 대여 시스템은 독자와 작가가 웹툰 플랫폼을 통해 상호작용할 수 있는 CLI 기반 애플리케이션입니다. Layered Architecture를 적용하여 관심사를 명확히 분리하고, 디자인 패턴을 통해 유지보수성과 확장성을 확보했습니다.

### 기술 스택

- **언어**: Java 21
- **빌드 도구**: Gradle 8.x
- **데이터 저장**: JSON 파일 (Gson 라이브러리)
- **테스트**: JUnit 5
- **인터페이스**: CLI (Command Line Interface)

---

## 레이어 아키텍처

이 시스템은 4개의 레이어로 구성되며, 각 레이어는 명확한 책임을 가집니다.

```
┌─────────────────────────────────────┐
│   Presentation Layer (CLI)          │  사용자 입력/출력
├─────────────────────────────────────┤
│   Service Layer                     │  비즈니스 로직
├─────────────────────────────────────┤
│   Domain Layer                      │  엔티티, 도메인 규칙
├─────────────────────────────────────┤
│   Repository Layer                  │  데이터 영속화
└─────────────────────────────────────┘
         │
         ▼
    JSON 파일 (src/main/resources/data/)
```

### Presentation Layer (CLI)

사용자와의 상호작용을 담당하는 계층입니다.

| 클래스 | 책임 | 라인 수 |
|--------|------|---------|
| `Main.java` | 프로그램 진입점, 인코딩 설정, DataInitializer 실행 | ~100 |
| `MenuController.java` | 공통 메뉴 처리 (로그인, 회원가입) | ~200 |
| `ReaderMenuController.java` | 독자 전용 메뉴 (웹툰 탐색, 대여, 포인트 충전, 알림 확인) | 632 |
| `AuthorMenuController.java` | 작가 전용 메뉴 (작품 관리, 회차 업로드, 통계 조회) | 354 |

**특징**:
- UTF-8 인코딩 강제 설정 (Windows 호환성)
- InputUtil을 통한 사용자 입력 검증
- 메뉴 기반 네비게이션

---

### Service Layer

비즈니스 로직을 처리하는 계층입니다. Repository를 통해 데이터를 조회/수정하고, Domain 객체를 조작합니다.

| Service | 주요 책임 |
|---------|----------|
| `AuthService` | 회원가입, 로그인, 인증 |
| `WebtoonService` | 웹툰 CRUD, 검색, 정렬, 팔로우/언팔로우, 회차 발행, 알림 발송 |
| `EpisodeService` | 회차 조회, 조회수 증가, 수정, 삭제 |
| `ReaderService` | 독자 프로필 관리, 홈 화면 조회 (팔로우한 웹툰 목록) |
| `AuthorService` | 작가 프로필 관리, 작품 목록 조회 |
| `NotificationService` | 알림 생성, 조회, 읽음 처리 |
| `PointService` | 포인트 충전, 차감, 잔액 조회 |
| `AccessService` | 대여/구매 처리, 접근 권한 확인 (Strategy Pattern 활용) |
| `StatisticsService` | 작가 통계 집계 (조회수, 대여/구매 수, 수익) |

**핵심 비즈니스 로직**:
- `WebtoonService.notifyFollowers()`: 새 회차 발행 시 팔로워에게 알림 전송 (Observer Pattern)
- `AccessService.grantAccess()`: 대여/구매 전략을 선택하여 접근 권한 부여 (Strategy Pattern)
- `PointService.chargePoints()`: 결제 전략을 선택하여 포인트 충전 (Strategy Pattern)

---

### Domain Layer

엔티티와 도메인 규칙을 정의하는 계층입니다.

**핵심 도메인 객체**:

| 클래스 | 역할 | 디자인 패턴 |
|--------|------|------------|
| `User` | 사용자 추상 클래스 (공통 속성: id, username, password, points) | - |
| `Reader` | 독자 (팔로우 목록, 알림 수신) | Observer |
| `Author` | 작가 (작가명) | - |
| `Webtoon` | 웹툰 (회차 목록, 팔로워 관리) | Subject |
| `Episode` | 회차 (에피소드 번호, 제목, 내용, 조회수) | - |
| `Notification` | 알림 (독자 ID, 웹툰 ID, 메시지, 읽음 여부) | - |
| `Purchase` | 구매 기록 (독자 ID, 회차 ID, 구매 시각) | - |
| `Rental` | 대여 기록 (독자 ID, 회차 ID, 만료 시각) | - |
| `PaymentHistory` | 결제 이력 (결제 금액, 결제 수단, 포인트) | - |
| `Statistics` | 통계 (조회수, 에피소드 수) | - |

**도메인 규칙 예시**:
- `Webtoon.addEpisode()`: 회차 추가 시 `updatedAt` 갱신
- `Webtoon.notifyObservers()`: 팔로워 목록을 순회하며 Observer.update() 호출
- `Reader.followWebtoon()`: 중복 팔로우 방지

---

### Repository Layer

데이터 영속화를 담당하는 계층입니다. JSON 파일을 통해 데이터를 저장/조회합니다.

**추상 베이스 클래스**:
- `JsonRepository<T>`: Generic 기반 CRUD 메서드, Auto Increment ID 생성, ReentrantReadWriteLock을 통한 동시성 제어

**구현체**:

| Repository | 저장 파일 | 구현 방식 |
|------------|----------|----------|
| `ReaderRepository` | `readers.json` | JsonRepository 상속 |
| `WebtoonRepository` | `webtoons.json` | JsonRepository 상속 |
| `EpisodeRepository` | `episodes.json` | JsonRepository 상속 |
| `AuthorRepository` | (메모리) | In-Memory |
| `NotificationRepository` | (메모리) | In-Memory |
| `PurchaseRepository` | (메모리) | In-Memory |
| `RentalRepository` | (메모리) | In-Memory |
| `PaymentHistoryRepository` | (메모리) | In-Memory |
| `StatisticsRepository` | (메모리) | In-Memory |

**특징**:
- UTF-8 인코딩 명시 (맥/윈도우 호환성)
- Read/Write Lock을 통한 동시성 제어
- 테스트를 위한 In-Memory 구현체 제공

---

## 패키지 구조

```
com.webtoon
├── cli/                          # Presentation Layer
│   ├── Main.java
│   ├── MenuController.java
│   ├── ReaderMenuController.java
│   └── AuthorMenuController.java
│
├── service/                      # Service Layer
│   ├── AuthService.java
│   ├── WebtoonService.java
│   ├── EpisodeService.java
│   ├── ReaderService.java
│   ├── AuthorService.java
│   ├── NotificationService.java
│   ├── PointService.java
│   ├── AccessService.java
│   └── StatisticsService.java
│
├── domain/                       # Domain Layer
│   ├── User.java
│   ├── Reader.java
│   ├── Author.java
│   ├── Webtoon.java
│   ├── Episode.java
│   ├── Notification.java
│   ├── Purchase.java
│   ├── Rental.java
│   ├── PaymentHistory.java
│   ├── Statistics.java
│   ├── AuthorStats.java
│   └── EpisodeStats.java
│
├── repository/                   # Repository Layer
│   ├── WebtoonRepository.java (Interface)
│   ├── EpisodeRepository.java (Interface)
│   ├── ... (기타 Interface)
│   ├── JsonWebtoonRepository.java
│   ├── JsonEpisodeRepository.java
│   ├── InMemoryAuthorRepository.java
│   └── ... (기타 구현체)
│
├── pattern/                      # 디자인 패턴 Interface/구현체
│   ├── Observer.java
│   ├── Subject.java
│   ├── AccessStrategy.java
│   ├── RentalAccessStrategy.java
│   ├── PurchaseAccessStrategy.java
│   ├── PaymentStrategy.java
│   ├── CreditCardPaymentStrategy.java
│   └── BankTransferPaymentStrategy.java
│
├── common/                       # 공통 유틸리티
│   ├── repository/
│   │   └── JsonRepository.java   # Repository 추상 베이스 클래스
│   ├── util/
│   │   ├── InputUtil.java        # 사용자 입력 검증
│   │   └── LocalDateTimeAdapter.java  # Gson Adapter
│   └── validation/
│       ├── Validator.java
│       └── ValidationException.java
│
└── util/
    └── DataInitializer.java      # 샘플 데이터 초기화
```

---

## 데이터 흐름

### 1. 회차 대여 및 열람 시나리오

```
[독자]
   │
   ▼
ReaderMenuController.rentEpisode()
   │
   ▼
AccessService.grantAccess(reader, episode, RentalAccessStrategy)
   │
   ├─▶ RentalAccessStrategy.execute()  (포인트 50P 차감)
   │       └─▶ new Rental(readerId, episodeId, expireAt)
   │
   ├─▶ RentalRepository.save(rental)
   │       └─▶ InMemory 저장
   │
   └─▶ ReaderRepository.update(reader)
           └─▶ readers.json 파일 업데이트

[회차 열람 시점]
   │
   ▼
EpisodeService.getEpisodeDetailForUser(episodeId, readerId)
   │
   ├─▶ 접근 권한 확인 (대여/구매 여부)
   │
   └─▶ EpisodeService.incrementViewCount(episodeId)
           └─▶ Episode.viewCount++
           └─▶ EpisodeRepository.update(episode)
                   └─▶ episodes.json 파일 업데이트
```

### 2. 새 회차 발행 및 알림 시나리오 (Observer Pattern)

```
[작가]
   │
   ▼
AuthorMenuController.uploadEpisode()
   │
   ▼
WebtoonService.publishEpisode(webtoonId, title, content)
   │
   ├─▶ new Episode(webtoonId, episodeNumber, title, content)
   ├─▶ EpisodeRepository.save(episode)
   │       └─▶ episodes.json 파일 저장
   │
   ├─▶ Webtoon.addEpisode(episodeId)
   │       └─▶ episodeIds.add(episodeId)
   │
   ├─▶ WebtoonRepository.update(webtoon)
   │       └─▶ webtoons.json 파일 업데이트
   │
   └─▶ WebtoonService.notifyFollowers(webtoonId)
           │
           └─▶ Webtoon.notifyObservers()
                   │
                   └─▶ for each Observer (Reader):
                           └─▶ Reader.update(webtoonId, title, message)
                                   └─▶ NotificationService.createNotification()
                                           └─▶ InMemory 저장
```

---

## 의존성 방향

```
cli  ──▶  service  ──▶  repository  ──▶  domain
         │                                    │
         └────────────────▶ pattern ◀────────┘
```

- Presentation Layer는 Service에만 의존
- Service는 Repository와 Domain에 의존
- Repository는 Domain에만 의존
- Domain은 Pattern에 의존 (Observer, Subject)
- Pattern은 Domain에 의존 (Reader, Episode 등 참조)

---

## 확장 포인트

향후 시스템 확장 시 고려할 수 있는 포인트입니다.

### 1. 데이터 저장소 교체
- `JsonRepository`를 DB 기반 Repository로 교체 가능 (JPA, JDBC 등)
- Interface를 준수하므로 Service 계층 수정 불필요

### 2. 새로운 결제 수단 추가
- `PaymentStrategy` 인터페이스를 구현하는 새로운 전략 클래스 추가
- 예: `KakaoPayPaymentStrategy`, `NaverPayPaymentStrategy`

### 3. 새로운 접근 권한 모델 추가
- `AccessStrategy` 인터페이스를 구현하는 새로운 전략 클래스 추가
- 예: `SubscriptionAccessStrategy` (구독 모델)

### 4. 웹/모바일 인터페이스 추가
- CLI Controller를 REST API Controller로 교체
- Service 계층은 그대로 재사용 가능
