# LegacyPoc Portfolio PRD

## 0. 사용 목적

이 문서는 레거시 현대화 포트폴리오 프로젝트의 제품/기능 범위, 우선순위, 성공 기준을 명확히 정리하기 위한 PRD이다.

```
왜 만드는가?   → 레거시 구조의 문제를 재현하고, 단계적 리팩토링 결과를 비교·증명하기 위해
누가 쓰는가?   → 백엔드/풀스택 개발자, 리팩토링 학습자, 포트폴리오 검토자
무엇을 제공?   → Servlet+jQuery+Oracle 레거시 샘플 + 3단계 현대화 스냅샷
어디까지?      → MVP: 핵심 2도메인(입출고·투자자) CRUD + 리팩토링 비교 문서화
성공 기준?     → Before/After 구조·API·성능 패턴이 코드와 문서로 설명 가능할 것
개발팀 구현?   → FR 단위 기능, NFR, API, 데이터 요구사항 (본 문서 §9~§13)
```

---

# 1. 문서 기본 정보

| 항목 | 내용 |
| --- | --- |
| 문서명 | LegacyPoc Portfolio PRD |
| 버전 | v0.1 |
| 작성일 | 2026-07-01 |
| 작성자 | Portfolio Team |
| 제품/서비스명 | LegacyPoc Portfolio |
| 대상 릴리즈 | MVP |
| 문서 상태 | Draft |
| 관련 문서 | `legacy-servlet-jquery-oracle/README.md`, 각 모듈 `CODE-WALKTHROUGH.md`, `oracle-schema/schema.mock.sql` |

---

# 2. 제품 한 줄 소개

## 2.1 One-liner

```
LegacyPoc Portfolio는 백엔드 개발자가 Servlet+jQuery+Oracle 레거시 시스템의 문제점을 직접 경험하고,
Spring Boot·Vue·Next.js로 단계적 현대화한 결과를 비교할 수 있도록 입출고·투자자 관리 샘플을 제공하는 교육/포트폴리오 MVP다.
```

---

# 3. 배경 및 문제 정의

## 3.1 현재 상황

- 많은 기업 내부 시스템이 Servlet action API, jQuery DOM 조작, 화면 전용 비정규화 테이블로 운영 중이다.
- 화면별 JS/CSS 중복, DAO 직접 호출, 수동 JSON 직렬화가 공존한다.
- 리팩토링 시 "무엇이 문제인지" 코드 레벨 증거 없이 감으로만 설명하는 경우가 많다.
- 본 저장소는 그 레거시를 **의도적으로 재현**하고, 3단계 리팩토링 스냅샷과 나란히 둔다.

## 3.2 핵심 문제

| 문제 ID | 문제 내용 | 영향을 받는 사용자 | 현재 영향 |
| --- | --- | --- | --- |
| P-001 | action 파라미터 기반 Servlet API (`?action=list`) | 프론트·백엔드 개발자 | REST 계약 부재, 확장·테스트 어려움 |
| P-002 | 필드 변경마다 개별 UPDATE API + 전체 목록 재조회 | 입출고 담당자(화면 사용자) | API/DB 왕복 과다, 응답 지연 |
| P-003 | 화면 전용 비정규화 테이블 (`TB_WAREHOUSE_IO_SCREEN`) | DBA·백엔드 | 데이터 중복, 도메인 모델 부재 |
| P-004 | 투자자 이름 검색 O(n) 선형 탐색 | 투자자 조회 사용자 | 데이터 증가 시 검색 성능 저하 |
| P-005 | 5개 화면 중 3개는 전용 백엔드 없이 warehouse API 재사용 | 기획·개발 | 도메인 경계 모호, 유지보수 혼란 |
| P-006 | 리팩터 모듈 YAML에 DB 설정만 있고 실제는 인메모리 | 신규 온보딩 개발자 | 설계 의도와 런타임 불일치 |

## 3.3 해결해야 할 핵심 질문

```
사용자가 어떤 상황에서 멈추는가?
→ 입출고 등록 시 필드마다 저장·전체 목록 새로고침으로 UX·성능 저하

어떤 데이터가 흩어져 있는가?
→ 화면 컬럼이 단일 테이블에 평탄화, 투자자는 DB 없이 메모리만 사용

어떤 판단을 시스템이 도와줘야 하는가?
→ 레거시 vs 리팩터 구조 선택 시 trade-off를 코드로 비교할 수 있어야 함

어떤 업무를 자동화해야 하는가?
→ chatty update → 일괄 저장, 선형 검색 → 인덱스/DB 검색, 전체 reload → 부분 갱신
```

---

# 4. 목표와 성공 지표

## 4.1 제품 목표

| 목표 ID | 목표 | 설명 |
| --- | --- | --- |
| G-001 | 레거시 문제 재현 | Servlet+jQuery+Oracle 안티패턴을 실행 가능한 샘플로 제공 |
| G-002 | 단계적 현대화 증명 | Vanilla JS → Vue → Next.js + Spring Boot API 진화 경로 제시 |
| G-003 | 구조·알고리즘 개선 | 자료구조(Map 인덱스)·알고리즘(호출 횟수·검색) 개선 전후 비교 |
| G-004 | 포트폴리오 설명 가능성 | 면접·코드리뷰에서 Before/After를 FR·API·NFR로 설명 가능 |

## 4.2 성공 지표

| 지표 ID | 성공 지표 | 측정 방식 | 목표값 |
| --- | --- | --- | --- |
| M-001 | 레거시 샘플 실행 성공률 | `javac` + `java LegacySampleServer` 후 index 접속 | 100% |
| M-002 | 입출고 필드 저장 시 API 호출 수 | 모달 1건 완성 시 네트워크 요청 수 | MVP 개선 후: ≤3회 (현재 ~26회 수준에서 감소) |
| M-003 | 투자자 이름 검색 시간복잡도 | 코드·문서 기준 | O(n) → O(1)~O(log n) 구조 도입 (인덱스 Map 또는 SQL) |
| M-004 | 리팩터 모듈 실행 성공률 | `mvn spring-boot:run` / `next dev` | 각 단계 100% |
| M-005 | 문서-코드 일치율 | README·PRD·실제 API 경로 대조 | 불일치 0건 (P0 범위) |

---

# 5. 사용자 및 이해관계자

## 5.1 주요 사용자

| 사용자 유형 | 설명 | 주요 니즈 |
| --- | --- | --- |
| Primary User | 백엔드/풀스택 개발자 (학습·포트폴리오) | 레거시 코드 읽기, 리팩토링 포인트 파악, 개선 코드 작성 |
| Secondary User | 기술 면접관·리뷰어 | 구조 이해, trade-off 평가, 개선 의도 확인 |
| Admin | (MVP 범위 외) 운영자 | 권한·설정·감사 로그 |

## 5.2 이해관계자

| 이해관계자 | 관심사 | 영향도 |
| --- | --- | --- |
| 기획/PM | 범위, 우선순위, MVP | 높음 |
| 개발 | FR 명확성, API, DB, 예외처리 | 높음 |
| 디자인 | 화면 흐름 (레거시 jQuery vs Vue/Next) | 중간 |
| QA | 수용 기준, 실행 검증 | 높음 |
| 운영/CS | (MVP 범위 외) | 낮음 |

---

# 6. 범위 정의

## 6.1 In Scope

| 기능 ID | 기능명 | 설명 | 우선순위 |
| --- | --- | --- | --- |
| F-001 | 입출고 목록 조회 | warehouse 목록 API + RealGrid/폴백 테이블 | P0 |
| F-002 | 입출고 상세·등록·수정·삭제 | 레거시 action API + 모달 필드 저장 | P0 |
| F-003 | 투자자 목록·검색·등록·상세 | investor action API | P0 |
| F-004 | 공통 그리드 화면 | order-status, settlement, dashboard — warehouse API 재사용 | P1 |
| F-005 | 리팩터 1단계 | Spring Boot + Vanilla JS REST | P0 |
| F-006 | 리팩터 2단계 | Spring Boot + Vue 3 | P1 |
| F-007 | 리팩터 3단계 | Next.js + Spring Boot API 분리 | P1 |
| F-008 | 성능 개선 (chatty API 제거) | 필드 저장 후 전체 reload 제거, 일괄 저장 | P0 |
| F-009 | 검색 구조 개선 | 투자자 이름 검색 인덱스/DB LIKE | P1 |
| F-010 | 문서화 | PRD, README, CODE-WALKTHROUGH | P0 |

## 6.2 Out of Scope

| 제외 항목 | 제외 이유 | 향후 검토 시점 |
| --- | --- | --- |
| 인증/인가 (로그인, RBAC) | MVP 범위 초과 | v1.0 |
| 실 Oracle DB 운영 연동 (리팩터) | 데모는 인메모리 우선 | v1.0 |
| order-status/settlement/dashboard 전용 백엔드 | 레거시 재현 목적상 API 재사용 유지 | v1.1 |
| CI/CD, Docker, K8s | 인프라 범위 초과 | v1.1 |
| 단위/통합 테스트 자동화 | MVP 이후 | v1.0 |
| RealGrid 라이선스·상용 배포 | 학습용 CDN 로드 | - |

---

# 7. 가정, 제약, 의존성

## 7.1 Assumptions

| ID | 가정 | 검증 방법 |
| --- | --- | --- |
| A-001 | 사용자는 Java 8+(레거시), Java 17+(리팩터) 환경 보유 | README 실행 가이드 |
| A-002 | Oracle 미연결 시에도 메모리 fallback으로 데모 가능 | DB 없이 서버 기동 테스트 |
| A-003 | 포트폴리오 목적이 "완성된 상용 서비스"가 아님 | PRD 범위 명시 |
| A-004 | 샘플 데이터는 2~3건 수준으로 충분 | 시드 데이터 확인 |

## 7.2 Constraints

| ID | 제약사항 | 영향 |
| --- | --- | --- |
| C-001 | 레거시 모듈은 의도적 안티패턴 유지 필요 | 리팩터와 diff 비교 가치 |
| C-002 | `mvn` 없이 레거시는 `javac`만으로 실행 | Servlet stub 클래스 사용 |
| C-003 | 리팩터 DB는 MariaDB/MySQL/PostgreSQL YAML만 존재 | 실 DB 마이그레이션은 후속 |
| C-004 | 레거시 API 계약 변경 시 프론트 동시 수정 필요 | 하위 호환 또는 버전 분리 검토 |

## 7.3 Dependencies

| ID | 의존 대상 | 설명 | 담당 |
| --- | --- | --- | --- |
| D-001 | jQuery 3.7.1 CDN | 레거시 프론트 | 외부 |
| D-002 | RealGrid CDN | 입출고 그리드 (없으면 폴백 테이블) | 외부 |
| D-003 | Spring Boot 3.3.5 | 리팩터 백엔드 | Maven |
| D-004 | Next.js 14 | 3단계 프론트 | npm |

---

# 8. 사용자 시나리오 / 유스케이스

## 8.1 대표 사용자 시나리오

```
개발자는 면접/학습 준비 상황에서 레거시 입출고 화면의 문제점(필드별 API, 전체 reload)을 확인하기 위해
legacy-servlet-jquery-oracle을 실행하고 warehouse-io 화면에서 등록·수정 흐름을 재현한다.
시스템은 action 기반 API와 비정규화 테이블 접근을 보여주고,
개발자는 refactor-springboot-vanilla의 REST 일괄 등록과 비교하여 개선 포인트를 문서화한다.
```

## 8.2 유스케이스 목록

| UC ID | 유스케이스명 | Actor | Trigger | Main Flow | 결과 |
| --- | --- | --- | --- | --- | --- |
| UC-001 | 입출고 목록 조회 | 사용자 | 조회 버튼 / 화면 진입 | 1. list API 2. 그리드 렌더 | 목록 표시 |
| UC-002 | 입출고 신규 등록 (레거시) | 사용자 | 등록 버튼 | 1. createEmptyRow 2. 모달 오픈 3. 필드별 update | DRAFT→ACTIVE 행 저장 |
| UC-003 | 입출고 상세 조회 | 사용자 | 행 클릭 | 1. detail API 2. 모달 바인딩 | 상세 표시 |
| UC-004 | 입출고 삭제 | 사용자 | 삭제 버튼 | 1. delete API 2. 목록 갱신 | 소프트 삭제 |
| UC-005 | 투자자 검색·등록 | 사용자 | 검색/등록 | 1. list?name= 2. create | 투자자 CRUD |
| UC-006 | 리팩터 입출고 일괄 등록 | 개발자 | POST /api/warehouses | 1. JSON body 2. save | 1회 API로 등록 |
| UC-007 | 레거시 vs 리팩터 비교 | 개발자 | 문서·코드 탐색 | 1. PRD·WALKTHROUGH 2. 모듈 실행 | 개선 근거 확보 |

---

# 9. 기능 요구사항

## 9.1 기능 요구사항 작성 규칙

```
FR-[번호]
시스템은 [사용자/조건]이 [행동]할 때 [결과]를 제공해야 한다.
```

## 9.2 기능 요구사항 목록

| ID | 기능명 | 요구사항 | 우선순위 | 관련 UC |
| --- | --- | --- | --- | --- |
| FR-001 | 입출고 목록 | 시스템은 삭제되지 않은(DELETED_YN='N') 입출고 행 목록을 ID 내림차순으로 반환해야 한다. | P0 | UC-001 |
| FR-002 | 입출고 상세 | 시스템은 warehouseIoId로 단건 상세를 반환해야 한다. | P0 | UC-003 |
| FR-003 | 입출고 빈 행 생성 | 시스템은 STATUS='DRAFT'인 빈 행을 생성하고 ID를 반환해야 한다. | P0 | UC-002 |
| FR-004 | 입출고 필드 수정 | 시스템은 단일 컬럼 UPDATE를 지원해야 한다. (레거시 호환) | P0 | UC-002 |
| FR-005 | 입출고 일괄 수정/등록 | 시스템은 리팩터 모듈에서 JSON body 1회로 입출고 행을 저장해야 한다. | P0 | UC-006 |
| FR-006 | 입출고 삭제 | 시스템은 물리 삭제 대신 DELETED_YN='Y' 소프트 삭제를 수행해야 한다. | P0 | UC-004 |
| FR-007 | 투자자 목록·검색 | 시스템은 이름 키워드(부분 일치)로 투자자 목록을 반환해야 한다. | P0 | UC-005 |
| FR-008 | 투자자 등록 | 시스템은 투자자 정보를 저장하고 investorId를 반환해야 한다. | P0 | UC-005 |
| FR-009 | DB fallback | 시스템은 Oracle 연결 실패 시 메모리 저장소로 자동 전환해야 한다. | P0 | UC-001 |
| FR-010 | 공통 그리드 | 시스템은 order-status/settlement/dashboard에서 warehouse 목록 API를 재사용해야 한다. | P1 | UC-001 |
| FR-011 | 필드 저장 후 부분 갱신 | 시스템은 필드 1건 저장 후 전체 목록 API를 호출하지 않고 해당 행만 갱신해야 한다. | P0 | UC-002 |
| FR-012 | 투자자 검색 인덱스 | 시스템은 이름 검색 시 O(n) 전체 순회 대신 인덱스 구조 또는 SQL 검색을 사용해야 한다. | P1 | UC-005 |

## 9.3 기능 상세

### FR-011. 필드 저장 후 부분 갱신 (P0 개선)

#### 설명

레거시 `warehouse-io.js`에서 `updateField` 성공 시 `loadList()`를 호출하여 O(n) 목록 API가 필드마다 반복 호출된다. 이를 제거하고 그리드/테이블의 해당 행만 갱신한다.

#### 입력값

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| action | string | Y | updateWarehouseName 등 |
| id | long | Y | warehouseIoId |
| [fieldKey] | string/number | Y | 변경된 필드 값 |

#### 처리 규칙

| 규칙 ID | 규칙 |
| --- | --- |
| BR-001 | update API 성공 시 목록 API를 호출하지 않는다 |
| BR-002 | RealGrid 모드에서는 `dataProvider` 해당 행만 갱신 |
| BR-003 | 폴백 테이블 모드에서는 `tr[data-id]` 셀만 갱신 |

#### 예외 상황

| 예외 ID | 상황 | 시스템 동작 |
| --- | --- | --- |
| E-001 | update 실패 | alert, 목록 유지 |
| E-002 | id 없음 | 저장 요청 차단 |

#### 수용 기준

```
Given 입출고 모달이 열려 있고 warehouseIoId가 존재한다
When 사용자가 WAREHOUSE_NAME 필드를 변경한다
Then updateWarehouseName API 1회만 호출되고 loadList는 호출되지 않으며 화면 해당 행만 갱신된다
```

---

### FR-005. 입출고 일괄 등록 (리팩터)

#### 설명

레거시의 createEmptyRow + 13회 update를 `POST /api/warehouses` 1회로 대체한다.

#### 입력값

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| warehouseName | string | N | 창고명 |
| productCode | string | N | 상품코드 |
| productName | string | N | 상품명 |
| productCategory | string | N | 분류 |
| inQty | number | N | 입고 수량 |
| outQty | number | N | 출고 수량 |
| currentStock | number | N | 현재 재고 |
| clientName | string | N | 거래처 |
| status | string | N | 상태 (기본 ACTIVE) |

#### 수용 기준

```
Given 리팩터 Spring Boot가 8081~8083에서 실행 중이다
When 클라이언트가 POST /api/warehouses에 JSON body를 전송한다
Then 1회 요청으로 warehouseIoId가 발급되고 { result: OK, warehouseIoId }가 반환된다
```

---

# 10. 비기능 요구사항

| ID | 구분 | 요구사항 | 기준 |
| --- | --- | --- | --- |
| NFR-001 | 성능 | 입출고 필드 1건 저장 시 네트워크 왕복 | FR-011 적용 후 1회 (기존 2회 이상) |
| NFR-002 | 성능 | 투자자 이름 검색 (1,000건 가정) | 인덱스 적용 후 선형 스캔 제거 |
| NFR-003 | 성능 | 목록 API (MVP) | 전체 반환 허용, v1.0에서 페이징 |
| NFR-004 | 보안 | SQL 컬럼 UPDATE | enum 화이트리스트 (`WarehouseIoColumn`) |
| NFR-005 | 보안 | 인증/인가 | MVP 제외 (Out of Scope) |
| NFR-006 | 사용성 | 레거시 서버 기동 | README 3줄 이내 명령으로 실행 |
| NFR-007 | 호환성 | 브라우저 | Chrome 최신, jQuery 3.7 |
| NFR-008 | 가용성 | DB 미연결 | 메모리 fallback으로 100% 데모 가능 |
| NFR-009 | 유지보수 | 모듈 폴더명 | 기술 스택 기반 명명 (servlet-jquery-oracle 등) |
| NFR-010 | 문서 | PRD-SRS 추적 | FR ID가 API·테스트에 매핑 가능 |

---

# 11. 데이터 요구사항

## 11.1 주요 데이터

| 데이터 객체 | 설명 | 주요 필드 |
| --- | --- | --- |
| WarehouseIo (입출고) | 창고 입출고 화면 행 | warehouseIoId, warehouseName, productCode, inQty, outQty, currentStock, status, deletedYn |
| Investor (투자자) | 투자자 화면 행 | investorId, investorName, investorGrade, totalAmount, lastProductName, screenMemo |

## 11.2 데이터 관계 초안

```
(MVP: 도메인 간 FK 없음 — 화면 단위 독립)

WarehouseIo 1:N (화면 필드) — 단일 비정규화 행
Investor     — 독립 메모리/향후 TB_INVESTOR
```

## 11.3 DB 설계로 넘길 항목

| 항목 | 설명 |
| --- | --- |
| Entity 후보 | `TB_WAREHOUSE_IO_SCREEN`, `TB_INVESTOR` (신규) |
| Key 후보 | `WAREHOUSE_IO_ID` PK, `INVESTOR_ID` PK |
| Index 후보 | `(DELETED_YN, WAREHOUSE_IO_ID DESC)`, `INVESTOR_NAME` (검색) |
| 상태값 | DRAFT, ACTIVE / deletedYn Y,N |
| 이력 관리 | UPDATED_AT, DELETED_YN (소프트 삭제) |

---

# 12. 화면 및 사용자 흐름

## 12.1 주요 화면

| 화면 ID | 화면명 | 목적 |
| --- | --- | --- |
| UI-001 | index.html | 메뉴 진입 |
| UI-002 | warehouse-io.html | 입출고 CRUD (핵심) |
| UI-003 | investor.html | 투자자 CRUD |
| UI-004 | order-status.html | warehouse API 재사용 목록 |
| UI-005 | settlement.html | warehouse API 재사용 목록 |
| UI-006 | dashboard.html | warehouse API 재사용 목록 |
| UI-007 | refactor pages | Vanilla / Vue / Next.js 각 단계 |

## 12.2 사용자 흐름 (입출고 레거시)

```
진입 (index)
 ↓
입출고 목록 (loadList)
 ↓
행 클릭 → 상세 모달 (detail)
 ↓
등록 → createEmptyRow → 모달
 ↓
필드 change → update* (FR-011: 부분 갱신)
 ↓
삭제 → delete → 목록 갱신
```

## 12.3 화면별 기능 매핑

| 화면 | 관련 기능 | 관련 API | 비고 |
| --- | --- | --- | --- |
| warehouse-io | FR-001~006, FR-011 | GET /warehouse?action=* | 레거시 |
| investor | FR-007, FR-008 | GET /investor?action=* | 메모리 |
| order-status 등 | FR-010 | GET /api/warehouse-io | list만 |
| refactor warehouse | FR-005 | GET/POST /api/warehouses | REST |

---

# 13. API 요구사항 초안

## 13.1 레거시 (action 기반)

| API ID | Method | Endpoint | 목적 | 관련 FR |
| --- | --- | --- | --- | --- |
| API-L01 | GET | /warehouse?action=list | 목록 | FR-001 |
| API-L02 | GET | /warehouse?action=detail&id={id} | 상세 | FR-002 |
| API-L03 | GET | /warehouse?action=createEmptyRow | 빈 행 | FR-003 |
| API-L04 | GET | /warehouse?action=update{Field}&id={id} | 필드 수정 | FR-004 |
| API-L05 | GET | /warehouse?action=delete&id={id} | 삭제 | FR-006 |
| API-L06 | GET | /api/warehouse-io | 목록 alias | FR-010 |
| API-L07 | GET | /investor?action=list&name={q} | 투자자 검색 | FR-007 |
| API-L08 | GET | /investor?action=create&... | 투자자 등록 | FR-008 |

## 13.2 리팩터 (REST)

| API ID | Method | Endpoint | 목적 | 관련 FR |
| --- | --- | --- | --- | --- |
| API-R01 | GET | /api/warehouses | 입출고 목록 | FR-001 |
| API-R02 | GET | /api/warehouses/{id} | 상세 | FR-002 |
| API-R03 | POST | /api/warehouses | 일괄 등록 | FR-005 |
| API-R04 | GET | /api/investors?name={q} | 투자자 검색 | FR-007 |
| API-R05 | POST | /api/investors | 투자자 등록 | FR-008 |

응답 공통 (리팩터 조회): `{ "data": T }` (`ApiResponse`)

---

# 14. 정책 및 비즈니스 규칙

| 정책 ID | 정책명 | 설명 | 관련 FR |
| --- | --- | --- | --- |
| POL-001 | 소프트 삭제 | 입출고 삭제는 DELETED_YN='Y'만 설정 | FR-006 |
| POL-002 | DRAFT 등록 | 신규 행은 createEmptyRow 후 STATUS=DRAFT | FR-003 |
| POL-003 | DB fallback | JDBC 실패 시 메모리 저장소 사용, UI 중단 없음 | FR-009 |
| POL-004 | 이름 검색 | 투자자 이름 부분 일치 (contains) | FR-007 |
| POL-005 | 레거시 API 호환 | FR-011·FR-005 적용 시 레거시 action URL은 deprecated 전까지 유지 | FR-004 |

---

# 15. 우선순위

| 우선순위 | 의미 | 본 프로젝트 예시 |
| --- | --- | --- |
| P0 | MVP 필수 | FR-011 chatty API 제거, FR-005 일괄 저장, 문서화 |
| P1 | P0 이후 | FR-012 검색 인덱스, Vue/Next 단계, 공통 그리드 |
| P2 | 후순위 | 페이징, 실DB 연결, 테스트 자동화 |
| P3 | 제외 가능 | 인증, CI/CD, 전용 도메인 백엔드 3화면 |

---

# 16. 릴리즈 계획

| 단계 | 목표 | 포함 기능 |
| --- | --- | --- |
| MVP (현재+α) | 레거시 재현 + 개선 FR-011·FR-005 명세 | P0, PRD, 폴더 리네임, SQL/구조 정리 |
| v1.0 | 운영에 가까운 리팩터 | P0+P1, FR-012, 페이징, 실DB |
| v1.1 | 도메인 분리 | order/settlement/dashboard 전용 API |
| v2.0 | 상용 수준 | 인증, CI, 모니터링 |

---

# 17. 리스크 및 대응 방안

| 리스크 ID | 리스크 | 영향도 | 대응 방안 |
| --- | --- | --- | --- |
| R-001 | 레거시 개선 시 "레거시다움" 상실 | 중간 | 개선 전 스냅샷·PRD에 Before 명시 |
| R-002 | 리팩터 YAML vs 인메모리 불일치 | 중간 | Repository 주석 + PRD P-006 |
| R-003 | RealGrid CDN 실패 | 낮음 | 폴백 테이블 이미 구현 |
| R-004 | FR-011 적용 시 프론트·백 동시 수정 | 높음 | 수용 기준·단계적 PR |
| R-005 | WarehouseIo 모델 필드 불일치 (레거시 vs 리팩터) | 중간 | v1.0에서 record 필드 통일 |

---

# 18. 오픈 질문

| 질문 ID | 질문 | 담당자 | 상태 |
| --- | --- | --- | --- |
| Q-001 | FR-011만 레거시에 적용할지, 일괄 저장 API도 레거시에 추가할지? | 개발 | Open |
| Q-002 | 투자자 검색: 메모리 Map 인덱스 vs 실DB LIKE? | 개발 | Open |
| Q-003 | 리팩터 모듈 실DB 연결 우선순위 (MariaDB/MySQL/PostgreSQL)? | 개발 | Open |
| Q-004 | order-status 등 3화면을 별도 도메인으로 분리할 시점? | PM | Open |

---

# 19. SRS로 넘길 요구사항

| PRD 항목 | SRS 변환 항목 |
| --- | --- |
| FR-001~012 | 시스템 기능 요구사항 (입출고·투자자·성능) |
| NFR-001~010 | 품질 속성 (성능·보안·가용성) |
| UC-001~007 | 상세 시나리오·시퀀스 |
| §11 데이터 | ERD, `schema.mock.sql`, 인덱스 DDL |
| §13 API | OpenAPI/Swagger 명세 |
| POL-001~005 | 비즈니스 룰 명세 |
| FR 수용 기준 | Given-When-Then 테스트 케이스 |

---

# 20. 구현 우선순위 (PRD → 개발 백로그)

자료구조·알고리즘 논의를 반영한 **권장 구현 순서**:

| 순서 | 백로그 | PRD | 성격 |
| --- | --- | --- | --- |
| 1 | `updateField` 후 `loadList()` 제거 | FR-011 | 알고리즘 (호출 횟수) |
| 2 | 레거시 일괄 저장 action 추가 (선택) | FR-005 | 알고리즘 + API |
| 3 | 투자자 이름 검색 Map 인덱스 | FR-012 | 자료구조 + 알고리즘 |
| 4 | `WarehouseIo` record 필드 통일 | §11 | 자료구조 |
| 5 | 목록 페이징 | NFR-003 v1.0 | 알고리즘 |
| 6 | DB 인덱스 DDL | §11.3 | 자료구조 (DB) |

---

# 21. 품질 체크리스트

| 체크 항목 | 확인 |
| --- | --- |
| 사용자가 누구인지 명확한가? | ☑ |
| 해결할 문제가 구체적인가? | ☑ |
| 목표와 성공 지표가 연결되는가? | ☑ |
| MVP 범위가 P0 중심으로 정리되었는가? | ☑ |
| 기능 요구사항이 FR 단위로 나뉘었는가? | ☑ |
| 각 기능에 유스케이스가 연결되는가? | ☑ |
| 기능별 입력값, 처리 규칙, 예외 상황이 있는가? | ☑ (FR-011, FR-005 상세) |
| 비기능 요구사항이 있는가? | ☑ |
| 주요 데이터와 DB 후보가 보이는가? | ☑ |
| API 초안으로 변환 가능한가? | ☑ |
| SRS로 넘길 수 있을 정도로 모호하지 않은가? | ☑ |
| Out of Scope가 명확한가? | ☑ |
| 리스크와 오픈 질문이 정리되었는가? | ☑ |

---

*다음 단계: 본 PRD의 P0 백로그(FR-011)부터 구현 → SRS/API 명세서 분리*
