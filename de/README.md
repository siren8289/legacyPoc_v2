# LegacyPOC — Legacy/Refactor 데이터 마이그레이션 정합성 및 Reconciliation

> **Legacy DB와 Refactor DB를 동일한 Grain·Schema·Lineage 기준으로 Snapshot하고, Normalize → Load → Reconciliation 과정을 통해 마이그레이션 후 데이터 정합성을 검증하는 DE/DBA 프로젝트**

* **프로젝트 구분:** 개인 프로젝트
* **핵심 역할:** Data Contract 정의, Snapshot 설계, Schema Normalize, Migration Reconciliation, Idempotent Load, Data Quality, Lineage 관리
* **기술 스택:** SQL, PostgreSQL, Oracle/Legacy DB 구조, dbt/Airflow 확장 설계
* **외부 데이터:** KVIC 모태펀드 운용사, KRX 상장종목정보, 금융위원회 공시정보
* **주요 영역:** Data Engineering / DBA / Data Migration / Data Quality

---

# 1. 문제 상황 및 요구사항

## 1-1. 프로젝트 배경

LegacyPOC는 기존 Legacy 구조와 Refactor 구조를 비교하는 프로젝트입니다.

DE 영역의 목적은 대규모 실시간 데이터 파이프라인을 만드는 것이 아니라, **Legacy DB와 Refactor DB가 같은 데이터를 같은 의미로 유지하고 있는지를 증명하는 것**입니다.

핵심 흐름은 다음과 같습니다.

```text
Legacy Snapshot
→ Schema Normalize
→ Refactor Load
→ Reconciliation
→ QA Evidence
```

---

## 1-2. 발생 가능한 문제

Legacy 시스템을 Refactor할 때 API 코드만 정상적으로 동작한다고 해서 데이터 마이그레이션까지 안전하다고 볼 수 없습니다.

예를 들어 다음 문제가 발생할 수 있습니다.

```text
Legacy DB
quantity = 10
status = ACTIVE

        ↓ Migration

Refactor DB
quantity = 9
status = NULL
```

또는:

```text
Legacy row count = 100,000
Refactor row count = 99,950
```

처럼 일부 Row가 유실될 수 있습니다.

반대로 동일 Snapshot을 다시 적재했을 때:

```text
1차 적재: 100,000 rows
2차 적재: 200,000 rows
```

처럼 중복이 발생할 수도 있습니다.

따라서 LegacyPOC의 DE 문제는 단순 ETL이 아니라 **Migration 전후 데이터가 동일하다는 것을 어떻게 증명할 것인가**였습니다.

---

# 2. 해결 요구사항

DE/DBA 관점에서 다음 요구사항을 설정했습니다.

* Legacy와 Refactor 데이터의 Grain을 동일하게 정의한다.
* Business Key를 명확하게 정의한다.
* Nullable, Type, Status, Quantity 등의 Data Contract를 고정한다.
* Raw Snapshot은 수정하지 않고 보존한다.
* Snapshot마다 Hash와 Row Count를 남긴다.
* 동일 Snapshot 재실행 시 Duplicate가 발생하지 않아야 한다.
* Batch 단위 실행 Metadata를 기록한다.
* Source와 Target 간 Row Count와 Business Value를 비교한다.
* DQ 실패 데이터를 Evidence로 남긴다.
* Source → Mapping → Target → Reconciliation → QA 결과까지 Lineage를 추적한다.
* 성능 개선은 실제 `EXPLAIN ANALYZE` Evidence가 있을 때만 주장한다.

---

# 3. 원인 분석

## 3-1. Legacy와 Refactor의 Schema가 같다고 가정할 수 없음

Legacy와 Refactor DB는 같은 업무 데이터를 다루더라도 Schema 구조가 달라질 수 있습니다.

예를 들어:

```text
Legacy
qty
stat
del_yn

Refactor
quantity
status
deleted
```

처럼 컬럼 이름, Type, NULL 정책, Default가 달라질 수 있습니다.

따라서 단순한:

```text
SELECT *
FROM legacy

→ INSERT INTO refactor
```

방식으로는 안전한 Migration을 보장하기 어렵습니다.

그래서 먼저 다음 Data Contract를 정의했습니다.

| 항목                | 검증 기준           |
| ----------------- | --------------- |
| Grain             | 비교할 최소 업무 단위    |
| Business Key      | 동일 Record 식별 기준 |
| Nullable          | NULL 허용 여부      |
| Type              | 데이터 타입          |
| Quantity / Status | 업무 값            |
| Deleted Flag      | 삭제 처리 기준        |
| Source Timestamp  | 원천 시간 정보        |

---

## 3-2. Row Count만 같다고 정합성이 보장되지는 않음

예를 들어:

```text
Source = 100 rows
Target = 100 rows
```

여도 실제 값이 다를 수 있습니다.

```text
Source quantity SUM = 10,000
Target quantity SUM = 9,950
```

또는 Key가 바뀌었을 수도 있습니다.

따라서 단순 Count뿐 아니라:

```text
Business Key
Quantity
Status
Deleted Flag
Hash
```

등을 함께 비교해야 한다고 판단했습니다.

---

## 3-3. 재실행에 안전하지 않은 Migration

ETL/마이그레이션은 실패 후 다시 실행될 수 있습니다.

만약 단순 INSERT만 사용하면:

```text
Run 1
→ 100,000 rows

Run 2
→ 동일 데이터 INSERT
→ 200,000 rows
```

가 될 수 있습니다.

따라서 동일 Snapshot을 여러 번 실행해도 결과가 변하지 않는 **Idempotency**가 필요했습니다.

---

# 4. 문제 해결 및 적용 과정

## Step 1. Data Contract 고정

Legacy와 Refactor를 비교하기 전에 비교 기준을 먼저 고정했습니다.

```text
Record Grain
Business Key
Nullable
Type
Quantity
Status
Deleted Flag
Source Timestamp
```

이를 통해:

```text
무엇이 같은 데이터인가?
어떤 값이 달라지면 오류인가?
```

를 SQL 실행 전에 정의했습니다.

---

## Step 2. Raw Snapshot Immutable 보존

Legacy DB에서 추출한 Raw Snapshot은 변환하지 않고 보존합니다.

각 Snapshot에는:

```text
row count
hash
source version
```

을 기록합니다.

구조는:

```text
Legacy DB
   ↓
Raw Snapshot
   ↓
Immutable
```

입니다.

이렇게 하면 Migration 결과에 문제가 생겼을 때 원본 데이터를 다시 확인할 수 있습니다.

---

## Step 3. Schema Normalize

Legacy Snapshot을 Refactor Schema에 맞게 바로 INSERT하지 않고 중간 Normalize 단계를 둡니다.

```text
Legacy Snapshot
       ↓
Column Mapping
       ↓
Type Conversion
       ↓
Default / NULL Handling
       ↓
Deleted Record Policy
       ↓
Normalized Dataset
```

변환 규칙은 문서로 고정합니다.

주요 Mapping 대상은 다음과 같습니다.

```text
Column Name
Data Type
Default
NULL Policy
Deleted Flag
Business Key
```

---

## Step 4. Business Key + UPSERT 기반 Idempotent Load

동일 Snapshot을 다시 실행해도 Duplicate가 생기지 않도록 Business Key와 UPSERT 정책을 사용합니다.

### 위험한 구조

```text
Snapshot
→ INSERT
→ INSERT
→ Duplicate
```

### 개선 구조

```text
Snapshot
→ Business Key 확인
→ UPSERT

있음 → UPDATE
없음 → INSERT
```

따라서:

```text
Run 1
→ 100,000 rows

Run 2
→ 동일 Snapshot
→ 100,000 rows 유지
```

를 목표로 합니다.

---

# 5. Reconciliation 설계

## Step 5. Source / Target Row Count 비교

Migration 후 가장 기본적인 검증은 Source와 Target의 Row Count입니다.

```text
Legacy Count
↕
Refactor Count
```

차이가 발생하면 Migration 누락 또는 Filter Rule을 확인합니다.

하지만 Count만으로 끝내지 않습니다.

---

## Step 6. Business Key 비교

다음 단계로 Source와 Target의 Key를 비교합니다.

```text
Legacy Business Key
↕
Refactor Business Key
```

검증 목적은:

```text
Missing Row
Extra Row
Duplicate Row
```

탐지입니다.

---

## Step 7. Quantity / Status / Deleted Flag 비교

Record가 존재하더라도 값이 달라질 수 있으므로 업무 값도 비교합니다.

```text
Legacy.quantity
↔ Refactor.quantity

Legacy.status
↔ Refactor.status

Legacy.deleted_flag
↔ Refactor.deleted
```

즉 Reconciliation은:

```text
Count
+
Key
+
Business Value
```

세 축으로 수행합니다.

---

# 6. Data Quality

LegacyPOC에서 설정한 주요 DQ 규칙은 다음과 같습니다.

| 검증 항목               | 기준               |
| ------------------- | ---------------- |
| Key Not Null        | NULL Key 없음      |
| Key Unique          | Duplicate Key 없음 |
| Quantity            | 허용 범위 내          |
| Status              | Accepted Value   |
| FK                  | Orphan 없음        |
| Source/Target Count | 차이 검증            |

---

## Step 8. DQ와 Reconciliation 분리

DQ와 Reconciliation은 비슷하지만 역할을 다르게 봅니다.

### Data Quality

```text
Target 데이터 자체가 올바른가?
```

예:

```text
NULL?
Duplicate?
Invalid Status?
Broken FK?
```

### Reconciliation

```text
Legacy와 Refactor 결과가 같은가?
```

예:

```text
Source Count = Target Count?
Source Key = Target Key?
Source Quantity = Target Quantity?
```

이 둘을 분리해 Migration 문제의 원인을 찾기 쉽게 구성했습니다.

---

# 7. Batch Metadata 관리

Migration Batch마다 다음 Metadata를 기록하도록 설계했습니다.

```text
run_id
source_version
row_count
reject_count
start_time
end_time
```

즉 단순히:

```text
Migration 성공
```

만 남기는 것이 아니라:

```text
어떤 Source Version을
언제
몇 건 처리했고
몇 건 Reject됐는가
```

를 확인할 수 있도록 했습니다.

---

# 8. Lineage 설계

LegacyPOC DE의 중요한 요소 중 하나는 Lineage입니다.

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

이를 통해 QA에서 Migration Error가 발견되면:

```text
QA Failure
→ Comparison Query
→ Target Table
→ Mapping Version
→ Source Snapshot
```

순서로 역추적할 수 있도록 했습니다.

---

# 9. 대량 데이터 비교 전략

대량 Dataset에서 모든 Row를 Application으로 읽어 Python/Java에서 비교하면 불필요한 Network와 Memory 비용이 발생할 수 있습니다.

LegacyPOC 설계에서는 대량 비교를 DB 내부의:

```text
COUNT
Aggregate
Hash
```

중심으로 처리하도록 했습니다.

즉:

```text
DB → 전체 Row Application 전송 → 비교
```

보다:

```text
DB 내부 Aggregate / Hash
→ 비교 결과만 반환
```

하는 방식입니다.

---

# 10. 성능 검증 기준

DB Index 또는 Query를 변경할 경우 단순 체감 성능으로 판단하지 않습니다.

Repository 문서에서는:

```text
EXPLAIN ANALYZE
```

를 성능 Evidence 기준으로 명시하고 있습니다.

확인 대상은 다음과 같습니다.

```text
Execution Plan
Scan Range
Index Usage
Query Count
Paging 여부
```

다만 현재 DE 문서에는 실제 전후 측정 수치가 기록되어 있지 않으므로:

```text
XX% 성능 향상
```

같은 수치는 주장하지 않습니다.

---

# 11. Public Data Ingestion 확장

Legacy/Refactor 비교 Fixture를 실제 공개 데이터로 확장하도록 설계했습니다.

사용 Source는 다음 세 가지입니다.

| Source            | Raw                | Target                            | 목적               |
| ----------------- | ------------------ | --------------------------------- | ---------------- |
| KVIC 모태펀드 자조합 운용사 | `raw_fund_manager` | `investment_company` / `investor` | 검색·Index 비교      |
| KRX 상장종목정보        | `raw_krx_company`  | `company`                         | 기업 Master·Paging |
| 금융위원회 공시정보        | `raw_disclosure`   | `disclosure`                      | 1:N JOIN·필터      |

---

# 12. Public Data Pipeline

외부 공개데이터는 다음 흐름으로 처리합니다.

```text
Extract
   ↓
Raw Snapshot
   ↓
Validate
   ↓
Normalize
   ↓
Upsert
   ↓
Reconciliation
   ↓
Benchmark Fixture
```

각 Raw Record에는 다음 Metadata를 보존합니다.

```text
source_system
source_updated_at
fetched_at
etl_batch_id
```

KRX와 공시 데이터는 Benchmark 실행 전에 Snapshot으로 고정해 Legacy와 Refactor가 서로 다른 시점 데이터를 비교하지 않도록 설계했습니다.

---

# 13. Public Data DQ

외부 데이터별 검증 기준도 별도로 정의했습니다.

| Source | 검사                                             |
| ------ | ---------------------------------------------- |
| KRX    | ISIN, 단축코드, 종목명 필수/중복                          |
| 운용사    | 대표운용사명 NULL/중복 정책, 규모 Numeric 검증               |
| 공시     | 기업 ID, 공시유형, 기준일자, Orphan Disclosure           |
| 공통     | Source/Target Count, Reject Count, Idempotency |

---

# 14. Benchmark Dataset으로 활용

공개 데이터를 단순 적재용으로 끝내지 않고 Benchmark Fixture로 활용합니다.

대표 Query는 다음과 같습니다.

```text
Investor 검색
Company 목록
Company ↔ Disclosure JOIN
```

이를 동일 Snapshot에서 Legacy / Refactor 양쪽에 적용해 비교합니다.

```text
Same Snapshot
     ↓
Legacy Query
     ↘
       Compare
     ↗
Refactor Query
```

비교 항목은:

```text
Execution Plan
Scan Range
Query Count
Paging
Result Parity
```

입니다.

---

# 15. DBA 관점 핵심 성과

## ① Migration Reconciliation

```text
Legacy DB
→ Snapshot
→ Normalize
→ Refactor DB
→ Reconciliation
```

DBA 포트폴리오에서는 **DB Migration 후 정합성을 검증하는 프로젝트**로 가져가는 것이 좋습니다.

---

## ② Data Integrity

```text
Key Not Null
Key Unique
FK
Status Accepted Value
Quantity Range
Source/Target Reconciliation
```

---

## ③ Idempotent Migration

```text
Business Key
+
UPSERT
→ 동일 Snapshot 재실행 안전
```

---

## ④ Migration Auditability

```text
run_id
source version
row count
reject count
start / end
```

---

## ⑤ Query Plan Evidence 원칙

```text
Index / Query 변경
→ EXPLAIN ANALYZE
→ Evidence
```

실행 Evidence가 없으면 성능 개선율을 주장하지 않는 기준을 적용했습니다.

---

# 16. DE 관점 핵심 성과

DE에서는 다음 흐름이 핵심입니다.

```text
Legacy Snapshot
      ↓
Immutable Raw
      ↓
Schema Normalize
      ↓
Idempotent Load
      ↓
DQ
      ↓
Reconciliation
      ↓
QA Evidence
```

그리고 운영 Metadata는:

```text
run_id
source_version
row_count
reject_count
mapping_version
```

로 추적합니다.

즉 **LegacyPOC의 DE 핵심은 파이프라인 규모가 아니라 Migration의 재현성·정합성·Lineage**입니다.

---

# 17. 문제 해결 흐름 요약

```text
[Problem]

Legacy → Refactor Migration 시
데이터 누락 / 중복 / 값 변경 위험

        ↓

[Root Cause]

Schema 차이
+
Business Key 불명확
+
재실행 정책 부재
+
Source/Target 비교 기준 부족

        ↓

[Action]

Data Contract 정의
→ Immutable Snapshot
→ Schema Normalize
→ Business Key + UPSERT
→ DQ
→ Reconciliation
→ Batch Metadata
→ Lineage

        ↓

[Result]

동일 Snapshot 기준으로
Legacy / Refactor 결과를
재현·검증할 수 있는 구조 확보
```

---

# 18. 해결 결과 및 성과

LegacyPOC DE의 성과는 정량 latency 개선보다 **Migration Validation 구조를 만든 것**에 있습니다.

| 영역        | 기존 위험              | 개선 후                      |
| --------- | ------------------ | ------------------------- |
| 원본 데이터    | 변경 가능              | Immutable Snapshot        |
| Schema    | Legacy/Refactor 차이 | Normalize Rule 명시         |
| 재실행       | Duplicate 가능       | Business Key + UPSERT     |
| 정합성       | 수동 확인              | Reconciliation Query      |
| DQ        | 단순 성공 여부           | Key/FK/Status/Range 검사    |
| 실행 이력     | 추적 어려움             | `run_id` + Batch Metadata |
| 원인 추적     | 결과만 확인             | Source → QA Lineage       |
| Benchmark | 서로 다른 데이터 가능       | 동일 Snapshot 고정            |

---

# 19. 회고 및 배운 점

첫째, DB Migration에서 가장 중요한 것은 데이터를 옮기는 것보다 **옮긴 데이터가 기존과 동일하다는 것을 증명하는 것**이라는 점을 배웠습니다.

둘째, Row Count가 같다고 데이터가 같은 것은 아니기 때문에 Business Key, Quantity, Status 등 업무 값까지 Reconciliation해야 한다는 점을 확인했습니다.

셋째, ETL은 한 번 성공하는 것보다 재실행 가능성이 중요했습니다. Business Key와 UPSERT 정책을 미리 정의하지 않으면 장애 복구 과정에서 데이터 중복이 발생할 수 있습니다.

넷째, Raw Snapshot과 Mapping Version을 보존하면 QA 오류가 발생했을 때 어느 변환 단계에서 문제가 생겼는지 역추적할 수 있다는 점에서 Lineage가 중요했습니다.

다섯째, 성능 개선은 실제 실행계획 Evidence가 없는 상태에서 숫자로 주장하지 않고, `EXPLAIN ANALYZE`로 검증된 결과만 사용하는 것이 중요하다는 기준을 세웠습니다.

---

# 20. 현재 프로젝트에서 주장하지 않는 것

현재 `de/` 문서가 지원하지 않는 다음 내용은 포트폴리오 성과로 적지 않습니다.

* 실제 수백만/수천만 건 Migration 완료
* Migration 시간 XX% 단축
* Query Latency XX% 개선
* PostgreSQL CPU 사용률 개선
* AWS DMS 실제 사용
* Oracle → PostgreSQL 운영 Migration 완료
* Zero-downtime Migration
* CDC 기반 실시간 Migration
* Airflow 운영 배포
* dbt 실제 운영 적용
* HA / Failover
* RTO / RPO
* Backup / Restore 자동화

현재 Repository가 명확하게 지원하는 핵심은 **Snapshot, Normalize, Idempotency, Reconciliation, DQ, Batch Metadata, Lineage 설계**입니다.

---

# 21. 포트폴리오용 최종 설명

> **LegacyPOC에서 Legacy DB와 Refactor DB의 데이터 마이그레이션 정합성을 검증하기 위한 Data Engineering 구조를 설계했습니다. 동일한 업무 Grain과 Business Key를 기준으로 Legacy Snapshot을 Immutable Raw로 보존하고, Column Mapping·Type Conversion·NULL/Default·Deleted Record 정책을 거쳐 Schema를 Normalize한 뒤 Business Key와 UPSERT 기반으로 Refactor DB에 Idempotent Load하도록 구성했습니다. Migration 후에는 Source/Target Row Count뿐 아니라 Business Key, Quantity, Status, Deleted Flag까지 Reconciliation하고, Key NULL/Unique, FK, Accepted Status 등 Data Quality 검사를 분리했습니다. 또한 Batch별 `run_id`, source version, row count, reject count와 Source Snapshot → Mapping Version → Target → Comparison Query → QA Result Lineage를 기록해 Migration 결과를 재현하고 역추적할 수 있도록 설계했습니다.**

## 한 줄 성과

> **Legacy → Refactor 마이그레이션을 Immutable Snapshot·Schema Normalize·Idempotent UPSERT·Reconciliation·Lineage 구조로 표준화해 데이터 누락·중복·값 불일치를 검증할 수 있는 DE/DBA Migration 검증 체계를 설계했습니다.**
