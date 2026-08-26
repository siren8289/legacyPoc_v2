# FastAPI Refactor Version

`legacy-servlet-jquery-oracle`과 Spring Boot 리팩터 모듈을 **동일 REST 계약**으로 비교하는 Python 백엔드입니다.

Spring Boot의 Controller → Service → Repository, memory/JPA 전환, DTO projection, Pageable, Domain/Entity 분리를 FastAPI + Protocol + SQLAlchemy로 대응합니다. 프론트는 포함하지 않으며, `refactor-nextjs-springboot-api`처럼 API 전용 모듈입니다.

## 기술 스택

- FastAPI + Pydantic v2
- 저장소: memory (기본) / SQLAlchemy 2 (JPA 대응)
- OpenAPI: `/docs` (자동 생성)

## 계층

```text
Router
→ HTTP 계약, DTO validation

Service
→ 기본값 정규화, column whitelist, 도메인 예외

Repository Protocol
├ Memory*Repository     → dict + 시퀀스 + 검색 버킷
└ SqlAlchemy*Repository → Entity 조회 후 mapper로 domain 반환
```

## 실행

```bash
cd refactor-fastapi
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8084
```

- API: `http://localhost:8084`
- OpenAPI: `http://localhost:8084/docs`
- Health: `http://localhost:8084/health`

SQLAlchemy(MySQL) 모드:

```bash
# 스키마는 Spring Vue 모듈과 동일 테이블을 재사용한다.
# mysql ... < ../db/mysql/schema.sql
APP_REPOSITORY_TYPE=sqlalchemy \
APP_DATABASE_URL=mysql+pymysql://legacy_user:legacy_pass@localhost:3306/legacy_vue_refactor \
uvicorn app.main:app --port 8084
```

## 테스트

```bash
cd refactor-fastapi
source .venv/bin/activate
pytest
```

기본 테스트는 memory와 SQLite adapter다. MySQL EXPLAIN 수치는 이 모듈이 성능을 주장하지 않으며, `db/mysql/explain-verify.sql` 실행 결과로만 기록한다.
