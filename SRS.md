# LegacyPoc Portfolio SRS

## Software Requirements Specification

---

## 0. 문서 목적

이 문서는 [PRD.md](./PRD.md)에서 정의한 제품 요구사항을 **개발·테스트·설계**에 사용 가능한 시스템 요구사항으로 구체화한다.

```text
PRD (LegacyPoc Portfolio PRD v0.1)
 ↓
SRS (본 문서 v0.1)
 ↓
SDD / 설계문서
 ├ Architecture (레거시 vs 리팩터 3단계)
 ├ ERD (TB_WAREHOUSE_IO_SCREEN, TB_INVESTOR)
 ├ DB Schema (oracle-schema/schema.mock.sql)
 ├ API Specification (action / REST)
 ├ Class Diagram (Servlet→DAO / Controller→Service→Repository)
 ├ Sequence Diagram (등록·필드수정·검색)
 └ Algorithm (호출 횟수, 검색 복잡도)
 ↓
Development
 ↓
Test (TC-xxx)
 ↓
Deploy (로컬 데모)
```

---

# 1. 문서 기본 정보

| 항목 | 내용 |
| --- | --- |
| 문서명 | LegacyPoc Portfolio SRS |
| 버전 | v0.1 |
| 작성일 | 2026-07-01 |
| 작성자 | Portfolio Team |
| 제품/서비스명 | LegacyPoc Portfolio |
| 대상 릴리즈 | MVP |
| 문서 상태 | Draft |
| 관련 PRD | [PRD.md](./PRD.md) v0.1 |
| 관련 설계문서 | `legacy-servlet-jquery-oracle/CODE-WALKTHROUGH.md`, 각 refactor 모듈 README |

---

# 2. 소개

## 2.1 목적

이 SRS 문서의 목적은 다음과 같다.

* PRD의 제품 요구사항을 **시스템이 수행해야 할 동작**으로 변환한다.
* 레거시 모듈(`legacy-servlet-jquery-oracle`)과 리팩터 모듈(3단계)의 **인터페이스·데이터·예외**를 정의한다.
* FR/NFR/DR/BR/API를 고유 ID로 관리하고 테스트케이스와 추적 가능하게 한다.
* SDD(ERD, API 명세, 시퀀스 다이어그램) 작성의 입력 문서로 사용한다.

## 2.2 범위

본 문서는 LegacyPoc Portfolio **MVP** 범위의 시스템 요구사항을 정의한다.

**포함 범위:**

* 입출고(WarehouseIo) 목록·상세·등록·필드수정·삭제 (레거시 action API)
* 투자자(Investor) 목록·검색·등록·상세 (레거시 action API)
* 공통 그리드 화면의 warehouse 목록 API 재사용
* 리팩터 모듈 REST API (Spring Boot + Vanilla/Vue/Next.js)
* DB 미연결 시 메모리 fallback
* P0 성능 개선: FR-011 (필드 저장 후 부분 UI 갱신)

**제외 범위:**

* 인증/인가 (로그인, JWT, RBAC)
* 리팩터 모듈 실 DB 영구 저장 (MVP는 인메모리)
* order-status / settlement / dashboard 전용 백엔드
* CI/CD, 컨테이너 배포
* 자동화 테스트 스위트 (수동 TC만 정의)

## 2.3 용어 정의

| 용어 | 정의 |
| --- | --- |
| 사용자 | 레거시/리팩터 화면을 조작하는 학습자·데모 사용자 |
| 개발자 | 저장소를 실행·분석·개선하는 Primary User (PRD 기준) |
| 시스템 | 본 SRS의 레거시 서버 또는 리팩터 Spring Boot API |
| action API | `?action=list` 형태의 Servlet 쿼리 파라미터 기반 API |
| REST API | `/api/warehouses` 등 HTTP 메서드·경로 기반 API |
| FR | Functional Requirement |
| NFR | Non-Functional Requirement |
| BR | Business Rule |
| DR | Data Requirement |
| 소프트 삭제 | `DELETED_YN='Y'` 설정, 물리 DELETE 미수행 |
| fallback | Oracle JDBC 실패 시 `LinkedHashMap` 메모리 저장소 사용 |

---

# 3. 전체 시스템 설명

## 3.1 시스템 개요

LegacyPoc Portfolio는 백엔드 개발자가 **Servlet+jQuery+Oracle 레거시 패턴**과 **Spring Boot 기반 현대화 결과**를 동일 도메인(입출고·투자자)으로 비교할 수 있도록 하는 교육/포트폴리오 시스템이다.

시스템은 다음을 제공한다.

1. **레거시 스택** — `LegacySampleServer` (:8080), action API, jQuery UI
2. **리팩터 1단계** — Spring Boot + Vanilla JS (:8081)
3. **리팩터 2단계** — Spring Boot + Vue 3 (:8082)
4. **리팩터 3단계** — Spring Boot API (:8083) + Next.js (:3000)

## 3.2 사용자 유형

| 사용자 유형 | 설명 | 주요 권한 (MVP) |
| --- | --- | --- |
| 데모 사용자 | 화면에서 CRUD 수행 | 전 기능 조회·등록·수정·삭제 (인증 없음) |
| 개발자 | 코드 실행·분석·개선 | 저장소 전체 접근, 로컬 서버 기동 |
| 관리자 | (MVP 범위 외) | - |
| 시스템 | 내부 fallback·ID 발급 | JDBC 실패 시 메모리 전환, 시퀀스 증가 |

## 3.3 운영 환경

| 구분 | 레거시 | 리팩터 |
| --- | --- | --- |
| Frontend | jQuery 3.7, RealGrid(CDN), HTML/CSS | Vanilla JS / Vue 3 CDN / Next.js 14 |
| Backend | Java 8+, `com.sun.net.httpserver`, Servlet stub | Java 17, Spring Boot 3.3.5 |
| Database | Oracle (설정), 메모리 fallback | YAML만 존재, 런타임 인메모리 Map |
| 실행 포트 | 8080 | 8081 / 8082 / 8083 / 3000 |
| API 문서 | 본 SRS §10 | 본 SRS §10 |
| 인증 | 없음 (MVP) | 없음 (MVP) |

---

# 4. 시스템 컨텍스트

## 4.1 시스템 흐름

### 레거시

```text
사용자 (브라우저)
 ↓
jQuery (warehouse-io.js / investor.js)
 ↓
LegacySampleServer (:8080)
 ↓
WarehouseIoServlet / InvestorServlet  (action 분기)
 ↓
WarehouseIoDao / InvestorDao
 ↓
Oracle JDBC  ──(실패 시)──→  LinkedHashMap 메모리
```

### 리팩터

```text
사용자 (브라우저 / Next.js)
 ↓
fetch / Vue / React
 ↓
Spring Boot Controller
 ↓
Service
 ↓
Repository (LinkedHashMap + AtomicLong)
```

## 4.2 외부 시스템 연동

| 외부 시스템 | 연동 목적 | 방식 | 비고 |
| --- | --- | --- | --- |
| jQuery CDN | 레거시 DOM/AJAX | `<script>` 로드 | 필수 |
| RealGrid CDN | 입출고 그리드 | `<script>` 로드 | 실패 시 폴백 테이블 |
| Oracle DB | 입출고 영구 저장 | JDBC thin | 미연결 시 fallback |
| (없음) | 투자자 저장 | - | MVP는 메모리만 |

## 4.3 시스템 인터페이스

| 인터페이스 ID | 대상 | 설명 | 관련 FR |
| --- | --- | --- | --- |
| IF-001 | Browser ↔ LegacySampleServer | action query API, 정적 파일 | FR-001~008 |
| IF-002 | WarehouseIoDao ↔ Oracle | JDBC PreparedStatement | FR-001, FR-009 |
| IF-003 | Browser ↔ Spring Boot | REST JSON | FR-005, FR-007, FR-008 |
| IF-004 | Next.js ↔ Spring Boot API | `NEXT_PUBLIC_API_BASE` fetch | FR-005 |
| IF-005 | legacy JS ↔ DOM | RealGrid / table render | FR-011 |

---

# 5. 유스케이스

## 5.1 유스케이스 목록

| UC ID | 유스케이스명 | Actor | Trigger | 결과 |
| --- | --- | --- | --- | --- |
| UC-001 | 입출고 목록 조회 | 사용자 | 화면 진입 / 조회 버튼 | 그리드·테이블에 목록 표시 |
| UC-002 | 입출고 신규 등록 (레거시) | 사용자 | 등록 버튼 | DRAFT 행 생성 후 모달 편집 |
| UC-003 | 입출고 상세 조회 | 사용자 | 행 클릭 | 모달에 상세 바인딩 |
| UC-004 | 입출고 필드 수정 | 사용자 | input change | 단일 컬럼 UPDATE |
| UC-005 | 입출고 삭제 | 사용자 | 삭제 버튼 | 소프트 삭제 후 목록 갱신 |
| UC-006 | 투자자 검색 | 사용자 | 검색 버튼 | 이름 부분 일치 목록 |
| UC-007 | 투자자 등록 | 사용자 | 등록 버튼 | investorId 발급 |
| UC-008 | 리팩터 입출고 일괄 등록 | 개발자 | POST /api/warehouses | 1회 API로 행 저장 |
| UC-009 | 공통 그리드 목록 | 사용자 | order-status 등 진입 | warehouse 목록 표시 |
| UC-010 | DB fallback 동작 | 시스템 | JDBC SQLException | 메모리 저장소로 전환 |

## 5.2 유스케이스 상세

### UC-004. 입출고 필드 수정 (FR-004, FR-011)

| 항목 | 내용 |
| --- | --- |
| Actor | 사용자 |
| Trigger | 모달 input `change` 이벤트 |
| Pre-condition | `warehouseIoId`가 모달에 존재 |
| Post-condition | 해당 컬럼이 DB/메모리에 반영, UI는 해당 행만 갱신 (FR-011) |
| Related Requirements | FR-004, FR-011, NFR-001, API-L04 |

#### Main Flow

1. 사용자가 모달 필드 값을 변경한다.
2. 프론트는 `update{Field}` action과 `id`, 필드값을 쿼리로 전송한다.
3. `WarehouseIoServlet`이 action Map에서 핸들러를 O(1) 조회한다.
4. `WarehouseIoDao.updateColumn`이 `WarehouseIoColumn` enum 화이트리스트로 UPDATE를 수행한다.
5. 시스템은 `{"result":"OK","updated":1}` JSON을 반환한다.
6. 프론트는 **목록 API를 호출하지 않고** 그리드/테이블의 해당 행만 갱신한다.

#### Alternative Flow

| 단계 | 조건 | 시스템 동작 |
| --- | --- | --- |
| A-001 | Oracle 연결 실패 | 메모리 Map에서 동일 컬럼 갱신 후 OK 반환 |
| A-002 | RealGrid 미로드 | 폴백 HTML 테이블 셀만 갱신 |

#### Exception Flow

| 예외 ID | 예외 상황 | 시스템 동작 |
| --- | --- | --- |
| E-001 | `id` 누락 또는 0 | update 0건, 프론트 alert |
| E-002 | unknown action | `{"result":"FAIL","message":"Unknown action"}` |
| E-003 | 네트워크 오류 | 프론트 alert, UI 롤백 없음 |

---

### UC-008. 리팩터 입출고 일괄 등록

| 항목 | 내용 |
| --- | --- |
| Actor | 개발자 / 사용자 |
| Trigger | 등록 버튼 → `POST /api/warehouses` |
| Pre-condition | Spring Boot 리팩터 모듈 실행 중 |
| Post-condition | `warehouseIoId` 발급, 목록에 신규 행 표시 |
| Related Requirements | FR-005, API-R03, DR-001 |

#### Main Flow

1. 클라이언트가 JSON body를 POST한다.
2. `WarehouseController` → `WarehouseService.create`가 null 기본값을 정규화한다.
3. `WarehouseRepository.save`가 `AtomicLong`으로 ID를 발급한다.
4. 시스템은 `{ "result": "OK", "warehouseIoId": <id> }`를 반환한다.

---

# 6. 기능 요구사항

## 6.1 기능 요구사항 작성 규칙

```text
FR-[번호]
시스템은 [조건]에서 [동작]을 수행해야 한다.
```

## 6.2 기능 요구사항 목록

| ID | 기능명 | 요구사항 | 우선순위 | UC |
| --- | --- | --- | --- | --- |
| FR-001 | 입출고 목록 | 시스템은 `DELETED_YN='N'`인 입출고 행을 `WAREHOUSE_IO_ID` 내림차순으로 반환해야 한다. | P0 | UC-001 |
| FR-002 | 입출고 상세 | 시스템은 `warehouseIoId`로 단건 상세 JSON을 반환해야 한다. | P0 | UC-003 |
| FR-003 | 빈 행 생성 | 시스템은 `STATUS='DRAFT'`인 빈 입출고 행을 생성하고 `warehouseIoId`를 반환해야 한다. | P0 | UC-002 |
| FR-004 | 필드 단위 수정 | 시스템은 허용된 단일 컬럼에 대해 UPDATE를 수행해야 한다. | P0 | UC-004 |
| FR-005 | 일괄 등록 | 시스템은 리팩터 모듈에서 JSON body 1회로 입출고 행을 저장해야 한다. | P0 | UC-008 |
| FR-006 | 소프트 삭제 | 시스템은 입출고 삭제 시 `DELETED_YN='Y'`만 설정해야 한다. | P0 | UC-005 |
| FR-007 | 투자자 검색 | 시스템은 이름 키워드 부분 일치로 투자자 목록을 반환해야 한다. | P0 | UC-006 |
| FR-008 | 투자자 등록 | 시스템은 투자자 정보를 저장하고 `investorId`를 반환해야 한다. | P0 | UC-007 |
| FR-009 | DB fallback | 시스템은 Oracle JDBC 실패 시 메모리 저장소로 자동 전환해야 한다. | P0 | UC-010 |
| FR-010 | 공통 API 재사용 | 시스템은 `/api/warehouse-io`로 입출고 목록 alias를 제공해야 한다. | P1 | UC-009 |
| FR-011 | 부분 UI 갱신 | 시스템은 필드 1건 저장 성공 후 프론트가 전체 목록 API를 호출하지 않아도 해당 행 UI를 갱신할 수 있어야 한다. | P0 | UC-004 |
| FR-012 | 검색 인덱스 | 시스템은 투자자 이름 검색 시 전체 순회 O(n) 대신 인덱스 Map 또는 SQL LIKE를 사용해야 한다. | P1 | UC-006 |

---

## 6.3 기능 요구사항 상세

### FR-001. 입출고 목록 조회

#### 설명

삭제되지 않은 입출고 화면 행 전체를 조회한다. DB 연결 시 SQL, 실패 시 메모리 Map 순회.

#### 입력값

| 필드명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| action | string | Y | `list` |

#### 처리 규칙

| 규칙 ID | 규칙 |
| --- | --- |
| BR-003 | `DELETED_YN='Y'` 행은 목록에서 제외 |
| BR-004 | 정렬: `WAREHOUSE_IO_ID DESC` |

#### 출력값

```json
{
  "data": [
    {
      "warehouseIoId": 1001,
      "warehouseName": "서울1창고",
      "productCode": "P-100",
      "productName": "산업용 센서",
      "productCategory": "전자부품",
      "inQty": 140,
      "outQty": 30,
      "currentStock": 110,
      "clientName": "한빛유통",
      "managerName": "김대리",
      "managerPhone": "010-1234-5678",
      "ioDate": "2026-04-27",
      "status": "ACTIVE",
      "memo": "우선 출고 요청"
    }
  ]
}
```

#### 수용 기준

```gherkin
Given 시스템에 삭제되지 않은 입출고 행이 2건 이상 존재하고
When 클라이언트가 GET /warehouse?action=list를 호출하면
Then data 배열에 DELETED_YN='N'인 행만 포함되고 warehouseIoId 내림차순이어야 한다.
```

---

### FR-004. 입출고 필드 단위 수정

#### 설명

레거시 호환을 위해 컬럼별 action API를 유지한다. `WarehouseIoColumn` enum이 허용 컬럼만 UPDATE한다.

#### 입력값

| 필드명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| action | string | Y | `updateWarehouseName` 등 13종 |
| id | long | Y | `warehouseIoId` |
| [field] | string/int | Y | action에 대응하는 필드값 |

#### 허용 action ↔ 컬럼 매핑

| action | 파라미터 키 | DB 컬럼 |
| --- | --- | --- |
| updateWarehouseName | warehouseName | WAREHOUSE_NAME |
| updateProductCode | productCode | PRODUCT_CODE |
| updateProductName | productName | PRODUCT_NAME |
| updateProductCategory | productCategory | PRODUCT_CATEGORY |
| updateInQty | inQty | IN_QTY |
| updateOutQty | outQty | OUT_QTY |
| updateCurrentStock | currentStock | CURRENT_STOCK |
| updateClientName | clientName | CLIENT_NAME |
| updateManagerName | managerName | MANAGER_NAME |
| updateManagerPhone | managerPhone | MANAGER_PHONE |
| updateIoDate | ioDate | IO_DATE |
| updateStatus | status | STATUS |
| updateMemo | memo | MEMO |

#### 출력값

```json
{ "result": "OK", "updated": 1 }
```

#### 예외 처리

| 예외 ID | 상황 | 시스템 동작 |
| --- | --- | --- |
| E-001 | 존재하지 않는 id | `updated: 0` |
| E-002 | enum 외 컬럼 시도 | 서버 코드에서 불가 (화이트리스트) |

---

### FR-011. 필드 저장 후 부분 UI 갱신

#### 설명

`warehouse-io.js`의 `updateField` 성공 콜백에서 `loadList()` 호출을 제거하고, 변경된 필드만 UI에 반영한다.

#### 처리 규칙

| 규칙 ID | 규칙 |
| --- | --- |
| BR-005 | update 성공 시 목록 API(`/warehouse?action=list`) 미호출 |
| BR-006 | RealGrid: `dataProvider`에서 `warehouseIoId` 매칭 행 필드 갱신 |
| BR-007 | 폴백 테이블: `tr[data-id='{id}']` 내 해당 `td` 텍스트 갱신 |

#### 수용 기준

```gherkin
Given 입출고 모달이 열려 있고 warehouseIoId=1001이며
When 사용자가 WAREHOUSE_NAME을 "부산3창고"로 변경하면
Then updateWarehouseName API는 1회만 호출되고
And list API는 호출되지 않으며
And 화면 행 1001의 창고명 셀만 "부산3창고"로 표시되어야 한다.
```

---

### FR-005. 리팩터 입출고 일괄 등록

#### 입력값 (Request Body)

| 필드명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| warehouseName | string | N | 창고명, null→`""` |
| productCode | string | N | 상품코드 |
| productName | string | N | 상품명 |
| productCategory | string | N | 분류 |
| inQty | int | N | null→0 |
| outQty | int | N | null→0 |
| currentStock | int | N | null→0 |
| clientName | string | N | 거래처 |
| status | string | N | 기본 ACTIVE |

#### 출력값

```json
{ "result": "OK", "warehouseIoId": 1003 }
```

---

### FR-007. 투자자 이름 검색

#### 입력값

| 필드명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| action | string | Y | `list` |
| name | string | N | 검색 키워드, 빈값이면 전체 |

#### 처리 규칙

| 규칙 ID | 규칙 |
| --- | --- |
| BR-008 | `investorName.contains(keyword)` 부분 일치 (MVP) |
| BR-009 | FR-012 적용 시: `Map<String, List<Long>>` 또는 SQL `LIKE` |

#### 수용 기준

```gherkin
Given 투자자 "김민수", "이서연"이 존재하고
When GET /investor?action=list&name=김 을 호출하면
Then data에 investorName에 "김"이 포함된 행만 반환되어야 한다.
```

---

# 7. 비기능 요구사항

## 7.1 비기능 요구사항 목록

| ID | 구분 | 요구사항 | 측정 기준 | 우선순위 |
| --- | --- | --- | --- | --- |
| NFR-001 | 성능 | 입출고 필드 1건 저장 시 클라이언트 네트워크 왕복 | FR-011 적용 후 update 1회만 (list 0회) | P0 |
| NFR-002 | 성능 | 투자자 검색 (1,000건) | FR-012 적용 후 O(n) 전체 순회 제거 | P1 |
| NFR-003 | 성능 | 목록 API 응답 (MVP) | 전체 반환 허용, n<100 | P2 |
| NFR-004 | 보안 | SQL UPDATE 컬럼 | `WarehouseIoColumn` enum만 허용 | P0 |
| NFR-005 | 보안 | 인증 | MVP 제외, 모든 API 익명 접근 | - |
| NFR-006 | 유지보수 | 레거시 action 분기 | `Map<String, Function>` O(1) 조회 | P0 |
| NFR-007 | 유지보수 | 리팩터 계층 | Controller → Service → Repository 분리 | P0 |
| NFR-008 | 가용성 | DB 미연결 | 서버 기동·목록 조회 100% 성공 (메모리) | P0 |
| NFR-009 | 사용성 | 레거시 실행 | README 3줄 이내 명령 | P0 |
| NFR-010 | 호환성 | 브라우저 | Chrome 최신, UTF-8 | P1 |
| NFR-011 | 문서 | 추적성 | PRD FR ↔ SRS FR ↔ API ↔ TC 1:1 매핑 | P0 |

---

# 8. 데이터 요구사항

## 8.1 주요 데이터 객체

| 데이터 ID | 데이터 객체 | 설명 | 관련 FR |
| --- | --- | --- | --- |
| DR-001 | WarehouseIo | 입출고 화면 행 (비정규화) | FR-001~006 |
| DR-002 | Investor | 투자자 화면 행 | FR-007, FR-008 |

## 8.2 데이터 필드 정의

### DR-001. WarehouseIo (`TB_WAREHOUSE_IO_SCREEN`)

| 필드명 | 타입 | 필수 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| warehouseIoId | BIGINT | Y | PK, AUTO | 고유 ID |
| warehouseName | VARCHAR(100) | N | | 창고명 |
| productCode | VARCHAR(40) | N | | 상품코드 |
| productName | VARCHAR(100) | N | | 상품명 |
| productCategory | VARCHAR(60) | N | | 분류 |
| inQty | INT | N | default 0 | 입고 수량 |
| outQty | INT | N | default 0 | 출고 수량 |
| currentStock | INT | N | default 0 | 현재 재고 |
| clientName | VARCHAR(100) | N | | 거래처 |
| managerName | VARCHAR(60) | N | | 담당자 |
| managerPhone | VARCHAR(30) | N | | 연락처 |
| ioDate | VARCHAR(20) | N | | 입출고일 (문자열) |
| status | VARCHAR(30) | N | DRAFT, ACTIVE | 상태 |
| memo | VARCHAR(500) | N | | 메모 |
| deletedYn | CHAR(1) | Y | Y/N, default N | 소프트 삭제 |
| updatedAt | TIMESTAMP | N | auto on update | 수정 시각 |

### DR-002. Investor (메모리 / 향후 `TB_INVESTOR`)

| 필드명 | 타입 | 필수 | 제약조건 | 설명 |
| --- | --- | --- | --- | --- |
| investorId | Long | Y | PK | 고유 ID (2000번대 시퀀스) |
| investorName | String | Y | max 100 | 이름 |
| investorGrade | String | N | VIP/GOLD/SILVER | 등급 |
| totalAmount | Long | N | ≥0 | 총 투자금 |
| lastProductName | String | N | | 최근 상품 |
| screenMemo | String | N | | 화면 메모 |

## 8.3 데이터 관계 초안

```text
(MVP — 도메인 간 FK 없음)

WarehouseIo  — 단일 비정규화 행 (화면 1:1)
Investor     — 독립 엔티티
```

## 8.4 DB 설계로 넘길 항목

| 항목 | 내용 |
| --- | --- |
| Entity | `TB_WAREHOUSE_IO_SCREEN`, `TB_INVESTOR` (v1.0) |
| PK | `WAREHOUSE_IO_ID`, `INVESTOR_ID` |
| Index | `IDX_WIO_ACTIVE (DELETED_YN, WAREHOUSE_IO_ID DESC)` |
| Index | `IDX_INVESTOR_NAME (INVESTOR_NAME)` — FR-012 |
| Enum | status: DRAFT, ACTIVE / deletedYn: Y, N |
| 삭제 정책 | Soft Delete (`DELETED_YN`) |
| 이력 | `UPDATED_AT` (입출고만) |

## 8.5 인메모리 자료구조 (런타임)

| 저장소 | 타입 | 연산 | 복잡도 |
| --- | --- | --- | --- |
| MEMORY_TABLE (Warehouse) | `LinkedHashMap<Long, Dto>` | get by id | O(1) |
| INVESTOR_TABLE | `LinkedHashMap<Long, Dto>` | list filter | O(n) → FR-012 |
| action handlers | `HashMap<String, Function>` | dispatch | O(1) |

---

# 9. 비즈니스 규칙

| BR ID | 규칙명 | 규칙 내용 | 관련 FR |
| --- | --- | --- | --- |
| BR-001 | 소프트 삭제 | 물리 DELETE 금지, `DELETED_YN='Y'`만 설정 | FR-006 |
| BR-002 | DRAFT 생성 | `createEmptyRow`는 STATUS='DRAFT', 빈 문자열·0으로 초기화 | FR-003 |
| BR-003 | 목록 필터 | 목록은 `DELETED_YN='N'`만 포함 | FR-001 |
| BR-004 | 목록 정렬 | `WAREHOUSE_IO_ID DESC` | FR-001 |
| BR-005 | 부분 갱신 | 필드 update 후 list API 미호출 | FR-011 |
| BR-006 | RealGrid 갱신 | dataProvider 행 단위 필드 업데이트 | FR-011 |
| BR-007 | 폴백 테이블 갱신 | tr[data-id] 셀 단위 업데이트 | FR-011 |
| BR-008 | 이름 검색 | 투자자 이름 `contains` 부분 일치 | FR-007 |
| BR-009 | 검색 최적화 | FR-012: 인덱스 Map 또는 SQL LIKE | FR-012 |
| BR-010 | 컬럼 화이트리스트 | UPDATE는 `WarehouseIoColumn` enum 값만 | FR-004, NFR-004 |
| BR-011 | DB fallback | SQLException 시 메모리 저장소, 예외 전파 없음 | FR-009 |
| BR-012 | ID 발급 | Warehouse: DB auto / 메모리 1000+, Investor: 2000+ | FR-003, FR-008 |

---

# 10. API 요구사항

## 10.1 API 목록 (레거시)

| API ID | Method | Endpoint | 목적 | FR |
| --- | --- | --- | --- | --- |
| API-L01 | GET | `/warehouse?action=list` | 입출고 목록 | FR-001 |
| API-L02 | GET | `/warehouse?action=detail&id={id}` | 상세 | FR-002 |
| API-L03 | GET | `/warehouse?action=createEmptyRow` | 빈 행 생성 | FR-003 |
| API-L04 | GET | `/warehouse?action=update{Field}&id={id}&...` | 필드 수정 | FR-004 |
| API-L05 | GET | `/warehouse?action=delete&id={id}` | 소프트 삭제 | FR-006 |
| API-L06 | GET | `/api/warehouse-io` | 목록 alias | FR-010 |
| API-L07 | GET | `/investor?action=list&name={q}` | 투자자 검색 | FR-007 |
| API-L08 | GET | `/investor?action=detail&id={id}` | 투자자 상세 | FR-008 |
| API-L09 | GET | `/investor?action=create&...` | 투자자 등록 | FR-008 |

## 10.2 API 목록 (리팩터)

| API ID | Method | Endpoint | 포트 | FR |
| --- | --- | --- | --- | --- |
| API-R01 | GET | `/api/warehouses` | 8081~8083 | FR-001 |
| API-R02 | GET | `/api/warehouses/{id}` | 8081~8083 | FR-002 |
| API-R03 | POST | `/api/warehouses` | 8081~8083 | FR-005 |
| API-R04 | GET | `/api/investors?name={q}` | 8081~8083 | FR-007 |
| API-R05 | POST | `/api/investors` | 8081~8083 | FR-008 |

## 10.3 API 상세

### API-L04. 입출고 필드 수정

| 항목 | 내용 |
| --- | --- |
| Method | GET |
| Endpoint | `/warehouse?action=updateWarehouseName&id=1001&warehouseName=부산3창고` |
| Auth | None (MVP) |
| Related FR | FR-004, FR-011 |

#### Response 200

```json
{ "result": "OK", "updated": 1 }
```

#### Error Response (레거시)

| 상황 | 응답 |
| --- | --- |
| unknown action | `{"result":"FAIL","message":"Unknown action"}` |
| id 없음 | `{"result":"OK","updated":0}` |

---

### API-R03. 입출고 일괄 등록

| 항목 | 내용 |
| --- | --- |
| Method | POST |
| Endpoint | `/api/warehouses` |
| Content-Type | `application/json` |
| Related FR | FR-005 |

#### Request Body

```json
{
  "warehouseName": "서울1창고",
  "productCode": "P-100",
  "productName": "산업용 센서",
  "productCategory": "전자부품",
  "inQty": 140,
  "outQty": 30,
  "currentStock": 110,
  "clientName": "한빛유통",
  "status": "ACTIVE"
}
```

#### Response 200

```json
{ "result": "OK", "warehouseIoId": 1003 }
```

#### Response (조회 API)

```json
{ "data": [ { "warehouseIoId": 1001, "warehouseName": "서울1창고", "...": "..." } ] }
```

---

# 11. 권한 및 보안 요구사항

## 11.1 권한 매트릭스 (MVP)

| 기능 | 데모 사용자 | 개발자 | 비고 |
| --- | --- | --- | --- |
| 목록/상세 조회 | O | O | 인증 없음 |
| 등록/수정/삭제 | O | O | 인증 없음 |
| 서버 기동 | - | O | 로컬 |

> MVP는 교육용으로 **SEC-001~003 미적용**. v1.0에서 JWT/세션 도입 예정.

## 11.2 보안 요구사항 (MVP 적용 분)

| ID | 요구사항 | 기준 |
| --- | --- | --- |
| SEC-001 | SQL UPDATE 컬럼은 enum 화이트리스트만 허용 | `WarehouseIoColumn` |
| SEC-002 | 수동 JSON 이스케이프 | `"` → `\"` (`LegacyJsonWriter`) |
| SEC-003 | (v1.0) 미인증 요청 차단 | 401 — Out of Scope |

---

# 12. 예외 및 오류 처리 요구사항

| Error ID | 상황 | HTTP/응답 | 시스템 동작 |
| --- | --- | --- | --- |
| ERR-001 | unknown action | 200 + FAIL JSON | `Unknown action` 메시지 |
| ERR-002 | JDBC 연결 실패 | 200 (정상 응답) | 메모리 fallback, 로그 없음(MVP) |
| ERR-003 | 존재하지 않는 id 상세 | 200 + `data:null` | null JSON |
| ERR-004 | update 대상 없음 | 200 + `updated:0` | |
| ERR-005 | 프론트 AJAX 실패 | - | `alert()` 표시 |
| ERR-006 | RealGrid CDN 실패 | - | `isFallbackMode=true`, 테이블 렌더 |
| ERR-007 | (리팩터 v1.0) 404 | 404 | 리소스 없음 |

---

# 13. 검증 및 수용 기준

## 13.1 검증 방법

| 검증 방법 | 적용 대상 |
| --- | --- |
| Test | API 응답, UI 동작 (curl, 브라우저) |
| Inspection | 코드 구조, enum 화이트리스트 |
| Analysis | 네트워크 탭 API 호출 횟수 (FR-011) |
| Demonstration | README 실행 후 화면 시연 |

## 13.2 요구사항별 검증 기준

| Requirement ID | 검증 방법 | 수용 기준 | TC |
| --- | --- | --- | --- |
| FR-001 | Test | list 응답에 삭제 행 미포함 | TC-001 |
| FR-002 | Test | detail id=1001 시 필드 전체 반환 | TC-002 |
| FR-003 | Test | createEmptyRow 후 DRAFT id 반환 | TC-003 |
| FR-004 | Test | updateWarehouseName 후 DB/메모리 반영 | TC-004 |
| FR-005 | Test | POST 1회로 warehouseIoId 발급 | TC-005 |
| FR-006 | Test | delete 후 list에 미표시 | TC-006 |
| FR-007 | Test | name=김 시 부분 일치만 | TC-007 |
| FR-009 | Test | DB 없이 서버 기동·list 성공 | TC-008 |
| FR-011 | Analysis | 필드 change 시 list API 0회 | TC-009 |
| NFR-004 | Inspection | enum 외 컬럼 UPDATE 코드 없음 | TC-010 |
| NFR-008 | Test | Oracle 미기동 시 데모 동작 | TC-008 |

## 13.3 테스트케이스 샘플

### TC-009. FR-011 부분 UI 갱신

| 항목 | 내용 |
| --- | --- |
| 사전조건 | LegacySampleServer :8080 실행, warehouse-io 화면 오픈 |
| 절차 | 1. 행 클릭 → 모달 오픈 2. 개발자도구 Network 탭 3. WAREHOUSE_NAME 변경 |
| 기대결과 | `updateWarehouseName` 1회, `action=list` 0회 |
| 통과/실패 | |

### TC-005. FR-005 일괄 등록

| 항목 | 내용 |
| --- | --- |
| 사전조건 | `refactor-springboot-vanilla` :8081 실행 |
| 절차 | `curl -X POST http://localhost:8081/api/warehouses -H 'Content-Type: application/json' -d '{"warehouseName":"테스트"}'` |
| 기대결과 | `result: OK`, `warehouseIoId` 숫자 반환 |
| 통과/실패 | |

---

# 14. 요구사항 추적성 매트릭스

| PRD FR | UC | SRS FR | API | DR | BR | TC |
| --- | --- | --- | --- | --- | --- | --- |
| FR-001 | UC-001 | FR-001 | API-L01, API-R01 | DR-001 | BR-003,4 | TC-001 |
| FR-002 | UC-003 | FR-002 | API-L02, API-R02 | DR-001 | - | TC-002 |
| FR-003 | UC-002 | FR-003 | API-L03 | DR-001 | BR-002,12 | TC-003 |
| FR-004 | UC-004 | FR-004 | API-L04 | DR-001 | BR-010 | TC-004 |
| FR-005 | UC-008 | FR-005 | API-R03 | DR-001 | - | TC-005 |
| FR-006 | UC-005 | FR-006 | API-L05 | DR-001 | BR-001 | TC-006 |
| FR-007 | UC-006 | FR-007 | API-L07, API-R04 | DR-002 | BR-008 | TC-007 |
| FR-008 | UC-007 | FR-008 | API-L08,09, API-R05 | DR-002 | BR-012 | - |
| FR-009 | UC-010 | FR-009 | - | DR-001 | BR-011 | TC-008 |
| FR-010 | UC-009 | FR-010 | API-L06 | DR-001 | - | - |
| FR-011 | UC-004 | FR-011 | API-L04 | - | BR-005~007 | TC-009 |
| FR-012 | UC-006 | FR-012 | API-L07 | DR-002 | BR-009 | - |
| NFR-001 | UC-004 | NFR-001 | API-L04 | - | BR-005 | TC-009 |
| NFR-004 | - | NFR-004 | API-L04 | DR-001 | BR-010 | TC-010 |

---

# 15. SDD로 넘길 설계 입력값

| SRS 항목 | SDD 산출물 | 경로/비고 |
| --- | --- | --- |
| FR-001~006 | WarehouseIoDao, WarehouseIoServlet | `servlet-jdbc-backend/...` |
| FR-005 | WarehouseController, Service, Repository | `refactor-*/...` |
| DR-001 | DDL, ERD | `oracle-schema/schema.mock.sql` |
| DR-002 | InvestorDao / InvestorRepository | 메모리 → v1.0 DDL |
| API-L* | Sequence Diagram (레거시) | 등록·필드수정 흐름 |
| API-R* | OpenAPI 3.0 초안 | Swagger (v1.0) |
| FR-011 | Frontend partial update spec | `warehouse-io.js` |
| FR-012 | Search index design | `Map<String,List<Long>>` or SQL |
| NFR-006 | Servlet action Map | `WarehouseIoServlet.buildActions()` |
| NFR-007 | Layered package | `feature/{investor,warehouse}/` |
| BR-* | Domain rules doc | Service 계층 |
| TC-* | QA Test Plan | `docs/test/` (v1.0) |

---

# 16. 리스크 및 오픈 이슈

## 16.1 리스크

| Risk ID | 리스크 | 영향도 | 대응 |
| --- | --- | --- | --- |
| R-001 | FR-011 적용 시 레거시 데모 특성 약화 | 중 | PRD Before/After 문서화 |
| R-002 | 리팩터 WarehouseIo 필드 < 레거시 DTO | 중 | v1.0 record 필드 통일 |
| R-003 | action API GET으로 수정 (멱등성 위반) | 낮 | 교육용으로 유지, REST가 대안 |
| R-004 | TC-009 수동 검증 의존 | 중 | v1.0 Playwright 도입 |

## 16.2 오픈 이슈

| Issue ID | 질문 | 담당 | 상태 |
| --- | --- | --- | --- |
| Q-001 | FR-011만 적용 vs 레거시 일괄 저장 action 추가? | 개발 | Open |
| Q-002 | FR-012: 메모리 Map 인덱스 vs Oracle LIKE? | 개발 | Open |
| Q-003 | 리팩터 실DB: MariaDB/MySQL/PostgreSQL 우선? | 개발 | Open |
| Q-004 | TC 자동화 도구 선정 시점? | QA | Open |

---

# 17. 변경 관리

| 버전 | 변경일 | 변경 내용 | 작성자 |
| --- | --- | --- | --- |
| v0.1 | 2026-07-01 | PRD 기반 최초 SRS 작성 | Portfolio Team |

---

# 18. SRS 품질 체크리스트

| 체크 항목 | 확인 |
| --- | --- |
| 요구사항이 “시스템은 ~해야 한다” 형식으로 작성되었는가? | ☑ |
| 요구사항이 하나의 문장에서 하나의 동작만 설명하는가? | ☑ |
| 기능 요구사항과 비기능 요구사항이 분리되었는가? | ☑ |
| 각 요구사항에 고유 ID가 있는가? | ☑ |
| 각 요구사항이 테스트 가능한가? | ☑ |
| 유스케이스와 기능 요구사항이 연결되는가? | ☑ |
| 기능 요구사항과 API가 연결되는가? | ☑ |
| 데이터 요구사항이 ERD/DB 설계로 넘어갈 수 있는가? | ☑ |
| 예외 상황과 오류 응답이 정의되었는가? | ☑ |
| 권한과 보안 요구사항이 정의되었는가? | ☑ (MVP 범위 명시) |
| 비즈니스 규칙이 별도로 정리되었는가? | ☑ |
| 요구사항 추적성 매트릭스가 작성되었는가? | ☑ |
| SDD로 넘길 설계 입력값이 정리되었는가? | ☑ |

---

# 19. 구현 백로그 (SRS → Development)

PRD §20과 동일 우선순위를 **개발 작업 단위**로 변환한다.

| 순서 | 작업 | SRS | 산출물 |
| --- | --- | --- | --- |
| 1 | `updateField` 후 `loadList()` 제거, 행 partial update | FR-011, TC-009 | `warehouse-io.js` |
| 2 | (선택) 레거시 `action=saveRow` 일괄 저장 | FR-005 확장 | Servlet + JS |
| 3 | Investor 이름 검색 인덱스 | FR-012, BR-009 | `InvestorDao`, Repository |
| 4 | WarehouseIo record 필드 통일 | DR-001 | refactor model |
| 5 | 페이징 API | NFR-003 v1.0 | Controller + SQL |
| 6 | DB 인덱스 DDL | §8.4 | `schema.mock.sql` |

---

*다음 단계: SDD (시퀀스 다이어그램, OpenAPI) 또는 P0 백로그(FR-011) 구현*
