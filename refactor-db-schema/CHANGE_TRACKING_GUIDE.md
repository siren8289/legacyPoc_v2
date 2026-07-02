# 변경 추적 컬럼 가이드 (Change Tracking)

데이터 엔지니어링·증분 ETL을 위해 **운영 테이블**에 공통 메타 컬럼을 둔다.  
도메인 model(`WarehouseIo`, `Investor`)에는 노출하지 않고, DB DDL + JPA `ChangeTrackedEntity` 전용이다.

## 컬럼

| 컬럼 | NULL | 역할 |
|------|------|------|
| `created_at` | NOT NULL | 최초 적재 시각 (변경 금지) |
| `updated_at` | NOT NULL | **증분 추출 워터마크** — 행이 바뀔 때마다 갱신 |
| `deleted_at` | NULL | soft delete 시각 (`deleted_yn = 'Y'`와 함께) |
| `source_system` | NOT NULL | 적재 원천 (`refactor-api`, `seed`, `legacy-servlet` 등) |
| `etl_batch_id` | NULL | 배치/잡 실행 ID (재처리·감사 추적) |

## 증분 추출 (핵심)

전체 재처리 대신 **마지막 적재 시각 이후 변경분**만 읽는다.

```sql
SELECT warehouse_io_id, warehouse_name, product_code, product_name,
       product_category, in_qty, out_qty, current_stock, client_name,
       status, deleted_yn, created_at, updated_at, deleted_at,
       source_system, etl_batch_id
FROM tb_warehouse_io
WHERE updated_at > :last_loaded_at
ORDER BY updated_at, warehouse_io_id;
```

- `:last_loaded_at` — DW/스테이징에 저장한 워터마크 (첫 실행은 `'1970-01-01'` 등)
- `ORDER BY updated_at, warehouse_io_id` — 동일 시각 다건 시 PK로 결정적 순서
- 인덱스: `idx_warehouse_updated_at (updated_at, warehouse_io_id)` — [INDEX_GUIDE.md](./INDEX_GUIDE.md)

삭제도 추적하려면 `deleted_yn`/`deleted_at`을 함께 적재하거나, CDC 별도 파이프라인을 둔다.

대용량 시 `updated_at`/`created_at` RANGE 파티션과 병행 — [PARTITIONING_GUIDE.md](./PARTITIONING_GUIDE.md).

## DB별 DEFAULT / 자동 갱신

| DB | `created_at` | `updated_at` 자동 갱신 |
|----|--------------|------------------------|
| **MariaDB** | `DEFAULT CURRENT_TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` |
| **MySQL** | 동일 | 동일 — [CREATE TABLE](https://dev.mysql.com/doc/refman/8.4/en/create-table.html), [날짜 함수](https://dev.mysql.com/doc/refman/8.4/en/date-and-time-functions.html) |
| **PostgreSQL** | `DEFAULT CURRENT_TIMESTAMP` | **DB 자동 없음** — JPA `@PreUpdate` 또는 ETL에서 `updated_at = CURRENT_TIMESTAMP` 명시 |
| **Oracle** | `DEFAULT SYSTIMESTAMP` | **DB 자동 없음** — JPA 또는 `MERGE`/`UPDATE` 시 `SYSTIMESTAMP` |

공식 문서:

| DB | 참고 |
|----|------|
| MariaDB | [CREATE TABLE](https://mariadb.com/kb/en/create-table/) — `DEFAULT`, `CURRENT_TIMESTAMP`, `DATETIME` |
| MySQL | [날짜/시간 함수](https://dev.mysql.com/doc/refman/8.4/en/date-and-time-functions.html) — `CURRENT_TIMESTAMP` |
| PostgreSQL | [CREATE TABLE](https://www.postgresql.org/docs/current/sql-createtable.html), [날짜/시간 함수](https://www.postgresql.org/docs/current/functions-datetime.html) |
| Oracle | [CREATE TABLE](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-TABLE.html) — column default, identity |

## JPA (`ChangeTrackedEntity`)

```java
@MappedSuperclass
public abstract class ChangeTrackedEntity {
    // created_at — @PrePersist, updatable = false
    // updated_at — @PrePersist + @PreUpdate
    // deleted_at — soft delete 시 Repository에서 setDeletedAt(now)
    // source_system — 기본값 'refactor-api'
    // etl_batch_id — ETL 잡에서만 설정 (nullable)
}
```

`WarehouseJpaEntity` / `InvestorJpaEntity`가 상속. API 저장·수정·soft delete 시 `updated_at`이 갱신된다.

## 시드 / Upsert

`data.sql` 시드 행은 `source_system = 'seed'`, `etl_batch_id = 'seed-batch-001'`.  
Upsert 재실행 시 `created_at`은 유지하고 `updated_at`만 `CURRENT_TIMESTAMP`/`SYSTIMESTAMP`로 갱신 — [SEED_UPSERT_GUIDE.md](./SEED_UPSERT_GUIDE.md).

## EXPLAIN 검증

증분 쿼리 플랜 확인: `*/explain-verify.sql` Q1 — [EXPLAIN_GUIDE.md](./EXPLAIN_GUIDE.md).

## 레거시와의 관계

레거시 Oracle `TB_WAREHOUSE_IO_SCREEN`에는 변경 추적 컬럼이 **없음** (의도적 대비).  
리팩터 `tb_*` 테이블만 DE 준비 스키마로 확장한다.
