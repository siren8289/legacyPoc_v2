-- =============================================================================
-- 실행계획 검증 — MariaDB (legacy_refactor)
-- 사용: mysql -u legacy_user -plegacy_pass legacy_refactor < explain-verify.sql
-- 문서: https://mariadb.com/kb/en/explain/
-- =============================================================================

SELECT '=== Q1 증분 (idx_warehouse_updated_at) ===' AS section;
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE updated_at > '2026-07-01 00:00:00'
ORDER BY updated_at, warehouse_io_id;

SELECT '=== Q2 목록 (idx_warehouse_active) ===' AS section;
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20 OFFSET 0;

SELECT '=== Q3 투자자 검색 (idx_investor_name) ===' AS section;
EXPLAIN
SELECT investor_id, investor_name, investor_grade, total_amount
FROM tb_investor
WHERE investor_name LIKE '%김%'
ORDER BY investor_id ASC
LIMIT 20;

SELECT '=== Q4 PK 상세 ===' AS section;
EXPLAIN
SELECT warehouse_io_id, warehouse_name, product_name, current_stock, status
FROM tb_warehouse_io
WHERE warehouse_io_id = 1001 AND deleted_yn = 'N';

-- 통계 갱신 (인덱스 미사용 시 재시도)
-- ANALYZE TABLE tb_warehouse_io, tb_investor;
