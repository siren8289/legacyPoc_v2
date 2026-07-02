-- =============================================================================
-- View / Materialized View 예시 — Oracle (참고용, schema.sql 적용 후 별도 실행)
-- 문서: ../VIEW_SUMMARY_GUIDE.md
-- CREATE VIEW: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-VIEW.html
-- CREATE MATERIALIZED VIEW: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-MATERIALIZED-VIEW.html
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

-- 창고별 재고 Materialized View
BEGIN
    EXECUTE IMMEDIATE 'DROP MATERIALIZED VIEW mv_summary_warehouse_stock';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -12003 THEN RAISE; END IF;
END;
/

CREATE MATERIALIZED VIEW mv_summary_warehouse_stock
    BUILD IMMEDIATE
    REFRESH COMPLETE ON DEMAND
AS
SELECT warehouse_name,
       COUNT(*) AS row_count,
       NVL(SUM(current_stock), 0) AS total_stock,
       NVL(SUM(in_qty), 0) AS total_in_qty,
       NVL(SUM(out_qty), 0) AS total_out_qty,
       SYSTIMESTAMP AS refreshed_at
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
GROUP BY warehouse_name;

CREATE UNIQUE INDEX uq_mv_summary_wh ON mv_summary_warehouse_stock (warehouse_name);

-- EXEC DBMS_MVIEW.REFRESH('MV_SUMMARY_WAREHOUSE_STOCK');

BEGIN
    EXECUTE IMMEDIATE 'DROP MATERIALIZED VIEW mv_summary_investor_grade';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -12003 THEN RAISE; END IF;
END;
/

CREATE MATERIALIZED VIEW mv_summary_investor_grade
    BUILD IMMEDIATE
    REFRESH COMPLETE ON DEMAND
AS
SELECT investor_grade,
       COUNT(*) AS investor_count,
       NVL(SUM(total_amount), 0) AS sum_amount,
       SYSTIMESTAMP AS refreshed_at
FROM tb_investor
GROUP BY investor_grade;

CREATE UNIQUE INDEX uq_mv_summary_inv_grade ON mv_summary_investor_grade (investor_grade);

-- 등급별 집계 VIEW (소량·즉시 최신)
CREATE OR REPLACE VIEW vw_investor_by_grade AS
SELECT investor_grade,
       COUNT(*) AS investor_count,
       NVL(SUM(total_amount), 0) AS sum_amount
FROM tb_investor
GROUP BY investor_grade;
