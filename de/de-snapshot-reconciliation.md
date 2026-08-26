# DE Snapshot / Reconciliation 원칙

LegacyPOC DE의 목적은 대규모 신규 파이프라인 구축이 아니라, **legacy/refactor 데이터 snapshot의 grain, schema, lineage, reconciliation을 고정해 같은 데이터 기준으로 비교하는 것**입니다. 비교 단위는 입출고 record 또는 도메인에서 정의한 최소 업무 단위로 고정하고, legacy DB와 refactor DB에서 비교할 business key, nullable, type, quantity/status, deleted flag, source timestamp를 명시합니다.

마이그레이션과 snapshot 흐름은 다음 순서로 관리합니다.

```text
Legacy Snapshot
→ Schema Normalize
→ Refactor Load
→ Reconciliation
→ QA Evidence
```

raw snapshot은 변경하지 않고 hash와 count를 남깁니다. 변환 rule은 column mapping, type conversion, default/null handling, deleted record 정책으로 문서화하고, 동일 snapshot을 재실행해도 duplicate row가 생기지 않도록 business key와 upsert 정책을 고정합니다.

| DE 검증 축 | 기준 |
| --- | --- |
| Data Contract | business key, nullable, type, quantity/status, deleted flag, source timestamp, grain 명시 |
| Snapshot Flow | raw snapshot 보존, hash/count 기록, normalize/load/reconciliation evidence 분리 |
| Idempotency | 동일 snapshot 재실행 시 duplicate row 없음, business key/upsert 정책 명시 |
| Batch Metadata | migration batch마다 `run_id`, source version, row count, reject count, start/end time 기록 |
| Data Quality | key not-null, key unique, 수량 범위, status accepted values, FK, source/target row-count reconciliation 검사 |
| Performance Evidence | 대량 비교는 DB aggregate/count/hash로 처리하고, 인덱스/쿼리 변경은 `EXPLAIN ANALYZE`로 검증 |
| Lineage | source snapshot → mapping version → target table → comparison query → QA result 연결 |

dbt를 도입하는 경우 built-in data tests와 custom comparison test를 활용합니다. Airflow를 도입하는 경우 DAG는 ingestion 순서와 batch metadata를 남기는 orchestration 역할로 제한하고, 데이터 정합성 판단은 reconciliation query와 QA evidence로 분리합니다.

공식문서 기준은 다음 항목을 중심으로 둡니다.

| 영역 | 공식문서 |
| --- | --- |
| 데이터 테스트 | dbt Data Tests |
| 원천 데이터 선언 | dbt Sources |
| 오케스트레이션 | Airflow Core Concepts |
| 실행계획 | PostgreSQL `EXPLAIN` |

### 2-2-1. Public Data Ingestion 확장

Legacy/refactor 비교 fixture는 실제 공개 데이터로 보강할 수 있습니다. 주 데이터는 한국벤처투자 모태펀드 자조합 운용사정보, 금융위원회 KRX상장종목정보, 금융위원회 공시정보로 구성합니다.

| Source | Raw | Normalized Target | 목적 |
| --- | --- | --- | --- |
| 모태펀드 자조합 운용사정보 | `raw_fund_manager` | `investment_company` / `investor` | 운용사 검색·인덱스 비교 |
| KRX상장종목정보 | `raw_krx_company` | `company` | 기업 master·paging·검색 |
| 금융위원회 공시정보 | `raw_disclosure` | `disclosure` | 1:N JOIN·필터·조회 최적화 |

ingestion은 다음 순서로 처리합니다.

```text
Extract
→ Raw Snapshot
→ Validate
→ Normalize
→ Upsert
→ Reconciliation
→ Benchmark Fixture
```

API/CSV 원본은 raw 영역에 보존하고 `source_system`, `source_updated_at`, `fetched_at`, `etl_batch_id`를 기록합니다. KRX와 공시 API는 데이터 시점 차이를 줄이기 위해 benchmark 실행 전에 snapshot으로 고정합니다.

추가 data quality 규칙은 다음과 같습니다.

| Source | 추가 검사 |
| --- | --- |
| KRX | ISIN, 단축코드, 종목명 필수 여부 및 중복 검사 |
| 운용사 | 대표운영사명 null/duplicate 정책 정의, 자조합 규모 숫자형 검증 |
| 공시 | 기업 식별자, 공시 유형, 기준일자 검증 및 orphan disclosure 검사 |
| 공통 | source/target row count, reject count 기록, 동일 batch 재실행 시 upsert/idempotency 검증 |

공개 데이터는 단순 적재로 끝내지 않고 `Investor 검색`, `Company 목록`, `Company ↔ Disclosure JOIN`을 대표 benchmark query로 고정합니다. legacy와 refactor가 동일 snapshot과 조건을 사용하도록 하고, 인덱스 전/후 실행계획, scan 범위, query count, paging 여부를 evidence로 남깁니다. 측정 전에는 개선율을 확정 수치로 쓰지 않습니다.

공식 데이터 링크는 다음을 기준으로 합니다.

| 데이터 | 링크 |
| --- | --- |
| 한국벤처투자 모태펀드 자조합 운용사정보 | [공공데이터포털](https://www.data.go.kr/data/3060708/fileData.do?recommendDataYn=Y) |
| 금융위원회 KRX상장종목정보 | [공공데이터포털](https://www.data.go.kr/data/15094775/openapi.do) |
| 금융위원회 공시정보 | [공공데이터포털](https://www.data.go.kr/data/15059649/openapi.do) |

---
