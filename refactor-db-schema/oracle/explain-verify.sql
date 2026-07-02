-- =============================================================================
-- 실행계획 검증 — Oracle
-- SQL*Plus / SQLcl에서 schema 적용 후 실행
-- 문서: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/EXPLAIN-PLAN.html
-- =============================================================================

PROMPT === Q1 incremental (idx_warehouse_updated_at) ===
EXPLAIN PLAN FOR
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE updated_at > TIMESTAMP '2026-07-01 00:00:00'
ORDER BY updated_at, warehouse_io_id;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

PROMPT === Q2 list active (idx_warehouse_active) ===
EXPLAIN PLAN FOR
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

PROMPT === Q3 investor search (idx_investor_name) ===
EXPLAIN PLAN FOR
SELECT investor_id, investor_name, investor_grade, total_amount
FROM tb_investor
WHERE investor_name LIKE '%김%'
ORDER BY investor_id ASC
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

PROMPT === Q4 PK lookup ===
EXPLAIN PLAN FOR
SELECT warehouse_io_id, warehouse_name, product_name, current_stock, status
FROM tb_warehouse_io
WHERE warehouse_io_id = 1001 AND deleted_yn = 'N';
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY);

-- EXEC DBMS_STATS.GATHER_TABLE_STATS(USER, 'TB_WAREHOUSE_IO');
