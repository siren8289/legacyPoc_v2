# Spring Boot Refactor Version

`legacy-servlet-jquery-oracle`의 1단계 리팩토링 버전입니다.

## 기술 스택

- 백엔드: Spring Boot 3 + REST API
- 프론트: Vanilla JavaScript (`fetch`)

## 목표

- 프론트엔드: jQuery 제거, Vanilla JavaScript(`fetch`) 기반
- 백엔드: Servlet action 분기 제거, Spring Boot REST API
- 구조: 기능별 패키지 분리
  - `feature/investor/{controller,service,repository,model,dto}`
  - `feature/warehouse/{controller,service,repository,model,dto}`

## 실행

```bash
cd refactor-springboot-vanilla/springboot-refactor
mvn spring-boot:run
```

기본 포트: `8081`

- 홈: `http://localhost:8081/`
- 투자자 화면: `http://localhost:8081/pages/investor.html`
- 입출고 화면: `http://localhost:8081/pages/warehouse.html`
