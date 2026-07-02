# 인덱스 설계 가이드

목록 조회, 이름 검색, `updated_at` 증분 추출에 맞춘 인덱스 정의.

## 공식 문서

| DB | CREATE INDEX 문서 | 비고 |
|----|-------------------|------|
| **MariaDB** | [CREATE INDEX](https://mariadb.com/kb/en/create-index/) | `IF NOT EXISTS` (10.1.4+) |
| **MySQL** | [CREATE INDEX](https://dev.mysql.com/doc/refman/8.4/en/create-index.html) | 복합 인덱스, DESC (8.0+) |
| **PostgreSQL** | [CREATE INDEX](https://www.postgresql.org/docs/current/sql-createindex.html) | B-tree, partial index, expression index |
| **Oracle** | [CREATE INDEX](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-INDEX.html) | 단일/복합, 파티션 테이블 |

## 인덱스 목록

### `tb_warehouse_io`

| 인덱스 | 컬럼 | 용도 | 대응 쿼리 |
|--------|------|------|-----------|
| `pk_tb_warehouse_io` | `warehouse_io_id` | PK | 단건 조회 |
| `idx_warehouse_active` | `(deleted_yn, warehouse_io_id DESC)` | **목록 조회** | `WHERE deleted_yn='N' ORDER BY warehouse_io_id DESC` |
| `idx_warehouse_updated_at` | `(updated_at, warehouse_io_id)` | **증분 추출** | `WHERE updated_at > :since ORDER BY updated_at, warehouse_io_id` |

```sql
CREATE INDEX idx_warehouse_updated_at
    ON tb_warehouse_io (updated_at, warehouse_io_id);
```

### `tb_investor`

| 인덱스 | 컬럼 | 용도 | 대응 쿼리 |
|--------|------|------|-----------|
| `pk_tb_investor` | `investor_id` | PK | 단건 조회 |
| `idx_investor_name` | `investor_name` | **이름 검색** (FR-012) | `WHERE investor_name LIKE '%:q%'` |
| `idx_investor_updated_at` | `(updated_at, investor_id)` | **증분 추출** | `WHERE updated_at > :since` |

### Legacy `TB_WAREHOUSE_IO_SCREEN`

| 인덱스 | 컬럼 | 용도 |
|--------|------|------|
| `IDX_WIO_ACTIVE` | `(DELETED_YN, WAREHOUSE_IO_ID DESC)` | 목록 |
| `IDX_WIO_UPDATED_AT` | `(UPDATED_AT, WAREHOUSE_IO_ID)` | 증분 |

## 쿼리 패턴별 설계 이유

### 1. 목록 조회

```sql
SELECT * FROM tb_warehouse_io
 WHERE deleted_yn = 'N'
 ORDER BY warehouse_io_id DESC;
```

- 선두 컬럼 `deleted_yn` → 활성 행 필터
- `warehouse_io_id DESC` → 정렬과 일치 (MySQL 8 / MariaDB 10.3+ DESC 인덱스)

### 2. 이름 검색

```sql
SELECT * FROM tb_investor
 WHERE investor_name LIKE '%김%';
```

- `idx_investor_name` — prefix 검색에는 유리, `%...%` contains는 풀스캔에 가깝지만 v1.0 규모에서는 충분
- v2.0: PostgreSQL `GIN` + `pg_trgm`, Oracle Text 등 검토

### 3. 증분 추출 (CDC / 동기화)

```sql
SELECT * FROM tb_warehouse_io
 WHERE updated_at > :last_sync
 ORDER BY updated_at, warehouse_io_id;
```

- `(updated_at, warehouse_io_id)` 복합 인덱스 → 범위 스캔 + 동일 시각 tie-break
- `updated_at`은 Entity `ChangeTrackedEntity` / DB `ON UPDATE CURRENT_TIMESTAMP`(MariaDB/MySQL) 또는 JPA `@PreUpdate`로 갱신

## DB별 CREATE INDEX 예시

```sql
-- MariaDB (IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_warehouse_updated_at
    ON tb_warehouse_io (updated_at, warehouse_io_id);

-- MySQL
CREATE INDEX idx_warehouse_updated_at
    ON tb_warehouse_io (updated_at, warehouse_io_id);

-- PostgreSQL (+ partial index 예시)
CREATE INDEX IF NOT EXISTS idx_warehouse_updated_at
    ON tb_warehouse_io (updated_at, warehouse_io_id);
-- CREATE INDEX idx_warehouse_active_live
--     ON tb_warehouse_io (warehouse_io_id DESC) WHERE deleted_yn = 'N';

-- Oracle
CREATE INDEX idx_warehouse_updated_at
    ON tb_warehouse_io (updated_at, warehouse_io_id);
```

## `updated_at` 컬럼

| DB | 정의 |
|----|------|
| MariaDB/MySQL | `TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` |
| PostgreSQL | `TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP` (+ JPA `@PreUpdate`) |
| Oracle | `TIMESTAMP(6) DEFAULT SYSTIMESTAMP NOT NULL` (+ JPA `@PreUpdate`) |

도메인 `WarehouseIo`에는 **포함하지 않음** — DB·Entity(`ChangeTrackedEntity`) 전용. 전체 컬럼: [CHANGE_TRACKING_GUIDE.md](./CHANGE_TRACKING_GUIDE.md)

## 적용 위치

| 경로 | 설명 |
|------|------|
| `refactor-db-schema/*/schema.sql` | 인덱스 DDL |
| `legacy-servlet-jquery-oracle/oracle-schema/schema.oracle.sql` | 레거시 Oracle |
| `*JpaEntity extends ChangeTrackedEntity` | JPA 갱신 시각·DE 메타 |

## 체크리스트

- [ ] 목록: `deleted_yn` + 정렬 컬럼 복합 인덱스
- [ ] 검색: `investor_name` 인덱스
- [ ] 증분: `(updated_at, id)` 복합 인덱스
- [ ] PK 외 인덱스는 `schema.sql`에 명시적 `CREATE INDEX`

## 관련 문서

- [EXPLAIN_GUIDE.md](./EXPLAIN_GUIDE.md) — `explain-verify.sql`로 인덱스 사용 검증
- [SELECT_QUERY_GUIDE.md](./SELECT_QUERY_GUIDE.md) — projection·pagination
