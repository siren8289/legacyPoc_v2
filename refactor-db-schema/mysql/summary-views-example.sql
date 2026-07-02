-- =============================================================================
-- View / Summary Table 예시 — MySQL (참고용, schema.sql 적용 후 별도 실행)
-- 문서: ../VIEW_SUMMARY_GUIDE.md
-- CREATE VIEW: https://dev.mysql.com/doc/refman/8.4/en/create-view.html
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

CREATE TABLE IF NOT EXISTS tb_summary_warehouse_stock (
    warehouse_name  VARCHAR(100) NOT NULL,
    row_count       INT          NOT NULL DEFAULT 0,
    total_stock     BIGINT       NOT NULL DEFAULT 0,
    total_in_qty    BIGINT       NOT NULL DEFAULT 0,
    total_out_qty   BIGINT       NOT NULL DEFAULT 0,
    refreshed_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (warehouse_name)
);

INSERT INTO tb_summary_warehouse_stock (
    warehouse_name, row_count, total_stock, total_in_qty, total_out_qty, refreshed_at
)
SELECT warehouse_name,
       COUNT(*),
       COALESCE(SUM(current_stock), 0),
       COALESCE(SUM(in_qty), 0),
       COALESCE(SUM(out_qty), 0),
       CURRENT_TIMESTAMP
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
GROUP BY warehouse_name
AS new_row
ON DUPLICATE KEY UPDATE
    row_count     = new_row.row_count,
    total_stock   = new_row.total_stock,
    total_in_qty  = new_row.total_in_qty,
    total_out_qty = new_row.total_out_qty,
    refreshed_at  = new_row.refreshed_at;

CREATE OR REPLACE VIEW vw_investor_by_grade AS
SELECT investor_grade,
       COUNT(*) AS investor_count,
       COALESCE(SUM(total_amount), 0) AS sum_amount
FROM tb_investor
GROUP BY investor_grade;
