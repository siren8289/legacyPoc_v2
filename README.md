# LegacyPOC 

## 1. 핵심 한 줄

> **LegacyPOC는 Servlet/JDBC 레거시 구조와 Spring Boot 리팩터 구조를 비교하면서, 라우팅·업데이트·검색·목록 조회·DB 인덱스·멀티 DB 스키마까지 효율화 포인트를 단계적으로 보여주는 교육용 PoC입니다.**

---

## 2. 프로젝트 성격

LegacyPOC는 일반적인 CRUD 프로젝트가 아니라, **레거시 구조와 리팩터 구조의 차이를 보여주기 위한 비교형 프로젝트**입니다.

```text
Legacy Servlet + jQuery + Oracle
→ 일부 안티패턴을 의도적으로 유지
→ 어디가 불편하고 느려지는지 보여줌

Spring Boot Refactor 3종
→ Controller → Service → Repository
→ DTO Projection + Pageable
→ memory / jpa 전환
→ 멀티 DB 스키마 분리
```

즉, 레거시 쪽은 무조건 다 고친 것이 아니라, 교육 목적상 God Servlet, `SELECT *`, 화면 전체 재조회 같은 패턴을 일부 남겨두고, 개선 가능한 부분은 FR-011/FR-012처럼 명확히 개선했습니다.

GitHub 공식문서에서도 README는 프로젝트가 왜 유용한지, 무엇을 할 수 있는지, 어떻게 사용하는지 설명하는 문서라고 안내합니다. 이 README는 해당 기준에 맞춰 프로젝트 목적, 구조, 효율화 포인트, 공식문서 근거를 함께 정리했습니다.

---

## 2-1. 리팩터링 검증 원칙

LegacyPOC 백엔드의 핵심은 **기존 동작을 깨지 않고 리팩터링 구조와 DB 구현을 교체한 뒤, PRD → SRS → SDD → Code → Test 추적성으로 parity를 증명하는 것**입니다. 새 구조가 더 깔끔하다는 이유만으로 API 동작이나 비즈니스 결과를 바꾸지 않습니다.

입출고 기능은 SRS 기준으로 `list`, `detail`, `create`, `update`, `delete`를 우선 검증 범위로 둡니다. legacy와 refactor가 같은 입력에 대해 같은 비즈니스 결과를 내는지 확인하고, 차이가 있다면 의도한 개선인지 regression인지 PRD/SRS/SDD/Test ID로 추적합니다.

| 검증 축 | 기준 |
| --- | --- |
| API Contract | OpenAPI 기준으로 endpoint별 path, method, request, response, error를 고정하고 legacy/refactor contract diff를 관리 |
| DTO 경계 | Entity를 직접 반환하지 않고 DTO로 응답하며 invalid id, column, input은 일관된 4xx 오류로 변환 |
| Repository Abstraction | MemoryRepository는 테스트·임시 실행용, JpaRepository는 영속 DB용으로 유지하고 Service는 port/interface만 의존 |
| Transaction | `create`, `update`, `delete`는 유스케이스 단위 `@Transactional`로 묶고 저장 실패 시 partial update가 남지 않도록 검증 |
| Concurrency | 재고·수량처럼 동시에 수정될 수 있는 값은 lost update 가능성을 동시성 테스트로 확인한 뒤 optimistic/pessimistic locking 적용 여부 결정 |
| Validation/Security | 입력 DTO와 id/path/query parameter에 서버 검증을 적용하고, Security 적용 시 BOLA, Broken Authentication, Broken Function Level Authorization을 회귀 대상으로 관리 |
| DB Performance | list/search는 필요한 column/projection과 pagination을 사용하고, WHERE/JOIN/ORDER BY 기준 인덱스를 `EXPLAIN (ANALYZE, BUFFERS)`로 검증 |
| Test Matrix | contract parity, CRUD happy/invalid/not found, Memory/JPA service behavior, rollback, pagination/sort, migration reconciliation, module smoke/integration test |

성능 비교는 commit, DB 종류, row count, warm-up, 반복 횟수를 고정한 상태에서 비교합니다. 이 프로젝트의 리팩터링 성공 기준은 “새 코드가 예쁘다”가 아니라, **동일 contract와 동일 business behavior를 유지하면서 구조·DB 선택지를 교체할 수 있음을 테스트로 보여주는 것**입니다.

공식문서 기준은 다음 항목을 중심으로 둡니다.

| 영역 | 공식문서 |
| --- | --- |
| API 명세 | OpenAPI Specification |
| 입력 검증 | Spring Validation, Bean Validation |
| 트랜잭션 | Spring Framework Transaction Management |
| 테스트 | Spring Boot Testing |
| 실행계획 | PostgreSQL `EXPLAIN` |
| API 보안 | OWASP API Security Top 10 |

---

## 2-2. DE Snapshot / Reconciliation 원칙

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

## 2-3. DA Measurement / Benchmark 원칙

LegacyPOC DA의 목적은 제품 성장 분석보다 **리팩터 전후 동작, DB 효율, 안정성 비교를 재현 가능한 측정으로 만드는 것**입니다. 기능 결과가 다르면 속도가 빨라도 성공으로 평가하지 않고, 동일 dataset과 동일 조건에서 legacy/refactor를 paired comparison으로 비교합니다.

핵심 분석 질문은 다음과 같습니다.

| 질문 | 확인 대상 |
| --- | --- |
| save/list/search 결과가 legacy와 refactor에서 동일한가? | API contract parity, response body, status code, business result |
| 동일 dataset에서 query plan, latency, read rows/buffers가 어떻게 달라지는가? | DB plan, p50/p95 latency, rows read, buffer hit/read |
| 어떤 module/profile에서 smoke failure가 발생하는가? | legacy servlet, Spring Boot vanilla/vue/api, memory/jpa profile |
| migration 후 count, business key, quantity/status 불일치는 없는가? | reconciliation SQL, mismatch report, QA evidence |

benchmark는 실행 조건을 함께 저장합니다.

| Metadata | 예시 |
| --- | --- |
| Code | commit SHA, module, profile |
| Runtime | DB version, JVM/Node version, local/container 여부 |
| Dataset | snapshot id, row count, grain, source version |
| Execution | warm-up, iteration, concurrency, query parameter |
| Evidence | result CSV/log, plan text 또는 JSON, smoke report 경로 |

p50/p95 latency와 DB plan은 분리해 기록합니다. 작은 표본의 한 번 측정값은 성능 결론으로 사용하지 않고, query plan과 rows/buffers evidence를 함께 봅니다.

기본 metric은 다음과 같습니다.

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

공식문서 기준은 다음 항목을 중심으로 둡니다.

| 영역 | 공식문서 |
| --- | --- |
| PostgreSQL 실행계획 | PostgreSQL `EXPLAIN` |
| BigQuery 성능 | BigQuery Performance Best Practices, BigQuery를 쓸 경우에만 적용 |
| 사용자 행동 계측 | GA4 Events, 사용자 행동 계측을 추가하는 경우에만 적용 |

---

## 2-4. AI / ML 적용 경계

현재 LegacyPOC의 핵심 런타임 기능은 AI/ML이 아닙니다. 이 프로젝트의 우선순위는 deterministic CRUD, repository/profile 교체, DB migration, parity 검증, 성능 측정입니다. AI가 CRUD 결과나 migration 결과를 자동 수정하거나 release를 승인하지 않습니다.

AI 문서는 향후 코드 현대화 분석 보조를 도입할 경우의 조건부 설계로만 관리합니다. 현재 구현된 기능처럼 서술하지 않고, 도입하더라도 **read-only reviewer assistance** 범위로 제한합니다.

| 구분 | 기준 |
| --- | --- |
| Current Boundary | CRUD 결과, migration 결과, release 승인은 deterministic test와 사람이 판단 |
| Optional Use Cases | code smell 설명, refactor 후보 분류, migration diff 요약, test failure triage |
| Write Boundary | 실제 코드 변경과 DB write는 사람이 검토한 뒤 별도 작업으로 실행 |
| Evaluation | 같은 commit/snapshot에서 rule-only 분석과 AI-assisted 분석을 paired comparison으로 비교 |
| Adoption Rule | AI가 기존 rule 결과를 누락시키거나 reviewer 시간을 늘리면 채택하지 않음 |
| Fixed Inputs | 평가 세트, prompt, model, version을 고정하고 결과를 비교 |

NIST AI RMF를 적용하는 경우 다음 기준으로 관리합니다.

| Function | LegacyPOC 적용 기준 |
| --- | --- |
| GOVERN | owner, 사용 제한, 변경 승인, write tool 비활성화 기본값 정의 |
| MAP | code/data sensitivity, intended use, 외부 provider 전송 여부 식별 |
| MEASURE | hallucination, unsupported suggestion, precision, rule-result 누락 여부 측정 |
| MANAGE | disable, fallback, human review, prompt/model 변경 승인 절차 정의 |

보안 기준은 소스코드와 DB schema가 외부 provider로 전송되는 경우에 특히 강화합니다. 데이터 분류, secret 제거, retention 정책을 먼저 확인하고, tool 권한은 least privilege를 적용합니다.

공식문서 기준은 다음 항목을 중심으로 둡니다.

| 영역 | 공식문서 |
| --- | --- |
| AI Risk Management Framework | [NIST AI RMF](https://www.nist.gov/itl/ai-risk-management-framework) |
| AI RMF 실행 가이드 | [NIST AI RMF Playbook](https://www.nist.gov/itl/ai-risk-management-framework/nist-ai-rmf-playbook) |
| 생성형 AI 프로파일 | [NIST GenAI Profile](https://www.nist.gov/itl/ai-risk-management-framework/ai-risk-management-framework-resources) |

---

## 3. Legacy 코드 효율화

### 3-1. Servlet action O(1) 라우팅

기존 if-else 기반 action 분기는 action이 늘어날수록 Servlet 내부가 길어지고, 조건문 순서에 따라 탐색 비용과 유지보수 비용이 증가합니다.

개선 후에는 `HashMap<String, Function>`으로 action별 handler를 등록했습니다.

```text
Before

if action == list
else if action == update
else if action == search
else if ...

After

actionMap.get(action)
→ 해당 handler 실행
```

Java 공식문서에서 `HashMap`은 hash function이 key를 bucket에 잘 분산한다는 전제에서 `get`, `put` 같은 기본 연산에 constant-time 성능을 제공한다고 설명합니다.

| 항목         | Before         | After                       |
| ---------- | -------------- | --------------------------- |
| action 라우팅 | if-else 분기     | `HashMap<String, Function>` |
| action 추가  | Servlet 조건문 수정 | map에 handler 추가             |
| 설명         | 분기문 증가         | action table 구조             |

비전공자식으로 말하면,
기존에는 손님이 올 때마다 직원이 “이 손님은 어디로 보내지?”를 하나씩 물어보는 방식이고, 개선 후에는 **안내판에서 담당 창구를 바로 찾는 방식**입니다.

---

### 3-2. 컬럼 UPDATE DRY + 화이트리스트

기존에는 컬럼별 update SQL이 중복될 수 있고, 컬럼명을 문자열로 직접 다루면 SQL injection 위험이 커질 수 있습니다.

개선 후에는 `WarehouseIoColumn` enum으로 수정 가능한 컬럼을 제한하고, `updateColumn()`으로 공통 UPDATE 로직을 묶었습니다.

```text
Before

updateAmount()
updateStatus()
updateMemo()
...

After

WarehouseIoColumn enum
+ updateColumn(id, column, value)
```

Java `PreparedStatement` 공식문서는 SQL 문이 미리 컴파일되어 `PreparedStatement` 객체에 저장되고, 같은 SQL을 효율적으로 여러 번 실행할 수 있다고 설명합니다.

다만 `PreparedStatement`로도 컬럼명 자체를 `?` 파라미터로 바인딩하는 구조는 아니기 때문에, 컬럼명은 enum whitelist로 제한하고 값은 parameter로 바인딩하는 구조가 안전합니다.

비전공자식으로 말하면,
누구나 창고 물품표를 마음대로 고칠 수 있게 둔 것이 아니라, **“수정 가능한 항목 목록”을 먼저 만들고 그 안에서만 수정하게 한 것**입니다.

---

### 3-3. JSON/파라미터 처리 분리

Servlet 안에 JSON 응답 작성, request parameter 파싱, 비즈니스 분기, DAO 호출이 모두 섞이면 파일 하나가 너무 많은 책임을 갖게 됩니다.

개선 후에는 다음처럼 책임을 분리했습니다.

| 역할         | 파일                        |
| ---------- | ------------------------- |
| JSON 응답 작성 | `LegacyJsonWriter.java`   |
| 요청 파라미터 읽기 | `RequestParamReader.java` |
| action 라우팅 | `WarehouseIoServlet.java` |
| DB 접근      | `WarehouseIoDao.java`     |

비전공자 관점에서는 **접수 직원, 서류 검토자, DB 담당자를 한 사람에게 몰아주던 구조를 역할별로 나눈 것**입니다.

---

### 3-4. FR-011: 저장 후 list 재조회 제거

기존에는 셀 하나를 수정해도 서버에 update 요청을 보낸 뒤 다시 전체 list를 가져오는 구조였습니다.

```text
Before

셀 수정
→ update API 호출
→ list API 재호출
→ 전체 그리드 다시 그림
```

개선 후에는 update 성공 후 해당 row의 cell만 patch합니다.

```text
After

셀 수정
→ update API 호출
→ 응답받은 row patch
→ 전체 list 재조회 없음
```

| 지표           | Before            | After        | 개선            |
| ------------ | ----------------- | ------------ | ------------- |
| 필드 저장 API 호출 | 2회, update + list | 1회, update만  | 호출 1회 감소      |
| 그리드 UI 갱신    | 전체 재렌더            | 해당 행/셀 patch | DOM/API 작업 감소 |

비전공자식으로 말하면,
상품 가격 하나 바꿨다고 전체 장부를 다시 인쇄하던 방식에서, **바뀐 한 줄만 고쳐 쓰는 방식**으로 바꾼 것입니다.

---

### 3-5. FR-012: 투자자 검색 버킷 인덱스

기존에는 투자자 검색 시 전체 투자자를 다 훑고 `contains`를 수행했습니다.

개선 후에는 첫 글자 기준 bucket을 만들고, 해당 bucket 안에서만 `contains`를 수행합니다.

```text
Before

투자자 전체 목록
→ 모든 이름 contains 검색

After

첫 글자 bucket 선택
→ 해당 bucket 안에서 contains 검색
```

데모 3건에서 “김” 검색 시 후보가 3건에서 1건으로 줄어 후보 수가 감소했습니다.
또한 1만 건을 26개 bucket에 균등 분산한다고 가정하면 후보는 약 10,000건에서 약 385건 수준으로 줄어듭니다. 단, 이 수치는 실제 데이터 분포가 아니라 **균등 분포 가정에 따른 설계 설명용 수치**입니다.

비전공자식으로 말하면,
이름표 전체를 처음부터 끝까지 훑는 것이 아니라, **“김씨 파일함”부터 열고 그 안에서 찾는 방식**입니다.

---

## 4. Legacy 코드 효율화 측정 요약

| 지표                       | Before  | After        | 설명                   |
| ------------------------ | ------- | ------------ | -------------------- |
| 필드 저장 API 호출, FR-011     | 2회      | 1회           | update 후 list 재조회 제거 |
| 그리드 UI 갱신                | 전체 재렌더  | 해당 행/셀 patch | 화면 갱신 범위 축소          |
| 투자자 검색 후보, 데모 3건         | 3건 전체   | 1건 bucket    | 데모 데이터 기준 후보 감소      |
| 투자자 검색 후보, 1만 건 균등 분포 가정 | 10,000건 | 약 385건       | 균등 분포 가정 기준          |

문서용으로는 이렇게 쓰면 됩니다.

> Legacy Servlet 구조는 교육 목적상 일부 안티패턴을 유지하되, action routing, 컬럼 update, 저장 후 list 재조회, 투자자 검색 후보 축소처럼 효과가 분명한 부분은 개선했습니다. 특히 저장 후 전체 list 재조회 제거로 필드 저장 API 호출을 2회에서 1회로 줄였고, 투자자 검색은 첫 글자 bucket을 적용해 대량 데이터에서 검색 후보를 줄일 수 있는 구조로 개선했습니다.

---

## 5. Refactor 3모듈 코드 효율화

Refactor 3모듈은 같은 도메인을 Spring Boot 구조로 다시 만든 버전입니다.

```text
refactor-springboot-vanilla
refactor-springboot-vue
refactor-nextjs-springboot-api
```

---

### 5-1. Controller → Service → Repository 계층 분리

Refactor 구조는 요청 처리, 비즈니스 로직, 데이터 접근을 나눴습니다.

```text
Controller
→ 요청/응답 처리

Service
→ 비즈니스 규칙 처리

Repository
→ 데이터 조회/저장
```

Spring Data JPA 공식문서는 JPA repository 지원을 제공해 JPA 데이터 접근 애플리케이션 개발을 쉽게 만든다고 설명합니다.

---

### 5-2. 생성자 DI

Refactor 모듈은 Service나 Repository를 직접 `new`로 만들지 않고, 생성자 주입으로 받습니다.

```text
Before

Service 내부에서 Repository 직접 생성
→ 교체/테스트 어려움

After

생성자로 Repository 주입
→ Mock 교체 쉬움
→ 테스트 쉬움
```

Spring 공식문서에서 Dependency Injection은 객체가 필요한 dependency를 생성자 인자, factory method 인자, property 등을 통해 받는 방식이라고 설명합니다.

비전공자식으로 말하면,
요리사가 냉장고를 직접 만드는 것이 아니라, **필요한 냉장고를 주방 시스템에서 제공받는 구조**입니다.

---

### 5-3. Domain / JPA Entity 분리

Refactor 구조에서는 Service가 `@Entity`를 직접 노출하지 않고, domain model과 JPA persistence model을 분리했습니다.

```text
Service
→ WarehouseIo, Investor 같은 domain model 사용

Persistence
→ WarehouseJpaEntity, InvestorJpaEntity
→ Mapper로 변환
```

이 구조는 DB 컬럼 변경이 곧바로 API와 서비스 로직 전체에 번지는 것을 줄입니다.

비전공자식으로 말하면,
DB 안의 저장 양식과 화면/서비스에서 쓰는 업무 양식을 분리해둔 것입니다.

---

### 5-4. DTO Projection + Pageable

목록 API는 `SELECT *`로 전체 컬럼을 가져오지 않고, 목록 화면에 필요한 4개 컬럼만 가져오는 DTO projection을 사용했습니다.

Spring Data JPA 공식문서는 JPA에서 projection을 여러 방식으로 사용할 수 있고, interface 기반 projection을 만들 때 Tuple query를 사용한다고 설명합니다.

```text
Before

SELECT *
→ tb_warehouse_io 17컬럼
→ tb_investor 11컬럼

After

WarehouseListItem 4컬럼
InvestorListItem 4컬럼
Pageable로 20건씩 조회
```

| 지표                        | Before       | After              | 설명             |
| ------------------------- | ------------ | ------------------ | -------------- |
| `tb_warehouse_io` 목록 컬럼 수 | 17컬럼         | 4컬럼                | 목록에 필요한 컬럼만 조회 |
| `tb_investor` 목록 컬럼 수     | 11컬럼         | 4컬럼                | 목록에 필요한 컬럼만 조회 |
| 1,000건 목록 전송              | 1,000 row    | 20 row             | 페이지 단위 전송      |
| JPA 목록 SQL                | entity 전체 로딩 | projection + count | 목록 응답 최적화      |

비전공자식으로 말하면,
상세 서류 전체를 들고 오는 대신, **목록 화면에 필요한 이름·상태·날짜 같은 요약 정보만 가져오는 것**입니다.

---

### 5-5. open-in-view 끄기

Refactor 모듈은 `application-jpa.yml`, `application-prod.yml`에서 open-in-view를 껐습니다.

```text
open-in-view=true
→ Controller/View 단계에서 lazy loading 가능
→ 숨은 쿼리 발생 가능

open-in-view=false
→ Service 계층에서 필요한 데이터를 명시적으로 조회
→ 조회 책임이 더 명확해짐
```

비전공자식으로 말하면,
화면을 그리는 중간에 몰래 창고를 다시 다녀오지 못하게 하고, **서비스 단계에서 필요한 물건을 미리 챙겨오게 만든 구조**입니다.

---

### 5-6. GlobalExceptionHandler

Refactor 모듈은 `GlobalExceptionHandler`로 404/400/500 응답을 정리하고 예외 로그를 남깁니다.

```text
Before

각 Controller마다 에러 처리 중복

After

GlobalExceptionHandler에서 공통 처리
```

비전공자식으로 말하면,
각 창구 직원이 제각각 민원을 처리하는 것이 아니라, **공통 민원 처리 데스크를 만든 것**입니다.

---

### 5-7. AtomicLong ID

Memory repository에서는 `AtomicLong`으로 ID 시퀀스를 관리합니다.

```text
Memory repository
→ 테스트/데모 모드에서 DB 없이 ID 생성
→ AtomicLong으로 증가값 관리
```

비전공자식으로 말하면,
DB가 없어도 연습용 번호표 발급기가 있어서 테스트 데이터를 만들 수 있는 구조입니다.

---

## 6. DB 효율화 요약

LegacyPOC의 DB 효율화는 `refactor-db-schema/`에서 MariaDB, MySQL, PostgreSQL, Oracle 4개 dialect를 기준으로 관리합니다.

| 항목                  | 내용                                                                        | 파일                                                                                                |
| ------------------- | ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| 복합 인덱스, 입출고 목록      | `(deleted_yn, warehouse_io_id DESC)`                                      | `refactor-db-schema/*/schema.sql`, `legacy-servlet-jquery-oracle/oracle-schema/schema.oracle.sql` |
| 복합 인덱스, 증분 ETL      | `(updated_at, warehouse_io_id)`                                           | schema + `INDEX_GUIDE.md`                                                                         |
| 투자자 이름 검색           | `idx_investor_name`                                                       | schema                                                                                            |
| 변경 추적 컬럼            | `created_at`, `updated_at`, `deleted_at`, `source_system`, `etl_batch_id` | schema + `CHANGE_TRACKING_GUIDE.md`                                                               |
| 멱등 시드               | UPSERT / MERGE                                                            | `refactor-db-schema/*/data.sql`, `SEED_UPSERT_GUIDE.md`                                           |
| EXPLAIN 검증          | Q1~Q4 실행계획                                                                | `refactor-db-schema/*/explain-verify.sql`, `EXPLAIN_GUIDE.md`                                     |
| 파티셔닝 예시             | RANGE partition 예시                                                        | `*/partition-example.sql`, `PARTITIONING_GUIDE.md`                                                |
| View / MV / Summary | 집계 분리                                                                     | `*/summary-views-example.sql`, `VIEW_SUMMARY_GUIDE.md`                                            |
| DDL 가이드             | MariaDB / MySQL / PostgreSQL / Oracle                                     | `DDL_GUIDE.md`, `PK_IDENTITY_GUIDE.md`                                                            |

---

## 7. DB 효율화 공식문서 근거

### 7-1. 인덱스와 목록 조회

LegacyPOC는 활성 목록 조회에 맞춰 `(deleted_yn, warehouse_io_id DESC)` 복합 인덱스를 둡니다.

PostgreSQL 공식문서는 인덱스가 DB 서버가 특정 row를 더 빠르게 찾고 가져오도록 도와주지만, 전체 DB 시스템에는 overhead도 추가되므로 신중하게 사용해야 한다고 설명합니다.

```sql
WHERE deleted_yn = 'N'
ORDER BY warehouse_io_id DESC
LIMIT 20
```

```text
→ deleted_yn + warehouse_io_id DESC 복합 인덱스 적용
```

비전공자식으로 말하면,
책 전체를 매번 처음부터 끝까지 읽지 않도록 **책 뒤에 색인을 붙이는 것**입니다.

---

### 7-2. 증분 ETL 인덱스

증분 ETL은 `updated_at > :since` 조건으로 변경된 데이터만 가져오는 흐름입니다.

```text
전체 재적재
→ 모든 row 다시 읽기

증분 적재
→ updated_at 이후 변경된 row만 읽기
```

`(updated_at, warehouse_io_id)` 인덱스는 변경 시각 기준 range scan과 동일 시각 내 정렬/식별을 함께 고려한 구조입니다.

비전공자식으로 말하면,
매번 전체 출석부를 다시 확인하는 것이 아니라, **마지막 확인 시간 이후 바뀐 사람만 보는 방식**입니다.

---

### 7-3. Partial Index / contains 검색 한계

`LIKE '%김%'`처럼 앞에 wildcard가 붙는 contains 검색은 일반 B-tree 인덱스만으로 한계가 있습니다. 그래서 LegacyPOC에서는 기본 이름 인덱스는 두되, contains 검색 고도화는 v2.0에서 PostgreSQL `pg_trgm` 같은 검색 최적화로 검토할 수 있게 남겼습니다.

비전공자식으로 말하면,
“김으로 시작하는 이름”은 이름표 순서로 찾기 쉽지만, “이름 중간에 김이 들어가는 사람”은 일반 이름표만으로는 찾기 어렵습니다.

---

### 7-4. EXPLAIN 검증

LegacyPOC는 DB별 `explain-verify.sql`을 두어 실제 실행계획을 확인할 수 있게 했습니다.

PostgreSQL 공식문서는 `EXPLAIN`을 사용하면 planner가 어떤 query plan을 만드는지 확인할 수 있다고 설명합니다.

즉, 이 프로젝트는 단일 DB 기준이 아니라 **여러 DB에서 실행계획을 확인할 수 있는 교육용 구조**를 갖고 있습니다.

비전공자식으로 말하면,
“빨라졌을 것 같다”가 아니라, **DB가 실제로 어떤 길로 데이터를 찾는지 지도처럼 확인한 것**입니다.

---

### 7-5. MariaDB / MySQL / PostgreSQL / Oracle 멀티 dialect

LegacyPOC는 `refactor-db-schema/{mariadb,mysql,postgresql,oracle}`처럼 DB별 DDL을 분리했습니다.

```text
하나의 공통 SQL만 사용
→ DB별 문법 차이 때문에 깨질 수 있음

DB별 schema.sql 분리
→ dialect 차이를 명시적으로 관리
→ 교육용 비교 가능
```

비전공자식으로 말하면,
한국어, 영어, 일본어 문법이 다르듯이 DB도 문법이 조금씩 다르기 때문에, **DB별 설명서를 따로 둔 것**입니다.

---

## 8. DB 효율화 측정/설계치

아래 수치는 대량 데이터 기준 설계·EXPLAIN 검증용 지표로 쓰면 됩니다. 실제 수치는 DB 종류, 데이터 분포, 통계 정보, 실행 환경에 따라 달라질 수 있습니다.

| 쿼리                                             | 인덱스 없음          | 인덱스 있음                                | 설명                        |
| ---------------------------------------------- | --------------- | ------------------------------------- | ------------------------- |
| 활성 목록 `ORDER BY warehouse_io_id DESC LIMIT 20` | Seq Scan + Sort | `idx_warehouse_active` Index Scan     | 전체 스캔/정렬 회피 목표            |
| 증분 `WHERE updated_at > :since`                 | Full Scan       | `idx_warehouse_updated_at` Range Scan | 변경분 조회 목표                 |
| PK 단건 조회                                       | —               | PK Index                              | 단건 식별 조회                  |
| 투자자 `LIKE '%김%'`                               | Full Scan       | prefix 검색 중심                          | contains는 v2.0 pg_trgm 검토 |

문서용으로는 이렇게 쓰면 됩니다.

> LegacyPOC의 DB 효율화는 단일 DB에만 맞춘 튜닝이 아니라, MariaDB/MySQL/PostgreSQL/Oracle 4개 dialect별 DDL과 EXPLAIN 검증 스크립트를 분리한 구조입니다. 활성 목록 조회는 `(deleted_yn, warehouse_io_id DESC)` 복합 인덱스로 최신순 목록 조회를 지원하고, 증분 ETL은 `(updated_at, warehouse_io_id)` 인덱스로 변경분 조회를 빠르게 확인할 수 있게 했습니다. 투자자 이름 검색은 기본 `idx_investor_name`을 두되, contains 검색의 한계는 명확히 표시하고 v2.0에서 PostgreSQL `pg_trgm` 검토 대상으로 남겼습니다.

---

## 9. 모듈별 구조

```text
LegacyPOC/
├── legacy-servlet-jquery-oracle/
│   └── Servlet + jQuery
│       안티패턴 유지 + FR-011/FR-012 개선
│
├── refactor-springboot-vanilla/
│   └── Spring Boot + 정적 JS
│
├── refactor-springboot-vue/
│   └── Spring Boot + Vue
│       GlobalExceptionHandler 포함
│
├── refactor-nextjs-springboot-api/
│   └── Next.js + Spring API
│
└── refactor-db-schema/
    └── MariaDB / MySQL / PostgreSQL / Oracle
        DDL, 인덱스, EXPLAIN, 파티션, View/MV 예시
```

---

## 10. 공식문서 근거 요약

| 적용 내용                      | 공식문서 근거                                  |
| -------------------------- | ---------------------------------------- |
| README 작성 기준               | GitHub README 문서                         |
| `HashMap` action routing   | Java `HashMap` constant-time `get`/`put` |
| PreparedStatement 기반 값 바인딩 | Java `PreparedStatement` precompiled SQL |
| Repository 계층              | Spring Data JPA repository support       |
| DTO Projection             | Spring Data JPA Projections              |
| PostgreSQL 인덱스             | PostgreSQL Indexes                       |
| PostgreSQL EXPLAIN         | PostgreSQL EXPLAIN                       |

---

## 11. 최종 요약 문장

LegacyPOC는 레거시 Servlet/JDBC 구조와 Spring Boot 리팩터 구조를 비교하기 위한 교육용 PoC입니다. 레거시 모듈은 God Servlet, `SELECT *`, 저장 후 전체 list 재조회 같은 안티패턴을 일부 의도적으로 유지하되, action routing, 컬럼 update, JSON/파라미터 분리, row patch, 투자자 검색 bucket index처럼 효과가 분명한 부분은 개선했습니다.

Legacy 쪽에서는 `HashMap<String, Function>` 기반 action routing으로 if-else 분기를 줄였고, Java `HashMap`은 `get`, `put` 기본 연산에서 constant-time 성능을 제공합니다. 또한 `WarehouseIoColumn` enum과 `updateColumn()`을 통해 수정 가능한 컬럼을 whitelist로 제한하고, 값은 `PreparedStatement` parameter로 처리해 update 로직 중복과 동적 SQL 위험을 줄였습니다.

Refactor 3모듈은 Controller → Service → Repository 구조, 생성자 DI, Domain/JPA Entity 분리, DTO Projection, Pageable, open-in-view 비활성화, GlobalExceptionHandler를 적용했습니다. Spring Data JPA는 DTO projection과 repository 기반 데이터 접근을 지원하고, Spring Framework는 Dependency Injection으로 객체 간 의존성을 외부에서 주입하는 구조를 제공합니다.

DB 효율화는 `refactor-db-schema/` 아래에서 MariaDB, MySQL, PostgreSQL, Oracle 4개 dialect별 DDL과 EXPLAIN 검증 스크립트를 분리한 점이 핵심입니다. 활성 입출고 목록은 `(deleted_yn, warehouse_io_id DESC)` 복합 인덱스로 최신순 목록 조회를 지원하고, 증분 ETL은 `(updated_at, warehouse_io_id)` 인덱스로 변경분 조회를 빠르게 확인할 수 있게 했습니다.

결과적으로 LegacyPOC는 “레거시를 그냥 Spring으로 바꿨다”가 아니라, 동일한 업무 기능을 기준으로 레거시 구조의 병목과 리팩터 구조의 개선 효과를 비교할 수 있게 만든 프로젝트입니다. Servlet 쪽에서는 API 호출 감소와 검색 후보 축소 구조를 보여주고, Refactor 쪽에서는 목록 컬럼 I/O를 줄이고 페이지 단위 조회를 적용하는 DTO Projection + Pageable 구조를 보여줍니다.

---

## 12. 한 줄 설명

> **LegacyPOC는 Servlet/JDBC 레거시의 God Servlet·전체 재조회·전체 스캔 문제를 일부 개선하고, Spring Boot 리팩터 3종에서는 3계층·생성자 DI·DTO Projection·Pageable·멀티 DB 스키마·EXPLAIN 검증으로 코드와 DB 효율화를 비교할 수 있게 만든 교육용 리팩터링 PoC입니다.**

---

## 13. 포트폴리오 카드용 문장

> **Servlet 레거시와 Spring Boot 리팩터 구조를 비교하며, row patch로 저장 후 전체 재조회 제거, 투자자 검색 bucket으로 검색 후보 축소, DTO Projection·Pageable로 목록 조회 범위와 전송량을 줄이는 구조를 구현했습니다.**
