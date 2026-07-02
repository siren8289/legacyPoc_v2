# 구현 변경 로그 & 아키텍처 점검

> PRD/SRS/SDD 문서 기반 P0 구현 반영 및 코드 품질·아키텍처 체크리스트 결과  
> 작성일: 2026-07-01

---

## 1. 이번에 수정한 내용

### FR-011 — 필드 저장 후 전체 목록 재조회 제거 (BUG-001 해결)

| 항목 | 내용 |
|------|------|
| 요구사항 | SRS FR-011, TC-009, SDD §5.2 |
| 문제 | `updateField()` 성공 시 `loadList()` 호출 → 필드 1회 저장마다 list API 1회 추가 |
| 해결 | 성공 콜백에서 `patchGridRow(id, fieldKey, value)`로 해당 행만 갱신 |
| 변경 파일 | `legacy-servlet-jquery-oracle/jquery-webapp/src/main/webapp/static/js/warehouse-io.js` |

**알고리즘 변화**

| Before | After |
|--------|-------|
| 저장 1회 → API 2회 (update + list) | 저장 1회 → API 1회 (update만) |
| RealGrid/Fallback 전체 재렌더 O(n) | 해당 행만 O(1)~O(n) 탐색 후 1셀 갱신 |

**자료구조**

- `GRID_FIELD_COLUMN_INDEX`: 필드명 → Fallback 테이블 컬럼 인덱스 매핑
- `NUMERIC_GRID_FIELDS`: 수량 필드 파싱 플래그
- RealGrid: `dataProvider.setValue(row, field, value)` 직접 갱신

---

### FR-012 — 투자자 이름 검색 인덱스

| 항목 | 내용 |
|------|------|
| 요구사항 | SRS FR-012, SDD §5.3 Option A |
| 문제 | `list(keyword)`가 매번 `table.values()` 전체 순회 O(n) |
| 해결 | 이름 **첫 글자 버킷** 인덱스로 후보 ID 축소 후 `contains` 검증 |
| 변경 파일 | 아래 표 참조 |

| 모듈 | 추가/수정 |
|------|-----------|
| Legacy | `InvestorSearchIndex.java` (신규), `InvestorDao.java` |
| refactor-springboot-vanilla | `common/InvestorSearchIndex.java`, `InvestorRepository.java` |
| refactor-springboot-vue | 동일 |
| refactor-nextjs-springboot-api | 동일 |

**자료구조 선택**

```
Primary:  LinkedHashMap<Long, Entity>   — PK 조회 O(1), 삽입 순서 유지
Index:    Map<Character, List<Long>>   — 첫 글자 버킷, 후보 집합 축소
```

**알고리즘**

1. `keyword` 없음 → 전체 반환 (인덱스 미사용)
2. `keyword` 있음 → `candidateIds = index[첫글자]`
3. 후보 없으면 풀스캔 fallback (엣지 케이스)
4. 후보에 대해 `name.contains(keyword)` 최종 필터

> **한계**: 완전한 O(1) 검색이 아님. 데이터가 커지면 v1.0에서 SQL `LIKE` + DB 인덱스로 전환 (SDD Option B).

---

## 2. 자료구조·알고리즘 전체 요약 (Before → After)

| 영역 | Before | After | 복잡도 |
|------|--------|-------|--------|
| Servlet action 분기 | if-else 체인 | `HashMap<String, Function>` | O(1) |
| PK 조회 (입출고/투자자) | Map / JDBC | `LinkedHashMap` 유지 | O(1) |
| 컬럼 UPDATE SQL | 메서드별 중복 SQL | `WarehouseIoColumn` enum + `updateColumn()` | DRY |
| 필드 저장 UI | update + list | update + row patch | API 50% 감소 |
| 투자자 검색 | 전체 스캔 | 첫 글자 버킷 + contains | 평균 후보 축소 |
| ID 발급 (refactor) | long++ | `AtomicLong` | 스레드 안전 |

---

## 3. 아키텍처·코드 품질 체크리스트

평가 기준: **Pass** ✅ / **Partial** ⚠️ / **Fail** ❌  
Legacy는 **의도적 안티패턴 포함** 교육용 PoC이므로, “개선됨”과 “의도적 유지”를 구분한다.

### A. 코드 품질 (6항목)

| # | 항목 | Legacy | Refactor (3모듈) | 비고 |
|---|------|--------|------------------|------|
| 1 | **Java 코드 스타일** | ⚠️ | ✅ | Legacy: 수동 JSON 문자열, Servlet 직접 응답. Refactor: record, 생성자 주입, Spring 관례 |
| 2 | **이름 짓기** | ✅ | ✅ | `WarehouseIoDao`, `CreateWarehouseRequest`, `InvestorSearchIndex` 등 역할이 이름에 드러남 |
| 3 | **클래스/메서드 책임 분리** | ❌ | ✅ | Legacy: Servlet이 라우팅+직렬화+DAO 호출. Refactor: Controller(HTTP) / Service(규칙) / Repository(저장) |
| 4 | **중복 제거 (DRY)** | ⚠️ | ⚠️ | Legacy: `updateColumn()`·action Map으로 개선. Refactor: 3모듈에 `InvestorSearchIndex` 동일 복제 (공유 모듈 없음) |
| 5 | **예외 처리** | ⚠️ | ⚠️ | Legacy: SQLException → 메모리 fallback, NumberFormatException → 0. Refactor: 전역 `@ControllerAdvice` 없음, null 반환 |
| 6 | **DTO / Entity / Service 역할 분리** | ❌ | ✅ | Legacy: DTO 혼재. Refactor: `Create*Request`(입력) + `WarehouseIo`/`Investor`(도메인) + `*JpaEntity`(DB) 3분리 |

### B. 클린 아키텍처 (5항목)

| # | 항목 | Legacy | Refactor | 비고 |
|---|------|--------|----------|------|
| 1 | **Dependency Rule** | ❌ | ⚠️ | Legacy: 상위(UI)가 하위(DAO/JDBC) 직접 의존. Refactor: Controller→Service→Repository 준수. 다만 Repository가 Spring `@Repository`에 묶임 |
| 2 | **Controller / Service / Repository 분리** | ❌ | ✅ | `WarehouseController` → `WarehouseService` → `WarehouseRepository` |
| 3 | **외부 기술 vs 비즈니스 로직 분리** | ⚠️ | ✅ | Legacy: JDBC가 DAO 내부. Refactor: JDBC는 `Jdbc*Repository`에 격리, Service는 interface만 의존 |
| 4 | **의존성 방향** | ❌ | ✅ | Refactor: 안쪽(Service)이 바깥(Controller)을 모름. 생성자 주입으로 방향 고정 |
| 5 | **테스트 가능한 구조** | ⚠️ | ⚠️ | Legacy: `processAction()` public으로 단위 테스트 가능. Refactor: Service가 `WarehouseRepository` 인터페이스에 의존해 Mock 주입 가능. **테스트 코드 미작성** |

---

## 4. 모듈별 상세 판정

### Legacy (`legacy-servlet-jquery-oracle`)

**의도적으로 유지하는 레거시 패턴**

- Servlet → DAO (Service 없음)
- `action` 쿼리 파라미터 API (13개 필드별 update)
- 수동 JSON (`LegacyJsonWriter`)
- jQuery change 이벤트 즉시 저장

**이번에 개선한 부분**

- action Map O(1) 라우팅
- `WarehouseIoColumn` enum 화이트리스트 UPDATE
- `LegacyJsonWriter`, `RequestParamReader` 추출
- FR-011: 프론트 row patch
- FR-012: `InvestorSearchIndex`

### Refactor (`refactor-springboot-*`, `refactor-nextjs-*`)

**달성**

- Controller / Service / Repository 3계층
- REST JSON API (`/api/warehouses`, `/api/investors`)
- Java `record` 불변 모델
- `CreateWarehouseRequest` 입력 DTO 분리
- `AtomicLong` 시퀀스

**미완 / 개선 여지**

| 항목 | 상태 |
|------|------|
| JPA/실DB 연동 | ❌ 메모리 Map만 사용 |
| `WarehouseIo` 필드 parity (managerPhone, memo 등) | ⚠️ 일부 필드 누락 |
| 전역 예외 처리 (`@ControllerAdvice`) | ❌ 없음 |
| Repository 인터페이스 | ✅ Warehouse + Investor interface, Memory/Jpa 구현체 분리 |
| 외부 기술 vs 비즈니스 | ✅ JPA/DB는 `Jpa*Repository` + `*JpaEntity`에 격리, Service는 interface만 의존 |
| 단위/통합 테스트 | ❌ 미작성 |
| 3모듈 `InvestorSearchIndex` 중복 | ⚠️ 공통 jar 또는 parent 모듈로 통합 가능 |

---

## 5. 종합 결론

| 구분 | 코드 품질 6항목 | 클린 아키텍처 5항목 | 총평 |
|------|----------------|-------------------|------|
| **Legacy** | 2✅ 2⚠️ 2❌ | 0✅ 1⚠️ 4❌ | 교육용 안티패턴 **의도적**. 구조적 분리·DRY·계층 분리는 **미달** (일부 알고리즘만 개선) |
| **Refactor** | 3✅ 3⚠️ 0❌ | 2✅ 3⚠️ 0❌ | **기본 골격은 충족**. 실DB·예외·테스트·인터페이스 추상화는 **추가 작업 필요** |

**“됐는지” 한 줄 답**

- **Refactor 모듈**: Controller/Service/Repository 분리, 의존성 방향, 이름·스타일 → **대체로 OK**
- **Legacy 모듈**: 구조 분리·DTO/Service 분리 → **의도적으로 NO** (비교 대상)
- **전체 프로젝트**: FR-011/012·Repository interface 분리 완료. 테스트·예외 통합 → **추가 작업**

---

## 6. 문서 추적성

| 변경 | PRD | SRS | SDD | TEST |
|------|-----|-----|-----|------|
| row patch | FR-011 | TC-009 | §5.2 | BUG-001 → **Resolved** |
| 이름 검색 인덱스 | FR-012 | TC-010 | §5.3 | TC-010 재검증 권장 |
| action Map | — | — | §4.1 | TC-008 |
| updateColumn enum | — | — | §4.2 | TC-007 |

---

## 7. 다음 권장 작업 (P1)

1. `WarehouseServiceTest` / `InvestorRepositoryTest` 추가 (테스트 가능 구조 완성)
2. `@ControllerAdvice` + `NotFoundException` (예외 처리 통합)
3. ~~`InvestorRepository` 인터페이스 추출 (DIP)~~ → Warehouse/Investor interface + Memory/Jdbc 분리 완료
4. v1.0: JPA + Oracle, 페이징, `WarehouseIo` 필드 parity
5. 3 refactor 모듈 공통 코드를 `refactor-common` 서브모듈로 통합 (DRY)

---

## 8. WarehouseRepository interface 분리 (2026-07-01)

### 변경 구조

```
WarehouseService
  ↓ (interface)
WarehouseRepository
  ↓
MemoryWarehouseRepository   ← LinkedHashMap, AtomicLong
```

### 적용 모듈

- `refactor-springboot-vanilla`
- `refactor-springboot-vue`
- `refactor-nextjs-springboot-api`

### 인터페이스 계약

```java
public interface WarehouseRepository {
    WarehouseIo save(WarehouseIo warehouseIo);
    Optional<WarehouseIo> findById(Long id);
    List<WarehouseIo> findAll();
    WarehouseIo updateField(Long id, String fieldName, Object value);
    void deleteById(Long id);
}
```

- `updateField` / `deleteById`는 인터페이스에 선언만 해두었고, Controller·Service는 아직 미노출 (향후 PATCH/DELETE API 연결용)
- Spring이 `MemoryWarehouseRepository`를 `WarehouseRepository` 빈으로 자동 주입

---

## 9. Repository interface 분리 + JPA 저장소 (2026-07-01)

### Repository 구조 (Warehouse / Investor 공통)

```
Service
  ↓
Repository interface          ← 저장소 규칙만 정의
  ├─ Memory*Repository        @ConditionalOnProperty(type=memory, 기본)
  └─ Jpa*Repository           @ConditionalOnProperty(type=jpa)
        ↓
SpringData*JpaRepository      ← Spring Data JPA (Jakarta Persistence API)
        ↓
*JpaEntity                    ← @Entity, DB 테이블 매핑
        ↓
tb_warehouse_io / tb_investor
```

**Spring Data JPA**는 Jakarta Persistence API 기반 Repository 지원을 제공한다. `JpaRepository<Entity, ID>`를 상속하면 `save`, `findById`, `delete` 등이 자동 구현되고, 메서드 이름(`findByDeletedYnOrderBy...`)으로 쿼리가 생성된다. `JpaWarehouseRepository`는 도메인 record(`WarehouseIo`)와 Entity 변환·비즈니스 저장소 계약 이행만 담당한다.

### 전환 방법

```yaml
# 기본 — 메모리 (DB/JPA 자동설정 OFF)
app.repository.type: memory

# JPA — schema.sql + data.sql 수동 적용 후
app.repository.type: jpa
# 실행: --spring.profiles.active=jpa
```

### 의존성

- `spring-boot-starter-data-jpa` (JdbcTemplate 구현체는 제거)
- `MemoryRepositoryAutoConfiguration` — memory 모드에서 DataSource/JPA 자동설정 제외

### DB 스키마

`refactor-db-schema/` — mariadb / mysql / postgresql

---

## 10. Domain과 DB Entity 분리 (2026-07-01)

### 패키지 역할

| 패키지 | 타입 | JPA 어노테이션 | 사용 계층 |
|--------|------|----------------|-----------|
| `feature.*.model` | `WarehouseIo`, `Investor` (record) | **없음** | Service, Controller |
| `feature.*.persistence` | `*JpaEntity` | `@Entity`, `@Table`, `@Column` | JpaRepository, Spring Data JPA |
| `feature.*.persistence` | `*JpaMapper` | — | Entity ↔ Domain 변환 |

### 데이터 흐름

```
Service          → WarehouseIo (도메인)
JpaRepository    → WarehouseJpaEntity (DB) → save → WarehouseJpaMapper.toDomain() → WarehouseIo 반환
MemoryRepository → WarehouseIo (도메인) 직접 — Entity 없음
```

Service는 `persistence` 패키지를 import하지 않는다. 테이블 컬럼·`deleted_yn` 등 DB 전용 필드는 `WarehouseJpaEntity`에만 존재한다.

---

## 11. DDL / 테이블 생성 정비 (2026-07-01)

- `refactor-db-schema/*/schema.sql` — `NOT NULL`, `DEFAULT`, 명시적 `PRIMARY KEY`, `CHECK`, DB별 Identity
- `refactor-db-schema/oracle/` — Oracle refactor DDL 추가
- `legacy-servlet-jquery-oracle/oracle-schema/schema.oracle.sql` — Oracle 레거시 DDL
- `refactor-db-schema/DDL_GUIDE.md` — DB별 공식 문서 링크·컬럼 정의표

---

## 12. PK / 자동 증가 ID 설계 (2026-07-01)

- `refactor-db-schema/PK_IDENTITY_GUIDE.md` — MariaDB `AUTO_INCREMENT`, PostgreSQL `IDENTITY`, Oracle identity/SEQUENCE
- 시드 ID: `warehouse_io_id` 1001+, `investor_id` 2001+ (메모리 `AtomicLong`과 동일)
- `GENERATED BY DEFAULT` / `AUTO_INCREMENT` 선택 이유: 시드 명시 INSERT + JPA `GenerationType.IDENTITY` 병행

---

## 13. 인덱스 설계 (2026-07-01)

- `updated_at` 컬럼 추가 (`tb_warehouse_io`, `tb_investor`) — 증분 추출용
- `idx_warehouse_updated_at`, `idx_investor_updated_at` 복합 인덱스
- `AuditableEntity` → `ChangeTrackedEntity` — JPA `@PrePersist`/`@PreUpdate` + DE 메타 (도메인 미노출)
- `refactor-db-schema/INDEX_GUIDE.md` — 목록/검색/증분 쿼리·DB별 CREATE INDEX

---

## 14. SELECT / Pagination (2026-07-01)

- `WarehouseListItem` / `InvestorListItem` — 목록 projection (`SELECT *` 지양)
- `PageResult<T>` — `items`, `totalCount`, `page`, `size`
- JPA constructor expression + `Pageable` → `LIMIT`/`OFFSET`
- API: `?page=0&size=20` (없으면 기존 전체 목록 하위 호환)
- `refactor-db-schema/SELECT_QUERY_GUIDE.md`

---

## 15. 실행계획(EXPLAIN) 검증 (2026-07-01)

- `refactor-db-schema/EXPLAIN_GUIDE.md` — MariaDB/MySQL/PostgreSQL/Oracle EXPLAIN 해석
- `refactor-db-schema/*/explain-verify.sql` — Q1~Q4 인덱스 타는지 확인 스크립트

---

## 16. Seed / Upsert (2026-07-01)

- `data.sql` — `ON DUPLICATE KEY UPDATE` / `ON CONFLICT` / `MERGE` 멱등 시드
- `refactor-db-schema/SEED_UPSERT_GUIDE.md` — DB별 Upsert 문법·재실행 검증

---

## 17. 변경 추적 컬럼 (2026-07-01)

- 운영 테이블 공통: `created_at`, `updated_at`, `deleted_at`, `source_system`, `etl_batch_id`
- 증분 ETL: `WHERE updated_at > :last_loaded_at ORDER BY updated_at, warehouse_io_id` + `idx_*_updated_at`
- `AuditableEntity` → `ChangeTrackedEntity` (`@PrePersist`/`@PreUpdate`, soft delete 시 `deleted_at`)
- `refactor-db-schema/CHANGE_TRACKING_GUIDE.md` — DB별 DEFAULT·공식 문서 링크
- 시드: `source_system = 'seed'`, `etl_batch_id = 'seed-batch-001'`

---

## 18. 파티셔닝 (2026-07-01)

- PoC `schema.sql`은 **비파티션** 유지 (소량 데이터)
- 확장 시 `created_at` / `updated_at` / `stock_date`(업무일) RANGE 파티션 검토
- `refactor-db-schema/PARTITIONING_GUIDE.md` — DB별 공식 문서·PK 제약·마이그레이션 체크리스트
- `refactor-db-schema/*/partition-example.sql` — dialect별 참고 DDL (`tb_warehouse_io_part`)

---

## 19. View / Materialized View / Summary Table (2026-07-01)

- 무거운 집계는 매번 `GROUP BY` 대신 요약 구조 사용
- MariaDB/MySQL: `VIEW` + **summary table** (`tb_summary_warehouse_stock` UPSERT)
- PostgreSQL/Oracle: `MATERIALIZED VIEW` + `REFRESH`
- `refactor-db-schema/VIEW_SUMMARY_GUIDE.md` — 선택 기준·JPA와 분리·체크리스트
- `refactor-db-schema/*/summary-views-example.sql` — `vw_warehouse_io_active`, 창고/등급별 요약 예시






