# 로드맵 — 피그마 디자인 기능 패리티 맞추기

피그마 53개 화면을 전수 분석해 뽑은 기능 명세와 현재 백엔드를 대조했다. 도메인 골격·ERD·컨벤션은
이미 잡혀 있으니 **기존 테이블은 건드리지 않고 ADD COLUMN만으로** 갭을 메운다.

## 원칙

1. **ERD 보존** — 기존 테이블/컬럼 삭제·타입 변경 없음. 추가만 한다. `V7`~`V9` 세 개로 나눈다.
2. **패턴 보존** — 옆 도메인 코드를 그대로 베낀다. UUID PK, `ApiResponse<T>`, `ErrorCode` +
   `BusinessException`, 커서 페이징, `@CurrentUser AuthPrincipal`.
3. **새 도메인 안 만든다** — 전부 기존 `domain/{auth,student,verification,notice,community,club,
   honeytip,notification}` 안에서 해결한다.

## 갭 목록 (피그마 → 현재 코드)

| # | 화면 | 갭 | 조치 |
|---|---|---|---|
| 1 | 로그인 | 디자인은 **아이디** 로그인, 코드는 email | `users.login_id` 추가 |
| 2 | 회원가입 1/3 | 아이디 중복 확인 버튼 | `GET /auth/students/check-login-id` |
| 3 | 로그인 | 자동 로그인 체크박스 | `auto_login` → refresh TTL 30d/1d 차등 |
| 4 | 로그인_오류 | 실패 횟수 제한 없음 | 5회 → 10분 잠금 |
| 5 | 회원가입 3/3 | 약관 동의 미저장 | `terms_agreed_at`, `privacy_agreed_at` |
| 6 | 회원가입 2/3 | 출생연도 가입 시 미수집(수정만 가능) | 가입 요청에 추가 |
| 7 | 이메일 인증 | 타이머 3분인데 코드는 5분 | 상수 3분으로 |
| 8 | 이메일 인증 | 재발송 쿨다운/시도 제한 없음 | 60초 쿨다운, 5회 제한 |
| 9 | 학생인증 | 이메일/서류 두 경로의 통합 인증 상태 없음 | `users.verification_status` |
| 10 | 서류 인증 홈 | 서류 종류 4종 구분 없음 | `document_type` |
| 11 | 서류 인증 실패 | **반려 사유 없음** | `reject_reason` |
| 12 | 학생인증 | 상태 조회가 404 (서류 미제출 시) | `/status` 엔드포인트 신설 |
| 13 | 공지사항 홈 | 중요 배너 + 최근 공지 통합 응답 없음 | `GET /notices/home` |
| 14 | 공지사항 홈 | 배너 노출 기간 없음 | `banner_start_date/end_date` |
| 15 | 새 글 작성 | **실명 토글** 동작 불가 (전면 익명) | `post.anonymous` |
| 16 | 게시글 상세 | 게시글 수정 API 없음 | `PATCH /posts/{id}` |
| 17 | 클럽 홈 | 카드 3상태(참여가능/참여중/마감) 서버 미계산 | `card_state` 응답 필드 |
| 18 | 클럽 상세 | 동시 참여 시 정원 초과 가능 | `club.version` 낙관적 락 |
| 19 | 내 클럽 | 클럽장 나가기 정책 없음 | 차단 + 위임 API |
| 20 | 정보글 상세 | 수수료/처리기간/링크/STEP/팁 구조 없음 | `honey_tip` 5컬럼 |
| 21 | 마이페이지 | **회원 탈퇴 없음** | `DELETE /students/me` |
| 22 | 알림 | 전체 읽음 없음 | `PATCH /notifications/read-all` |

## 의도적으로 안 하는 것

- **`club.deadline`은 DATE 그대로 둔다.** 목록 카드에 "10월 24일, 18:00"이 찍혀 있지만 정작
  클럽 만들기 화면의 DatePicker는 `mm/dd/yyyy`로 날짜만 받는다. 입력이 날짜뿐인데 저장을
  타임스탬프로 바꾸면 없는 정보를 만들어내는 꼴이라, 마감 판정을 **해당 일자 23:59:59**로
  정의하고 컬럼은 보존한다. 시각 표기는 클라이언트 몫.
- **채팅 번역** — 게시판은 번역하는데 클럽 채팅 번역 여부가 디자인에 없다. 정책 미확정이라 보류.
- **관리자 콘솔 화면** — 디자인 0장. 서버 API는 이미 `/admin/**`에 있으므로 이번 범위 밖.

## 마이그레이션 계획

전부 `ADD COLUMN`이라 H2(PostgreSQL 모드)에서도 그대로 돌아간다.

- `V7__add_student_account_fields.sql` — users 8컬럼 + email_verification 시도 횟수
- `V8__extend_verification_document.sql` — document_type, reject_reason
- `V9__extend_content_fields.sql` — post.anonymous / honey_tip 5 / notice 2 / club.version

`login_id`는 NOT NULL + UNIQUE라 기존 행 백필이 필요하다. 이메일 로컬파트로 채우고, 그래도
겹치면 UUID로 대체한다(해당 계정은 아이디 재설정 안내 필요).

## 작업 순서

1. 마이그레이션 V7~V9
2. auth/student — 아이디 로그인, 약관, 자동 로그인, 잠금, 탈퇴
3. verification — 서류 종류/반려 사유/통합 상태
4. notice/community — 홈 배너, 게시글 수정, 익명 토글
5. club/honeytip/notification — 카드 상태·낙관적 락·위임, 정보글 구조화, 전체 읽음
6. `./gradlew build` + Flyway 검증 → 보고서

## 건드릴 파일

`ErrorCode`, `Student`, `StudentRepository`, `StudentAuthService/Controller`, `StudentService/Controller`,
`JwtTokenProvider`, `EmailVerification(Service)`, `VerificationDocument/Service/Controller`,
`Notice/NoticeQueryService/StudentNoticeController`, `Post/PostService/PostController`,
`Club/ClubService/ClubController/ClubResponse`, `HoneyTip/HoneyTipService/HoneyTipResponse`,
`NotificationRepository/Service/Controller` + 각 DTO.
