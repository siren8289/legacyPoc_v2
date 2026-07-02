# Legacy Servlet + jQuery + Oracle Sample

기존 구조를 재현한 뒤 리팩토링 포인트를 분석/개선하기 위한 샘플 프로젝트입니다.

## 기술 스택

| 영역 | 스택 |
|------|------|
| 백엔드 | Java Servlet + JDBC (Oracle) |
| 프론트 | jQuery + RealGrid |
| DB | Oracle (`TB_WAREHOUSE_IO_SCREEN`) |

## 의도적으로 재현한 레거시 특징

- jQuery 기반 프론트엔드
- Spring Boot가 아닌 Servlet 기반
- 단일 Servlet에서 화면 요청/응답 처리 로직 집중
- DB 직접 연결 (DAO에서 JDBC 직접 사용)
- 화면 단위 데이터가 단일 비정규화 테이블에 집중
- JSON 직렬화도 수동 문자열 생성
- 화면이 5개로 분리되어 있으며 화면별 JS/CSS 중복 다수

## 구조

```text
legacy-servlet-jquery-oracle/
├── jquery-webapp/
│   └── src/main/webapp/
│       ├── WEB-INF/web.xml
│       ├── pages/
│       ├── static/js/
│       └── index.html
├── servlet-jdbc-backend/
│   └── src/main/java/com/example/legacy/
│       ├── servlet/
│       ├── dao/
│       ├── dto/
│       └── util/
├── oracle-schema/
│   ├── schema.mock.sql
│   └── sample-data.sql
└── README.md
```

## 리팩토링 분석 포인트 예시

1. 수동 JSON 직렬화 제거 (Jackson 등으로 교체)
2. DB 설정 외부화 및 커넥션 풀 도입
3. 화면 전용 비정규화 테이블 의존 축소
4. 프론트엔드에서 화면/데이터/이벤트 로직 분리
5. XML/Servlet 설정에서 Java Config 또는 Spring Boot로 단계적 전환

## 컬럼 일괄 반영 구조

- 공통 컬럼 정의는 `jquery-webapp/src/main/webapp/static/js/common-table.js`의 `columns` 배열 한 곳에서 관리
- 5개 화면은 모두 `LegacyGrid.initPage(...)`를 호출해 동일한 컬럼/렌더러를 공유

## 등록/조회 레거시 구조 (warehouse 화면)

- 조회: `/warehouse?action=list`
- row 클릭: `/warehouse?action=detail&id={id}`
- 신규등록: `/warehouse?action=createEmptyRow` 후 필드별 `update*` API
- 삭제: `/warehouse?action=delete&id={id}` (소프트 삭제)

## 로컬 실행

```bash
cd legacy-servlet-jquery-oracle
mkdir -p out
javac -encoding UTF-8 -d out servlet-jdbc-backend/src/main/java/**/*.java
java -cp out com.example.legacy.LegacySampleServer
```

서버 실행 후:

- 화면: `http://localhost:8080/index.html`
- API: `http://localhost:8080/api/warehouse-io`

## 리팩토링 단계

| 폴더 | 스택 |
|------|------|
| `refactor-springboot-vanilla` | Spring Boot + Vanilla JS |
| `refactor-springboot-vue` | Spring Boot + Vue 3 |
| `refactor-nextjs-springboot-api` | Next.js + Spring Boot API |
