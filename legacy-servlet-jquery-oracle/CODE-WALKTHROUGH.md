# CODE WALKTHROUGH (원본 레거시)

## 작업 방식

1. 화면 중심 구조를 유지하면서 레거시 문제를 의도적으로 드러냈습니다.
2. 백엔드는 `Servlet -> DAO` 직접 호출, action 분기, 컬럼별 update를 사용합니다.
3. 프론트는 jQuery + 직접 DOM 조작으로 목록/모달/필드 저장을 처리합니다.

## 핵심 파일 설명

- `servlet-jdbc-backend/src/main/java/com/example/legacy/servlet/WarehouseIoServlet.java`
  - `action` 값으로 모든 동작을 분기합니다.
  - `list/detail/createEmptyRow/update.../delete`를 한 클래스에서 처리합니다.
- `servlet-jdbc-backend/src/main/java/com/example/legacy/dao/WarehouseIoDao.java`
  - 컬럼별 update 메서드가 다수 존재합니다.
  - JDBC 시도 후 실패하면 메모리 fallback으로 동작합니다.
- `jquery-webapp/src/main/webapp/static/js/warehouse-io.js`
  - 조회/등록/상세/수정/삭제를 하나의 JS 파일에서 직접 제어합니다.
  - 필드 change 시 즉시 API 호출로 저장합니다.
- `jquery-webapp/src/main/webapp/static/js/investor.js`
  - 투자자 목록/상세/등록을 직접 렌더링 방식으로 처리합니다.

## 왜 이렇게 구성했는가

- 리팩토링 전 상태를 분석하기 위한 샘플이므로, 좋은 구조보다 레거시 특성 재현을 우선했습니다.
