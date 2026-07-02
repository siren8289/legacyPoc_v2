# CODE WALKTHROUGH (Spring Boot + Vue)

## 작업 방식

1. 백엔드는 1단계 리팩토링과 동일하게 기능별 Spring Boot 구조를 사용했습니다.
2. 프론트는 Vue 3(CDN)로 상태/렌더링을 컴포넌트 방식으로 단순화했습니다.
3. 기능별 페이지를 분리해 투자자와 입출고를 독립적으로 개발 가능하게 했습니다.

## 핵심 파일

- 백엔드
  - `feature/investor/*`
  - `feature/warehouse/*`
- 프론트
  - `static/pages/investor.html` + `static/js/investor-vue.js`
  - `static/pages/warehouse.html` + `static/js/warehouse-vue.js`

## 동작 요약

- Vue 앱 `mounted()`에서 목록을 초기 조회
- 등록 버튼 클릭 시 form 데이터를 `POST`로 전송
- 등록 성공 후 목록 재조회
