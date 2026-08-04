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

## CLAUDE.md와 실제 설정이 다른 부분 발견 (이번 작업 범위 아님, 확인 필요)

CLAUDE.md는 "`ddl-auto: update` 쓰는 중"이라고 되어 있지만, 실제 `application.yaml`은
`ddl-auto: validate` + Flyway(`V1__init_schema.sql`)로 스키마를 관리하고 있었다. 다만 `V1__init_schema.sql`이
현재 빈 파일(0줄)이라 Postgres 실 스키마의 소스가 불명확하다 — 이번 작업으로 만든 문제는 아니고 기존
갭이니, 다음에 Postgres 붙여서 확인할 때 CLAUDE.md 갱신 여부와 함께 짚고 넘어가면 좋겠다. 신규 테이블은
`V2__add_email_verification.sql`로 추가해 Postgres 경로도 일단 일관되게는 맞춰뒀다(V1은 손대지 않음).

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
