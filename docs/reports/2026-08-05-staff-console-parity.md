# 보고서 — 담당자 웹(직원 관리자 콘솔) 기능 패리티

로드맵: `docs/roadmaps/2026-08-05-staff-console-parity.md`

## 한 일

담당자 웹 프로토타입을 기능명세로 정리하며 뽑은 결함 22건을 전부 메웠다. 기존 테이블은 하나도
삭제·변경하지 않고 **`ADD COLUMN`만으로** 확장했다(V10~V11, 총 9컬럼).

### 1. 마이그레이션 (V10~V11)

| 파일 | 대상 | 컬럼 |
|---|---|---|
| `V10__add_notice_lifecycle.sql` | `notice` | `status`, `sent_at`, `deleted_at`, `audience_mode`, `audience_label`, `resend_count`, `last_resent_at` |
| `V11__add_chat_lang_and_thread_origin.sql` | `chat_message` | `original_lang` |
| | `chat_thread` | `initiated_by` |

기존 행은 전부 `status='SENT'`, `sent_at=created_at`, `audience_label='전체 학생'`로 백필한다.

### 2. 보안 — 먼저 막은 것들

**미승인 담당자가 관리자 전 기능을 쓸 수 있었다.** `StaffAuthService.login`에 `verified` 검사가 없었고,
`STAFF_NOT_VERIFIED` 에러코드는 선언만 되어 있었으며, `verified`를 `true`로 바꾸는 코드가 프로젝트에
아예 없었다. 로그인에 검사를 넣고, **초대 코드 사용 시점을 승인으로 확정**했다 — 부서 관리자가
발급한 1회용 코드를 가진 사람만 가입할 수 있으므로 별도 승인 화면을 두면 코드 발급이 무의미해진다.

**공지 상세/수신자/재발송/삭제 4개 엔드포인트가 `@CurrentUser`를 받지도 않았다.** `hasRole(STAFF)` 하나만
통과하면 다른 **대학** 담당자도 noticeId만 알면 남의 공지를 지우고 수신자 명단(이름·이메일)을
조회할 수 있었다. 권한을 두 단계로 나눴다.

- **조회**: 같은 대학이면 허용 — 다른 부서 공지도 참고할 수 있어야 한다
- **변경**(수정·재발송·삭제): **같은 부서만**

**부서 사칭**도 막았다. `CreateNoticeRequest.department`를 제거하고 `staff.getDepartment()`를 쓴다.
장학금·비자 같은 민감 공지에서 발신 부서를 자유롭게 고를 수 있는 건 그 자체로 사고다.

### 3. 공지 수신 대상 — 축을 분리했다

기존 `resolveAudience`의 GROUP 모드는 `upper(country) IN (...)`만 봤다. 화면 그룹 키 중
`cs`(컴퓨터공학과), `bz`(경영학과)는 국가 코드가 아니라 **조회 결과 0명인데 201 CREATED로 성공**했다.
담당자는 63명에게 보냈다고 믿고 끝낸다.

- `AudienceRequest`를 `countryCodes` / `majors`로 **축 분리**. 규칙은 **축 안에서 OR, 축 사이에서 AND**
- **수신자 0명이면 `NOTICE_AUDIENCE_EMPTY`(400)로 거절**
- GROUP인데 아무 조건도 없으면 거절 — 실수로 전체 312명에게 나가는 걸 막는다
- 해석 로직을 `NoticeAudienceResolver`로 분리. 발송 전 인원 미리보기와 실제 발송이 **같은 규칙**을
  타야 "312명에게 갑니다"와 실제 수신자가 어긋나지 않는다
- `POST /admin/notices/audience/count` 신설 — 화면이 그룹 인원을 합산하면 국가·학과가 겹치는 학생이
  이중 계산된다. 서버가 계산한 수를 그대로 쓰게 한다
- 수신 대상에서 **탈퇴(WITHDRAWN) 학생 제외** (V7에서 추가한 `users.status` 활용)

### 4. 재발송 — 동작 자체가 틀려 있었다

기존 `resend()`는 `NoticeReception::resetRead`만 호출했다. 실제로 일어나던 일:

1. 이미 읽은 214명이 안 읽은 것으로 되돌아감 → 열람률 68% → 0%
2. `readAt`(언제 읽었는지) 영구 소실
3. 알림은 나가지 않음 → **학생은 아무것도 못 받음**

화면은 "재발송 되었습니다 📨" 토스트를 띄우고 있었다.

→ **미열람자에게 알림을 다시 보내고 `read`는 건드리지 않는다.** 24시간 쿨다운(`recordResend`)과
횟수 기록을 넣었고, 응답으로 몇 명에게 갔는지 돌려준다.

### 5. 삭제 — 소프트 삭제로 전환

`chat_thread.notice_id`가 `notice`를 FK로 참조하는데 `ON DELETE` 절이 없다. 그런데 기존 `delete()`는
`chat_thread`를 전혀 정리하지 않았다 → **문의를 받은 공지는 삭제 시 500**. 정작 지우고 싶은 건
문의가 많이 온 공지다. 게다가 `notice_reception`을 물리 삭제해 열람 이력도 통째로 사라졌다.

→ `deleted_at`만 찍는다. 학생 대상 조회 쿼리 전부에 `r.notice.deletedAt is null`을 걸어 학생 앱에서는
즉시 사라지되 이력과 문의 스레드는 남는다. 첨부파일 고아 문제(BE-19)도 같이 해소된다.

### 6. 발송 성능 — 쿼리 936회 → 배치

기존은 수신자마다 `reception.save()` + `notify()`(설정 조회 1 + insert 1)를 돌아 312명에 약 936회였다.

→ `NotificationService.notifyAll()`을 추가해 **설정을 한 번에 조회**(`findByStudentIdInAndCategory`)하고
`saveAll`로 묶는다. `NoticeReception`도 `saveAll`.

### 7. 임시저장 · 수정

- `notice.status = DRAFT`로 초안을 저장한다. 제목/내용이 비어 있을 수 있어 빈 문자열로 저장한다
  (컬럼 NOT NULL 제약을 풀지 않기 위함)
- `POST/PATCH /admin/notices/drafts`, `GET /admin/notices/drafts`(작성자 본인만),
  `POST /admin/notices/drafts/{id}/send`
- `PATCH /admin/notices/{id}` — 발송 후 오탈자 수정. 수신자 목록은 바뀌지 않는다

### 8. 대시보드 통계 4종

`AdminDashboardResponse`에 `stats`를 추가했다.

| 지표 | 정의 |
|---|---|
| `sentNoticeCount` | 이번 학기 발송 건수 (삭제·초안 제외) |
| `averageReadRate` | **가중 평균** `Σ열람 / Σ수신자`. 단순 평균은 수신자 3명짜리 공지가 312명짜리와 같은 무게를 가져 왜곡된다 |
| `activeStudentCount` | 탈퇴하지 않은 등록 유학생 수 |
| `pendingReplyCount` | **스레드 기준** — 화면의 사이드바 뱃지와 통계 카드가 서로 다른 정의를 쓰던 문제(DEF-T01) 해소 |

"이번 학기" 경계는 `SemesterCalculator`가 설정값(`hellomate.academic.*-semester-start-month`, 기본 3월/9월)으로
계산한다. 학사일정 테이블을 만들 근거가 부족해서 설정으로 뺐고, 나중에 학사일정 도메인이 생기면
이 클래스만 갈아끼우면 된다.

또 `recentNotices` 개수(4건)를 서버 상수로 일원화했다. 기존엔 서버 `findTop4`와 화면 `slice(0,4)`가
우연히 맞아떨어지고 있었다.

### 9. 개별 메시지

- **담당자 선발신 DM 허용.** 기존 `POST /chats/threads`는 `principal.id()`를 studentId로 쓰기 때문에
  담당자가 부르면 `STUDENT_NOT_FOUND`로 터졌다. `POST /admin/chats/threads`를 신설했고,
  학생의 `CHAT_DIRECT` 알림 설정을 존중하며 `chat_thread.initiated_by`에 개설 주체를 남긴다
- **채팅 번역.** `chat_message.original_lang`을 기록하고 `Accept-Language`를 보낸 쪽에 번역본을 함께
  내린다(`TranslationContentType.CHAT_MESSAGE`). 학생은 모국어로 질문하는데 담당자는 한국어만
  읽는다는 게 이 서비스에서 가장 이상한 지점이었다. 번역 실패는 예외로 올리지 않는다 — 원문이
  이미 있으므로 대화가 끊기면 안 된다

### 10. 나머지

- 수신자별 열람 현황 페이징(`GET /admin/notices/{id}/receptions?page&size`), 응답에 국가·열람 시각 추가
- 첨부 검증 — 최대 5개, PDF/DOCX/DOC/JPEG/PNG/HEIC만 허용
- 발송함 응답에 `canManage` — 다른 부서 공지는 읽기 전용이라는 걸 서버가 계산해서 알려준다
- `GET /admin/honey-tips/edit-requests` — 정보글별 조회만 있어서 어느 글에 수정 요청이 왔는지 알 방법이
  없었다. 학교 전체 대기 건을 한 화면에 모은다
- `PATCH /admin/staff/me` — 담당자 프로필 수정(부서는 초대 코드가 정하므로 변경 불가)

## 로드맵 대비 달라진 점

없다. 로드맵에 적은 정책 8건을 그대로 구현했다.

한 가지 부수 작업이 있었다: `AudienceMode`를 `dto/request` → `entity` 패키지로 **옮겼다.**
`Notice` 엔티티가 발송 대상 모드를 스냅샷으로 들고 있어야 하는데, 엔티티가 DTO 패키지를 import하면
레이어가 역전된다. 새 enum을 만들어 중복시키는 것보다 이동이 낫다고 판단했다.

## 의도적으로 안 한 것

- **화면 자체**(로그인 / 서류 심사 / 신고 처리 / 생활정보 관리) — 프로토타입에 없는 화면이고
  서버 API는 이미 있다. **서류 심사 화면이 없으면 유학생이 인증 대기에서 못 벗어난다** — 프론트에서
  가장 먼저 만들어야 할 화면이다
- **예약 발송** — 프로토타입에 UI가 없다
- **비동기 발송 + 진행률** — 배치로 쿼리를 크게 줄였으니 현재 규모(312명)에서는 동기로 충분하다.
  유학생이 수천 명 되면 `notice.status`에 `SENDING`을 추가하고 폴링으로 바꾸면 된다
- **학사일정 테이블** — 학기 경계 하나 때문에 테이블을 만들 근거가 부족하다

## 신규/변경 엔드포인트

| Method | Path | 비고 |
|---|---|---|
| POST | `/auth/staff/login` | **동작 변경** — 미승인 계정은 `STAFF_NOT_VERIFIED`(403) |
| POST | `/auth/staff/signup` | 동작 변경 — 초대 코드 사용 시 자동 승인 |
| GET | `/admin/dashboard` | **응답 변경** — `stats` 추가, 공지/스레드 항목 필드 확장 |
| POST | `/admin/notices` | **요청 변경** — `department` 제거, `audience` 구조 변경 |
| POST | `/admin/notices/audience/count` | 신규 (발송 전 수신자 수) |
| GET | `/admin/notices` | 응답 변경 — `audienceLabel`, `resendCount`, `canManage`, `sentAt` |
| GET | `/admin/notices/drafts` | 신규 |
| POST | `/admin/notices/drafts` | 신규 (임시저장) |
| PATCH | `/admin/notices/drafts/{id}` | 신규 |
| POST | `/admin/notices/drafts/{id}/send` | 신규 |
| GET | `/admin/notices/{id}` | **권한 추가** + 응답 확장 |
| PATCH | `/admin/notices/{id}` | 신규 (발송 후 수정) |
| GET | `/admin/notices/{id}/receptions` | **권한 추가 + 페이징** |
| POST | `/admin/notices/{id}/resend` | **동작 전면 변경** — 미열람자 알림, 24h 쿨다운 |
| DELETE | `/admin/notices/{id}` | **권한 추가 + 소프트 삭제** |
| POST | `/admin/chats/threads` | 신규 (담당자 선발신) |
| GET | `/chats/threads/{id}/messages` | 응답 변경 — `originalLang`, `translated` 추가 |
| GET | `/chats/threads` | 응답 변경 — `initiatedBy` 추가 |
| GET | `/admin/honey-tips/edit-requests` | 신규 (수정 요청 대기 목록) |
| PATCH | `/admin/staff/me` | 신규 (프로필 수정) |

`ErrorCode` 신규 9종: `NOT_MY_UNIVERSITY`, `NOT_MY_DEPARTMENT`, `NOTICE_AUDIENCE_EMPTY`,
`NOTICE_ALREADY_SENT`, `NOTICE_NOT_SENT`, `NOTICE_RESEND_TOO_SOON`, `NOTICE_ATTACHMENT_LIMIT`,
`NOTICE_ATTACHMENT_TYPE` (+ 기존 `STAFF_NOT_VERIFIED`를 실제로 사용).

## 검증

작업 환경에 JDK 21과 Maven 중앙 저장소 접근이 없어 **`./gradlew build`를 직접 돌리지 못했다.**
스크립트로 정적 검증했다:

- Java 243개 파일 / 타입 244개 — **미해결 내부 import 0건, import 누락 0건**
- 이번에 건드린 14개 파일 **미사용 import 0건**
- 시그니처 변경분 호출부 대조 — `AdminDashboardResponse`(3) / `DashboardStatsResponse`(4) /
  `UnansweredThreadItem`(5) / `ChatThreadResponse`(8) / `CreateNoticeResponse`(3) /
  `AudienceCountResponse`(2) / `ChatMessage`(5) / `ChatThread`(5) **전부 일치**
- 제거된 API(`findTop4...`, `new Notice(...)` 외부 호출, 구 `toResponseList`)의 잔존 호출 **0건**
- 마이그레이션 9컬럼 ↔ 엔티티 필드 매핑 **9/9 일치** (`ddl-auto: validate` 통과 조건)

**커밋 전에 로컬에서 반드시 확인할 것:**

```bash
./gradlew build
docker compose up -d
./gradlew bootRun     # Flyway V10~V11 적용 + Hibernate validate 통과 확인
```

테스트는 H2 `create-drop`이라 마이그레이션을 타지 않는다. **V10~V11은 Postgres에 붙여서만 검증된다.**

리눅스 git으로 보면 변경 파일 수가 부풀어 보이는데, 작업 트리 CRLF와 HEAD의 LF 차이 때문이다.
실제 변경분만 보려면 `2026-08-05-figma-feature-parity.md`의 CRLF 항목에 적어둔 명령을 쓸 것.

## 추가한 테스트

`@SpringBootTest` 없이 도는 단위 테스트 2개(11 케이스):

- `NoticeTest` — 부서 출처, 초안 허용 범위, 발송 스냅샷, 중복 발송 차단, 재발송 쿨다운,
  소프트 삭제, 부서별 관리 권한
- `SemesterCalculatorTest` — 1·2학기 경계와 1~2월(직전 해 2학기) 처리

## 남은 이슈 / 다음 작업 후보

1. **프론트가 먼저 만들어야 할 화면** — 로그인, **서류 인증 심사**(없으면 유학생이 앱을 못 씀),
   신고 처리, 생활정보 관리.
2. **프론트 저장형 XSS** — 프로토타입이 모든 렌더링을 `innerHTML` 문자열 연결로 하고 이스케이프를
   하지 않는다. 학생이 채팅으로 스크립트를 보내면 담당자 브라우저에서 실행되고, 그 토큰은
   유학생 전원의 개인정보 접근 권한이다. 실서비스는 자동 이스케이프 프레임워크로 갈 것.
3. **발송/삭제 확인 다이얼로그** — 312명에게 나가는 비가역 액션에 확인이 없다. 삭제·재발송 후
   목록 갱신도 없어 중복 실행을 유발한다(DEF-T06, DEF-T11).
4. **미리보기 번역** — 서버는 `POST /translations/official`이 이미 있다. 화면이 호출만 하면 된다.
5. **초대 코드 발급 API** — 지금은 DB에 직접 넣어야 한다. 담당자 온보딩을 하려면 필요하다.
6. `docs/resources/api-reference.md`는 학생 앱 기준 문서라 이번 관리자 API 변경은 반영하지 않았다.

## 커밋 메시지 제안

```
🪲 Fix : 미승인 담당자 로그인 차단 및 공지 소유자 검증 추가
🪲 Fix : 재발송이 열람률을 초기화하던 문제 수정
✨ Feature : 공지 수신 대상 국가·학과 축 분리 및 수신자 수 미리보기 추가
✨ Feature : 공지 임시저장·수정·소프트 삭제 지원
✨ Feature : 관리자 대시보드 통계 및 담당자 선발신 메시지 추가
✨ Feature : 1:1 문의 채팅 번역 지원
```
