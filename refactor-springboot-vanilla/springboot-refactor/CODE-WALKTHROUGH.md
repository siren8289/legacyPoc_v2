# CODE WALKTHROUGH (Spring Boot + Vanilla JS)

## 작업 방식

1. 레거시 action servlet을 Spring Boot REST로 대체했습니다.
2. 기능별 패키지(`investor`, `warehouse`)로 controller/service/repository/model/dto를 분리했습니다.
3. 프론트는 jQuery 없이 Vanilla JS(`fetch`)로 재작성했습니다.

## 핵심 흐름

- 투자자 조회: `/api/investors` -> `InvestorController` -> `InvestorService` -> `InvestorRepository`
- 투자자 등록: `POST /api/investors` 동일 경로
- 입출고 조회/등록: `/api/warehouses` 경로로 동일한 계층 흐름

## 파일별 포인트

- `src/main/java/com/example/refactor/feature/*/controller/*Controller.java`
  - HTTP 요청/응답 계약 담당
- `src/main/java/com/example/refactor/feature/*/service/*Service.java`
  - 입력 정리, 기본값 처리 등 비즈니스 로직 담당
- `src/main/java/com/example/refactor/feature/*/repository/*Repository.java`
  - 현재는 인메모리 저장소 구현
- `src/main/resources/static/js/*.js`
  - API 호출 + DOM 렌더링 담당
