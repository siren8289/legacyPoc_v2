# CODE WALKTHROUGH (FastAPI)

## 작업 방식

1. Spring Boot 리팩터의 `/api/warehouses`, `/api/investors` JSON 계약을 유지했습니다.
2. Router / Service / Repository Protocol 로 요청·업무·저장을 나눴습니다.
3. memory 구현과 SQLAlchemy 구현을 `APP_REPOSITORY_TYPE` 으로 교체합니다.
4. 필드 수정은 enum whitelist + PATCH 응답 row 로 전체 list 재조회를 제거합니다 (FR-004, FR-007).

## 핵심 흐름

```text
GET/POST/PATCH/DELETE /api/warehouses
→ warehouse/router.py
→ WarehouseService
→ WarehouseRepository (memory | sqlalchemy)
```

투자자도 동일합니다. 검색은 memory에서 첫 글자 버킷, SQLAlchemy에서는 `LIKE` 바인딩 + `idx_investor_name` 후보를 사용합니다.

## 파일별 포인트

- `app/features/*/router.py` — HTTP 경로·메서드, Spring Controller 대응
- `app/features/*/service.py` — null 기본값, 컬럼 화이트리스트, 404/400 도메인 예외
- `app/features/*/repository.py` — Protocol (Spring interface)
- `app/features/*/memory_repository.py` — DB 없는 데모
- `app/features/*/sqlalchemy_repository.py` — projection SELECT + soft delete
- `app/persistence/entities.py` — 테이블 매핑, 도메인 dataclass와 분리
- `app/common/columns.py` — `WarehouseIoColumn` allowlist
- `app/common/handlers.py` — stack/SQL 미노출 공통 오류

## Spring 대비 의도적 차이

- FastAPI는 PATCH/DELETE 를 컨트롤러에 노출한다. Spring 모듈은 repository에만 있고 컨트롤러 미연결 상태다.
- 없는 id 상세는 SRS ERR-007 대로 404를 반환한다.
- OpenAPI 문서는 프레임워크가 생성한다.
