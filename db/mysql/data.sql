-- =============================================================================
-- Refactor 시드 — MySQL Upsert (멱등)
-- ON DUPLICATE KEY UPDATE: https://dev.mysql.com/doc/refman/8.4/en/insert-on-duplicate.html
-- Upsert: ../SEED_UPSERT_GUIDE.md
-- =============================================================================

INSERT INTO tb_warehouse_io (
    warehouse_io_id, warehouse_name, product_code, product_name, product_category,
    in_qty, out_qty, current_stock, client_name, status, deleted_yn,
    source_system, etl_batch_id
) VALUES
    (1001, '서울1창고', 'P-100', '산업용 센서', '전자부품', 140, 30, 110, '한빛유통', 'ACTIVE', 'N', 'seed', 'seed-batch-001'),
    (1002, '부산2창고', 'P-220', '모터 모듈', '기계부품', 90, 12, 78, '남해물산', 'ACTIVE', 'N', 'seed', 'seed-batch-001')
AS new_row
ON DUPLICATE KEY UPDATE
    warehouse_name   = new_row.warehouse_name,
    product_code       = new_row.product_code,
    product_name       = new_row.product_name,
    product_category   = new_row.product_category,
    in_qty             = new_row.in_qty,
    out_qty            = new_row.out_qty,
    current_stock      = new_row.current_stock,
    client_name        = new_row.client_name,
    status             = new_row.status,
    deleted_yn         = new_row.deleted_yn,
    updated_at         = CURRENT_TIMESTAMP,
    source_system      = new_row.source_system,
    etl_batch_id       = new_row.etl_batch_id;

INSERT INTO tb_investor (
    investor_id, investor_name, investor_grade, total_amount, last_product_name, screen_memo,
    source_system, etl_batch_id
) VALUES
    (2001, '김민수', 'VIP', 850000000, '글로벌인컴펀드', '최근 요청: 월간 리포트 이메일 발송', 'seed', 'seed-batch-001'),
    (2002, '이서연', 'GOLD', 320000000, '국내배당주랩', '최근 문의: 수익률 변동 사유', 'seed', 'seed-batch-001'),
    (2003, '박준호', 'SILVER', 120000000, '안정형채권플랜', '만기 도래 예정 상품 설명 필요', 'seed', 'seed-batch-001')
AS new_row
ON DUPLICATE KEY UPDATE
    investor_name     = new_row.investor_name,
    investor_grade    = new_row.investor_grade,
    total_amount      = new_row.total_amount,
    last_product_name = new_row.last_product_name,
    screen_memo       = new_row.screen_memo,
    updated_at        = CURRENT_TIMESTAMP,
    source_system     = new_row.source_system,
    etl_batch_id      = new_row.etl_batch_id;
