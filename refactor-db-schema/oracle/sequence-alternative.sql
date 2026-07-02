-- =============================================================================
-- Oracle SEQUENCE 대안 (참고용 — 본 프로젝트는 Identity Column 사용)
-- CREATE SEQUENCE: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-SEQUENCE.html
--
-- JPA에서 사용 시:
--   @SequenceGenerator(name = "warehouse_io_seq", sequenceName = "SEQ_WAREHOUSE_IO", allocationSize = 1)
--   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_io_seq")
-- =============================================================================

CREATE SEQUENCE seq_warehouse_io
    START WITH 1001
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE seq_investor
    START WITH 2001
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Identity 대신 SEQUENCE를 쓸 때 테이블 예시 (IDENTITY 절 없음):
-- CREATE TABLE tb_warehouse_io (
--     warehouse_io_id NUMBER(19) DEFAULT seq_warehouse_io.NEXTVAL NOT NULL,
--     ...
--     CONSTRAINT pk_tb_warehouse_io PRIMARY KEY (warehouse_io_id)
-- );
