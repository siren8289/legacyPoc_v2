# 실행계획(EXPLAIN) 확인 가이드

인덱스를 만든 뒤 **실제 쿼리가 인덱스를 타는지** `EXPLAIN`으로 검증한다.

## 공식 문서

| DB | 문서 | 특징 |
|----|------|------|
| **MariaDB** | [EXPLAIN](https://mariadb.com/kb/en/explain/) | `SELECT`/`UPDATE`/`DELETE` 계획. `EXPLAIN FORMAT=JSON` |
| **MySQL** | [EXPLAIN](https://dev.mysql.com/doc/refman/8.4/en/explain-output.html) | optimizer 실행계획. `EXPLAIN ANALYZE` (8.0.18+) |
| **PostgreSQL** | [EXPLAIN](https://www.postgresql.org/docs/current/sql-explain.html) | `EXPLAIN ANALYZE`로 **실제 실행**·시간 측정 |
| **Oracle** | [EXPLAIN PLAN](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/EXPLAIN-PLAN.html) | `EXPLAIN PLAN FOR` + `DBMS_XPLAN.DISPLAY` |

## 검증 SQL 스크립트

| DB | 파일 |
|----|------|
| MariaDB | `mariadb/explain-verify.sql` |
| MySQL | `mysql/explain-verify.sql` |
| PostgreSQL | `postgresql/explain-verify.sql` |
| Oracle | `oracle/explain-verify.sql` |

```bash
# MariaDB 예시 (schema + data 적용 후)
mysql -u legacy_user -plegacy_pass legacy_refactor < refactor-db-schema/mariadb/explain-verify.sql
```

## 검증 대상 쿼리 (4종)

| # | 패턴 | 기대 인덱스 | 확인 포인트 |
|---|------|-------------|-------------|
| Q1 | `updated_at > ?` 증분 | `idx_warehouse_updated_at` | `key`/`Index Scan`에 인덱스명 |
| Q2 | 활성 목록 `deleted_yn='N'` | `idx_warehouse_active` | range/ref, filesort 최소화 |
| Q3 | 투자자 이름 `LIKE` | `idx_investor_name` | (contains는 풀스캔 가능 — 소량 OK) |
| Q4 | 목록 + pagination | Q2 + `LIMIT` | rows·cost 확인 |

---

## Q1. 증분 추출 — `updated_at`

### MariaDB / MySQL

```sql
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE updated_at > '2026-07-01 00:00:00'
ORDER BY updated_at, warehouse_io_id;
```

**기대 (MariaDB/MySQL `EXPLAIN` 출력):**

| column | 기대값 |
|--------|--------|
| `type` | `range` (또는 `ref`) |
| `key` | `idx_warehouse_updated_at` |
| `Extra` | `Using index condition` 등 |

### PostgreSQL

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE updated_at > TIMESTAMP '2026-07-01 00:00:00'
ORDER BY updated_at, warehouse_io_id;
```

**기대:** `Index Scan using idx_warehouse_updated_at on tb_warehouse_io`

### Oracle

```sql
EXPLAIN PLAN FOR
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE updated_at > TIMESTAMP '2026-07-01 00:00:00'
ORDER BY updated_at, warehouse_io_id;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);
```

**기대:** `INDEX RANGE SCAN` on `IDX_WAREHOUSE_UPDATED_AT` (또는 동일 이름)

---

## Q2. 목록 조회 — 활성 행

```sql
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20 OFFSET 0;
```

**기대:** `key` = `idx_warehouse_active`

---

## Q3. 투자자 이름 검색

```sql
EXPLAIN
SELECT investor_id, investor_name, investor_grade, total_amount
FROM tb_investor
WHERE investor_name LIKE '%김%'
ORDER BY investor_id ASC
LIMIT 20;
```

**참고:** `%...%` contains 검색은 인덱스를 못 탈 수 있음. `EXPLAIN`에서 `type=ALL`이면 데이터 증가 시 `pg_trgm`/Full-Text 검토.

---

## Q4. 상세 — PK 조회

```sql
EXPLAIN
SELECT *
FROM tb_warehouse_io
WHERE warehouse_io_id = 1001 AND deleted_yn = 'N';
```

**기대:** `type=const` 또는 `eq_ref`, `key=PRIMARY`

---

## DB별 출력 읽는 법

### MariaDB / MySQL

| 컬럼 | 의미 |
|------|------|
| `type` | `ALL`(풀스캔) → `index` → `range` → `ref` → `const` (좋을수록 오른쪽) |
| `key` | 실제 사용된 인덱스 이름 (`NULL`이면 미사용) |
| `rows` | 예상 검사 행 수 (적을수록 좋음) |
| `Extra` | `Using filesort`, `Using temporary` — 추가 비용 신호 |

```sql
-- JSON 상세 (MariaDB 10.1+ / MySQL 5.6+)
EXPLAIN FORMAT=JSON
SELECT ... ;
```

### PostgreSQL

```
Index Scan using idx_warehouse_updated_at on tb_warehouse_io
  Index Cond: (updated_at > '2026-07-01 00:00:00'::timestamp without time zone)
```

| 패턴 | 의미 |
|------|------|
| `Seq Scan` | 풀 테이블 스캔 — 인덱스 미사용 |
| `Index Scan` / `Index Only Scan` | 인덱스 사용 (Only Scan이 더 유리) |
| `Bitmap Index Scan` | 중간 규모 범위 |

`EXPLAIN ANALYZE` — 실제 실행 시간·버퍼 읽기 포함.

### Oracle

`DBMS_XPLAN.DISPLAY` 출력에서:

| 접근 | 의미 |
|------|------|
| `TABLE ACCESS FULL` | 풀스캔 |
| `INDEX RANGE SCAN` | 인덱스 범위 스캔 (증분·범위에 적합) |
| `INDEX UNIQUE SCAN` | PK/유니크 |

---

## 인덱스 미사용 시 조치

1. **통계 갱신**
   - MySQL/MariaDB: `ANALYZE TABLE tb_warehouse_io;`
   - PostgreSQL: `ANALYZE tb_warehouse_io;`
   - Oracle: `EXEC DBMS_STATS.GATHER_TABLE_STATS(...);`

2. **쿼리·인덱스 컬럼 순서** — `WHERE` 선두 컬럼이 인덱스 선두와 일치하는지 확인

3. **선택도** — 테이블 행 수가 너무 적으면 optimizer가 풀스캔 선택 (데모 2~3행은 정상)

4. **함수/형변환** — `WHERE DATE(updated_at) = ...` 는 인덱스 무효 → `updated_at >= ... AND updated_at < ...`

---

## 체크리스트 (릴리즈 전)

- [ ] `schema.sql` + `data.sql` 적용 후 `explain-verify.sql` 실행
- [ ] Q1 `idx_warehouse_updated_at` 사용 확인
- [ ] Q2 `idx_warehouse_active` 사용 확인
- [ ] Q4 PK 조회 `const`/`eq_ref` 확인
- [ ] PostgreSQL: `EXPLAIN ANALYZE`로 실측 1회
- [ ] 운영 데이터 규모에서 `ANALYZE` 후 재확인

## 관련 문서

- [INDEX_GUIDE.md](./INDEX_GUIDE.md) — 인덱스 정의
- [SELECT_QUERY_GUIDE.md](./SELECT_QUERY_GUIDE.md) — projection·pagination
