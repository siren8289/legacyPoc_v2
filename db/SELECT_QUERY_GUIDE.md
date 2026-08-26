# SELECT / LIMIT / Pagination 가이드

`SELECT *` 대신 **필요 컬럼만** 조회하고, `LIMIT`/`OFFSET`(또는 Oracle `FETCH`)으로 페이지네이션한다.

## 공식 문서

| DB | 문서 | Pagination |
|----|------|------------|
| **MariaDB** | [SELECT](https://mariadb.com/kb/en/select/) · [EXPLAIN](https://mariadb.com/kb/en/explain/) | `LIMIT n OFFSET m` |
| **MySQL** | [SELECT](https://dev.mysql.com/doc/refman/8.4/en/select.html) | `LIMIT offset, count` 또는 `LIMIT count OFFSET offset` |
| **PostgreSQL** | [SELECT](https://www.postgresql.org/docs/current/sql-select.html) | `LIMIT` / `OFFSET` 또는 `FETCH FIRST n ROWS ONLY` |
| **Oracle** | [SELECT](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html) | `OFFSET n ROWS FETCH NEXT m ROWS ONLY` |

## Anti-pattern vs 권장

```sql
-- ❌ SELECT * — 불필요 컬럼·I/O 증가
SELECT * FROM tb_warehouse_io WHERE deleted_yn = 'N';

-- ✅ 목록에 필요한 컬럼만
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20 OFFSET 0;
```

## DB별 Pagination SQL

### MariaDB / MySQL

```sql
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20 OFFSET 0;
```

### PostgreSQL

```sql
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20 OFFSET 0;

-- 또는 SQL:2008 표준
-- FETCH FIRST 20 ROWS ONLY OFFSET 0;
```

### Oracle

```sql
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;
```

## 애플리케이션 구현

### Projection record (도메인 목록 DTO)

```java
public record WarehouseListItem(
    Long warehouseIoId,
    String warehouseName,
    String productName,
    Integer currentStock
) {}
```

### JPA — constructor expression (`SELECT *` 없음)

```java
@Query("""
    SELECT new com.example.refactor.feature.warehouse.model.WarehouseListItem(
        w.warehouseIoId, w.warehouseName, w.productName, w.currentStock
    )
    FROM WarehouseJpaEntity w
    WHERE w.deletedYn = 'N'
    ORDER BY w.warehouseIoId DESC
    """)
List<WarehouseListItem> findActiveListItems(Pageable pageable);
```

Hibernate가 생성하는 SQL에 `LIMIT`/`OFFSET`(또는 dialect별 pagination)이 붙는다.

### REST API

```http
# 기존 — 전체 목록 (하위 호환)
GET /api/warehouses

# 페이지네이션 + projection
GET /api/warehouses?page=0&size=20
```

응답 (`PageResult`):

```json
{
  "data": {
    "items": [
      { "warehouseIoId": 1002, "warehouseName": "부산2창고", "productName": "모터 모듈", "currentStock": 78 }
    ],
    "totalCount": 2,
    "page": 0,
    "size": 20
  }
}
```

### 투자자 검색 + 페이지

```http
GET /api/investors?name=김&page=0&size=20
```

```sql
SELECT investor_id, investor_name, investor_grade, total_amount
FROM tb_investor
WHERE investor_name LIKE '%김%'
ORDER BY investor_id ASC
LIMIT 20 OFFSET 0;
```

## 계층별 역할

| 계층 | 역할 |
|------|------|
| `WarehouseListItem` | 목록 화면용 projection (4컬럼) |
| `WarehouseIo` | 상세·저장용 전체 도메인 |
| `SpringData*JpaRepository` | JPQL constructor + `Pageable` |
| `Jpa*Repository` | `PageResult` 조립 |
| Controller | `page`/`size` 있으면 projection 페이지, 없으면 기존 전체 |

## 성능 점검

```sql
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20;
```

- `idx_warehouse_active` — `(deleted_yn, warehouse_io_id DESC)` 활용 확인
- 상세: [EXPLAIN_GUIDE.md](./EXPLAIN_GUIDE.md) · 스크립트: `*/explain-verify.sql`

## 관련 파일

| 파일 | 설명 |
|------|------|
| `WarehouseListItem.java` / `InvestorListItem.java` | 목록 projection |
| `PageResult.java` | `items`, `totalCount`, `page`, `size` |
| `SpringDataWarehouseJpaRepository.findActiveListItems` | JPQL SELECT 명시 |
| `INDEX_GUIDE.md` | 목록·검색 인덱스 |

## 체크리스트

- [ ] 목록 API는 `SELECT *` / 전체 Entity 로딩 지양
- [ ] `page`+`size` 요청 시 projection + LIMIT
- [ ] `size` 상한 (예: 100)으로 과도한 조회 방지
- [ ] 상세 API만 `findById` → 전체 `WarehouseIo` 반환

상세: [EXPLAIN_GUIDE.md](./EXPLAIN_GUIDE.md)

무거운 **집계**는 VIEW만으로 부족할 수 있다 — [VIEW_SUMMARY_GUIDE.md](./VIEW_SUMMARY_GUIDE.md) (summary table / materialized view).
