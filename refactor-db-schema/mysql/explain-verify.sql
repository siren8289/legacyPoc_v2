-- =============================================================================
-- 실행계획 검증 — MySQL (legacy_vue_refactor)
-- 사용: mysql -u legacy_user -plegacy_pass legacy_vue_refactor < explain-verify.sql
-- 문서: https://dev.mysql.com/doc/refman/8.4/en/explain.html
-- =============================================================================

SELECT '=== Q1 incremental (idx_warehouse_updated_at) ===' AS section;
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE updated_at > '2026-07-01 00:00:00'
ORDER BY updated_at, warehouse_io_id;

SELECT '=== Q2 list active (idx_warehouse_active) ===' AS section;
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20 OFFSET 0;

SELECT '=== Q3 investor search (idx_investor_name) ===' AS section;
EXPLAIN
SELECT investor_id, investor_name, investor_grade, total_amount
FROM tb_investor
WHERE investor_name LIKE '%김%'
ORDER BY investor_id ASC
LIMIT 20;

SELECT '=== Q4 PK lookup ===' AS section;
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock, status
FROM tb_warehouse_io
WHERE warehouse_io_id = 1001 AND deleted_yn = 'N';

-- MySQL 8.0.18+: 실제 실행 시간 포함
-- EXPLAIN ANALYZE
-- SELECT warehouse_io_id FROM tb_warehouse_io
-- WHERE updated_at > '2026-07-01 00:00:00';

-- ANALYZE TABLE tb_warehouse_io, tb_investor;
