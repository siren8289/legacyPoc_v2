# Refactor DB Schema

리팩터 Spring Boot 모듈용 DB 스키마입니다. 레거시 Oracle `TB_WAREHOUSE_IO_SCREEN`과 분리된 **리팩터 전용 테이블**을 사용합니다.

## Repository 계층 (JPA)

```
Service                    → WarehouseIo / Investor (도메인, JPA 어노테이션 없음)
  ↓
WarehouseRepository interface
  ├─ Memory*Repository      → 도메인 직접 저장
  └─ Jpa*Repository           → Entity 저장 후 Mapper로 도메인 반환
        ↓
SpringData*JpaRepository     → *JpaEntity (@Entity)
        ↓
tb_warehouse_io / tb_investor
```

**Domain / Entity 분리:** `WarehouseIo`는 Service 전용 도메인 record이고, `WarehouseJpaEntity`만 `@Entity`·`@Table`·`@Column`을 가진다. `WarehouseJpaMapper`가 양쪽을 변환한다.

## 테이블

| 테이블 | Entity | 설명 |
|--------|--------|------|
| `tb_warehouse_io` | `WarehouseJpaEntity` | 입출고 (soft delete) |
| `tb_investor` | `InvestorJpaEntity` | 투자자 (`IDX_INVESTOR_NAME`) |

## 모듈별 DB

| 모듈 | 포트 | DB | 스키마 경로 |
|------|------|-----|-------------|
| springboot-vanilla | 8081 | MariaDB `legacy_refactor` | `mariadb/` |
| springboot-vue | 8082 | MySQL `legacy_vue_refactor` | `mysql/` |
| springboot-api (nextjs) | 8083 | PostgreSQL `legacy_next_refactor` | `postgresql/` |
| (참고) Oracle | — | — | `oracle/` |

**DDL 상세:** [DDL_GUIDE.md](./DDL_GUIDE.md) · **PK:** [PK_IDENTITY_GUIDE.md](./PK_IDENTITY_GUIDE.md) · **인덱스:** [INDEX_GUIDE.md](./INDEX_GUIDE.md) · **SELECT:** [SELECT_QUERY_GUIDE.md](./SELECT_QUERY_GUIDE.md) · **EXPLAIN:** [EXPLAIN_GUIDE.md](./EXPLAIN_GUIDE.md) · **Seed/Upsert:** [SEED_UPSERT_GUIDE.md](./SEED_UPSERT_GUIDE.md) · **변경 추적:** [CHANGE_TRACKING_GUIDE.md](./CHANGE_TRACKING_GUIDE.md) · **파티셔닝:** [PARTITIONING_GUIDE.md](./PARTITIONING_GUIDE.md) · **View/요약:** [VIEW_SUMMARY_GUIDE.md](./VIEW_SUMMARY_GUIDE.md)

## 초기화

```bash
# MariaDB 예시
mysql -u root -p -e "CREATE DATABASE legacy_refactor CHARACTER SET utf8mb4;"
mysql -u legacy_user -plegacy_pass legacy_refactor < refactor-db-schema/mariadb/schema.sql
mysql -u legacy_user -plegacy_pass legacy_refactor < refactor-db-schema/mariadb/data.sql
```

## 저장소 전환

| 모드 | 설정 | 설명 |
|------|------|------|
| **memory** (기본) | `app.repository.type: memory` | DB 없이 데모. JPA/DataSource 자동설정 비활성 |
| **jpa** | `--spring.profiles.active=jpa` | schema.sql 적용 후 JPA Repository 사용 |

```bash
java -jar app.jar --spring.profiles.active=jpa
```

## 의존성

- `spring-boot-starter-data-jpa`
- DB 드라이버: MariaDB / MySQL / PostgreSQL (모듈별)
