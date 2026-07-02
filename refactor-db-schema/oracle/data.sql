-- =============================================================================
-- Refactor 시드 — Oracle MERGE Upsert (멱등)
-- MERGE: https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/MERGE.html
-- Upsert: ../SEED_UPSERT_GUIDE.md
-- =============================================================================

MERGE INTO tb_warehouse_io t
USING (
    SELECT 1001 AS warehouse_io_id, '서울1창고' AS warehouse_name, 'P-100' AS product_code,
           '산업용 센서' AS product_name, '전자부품' AS product_category,
           140 AS in_qty, 30 AS out_qty, 110 AS current_stock,
           '한빛유통' AS client_name, 'ACTIVE' AS status, 'N' AS deleted_yn,
           'seed' AS source_system, 'seed-batch-001' AS etl_batch_id FROM DUAL
    UNION ALL
    SELECT 1002, '부산2창고', 'P-220', '모터 모듈', '기계부품',
           90, 12, 78, '남해물산', 'ACTIVE', 'N', 'seed', 'seed-batch-001' FROM DUAL
) s
ON (t.warehouse_io_id = s.warehouse_io_id)
WHEN MATCHED THEN UPDATE SET
    t.warehouse_name   = s.warehouse_name,
    t.product_code     = s.product_code,
    t.product_name     = s.product_name,
    t.product_category = s.product_category,
    t.in_qty           = s.in_qty,
    t.out_qty          = s.out_qty,
    t.current_stock    = s.current_stock,
    t.client_name      = s.client_name,
    t.status           = s.status,
    t.deleted_yn       = s.deleted_yn,
    t.updated_at       = SYSTIMESTAMP,
    t.source_system    = s.source_system,
    t.etl_batch_id     = s.etl_batch_id
WHEN NOT MATCHED THEN INSERT (
    warehouse_io_id, warehouse_name, product_code, product_name, product_category,
    in_qty, out_qty, current_stock, client_name, status, deleted_yn,
    source_system, etl_batch_id
) VALUES (
    s.warehouse_io_id, s.warehouse_name, s.product_code, s.product_name, s.product_category,
    s.in_qty, s.out_qty, s.current_stock, s.client_name, s.status, s.deleted_yn,
    s.source_system, s.etl_batch_id
);

MERGE INTO tb_investor t
USING (
    SELECT 2001 AS investor_id, '김민수' AS investor_name, 'VIP' AS investor_grade,
           850000000 AS total_amount, '글로벌인컴펀드' AS last_product_name,
           '최근 요청: 월간 리포트 이메일 발송' AS screen_memo,
           'seed' AS source_system, 'seed-batch-001' AS etl_batch_id FROM DUAL
    UNION ALL
    SELECT 2002, '이서연', 'GOLD', 320000000, '국내배당주랩', '최근 문의: 수익률 변동 사유', 'seed', 'seed-batch-001' FROM DUAL
    UNION ALL
    SELECT 2003, '박준호', 'SILVER', 120000000, '안정형채권플랜', '만기 도래 예정 상품 설명 필요', 'seed', 'seed-batch-001' FROM DUAL
) s
ON (t.investor_id = s.investor_id)
WHEN MATCHED THEN UPDATE SET
    t.investor_name     = s.investor_name,
    t.investor_grade    = s.investor_grade,
    t.total_amount      = s.total_amount,
    t.last_product_name = s.last_product_name,
    t.screen_memo       = s.screen_memo,
    t.updated_at        = SYSTIMESTAMP,
    t.source_system     = s.source_system,
    t.etl_batch_id      = s.etl_batch_id
WHEN NOT MATCHED THEN INSERT (
    investor_id, investor_name, investor_grade, total_amount, last_product_name, screen_memo,
    source_system, etl_batch_id
) VALUES (
    s.investor_id, s.investor_name, s.investor_grade, s.total_amount, s.last_product_name, s.screen_memo,
    s.source_system, s.etl_batch_id
);
