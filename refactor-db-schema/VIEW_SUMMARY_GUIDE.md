# View / Materialized View / Summary Table 가이드

조회가 무거운 **집계**는 API·리포트 요청마다 `GROUP BY`를 돌리지 말고, 요약 구조를 둔다.

| 방식 | 저장 | 갱신 | 적합한 경우 |
|------|------|------|-------------|
| **VIEW** | 없음 (쿼리 재작성) | 항상 최신 | 단순 필터·조인, 행 수 적음 |
| **Summary Table** | 물리 테이블 | 배치/트리거/앱이 `INSERT`·`UPSERT` | MariaDB/MySQL **성능 개선** (MV 없음) |
| **MATERIALIZED VIEW** | 스냅샷 | `REFRESH` | PostgreSQL·Oracle 공식 기능 |

PoC 앱 JPA는 `tb_warehouse_io` / `tb_investor` 직접 조회.  
아래 예시는 **리포트·대시보드·DE**용 참고 DDL (`summary-views-example.sql`)이다.

## 공식 문서

| DB | VIEW | Materialized View |
|----|------|-------------------|
| **MariaDB** | [CREATE VIEW](https://mariadb.com/kb/en/create-view/) | 기본 미지원 → **summary table** |
| **MySQL** | [CREATE VIEW](https://dev.mysql.com/doc/refman/8.4/en/create-view.html) | 기본 미지원 → **summary table** |
| **PostgreSQL** | [CREATE VIEW](https://www.postgresql.org/docs/current/sql-createview.html) | [CREATE MATERIALIZED VIEW](https://www.postgresql.org/docs/current/sql-creatematerializedview.html) |
| **Oracle** | [CREATE VIEW](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-VIEW.html) | [CREATE MATERIALIZED VIEW](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-MATERIALIZED-VIEW.html) |

## 언제 무엇을 쓰는가

```
매 요청 GROUP BY (느림)
        ↓
   VIEW?  → 행 많으면 여전히 느림 (매번 집계)
        ↓
Summary / MV → 미리 계산해 두고 읽기만
        ↓
   배치 REFRESH (분·시간 단위) 또는 ETL 후 UPSERT
```

| 시나리오 | 권장 |
|----------|------|
| 활성 입출고 목록 (`deleted_yn = 'N'`) | **VIEW** — [SELECT_QUERY_GUIDE.md](./SELECT_QUERY_GUIDE.md)와 동일 필터 |
| 창고별 재고 합계 대시보드 | **Summary table** (MariaDB/MySQL) 또는 **MV** (PG/Oracle) |
| 등급별 투자자 수·금액 합계 | 동일 |
| 실시간에 가깝게 | VIEW 또는 summary + 짧은 주기 REFRESH |
| 일 1회 리포트 | MV/summary + 야간 배치 REFRESH |

## 예시 도메인 집계

### 1. VIEW — 활성 입출고 (`vw_warehouse_io_active`)

```sql
CREATE VIEW vw_warehouse_io_active AS
SELECT warehouse_io_id, warehouse_name, product_name, current_stock, status
FROM tb_warehouse_io
WHERE deleted_yn = 'N';
```

- 목록 API와 동일 조건을 SQL 레이어로 캡슐화
- BI 도구·읽기 전용 계정에 VIEW만 GRANT 가능

### 2. 창고별 재고 요약 (`summary` / `mv`)

```sql
-- 집계 원본 (매번 실행하면 비쌈)
SELECT warehouse_name,
       COUNT(*)           AS row_count,
       SUM(current_stock) AS total_stock,
       SUM(in_qty)        AS total_in_qty,
       SUM(out_qty)       AS total_out_qty
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
GROUP BY warehouse_name;
```

**MariaDB/MySQL:** `tb_summary_warehouse_stock` 테이블 + 배치 `REPLACE`/`UPSERT`  
**PostgreSQL/Oracle:** `MATERIALIZED VIEW` + `REFRESH`

### 3. 등급별 투자자 요약

```sql
SELECT investor_grade,
       COUNT(*)         AS investor_count,
       SUM(total_amount) AS sum_amount
FROM tb_investor
GROUP BY investor_grade;
```

## MariaDB / MySQL — VIEW + Summary Table

Materialized View가 없으므로 **요약 전용 테이블**을 직접 만든다.

```sql
CREATE TABLE tb_summary_warehouse_stock (
    warehouse_name  VARCHAR(100) NOT NULL,
    row_count       INT          NOT NULL DEFAULT 0,
    total_stock     BIGINT       NOT NULL DEFAULT 0,
    total_in_qty    BIGINT       NOT NULL DEFAULT 0,
    total_out_qty   BIGINT       NOT NULL DEFAULT 0,
    refreshed_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (warehouse_name)
);
```

갱신 패턴 (배치·이벤트 후):

```sql
INSERT INTO tb_summary_warehouse_stock (...)
SELECT ... GROUP BY warehouse_name
ON DUPLICATE KEY UPDATE
    row_count    = VALUES(row_count),
    total_stock  = VALUES(total_stock),
    ...
    refreshed_at = CURRENT_TIMESTAMP;
```

- [CHANGE_TRACKING_GUIDE.md](./CHANGE_TRACKING_GUIDE.md)의 `updated_at` 증분 ETL **이후** summary 갱신을 같은 배치에 묶을 수 있다.
- 트리거로 실시간 유지도 가능하나 PoC·운영 초기에는 **배치 UPSERT**가 단순하다.

## PostgreSQL — MATERIALIZED VIEW

```sql
CREATE MATERIALIZED VIEW mv_summary_investor_grade AS
SELECT investor_grade,
       COUNT(*)::BIGINT AS investor_count,
       COALESCE(SUM(total_amount), 0) AS sum_amount
FROM tb_investor
GROUP BY investor_grade
WITH NO DATA;

REFRESH MATERIALIZED VIEW mv_summary_investor_grade;

-- 동시 읽기 허용 시 UNIQUE 인덱스 후:
-- REFRESH MATERIALIZED VIEW CONCURRENTLY mv_summary_investor_grade;
```

- `REFRESH`는 스냅샷 교체 — 주기(cron, pg_cron, Airflow) 명시
- [CREATE MATERIALIZED VIEW](https://www.postgresql.org/docs/current/sql-creatematerializedview.html) — `WITH DATA` / `WITH NO DATA`, `CONCURRENTLY`

## Oracle — MATERIALIZED VIEW

```sql
CREATE MATERIALIZED VIEW mv_summary_investor_grade
BUILD IMMEDIATE
REFRESH COMPLETE ON DEMAND
AS
SELECT investor_grade,
       COUNT(*) AS investor_count,
       SUM(total_amount) AS sum_amount
FROM tb_investor
GROUP BY investor_grade;

-- EXEC DBMS_MVIEW.REFRESH('MV_SUMMARY_INVESTOR_GRADE');
```

- `REFRESH FAST`는 materialized view log 필요 — PoC 예시는 `COMPLETE ON DEMAND`
- [CREATE MATERIALIZED VIEW](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-MATERIALIZED-VIEW.html)

## JPA / 앱과의 관계

| 레이어 | 사용 |
|--------|------|
| API 목록·페이지 | `WarehouseListItem` projection — 운영 테이블 직접 ([SELECT_QUERY_GUIDE.md](./SELECT_QUERY_GUIDE.md)) |
| 대시보드·리포트 | VIEW / summary / MV — **별도 read repository** 또는 BI |
| 도메인 `WarehouseIo` | 요약 테이블 미매핑 (DE·리포트 전용) |

## PoC vs 운영

| 항목 | PoC | 운영 확장 |
|------|-----|-----------|
| `schema.sql` | VIEW/MV/summary **미포함** | `summary-views-example.sql` 참고 |
| 갱신 | 없음 | 배치 스케줄 + 모니터링 `refreshed_at` |
| EXPLAIN | 집계 원본 쿼리 | summary/MV 조회는 PK 스캔 수준 — [EXPLAIN_GUIDE.md](./EXPLAIN_GUIDE.md) |

## 예시 DDL 위치

| DB | 파일 |
|----|------|
| MariaDB | [mariadb/summary-views-example.sql](./mariadb/summary-views-example.sql) |
| MySQL | [mysql/summary-views-example.sql](./mysql/summary-views-example.sql) |
| PostgreSQL | [postgresql/summary-views-example.sql](./postgresql/summary-views-example.sql) |
| Oracle | [oracle/summary-views-example.sql](./oracle/summary-views-example.sql) |

`schema.sql` · `data.sql` 적용 **이후** 별도 실행.

## 체크리스트

- [ ] 무거운 쿼리를 VIEW만으로 해결하려 하지 않았는가 (VIEW는 집계 비용 그대로)
- [ ] summary/MV 갱신 주기·실패 알림이 있는가
- [ ] `refreshed_at` 또는 배치 ID로 신선도 표시하는가
- [ ] soft delete (`deleted_yn`)를 집계 WHERE에 반영했는가
- [ ] PostgreSQL `CONCURRENTLY` 사용 시 UNIQUE 인덱스를 달았는가
