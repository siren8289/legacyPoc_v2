-- =============================================================================
-- 파티션 테이블 예시 — MySQL (참고용, PoC schema.sql 과 별도)
-- 문서: ../PARTITIONING_GUIDE.md
-- Partitioning: https://dev.mysql.com/doc/refman/8.4/en/partitioning.html
-- =============================================================================

-- DROP TABLE IF EXISTS tb_warehouse_io_part;

CREATE TABLE tb_warehouse_io_part (
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
    stock_date        DATE          NOT NULL COMMENT '업무 기준일',
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        TIMESTAMP     NULL,
    source_system     VARCHAR(50)   NOT NULL DEFAULT 'refactor-api',
    etl_batch_id      VARCHAR(50)   NULL,
    CONSTRAINT pk_tb_warehouse_io_part PRIMARY KEY (created_at, warehouse_io_id),
    CONSTRAINT ck_tb_warehouse_io_part_deleted CHECK (deleted_yn IN ('Y', 'N'))
)
PARTITION BY RANGE (UNIX_TIMESTAMP(created_at)) (
    PARTITION p2026_06 VALUES LESS THAN (UNIX_TIMESTAMP('2026-07-01 00:00:00')),
    PARTITION p2026_07 VALUES LESS THAN (UNIX_TIMESTAMP('2026-08-01 00:00:00')),
    PARTITION p_future  VALUES LESS THAN MAXVALUE
);

-- stock_date 기준 RANGE 예시 (업무일 파티션)
-- PARTITION BY RANGE (TO_DAYS(stock_date)) ( ... );
