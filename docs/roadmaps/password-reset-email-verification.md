# 로드맵 — 비밀번호 찾기 / 회원가입 이메일 인증 / 서류 인증 상태 조회 / 마이페이지 비밀번호 변경

## 배경

`docs/resources/api.md`, `docs/resources/api-errors.md`의 🆕 항목 중 사용자가 선택한 범위:

1. 비밀번호 찾기 3종 (`/auth/students/password-reset/*`)
2. 회원가입 이메일 중복확인·인증코드 4종 (`check-email`, `email-verifications`, `email-verifications/confirm`)
3. 서류 인증 상태 조회 (`GET /students/me/verification-documents`)
4. 로그인 상태 비밀번호 변경 (`PATCH /students/me/password`)

`api.md`가 권장한 대로, 비밀번호 찾기와 회원가입 이메일 인증은 "이메일로 6자리 코드 발송 → 확인" 로직이
동일하므로 공통 컴포넌트로 설계한다. 마이페이지 비밀번호 변경도 같은 코드 방식이므로 재사용한다.

## 기존 코드 확인 결과

- `StudentAuthController`/`StudentAuthService` — signup/login/refresh/logout 패턴 확인. `BusinessException(ErrorCode, "커스텀 메시지")` 오버로드 사용 예시 있음(존재하지 않는 학교).
- `FileStorageService`/`LocalStubFileStorageService` — 인터페이스+스텁 구현체 패턴. 새 `email` 도메인도 동일하게 간다(실제 SMTP 연동 전까지 로그만 남기는 스텁).
- `VerificationController`/`VerificationService`/`VerificationDocumentRepository` — 제출(POST)만 있고 조회 없음. `VerificationDocumentResponse.from(entity)` 재사용 가능.
- `Student` 엔티티에 비밀번호 변경 메서드 없음 → `updatePassword` 추가 필요.
- **중요한 발견 — CLAUDE.md와 실제 설정이 다름**: CLAUDE.md는 "`ddl-auto: update` 쓰는 중"이라고 되어 있지만, 실제 `application.yaml`은 `ddl-auto: validate` + Flyway(`V1__init_schema.sql`)로 스키마를 관리한다. 다만 `V1__init_schema.sql`은 현재 빈 파일(0줄)이라 Postgres 스키마 소스가 불명확한 상태(기존 갭, 이번 작업 범위 아님). 테스트는 `src/test/resources/application.yaml`에서 H2 + `ddl-auto: create-drop`이라 Flyway와 무관하게 통과한다. 신규 테이블은 Flyway 쪽에도 `V2__` 마이그레이션으로 추가해 Postgres 경로도 일관되게 유지한다(V1은 손대지 않음 — 이미 존재하는 마이그레이션 수정은 위험).
- `University`에 이메일 도메인 컬럼 없음 → "학교 공식 이메일" 검증은 스텁 수준으로 `.ac.kr`/`.edu` 접미사 검사로 간다(실제 학교별 도메인 화이트리스트는 추후 과제, `api.md`도 이 부분 기획 미확정이라고 명시함).

## 설계

### 신규 `email` 도메인 (번역/파일과 동일한 스텁 패턴)
- `domain/email/service/EmailService.java` — 인터페이스, `sendVerificationCode(String to, String code)`.
- `domain/email/service/StubEmailService.java` — 실제 발송 없이 로그만 남김(`LocalStubFileStorageService`와 동급 스텁).

### `auth` 도메인 — 공통 이메일 인증코드 컴포넌트
- `entity/EmailVerification.java` (PK UUID) — `email`, `code`(6자리), `purpose`(`EmailVerificationPurpose`: `SIGNUP`/`PASSWORD_RESET`), `used`(boolean), `resetToken`(nullable, PASSWORD_RESET 검증 성공 시 발급), `expiresAt`. `BaseTimeEntity` 상속.
- `entity/EmailVerificationPurpose.java` — enum.
- `repository/EmailVerificationRepository.java` — `findTopByEmailAndPurposeOrderByCreatedAtDesc`, `findByResetTokenAndPurpose`.
- `service/EmailVerificationService.java` — `sendCode(email, purpose)`(코드 생성/저장/발송, SIGNUP이면 학교 이메일 도메인 검증), `confirmCode(email, code, purpose)`(코드 일치/만료/재사용 검증 후 `used=true`, PASSWORD_RESET이면 `resetToken` 발급까지).
- 코드 유효시간 5분(와이어프레임 `00:00` 타이머와 일치), 6자리 숫자(`SecureRandom`).

### `ErrorCode` 추가 3개
`INVALID_VERIFICATION_CODE`(400), `VERIFICATION_CODE_EXPIRED`(400), `INVALID_RESET_TOKEN`(401) — `api-errors.md` 제안 메시지 그대로.

### `StudentAuthController`/`StudentAuthService` 엔드포인트 추가
- `POST /auth/students/check-email` — 이메일 중복이면 `DUPLICATE_ACCOUNT`(기존 코드 재사용), 아니면 `data: null`.
- `POST /auth/students/email-verifications` — 가입 단계 코드 발송(`purpose=SIGNUP`).
- `POST /auth/students/email-verifications/confirm` — 코드 확인.
- `POST /auth/students/password-reset/email` — 코드 발송(`purpose=PASSWORD_RESET`), 이메일 미존재 시 `STUDENT_NOT_FOUND`.
- `POST /auth/students/password-reset/verify` — 코드 확인 + `resetToken` 발급.
- `PATCH /auth/students/password-reset` — `resetToken` + `newPassword`로 비밀번호 변경, 토큰 불일치/만료/사용됨은 `INVALID_RESET_TOKEN`.

### 마이페이지 비밀번호 변경
- `Student.updatePassword(String encodedPassword)` 추가.
- `StudentController`에 `PATCH /students/me/password` 추가, `StudentService.changePassword(studentId, code, newPassword)` — 본인 이메일 기준으로 `EmailVerificationService.confirmCode(student.getEmail(), code, PASSWORD_RESET)` 검증 후 비밀번호 인코딩/저장. 코드 발송은 기존 `POST /auth/students/password-reset/email`을 재사용(로그인 상태에서도 호출 가능, 별도 엔드포인트 안 만듦).

### 서류 인증 상태 조회
- `VerificationDocumentRepository`에 `findTopByStudentIdOrderByCreatedAtDesc(String studentId)` 추가.
- `VerificationService.getMyDocument(studentId)` — 없으면 `VERIFICATION_DOCUMENT_NOT_FOUND`.
- `VerificationController`에 `GET /students/me/verification-documents` 추가(같은 클래스, 기존 POST와 매핑 경로 공유).

### DB 마이그레이션
`src/main/resources/db/migration/V2__add_email_verification.sql` — `email_verification` 테이블 신설(V1은 비어있어 손대지 않음).

## 구현 순서

1. `ErrorCode` 3개 추가
2. `email` 도메인(인터페이스+스텁)
3. `auth` 도메인 `EmailVerification` 엔티티/repository/service
4. `StudentAuthController`/`Service` + DTO 6종
5. `Student.updatePassword` + `StudentController`/`Service` 비밀번호 변경
6. `VerificationDocumentRepository`/`Service`/`Controller` GET 추가
7. Flyway `V2` 마이그레이션
8. `./gradlew test` 통과 확인
