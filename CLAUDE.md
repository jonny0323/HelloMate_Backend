# CLAUDE.md

너는 이 프로젝트를 맡은 시니어 백엔드 개발자다. HelloMate 백엔드고, 유학생 캠퍼스 플랫폼이다.
인천대 1차 런칭 2026.09 목표. 학생 앱/담당자 웹/관리자 콘솔 세 클라이언트가 이 서버 하나를 같이
쓴다. `Role`(`STUDENT`,`STAFF`)로 갈리고 `/admin/**`은 STAFF만.

## 스택

Java 21 / Spring Boot 3.5 / PostgreSQL 16 / JWT(jjwt) / springdoc. 테스트는 H2로 돌리니까 로컬에
Postgres 안 띄워도 `./gradlew test`는 통과해야 정상이다.

```bash
docker compose up -d   # Postgres
./gradlew bootRun      # 8080
./gradlew test
```

## 구조 — 이거 깨지 마라

도메인 주도 패키지. `domain/{도메인}/controller · dto/request · dto/response · entity · repository ·
service`. 공통 로직은 `global/{common,config,security}`에만 넣고 도메인 패키지에 흘리지 않는다.

도메인: admin, auth, chat, club, community, file, honeytip, notice, search, staff, student,
translation, university, verification.

새 도메인/기능 만들 때 옆 도메인 아무거나 열어서 패턴 그대로 베껴라. 여기서 새로운 스타일 만들지
말 것 — 통일성이 코드 품질보다 중요한 프로젝트 단계다.

## 규칙

**PK는 UUID 문자열이다.** `String id`, `UuidCreator.create()`로 생성, `@GeneratedValue` 쓰지 마라.
캐시/로그성 테이블(`TranslationCache` 등)만 예외로 `Long id` + IDENTITY 써도 된다. 헷갈리면 UUID로
가라.

**응답은 무조건 `ApiResponse<T>`로 감싼다.** `{success, data, meta, error}`. 컨트롤러에서 맨몸으로
DTO 리턴하지 마라. JSON은 snake_case로 나간다(Jackson 전역 설정) — DTO는 그냥 camelCase로 짜면 알아서
변환된다, 수동으로 스네이크케이스 필드명 쓰지 마라.

**에러는 `ErrorCode` + `BusinessException`.** 새 에러 상황 생기면 `ErrorCode`에 등록하고
`throw new BusinessException(...)` 던져라. try-catch로 컨트롤러에서 직접 처리하지 마라,
`GlobalExceptionHandler`가 다 받는다.

**페이지네이션은 커서 기반이 기본이다.** UUID는 정렬 기준이 안 되니까 `createdAt`을 커서로 쓴다
(`CursorPageUtil`). `Slice` 반환 + `CursorMeta`. offset이 실제로 맞는 목록(정렬 고정된 관리자 화면 등)
말고는 커서로 가라.

**인증은 `@CurrentUser AuthPrincipal`로 받는다.** id/role만 들어있다. university_id는 토큰에 없다 —
필요하면 principal.id()로 엔티티 다시 조회해서 꺼내라. 매 요청 조회 비용 신경 쓰이면 나중에 토큰
클레임에 넣는 걸 고려하되, 지금 단계에서 미리 손대지 마라.

**번역/파일은 스텁이다.** `TranslationService`(→`StubTranslationService`), `FileStorageService`
(→`LocalStubFileStorageService`) 둘 다 인터페이스 뒤에 스텁 박아놓은 거다. NLLB-200/S3 붙일 때
구현체만 새로 만들어 갈아끼워라, 인터페이스나 호출부 건드릴 필요 없다. 번역 캐시는
`TranslationCache`에 (content_type, content_id, target_lang)로 저장.

**Staff는 SSO 없다.** 부서별 1회용 초대 코드 + 학교 이메일 인증으로 온보딩한다. SSO는 나중 얘기,
지금 짜지 마라.

**주석은 핵심 로직에만, "왜"만 적는다.** 뭘 하는지는 코드로 알 수 있다 — 코드만 봐서 이유를 알 수
없을 때만 한 줄 남겨라.

## 지금 비어있는 것 (알고 있는 갭, 놀라지 말 것)

- `ChatMessage`에 `context_type`/`context_id` 없음 → "이 공지 문의하기" 같은 딥링크 채팅 아직 못 만듦
- JWT에 `university_id` 클레임 없음
- `ddl-auto: validate` + Flyway(`src/main/resources/db/migration/V*__*.sql`)로 스키마를 관리한다.
  엔티티에 컬럼/테이블을 추가·변경하면 반드시 새 `V{n}__*.sql` 마이그레이션도 같이 추가해야
  Postgres에서 `validate`가 통과한다 — 엔티티만 고치고 마이그레이션을 빼먹으면 앱이 기동 자체가
  안 된다. 이미 적용된 `V` 파일은 체크섬 때문에 수정하지 말고 새 버전을 추가할 것. 테스트는 H2
  `ddl-auto: create-drop`이라 마이그레이션 없이도 통과하니, 로컬에서 `./gradlew test`만 돌려서는 이
  실수를 못 잡는다 — Postgres 붙여서 확인하거나 리뷰 때 마이그레이션 파일 존재 여부를 챙길 것.

## 문서 — `docs/`

작업 관련 문서는 전부 `docs/` 밑에 모은다. 하위 디렉토리 셋:

- `docs/roadmaps/` — 작업 시작 전 로드맵
- `docs/reports/` — 작업 끝난 뒤 보고서
- `docs/resources/` — 커밋 컨벤션, 코드 컨벤션 등 작업할 때 참고하는 자료 (예: `conventions.md`)

파일명은 작업 단위로: `docs/roadmaps/{도메인 또는 기능}.md`, `docs/reports/{도메인 또는 기능}.md`.
같은 이름이면 로드맵-보고서가 짝이 맞아서 나중에 찾기 쉽다.

## 작업 순서

1. **시작 전 — 로드맵 작성.** `docs/roadmaps/`에 뭘 할지, 어떤 파일을 건드릴지, 어떤 순서로 할지
   적어라. 같은 도메인 기존 코드부터 읽고(에러 처리, 응답, 트랜잭션 경계 어떻게 했는지 확인), 그
   내용도 로드맵에 반영한다.
2. **참고 자료 확인.** 코드 짜기 전에 `docs/resources/`에 관련 자료 있는지 먼저 봐라(커밋 메시지
   태그는 `conventions.md` 참고). 있으면 그 컨벤션 따르고, 없으면 옆 도메인 패턴 그대로 베껴라 —
   여기서 새 스타일 만들지 말 것.
3. **구현.** 도메인 넘나드는 참조는 Repository/Service 직접 주입으로 — 순환 의존 만들면 안 된다.
4. **빌드 & 테스트.** 커밋 전 `./gradlew build` (혹은 최소 `./gradlew test`) 무조건 통과시켜라. 실패한
   채로 다음 단계로 넘어가지 마라.
5. **커밋 메시지.** `docs/resources/conventions.md`의 이모지 태그 규칙을 따라 추천 문구를 제시한다.
   형식은 `{이모지} {Tag} : {요약}` (예: `✨ Feature : 공지 상세 조회 API 추가`). 여러 태그가 걸치면
   가장 비중 큰 것 하나로 고른다.
6. **끝난 후 — 보고서 작성.** `docs/reports/`에 뭘 했는지, 로드맵 대비 뭐가 달라졌는지, 빌드/테스트
   결과, 남은 이슈나 다음 작업 후보를 적어라.
