# LegacyPoc Portfolio SDD

## Software Design Description

---

## 1. 문서 정보

| 항목 | 내용 |
| --- | --- |
| 문서명 | LegacyPoc Portfolio SDD |
| 버전 | v0.1 |
| 작성일 | 2026-07-01 |
| 작성자 | Portfolio Team |
| 관련 문서 | [PRD.md](./PRD.md) v0.1, [SRS.md](./SRS.md) v0.1 |
| 대상 릴리즈 | MVP |

---

## 2. 설계 목적

이 문서는 [SRS.md](./SRS.md)의 요구사항을 **실제 코드 구조·알고리즘·데이터 설계**로 변환한다.

```text
PRD.md
 ↓
SRS.md
 ↓
SDD.md (본 문서)
 ↓
Code
 ├ legacy-servlet-jquery-oracle/
 ├ refactor-springboot-vanilla/
 ├ refactor-springboot-vue/
 └ refactor-nextjs-springboot-api/
```

SDD가 답하는 질문:

| 질문 | 본 문서 섹션 |
| --- | --- |
| 어떤 모듈로 나눌 것인가? | §4, §5, §6 |
| 각 모듈의 책임은? | §4, §9 |
| 데이터는 어떤 구조로? | §7 |
| API 요청/응답은? | §8 |
| 주요 알고리즘은? | §10 |
| 예외·트랜잭션은? | §12, §13 |
| 상태 전이는? | §11 |

---

## 3. 설계 범위

| 기능 ID | 기능명 | 설명 | 관련 SRS |
| --- | --- | --- | --- |
| F-001 | 입출고 CRUD | 목록·상세·등록·필드수정·삭제 | FR-001~006 |
| F-002 | 입출고 UI 최적화 | 필드 저장 후 부분 갱신 | FR-011, NFR-001 |
| F-003 | 투자자 CRUD | 검색·등록·상세 | FR-007, FR-008 |
| F-004 | 공통 그리드 | warehouse 목록 API 재사용 | FR-010 |
| F-005 | DB fallback | JDBC 실패 시 메모리 전환 | FR-009, BR-011 |
| F-006 | 리팩터 REST | Spring Boot 계층 + 일괄 등록 | FR-005, NFR-007 |
| F-007 | 검색 인덱스 (P1) | 투자자 O(n) → 인덱스 | FR-012 |

**제외:** 인증(Auth), 실 DB(리팩터), 페이징(v1.0), order/settlement/dashboard 전용 API

---

## 4. 전체 구조

### 4.1 레거시 (`legacy-servlet-jquery-oracle`)

```text
Browser (jQuery)
 ↓
LegacySampleServer (:8080)
 ├ /warehouse, /investor  → Servlet.processAction()
 └ /, /static             → 정적 파일
 ↓
WarehouseIoServlet / InvestorServlet
 ↓ (서비스 계층 없음 — 의도적)
WarehouseIoDao / InvestorDao
 ↓
Oracle JDBC  ──SQLException──→  LinkedHashMap 메모리
```

| 계층 | 클래스 | 책임 |
| --- | --- | --- |
| Server | `LegacySampleServer` | HTTP 라우팅, 쿼리 파싱, 정적 파일 |
| Servlet | `WarehouseIoServlet`, `InvestorServlet` | action 분기, JSON 응답 조립 |
| DAO | `WarehouseIoDao`, `InvestorDao` | SQL/메모리 CRUD |
| DTO | `WarehouseIoDto`, `InvestorDto` | 행 데이터 전달 |
| Util | `DbConnection`, `LegacyJsonWriter` | DB 연결, 수동 JSON |
| Enum | `WarehouseIoColumn` | UPDATE 컬럼 화이트리스트 |

### 4.2 리팩터 (`refactor-springboot-*`)

```text
Browser (Vanilla / Vue / Next.js)
 ↓
@RestController
 ↓
@Service
 ↓
@Repository (LinkedHashMap + AtomicLong)
```

| 계층 | 책임 |
| --- | --- |
| Controller | HTTP 메서드·경로, `ApiResponse` 래핑 |
| Service | null 기본값 정규화, 비즈니스 흐름 |
| Repository | 인메모리 CRUD, 시퀀스 발급 |
| Model (record) | 도메인 데이터 |
| DTO | `CreateWarehouseRequest`, `CreateInvestorRequest` |
| Common | `ApiResponse<T>` |

### 4.3 아키텍처 비교 (Before / After)

```text
[Before]  Browser → Servlet → DAO → DB
[After]   Browser → Controller → Service → Repository → (DB v1.0)
```

---

## 5. 패키지 구조

### 5.1 레거시

```text
legacy-servlet-jquery-oracle/
├── jquery-webapp/src/main/webapp/
│   ├── pages/           # warehouse-io, investor, order-status, ...
│   └── static/js/     # warehouse-io.js, investor.js, common-table.js
├── servlet-jdbc-backend/src/main/java/com/example/legacy/
│   ├── LegacySampleServer.java
│   ├── servlet/
│   │   ├── WarehouseIoServlet.java
│   │   ├── InvestorServlet.java
│   │   ├── LegacyJsonWriter.java
│   │   └── RequestParamReader.java
│   ├── dao/
│   │   ├── WarehouseIoDao.java
│   │   ├── WarehouseIoColumn.java
│   │   └── InvestorDao.java
│   ├── dto/
│   │   ├── WarehouseIoDto.java
│   │   └── InvestorDto.java
│   └── util/
│       └── DbConnection.java
└── oracle-schema/
    ├── schema.mock.sql
    └── sample-data.sql
```

### 5.2 리팩터 (공통)

```text
refactor-*/springboot-*/src/main/java/com/example/refactor/
├── Refactor*Application.java
├── common/
│   └── ApiResponse.java
└── feature/
    ├── warehouse/
    │   ├── controller/WarehouseController.java
    │   ├── service/WarehouseService.java
    │   ├── repository/WarehouseRepository.java
    │   ├── model/WarehouseIo.java
    │   └── dto/CreateWarehouseRequest.java
    └── investor/
        ├── controller/InvestorController.java
        ├── service/InvestorService.java
        ├── repository/InvestorRepository.java
        ├── model/Investor.java
        └── dto/CreateInvestorRequest.java
```

### 5.3 Next.js 프론트 (3단계)

```text
refactor-nextjs-springboot-api/nextjs-front/
├── src/app/investor/page.js
├── src/app/warehouse/page.js
└── src/lib/api.js          # apiGet, apiPost → :8083
```

---

## 6. 모듈 설계

| 모듈 | 스택 | 책임 | 관련 FR |
| --- | --- | --- | --- |
| **Warehouse Legacy** | Servlet+DAO | action API, 필드별 UPDATE, 소프트 삭제 | FR-001~006, FR-011 |
| **Investor Legacy** | Servlet+DAO | 메모리 CRUD, 이름 검색 | FR-007, FR-008 |
| **Warehouse Refactor** | Spring Boot | REST 목록/상세/일괄등록 | FR-001, FR-002, FR-005 |
| **Investor Refactor** | Spring Boot | REST 검색/등록 | FR-007, FR-008 |
| **LegacyGrid** | jQuery | 공통 컬럼·목록 렌더 (3화면) | FR-010 |
| **Fallback** | DAO 내부 | JDBC 실패 → 메모리 | FR-009 |

---

## 7. 데이터 설계

### 7.1 Entity / DTO

| 이름 | 레거시 | 리팩터 | 설명 |
| --- | --- | --- | --- |
| WarehouseIo | `WarehouseIoDto` (15필드) | `WarehouseIo` record (10필드) | 입출고 행 |
| Investor | `InvestorDto` (6필드) | `Investor` record (6필드) | 투자자 행 |

> **설계 부채 (R-002):** 리팩터 `WarehouseIo`는 manager, memo, deletedYn 미포함 — v1.0 통일 예정.

### 7.2 DB 테이블

#### `TB_WAREHOUSE_IO_SCREEN` (Oracle / schema.mock.sql)

| 컬럼 | 타입 | PK/FK | 설명 |
| --- | --- | --- | --- |
| WAREHOUSE_IO_ID | BIGINT | PK | 자동 증가 |
| WAREHOUSE_NAME ~ MEMO | VARCHAR/INT | | 화면 컬럼 (비정규화) |
| DELETED_YN | CHAR(1) | | Y/N |
| UPDATED_AT | TIMESTAMP | | 수정 시각 |

#### `TB_INVESTOR` (v1.0 예정, MVP는 메모리)

| 컬럼 | 타입 | PK | 설명 |
| --- | --- | --- | --- |
| INVESTOR_ID | BIGINT | PK | 2000+ 시퀀스 |
| INVESTOR_NAME | VARCHAR(100) | INDEX | 검색 대상 |
| INVESTOR_GRADE | VARCHAR(20) | | VIP/GOLD/SILVER |
| TOTAL_AMOUNT | BIGINT | | |
| LAST_PRODUCT_NAME | VARCHAR(100) | | |
| SCREEN_MEMO | VARCHAR(500) | | |

### 7.3 인메모리 자료구조

| 저장소 | 타입 | 키 | 용도 |
| --- | --- | --- | --- |
| `MEMORY_TABLE` | `LinkedHashMap<Long, WarehouseIoDto>` | warehouseIoId | fallback + 삽입순서 |
| `INVESTOR_TABLE` | `LinkedHashMap<Long, InvestorDto>` | investorId | 투자자 전용 |
| `actions` | `HashMap<String, Function>` | action명 | Servlet O(1) 분기 |
| refactor `table` | `LinkedHashMap<Long, T>` | id | Repository |

### 7.4 인덱스 (v1.0 DDL)

```sql
CREATE INDEX IDX_WIO_ACTIVE
  ON TB_WAREHOUSE_IO_SCREEN (DELETED_YN, WAREHOUSE_IO_ID DESC);

CREATE INDEX IDX_INVESTOR_NAME
  ON TB_INVESTOR (INVESTOR_NAME);
```

### 7.5 ERD (MVP)

```text
┌─────────────────────────┐     ┌─────────────────┐
│ TB_WAREHOUSE_IO_SCREEN  │     │ Investor (메모리) │
│ PK: WAREHOUSE_IO_ID     │     │ PK: investorId  │
│ (비정규화 단일 테이블)    │     │ (독립, FK 없음)  │
└─────────────────────────┘     └─────────────────┘
```

---

## 8. API 설계

### 8.1 레거시 action API

| API ID | Method | Endpoint | Handler |
| --- | --- | --- | --- |
| API-L01 | GET | `/warehouse?action=list` | `actions.get("list")` |
| API-L02 | GET | `/warehouse?action=detail&id=` | `detail` |
| API-L03 | GET | `/warehouse?action=createEmptyRow` | `createEmptyRow` |
| API-L04 | GET | `/warehouse?action=update*&id=` | `update*` → `updateColumn` |
| API-L05 | GET | `/warehouse?action=delete&id=` | `delete` |
| API-L06 | GET | `/api/warehouse-io` | list alias |
| API-L07 | GET | `/investor?action=list&name=` | `InvestorDao.list` |
| API-L08 | GET | `/investor?action=create&...` | `InvestorDao.create` |

### 8.2 리팩터 REST API

| API ID | Method | Endpoint | Controller 메서드 |
| --- | --- | --- | --- |
| API-R01 | GET | `/api/warehouses` | `list()` |
| API-R02 | GET | `/api/warehouses/{id}` | `detail(id)` |
| API-R03 | POST | `/api/warehouses` | `create(body)` |
| API-R04 | GET | `/api/investors?name=` | `list(name)` |
| API-R05 | POST | `/api/investors` | `create(body)` |

### 8.3 응답 형식

**레거시 목록/상세:**

```json
{ "data": [ { "warehouseIoId": 1001, "...": "..." } ] }
```

**레거시 수정:**

```json
{ "result": "OK", "updated": 1 }
```

**리팩터 조회 (`ApiResponse`):**

```json
{ "data": [ { "warehouseIoId": 1001, "warehouseName": "서울1창고" } ] }
```

**리팩터 등록:**

```json
{ "result": "OK", "warehouseIoId": 1003 }
```

### 8.4 오류 형식 (레거시 MVP)

```json
{ "result": "FAIL", "message": "Unknown action" }
```

리팩터 v1.0에서 HTTP status + errorCode 표준화 예정 (§13).

---

## 9. 클래스 설계

### 9.1 Warehouse (레거시)

| 구분 | 클래스 | 책임 |
| --- | --- | --- |
| Server | `LegacySampleServer` | `/warehouse` context 등록 |
| Servlet | `WarehouseIoServlet` | `buildActions()` Map, `processAction()` |
| DAO | `WarehouseIoDao` | SQL 상수, `updateColumn`, fallback |
| Enum | `WarehouseIoColumn` | 컬럼명 + DTO `apply()` |
| DTO | `WarehouseIoDto` | 15필드 getter/setter |
| Util | `LegacyJsonWriter` | `warehouseList`, `warehouseDetail` |
| Util | `DbConnection` | Oracle JDBC URL 하드코딩 |

### 9.2 Warehouse (리팩터)

| 구분 | 클래스 | 책임 |
| --- | --- | --- |
| Controller | `WarehouseController` | `@GetMapping`, `@PostMapping` |
| Service | `WarehouseService` | `safe()`, null→0, `create()` |
| Repository | `WarehouseRepository` | `findAll`, `findById`, `save` |
| Model | `WarehouseIo` | Java record |
| DTO | `CreateWarehouseRequest` | POST body |

### 9.3 Investor

| 구분 | 레거시 | 리팩터 |
| --- | --- | --- |
| Servlet/Controller | `InvestorServlet` | `InvestorController` |
| DAO/Repository | `InvestorDao` (메모리) | `InvestorRepository` |
| DTO/Model | `InvestorDto` | `Investor` record |

### 9.4 프론트 (입출고 핵심)

| 파일 | 책임 |
| --- | --- |
| `warehouse-io.js` | RealGrid/폴백, 모달, `updateField`, `loadList` |
| `common-table.js` | `LegacyGrid.initPage`, 공통 columns |
| `warehouse.js` / `warehouse-vue.js` | 리팩터 fetch/Vue |
| `nextjs .../warehouse/page.js` | React state + `apiPost` |

---

## 10. 주요 로직 / 알고리즘

### 10.1 action 분기 (Servlet)

```
입력: action (String), params (Map)
처리:
1. action이 null/empty이면 "list"로 설정          — O(1)
2. actions.get(action)으로 핸들러 조회              — O(1) HashMap
3. handler.apply(params) 실행
4. JSON 문자열 반환
출력: response body
```

**복잡도:** O(1) — 기존 if-else 체인 O(n) 대비 개선됨 (NFR-006)

---

### 10.2 입출고 목록 조회

```
입력: (없음)
처리:
1. PreparedStatement(SQL_LIST) 실행
   WHERE DELETED_YN='N' ORDER BY WAREHOUSE_IO_ID DESC
2. ResultSet 순회 → mapRow() → List           — O(n)
3. SQLException 시 listMemory() fallback
   - Map.values() 순회, deleted 필터, cloneDto   — O(n)
출력: List<WarehouseIoDto>
```

---

### 10.3 필드 단위 UPDATE

```
입력: id, WarehouseIoColumn column, value
처리:
1. sql = "UPDATE ... SET {column.sqlName()} = ? WHERE WAREHOUSE_IO_ID = ?"
2. JDBC executeUpdate                            — O(1) PK 조건
3. SQLException 시 MEMORY_TABLE.get(id).apply()  — O(1)
출력: updated row count (0 or 1)
```

**보안:** `column`은 enum만 허용 — 동적 문자열 삽입 방지 (BR-010)

---

### 10.4 필드 저장 후 부분 UI 갱신 (FR-011 — 설계 대상)

```
입력: warehouseIoId, fieldKey, newValue
처리 (프론트):
1. updateField(action, key, value) → AJAX 1회
2. 성공 시:
   - loadList() 호출하지 않음                    — BR-005
   - RealGrid: dataProvider에서 id 매칭 행 찾기   — O(n) 그리드 행 수
     → setValue(field, newValue)                 — O(1)
   - 폴백: tr[data-id] 내 td 텍스트 갱신         — O(1)
출력: 화면 해당 셀만 변경
```

**Before:** update 1회 + list 1회 = **2회 네트워크 왕복**  
**After:** update 1회 = **1회** (NFR-001)

---

### 10.5 리팩터 일괄 등록

```
입력: CreateWarehouseRequest (JSON)
처리:
1. Service: null → "" / 0 정규화
2. new WarehouseIo(null, fields...)
3. Repository.save:
   - id == null → sequence.incrementAndGet()    — O(1)
   - table.put(id, saved)                       — O(1)
출력: { result: OK, warehouseIoId }
```

**Before (레거시):** createEmptyRow + 13× update = **최대 14회 API**  
**After:** POST 1회

---

### 10.6 투자자 이름 검색

**현재 (MVP):**

```
입력: keyword
처리:
  for each investor in table.values():           — O(n)
    if name.contains(keyword): add to result
출력: List<Investor>
```

**설계 (FR-012, P1):**

```
옵션 A — 메모리 인덱스:
  Map<String, List<Long>> nameIndex
  검색: keyword → 후보 id 목록 → O(1) get      — 접두사 한정

옵션 B — SQL:
  SELECT * FROM TB_INVESTOR
  WHERE INVESTOR_NAME LIKE '%' || ? || '%'     — 인덱스 시 O(log n)~O(n)
```

---

### 10.7 DB fallback

```
try:
  JDBC 연결 및 쿼리
catch SQLException:
  메모리 저장소 동일 연산
  예외를 클라이언트에 전파하지 않음
```

---

## 11. 상태 설계

### 11.1 WarehouseIo.status

| 상태 | 설명 | 전이 |
| --- | --- | --- |
| DRAFT | `createEmptyRow` 직후 빈 행 | → ACTIVE (필드 입력 후) |
| ACTIVE | 정상 사용 행 | — |

### 11.2 WarehouseIo.deletedYn

| 값 | 설명 |
| --- | --- |
| N | 목록에 표시 |
| Y | 소프트 삭제, 목록 제외 |

```text
[생성] createEmptyRow → DRAFT, deletedYn=N
[삭제] delete → deletedYn=Y (물리 삭제 없음)
```

---

## 12. 트랜잭션 설계

MVP는 **단일 연산 단위**, 명시적 트랜잭션 없음.

| 유스케이스 | 트랜잭션 | 실패 시 (MVP) |
| --- | --- | --- |
| 필드 1건 UPDATE | 단일 UPDATE | updated=0 또는 fallback |
| createEmptyRow | 단일 INSERT | 메모리 fallback |
| delete | 단일 UPDATE (soft) | updated=0 |
| 리팩터 create | Map put 1건 | (예외 없음) |
| 등록+일정 (v1.0) | — | Out of Scope |

**v1.0:** `@Transactional` on Service, Oracle 커넥션 풀

---

## 13. 예외 처리 설계

### 13.1 레거시

| 상황 | 처리 클래스 | 동작 |
| --- | --- | --- |
| unknown action | `WarehouseIoServlet` | FAIL JSON |
| SQLException | `WarehouseIoDao` | catch → 메모리 fallback |
| AJAX error | `warehouse-io.js` | `alert()` |
| RealGrid 없음 | `warehouse-io.js` | `isFallbackMode=true` |

### 13.2 Error Code (v1.0 리팩터)

| Error Code | HTTP | 설명 |
| --- | --- | --- |
| UNKNOWN_ACTION | 200 | 레거시 호환 FAIL JSON |
| RESOURCE_NOT_FOUND | 404 | id 없음 |
| INVALID_REQUEST | 400 | body 검증 실패 |
| INTERNAL_ERROR | 500 | 미처리 예외 |

MVP: `GlobalExceptionHandler` 미구현

---

## 14. 보안 설계

| 항목 | MVP 설계 | v1.0 |
| --- | --- | --- |
| 인증 | 없음 | JWT |
| 인가 | 없음 | Role |
| SQL Injection | `WarehouseIoColumn` enum | + PreparedStatement |
| XSS | 수동 JSON escape | Jackson |
| CORS | Next→API 로컬만 | `@CrossOrigin` |

---

## 15. 시퀀스 다이어그램 (핵심)

### 15.1 레거시 필드 수정 (현재)

```text
User → warehouse-io.js: change event
  → GET /warehouse?action=updateWarehouseName&id=&warehouseName=
  → WarehouseIoServlet.processAction
  → WarehouseIoDao.updateColumn
  → Oracle or MEMORY_TABLE
  → {result:OK}
  → warehouse-io.js: loadList()  ← FR-011에서 제거 대상
  → GET /warehouse?action=list
```

### 15.2 FR-011 적용 후

```text
User → updateField
  → GET updateWarehouseName
  → DAO update
  → {result:OK}
  → patchGridRow(id, field, value)   ← list 호출 없음
```

### 15.3 리팩터 일괄 등록

```text
User → POST /api/warehouses + JSON
  → WarehouseController.create
  → WarehouseService.create (normalize)
  → WarehouseRepository.save
  → {result:OK, warehouseIoId}
  → loadRows() (1회)
```

---

## 16. 테스트 설계

| TC ID | 대상 | 검증 | FR |
| --- | --- | --- | --- |
| TC-001 | WarehouseIoDao.list | deleted 제외, DESC 정렬 | FR-001 |
| TC-002 | detail(1001) | 15필드 반환 | FR-002 |
| TC-003 | createEmptyRow | DRAFT id | FR-003 |
| TC-004 | updateColumn | 컬럼 반영 | FR-004 |
| TC-005 | POST /api/warehouses | 1회 등록 | FR-005 |
| TC-006 | delete | list 미포함 | FR-006 |
| TC-007 | investor list?name= | 부분 일치 | FR-007 |
| TC-008 | DB 미연결 기동 | 메모리 목록 | FR-009 |
| TC-009 | Network 탭 | update 후 list 0회 | FR-011 |
| TC-010 | 코드 리뷰 | enum 외 UPDATE 없음 | NFR-004 |

---

## 17. 개발로 넘길 항목

| SDD 항목 | 산출물 | 경로 |
| --- | --- | --- |
| 레거시 Servlet | `WarehouseIoServlet` | `servlet-jdbc-backend/.../servlet/` |
| DAO + enum | `WarehouseIoDao`, `WarehouseIoColumn` | `.../dao/` |
| FR-011 프론트 | `patchGridRow()` | `jquery-webapp/.../warehouse-io.js` |
| REST Controller | `WarehouseController` | `refactor-*/.../controller/` |
| DDL | schema + index | `oracle-schema/schema.mock.sql` |
| FR-012 검색 | `NameIndex` or SQL | `InvestorDao` / Repository |
| TC 수동 | curl + DevTools | SRS §13.3 |

---

## 18. SRS 추적성

| SRS FR | SDD 섹션 | 클래스/알고리즘 |
| --- | --- | --- |
| FR-001 | §10.2 | `SQL_LIST`, `listMemory` |
| FR-004 | §10.3 | `updateColumn`, `WarehouseIoColumn` |
| FR-005 | §10.5 | `WarehouseService.create` |
| FR-009 | §10.7 | try/catch in DAO |
| FR-011 | §10.4, §15.2 | `patchGridRow` (구현 예정) |
| FR-012 | §10.6 | NameIndex 설계 |
| NFR-006 | §10.1 | `HashMap` action dispatch |

---

## 19. 체크리스트

| 체크 항목 | 확인 |
| --- | --- |
| SRS 요구사항과 설계가 연결되는가? | ☑ |
| Controller / Service / Repository 책임이 분리되었는가? | ☑ (리팩터) |
| 레거시 Servlet→DAO 의도적 생략이 문서화되었는가? | ☑ |
| Entity와 DTO가 구분되었는가? | ☑ |
| API 요청/응답이 정의되었는가? | ☑ |
| DB 테이블과 관계가 정의되었는가? | ☑ |
| 주요 알고리즘·복잡도가 정리되었는가? | ☑ |
| 예외·fallback 처리가 정의되었는가? | ☑ |
| FR-011 Before/After 시퀀스가 있는가? | ☑ |
| 테스트케이스로 검증 가능한가? | ☑ |

---

## 20. 변경 이력

| 버전 | 일자 | 내용 |
| --- | --- | --- |
| v0.1 | 2026-07-01 | SRS 기반 최초 SDD 작성 |

---

*다음 단계: FR-011 구현 (`warehouse-io.js` partial update) 또는 OpenAPI 명세 분리*
