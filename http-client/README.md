# HTTP Client 자동 검증 테스트 스위트

이 프로젝트의 모든 API(학생 앱 22개 컨트롤러 전부)에 대한 IntelliJ HTTP Client(`.http`) 테스트다.
각 요청은 응답 핸들러 스크립트(`> {% client.test(...) %}`)로 status/응답 바디를 자동 assert하므로,
사람이 응답을 눈으로 읽어 통과 여부를 판단할 필요가 없다.

## 실행 준비

1. `docker compose up -d` — 로컬 Postgres 기동
2. **한 번만**: 담당자(Staff) 초대 코드 시드
   ```
   psql "postgresql://hellomate:hellomate@localhost:5432/hellomate" -f http-client/seed-invite-codes.sql
   ```
   (담당자 초대 코드를 발급하는 API가 아직 없어서, `POST /auth/staff/signup`을 쓰는 파일들은 이 시드가
   먼저 있어야 한다. 상세 이유는 `docs/roadmaps/2026-08-05-http-client-tests.md` 참고.)
3. `./gradlew bootRun` — 서버 기동 (`http://localhost:8080`)
4. IntelliJ에서 `http-client.env.json`의 `dev` 환경을 선택하고 각 `.http` 파일을 위에서 아래로 실행,
   또는 CLI: `ijhttp --env dev http-client/02-student-auth.http` 등

## 실행 순서

파일끼리 실행 순서 의존성은 없다(각 파일이 자기 테스트 데이터를 직접 만듦). 단 예외:

- **`03-staff-auth.http`의 회원가입 성공 테스트는 초대 코드 1개를 소모한다.** 이 파일을 다시 돌리려면
  `seed-invite-codes.sql`을 재실행해야 한다(다른 파일 코드는 그대로 두고 `HTTP-TEST-STAFFAUTH`만
  재발급해도 됨).
- 담당자 계정이 필요한 파일(`03`, `05`, `08`, `09`, `10`, `12`, `14`, `15`, `16`)은 파일마다 전용
  초대 코드를 하나씩 쓴다 — 서로 코드를 공유하지 않으므로 독립적으로 재실행 가능하다.

## 알려진 한계 (100% 자동화 불가능한 부분)

- **이메일 인증 코드**: `StubEmailService`가 6자리 코드를 서버 로그에만 남기고 조회 API가 없다.
  회원가입 이메일 인증(`/auth/students/email-verifications/confirm`), 비밀번호 재설정
  (`/auth/students/password-reset/verify`), 마이페이지 비밀번호 변경(`/students/me/password`)의
  **해피패스(정확한 코드로 성공)** 는 `.http` 파일만으로는 검증할 수 없다. 코드 발송 자체(200 성공)와
  잘못된 코드를 넣었을 때의 에러 응답(`400 INVALID_VERIFICATION_CODE`)까지는 자동 검증한다.
- **담당자 초대 코드 발급**: 위에서 설명한 대로 API가 없어 수동 SQL 시드가 필요하다.

두 갭 모두 코드에 실제로 없는 기능이라 `.http` 파일 쪽에서 우회할 방법이 없다 — 필요하면 스텁을
"고정 코드 반환" 모드로 바꾸거나 코드 조회용 테스트 전용 엔드포인트를 추가하는 별도 작업으로 제안한다
(`docs/reports/2026-08-05-http-client-tests.md` 참고).

## 컨벤션

- 요청/응답 JSON은 전부 **snake_case** (`spring.jackson.property-naming-strategy: SNAKE_CASE`).
- 모든 파일 첫 요청에서 `client.global.set("runId", Date.now())`로 그 실행 전용 유니크 값을 만들고,
  `login_id`/`email` 등에 `qa{{runId}}` 형태로 붙여써서 재실행해도 중복 가입 에러가 안 나게 한다.
- 로그인 성공 응답에서 `access_token`을 캡처해 이후 요청의 `Authorization: Bearer {{...AccessToken}}`
  으로 재사용한다.
- 에러 케이스도 최소 1개 이상 검증한다(`response.body.error.code`가 `ErrorCode` enum 이름과 일치하는지).
