-- =============================================================================
-- 실행계획 검증 — PostgreSQL (legacy_next_refactor)
-- 사용: psql -U legacy_user -d legacy_next_refactor -f explain-verify.sql
-- 문서: https://www.postgresql.org/docs/current/sql-explain.html
-- =============================================================================

\echo '=== Q1 incremental (idx_warehouse_updated_at) ==='
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE updated_at > TIMESTAMP '2026-07-01 00:00:00'
ORDER BY updated_at, warehouse_io_id;

\echo '=== Q2 list active (idx_warehouse_active) ==='
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20 OFFSET 0;

\echo '=== Q3 investor search (idx_investor_name) ==='
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT investor_id, investor_name, investor_grade, total_amount
FROM tb_investor
WHERE investor_name LIKE '%김%'
ORDER BY investor_id ASC
LIMIT 20;

\echo '=== Q4 PK lookup ==='
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT warehouse_io_id, warehouse_name, product_name, current_stock, status
FROM tb_warehouse_io
WHERE warehouse_io_id = 1001 AND deleted_yn = 'N';

-- ANALYZE tb_warehouse_io;
-- ANALYZE tb_investor;
