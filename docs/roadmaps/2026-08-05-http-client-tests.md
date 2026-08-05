# HTTP Client 자동 검증 테스트 스위트

## 목적

프로젝트의 모든 API(학생 앱 + 담당자 웹 + 관리자 콘솔, 총 22개 컨트롤러)에 대해 IntelliJ HTTP
Client(`.http`) 파일을 작성한다. 응답 바디/상태코드를 사람이 눈으로 확인하지 않아도 되도록, 각 요청마다
`client.test()` 응답 핸들러 스크립트로 자동 assert를 건다(IntelliJ `ijhttp` CLI 러너로 CI에서도 그대로
돌릴 수 있음).

## 기존 코드 조사 결과

- 컨트롤러 22개(`domain/*/controller`) 전수 조사 완료. `docs/resources/api-reference.md`는 학생 앱만
  다루고, `🔨 Refactor : 오푸스로 전면 개편`(75074fa) 이후 필드가 꽤 늘어서(클럽 카드 상태, 꿀팁 스텝,
  공지 배너, 학생 인증 상태/서류 유형, 관리자 대시보드/학생 디렉토리/게시글 모더레이션 등) 문서보다 실제
  DTO를 기준으로 삼았다.
- **중요**: `application.yaml`에 `spring.jackson.property-naming-strategy: SNAKE_CASE`가 전역
  설정되어 있다(`CLAUDE.md`에도 명시). 즉 모든 요청/응답 JSON 키는 실제로 snake_case로 오간다
  (`loginId` → `login_id`, `accessToken` → `access_token`). `api-reference.md`의 예시는 camelCase로
  적혀 있어 실제 와이어 포맷과 다르다 — `.http` 파일은 snake_case로 작성한다.
- 응답 봉투는 항상 `{success, data, meta, error}` (`ApiResponse<T>`). 실패 시 `error: {code, message}`,
  `code`는 `ErrorCode` enum 이름 그대로(예: `POST_NOT_FOUND`).
- 인증: `Authorization: Bearer {accessToken}`, JWT는 `role` 클레임(`STUDENT`/`STAFF`)만 갖고 있다.
  `/admin/**`만 `hasRole(STAFF)`로 게이트되고 나머지는 `anyRequest().authenticated()`—role 불일치를
  걸러주는 건 서비스 계층(`STUDENT_NOT_FOUND`/`STAFF_NOT_FOUND`)이다.
- 시드 데이터는 `V4__add_major.sql`의 `university(id='univ-inu')` + 전공 10개뿐. 학생/담당자/공지/클럽/
  꿀팁 등은 전부 `.http` 파일이 실행 시점에 API로 직접 만든다.
- **담당자(Staff) 초대 코드를 발급하는 API가 어디에도 없다** (관리자 콘솔에도 없음 — 알려진 갭). 초대
  코드 테이블(`staff_invite_code`)에 직접 SQL로 미리 넣어둬야 `POST /auth/staff/signup`을 테스트할 수
  있다. `http-client/seed-invite-codes.sql`을 로컬 Postgres에 한 번 실행해야 한다.
- 이메일 인증 코드(`EmailVerificationService`)는 `StubEmailService`가 서버 로그에만 남기고 코드를 어떤
  API로도 조회할 수 없다. 따라서 회원가입 이메일 인증/비밀번호 재설정의 "성공" 해피패스는 100% 자동화가
  불가능하다 — 코드 발송 자체(200)와 잘못된 코드 입력 시 에러(400 `INVALID_VERIFICATION_CODE`)까지는
  자동 검증하고, 그 이상은 로드맵 밖으로 둔다(알려진 한계로 보고서에 기록).

## 구조

리포 루트에 `http-client/` 디렉토리를 새로 만든다(테스트 결과물이라 `src/` 밖에 둠).

```
http-client/
  http-client.env.json       # host = http://localhost:8080
  seed-invite-codes.sql      # 담당자 초대 코드 9개(파일당 1개, 비만료) 수동 시드용
  README.md                  # 실행 방법, 시드 순서, 알려진 한계
  01-university-major.http
  02-student-auth.http
  03-staff-auth.http
  04-student-profile.http
  05-verification.http       # files + verification(student) + admin verification review
  06-club.http
  07-community-post.http     # posts + comments
  08-honeytip.http           # student + admin honey-tip
  09-notice.http             # student + admin notice
  10-chat.http
  11-notification.http
  12-search.http
  13-translation.http
  14-admin-dashboard.http
  15-admin-student-directory.http
  16-admin-post-moderation.http
```

각 파일은 **독립 실행 가능**하게 짠다(실행 순서 의존 없음) — 파일 맨 위에서 그 파일에 필요한
학생/담당자 계정을 직접 회원가입시키고, `Date.now()` 기반 `runId`로 `login_id`/`email`을 매 실행마다
고유하게 만든다. 담당자가 필요한 파일은 자기 전용 초대 코드를 쓴다(파일끼리 초대 코드를 공유하지
않음 — 코드가 1회용이라 공유하면 재실행 시 깨짐).

## 컨벤션

- 모든 요청 앞에 `< {% request.variables.set(...) %}`로 필요한 동적값을 만들거나, 첫 요청에서
  `client.global.set("runId", Date.now())`로 파일 전체에서 쓸 유니크 시드를 하나 만든다.
- 모든 응답 뒤에 `> {% client.test(...) %}`로 최소 status/success/핵심 필드를 assert하고, 후속 요청이
  참조할 id/토큰은 `client.global.set()`으로 저장한다.
- 요청 바디 JSON 키는 snake_case로 작성.
- 에러 케이스도 최소 1개 이상 자동 검증(404/409/400/403 등 — `ErrorCode` enum 이름까지 확인).

## 순서

1. 로드맵 작성 (이 문서)
2. `http-client/` 스캐폴딩(env, seed sql, README)
3. 16개 `.http` 파일 작성
4. 가능하면 `docker compose up -d` + `./gradlew bootRun` + `ijhttp`(또는 IDE)로 최소 1~2개 파일 실행
   검증 — 안 되면 정적 검토(경로/필드명 재대조)로 대체하고 보고서에 명시
5. 보고서 작성 (`docs/reports/2026-08-05-http-client-tests.md`)
