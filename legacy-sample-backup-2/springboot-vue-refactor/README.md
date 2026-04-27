# Spring Boot + Vue Refactor Version

`legacy-sample-backup-2`의 리팩토링 버전입니다.

## 목표

- 프론트엔드: Vue 3(Composition API 대신 CDN 전역 빌드 기반) 적용
- 백엔드: Spring Boot REST API
- 구조: 기능별 패키지 분리
  - `feature/investor/{controller,service,repository,model,dto}`
  - `feature/warehouse/{controller,service,repository,model,dto}`

## 실행

```bash
cd springboot-vue-refactor
mvn spring-boot:run
```

기본 포트: `8082`

- 홈: `http://localhost:8082/`
- 투자자 기능: `http://localhost:8082/pages/investor.html`
- 입출고 기능: `http://localhost:8082/pages/warehouse.html`
