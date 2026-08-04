# 보고서 — 비밀번호 찾기 / 회원가입 이메일 인증 / 서류 인증 상태 조회 / 마이페이지 비밀번호 변경

로드맵: `docs/roadmaps/password-reset-email-verification.md`

## 한 일

로드맵대로 신규 `email` 도메인(인터페이스+스텁)과 `auth` 도메인 `EmailVerification` 공통 컴포넌트를 만들고,
이를 재사용해 아래 10개 엔드포인트를 구현했다.

- `POST /auth/students/check-email`
- `POST /auth/students/email-verifications`, `POST /auth/students/email-verifications/confirm`
- `POST /auth/students/password-reset/email`, `POST /auth/students/password-reset/verify`,
  `PATCH /auth/students/password-reset`
- `GET /students/me/verification-documents`
- `PATCH /students/me/password`

로드맵 대비 달라진 점은 없다. `docs/resources/api-reference.md`도 위 엔드포인트를 반영해 업데이트했고(각
섹션에 요청/응답/에러 표 추가), "미구현 API" 목록에서 해당 4개 항목을 제거했다.

## 로드맵 대비 설계 결정 (기획 미확정 부분 스텁 처리)

- **학교 이메일 도메인 검증**: `University`에 이메일 도메인 컬럼이 없어서, `.ac.kr`/`.edu` 접미사만 보는
  스텁 수준 검증으로 처리(`EmailVerificationService.isSchoolEmail`). 학교별 화이트리스트는 후속 과제.
- **비밀번호 찾기 계정 존재 노출**: `api-errors.md`가 제안한 대로 `STUDENT_NOT_FOUND`를 그대로 노출하는
  방식으로 갔다(계정 존재 여부 비노출 방식은 후속 검토 대상으로 남김).
- **마이페이지 비밀번호 변경용 코드 발송**: 별도 엔드포인트를 새로 만들지 않고 기존
  `POST /auth/students/password-reset/email`을 재사용하도록 설계(로그인 상태에서도 호출 가능한 public
  엔드포인트라 문제 없음). `PATCH /students/me/password`는 `resetToken` 단계 없이 코드를 바로 검증하고
  비밀번호를 바꾼다(본인 이메일 기준으로만 검증하므로 토큰 레이어가 굳이 필요 없음).
- **코드 유효시간**: 5분 고정(와이어프레임 `00:00` 타이머와 일치). 재사용/재확인 방지를 위해
  `EmailVerification.used` 플래그와 `resetToken` 1회성 무효화(`invalidateResetToken`)를 둠.

## CLAUDE.md와 실제 설정이 다른 부분 발견 → V1 마이그레이션 채워 넣음 (추가 작업)

CLAUDE.md는 "`ddl-auto: update` 쓰는 중"이라고 되어 있지만, 실제 `application.yaml`은
`ddl-auto: validate` + Flyway로 스키마를 관리하고 있었다. 그런데 `V1__init_schema.sql`이 빈 파일(0줄)이라,
새 Postgres에 `flyway migrate`를 돌리면 `email_verification`(V2) 테이블만 생기고 나머지 기존 테이블(users,
club, notice, post, chat_thread 등)은 하나도 안 생겨서 앱이 시작조차 안 되는 상태였다. 사용자 확인 후
V1을 원래 스키마(기존 엔티티 23종 전체)로 채워 넣었다:

- 로컬에 Docker/Postgres가 전혀 떠 있지 않고(`docker volume ls`/`docker ps` 모두 비어있음, `docker info` 자체가
  실패) V1이 어제 빈 파일로 커밋된 뒤 한 번도 수정되지 않은 것을 git log로 확인 — 즉 이 V1을 실제로
  `flyway migrate`한 환경이 없었다는 뜻이라 지금 채워도 체크섬 충돌이 날 곳이 없다.
- 모든 엔티티(`domain/**/entity/*.java`)를 직접 읽어 컬럼 타입/길이/nullable/유니크 제약/FK를 그대로
  옮겼다. `refresh_token`, `post_anon_participant`는 `BaseTimeEntity`를 상속하지 않아 `created_at`/`updated_at`이
  없다는 것도 코드 기준으로 정확히 반영.
- **실제로 검증**: H2를 PostgreSQL 호환 모드(`MODE=PostgreSQL`)의 파일 DB로 띄우고, `ddl-auto: validate` +
  `spring.flyway.enabled: true`로 임시 테스트 프로파일을 만들어 `V1`+`V2`를 실제로 migrate시킨 뒤 Spring
  컨텍스트가 정상 기동되는지 확인했다 — Flyway가 두 마이그레이션을 순서대로 적용하고, Hibernate
  `validate`가 전체 엔티티에 대해 통과함을 로그로 직접 확인(`Initialized JPA EntityManagerFactory` 성공,
  검증 에러 없음). 검증에 쓴 임시 파일(`SchemaValidateCheckTest.java`,
  `application-schemacheck.yaml`)은 확인 후 삭제했다.
- CLAUDE.md의 "ddl-auto: update 쓰는 중" 문구는 이제 실제와 다르므로 갱신이 필요하다(이번엔 안 건드림 —
  원하면 별도로 반영).

## 빌드/테스트 결과

`./gradlew test`, `./gradlew build` 모두 통과(H2 `create-drop`으로 스키마 검증됨). 기존 테스트 스위트가
`HelloMateBackendApplicationTests` 하나뿐이라 신규 도메인 단위 테스트는 추가하지 않았다(기존 도메인들도
전부 테스트 없이 컨트롤러/서비스만 있는 패턴을 그대로 따름).

## 남은 이슈 / 다음 작업 후보

- 사용자가 이번에 선택하지 않은 나머지 미구현 API: 전공 검색, 클럽 그룹 채팅, 알림 피드/설정, 내가 쓴
  글/댓글.
- 학교별 이메일 도메인 화이트리스트(현재 `.ac.kr`/`.edu` 하드코딩) — `University`에 컬럼 추가할지 결정
  필요.
- Flyway `V1__init_schema.sql`이 비어있는 문제 — Postgres 환경에서 실제로 migrate가 되는지 확인 필요.

## 커밋 메시지 제안

```
✨ Feature : 비밀번호 찾기, 회원가입 이메일 인증, 서류 인증 상태 조회, 비밀번호 변경 API 추가
```
