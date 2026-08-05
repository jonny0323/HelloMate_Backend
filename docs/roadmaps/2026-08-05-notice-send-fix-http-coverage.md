# 2026-08-05 — 공지 즉시발송 버그 수정 + http-client 커버리지 보강

## 배경

`./gradlew test` 실행 결과 23개 중 5개 실패. 전부 `NoticeTest`이고, 실패 원인이 전부 동일하다 —
테스트 헬퍼 `sentNotice()`가 `Notice.sent()`로 만든(이미 `status = SENT`인) 공지에 다시
`markSent()`를 호출해서 `NOTICE_ALREADY_SENT`가 터진다.

그런데 이 패턴이 프로덕션 코드에도 똑같이 있다:

```java
// NoticeService.createAndSend
Notice notice = Notice.sent(...);   // status = SENT
...
dispatch(notice, author, request.audience());  // 내부에서 notice.markSent(...) → 409
```

`markSent()`는 `status == SENT`면 무조건 예외를 던지므로 **`POST /admin/notices`(공지 즉시 발송)는
항상 409 NOTICE_ALREADY_SENT로 실패한다.** 담당자 웹의 핵심 기능이 죽어 있는 상태다.
`http-client/09-notice.http`, `12-search.http`의 공지 작성 요청도 같은 이유로 전부 실패한다.

원인은 `Notice.sent()` 팩토리 자체다. 이름과 달리 `sentAt`/수신자 수/대상 스냅샷을 채우지 않으면서
상태만 `SENT`로 먼저 박아버려서, 뒤따르는 `markSent()`의 정상 흐름을 막는다. 초안이든 즉시발송이든
"발송 확정"은 `markSent()` 한 곳에서만 일어나야 한다.

## 할 일

### 1. 프로덕션 버그 수정

- `Notice.sent()` 팩토리 제거. 발송 확정 경로를 `markSent()` 하나로 모은다.
- `NoticeService.createAndSend()` — `Notice.draft(...)`로 만들고 `dispatch()`가 `markSent()`로
  확정하게 한다. 즉시발송과 초안발송(`sendDraft`)이 같은 경로를 타게 되어 흐름이 하나로 정리된다.
- `Notice.draft()`의 title/content null 방어는 그대로 둔다 (즉시발송은 `@NotBlank`라 영향 없음).

### 2. 테스트 수정

- `NoticeTest.sentNotice()` 헬퍼 — `Notice.draft(...)` + `markSent(...)`로 교체.
- `departmentComesFromAuthor` 도 `Notice.draft()` 사용으로 변경.

### 3. http-client 미커버 엔드포인트 10개 추가

경로/메서드/성공 status/`ErrorCode`/enum 표기는 전수 대조 결과 전부 일치했고(104개 중 94개 커버),
아래 10개만 `.http`가 건드리지 않는다. 각 파일의 기존 패턴(`runId` 유니크, 토큰 캡처, 에러 케이스
최소 1개)을 그대로 따라 붙인다.

| 파일 | 추가할 엔드포인트 |
| --- | --- |
| `09-notice.http` | `POST /admin/notices/audience/count`, `POST /admin/notices/drafts`, `GET /admin/notices/drafts`, `PATCH /admin/notices/drafts/{id}`, `POST /admin/notices/drafts/{id}/send`, `PATCH /admin/notices/{id}` |
| `15-admin-student-directory.http` | `GET /admin/students/target-groups` |
| `03-staff-auth.http` | `PATCH /admin/staff/me` (로그아웃 요청 앞에 삽입 — 뒤에 붙이면 토큰 흐름이 어색해진다) |
| `08-honeytip.http` | `GET /admin/honey-tips/edit-requests` |
| `10-chat.http` | `POST /admin/chats/threads` |

### 4. 빌드 & 테스트

`./gradlew build` 통과 확인. 단 `.http`는 서버가 떠 있어야 검증되므로 이번 작업 범위에서는
정적 대조(경로/DTO 필드/status/ErrorCode)까지만 보증한다.

## 건드리지 않는 것

- `09/12`번 파일 공지 작성 바디의 `department` 필드 — `CreateNoticeRequest`에 없어서 무시되는
  죽은 필드다(부서는 작성자에게서 가져오는 게 설계 의도). 이번엔 그대로 두고 보고서에만 남긴다.
- `ChatMessage`의 `context_type`/`context_id`, JWT `university_id` 클레임 — 기존에 알고 있는 갭.

## 참고

- 마이그레이션 추가 없음 (엔티티 컬럼 변경이 없다 — 팩토리 메서드만 정리).
- 커밋 태그는 `docs/resources/conventions.md` 기준 `🪲 Fix`.
