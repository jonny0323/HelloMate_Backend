# 보고서 — 피그마 디자인 기능 패리티 맞추기

로드맵: `docs/roadmaps/2026-08-04-figma-feature-parity.md`

## 한 일

피그마 53개 화면과 현재 백엔드를 대조해 뽑은 갭 22건을 전부 메웠다. 기존 테이블은 하나도 삭제·변경하지
않고 **`ADD COLUMN`만으로** 스키마를 확장했다(V7~V9, 총 21컬럼).

### 1. 마이그레이션 (V7~V9)

| 파일 | 대상 | 컬럼 |
|---|---|---|
| `V7__add_student_account_fields.sql` | `users` | `login_id`, `terms_agreed_at`, `privacy_agreed_at`, `login_fail_count`, `locked_until`, `status`, `withdrawn_at`, `verification_status`, `verified_at` |
| | `email_verification` | `attempt_count` |
| `V8__extend_verification_document.sql` | `verification_document` | `document_type`, `reject_reason` |
| `V9__extend_content_fields.sql` | `post` | `anonymous` |
| | `honey_tip` | `tip_message`, `steps_json`, `estimated_fee`, `processing_period`, `external_link` |
| | `notice` | `banner_start_date`, `banner_end_date` |

`login_id`는 NOT NULL + UNIQUE라 기존 행을 백필한다. 이메일 로컬파트를 쓰고, 학교가 달라 로컬파트가
겹치는 계정은 UUID로 대체한다 — **해당 계정은 아이디 재설정 안내가 필요하다.**

전부 `ADD COLUMN` / `UPDATE` / `SET NOT NULL`이라 Postgres·H2(PostgreSQL 모드) 양쪽에서 동작한다.

### 2. 인증 — 아이디 로그인 · 약관 · 자동 로그인 · 잠금

- **로그인 식별자를 `loginId`로 전환.** 디자인의 로그인 화면은 '아이디' 필드에 중복 확인 버튼까지
  있어서 이메일 로그인으로는 재현이 안 됐다. `email`은 학교 이메일 인증/비밀번호 찾기용으로 남긴다.
- `POST /auth/students/check-login-id` 신설. 기존 `check-email`도 `AvailabilityResponse`(사용 가능
  여부 + 문구)를 반환하도록 통일했다.
- 로그인 요청에 `autoLogin` 추가 → 리프레시 토큰 수명 **30일 / 1일** 차등. `application.yaml`에
  `refresh-token-validity-auto-login-ms` 추가.
- **로그인 5회 실패 시 10분 잠금**(`ACCOUNT_LOCKED`). 실패 판정은 아이디/비밀번호를 구분하지 않는다 —
  구분해 알려주면 가입된 아이디를 긁어낼 수 있다.
- 회원가입에 `termsAgreed`/`privacyAgreed`(둘 다 `@AssertTrue`)와 `birthYear` 추가.
  `loginId`·`password`는 정규식으로 형식 검증.
- 비밀번호 재설정/변경 시 리프레시 토큰 전량 폐기. 계정을 탈취당한 상태에서 되찾는 경로라 기존 세션을
  살려두면 안 된다.

### 3. 이메일 인증 강화

- 코드 유효시간 **5분 → 3분** (디자인 타이머가 03:00에서 시작).
- **재발송 60초 쿨다운**, **확인 시도 5회 제한**(`attempt_count`). 6자리는 무제한 재시도를 허용하면
  뚫린다.

### 4. 학생 인증 — 통합 상태 · 서류 종류 · 반려 사유

- `users.verification_status`로 **이메일 경로와 서류 경로를 하나의 상태로 합쳤다**
  (`REGISTERED → DOC_PENDING → DOC_REJECTED → VERIFIED`). 마이페이지 '학생 인증됨' 뱃지와 학생 인증
  화면 분기가 이 값 하나만 본다.
- 이메일 인증 성공 시 해당 계정을 곧바로 `VERIFIED`로 올린다.
- `DocumentType` 4종(학생증/재학증명서/입학허가서/성적증명서) 추가.
- **반려 사유(`reject_reason`) 필수화.** 반려 API는 사유 없이 호출하면 `REJECT_REASON_REQUIRED`로
  막는다. 디자인의 '서류 인증 실패' 화면에는 사유 표시 영역이 없는데, 사유를 안 주면 학생이 같은
  서류를 그대로 다시 올리는 루프에 빠진다 — **프론트에 표시 영역 추가 요청 필요.**
- `GET /students/me/verification-documents/status` 신설. 서류를 한 번도 안 낸 학생도 화면 분기를 해야
  해서 404 대신 `latestDocument: null`로 200을 준다.
- 승인/반려 시 학생에게 `SYSTEM` 알림 발행.

### 5. 마이페이지

- `DELETE /students/me` **회원 탈퇴** 신설. `post`/`post_comment`가 `users`를 FK로 물고 있어 행을
  지우면 커뮤니티 이력이 깨지므로 `status = WITHDRAWN` 전환 + 리프레시 토큰 폐기로 처리했다.
  개인정보 파기는 별도 배치의 몫으로 남긴다.
- 탈퇴 계정은 로그인 시 `ACCOUNT_WITHDRAWN`.

### 6. 공지사항

- `GET /notices/home` 신설 — 중요 배너 캐러셀 + 최근 공지 5건을 한 번에. 따로 부르면 왕복이 두 번이다.
- 배너는 `notice.type = URGENT` + `banner_start_date`/`banner_end_date` 기간이 유효한 것만.
  기간을 비우면 제한 없이 계속 노출된다.

### 7. 커뮤니티

- **익명/실명 토글**(`post.anonymous`). 지금까지 전면 익명이라 작성 화면의 '실명' 뱃지가 동작할 수
  없었다. 요청에서 생략하면 익명으로 본다 — 실수로 실명이 노출되는 쪽이 더 위험하다.
- 응답 필드 `anonName` → **`authorName` + `anonymous`**로 변경. 실명 글이면 작성자 이름이 들어간다.
  상세 응답에 `mine`(내 글 여부)도 추가 — 수정/삭제 버튼 노출 판단용.
- `PATCH /posts/{postId}` **게시글 수정** 신설(작성자 본인만). 수정 시 본문 언어를 다시 감지한다.
- **대댓글 1 depth 제한**(`REPLY_DEPTH_EXCEEDED`). 디자인의 ↳ 인디케이터가 한 단계뿐이라 그 이상
  중첩되면 화면이 표현할 수 없다.
- 댓글은 디자인상 항상 익명이라 그대로 뒀다.

### 8. 클럽

- **카드 3상태를 서버가 계산**해서 `cardState`(`joinable`/`joined`/`closed`)로 내려준다.
  클라이언트마다 판정이 갈리는 걸 막는다. `remainingSeats`("7자리 남았어요")도 함께.
- **정원 초과 동시성 해결.** 참여 시 `findByIdForUpdate`(`PESSIMISTIC_WRITE`)로 클럽 행을 잠그고 읽어
  직렬화한다. 정원/마감 검증은 `Club.join()` 안으로 옮겨 어느 경로로 참여하든 같은 규칙을 타게 했다.
- **클럽장 나가기 차단**(`CLUB_OWNER_CANNOT_LEAVE`) + `PATCH /clubs/{clubId}/owner` **위임** 신설.
  클럽장이 그냥 나가면 클럽이 주인 없이 남는다.
- 마감일이 지나면 정원이 남아도 `CLUB_RECRUIT_CLOSED`.
- 멤버 목록 조회 권한을 **클럽장 → 참여 멤버**로 완화했다. 단톡방 헤더의 '멤버 24명'에서 들어가는
  화면이라 클럽장만 볼 수 있으면 안 된다.
- 목록의 '참여 중' 판정을 `findJoinedClubIds` 한 방으로 바꿔 N+1을 없앴다.

### 9. 생활정보

- 정보글 상세의 구조를 컬럼으로 받았다: `tipMessage`(ℹ️ 팁 박스), `steps`(번호 STEP 리스트),
  `estimatedFee`, `processingPeriod`, `externalLink`.
- STEP은 배열이라 별도 테이블 대신 `steps_json`(TEXT)에 담는다 — 관리자가 통으로 저장/수정하고
  STEP 단위로 질의할 일이 없다. 변환은 `HoneyTipStepCodec`이 전담하고, 파싱이 깨져도 본문은 보여야
  하므로 예외 대신 빈 목록으로 떨어뜨린다.
- 응답에 **면책 문구(`disclaimer`)** 상시 포함. 비자·보험처럼 틀리면 체류자격까지 걸리는 정보를
  다룬다. 디자인에는 없지만 클라이언트가 노출해야 한다.

### 10. 알림

- `PATCH /notifications/read-all` 신설. 알림이 쌓인 계정에서 엔티티를 전부 로드하면 수백 건을 메모리에
  올리게 되므로 벌크 업데이트(`@Modifying(clearAutomatically = true)`)로 처리했다.

## 로드맵 대비 달라진 점

**낙관적 락(`@Version`) → 비관적 락으로 변경.** 로드맵에는 `club.version` 컬럼을 추가한다고 적었는데,
낙관적 락은 충돌 시 재시도가 필요하고 그러려면 `spring-retry` 의존성이 새로 들어간다. 클럽 참여는
빈도가 낮아 잠금 비용이 문제되지 않으므로 `SELECT ... FOR UPDATE` 한 줄로 끝내는 쪽이 낫다고 판단해
**V9에서 `version` 컬럼을 뺐고 의존성 추가도 없다.**

그 외 로드맵과 동일하다.

## 의도적으로 안 한 것

- **`club.deadline`은 DATE 그대로.** 목록 카드에 "10월 24일, 18:00"이 찍혀 있지만 클럽 만들기 화면의
  DatePicker는 `mm/dd/yyyy`로 날짜만 받는다. 입력이 날짜뿐인데 저장을 타임스탬프로 바꾸면 없는 정보를
  만들어내는 꼴이라, 마감 판정을 **해당 일자까지 유효**(다음 날 00:00부터 마감)로 정의하고 컬럼을
  보존했다. 시각 표기는 클라이언트 몫.
- **학생 유형 × 학년 교차 검증.** ERD의 `StudentType`(교환학생/정규과정생/어학당/한국인)은 피그마의
  학부·대학원 구분과 축이 달라 검증 규칙을 확정할 수 없다. 근거 없는 정책은 넣지 않았다.
- **클럽 채팅 번역.** 게시판은 번역하는데 클럽 채팅 번역 여부가 디자인에 없다. 정책 미확정.
- **관리자 콘솔 화면.** 디자인 0장. 서버 API는 이미 `/admin/**`에 있다.

## 신규/변경 엔드포인트

| Method | Path | 비고 |
|---|---|---|
| POST | `/auth/students/check-login-id` | 신규 |
| POST | `/auth/students/login` | **요청 변경** — `email` → `loginId`, `autoLogin` 추가 |
| POST | `/auth/students/signup` | **요청 변경** — `loginId`, `birthYear`, 약관 2종 추가 |
| POST | `/auth/students/check-email` | 응답 변경 — `AvailabilityResponse` |
| GET | `/students/me/verification-documents/status` | 신규 |
| POST | `/students/me/verification-documents` | **요청 변경** — `documentType` 추가 |
| PATCH | `/admin/verification-documents/{id}` | **요청 변경** — `rejectReason` 추가(반려 시 필수) |
| DELETE | `/students/me` | 신규 (회원 탈퇴) |
| GET | `/notices/home` | 신규 |
| POST | `/admin/notices` | 요청 변경 — 배너 기간 2필드 추가 |
| PATCH | `/posts/{postId}` | 신규 (게시글 수정) |
| POST | `/posts` | 요청 변경 — `anonymous` 추가 |
| GET | `/posts`, `/posts/{id}`, `/posts/mine` | **응답 변경** — `anonName` → `authorName`+`anonymous`(+상세 `mine`) |
| PATCH | `/clubs/{clubId}/owner` | 신규 (클럽장 위임) |
| GET | `/clubs`, `/clubs/{id}`, `/clubs/mine` | 응답 변경 — `cardState`, `remainingSeats` 추가 |
| GET/POST/PATCH | `/honey-tips/**`, `/admin/honey-tips/**` | 요청·응답 변경 — 팁/STEP/수수료/기간/링크/면책 |
| PATCH | `/notifications/read-all` | 신규 |

`ErrorCode` 신규 10종: `ACCOUNT_LOCKED`, `ACCOUNT_WITHDRAWN`, `VERIFICATION_ATTEMPT_EXCEEDED`,
`VERIFICATION_RESEND_TOO_SOON`, `STUDENT_NOT_VERIFIED`, `DUPLICATE_LOGIN_ID`, `REPLY_DEPTH_EXCEEDED`,
`CLUB_RECRUIT_CLOSED`, `CLUB_OWNER_CANNOT_LEAVE`, `REJECT_REASON_REQUIRED`.

## 검증

작업 환경에 JDK 21과 Maven 중앙 저장소 접근이 없어 **`./gradlew build`를 직접 돌리지 못했다.**
대신 스크립트로 정적 검증했다:

- Java 230개 파일 / 타입 231개 — **미해결 내부 import 0건, import 누락 0건**
- 시그니처를 바꾼 생성자·레코드의 호출부 인자 개수 전수 대조 — `Student`(12) / `Post`(7) /
  `VerificationDocument`(4) / `PostDetailResponse`(12) / `CreatePostResponse`(4) /
  `PostCommentResponse`(9) / `VerificationStatusResponse`(3) **전부 일치**
- 제거된 API(`ClubResponse.from`, `HoneyTipResponse.from`, `document.review`)의 잔존 호출 **0건**
- 마이그레이션 21컬럼 ↔ 엔티티 필드 매핑 **21/21 일치** (`ddl-auto: validate` 통과 조건)

**커밋 전에 로컬에서 반드시 확인할 것:**

```bash
./gradlew build                 # 컴파일 + 테스트
docker compose up -d            # Postgres
./gradlew bootRun               # Flyway V7~V9 적용 + Hibernate validate 통과 확인
```

테스트는 H2 `create-drop`이라 마이그레이션을 타지 않는다. **V7~V9는 Postgres에 붙여서만 검증된다.**

### 줄바꿈(CRLF) 관련 주의

리눅스 git으로 확인하면 `git diff`가 191개 파일을 변경으로 잡는데, **실제 내용이 바뀐 건 50개뿐**이다.
나머지 141개는 작업 트리가 CRLF이고 HEAD 블롭이 LF라서 생기는 차이로, Windows git(`core.autocrlf=true`)
에서는 정규화되어 보이지 않는다. 실제 변경분만 보려면:

```bash
for f in $(git diff --name-only); do
  diff -q <(git show HEAD:"$f" | tr -d '\r') <(tr -d '\r' < "$f") >/dev/null || echo "$f"
done
```

## 추가한 테스트

`@SpringBootTest` 없이 도는 순수 도메인 단위 테스트 2개(11 케이스):

- `ClubTest` — 정원/마감 경계값, 마감일 당일 처리, 카드 상태 3종 판정
- `StudentTest` — 인증 상태 머신, 로그인 잠금, 비밀번호 변경 시 잠금 해제, 탈퇴

## 남은 이슈 / 다음 작업 후보

1. **프론트에 요청 필요** — 서류 반려 사유 표시 영역, 만료된 인증번호에서 [다음] 버튼 비활성,
   전공 검색 결과 없음일 때 [선택 완료] 비활성.
2. **`login_id` 백필로 UUID가 들어간 계정 처리** — 운영 데이터가 있다면 아이디 재설정 안내 필요.
3. **정책 확정 대기** — 클럽 채팅 번역 여부, 미인증(`DOC_PENDING`) 학생의 서비스 이용 범위.
   지금은 인증 여부와 무관하게 열려 있다. 막으려면 `SecurityConfig`에 `VERIFIED` 권한 분기를
   추가해야 한다(`STUDENT_NOT_VERIFIED` 에러코드는 미리 만들어 뒀다).
4. **개인정보 파기 배치** — 탈퇴 계정의 이름/이메일 마스킹.
5. `docs/resources/api.md`(화면별 구현 상태)는 이번에 갱신하지 않았다. `api-reference.md`만 갱신함.

## 커밋 메시지 제안

작업 단위로 5개로 쪼개는 걸 권한다(마이그레이션은 기능 커밋과 같이 가야 `validate`가 깨지지 않는다):

```
✨ Feature : 아이디 로그인 전환 및 약관 동의·자동 로그인·계정 잠금 추가
✨ Feature : 학생 인증 상태 통합 및 서류 종류·반려 사유 추가
✨ Feature : 회원 탈퇴, 공지 홈, 알림 전체 읽음 API 추가
✨ Feature : 커뮤니티 익명/실명 토글 및 게시글 수정 API 추가
✨ Feature : 클럽 카드 상태·정원 동시성·클럽장 위임, 생활정보 구조화 필드 추가
```
