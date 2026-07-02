-- =============================================================================
-- Refactor 시드 — PostgreSQL Upsert (멱등)
-- ON CONFLICT: https://www.postgresql.org/docs/current/sql-insert.html#SQL-ON-CONFLICT
-- Upsert: ../SEED_UPSERT_GUIDE.md
-- =============================================================================

INSERT INTO tb_warehouse_io (
    warehouse_io_id, warehouse_name, product_code, product_name, product_category,
    in_qty, out_qty, current_stock, client_name, status, deleted_yn,
    source_system, etl_batch_id
) VALUES
    (1001, '서울1창고', 'P-100', '산업용 센서', '전자부품', 140, 30, 110, '한빛유통', 'ACTIVE', 'N', 'seed', 'seed-batch-001'),
    (1002, '부산2창고', 'P-220', '모터 모듈', '기계부품', 90, 12, 78, '남해물산', 'ACTIVE', 'N', 'seed', 'seed-batch-001')
ON CONFLICT (warehouse_io_id) DO UPDATE SET
    warehouse_name   = EXCLUDED.warehouse_name,
    product_code     = EXCLUDED.product_code,
    product_name     = EXCLUDED.product_name,
    product_category = EXCLUDED.product_category,
    in_qty           = EXCLUDED.in_qty,
    out_qty          = EXCLUDED.out_qty,
    current_stock    = EXCLUDED.current_stock,
    client_name      = EXCLUDED.client_name,
    status           = EXCLUDED.status,
    deleted_yn       = EXCLUDED.deleted_yn,
    updated_at       = CURRENT_TIMESTAMP,
    source_system    = EXCLUDED.source_system,
    etl_batch_id     = EXCLUDED.etl_batch_id;

INSERT INTO tb_investor (
    investor_id, investor_name, investor_grade, total_amount, last_product_name, screen_memo,
    source_system, etl_batch_id
) VALUES
    (2001, '김민수', 'VIP', 850000000, '글로벌인컴펀드', '최근 요청: 월간 리포트 이메일 발송', 'seed', 'seed-batch-001'),
    (2002, '이서연', 'GOLD', 320000000, '국내배당주랩', '최근 문의: 수익률 변동 사유', 'seed', 'seed-batch-001'),
    (2003, '박준호', 'SILVER', 120000000, '안정형채권플랜', '만기 도래 예정 상품 설명 필요', 'seed', 'seed-batch-001')
ON CONFLICT (investor_id) DO UPDATE SET
    investor_name     = EXCLUDED.investor_name,
    investor_grade    = EXCLUDED.investor_grade,
    total_amount      = EXCLUDED.total_amount,
    last_product_name = EXCLUDED.last_product_name,
    screen_memo       = EXCLUDED.screen_memo,
    updated_at        = CURRENT_TIMESTAMP,
    source_system     = EXCLUDED.source_system,
    etl_batch_id      = EXCLUDED.etl_batch_id;

-- IDENTITY 시퀀스를 시드 ID 이후로 맞춘다.
SELECT setval(pg_get_serial_sequence('tb_warehouse_io', 'warehouse_io_id'),
              GREATEST((SELECT COALESCE(MAX(warehouse_io_id), 1) FROM tb_warehouse_io), 1002));
SELECT setval(pg_get_serial_sequence('tb_investor', 'investor_id'),
              GREATEST((SELECT COALESCE(MAX(investor_id), 1) FROM tb_investor), 2003));
