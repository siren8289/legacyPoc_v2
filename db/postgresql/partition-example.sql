-- =============================================================================
-- 파티션 테이블 예시 — PostgreSQL (참고용, PoC schema.sql 과 별도)
-- 문서: ../PARTITIONING_GUIDE.md
-- Table Partitioning: https://www.postgresql.org/docs/current/ddl-partitioning.html
-- =============================================================================

-- DROP TABLE IF EXISTS tb_warehouse_io_part_2026_07 CASCADE;
-- DROP TABLE IF EXISTS tb_warehouse_io_part_2026_06 CASCADE;
-- DROP TABLE IF EXISTS tb_warehouse_io_part CASCADE;

CREATE TABLE tb_warehouse_io_part (
    warehouse_io_id   BIGINT        NOT NULL,
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
    stock_date        DATE          NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at        TIMESTAMP     NULL,
    source_system     VARCHAR(50)   NOT NULL DEFAULT 'refactor-api',
    etl_batch_id      VARCHAR(50)   NULL,
    CONSTRAINT pk_tb_warehouse_io_part PRIMARY KEY (created_at, warehouse_io_id),
    CONSTRAINT ck_tb_warehouse_io_part_deleted CHECK (deleted_yn IN ('Y', 'N'))
) PARTITION BY RANGE (created_at);

CREATE TABLE tb_warehouse_io_part_2026_06
    PARTITION OF tb_warehouse_io_part
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

CREATE TABLE tb_warehouse_io_part_2026_07
    PARTITION OF tb_warehouse_io_part
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE TABLE tb_warehouse_io_part_future
    PARTITION OF tb_warehouse_io_part
    FOR VALUES FROM ('2026-08-01') TO (MAXVALUE);

CREATE INDEX idx_wh_part_updated ON tb_warehouse_io_part (updated_at, warehouse_io_id);

-- EXPLAIN SELECT warehouse_io_id, updated_at
-- FROM tb_warehouse_io_part
-- WHERE updated_at > TIMESTAMP '2026-07-01 00:00:00'
-- ORDER BY updated_at, warehouse_io_id;
