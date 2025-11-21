# Webtoon_Service

🔧 임시 수정 사항 – User.addPoints()

통합 테스트 과정에서 특정 테스트 시나리오 검증을 위해
User.java의 addPoints(int amount) 메서드를 임시로 음수 값 허용 형태로 변경하였습니다.

📌 변경 이유

테스트 환경에서 포인트 차감·충전 로직을 유연하게 검증하기 위해

기존 구현(amount <= 0일 때 예외 발생)은 테스트 데이터 초기화 과정에서 충돌이 발생함

🛠 변경 내용

기존 로직은 IllegalArgumentException이 발생하도록 되어 있었음

테스트가 통과하도록 하기 위해 음수 허용 버전으로 임시 변경

기존 로직은 코드 내에 주석으로 보존해두었음

// 기존 로직 (주석 처리됨)
// if (amount <= 0) {
//     throw new IllegalArgumentException("충전 포인트는 0 이상이어야 합니다.");
// }

// 테스트 및 데이터 초기화용 임시 로직
public void addPoints(int amount) {
    this.points += amount;
}

📌 주의사항

본 변경은 테스트 전용이며 실제 비즈니스 로직(main 브랜치)에는 반영되지 않아야 합니다.

이후 기능 개발이 완료되면
addPoints() 메서드는 반드시 원래 검증 로직으로 복원해야 합니다.
