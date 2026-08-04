# 학생 앱 API 에러 응답 정리

`docs/resources/api.md`가 정상 응답 기준 정리라면, 이 문서는 같은 화면/엔드포인트 기준으로 에러 응답을
정리한 것이다. 실제 코드(`ErrorCode`, `GlobalExceptionHandler`, 각 도메인 `Service`의
`throw new BusinessException(...)` 지점)를 근거로 작성했고, `api.md`에서 🆕로 표시된 미구현 엔드포인트는
기존 `ErrorCode` 네이밍 규칙을 따라 제안만 해둔 것이라 실제 구현 시 값이 달라질 수 있다.

**범례**: ✅ 코드에 이미 있는 에러 · 🆕 신규 엔드포인트용으로 새로 정의해야 하는 에러(제안)

## 공통 포맷

에러 응답은 `ApiResponse<T>`의 `success=false` 형태로, `data`는 항상 `null`이고 `error`에 코드/메시지가
담긴다.

```json
{
  "success": false,
  "data": null,
  "meta": null,
  "error": { "code": "POST_NOT_FOUND", "message": "해당 게시글을 찾을 수 없습니다." }
}
```

- `code`는 `ErrorCode` enum 이름 그대로(예: `POST_NOT_FOUND`), HTTP 상태 코드는 그 enum에 매핑된
  `HttpStatus`를 그대로 씀(`GlobalExceptionHandler.handleBusinessException`).
- `BusinessException(ErrorCode, String)` 오버로드를 쓰면 `error.message`가 enum 기본 메시지 대신 해당
  문자열로 바뀐다(예: `StudentAuthService`의 "존재하지 않는 학교입니다.").

## 모든 API 공통 에러

와이어프레임에 있는 화면들 전부에 적용되는, 컨트롤러 로직과 무관하게 프레임워크 레벨에서 나가는 에러다.

| 상태 | HTTP | code | 메시지 | 발생 조건 |
| --- | --- | --- | --- | --- |
| ✅ | 400 | `INVALID_INPUT` | 요청 값이 올바르지 않습니다. / `{필드명}: {검증 메시지}` | `@Valid` 검증 실패(`MethodArgumentNotValidException`) — 필드별 첫 번째 에러만 메시지에 노출 |
| ✅ | 401 | `UNAUTHORIZED` | 인증이 필요합니다. | `Authorization` 헤더 없음/JWT 파싱 실패 (`CurrentUserArgumentResolver`) |
| ✅ | 403 | `FORBIDDEN` | 접근 권한이 없습니다. | Spring Security `AccessDeniedException`/`BadCredentialsException`, 또는 본인 소유가 아닌 리소스 접근(아래 도메인별 표 참고) |
| ✅ | 500 | `INTERNAL_ERROR` | 서버 내부 오류가 발생했습니다. | 그 외 처리 안 된 모든 예외(`Exception`) — 로그에만 스택트레이스 남고 클라이언트엔 상세 미노출 |

이 표는 아래 도메인별 표에서 반복하지 않는다.

---

## 1. 로그인 / 비밀번호 찾기

| 상태 | Endpoint | code | 상태코드 | 메시지 | 비고 |
| --- | --- | --- | --- | --- | --- |
| ✅ | `POST /auth/students/login` | `INVALID_CREDENTIALS` | 401 | 아이디 또는 비밀번호가 올바르지 않습니다. | 이메일 미존재/비밀번호 불일치 둘 다 같은 에러(계정 존재 여부 노출 방지) |
| ✅ | `POST /auth/students/refresh` | `INVALID_REFRESH_TOKEN` | 401 | 리프레시 토큰이 유효하지 않습니다. | 서명 검증 실패, 저장된 토큰과 불일치, 토큰 주체가 학생이 아닌 경우 모두 동일 코드 |
| 🆕 | `POST /auth/students/password-reset/email` | `STUDENT_NOT_FOUND` | 404 | 해당 학생을 찾을 수 없습니다. | 기존 코드 재사용 가능. 다만 계정 존재 여부를 노출하고 싶지 않다면 이메일 존재와 무관하게 200을 주고 실제 발송만 스킵하는 방식도 검토 |
| 🆕 | `POST /auth/students/password-reset/verify` | `INVALID_VERIFICATION_CODE` | 400 | 인증번호가 일치하지 않아요. | 와이어프레임의 "인증번호가 일치하지 않아요" 문구 그대로 |
| 🆕 | `POST /auth/students/password-reset/verify` | `VERIFICATION_CODE_EXPIRED` | 400 | 인증번호가 만료되었습니다. | 화면의 `00:00` 타이머 만료 케이스 |
| 🆕 | `PATCH /auth/students/password-reset` | `INVALID_RESET_TOKEN` | 401 | 재설정 요청이 만료되었거나 유효하지 않습니다. | verify 단계에서 발급한 임시 토큰이 만료/위조된 경우 |

---

## 2. 회원가입

| 상태 | Endpoint | code | 상태코드 | 메시지 | 비고 |
| --- | --- | --- | --- | --- | --- |
| ✅ | `POST /auth/students/signup` | `DUPLICATE_ACCOUNT` | 409 | 이미 가입된 계정입니다. | 이메일 중복 |
| ✅ | `POST /auth/students/signup` | `INVALID_INPUT` | 400 | 존재하지 않는 학교입니다. | `universityId`가 `University` 테이블에 없는 경우 — 기본 메시지 대신 커스텀 메시지 사용 |
| ✅ | `POST /students/me/verification-documents` | `FILE_NOT_FOUND` | 404 | 해당 파일을 찾을 수 없습니다. | `fileId`가 사전에 `/files/presigned-url`로 발급받은 파일이 아닌 경우 |
| 🆕 | `POST /auth/students/check-email` | `DUPLICATE_ACCOUNT` | 409 | 이미 가입된 계정입니다. | 기존 코드 재사용 |
| 🆕 | `POST /auth/students/email-verifications` | `INVALID_INPUT` | 400 | 학교 공식 이메일만 사용할 수 있습니다. | 학교 도메인(`@ac.kr`, `@edu` 등) 아닌 이메일 |
| 🆕 | `POST /auth/students/email-verifications/confirm` | `INVALID_VERIFICATION_CODE` | 400 | 인증번호가 일치하지 않아요. | |
| 🆕 | `POST /auth/students/email-verifications/confirm` | `VERIFICATION_CODE_EXPIRED` | 400 | 인증번호가 만료되었습니다. | |
| 🆕 | `GET /students/me/verification-documents` | `VERIFICATION_DOCUMENT_NOT_FOUND` | 404 | 해당 재학 인증 서류를 찾을 수 없습니다. | 기존 코드 재사용 — 아직 서류를 한 번도 제출하지 않은 경우 |
| 🆕 | `GET /universities/{universityId}/majors` | `INVALID_INPUT` | 400 | 존재하지 않는 학교입니다. | 잘못된 `universityId` |

---

## 3. 공지사항

| 상태 | Endpoint | code | 상태코드 | 메시지 | 비고 |
| --- | --- | --- | --- | --- | --- |
| ✅ | `GET/PATCH /notices/{noticeId}`, `/notices/{noticeId}/read`, `/notices/{noticeId}/files` | `NOTICE_NOT_FOUND` | 404 | 해당 공지를 찾을 수 없습니다. | `NoticeReception`이 없는 경우도 동일 코드 — 본인에게 발송되지 않은 공지 ID로 조회해도 404(존재 유무 노출 안 함) |
| ✅ | `POST /chats/threads` (`noticeId` 포함) | `NOTICE_NOT_FOUND` | 404 | 해당 공지를 찾을 수 없습니다. | `ChatService`에서 `noticeId` 유효성 검사 |

---

## 4. 클럽

| 상태 | Endpoint | code | 상태코드 | 메시지 | 비고 |
| --- | --- | --- | --- | --- | --- |
| ✅ | `GET/PATCH/DELETE /clubs/{clubId}`, `/join`, `/leave`, `/members` | `CLUB_NOT_FOUND` | 404 | 해당 클럽을 찾을 수 없습니다. | |
| ✅ | `PATCH /clubs/{clubId}` | `FORBIDDEN` | 403 | 접근 권한이 없습니다. | 개설자(`creator`)가 아닌 학생이 수정 시도 (`requireCreator`) |
| ✅ | `DELETE /clubs/{clubId}` | `FORBIDDEN` | 403 | 접근 권한이 없습니다. | 개설자가 아닌 학생이 삭제 시도 |
| ✅ | `GET /clubs/{clubId}/members` | `FORBIDDEN` | 403 | 접근 권한이 없습니다. | 멤버 목록도 개설자만 조회 가능(`requireCreator`) — 일반 멤버는 못 봄 |
| ✅ | `POST /clubs/{clubId}/join` | `ALREADY_CLUB_MEMBER` | 409 | 이미 참여 중인 클럽입니다. | |
| ✅ | `POST /clubs/{clubId}/join` | `CLUB_FULL` | 409 | 클럽 정원이 초과되었습니다. | |
| ✅ | `DELETE /clubs/{clubId}/leave` | `NOT_CLUB_MEMBER` | 400 | 클럽에 참여 중이 아닙니다. | |
| 🆕 | `POST/GET /clubs/{clubId}/messages` | `CLUB_NOT_FOUND` | 404 | 해당 클럽을 찾을 수 없습니다. | 기존 코드 재사용 |
| 🆕 | `POST/GET /clubs/{clubId}/messages` | `NOT_CLUB_MEMBER` | 403 | 클럽에 참여 중이 아닙니다. | 채팅방은 멤버만 접근 가능 — 기존 `NOT_CLUB_MEMBER`(400)를 그대로 쓸지, 403으로 바꿔 쓸지는 통일 필요 |

---

## 5. 정보 (생활정보/꿀팁)

| 상태 | Endpoint | code | 상태코드 | 메시지 | 비고 |
| --- | --- | --- | --- | --- | --- |
| ✅ | `GET /honey-tips/{honeyTipId}` | `HONEY_TIP_NOT_FOUND` | 404 | 해당 정보글을 찾을 수 없습니다. | |
| ✅ | `POST /honey-tips/{honeyTipId}/edit-requests` | `HONEY_TIP_NOT_FOUND` | 404 | 해당 정보글을 찾을 수 없습니다. | 존재하지 않는 정보글에 수정 요청 시도 |

---

## 6. 알람 (채팅/알림)

| 상태 | Endpoint | code | 상태코드 | 메시지 | 비고 |
| --- | --- | --- | --- | --- | --- |
| ✅ | `GET /chats/threads/{threadId}/messages`, `POST .../messages`, `PATCH .../read` | `CHAT_THREAD_NOT_FOUND` | 404 | 해당 대화 스레드를 찾을 수 없습니다. | |
| ✅ | 위 채팅 스레드 엔드포인트 전체 | `FORBIDDEN` | 403 | 접근 권한이 없습니다. | 스레드 당사자(학생 본인 또는 담당 직원)가 아닌 경우 |
| 🆕 | `PATCH /notifications/{notificationId}/read` | `NOTIFICATION_NOT_FOUND` | 404 | 해당 알림을 찾을 수 없습니다. | 신규 도메인이라 `ErrorCode`도 새로 추가해야 함 |
| 🆕 | `PATCH /notifications/settings` | `INVALID_INPUT` | 400 | 요청 값이 올바르지 않습니다. | "시스템 안내"처럼 끌 수 없는 필수 카테고리를 끄려는 요청 — 전용 코드(`REQUIRED_NOTIFICATION_CATEGORY`)를 새로 팔지, `INVALID_INPUT`으로 뭉뚱그릴지는 결정 필요 |

---

## 7. 마이페이지

| 상태 | Endpoint | code | 상태코드 | 메시지 | 비고 |
| --- | --- | --- | --- | --- | --- |
| ✅ | `GET/PATCH /students/me` | `STUDENT_NOT_FOUND` | 404 | 해당 학생을 찾을 수 없습니다. | 토큰은 유효한데 학생 레코드가 삭제된 경우 등 — 일반적인 플로우에선 거의 안 남 |
| 🆕 | `PATCH /students/me/password` | `INVALID_VERIFICATION_CODE` / `VERIFICATION_CODE_EXPIRED` | 400 | (1번 섹션과 동일) | 마이페이지 비밀번호 재설정도 이메일 인증코드 방식이라면 1번 섹션 에러를 그대로 공유 |
| 🆕 | `GET /posts/mine`, `GET /comments/mine` | — | — | — | 본인 글/댓글 목록 조회라 별도 not-found 케이스 없음. 빈 배열 반환이 기본 |

---

## 8. 커뮤니티

| 상태 | Endpoint | code | 상태코드 | 메시지 | 비고 |
| --- | --- | --- | --- | --- | --- |
| ✅ | `GET /posts/{postId}`, `.../comments`, `.../like`, `.../report` | `POST_NOT_FOUND` | 404 | 해당 게시글을 찾을 수 없습니다. | |
| ✅ | `DELETE /posts/{postId}` | `FORBIDDEN` | 403 | 접근 권한이 없습니다. | 작성자 본인이 아닌 학생이 삭제 시도 (`isStudent()` && 작성자 불일치) |
| ✅ | `POST /posts/{postId}/like` | `ALREADY_LIKED` | 409 | 이미 좋아요를 눌렀습니다. | |
| ✅ | `DELETE /posts/{postId}/like` | `NOT_LIKED_YET` | 400 | 좋아요를 누르지 않았습니다. | |
| ✅ | `POST /posts/{postId}/comments` (`parentCommentId` 포함 시) | `COMMENT_NOT_FOUND` | 404 | 해당 댓글을 찾을 수 없습니다. | 대댓글 작성 시 부모 댓글이 없는 경우 |
| ✅ | `DELETE /posts/{postId}/comments/{commentId}` | `COMMENT_NOT_FOUND` | 404 | 해당 댓글을 찾을 수 없습니다. | 댓글이 해당 게시글 소속이 아니거나 없음 |
| ✅ | `DELETE /posts/{postId}/comments/{commentId}` | `FORBIDDEN` | 403 | 접근 권한이 없습니다. | 작성자 본인이 아닌 학생이 삭제 시도 |
| ✅ | `POST/DELETE /posts/{postId}/comments/{commentId}/like` | `ALREADY_LIKED` / `NOT_LIKED_YET` | 409 / 400 | (게시글 좋아요와 동일 메시지) | |
| ✅ | `POST /posts/{postId}/report`, `.../comments/{commentId}/report` | `POST_NOT_FOUND` / `COMMENT_NOT_FOUND` | 404 | (표 위와 동일) | |

---

## 전체 `ErrorCode` 레퍼런스

### 기존 (`ErrorCode.java`)

| code | 상태코드 | 메시지 |
| --- | --- | --- |
| `INVALID_INPUT` | 400 | 요청 값이 올바르지 않습니다. |
| `UNAUTHORIZED` | 401 | 인증이 필요합니다. |
| `FORBIDDEN` | 403 | 접근 권한이 없습니다. |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류가 발생했습니다. |
| `INVALID_CREDENTIALS` | 401 | 아이디 또는 비밀번호가 올바르지 않습니다. |
| `INVALID_REFRESH_TOKEN` | 401 | 리프레시 토큰이 유효하지 않습니다. |
| `INVALID_INVITE_CODE` | 400 | 초대 코드가 유효하지 않거나 이미 사용되었습니다. |
| `STAFF_NOT_VERIFIED` | 403 | 아직 승인되지 않은 담당자 계정입니다. |
| `STUDENT_NOT_FOUND` | 404 | 해당 학생을 찾을 수 없습니다. |
| `STAFF_NOT_FOUND` | 404 | 해당 담당자를 찾을 수 없습니다. |
| `DUPLICATE_ACCOUNT` | 409 | 이미 가입된 계정입니다. |
| `NOTICE_NOT_FOUND` | 404 | 해당 공지를 찾을 수 없습니다. |
| `NOTICE_FILE_NOT_FOUND` | 404 | 해당 첨부파일을 찾을 수 없습니다. |
| `CHAT_THREAD_NOT_FOUND` | 404 | 해당 대화 스레드를 찾을 수 없습니다. |
| `POST_NOT_FOUND` | 404 | 해당 게시글을 찾을 수 없습니다. |
| `COMMENT_NOT_FOUND` | 404 | 해당 댓글을 찾을 수 없습니다. |
| `ALREADY_LIKED` | 409 | 이미 좋아요를 눌렀습니다. |
| `NOT_LIKED_YET` | 400 | 좋아요를 누르지 않았습니다. |
| `CLUB_NOT_FOUND` | 404 | 해당 클럽을 찾을 수 없습니다. |
| `CLUB_FULL` | 409 | 클럽 정원이 초과되었습니다. |
| `ALREADY_CLUB_MEMBER` | 409 | 이미 참여 중인 클럽입니다. |
| `NOT_CLUB_MEMBER` | 400 | 클럽에 참여 중이 아닙니다. |
| `HONEY_TIP_NOT_FOUND` | 404 | 해당 정보글을 찾을 수 없습니다. |
| `HONEY_TIP_EDIT_REQUEST_NOT_FOUND` | 404 | 해당 수정 요청을 찾을 수 없습니다. |
| `VERIFICATION_DOCUMENT_NOT_FOUND` | 404 | 해당 재학 인증 서류를 찾을 수 없습니다. |
| `FILE_NOT_FOUND` | 404 | 해당 파일을 찾을 수 없습니다. |

### 신규 제안 (미구현 엔드포인트용)

| code | 상태코드 | 메시지(안) | 필요한 곳 |
| --- | --- | --- | --- |
| `INVALID_VERIFICATION_CODE` | 400 | 인증번호가 일치하지 않아요. | 비밀번호 찾기, 회원가입 이메일 인증 |
| `VERIFICATION_CODE_EXPIRED` | 400 | 인증번호가 만료되었습니다. | 비밀번호 찾기, 회원가입 이메일 인증 |
| `INVALID_RESET_TOKEN` | 401 | 재설정 요청이 만료되었거나 유효하지 않습니다. | 비밀번호 재설정 |
| `NOTIFICATION_NOT_FOUND` | 404 | 해당 알림을 찾을 수 없습니다. | 알림 도메인 |

이 4개는 `api.md`의 🆕 항목을 실제로 구현할 때 `ErrorCode.java`에 추가하면 된다. 이름/메시지는 기존
네이밍(`{도메인}_{상태}` 또는 `INVALID_{항목}`) 패턴을 따라 제안한 것이라 확정은 아니다.
