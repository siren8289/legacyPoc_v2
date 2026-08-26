# 파티셔닝 가이드 (Partitioning)

데이터가 커지면 테이블을 **시간 축**으로 나눠, 쿼리가 필요한 파티션만 읽게 한다(partition pruning).  
현재 PoC `schema.sql`은 **비파티션** 테이블이다. 이 문서와 `*/partition-example.sql`은 **확장 시 참고용**이다.

## 언제 고려하는가

| 규모·패턴 | 권장 |
|-----------|------|
| 수백만 행 이하, 단일 DB | 인덱스 + 증분 ETL로 충분 — [INDEX_GUIDE.md](./INDEX_GUIDE.md), [CHANGE_TRACKING_GUIDE.md](./CHANGE_TRACKING_GUIDE.md) |
| 수천만 행+, 월별 아카이브·배치 | `PARTITION BY RANGE` 검토 |
| 일별 입출고 이력 누적 | `stock_date`(업무일) 기준 RANGE |
| CDC·증분 DW 적재 | `updated_at` 기준 RANGE (최근 파티션만 스캔) |
| 신규 적재·보관 정책 | `created_at` 기준 RANGE (오래된 파티션 DROP) |

## 파티션 키 후보 (`tb_warehouse_io`)

| 키 | 용도 | 쿼리 예 |
|----|------|---------|
| `created_at` | 적재 시점·보관 주기 (월별 DROP) | `WHERE created_at >= '2026-07-01'` |
| `updated_at` | 증분 ETL·최근 변경분 | `WHERE updated_at > :last_loaded_at` |
| `stock_date` | **업무 기준일** (입출고 발생일) — 스키마에 없으면 확장 시 `DATE NOT NULL` 추가 | `WHERE stock_date BETWEEN ...` |

`tb_investor`는 변경 빈도가 낮아 PoC에서는 파티셔닝 우선순위가 낮다. 필요 시 `created_at` RANGE로 동일 패턴 적용.

## 공식 문서

| DB | 문서 | 요약 |
|----|------|------|
| **MariaDB** | [Partitioning Overview](https://mariadb.com/kb/en/partitioning-overview/) | 큰 테이블을 subset으로 분할, 일부 파티션만 읽으면 쿼리 가속 |
| **MySQL** | [Partitioning](https://dev.mysql.com/doc/refman/8.4/en/partitioning.html) | 개념·RANGE/LIST/HASH/KEY 문법 |
| **PostgreSQL** | [Table Partitioning](https://www.postgresql.org/docs/current/ddl-partitioning.html) | 선언적 range/list/hash 파티션 |
| **Oracle** | [Partitioning Concepts](https://docs.oracle.com/en/database/oracle/oracle-database/19/cncpt/partitioning.html) | partitioned table 개념·관리 |

## RANGE 예시 (개념)

```sql
PARTITION BY RANGE (created_at) (
    PARTITION p2026_06 VALUES LESS THAN ('2026-07-01'),
    PARTITION p2026_07 VALUES LESS THAN ('2026-08-01'),
    PARTITION p_future  VALUES LESS THAN (MAXVALUE)
);
```

증분 ETL은 `updated_at` 파티션 + `WHERE updated_at > :watermark` 조합 시 **최근 파티션만** 스캔할 수 있다.

```sql
SELECT warehouse_io_id, warehouse_name, updated_at
FROM tb_warehouse_io
WHERE updated_at > :last_loaded_at
ORDER BY updated_at, warehouse_io_id;
```

## DB별 주의사항

### MariaDB / MySQL

- 파티션 표현식에 쓰는 컬럼은 **모든 UNIQUE 키(PK 포함)에 포함**되어야 한다.  
  → `PARTITION BY RANGE (created_at)` 이면 PK를 `(created_at, warehouse_io_id)` 등 **복합 PK**로 설계하거나, 파티션 키를 PK에 넣는다.
- [Partition Pruning](https://dev.mysql.com/doc/refman/8.4/en/partitioning-pruning.html) — `WHERE`에 파티션 키 상수/범위가 있어야 pruning.
- `AUTO_INCREMENT`는 파티션 테이블에서도 동작하나, PK 설계와 함께 검증.

### PostgreSQL

- **선언적 파티션**: 부모 `PARTITION BY RANGE (created_at)`, 자식 `tb_warehouse_io_2026_07 FOR VALUES FROM ... TO ...`.
- PK는 **파티션 키를 포함**해야 한다: `PRIMARY KEY (created_at, warehouse_io_id)`.
- `CREATE INDEX ON ONLY parent` + 자식별 인덱스 또는 부모에 통합 인덱스 — [Indexing on Partitioned Tables](https://www.postgresql.org/docs/current/ddl-partitioning.html#DDL-PARTITIONING-INDEXING).
- 증분 쿼리: `updated_at` 인덱스는 파티션별로 생성되거나 부모에 정의.

### Oracle

- `PARTITION BY RANGE (created_at)` + `INTERVAL` 자동 파티션 생성 가능.
- Local vs Global 인덱스 선택 — 파티션 DROP 시 local이 유리한 경우 많음.
- Identity PK + RANGE 파티션: 파티션 키와 PK 정렬 규칙 확인.

## PoC vs 운영

| 항목 | PoC (`schema.sql`) | 운영 확장 |
|------|-------------------|-----------|
| 테이블 | 단일 비파티션 | `partition-example.sql` 참고 |
| `stock_date` | 없음 | 입출고 **업무일** 필요 시 `DATE NOT NULL` 추가 후 RANGE |
| JPA | `@Table` 단일 엔티티 | 파티션 전환 시 스키마 마이그레이션·배치 DDL 별도 |
| 보관 | soft delete | 오래된 파티션 `DROP PARTITION` / detach |

## 마이그레이션 순서 (요약)

1. 파티션 키 컬럼 확정 (`created_at` 이미 있음 / `stock_date` 추가 여부).
2. PK를 `(partition_key, warehouse_io_id)` 형태로 재설계.
3. 새 파티션 테이블 생성 → 데이터 이관 → 스왑(또는 점진 이관).
4. 월별 `ADD PARTITION` / PostgreSQL `CREATE TABLE ... PARTITION OF` 자동화.
5. EXPLAIN으로 partition pruning 확인 — [EXPLAIN_GUIDE.md](./EXPLAIN_GUIDE.md).

## 예시 DDL 위치

| DB | 파일 |
|----|------|
| MariaDB | [mariadb/partition-example.sql](./mariadb/partition-example.sql) |
| MySQL | [mysql/partition-example.sql](./mysql/partition-example.sql) |
| PostgreSQL | [postgresql/partition-example.sql](./postgresql/partition-example.sql) |
| Oracle | [oracle/partition-example.sql](./oracle/partition-example.sql) |

**실행:** PoC 초기화(`schema.sql`)와 분리. 참고·리허설용이며 기본 앱 JPA 스키마와는 다르다(PK 복합 등).

## 체크리스트

- [ ] 파티션 키가 모든 UNIQUE/PK에 포함되는가 (MySQL/MariaDB/PostgreSQL)
- [ ] 증분 ETL `WHERE updated_at > ?` 가 pruning 또는 인덱스와 맞는가
- [ ] 월별 파티션 추가·오래된 파티션 DROP 절차가 있는가
- [ ] `stock_date` 업무일 파티션이면 API·도메인에 일자 필드 정의했는가
