# DA Measurement / Benchmark 원칙

LegacyPOC DA의 목적은 제품 성장 분석보다 **리팩터 전후 동작, DB 효율, 안정성 비교를 재현 가능한 측정으로 만드는 것**입니다. 기능 결과가 다르면 속도가 빨라도 성공으로 평가하지 않고, 동일 dataset과 동일 조건에서 legacy/refactor를 paired comparison으로 비교합니다.

## 핵심 분석 질문

| 질문 | 확인 대상 |
| --- | --- |
| save/list/search 결과가 legacy와 refactor에서 동일한가? | API contract parity, response body, status code, business result |
| 동일 dataset에서 query plan, latency, read rows/buffers가 어떻게 달라지는가? | DB plan, p50/p95 latency, rows read, buffer hit/read |
| 어떤 module/profile에서 smoke failure가 발생하는가? | legacy servlet, Spring Boot vanilla/vue/api, memory/jpa profile |
| migration 후 count, business key, quantity/status 불일치는 없는가? | reconciliation SQL, mismatch report, QA evidence |

## Benchmark metadata

benchmark는 실행 조건을 함께 저장합니다.

| Metadata | 예시 |
| --- | --- |
| Code | commit SHA, module, profile |
| Runtime | DB version, JVM/Node version, local/container 여부 |
| Dataset | snapshot id, row count, grain, source version |
| Execution | warm-up, iteration, concurrency, query parameter |
| Evidence | result CSV/log, plan text 또는 JSON, smoke report 경로 |

p50/p95 latency와 DB plan은 분리해 기록합니다. 작은 표본의 한 번 측정값은 성능 결론으로 사용하지 않고, query plan과 rows/buffers evidence를 함께 봅니다.

## 기본 metric

| Metric | 의미 | Evidence |
| --- | --- | --- |
| Parity Pass Rate | legacy/refactor 동일 입력 대비 동일 결과 비율 | contract parity test result |
| Migration Mismatch Count | count, key, quantity/status reconciliation 불일치 건수 | reconciliation SQL result |
| Query p95 | 반복 측정 기준 상위 지연 시간 | benchmark CSV/log |
| Rows Read | 쿼리 수행 시 읽은 row 범위 | DB execution plan |
| Buffer Hit/Read | PostgreSQL buffer hit/read 추적 | `EXPLAIN (ANALYZE, BUFFERS)` |
| Regression Count | 기능 또는 성능 회귀 건수 | regression report |
| Smoke Pass Rate | module/profile별 smoke test 통과율 | smoke test log |

분석 방법은 변경 전/후를 동일 데이터로 비교하고, 성능 개선과 기능 정확성 회귀를 동시에 보는 것입니다. benchmark script와 SQL은 version control에 보관하고, result CSV/log에는 environment metadata를 저장합니다. query plan은 plan text 또는 JSON으로 보존해 나중에 같은 조건에서 재현할 수 있게 합니다.

## 공식문서 기준

| 영역 | 공식문서 |
| --- | --- |
| PostgreSQL 실행계획 | PostgreSQL `EXPLAIN` |
| BigQuery 성능 | BigQuery Performance Best Practices, BigQuery를 쓸 경우에만 적용 |
| 사용자 행동 계측 | GA4 Events, 사용자 행동 계측을 추가하는 경우에만 적용 |
