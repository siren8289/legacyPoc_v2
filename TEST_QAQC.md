# LegacyPoc Portfolio — Test / QAQC 문서

> 관련 문서: [PRD.md](./PRD.md) · [SRS.md](./SRS.md) · [SDD.md](./SDD.md) · [ERD_API_DB_SCHEMA.md](./ERD_API_DB_SCHEMA.md)

---

## 1. 문서 정보

| 항목 | 내용 |
| --- | --- |
| 문서명 | LegacyPoc Portfolio Test / QAQC 문서 |
| 버전 | v0.1 |
| 작성일 | 2026-07-01 |
| 작성자 | Portfolio Team |
| 대상 릴리즈 | MVP |
| 관련 문서 | PRD v0.1, SRS v0.1, SDD v0.1, ERD/API/DB Schema v0.1 |

---

## 2. 문서 목적

개발된 레거시·리팩터 모듈이 SRS 수용 기준을 만족하는지 검증하기 위한 테스트 및 QA/QC 기준 문서다.

```text
PRD → SRS → SDD → ERD/API/DB Schema → Test/QAQC (본 문서) → Release
```

**검증 대상:**

* 레거시 `legacy-servlet-jquery-oracle` (:8080)
* 리팩터 `refactor-springboot-vanilla` (:8081)
* 리팩터 `refactor-springboot-vue` (:8082)
* 리팩터 `refactor-nextjs-springboot-api` (:8083 + :3000)

---

## 3. QA / QC 구분

| 구분 | 본 프로젝트 적용 | 예시 |
| --- | --- | --- |
| **QA** | 요구사항·설계 문서 정합성, 테스트 계획 수립 | PRD↔SRS↔TC 추적성, P0 우선순위 정의 |
| **QC** | 실행 결과가 기준을 만족하는지 검사 | curl API 검증, 브라우저 Network 탭, 코드 리뷰 |
| **Testing** | 기능·API·화면·DB fallback 실제 실행 | TC-001~015 수동 테스트 |

---

## 4. 테스트 범위

### 4.1 In Scope

| 범위 ID | 테스트 대상 | 설명 |
| --- | --- | --- |
| TS-001 | 기능 테스트 | 입출고·투자자 CRUD (FR-001~008) |
| TS-002 | API 테스트 | action API + REST API 요청/응답 |
| TS-003 | DB / fallback | Oracle 연결·미연결 시 메모리 동작 (FR-009) |
| TS-004 | 성능 패턴 | FR-011 필드 저장 후 list API 미호출 (NFR-001) |
| TS-005 | 예외 테스트 | unknown action, id 없음, RealGrid 폴백 |
| TS-006 | 화면 테스트 | warehouse-io, investor, 공통 그리드 3화면 |
| TS-007 | 모듈 실행 | javac, mvn spring-boot:run, next dev |
| TS-008 | 보안(부분) | WarehouseIoColumn enum 화이트리스트 (NFR-004) |

### 4.2 Out of Scope

| 제외 항목 | 제외 이유 | 향후 검토 |
| --- | --- | --- |
| 권한/인증 테스트 | MVP Out of Scope | v1.0 |
| 부하/스트레스 테스트 | PoC 규모 | v1.0 |
| 자동화 테스트 (JUnit/Playwright) | MVP 수동 TC만 | v1.0 |
| 크로스브라우저 전체 | Chrome 우선 | v1.0 |
| 보안 취약점 스캔 | 교육용 PoC | 운영 전 |
| Oracle 실 DB 통합 (필수) | fallback으로 대체 가능 | v1.0 |

---

## 5. 테스트 전략

| 테스트 유형 | 목적 | 수행 시점 | MVP |
| --- | --- | --- | --- |
| Manual API Test | curl/브라우저로 action·REST 검증 | 기능 완료 후 | **주력** |
| UI / Network Analysis | FR-011 호출 횟수 확인 | warehouse-io 수정 후 | P0 |
| Inspection | enum, 계층 분리 코드 리뷰 | PR 리뷰 시 | P0 |
| Smoke Test | 서버 기동·index 접속 | 배포/데모 전 | P0 |
| Demonstration | README 3줄 실행 시연 | 릴리즈 전 | P0 |
| Unit Test | Service, DAO 단위 | v1.0 | 제외 |
| E2E 자동화 | Playwright | v1.0 | 제외 |
| Regression | 수정 후 P0 TC 재실행 | FR-011 적용 후 | P0 |

---

## 6. 테스트 환경

| 항목 | 레거시 | 리팩터 |
| --- | --- | --- |
| OS | macOS / Linux / Windows | 동일 |
| Java | 8+ | 17+ |
| Frontend | jQuery 3.7, RealGrid CDN | Vanilla / Vue CDN / Next.js 14 |
| Backend | `LegacySampleServer` | Spring Boot 3.3.5 |
| Database | Oracle (선택), 메모리 fallback | 인메모리 Map |
| 포트 | 8080 | 8081, 8082, 8083, 3000 |
| API Tool | curl, Chrome DevTools Network | curl, Postman |
| 브라우저 | Chrome 최신 | Chrome 최신 |

### 기동 명령 (Smoke Test)

```bash
# 레거시
cd legacy-servlet-jquery-oracle
mkdir -p out && javac -encoding UTF-8 -d out servlet-jdbc-backend/src/main/java/**/*.java
java -cp out com.example.legacy.LegacySampleServer

# 리팩터 1단계
cd refactor-springboot-vanilla/springboot-refactor && mvn spring-boot:run

# 리팩터 3단계 API
cd refactor-nextjs-springboot-api/springboot-api && mvn spring-boot:run
```

---

## 7. 테스트 데이터

| 데이터 ID | 데이터명 | 설명 | 출처 |
| --- | --- | --- | --- |
| TD-W01 | 서울1창고 행 | warehouseIoId≈1001, ACTIVE | 메모리 seed / sample-data.sql |
| TD-W02 | 부산2창고 행 | warehouseIoId≈1002, ACTIVE | 메모리 seed |
| TD-I01 | 김민수 | VIP, 8.5억 | InvestorDao seed |
| TD-I02 | 이서연 | GOLD | InvestorDao seed |
| TD-I03 | 박준호 | SILVER | InvestorDao seed |
| TD-NEW | 신규 등록 데이터 | POST body / create action | 테스트 시 생성 |
| TD-DRAFT | 빈 행 | createEmptyRow 후 DRAFT | TC-003 생성 |

---

## 8. 테스트케이스 목록

| TC ID | 기능 | 테스트 항목 | FR | API | 우선순위 | 상태 |
| --- | --- | --- | --- | --- | --- | --- |
| TC-001 | 입출고 목록 | 삭제 행 제외, DESC 정렬 | FR-001 | API-L01 | P0 | Not Run |
| TC-002 | 입출고 상세 | id로 15필드 반환 | FR-002 | API-L02 | P0 | Not Run |
| TC-003 | 빈 행 생성 | DRAFT id 발급 | FR-003 | API-L03 | P0 | Not Run |
| TC-004 | 필드 수정 | update 후 값 반영 | FR-004 | API-L04a | P0 | Not Run |
| TC-005 | REST 일괄 등록 | POST 1회 warehouseIoId | FR-005 | API-R03 | P0 | Not Run |
| TC-006 | 소프트 삭제 | delete 후 list 미포함 | FR-006 | API-L05 | P0 | Not Run |
| TC-007 | 투자자 검색 | name 부분 일치 | FR-007 | API-L07 | P0 | Not Run |
| TC-008 | DB fallback | Oracle 없이 list 성공 | FR-009 | API-L01 | P0 | Not Run |
| TC-009 | 부분 UI 갱신 | update 후 list 0회 | FR-011 | API-L04 | P0 | Not Run |
| TC-010 | SQL 화이트리스트 | enum 외 UPDATE 없음 | NFR-004 | - | P0 | Not Run |
| TC-011 | unknown action | FAIL JSON | ERR-001 | - | P1 | Not Run |
| TC-012 | 공통 그리드 | /api/warehouse-io 목록 | FR-010 | API-L06 | P1 | Not Run |
| TC-013 | 투자자 등록 | create 후 id 반환 | FR-008 | API-L09 | P0 | Not Run |
| TC-014 | REST 투자자 검색 | GET /api/investors?name= | FR-007 | API-R04 | P1 | Not Run |
| TC-015 | Smoke | index.html 로드 | NFR-009 | - | P0 | Not Run |
| TC-016 | RealGrid 폴백 | CDN 실패 시 테이블 | ERR-006 | - | P2 | Not Run |
| TC-017 | 리팩터 3단계 | Next→API 연동 | FR-005 | API-R03 | P1 | Not Run |

---

## 9. 테스트케이스 상세

### TC-001. 입출고 목록 조회

| 항목 | 내용 |
| --- | --- |
| Test Case ID | TC-001 |
| 관련 요구사항 | FR-001, BR-003, BR-004 |
| 관련 API | API-L01 |
| 우선순위 | P0 |
| 테스트 유형 | API |
| 사전조건 | LegacySampleServer :8080 실행 |
| 테스트 데이터 | TD-W01, TD-W02 |

#### 테스트 절차

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | `curl -s "http://localhost:8080/warehouse?action=list"` | HTTP 200 |
| 2 | 응답 JSON 파싱 | `data` 배열 존재 |
| 3 | 각 항목 확인 | `deletedYn` 필드 없거나 목록에 삭제 행 미포함 |
| 4 | 순서 확인 | `warehouseIoId` 내림차순 |

#### curl 예시

```bash
curl -s "http://localhost:8080/warehouse?action=list" | head -c 300
```

---

### TC-004. 입출고 필드 수정

| 항목 | 내용 |
| --- | --- |
| Test Case ID | TC-004 |
| 관련 요구사항 | FR-004, BR-010 |
| 관련 API | API-L04a |
| 우선순위 | P0 |
| 테스트 유형 | API + DB(또는 메모리) |

#### 테스트 절차

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | `GET .../warehouse?action=updateWarehouseName&id=1001&warehouseName=테스트창고` | `{"result":"OK","updated":1}` |
| 2 | `GET .../warehouse?action=detail&id=1001` | `warehouseName` = "테스트창고" |

---

### TC-006. 입출고 소프트 삭제

| 항목 | 내용 |
| --- | --- |
| Test Case ID | TC-006 |
| 관련 요구사항 | FR-006, BR-001 |
| 관련 API | API-L05, API-L01 |

#### 테스트 절차

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | `GET .../warehouse?action=delete&id=1001` | `updated: 1` |
| 2 | `GET .../warehouse?action=list` | data에 id=1001 미포함 |

---

### TC-007. 투자자 이름 검색

| 항목 | 내용 |
| --- | --- |
| Test Case ID | TC-007 |
| 관련 요구사항 | FR-007, BR-008 |
| 관련 API | API-L07 |

#### 테스트 절차

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | `GET .../investor?action=list&name=김` | data에 "김민수"만 또는 김 포함 행 |
| 2 | `GET .../investor?action=list&name=` | 전체 3건 (TD-I01~03) |

---

### TC-008. DB fallback (Oracle 미연결)

| 항목 | 내용 |
| --- | --- |
| Test Case ID | TC-008 |
| 관련 요구사항 | FR-009, BR-011, NFR-008 |
| 우선순위 | P0 |

#### 테스트 절차

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | Oracle 미기동 상태에서 서버 시작 | 서버 정상 기동 |
| 2 | `GET .../warehouse?action=list` | 시드 2건 반환 (메모리) |
| 3 | 에러 페이지 없음 | JSON 정상 |

---

### TC-009. FR-011 부분 UI 갱신 (P0 개선)

| 항목 | 내용 |
| --- | --- |
| Test Case ID | TC-009 |
| 관련 요구사항 | FR-011, NFR-001, BR-005 |
| 관련 API | API-L04 |
| 우선순위 | P0 |
| 테스트 유형 | UI + Network Analysis |
| 상태 | **Not Run** (구현 전: 현재 Fail 예상) |

#### 테스트 절차

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | `http://localhost:8080/pages/warehouse-io.html` 접속 | 목록 표시 |
| 2 | DevTools → Network 탭, Preserve log | |
| 3 | 행 클릭 → 모달에서 WAREHOUSE_NAME 변경 | |
| 4 | Network 요청 확인 | `updateWarehouseName` **1회** |
| 5 | Network 요청 확인 | `action=list` **0회** |
| 6 | 화면 확인 | 해당 행 창고명만 변경 |

#### 현재 알려진 이슈

| Bug ID | 설명 | 심각도 |
| --- | --- | --- |
| — | (없음) | — |

---

### TC-005. REST 입출고 일괄 등록

| 항목 | 내용 |
| --- | --- |
| Test Case ID | TC-005 |
| 관련 요구사항 | FR-005 |
| 관련 API | API-R03 |
| 사전조건 | refactor-springboot-vanilla :8081 |

#### 테스트 절차

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | POST JSON body | HTTP 200 |
| 2 | 응답 확인 | `result: OK`, `warehouseIoId` 숫자 |
| 3 | GET /api/warehouses | 신규 행 포함 |

```bash
curl -s -X POST http://localhost:8081/api/warehouses \
  -H 'Content-Type: application/json' \
  -d '{"warehouseName":"QA테스트","productCode":"P-999","status":"ACTIVE"}'
```

---

### TC-010. SQL 컬럼 화이트리스트 (Inspection)

| 항목 | 내용 |
| --- | --- |
| Test Case ID | TC-010 |
| 관련 요구사항 | NFR-004, BR-010 |
| 테스트 유형 | Inspection (코드 리뷰) |

#### 검증 항목

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | `WarehouseIoDao.updateColumn` 확인 | column은 `WarehouseIoColumn` enum만 |
| 2 | 동적 SQL | `column.sqlName()` = enum name() |
| 3 | 외부 입력으로 컬럼명 주입 불가 | action→고정 handler→고정 enum |

---

### TC-015. Smoke — 서버 기동 및 메인 화면

| Step | Action | Expected Result |
| --- | --- | --- |
| 1 | README 명령으로 레거시 서버 기동 | "Legacy sample server started" |
| 2 | `http://localhost:8080/index.html` | 메뉴 5개 화면 링크 표시 |
| 3 | 각 화면 링크 클릭 | 404 없음 |

---

## 10. API 테스트 매트릭스

### 레거시 (:8080)

| API ID | Method | Endpoint | 요청 조건 | 기대 응답 | TC | Status |
| --- | --- | --- | --- | --- | --- | --- |
| API-L01 | GET | `/warehouse?action=list` | 서버 기동 | 200, `data:[]` | TC-001 | Not Run |
| API-L02 | GET | `/warehouse?action=detail&id=1001` | 유효 id | 200, `data:{...}` | TC-002 | Not Run |
| API-L03 | GET | `/warehouse?action=createEmptyRow` | - | 200, `warehouseIoId` | TC-003 | Not Run |
| API-L04a | GET | `/warehouse?action=updateWarehouseName&...` | id+값 | `updated:1` | TC-004 | Not Run |
| API-L05 | GET | `/warehouse?action=delete&id=1001` | 유효 id | `updated:1` | TC-006 | Not Run |
| API-L06 | GET | `/api/warehouse-io` | - | list와 동일 | TC-012 | Not Run |
| API-L07 | GET | `/investor?action=list&name=김` | - | 부분 일치 | TC-007 | Not Run |
| API-L09 | GET | `/investor?action=create&...` | 파라미터 | `investorId` | TC-013 | Not Run |
| - | GET | `/warehouse?action=invalid` | - | FAIL JSON | TC-011 | Not Run |

### 리팩터 (:8081~8083)

| API ID | Method | Endpoint | 기대 응답 | TC | Status |
| --- | --- | --- | --- | --- | --- |
| API-R01 | GET | `/api/warehouses` | `{data:[...]}` | TC-001 | Not Run |
| API-R02 | GET | `/api/warehouses/{id}` | `{data:{...}}` | TC-002 | Not Run |
| API-R03 | POST | `/api/warehouses` | `{result:OK,warehouseIoId}` | TC-005 | Not Run |
| API-R04 | GET | `/api/investors?name=` | `{data:[...]}` | TC-014 | Not Run |
| API-R05 | POST | `/api/investors` | `{result:OK,investorId}` | TC-013 | Not Run |

---

## 11. DB 검증

### 11.1 Oracle 연결 시

| DB TC ID | 테이블 | 검증 항목 | 검증 SQL | 기대 | TC |
| --- | --- | --- | --- | --- | --- |
| DBTC-001 | TB_WAREHOUSE_IO_SCREEN | 목록 조회 | `SELECT COUNT(*) FROM TB_WAREHOUSE_IO_SCREEN WHERE DELETED_YN='N'` | ≥1 | TC-001 |
| DBTC-002 | TB_WAREHOUSE_IO_SCREEN | 필드 수정 | `SELECT WAREHOUSE_NAME FROM ... WHERE WAREHOUSE_IO_ID=1001` | 수정값 | TC-004 |
| DBTC-003 | TB_WAREHOUSE_IO_SCREEN | 소프트 삭제 | `SELECT DELETED_YN FROM ... WHERE WAREHOUSE_IO_ID=1001` | 'Y' | TC-006 |
| DBTC-004 | TB_WAREHOUSE_IO_SCREEN | DRAFT 생성 | `SELECT STATUS FROM ... ORDER BY WAREHOUSE_IO_ID DESC FETCH FIRST 1 ROW ONLY` | 'DRAFT' | TC-003 |

### 11.2 메모리 fallback (Oracle 미연결)

| DB TC ID | 검증 항목 | 기대 | TC |
| --- | --- | --- | --- |
| DBTC-005 | list API 정상 | 시드 2건 | TC-008 |
| DBTC-006 | update 후 detail 일치 | 메모리 반영 | TC-004 |
| DBTC-007 | delete 후 list 제외 | 소프트 삭제 동작 | TC-006 |

### 11.3 v1.0 (TB_INVESTOR)

| DB TC ID | 검증 항목 | TC |
| --- | --- | --- |
| DBTC-008 | 투자자 INSERT 후 SELECT | TC-013 |
| DBTC-009 | IDX_INVESTOR_NAME 사용 검색 | FR-012 |

---

## 12. 결함 / Bug Report

### BUG-001 (해결됨 — FR-011 구현)

| 항목 | 내용 |
| --- | --- |
| Bug ID | BUG-001 |
| 발견일 | 2026-07-01 |
| 관련 TC | TC-009 |
| 심각도 | Major |
| 우선순위 | P0 |
| 상태 | **Resolved** (2026-07-01) |
| 발생 환경 | Local, legacy warehouse-io |
| 제목 | 필드 저장 후 `loadList()`로 list API 중복 호출 |
| 재현 절차 | 1. warehouse-io 모달 오픈 2. 필드 change 3. Network 탭 확인 |
| 기대 결과 | update 1회만, list 0회 |
| 실제 결과 (수정 전) | update + list 각 1회 (필드당 2회 왕복) |
| 수정 내용 | `warehouse-io.js` — `patchGridRow()` 도입, `updateField` success에서 `loadList()` 제거 |
| 담당자 | 개발 |

### Bug Report 템플릿 (신규 등록용)

| 항목 | 내용 |
| --- | --- |
| Bug ID | BUG-___ |
| 발견일 | YYYY-MM-DD |
| 발견자 | |
| 관련 TC | |
| 심각도 | Critical / Major / Minor / Trivial |
| 우선순위 | P0 / P1 / P2 |
| 상태 | Open / In Progress / Fixed / Retest / Closed |
| 발생 환경 | Local / :8080 / :8081 ... |
| 제목 | |
| 재현 절차 | |
| 기대 결과 | |
| 실제 결과 | |
| 첨부 | Screenshot / curl output |
| 담당자 | |
| 수정 버전 | |

---

## 13. 결함 심각도 기준

| 심각도 | 의미 | 본 프로젝트 예시 |
| --- | --- | --- |
| Critical | 데모 불가, 데이터 손상 | 서버 기동 실패, list 항상 빈 배열 |
| Major | 핵심 기능 오류, 우회 가능 | BUG-001 chatty reload, delete 미동작 |
| Minor | UX/표시 문제 | 폴백 테이블 컬럼 정렬, 한글 깨짐 |
| Trivial | 운영 영향 거의 없음 | CSS 여백, 주석 오타 |

---

## 14. 테스트 완료 기준 (MVP Release)

| 기준 | 완료 조건 |
| --- | --- |
| P0 TC (TC-001~010, 013, 015) | 100% Pass |
| P1 TC | 80% 이상 Pass 또는 Blocked 사유 문서화 |
| Critical Bug | 0건 |
| Major Bug | BUG-001 해결 또는 릴리즈 승인 예외 |
| Smoke (TC-015) | Pass |
| API-L01, L07, R03 | Pass |
| DB fallback (TC-008) | Pass |
| 문서 정합성 | PRD/SRS/SDD/API TC ID 일치 |

---

## 15. 테스트 결과 요약

| 항목 | 수량 |
| --- | --- |
| 전체 테스트케이스 | 17 |
| Pass | 0 |
| Fail | 0 |
| Blocked | 0 |
| Not Run | 17 |
| Pass Rate | — (미실행) |
| Open Bug | 0 |
| Critical Bug | 0 |

> MVP 데모 전 TC-008, TC-015, TC-001 수동 실행 후 본 표 업데이트.

---

## 16. 요구사항 추적성

| PRD FR | SRS FR | API | TC | DB TC | 결과 |
| --- | --- | --- | --- | --- | --- |
| FR-001 | FR-001 | API-L01, R01 | TC-001 | DBTC-001 | Not Run |
| FR-002 | FR-002 | API-L02, R02 | TC-002 | - | Not Run |
| FR-003 | FR-003 | API-L03 | TC-003 | DBTC-004 | Not Run |
| FR-004 | FR-004 | API-L04 | TC-004 | DBTC-002 | Not Run |
| FR-005 | FR-005 | API-R03 | TC-005 | - | Not Run |
| FR-006 | FR-006 | API-L05 | TC-006 | DBTC-003 | Not Run |
| FR-007 | FR-007 | API-L07, R04 | TC-007, TC-014 | - | Not Run |
| FR-008 | FR-008 | API-L09, R05 | TC-013 | - | Not Run |
| FR-009 | FR-009 | - | TC-008 | DBTC-005~007 | Not Run |
| FR-010 | FR-010 | API-L06 | TC-012 | - | Not Run |
| FR-011 | FR-011 | API-L04 | TC-009 | - | Not Run (BUG-001 Resolved) |
| NFR-001 | NFR-001 | API-L04 | TC-009 | - | Not Run |
| NFR-004 | NFR-004 | - | TC-010 | - | Not Run |
| NFR-008 | NFR-008 | API-L01 | TC-008 | DBTC-005 | Not Run |
| NFR-009 | NFR-009 | - | TC-015 | - | Not Run |

---

## 17. QA 체크리스트

| 체크 항목 | 확인 |
| --- | --- |
| 요구사항이 테스트 가능한 수준으로 작성되었는가? | ☑ |
| SRS FR/NFR이 TC와 연결되었는가? | ☑ |
| P0 TC가 정의되었는가? | ☑ |
| 정상/예외/fallback/화면 케이스 포함? | ☑ |
| 권한 TC는 MVP 제외 명시? | ☑ |
| 테스트 환경·데이터 정의? | ☑ |
| 결함 심각도·완료 기준 정의? | ☑ |
| 알려진 이슈(BUG-001) 등록? | ☑ |

---

## 18. QC 체크리스트 (릴리즈 전)

| 체크 항목 | 확인 |
| --- | --- |
| P0 TC 수동 실행 완료? | □ |
| TC-008 fallback Pass? | □ |
| TC-015 Smoke Pass? | □ |
| BUG-001 수정·TC-009 Retest? | ☑ (코드 반영 완료, Retest 권장) |
| Critical Bug 0건? | □ |
| README 실행 명령 동작? | □ |
| 문서 체인 PRD→SRS→SDD→Test 일치? | ☑ |

---

## 19. 변경 이력

| 버전 | 일자 | 내용 |
| --- | --- | --- |
| v0.1 | 2026-07-01 | SRS/SDD 기반 최초 Test/QAQC 문서, TC-001~017, BUG-001 등록 |

---

*다음 단계: TC-008·TC-015 Smoke 실행 → TC-009 Retest*
