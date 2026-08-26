-- =============================================================================
-- 파티션 테이블 예시 — MariaDB (참고용, PoC schema.sql 과 별도)
-- 문서: ../PARTITIONING_GUIDE.md
-- Overview: https://mariadb.com/kb/en/partitioning-overview/
-- =============================================================================
-- 주의: RANGE(created_at) 사용 시 PK에 created_at 포함 필수.
--       아래는 운영 확장 시 리허설용 DDL이다. 기본 앱은 비파티션 tb_warehouse_io 사용.

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
    stock_date        DATE          NOT NULL COMMENT '업무 기준일 (파티션 키 후보)',
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

-- 증분 ETL (updated_at) — 최근 파티션 + 인덱스 조합 검토
-- CREATE INDEX idx_wh_part_updated ON tb_warehouse_io_part (updated_at, warehouse_io_id);

-- EXPLAIN SELECT warehouse_io_id, updated_at
-- FROM tb_warehouse_io_part
-- WHERE updated_at > '2026-07-01 00:00:00'
-- ORDER BY updated_at, warehouse_io_id;
