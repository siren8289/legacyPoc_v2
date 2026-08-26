# LegacyPOC — Data Engineering (Snapshot / Reconciliation)

> **Legacy DB와 Refactor DB를 같은 grain·schema·lineage 기준으로 snapshot하고, migration 후 정합성을 reconciliation evidence로 증명하는 Data Engineering 영역**

---

## 1. 목적

LegacyPOC DE는 대규모 실시간 파이프라인 구축이 목표가 아닙니다.
**같은 데이터 기준**으로 legacy/refactor를 비교할 수 있도록 snapshot, normalize, load, reconciliation 규칙을 고정하는 것이 목적입니다.

```text
Legacy Snapshot
→ Schema Normalize
→ Refactor Load
→ Reconciliation
→ QA Evidence
```

---

## 2. 문서 구성

| 문서 | 내용 |
| --- | --- |
| [de-snapshot-reconciliation.md](./de-snapshot-reconciliation.md) | Data Contract, Snapshot Flow, Idempotency, DQ, Public Data Ingestion |

---

## 3. 핵심 원칙

- raw snapshot은 변경하지 않고 hash/count를 남긴다.
- business key와 upsert 정책으로 동일 snapshot 재실행 시 duplicate를 막는다.
- batch마다 `run_id`, source version, row count, reject count를 기록한다.
- 성능 주장은 `EXPLAIN ANALYZE` 등 실행 evidence 없이 확정하지 않는다.

관련 DB DDL·EXPLAIN·시드 스크립트는 루트 `db/`에서 관리합니다.
