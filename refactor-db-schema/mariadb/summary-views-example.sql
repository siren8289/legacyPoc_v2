-- =============================================================================
-- View / Summary Table 예시 — MariaDB (참고용, schema.sql 적용 후 별도 실행)
-- 문서: ../VIEW_SUMMARY_GUIDE.md
-- CREATE VIEW: https://mariadb.com/kb/en/create-view/
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. VIEW — 활성 입출고 (매 조회 시 base table 스캔, 행 적으면 충분)
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- 2. Summary Table — 창고별 재고 합계 (Materialized View 대체)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_summary_warehouse_stock (
    warehouse_name  VARCHAR(100) NOT NULL,
    row_count       INT          NOT NULL DEFAULT 0,
    total_stock     BIGINT       NOT NULL DEFAULT 0,
    total_in_qty    BIGINT       NOT NULL DEFAULT 0,
    total_out_qty   BIGINT       NOT NULL DEFAULT 0,
    refreshed_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (warehouse_name)
);

-- 배치 갱신 (멱등 UPSERT)
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
ON DUPLICATE KEY UPDATE
    row_count    = VALUES(row_count),
    total_stock  = VALUES(total_stock),
    total_in_qty = VALUES(total_in_qty),
    total_out_qty = VALUES(total_out_qty),
    refreshed_at = CURRENT_TIMESTAMP;

-- 리포트 조회 (가벼움)
-- SELECT * FROM tb_summary_warehouse_stock ORDER BY warehouse_name;

-- ---------------------------------------------------------------------------
-- 3. VIEW — 등급별 투자자 (소량이면 VIEW, 대량이면 summary 테이블로 동일 패턴)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_investor_by_grade AS
SELECT investor_grade,
       COUNT(*) AS investor_count,
       COALESCE(SUM(total_amount), 0) AS sum_amount
FROM tb_investor
GROUP BY investor_grade;
