# Seed / Upsert 가이드

초기 데이터(`data.sql`)를 **여러 번 실행해도 중복·오류 없이** 맞추는 멱등 시드 패턴.

## 공식 문서

| DB | 문서 | 문법 |
|----|------|------|
| **MariaDB** | [INSERT ... ON DUPLICATE KEY UPDATE](https://mariadb.com/kb/en/insert-on-duplicate-key-update/) | PK/UNIQUE 충돌 시 UPDATE |
| **MySQL** | [INSERT ... ON DUPLICATE KEY UPDATE](https://dev.mysql.com/doc/refman/8.4/en/insert-on-duplicate.html) | 동일. 8.0.19+ alias 권장 |
| **PostgreSQL** | [INSERT ... ON CONFLICT](https://www.postgresql.org/docs/current/sql-insert.html#SQL-ON-CONFLICT) | `DO UPDATE` / `DO NOTHING` |
| **Oracle** | [MERGE](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/MERGE.html) | `WHEN MATCHED` / `WHEN NOT MATCHED` |

## 시드 파일 위치

| DB | 파일 |
|----|------|
| MariaDB | `mariadb/data.sql` |
| MySQL | `mysql/data.sql` |
| PostgreSQL | `postgresql/data.sql` |
| Oracle | `oracle/data.sql` |

## 적용 순서

```bash
mysql -u legacy_user -plegacy_pass legacy_refactor < refactor-db-schema/mariadb/schema.sql
mysql -u legacy_user -plegacy_pass legacy_refactor < refactor-db-schema/mariadb/data.sql
# 재실행해도 멱등 (upsert)
mysql -u legacy_user -plegacy_pass legacy_refactor < refactor-db-schema/mariadb/data.sql
```

---

## MariaDB

```sql
INSERT INTO tb_investor (
    investor_id, investor_name, investor_grade, total_amount, last_product_name, screen_memo
) VALUES
    (2001, '김민수', 'VIP', 850000000, '글로벌인컴펀드', '최근 요청: 월간 리포트 이메일 발송')
ON DUPLICATE KEY UPDATE
    investor_name     = VALUES(investor_name),
    investor_grade    = VALUES(investor_grade),
    total_amount      = VALUES(total_amount),
    last_product_name = VALUES(last_product_name),
    screen_memo       = VALUES(screen_memo);
```

| 항목 | 설명 |
|------|------|
| 충돌 키 | `PRIMARY KEY` (`investor_id`) |
| 동작 | 없으면 INSERT, 있으면 UPDATE |
| `VALUES(col)` | 삽입하려던 값으로 갱신 |

---

## MySQL 8.0.19+

```sql
INSERT INTO tb_investor (...) VALUES (...)
AS new_row
ON DUPLICATE KEY UPDATE
    investor_name = new_row.investor_name;
```

`VALUES()` 대신 **row alias** (`AS new_row`) 사용 권장.

---

## PostgreSQL

```sql
INSERT INTO tb_investor (
    investor_id, investor_name, investor_grade, total_amount, last_product_name, screen_memo
) VALUES
    (2001, '김민수', 'VIP', 850000000, '글로벌인컴펀드', '최근 요청: 월간 리포트 이메일 발송')
ON CONFLICT (investor_id) DO UPDATE SET
    investor_name     = EXCLUDED.investor_name,
    investor_grade    = EXCLUDED.investor_grade,
    total_amount      = EXCLUDED.total_amount,
    last_product_name = EXCLUDED.last_product_name,
    screen_memo       = EXCLUDED.screen_memo;
```

| 항목 | 설명 |
|------|------|
| `ON CONFLICT (investor_id)` | PK 충돌 대상 명시 |
| `EXCLUDED.*` | 이번 INSERT에서 제안된 값 |
| 시퀀스 | 시드 후 `setval(...)` — [PK_IDENTITY_GUIDE.md](./PK_IDENTITY_GUIDE.md) |

`DO NOTHING` — 변경 없이 스킵만 할 때:

```sql
ON CONFLICT (investor_id) DO NOTHING;
```

---

## Oracle

```sql
MERGE INTO tb_investor t
USING (
    SELECT 2001 AS investor_id, '김민수' AS investor_name, ... FROM DUAL
    UNION ALL
    SELECT 2002, '이서연', ... FROM DUAL
) s
ON (t.investor_id = s.investor_id)
WHEN MATCHED THEN UPDATE SET
    t.investor_name = s.investor_name
WHEN NOT MATCHED THEN INSERT (investor_id, investor_name, ...)
    VALUES (s.investor_id, s.investor_name, ...);
```

여러 행은 `USING` 절에 `UNION ALL`로 묶어 한 번에 MERGE.

---

## 이전 패턴 vs Upsert

| 패턴 | 예 | 단점 |
|------|-----|------|
| `WHERE NOT EXISTS` | `INSERT ... SELECT ... WHERE NOT EXISTS` | 행마다 서브쿼리, UPDATE 없음 |
| **Upsert** (채택) | `ON DUPLICATE KEY` / `ON CONFLICT` / `MERGE` | 재실행 시 데이터 동기화 가능 |

## 시드 ID 규칙

| 테이블 | ID 범위 | 비고 |
|--------|---------|------|
| `tb_warehouse_io` | 1001, 1002 | [PK_IDENTITY_GUIDE](./PK_IDENTITY_GUIDE.md) |
| `tb_investor` | 2001~2003 | 메모리 Repository와 동일 |

## 체크리스트

- [ ] `schema.sql` 후 `data.sql` 실행
- [ ] `data.sql` **2회 실행** — row 수 증가 없음 (멱등)
- [ ] 시드 행 수: warehouse 2, investor 3
- [ ] PostgreSQL: `setval` 후 다음 JPA INSERT ID 정상
- [ ] Upsert 후 `explain-verify.sql` 실행

## 관련 문서

- [PK_IDENTITY_GUIDE.md](./PK_IDENTITY_GUIDE.md) — 시드 ID·시퀀스
- [EXPLAIN_GUIDE.md](./EXPLAIN_GUIDE.md) — 시드 후 실행계획
