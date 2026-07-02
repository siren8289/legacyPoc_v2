-- =============================================================================
-- View / Materialized View 예시 — PostgreSQL (참고용, schema.sql 적용 후 별도 실행)
-- 문서: ../VIEW_SUMMARY_GUIDE.md
-- CREATE VIEW: https://www.postgresql.org/docs/current/sql-createview.html
-- CREATE MATERIALIZED VIEW: https://www.postgresql.org/docs/current/sql-creatematerializedview.html
-- =============================================================================

CREATE OR REPLACE VIEW vw_warehouse_io_active AS
SELECT warehouse_io_id,
       warehouse_name,
       product_code,
       product_name,
       current_stock,
       status,
       updated_at
FROM tb_warehouse_io
WHERE deleted_yn = 'N';

-- 창고별 재고 — Materialized View
DROP MATERIALIZED VIEW IF EXISTS mv_summary_warehouse_stock;

CREATE MATERIALIZED VIEW mv_summary_warehouse_stock AS
SELECT warehouse_name,
       COUNT(*)::INT AS row_count,
       COALESCE(SUM(current_stock), 0)::BIGINT AS total_stock,
       COALESCE(SUM(in_qty), 0)::BIGINT AS total_in_qty,
       COALESCE(SUM(out_qty), 0)::BIGINT AS total_out_qty,
       CURRENT_TIMESTAMP AS refreshed_at
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
GROUP BY warehouse_name
WITH DATA;

CREATE UNIQUE INDEX uq_mv_summary_warehouse_stock ON mv_summary_warehouse_stock (warehouse_name);

-- 배치 갱신 (동시 읽기: CONCURRENTLY — UNIQUE 인덱스 필요)
-- REFRESH MATERIALIZED VIEW CONCURRENTLY mv_summary_warehouse_stock;
REFRESH MATERIALIZED VIEW mv_summary_warehouse_stock;

-- 등급별 투자자 — Materialized View
DROP MATERIALIZED VIEW IF EXISTS mv_summary_investor_grade;

CREATE MATERIALIZED VIEW mv_summary_investor_grade AS
SELECT investor_grade,
       COUNT(*)::BIGINT AS investor_count,
       COALESCE(SUM(total_amount), 0)::BIGINT AS sum_amount,
       CURRENT_TIMESTAMP AS refreshed_at
FROM tb_investor
GROUP BY investor_grade
WITH DATA;

CREATE UNIQUE INDEX uq_mv_summary_investor_grade ON mv_summary_investor_grade (investor_grade);

REFRESH MATERIALIZED VIEW mv_summary_investor_grade;

-- 리포트: SELECT * FROM mv_summary_warehouse_stock;
