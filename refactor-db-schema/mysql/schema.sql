-- =============================================================================
-- Refactor DDL — MySQL (springboot-vue, :8082)
-- CREATE TABLE: https://dev.mysql.com/doc/refman/8.4/en/create-table.html
-- CREATE INDEX: https://dev.mysql.com/doc/refman/8.4/en/create-index.html
-- 변경 추적: ../CHANGE_TRACKING_GUIDE.md
-- 파티셔닝(확장 시): ../PARTITIONING_GUIDE.md · partition-example.sql
-- View/요약(리포트): ../VIEW_SUMMARY_GUIDE.md · summary-views-example.sql
-- =============================================================================

CREATE TABLE IF NOT EXISTS tb_warehouse_io (
    warehouse_io_id   BIGINT        NOT NULL AUTO_INCREMENT,
    warehouse_name    VARCHAR(100)  NOT NULL DEFAULT '',
    product_code      VARCHAR(40)   NOT NULL DEFAULT '',
    product_name      VARCHAR(100)  NOT NULL DEFAULT '',
    product_category  VARCHAR(60)   NOT NULL DEFAULT '',
    in_qty            INT           NOT NULL DEFAULT 0,
    out_qty           INT           NOT NULL DEFAULT 0,
    current_stock     INT           NOT NULL DEFAULT 0,
    client_name       VARCHAR(100)  NOT NULL DEFAULT '',
    status            VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    deleted_yn        CHAR(1)       NOT NULL DEFAULT 'N',
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        TIMESTAMP     NULL,
    source_system     VARCHAR(50)   NOT NULL DEFAULT 'refactor-api',
    etl_batch_id      VARCHAR(50)   NULL,
    CONSTRAINT pk_tb_warehouse_io PRIMARY KEY (warehouse_io_id),
    CONSTRAINT ck_tb_warehouse_io_deleted CHECK (deleted_yn IN ('Y', 'N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=1001;

CREATE TABLE IF NOT EXISTS tb_investor (
    investor_id         BIGINT        NOT NULL AUTO_INCREMENT,
    investor_name       VARCHAR(100)  NOT NULL,
    investor_grade      VARCHAR(20)   NOT NULL DEFAULT '',
    total_amount        BIGINT        NOT NULL DEFAULT 0,
    last_product_name   VARCHAR(100)  NOT NULL DEFAULT '',
    screen_memo         VARCHAR(500)  NOT NULL DEFAULT '',
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP     NULL,
    source_system       VARCHAR(50)   NOT NULL DEFAULT 'refactor-api',
    etl_batch_id        VARCHAR(50)   NULL,
    CONSTRAINT pk_tb_investor PRIMARY KEY (investor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=2001;

CREATE INDEX idx_warehouse_active ON tb_warehouse_io (deleted_yn, warehouse_io_id DESC);
CREATE INDEX idx_warehouse_updated_at ON tb_warehouse_io (updated_at, warehouse_io_id);
CREATE INDEX idx_investor_name ON tb_investor (investor_name);
CREATE INDEX idx_investor_updated_at ON tb_investor (updated_at, investor_id);
