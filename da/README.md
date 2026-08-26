
````markdown
# LegacyPOC — Migration Performance & Data Reconciliation Analytics

> **Legacy Servlet/JDBC 구조와 Spring Boot·FastAPI 리팩터 구조를 동일 데이터·동일 계약 기준으로 비교하여, 기능 정합성·DB 실행계획·지연시간·Migration 데이터 일치 여부를 측정하는 Data Analytics 프로젝트**

---

## 1. Project Overview

LegacyPOC는 일반적인 서비스 KPI 분석 프로젝트가 아니라  
**레거시 시스템을 현대적인 구조로 전환했을 때 실제로 무엇이 달라졌는지를 측정하는 비교형 프로젝트**입니다.

비교 대상은 다음과 같습니다.

```text
Legacy Servlet / JDBC
        ↓
Spring Boot Refactor
        ↓
FastAPI Refactor
````

단순히 코드 구조가 더 깔끔해졌다는 이유만으로 개선이라고 판단하지 않습니다.

DA 영역에서는 다음 질문을 데이터로 검증하는 데 집중했습니다.

```text
같은 입력인가?
      ↓
같은 결과가 나오는가?
      ↓
같은 Dataset을 사용하는가?
      ↓
Query Plan은 어떻게 달라졌는가?
      ↓
Latency / Rows / Buffers는 어떻게 달라졌는가?
      ↓
Migration 후 데이터는 동일한가?
```

즉,

> **Correctness → Performance → Reconciliation**

순서로 리팩터링 결과를 평가합니다.

---

# 2. Analytics Problem

레거시 시스템을 리팩터링하면 흔히 다음과 같은 표현을 사용합니다.

```text
"더 빨라졌다"
"DB가 최적화됐다"
"구조가 개선됐다"
```

하지만 비교 조건이 다르면 이러한 주장은 신뢰하기 어렵습니다.

예를 들어:

```text
Legacy
데이터 1,000건

vs

Refactor
데이터 100건
```

을 비교하고 Refactor가 빠르다고 말할 수는 없습니다.

또한 API 응답속도가 빨라졌더라도:

```text
Legacy 결과 = 100건
Refactor 결과 = 97건
```

이라면 성능 개선이 아니라 **기능 Regression 또는 데이터 손실**일 수 있습니다.

따라서 LegacyPOC DA에서는:

```text
동일 Contract
+
동일 Dataset
+
동일 Business Result
+
동일 측정 조건
```

을 먼저 고정한 뒤 성능을 비교합니다.

---

# 3. Core Analysis Questions

## BQ-01. Legacy와 Refactor의 기능 결과는 동일한가?

가장 먼저 확인하는 것은 속도가 아니라 **Parity**입니다.

검증 대상:

* Save 결과
* List 결과
* Search 결과
* HTTP Status
* Response Body
* Business Key
* Quantity
* Status
* Deleted Flag

기본 원칙:

```text
Same Input
    ↓
Legacy
Refactor
    ↓
Same Business Result?
```

다르면 성능 비교 전에 먼저 Regression 여부를 확인합니다.

---

## BQ-02. 동일 Dataset에서 조회 성능은 어떻게 달라지는가?

기능 결과가 동일하다는 조건을 만족한 뒤 성능을 비교합니다.

확인 지표:

```text
Query Latency
Query p50
Query p95

Rows Read

Buffer Hit
Buffer Read

Query Plan

Result Row Count
```

단순한 평균 응답시간 하나만으로 성능을 판단하지 않습니다.

---

## BQ-03. DB Index가 실제 Query Plan에 사용되는가?

Index를 생성했다고 해서 실제 Query가 항상 Index를 사용하는 것은 아닙니다.

따라서 PostgreSQL에서:

```sql
EXPLAIN (ANALYZE, BUFFERS)
```

를 이용해 실제 실행계획을 검증합니다.

확인 대상:

```text
Seq Scan
Index Scan
Bitmap Scan

Rows
Execution Time

Shared Hit Blocks
Shared Read Blocks
```

---

## BQ-04. Migration 이후 데이터가 Legacy와 동일한가?

시스템 전환에서 가장 중요한 문제 중 하나는 **데이터 보존**입니다.

분석 흐름:

```text
Legacy Snapshot
      ↓
Schema Normalize
      ↓
Refactor Load
      ↓
Reconciliation
      ↓
Mismatch Report
```

비교 항목:

* Row Count
* Business Key
* NULL
* Type
* Quantity
* Status
* Deleted Flag
* Source Timestamp

---

## BQ-05. Full Load와 재실행 결과가 동일한가?

동일 Snapshot을 다시 처리했을 때 데이터가 중복되어서는 안 됩니다.

따라서 다음을 확인합니다.

```text
Same Snapshot
    ↓
Run #1
    ↓
Run #2
    ↓
Duplicate?
Mismatch?
```

Business Key와 Upsert 정책을 기준으로 **Idempotency**를 검증합니다.

---

# 4. DA Analysis Framework

LegacyPOC의 분석 순서는 다음과 같습니다.

```text
Business Contract
       ↓
Dataset Freeze
       ↓
Legacy Execution
       ↓
Refactor Execution
       ↓
Contract Parity
       ↓
DB / API Measurement
       ↓
Snapshot Reconciliation
       ↓
Regression Detection
       ↓
Evidence Report
```

성능이 아무리 좋아도 Contract Parity가 깨지면 성공으로 평가하지 않습니다.

---

# 5. KPI Catalog

## 5.1 Parity Pass Rate

Legacy와 Refactor가 동일 입력에서 동일 결과를 반환한 비율입니다.

```text
Parity Pass Rate
=
Matching Cases
/
Total Comparison Cases
```

검증 대상:

```text
HTTP Status
Response Field
Business Value
Row Count
```

---

## 5.2 Migration Mismatch Count

Migration 이후 Legacy Snapshot과 Target 데이터가 일치하지 않는 건수입니다.

```text
Migration Mismatch Count
=
Count
+ Business Key
+ Field Value
+ Quantity
+ Status
Mismatch
```

목표는 단순히 전체 Row 수만 비교하는 것이 아니라 **업무적으로 중요한 값까지 비교하는 것**입니다.

---

## 5.3 Query p95

반복 실행한 Query 또는 API 요청 중 상위 지연구간을 확인합니다.

```text
Latency Measurements
      ↓
Distribution
      ↓
p50 / p95
```

한 번 빠르게 실행된 값보다 반복 측정 분포를 기준으로 판단합니다.

---

## 5.4 Rows Read

DB가 결과를 만들기 위해 얼마나 많은 Row를 읽었는지 확인합니다.

예를 들어:

```text
Result = 20 Rows
```

이더라도 DB 내부에서:

```text
100,000 Rows Scan
→ 20 Rows Return
```

했다면 개선 여지가 있습니다.

---

## 5.5 Buffer Hit / Read

PostgreSQL의 `EXPLAIN (ANALYZE, BUFFERS)` 결과를 이용해 확인합니다.

```text
Shared Hit
→ Buffer Cache에서 읽음

Shared Read
→ 실제 Storage Read
```

Latency와 함께 DB가 데이터를 어떤 방식으로 읽었는지 확인합니다.

---

## 5.6 Regression Count

Refactor 과정에서 발생한 기능 또는 성능 회귀 건수입니다.

```text
Regression
├── Contract
├── Business Result
├── Migration
├── Query
└── Performance
```

---

## 5.7 Smoke Pass Rate

Module / Profile별 기본 실행 가능 여부를 측정합니다.

분석 대상 예:

```text
Legacy Servlet

Spring Boot
├── Memory Profile
└── JPA Profile

FastAPI
```

---

# 6. Measurement Metadata

성능 숫자만 저장하면 동일한 결과를 재현하기 어렵습니다.

따라서 Benchmark에는 실행 환경 Metadata를 함께 기록하도록 설계했습니다.

| Category     | Metadata                       |
| ------------ | ------------------------------ |
| Code         | Commit SHA                     |
| Module       | Legacy / Spring Boot / FastAPI |
| Profile      | Memory / JPA 등                 |
| Runtime      | JVM / Python / DB Version      |
| Environment  | Local / Container              |
| Dataset      | Snapshot ID                    |
| Dataset Size | Row Count                      |
| Data Grain   | 비교 단위                          |
| Execution    | Warm-up                        |
| Execution    | Iteration                      |
| Execution    | Concurrency                    |
| Query        | Parameter                      |
| Evidence     | Log / CSV / Plan               |

분석 결과는:

```text
metric
+
environment
+
dataset
+
commit
```

을 하나의 Evidence로 봅니다.

---

# 7. Paired Comparison

Legacy와 Refactor를 서로 다른 조건에서 비교하지 않습니다.

```text
Dataset A
Contract V1
Environment X

        ↓

┌─────────────┬─────────────┐
│   Legacy    │  Refactor   │
├─────────────┼─────────────┤
│ Dataset A   │ Dataset A   │
│ Contract V1 │ Contract V1 │
│ Env X       │ Env X       │
└─────────────┴─────────────┘
        ↓
      Compare
```

이를 **Paired Comparison** 기준으로 사용합니다.

---

# 8. DB Query Analysis

Repository에는 PostgreSQL Query Plan을 검증하기 위한 SQL을 별도로 구성했습니다.

## Q1. Incremental Query

```sql
SELECT
    warehouse_io_id,
    warehouse_name,
    product_name,
    current_stock
FROM tb_warehouse_io
WHERE updated_at > :watermark
ORDER BY updated_at, warehouse_io_id;
```

분석 대상:

```text
updated_at
↓
Incremental Extraction
↓
Index Usage
```

---

## Q2. Active Warehouse List

```sql
SELECT
    warehouse_io_id,
    warehouse_name,
    product_name,
    current_stock
FROM tb_warehouse_io
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20 OFFSET 0;
```

확인 항목:

* Filtering
* Sorting
* Pagination
* Index 사용 여부

---

## Q3. Investor Search

```sql
SELECT
    investor_id,
    investor_name,
    investor_grade,
    total_amount
FROM tb_investor
WHERE investor_name LIKE :keyword
ORDER BY investor_id
LIMIT 20;
```

분석 목적:

```text
Investor Search
      ↓
Search Predicate
      ↓
Paging
      ↓
Execution Plan
```

---

## Q4. Primary Key Lookup

```sql
SELECT
    warehouse_io_id,
    warehouse_name,
    product_name,
    current_stock,
    status
FROM tb_warehouse_io
WHERE warehouse_io_id = :id
  AND deleted_yn = 'N';
```

PK 기반 단건 조회의 실행계획을 확인합니다.

---

# 9. Execution Plan Evidence

각 주요 Query는 다음 방식으로 확인합니다.

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT ...;
```

분석 순서:

```text
Query
 ↓
Execution Plan
 ↓
Scan Type
 ↓
Rows
 ↓
Buffers
 ↓
Execution Time
 ↓
Before / After Compare
```

Index 존재 여부가 아니라 **실제 Planner가 어떤 Plan을 선택했는지**를 Evidence로 남깁니다.

---

# 10. Snapshot Reconciliation

Migration 분석의 Source of Truth는 Snapshot입니다.

```text
Legacy DB
   ↓
Raw Snapshot
   ↓
Normalize
   ↓
Refactor DB
   ↓
Reconciliation
```

Raw Snapshot은 비교 과정에서 임의로 수정하지 않습니다.

기록 대상:

```text
snapshot id
source version
row count
hash
created/fetched time
```

---

# 11. Data Contract

Snapshot 비교 전에 Dataset의 Contract를 고정합니다.

| Field            | 검증 내용                |
| ---------------- | -------------------- |
| Grain            | 데이터 최소 업무 단위         |
| Business Key     | Legacy/Target 연결 Key |
| Nullable         | NULL 허용 여부           |
| Type             | 데이터 타입               |
| Quantity         | 수량 범위                |
| Status           | 허용 상태값               |
| Deleted Flag     | 삭제 정책                |
| Source Timestamp | 원천 기준시각              |

예를 들어 Warehouse I/O라면:

```text
Warehouse I/O Record 1건
```

처럼 비교 Grain을 먼저 고정합니다.

---

# 12. Reconciliation Checks

Migration 검증은 단순 Count 비교로 끝내지 않습니다.

```text
01. Source Row Count
02. Target Row Count
03. Business Key Match
04. Key NULL
05. Key Duplicate
06. Quantity Match
07. Status Match
08. Deleted Flag Match
09. Reject Count
10. Source / Target Timestamp
```

Mismatch가 존재할 경우:

```text
Migration Successful
```

로 처리하지 않고 별도 Evidence로 남깁니다.

---

# 13. Data Quality Gate

Migration 데이터에는 다음 DQ Rule을 적용하도록 설계했습니다.

```text
Business Key NOT NULL
Business Key UNIQUE

Quantity Valid Range

Status Accepted Values

Foreign Key Integrity

Source / Target Row Count

Reject Count
```

흐름:

```text
Snapshot
   ↓
Load
   ↓
DQ
   ↓
Reconciliation
   ↓
PASS?
 ┌───────┐
NO      YES
↓        ↓
Fix    Benchmark
```

데이터 정합성이 확보되지 않은 상태에서 성능 Benchmark 결과를 최종 비교 결과로 사용하지 않습니다.

---

# 14. Idempotency

동일 Snapshot을 다시 적재해도 Duplicate가 발생하지 않도록:

```text
Business Key
+
Upsert Policy
```

를 기준으로 사용합니다.

```text
Snapshot A
   ↓
Run 1
   ↓
Target

Snapshot A
   ↓
Run 2
   ↓
Same Target State
```

을 목표로 합니다.

---

# 15. Batch Metadata

Migration / Reconciliation 실행 단위별로 다음 정보를 관리하도록 정의했습니다.

```text
run_id
source_version

row_count
reject_count

start_time
end_time
```

이를 통해 특정 Reconciliation 결과가 어느 Batch에서 생성되었는지 추적할 수 있습니다.

---

# 16. Data Lineage

분석 결과는 다음 흐름으로 추적할 수 있어야 합니다.

```text
Source Snapshot
      ↓
Mapping Version
      ↓
Target Table
      ↓
Comparison Query
      ↓
QA Result
```

즉:

> “Mismatch가 3건이다”

라는 숫자만 남기는 것이 아니라,

```text
어떤 Snapshot인가?
어떤 Mapping을 사용했는가?
어느 Target인가?
어떤 Query로 비교했는가?
어떤 QA 결과인가?
```

까지 연결하는 것을 원칙으로 합니다.

---

# 17. Public Data Extension

Benchmark Fixture는 실제 공개데이터를 이용하는 방향으로 확장할 수 있도록 설계했습니다.

## Candidate Sources

| Source                | Normalized Target                 | Analysis Purpose          |
| --------------------- | --------------------------------- | ------------------------- |
| 한국벤처투자 모태펀드 자조합 운용사정보 | `investment_company` / `investor` | Investor 검색               |
| 금융위원회 KRX 상장종목정보      | `company`                         | Company 목록 / 검색           |
| 금융위원회 공시정보            | `disclosure`                      | Company ↔ Disclosure JOIN |

전체 흐름:

```text
Public API / CSV
       ↓
Raw Snapshot
       ↓
Validation
       ↓
Normalize
       ↓
Upsert
       ↓
Reconciliation
       ↓
Benchmark Fixture
```

이 영역은 **Benchmark Dataset 확장 설계**이며, 실제 적재 및 측정 Evidence가 확인되기 전에는 구현 성과로 표현하지 않습니다.

---

# 18. Public Data Quality

외부 데이터 사용 시 Source별 DQ를 별도로 정의합니다.

## KRX

```text
ISIN
Short Code
Security Name
Duplicate
```

검증

## Fund Manager

```text
Manager Name NULL
Duplicate Policy
Fund Size Numeric Validation
```

검증

## Disclosure

```text
Company Identifier
Disclosure Type
Reference Date
Orphan Disclosure
```

검증

---

# 19. Analysis Interpretation Rules

## Rule 1. Correctness First

```text
Fast
+
Wrong Result
=
Failure
```

Refactor의 응답시간이 줄어도 Legacy와 결과가 다르면 개선으로 평가하지 않습니다.

---

## Rule 2. One Measurement ≠ Performance

Query 한 번의 실행시간만 가지고 성능 개선을 단정하지 않습니다.

```text
Latency
+
Rows
+
Buffers
+
Plan
+
Parity
```

를 함께 확인합니다.

---

## Rule 3. Different Dataset ≠ Comparison

Row Count나 Snapshot이 다르면 결과를 직접 비교하지 않습니다.

---

## Rule 4. Index Created ≠ Index Used

Index 생성 여부보다 실제 `EXPLAIN` Plan을 확인합니다.

---

## Rule 5. Unmeasured Improvement ≠ Result

실제로 측정되지 않은 개선율은:

```text
TBD
NOT MEASURED
```

로 남깁니다.

추정값을 실제 성과처럼 기록하지 않습니다.

---

# 20. Evidence Levels

LegacyPOC DA에서는 문서 설계와 실제 Runtime Evidence를 구분합니다.

| Evidence    | 의미                           |
| ----------- | ---------------------------- |
| DESIGNED    | 분석 기준 / Metric / Protocol 정의 |
| IMPLEMENTED | SQL / Test / 비교 구조 구현        |
| MEASURED    | 동일 조건으로 Runtime 측정 완료        |
| VERIFIED    | 결과와 Evidence 검토 완료           |
| TBD         | 아직 측정하지 않음                   |

예를 들어:

```text
EXPLAIN 검증 SQL
→ IMPLEMENTED

Query p95 결과
→ 실제 Benchmark 수행 전까지 TBD
```

처럼 구분합니다.

---

# 21. Current Evidence

| Area                                 | Status                           |
| ------------------------------------ | -------------------------------- |
| Legacy / Refactor 비교 기준              | Defined                          |
| API / Business Parity 원칙             | Defined                          |
| DA KPI Catalog                       | Defined                          |
| Snapshot Reconciliation Contract     | Defined                          |
| Data Quality Rules                   | Defined                          |
| PostgreSQL EXPLAIN SQL               | Implemented                      |
| Incremental Query Plan Check         | Implemented                      |
| Active List Plan Check               | Implemented                      |
| Investor Search Plan Check           | Implemented                      |
| PK Lookup Plan Check                 | Implemented                      |
| Lineage / Batch Metadata             | Designed                         |
| Public Data Benchmark Fixture        | Extension Design                 |
| Runtime p50 / p95 Benchmark Result   | Not verified in `da/`            |
| Production Performance Improvement % | Not claimed                      |
| BI Dashboard                         | Not applicable / Not implemented |

---

# 22. Tech Stack

| Category            | Stack                              |
| ------------------- | ---------------------------------- |
| Legacy Comparison   | Servlet / JDBC                     |
| Refactor Comparison | Spring Boot / FastAPI              |
| Database Analysis   | PostgreSQL                         |
| Query Analysis      | SQL                                |
| DB Evidence         | `EXPLAIN (ANALYZE, BUFFERS)`       |
| API Evidence        | Contract / Smoke / Regression Test |
| Migration Analysis  | Snapshot Reconciliation            |
| Data Quality        | SQL-based DQ                       |
| Documentation       | Markdown                           |
| Version Control     | Git / GitHub                       |

---

# 23. Repository Structure

```text
da/
├── README.md
├── da-measurement-benchmark.md
└── de-snapshot-reconciliation.md
```

관련 Query Plan Evidence:

```text
refactor-db-schema/
└── postgresql/
    └── explain-verify.sql
```

전체 프로젝트에서는:

```text
Legacy Servlet/JDBC
        ↓
Spring Boot Refactor
        ↓
FastAPI Refactor

        +

DB Schema / Migration
        ↓
Contract Tests
        ↓
DA Benchmark
        ↓
Snapshot Reconciliation
```

구조로 연결됩니다.

---

# 24. Analysis Workflow

```text
1. Contract 고정
        ↓
2. Snapshot 고정
        ↓
3. Legacy 실행
        ↓
4. Refactor 실행
        ↓
5. Parity 검증
        ↓
6. Query / API 측정
        ↓
7. EXPLAIN 분석
        ↓
8. Migration Reconciliation
        ↓
9. Regression 확인
        ↓
10. Evidence 저장
```

---

# 25. What I Focused On

이 프로젝트의 DA 영역에서는 일반적인 Dashboard 제작보다 **리팩터링 결과를 숫자로 검증할 수 있는 측정 체계**를 만드는 데 집중했습니다.

```text
Refactoring Claim
       ↓
Measurement Question
       ↓
Fixed Dataset
       ↓
Metric
       ↓
Paired Comparison
       ↓
Reconciliation
       ↓
Evidence
       ↓
Decision
```

특히:

* 동일 Dataset
* 동일 API Contract
* 동일 Business Behavior
* 동일 실행 환경

을 고정하지 않은 비교는 성능 Evidence로 사용하지 않는 것을 원칙으로 했습니다.

또한 API Latency만 보는 것이 아니라:

```text
Parity
Latency
Rows
Buffers
Query Plan
Migration Mismatch
Regression
```

을 함께 확인하도록 분석 범위를 설계했습니다.

---

# 26. Next Steps

현재 DA 구조에서 다음 단계로 확장할 수 있습니다.

```text
Current Measurement Contract
        ↓
Automated Benchmark Runner
        ↓
CSV / JSON Result
        ↓
Historical Snapshot
        ↓
Before / After Visualization
        ↓
Regression Alert
```

확장 후보:

* Legacy / Spring Boot / FastAPI 자동 Benchmark Runner
* p50 / p95 / p99 결과 수집
* Throughput / Error Rate 추가
* Execution Plan JSON 저장
* Commit별 Benchmark History
* Migration Mismatch 자동 Report
* Full vs Incremental Reconciliation 자동화
* Public Dataset 기반 대규모 Fixture
* Performance Regression Threshold
* CI Benchmark Gate
* Before / After Dashboard

---

# Summary

**LegacyPOC DA는 Legacy Servlet/JDBC와 Spring Boot·FastAPI 리팩터 구현을 단순 코드 구조가 아니라 데이터로 비교하기 위한 Migration Analytics 프로젝트입니다.**

동일 Contract와 동일 Snapshot을 기준으로 기능 Parity를 먼저 확인하고, PostgreSQL `EXPLAIN (ANALYZE, BUFFERS)`를 이용해 Query Plan·Rows·Buffer를 분석하며, Migration 이후에는 Row Count·Business Key·Quantity·Status를 Reconciliation합니다.

실제 Runtime Measurement가 없는 항목은 성능 개선율을 임의로 만들지 않고 `TBD / NOT MEASURED`로 구분하여, **설계된 개선과 실제 검증된 Evidence를 분리하는 것**을 핵심 원칙으로 합니다.

```
