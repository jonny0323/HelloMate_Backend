# 학생 앱 API 레퍼런스 (요청/정상 응답/에러 응답)

`docs/resources/api.md`(화면별 구현 상태 분석), `docs/resources/api-errors.md`(에러 코드만 정리)를 바탕으로,
실제 구현된 API 각각에 대해 요청·정상 응답·에러 응답을 한 곳에 모았다. 모든 예시는 실제 컨트롤러/서비스/DTO
코드를 근거로 작성했다 (허구 필드 없음). 화면 섹션 순서는 `api.md`와 동일하게 맞췄다.

## 공통 사항

- 모든 응답은 `ApiResponse<T>`로 감싸짐: `{ "success": boolean, "data": T, "meta": object|null, "error": {code, message}|null }`.
- 인증: `/auth/**`, `/docs/**`, `/swagger-ui/**` 를 제외한 모든 엔드포인트는 `Authorization: Bearer {accessToken}`
  헤더가 필요하다(`SecurityConfig` — `anyRequest().authenticated()`). 아래 각 API 표에서 이미 인증이 필요한 건
  반복 표기하지 않고, 필요 없는 것만 별도 표시했다.
- 목록 API의 커서 페이지네이션: 요청 `?cursor={직전 마지막 항목의 인코딩된 cursor}&limit={개수, 기본 20}`,
  응답 `meta: { "nextCursor": string|null, "hasNext": boolean, "size": number }`.
- 번역: `Accept-Language` 헤더를 보내면 해당 엔드포인트가 원문과 함께 번역본을 `translated` 필드로 같이
  내려준다(`translated: null`이면 번역 대상 언어가 없거나 헤더가 없는 경우).
- 아래 모든 엔드포인트에 공통으로 걸리는 에러이므로 개별 표에서 생략함: 요청 바디 검증 실패 시
  `400 INVALID_INPUT`, 토큰 없음/무효 시 `401 UNAUTHORIZED`, 처리되지 않은 서버 예외 시
  `500 INTERNAL_ERROR`.

---

## 1. 로그인

### `POST /auth/students/login` — 인증 불필요

요청
```json
{ "email": "student123@inu.ac.kr", "password": "pa1234" }
```

성공 응답 `200`
```json
{
  "success": true,
  "data": { "accessToken": "eyJhbGciOi...", "refreshToken": "eyJhbGciOi..." },
  "meta": null,
  "error": null
}
```

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 401 | `INVALID_CREDENTIALS` | 아이디 또는 비밀번호가 올바르지 않습니다. | 이메일 미존재 또는 비밀번호 불일치 (계정 존재 여부를 구분해서 노출하지 않음) |

### `POST /auth/students/refresh` — 인증 불필요

요청
```json
{ "refreshToken": "eyJhbGciOi..." }
```

성공 응답 `200`: 로그인과 동일한 `TokenResponse` 형태.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 401 | `INVALID_REFRESH_TOKEN` | 리프레시 토큰이 유효하지 않습니다. | 서명 검증 실패 / 저장된 토큰과 불일치 / 토큰 주체가 학생이 아님 — 세 경우 모두 동일 코드 |

### `POST /auth/students/logout`

요청 바디 없음, `Authorization` 헤더만 필요.

성공 응답 `200`
```json
{ "success": true, "data": null, "meta": null, "error": null }
```

에러 응답: 별도 없음 (공통 에러만 해당).

### `POST /auth/students/password-reset/email` — 인증 불필요

비밀번호 재설정용 인증번호를 이메일로 발송한다(코드 유효시간 5분). 실제 발송은 스텁(`StubEmailService`)이라
로그에만 코드가 남는다.

요청
```json
{ "email": "student123@inu.ac.kr" }
```

성공 응답 `200`: `{ "success": true, "data": null, "meta": null, "error": null }`

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `STUDENT_NOT_FOUND` | 해당 학생을 찾을 수 없습니다. | 가입되지 않은 이메일 |

### `POST /auth/students/password-reset/verify` — 인증 불필요

요청
```json
{ "email": "student123@inu.ac.kr", "code": "123456" }
```

성공 응답 `200`
```json
{ "success": true, "data": { "resetToken": "b1f2c3d4-..." }, "meta": null, "error": null }
```
`resetToken`은 1회용이며 다음 `PATCH /auth/students/password-reset` 호출에만 쓸 수 있다.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 400 | `INVALID_VERIFICATION_CODE` | 인증번호가 일치하지 않아요. | 코드 불일치 또는 이미 사용된 코드 |
| 400 | `VERIFICATION_CODE_EXPIRED` | 인증번호가 만료되었습니다. | 발송 후 5분 경과 |

### `PATCH /auth/students/password-reset` — 인증 불필요

요청
```json
{ "resetToken": "b1f2c3d4-...", "newPassword": "newPa1234" }
```

성공 응답 `200`: `data: null`

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 401 | `INVALID_RESET_TOKEN` | 재설정 요청이 만료되었거나 유효하지 않습니다. | 토큰이 존재하지 않거나(이미 사용됨 포함) 만료됨 |

---

## 2. 회원가입

### `POST /auth/students/signup` — 인증 불필요

요청 (`StudentType`은 `"교환학생" | "정규과정생" | "어학당 수강생" | "한국인 대학생"` 라벨 문자열)
```json
{
  "email": "student123@inu.ac.kr",
  "name": "김지수",
  "password": "pa1234",
  "country": "KR",
  "language": "ko",
  "studentType": "정규과정생",
  "major": "컴퓨터공학부",
  "grade": "1학년",
  "universityId": "univ-inu"
}
```

성공 응답 `201`
```json
{
  "success": true,
  "data": { "id": "stu_01h...", "name": "김지수" },
  "meta": null,
  "error": null
}
```

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 409 | `DUPLICATE_ACCOUNT` | 이미 가입된 계정입니다. | 이메일 중복 |
| 400 | `INVALID_INPUT` | 존재하지 않는 학교입니다. | `universityId`가 `university` 테이블에 없음 (기본 메시지 대신 커스텀 메시지로 내려감) |

### `POST /auth/students/check-email` — 인증 불필요

이메일(로그인 아이디) 중복 확인. 1/3 화면의 "중복 확인" 버튼용.

요청
```json
{ "email": "student123@inu.ac.kr" }
```

성공 응답 `200`(사용 가능): `data: null`

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 409 | `DUPLICATE_ACCOUNT` | 이미 가입된 계정입니다. | 이메일 중복 |

### `POST /auth/students/email-verifications` — 인증 불필요

가입 단계 학교 이메일 인증(서류 인증과 별개 트랙). 코드 유효시간 5분. 학교 공식 이메일(`.ac.kr`/`.edu`
접미사)만 허용 — 학교별 도메인 화이트리스트는 아직 없음(스텁 수준 검증).

요청
```json
{ "email": "student123@inu.ac.kr" }
```

성공 응답 `200`: `data: null`

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 400 | `INVALID_INPUT` | 학교 공식 이메일만 사용할 수 있습니다. | `.ac.kr`/`.edu`로 끝나지 않는 이메일 |

### `POST /auth/students/email-verifications/confirm` — 인증 불필요

요청
```json
{ "email": "student123@inu.ac.kr", "code": "123456" }
```

성공 응답 `200`: `data: null`

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 400 | `INVALID_VERIFICATION_CODE` | 인증번호가 일치하지 않아요. | 코드 불일치 또는 이미 사용된 코드 |
| 400 | `VERIFICATION_CODE_EXPIRED` | 인증번호가 만료되었습니다. | 발송 후 5분 경과 |

### `GET /universities/{universityId}/majors?query=` — 인증 불필요

3/3 단계(학교 정보) 전공 자동완성. `query` 생략하면 해당 학교 전공 전체 목록. **지금 시드 데이터는
플레이스홀더**다(컴퓨터공학부/경영학과 등 흔한 학과명 10개) — 인천대 공식 학과 목록이 아니니 실제
데이터로 교체 전까지는 자동완성 결과가 정확하지 않을 수 있다.

성공 응답 `200`
```json
{
  "success": true,
  "data": [
    { "id": "major-inu-placeholder-01", "name": "컴퓨터공학부" },
    { "id": "major-inu-placeholder-02", "name": "소프트웨어학과" }
  ],
  "meta": null,
  "error": null
}
```

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 400 | `INVALID_INPUT` | 존재하지 않는 학교입니다. | 잘못된 `universityId` |

### `POST /files/presigned-url`

서류 사진(학생증 등) 업로드 전에 먼저 호출한다.

요청
```json
{ "filename": "student_card.jpg", "contentType": "image/jpeg", "purpose": "verification_document" }
```

성공 응답 `200`
```json
{
  "success": true,
  "data": {
    "uploadUrl": "https://storage.local/upload/....",
    "fileId": "file_01h...",
    "fileUrl": "https://storage.local/files/...."
  },
  "meta": null,
  "error": null
}
```
`LocalStubFileStorageService` 스텁이라 실제 업로드는 로컬 저장이며, 응답 형태는 S3 붙여도 동일하게 유지될
예정이다(`global 참고: 번역/파일은 스텁` 규칙).

에러 응답: 별도 없음 (공통 에러만 해당).

### `POST /students/me/verification-documents`

`presigned-url`로 발급받은 `fileId`로 서류 인증을 제출한다.

요청
```json
{ "fileId": "file_01h..." }
```

성공 응답 `201`
```json
{
  "success": true,
  "data": {
    "id": "vdoc_01h...",
    "studentId": "stu_01h...",
    "studentName": "김지수",
    "fileUrl": "https://storage.local/files/....",
    "status": "pending",
    "createdAt": "2026-08-04T10:00:00"
  },
  "meta": null,
  "error": null
}
```
`status`는 `"pending" | "approved" | "rejected"` 중 하나(제출 직후는 항상 `pending`).

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `FILE_NOT_FOUND` | 해당 파일을 찾을 수 없습니다. | `fileId`가 사전에 업로드된 파일이 아님 |

### `GET /students/me/verification-documents`

본인이 제출한 서류 인증 중 가장 최근 것의 상태를 조회한다. 회원가입 중 "검토 중"/"완료"/"재제출" 폴링과
마이페이지 "이메일 인증: 학생 인증됨" 뱃지용.

성공 응답 `200`: `POST`와 동일한 `VerificationDocumentResponse` 형태.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `VERIFICATION_DOCUMENT_NOT_FOUND` | 해당 재학 인증 서류를 찾을 수 없습니다. | 서류를 한 번도 제출하지 않음 |

---

## 3. 공지사항

### `GET /notices?groupBy=&q=&cursor=&limit=`

`groupBy=department`를 주면 데이터가 배열이 아니라 부서별 맵으로 내려온다(둘 다 실제 응답 형태). `q`는
제목+본문 부분일치 검색이고 `groupBy`와 동시에 쓸 수 있다(부서 필터 화면의 "검색+카테고리 필터" 요구
사항). 통합 검색(`/search`, 9번 섹션)과는 별개 — `/search`는 부서 필터를 못 걸어서 이 파라미터를
따로 뒀다.

성공 응답 `200` — 기본(`groupBy` 없음)
```json
{
  "success": true,
  "data": [
    {
      "id": "notice_01h...",
      "title": "2024학년도 외국인 유학생 건강보험 가입 및 갱신 안내",
      "department": "국제교류처",
      "type": "urgent",
      "isRead": false,
      "createdAt": "2026-08-01T09:00:00"
    }
  ],
  "meta": { "nextCursor": "MjAyNi0wOC0wMVQwOTowMDowMA==", "hasNext": true, "size": 20 },
  "error": null
}
```

성공 응답 `200` — `groupBy=department`
```json
{
  "success": true,
  "data": {
    "국제교류처": [ { "id": "notice_01h...", "title": "...", "department": "국제교류처", "type": "urgent", "isRead": false, "createdAt": "..." } ],
    "학생처": [ ]
  },
  "meta": { "nextCursor": null, "hasNext": false, "size": 4 },
  "error": null
}
```
`type`은 `"urgent" | "normal"`.

에러 응답: 별도 없음 (공통 에러만 해당) — 목록은 본인에게 발송된 것만 조회하므로 404가 없다.

### `GET /notices/{noticeId}`

읽음 처리도 같이 된다(`markRead`).

성공 응답 `200`
```json
{
  "success": true,
  "data": {
    "id": "notice_01h...",
    "title": "2024 글로벌 문화 교류 축제",
    "content": "안녕하세요. 국제교류처입니다...",
    "translated": { "lang": "en", "text": "Hello, this is the Office of International Affairs..." },
    "department": "국제교류처",
    "type": "normal",
    "isRead": true,
    "files": [ { "id": "nf_01h...", "filename": "보험가입_안내문.pdf", "fileUrl": "https://storage.local/files/..." } ],
    "createdAt": "2026-08-01T09:00:00"
  },
  "meta": null,
  "error": null
}
```

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `NOTICE_NOT_FOUND` | 해당 공지를 찾을 수 없습니다. | 존재하지 않는 ID이거나 본인에게 발송되지 않은 공지(`NoticeReception` 없음) — 두 경우 구분 없이 동일 |

### `PATCH /notices/{noticeId}/read`

요청 바디 없음.

성공 응답 `200`: `{ "success": true, "data": null, "meta": null, "error": null }`

에러 응답: `GET /notices/{noticeId}`와 동일하게 `404 NOTICE_NOT_FOUND`.

### `GET /notices/unread-count`

성공 응답 `200`
```json
{ "success": true, "data": { "count": 3 }, "meta": null, "error": null }
```
에러 응답: 별도 없음.

### `GET /notices/{noticeId}/files`

성공 응답 `200`
```json
{
  "success": true,
  "data": [ { "id": "nf_01h...", "filename": "보험가입_안내문.pdf", "fileUrl": "https://storage.local/files/..." } ],
  "meta": null,
  "error": null
}
```
에러 응답: `404 NOTICE_NOT_FOUND` (본인에게 발송된 공지가 아니면).

---

## 4. 클럽

### `GET /clubs?status=`

성공 응답 `200`
```json
{
  "success": true,
  "data": [
    {
      "id": "club_01h...",
      "title": "아시아 음식 러버들",
      "introduction": "아시아 음식 러버들에 오신 것을 환영합니다! ...",
      "creatorId": "stu_02h...",
      "creatorName": "Ji-won Kim",
      "maxMembers": 25,
      "currentMembers": 22,
      "full": false,
      "deadline": "2026-10-24",
      "createdAt": "2026-07-20T12:00:00"
    }
  ],
  "meta": null,
  "error": null
}
```
에러 응답: 별도 없음.

### `POST /clubs`

요청
```json
{ "title": "아시아 음식 러버들", "introduction": "매주 금요일 저녁 7시 저녁 모임...", "maxMembers": 25, "deadline": "2026-10-24" }
```
`deadline`은 `@Future` 검증(오늘 이후 날짜만 허용).

성공 응답 `201`: `ClubResponse` (위 목록 항목과 동일 형태, `currentMembers: 1`로 생성자 자동 참여).

에러 응답: 공통 에러(`deadline`이 과거 날짜면 `400 INVALID_INPUT`)만 해당.

### `GET /clubs/mine`

성공 응답 `200`: `ClubResponse[]` — 내가 만들었거나 참여 중인 클럽만.

### `GET /clubs/{clubId}`

성공 응답 `200`: `ClubResponse` 단건.

에러 응답: `404 CLUB_NOT_FOUND`.

### `PATCH /clubs/{clubId}`

요청 (전부 선택값, null이 아닌 필드만 반영)
```json
{ "title": null, "introduction": "수정된 소개", "maxMembers": null, "deadline": null }
```

성공 응답 `200`: 수정된 `ClubResponse`.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `CLUB_NOT_FOUND` | 해당 클럽을 찾을 수 없습니다. | |
| 403 | `FORBIDDEN` | 접근 권한이 없습니다. | 개설자(`creator`)가 아닌 학생이 수정 시도 |

### `DELETE /clubs/{clubId}`

성공 응답 `200`: `data: null`.

에러 응답: `PATCH`와 동일 (`404 CLUB_NOT_FOUND`, `403 FORBIDDEN` — 개설자만 삭제 가능).

### `POST /clubs/{clubId}/join`

요청 바디 없음.

성공 응답 `200`: `data: null`.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `CLUB_NOT_FOUND` | 해당 클럽을 찾을 수 없습니다. | |
| 409 | `ALREADY_CLUB_MEMBER` | 이미 참여 중인 클럽입니다. | |
| 409 | `CLUB_FULL` | 클럽 정원이 초과되었습니다. | `currentMembers == maxMembers` |

### `DELETE /clubs/{clubId}/leave`

성공 응답 `200`: `data: null`.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `CLUB_NOT_FOUND` | 해당 클럽을 찾을 수 없습니다. | |
| 400 | `NOT_CLUB_MEMBER` | 클럽에 참여 중이 아닙니다. | |

### `GET /clubs/{clubId}/members`

성공 응답 `200`
```json
{
  "success": true,
  "data": [ { "studentId": "stu_03h...", "studentName": "Ji-won Kim", "joinedAt": "2026-07-21T10:00:00" } ],
  "meta": null,
  "error": null
}
```

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `CLUB_NOT_FOUND` | 해당 클럽을 찾을 수 없습니다. | |
| 403 | `FORBIDDEN` | 접근 권한이 없습니다. | **개설자만 멤버 목록을 볼 수 있음** — 일반 멤버가 호출해도 403 (와이어프레임엔 멤버도 볼 수 있는 것처럼 보이니 프론트 연동 시 주의) |

### `POST /clubs/{clubId}/messages`

클럽 그룹 채팅 메시지 전송. 커뮤니티 게시글과 달리 멤버끼리는 서로 이름을 안다는 전제라
익명화하지 않는다.

요청
```json
{ "content": "이번 주 금요일 모임 장소 정했어요!" }
```

성공 응답 `201`
```json
{
  "success": true,
  "data": { "id": "cmsg_01h...", "senderId": "stu_03h...", "senderName": "Ji-won Kim", "content": "이번 주 금요일 모임 장소 정했어요!", "createdAt": "2026-08-04T10:00:00" },
  "meta": null,
  "error": null
}
```

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `CLUB_NOT_FOUND` | 해당 클럽을 찾을 수 없습니다. | |
| 400 | `NOT_CLUB_MEMBER` | 클럽에 참여 중이 아닙니다. | 멤버(개설자 포함)만 전송 가능 — `leave`와 동일 코드/상태 재사용 |

### `GET /clubs/{clubId}/messages?cursor=&limit=`

성공 응답 `200`
```json
{
  "success": true,
  "data": [ { "id": "cmsg_01h...", "senderId": "stu_03h...", "senderName": "Ji-won Kim", "content": "이번 주 금요일 모임 장소 정했어요!", "createdAt": "2026-08-04T10:00:00" } ],
  "meta": { "nextCursor": null, "hasNext": false, "size": 1 },
  "error": null
}
```

에러 응답: `POST`와 동일 (`404 CLUB_NOT_FOUND`, `400 NOT_CLUB_MEMBER`).

---

## 5. 정보 (생활정보/꿀팁)

### `GET /honey-tips?category=`

성공 응답 `200`
```json
{
  "success": true,
  "data": [
    {
      "id": "tip_01h...",
      "category": "비자/체류",
      "title": "D-2 비자 연장 완벽 가이드",
      "content": "만료일 4개월 전부터 연장을 준비하는것을 권장해요...",
      "viewCount": 1200,
      "createdAt": "2026-07-15T00:00:00",
      "updatedAt": "2026-08-01T00:00:00"
    }
  ],
  "meta": null,
  "error": null
}
```
`content`는 단일 텍스트 필드다 — 와이어프레임의 "단계별 안내 + 예상 수수료 + 처리 기간 + 문서 태그"는 이
필드 안에서 프론트가 마크다운 등으로 파싱해서 그려야 한다(`api.md` 5번 섹션 참고).

### `GET /honey-tips/{honeyTipId}`

성공 응답 `200`: 위와 동일한 `HoneyTipResponse` 단건 (조회 시 `viewCount` 1 증가).

에러 응답: `404 HONEY_TIP_NOT_FOUND`.

### `POST /honey-tips/{honeyTipId}/edit-requests`

요청
```json
{ "content": "비자 만료 4개월이 아니라 3개월 전부터로 수정해주세요." }
```

성공 응답 `201`: `data: null`.

에러 응답: `404 HONEY_TIP_NOT_FOUND`.

---

## 6. 알람 — 채팅/알림

### `POST /chats/threads`

`teacherId`는 담당자(Staff) ID, `noticeId`는 "담당자에게 질문하기"처럼 특정 공지에서 문의를 시작할 때만
채운다(선택값).

요청
```json
{ "teacherId": "staff_01h...", "noticeId": "notice_01h...", "message": "안녕하세요, 외국인 유학생 전용 보험 가입 기간이 언제까지인지 궁금해서 연락드렸습니다." }
```

성공 응답 `201`
```json
{ "success": true, "data": { "threadId": "thread_01h..." }, "meta": null, "error": null }
```
같은 `(student, teacher)` 조합으로 이미 스레드가 있으면 새로 만들지 않고 기존 스레드에 메시지만 추가하는
방식으로 보인다(`ChatThread`에 `(student_id, staff_id)` unique 제약).

에러 응답: `noticeId`를 보냈는데 존재하지 않으면 `404 NOTICE_NOT_FOUND`.

### `GET /chats/threads`

성공 응답 `200`
```json
{
  "success": true,
  "data": [
    {
      "threadId": "thread_01h...",
      "counterpartId": "staff_01h...",
      "counterpartName": "김하늘",
      "lastMessage": "유학생 전용 보험은 이번 주 금요일(27일) 오후 5시까지 신청이 가능합니다.",
      "lastMessageAt": "2026-08-03T19:14:00",
      "unread": true,
      "noticeId": "notice_01h..."
    }
  ],
  "meta": null,
  "error": null
}
```
클럽 그룹 채팅(여러 멤버가 있는 방)은 이 `chats/threads` API로 안 나온다 — `club` 도메인의
`GET /clubs/{clubId}/messages`로 별도 구현됨(4번 섹션 참고).

### `GET /chats/threads/{threadId}/messages?cursor=&limit=`

성공 응답 `200`
```json
{
  "success": true,
  "data": [ { "id": "msg_01h...", "senderType": "teacher", "content": "안녕하세요!국제교류처 김하늘입니다.", "createdAt": "2026-08-03T19:14:00" } ],
  "meta": { "nextCursor": null, "hasNext": false, "size": 4 },
  "error": null
}
```
`senderType`은 `"user" | "teacher"`.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `CHAT_THREAD_NOT_FOUND` | 해당 대화 스레드를 찾을 수 없습니다. | |
| 403 | `FORBIDDEN` | 접근 권한이 없습니다. | 스레드 당사자(본인 또는 상대 담당자)가 아님 |

### `POST /chats/threads/{threadId}/messages`

요청
```json
{ "content": "안녕하세요, 외국인 유학생 전용 보험 가입 기간이 언제까지인지 궁금해서 연락드렸습니다." }
```

성공 응답 `201`: `ChatMessageResponse` 단건 (위 메시지 형태와 동일).

에러 응답: `GET messages`와 동일 (`404 CHAT_THREAD_NOT_FOUND`, `403 FORBIDDEN`).

### `PATCH /chats/threads/{threadId}/read`

요청 바디 없음. 성공 응답 `200`: `data: null`.

에러 응답: 동일 (`404 CHAT_THREAD_NOT_FOUND`, `403 FORBIDDEN`).

### `GET /chats/unread-count`

성공 응답 `200`
```json
{ "success": true, "data": { "count": 2 }, "meta": null, "error": null }
```
에러 응답: 별도 없음.

### `GET /notifications?category=&cursor=&limit=`

"알림" 탭 피드. `category`는 `notice`/`community`/`club`/`honey_tip` 중 하나(생략하면 전체). **채팅
알림(`chat_direct`/`chat_club`)은 이 피드에 안 쌓인다** — 채팅은 이미 `chats/threads`가 자체
읽음/안읽음을 관리하고 있어서 별도 알림 행을 안 만든다(`api.md`의 필터 목록도 채팅은 빼고
전체/공지/커뮤니티/클럽/생활정보 4개만 나열함). "오늘/어제 구분"은 프론트가 `createdAt` 기준으로
그린다(서버가 미리 그룹핑해서 내려주지 않음).

성공 응답 `200`
```json
{
  "success": true,
  "data": [
    { "id": "notif_01h...", "category": "notice", "title": "2024 글로벌 문화 교류 축제", "linkType": "notice", "linkId": "notice_01h...", "read": false, "createdAt": "2026-08-04T09:00:00" }
  ],
  "meta": { "nextCursor": null, "hasNext": false, "size": 1 },
  "error": null
}
```
`category`는 `"chat_direct" | "chat_club" | "notice" | "community" | "club" | "honey_tip" | "system"`
중 하나(실제로 피드에 쌓이는 건 `chat_*`를 뺀 나머지). `linkType`/`linkId`로 프론트가 딥링크한다
(`notice`→공지 상세, `post`→게시글 상세, `club`→클럽 상세, `honey_tip`→정보글 상세).

에러 응답: 별도 없음.

### `PATCH /notifications/{notificationId}/read`

요청 바디 없음. 성공 응답 `200`: `data: null`.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `NOTIFICATION_NOT_FOUND` | 해당 알림을 찾을 수 없습니다. | 본인 알림이 아니거나 존재하지 않음 |

### `GET /notifications/unread-count`

성공 응답 `200`: `{ "success": true, "data": { "count": 3 }, "meta": null, "error": null }`

### `GET /notifications/settings`

채팅 토글 2개(`chat_direct`/`chat_club`) + 서비스 토글 5개(`notice`/`community`/`club`/`honey_tip`/`system`),
총 7개를 항상 전부 내려준다(따로 설정한 적 없으면 `enabled: true` 기본값). `system`은 `required: true`라서
끌 수 없다.

성공 응답 `200`
```json
{
  "success": true,
  "data": [
    { "category": "chat_direct", "enabled": true, "required": false },
    { "category": "chat_club", "enabled": true, "required": false },
    { "category": "notice", "enabled": true, "required": false },
    { "category": "community", "enabled": true, "required": false },
    { "category": "club", "enabled": true, "required": false },
    { "category": "honey_tip", "enabled": true, "required": false },
    { "category": "system", "enabled": true, "required": true }
  ],
  "meta": null,
  "error": null
}
```

### `PATCH /notifications/settings`

여러 카테고리를 한 번에 바꿀 수 있다.

요청
```json
{ "settings": [ { "category": "community", "enabled": false }, { "category": "honey_tip", "enabled": false } ] }
```

성공 응답 `200`: 변경 반영된 `GET /notifications/settings`와 동일한 목록.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 400 | `INVALID_INPUT` | 시스템 안내는 끌 수 없습니다. | `category: "system"`을 `enabled: false`로 끄려는 요청 |

---

## 7. 마이페이지

### `GET /students/me`

성공 응답 `200`
```json
{
  "success": true,
  "data": {
    "id": "stu_01h...",
    "email": "student123@inu.ac.kr",
    "name": "김지수",
    "country": "KR",
    "birthYear": 2001,
    "language": "ko",
    "studentType": "정규과정생",
    "major": "컴퓨터공학부",
    "grade": "1학년",
    "universityName": "인천대학교"
  },
  "meta": null,
  "error": null
}
```
`birthYear`는 가입 시점엔 안 받고 마이페이지에서만 채울 수 있어서, 아직 입력한 적 없는 학생은 `null`.

에러 응답: `404 STUDENT_NOT_FOUND` (토큰은 유효한데 학생 레코드가 없는 예외적인 경우 — 정상 플로우에선 거의
발생하지 않음).

### `PATCH /students/me`

요청 — 전부 선택값(null이 아닌 필드만 반영). `name`/`country`를 열어주는 것과 서류 인증(재학증명서 등)
실명 불일치 리스크는 자동 검증하지 않는다 — 서류 인증 검토 단계에서 담당자가 걸러내는 걸 전제로 한다.
```json
{ "name": "김지수", "country": "KR", "birthYear": 2001, "language": "en", "major": "소프트웨어학과", "grade": "2학년" }
```

성공 응답 `200`: 수정된 `StudentProfileResponse` (위와 동일 형태).

에러 응답: `404 STUDENT_NOT_FOUND`.

### `PATCH /students/me/password`

로그인 상태에서 비밀번호를 재설정한다(현재 비밀번호 확인 없이, 이메일 인증코드 방식). 코드는
`POST /auth/students/password-reset/email`로 본인 이메일에 발송한 뒤 받은 6자리 코드를 그대로 쓴다(로그인
상태에서도 이 엔드포인트 호출 가능 — 별도 발송 API를 새로 만들지 않고 재사용).

요청
```json
{ "code": "123456", "newPassword": "newPa1234" }
```

성공 응답 `200`: `data: null`

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 400 | `INVALID_VERIFICATION_CODE` | 인증번호가 일치하지 않아요. | 코드 불일치 또는 이미 사용된 코드 |
| 400 | `VERIFICATION_CODE_EXPIRED` | 인증번호가 만료되었습니다. | 발송 후 5분 경과 |

---

## 8. 커뮤니티

### `GET /posts?q=&cursor=&limit=`

`q`는 제목+본문 부분일치 검색(자유게시판 상단 검색창). `/posts/mine`(작성자 필터)에는 없음.

성공 응답 `200`
```json
{
  "success": true,
  "data": [
    {
      "id": "post_01h...",
      "anonName": "익명 1",
      "title": null,
      "content": "Does anyone know a good Tteokbokki place near the central library?",
      "originalLang": "en",
      "likeCount": 3,
      "commentCount": 3,
      "createdAt": "2026-08-03T21:50:00"
    }
  ],
  "meta": { "nextCursor": "MjAyNi0wOC0wM1QyMTo1MDowMA==", "hasNext": true, "size": 20 },
  "error": null
}
```
`anonName`은 게시글마다 매번 랜덤 배정되는 게 아니라 같은 게시글/댓글 스레드 안에서 같은 작성자에게 고정된
별명이다(`PostAnonService`).

### `GET /posts/mine?cursor=&limit=`

본인이 작성한 게시글만. 응답 형태는 `GET /posts`와 동일(`PostSummaryResponse[]` + 커서 `meta`). 클럽
게시물은 다루지 않는다 — 클럽에는 "글" 개념이 없고 멤버십/그룹 채팅만 있음.

에러 응답: 별도 없음(본인 목록 조회라 404 대상 없음).

### `POST /posts`

요청 — 어떤 언어로 작성해도 됨(서버가 `originalLang`을 자동 감지).
```json
{ "title": "한국 음식 그리울 때 다들 어디 가세요?", "content": "요즘 김치찌개가 너무 먹고 싶네요... 추천 맛집 부탁드려요!" }
```

성공 응답 `201`
```json
{ "success": true, "data": { "id": "post_02h...", "anonName": "익명 5", "createdAt": "2026-08-04T10:00:00" }, "meta": null, "error": null }
```

### `GET /posts/{postId}`

`Accept-Language` 헤더를 보내면 게시글/댓글 각각 `translated` 필드가 채워진다.

성공 응답 `200`
```json
{
  "success": true,
  "data": {
    "id": "post_01h...",
    "anonName": "익명 2",
    "title": "기숙사 세탁기가 고장 난 것 같은데",
    "originalLang": "zh",
    "content": "宿舍里的洗衣机好像坏了，有人知道该联系谁修理吗？",
    "translated": { "lang": "ko", "text": "기숙사 세탁기가 고장 난 것 같은데, 수리하려면 어디로 연락해야 하는지 아시는 분 계신가요?" },
    "likeCount": 3,
    "likedByMe": false,
    "commentCount": 2,
    "comments": [
      {
        "id": "cmt_01h...",
        "parentCommentId": null,
        "anonName": "익명 3",
        "content": "작년에는 시험 기간에 밤 11시까지 했어요.",
        "originalLang": "ko",
        "translated": null,
        "likeCount": 0,
        "likedByMe": false,
        "createdAt": "2026-08-03T22:00:00"
      }
    ]
  },
  "meta": null,
  "error": null
}
```

에러 응답: `404 POST_NOT_FOUND`.

### `DELETE /posts/{postId}`

성공 응답 `200`: `data: null`.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `POST_NOT_FOUND` | 해당 게시글을 찾을 수 없습니다. | |
| 403 | `FORBIDDEN` | 접근 권한이 없습니다. | 작성자 본인이 아닌 학생이 삭제 시도 |

### `POST /posts/{postId}/like` / `DELETE /posts/{postId}/like`

요청 바디 없음. 성공 응답 `200`: `data: null`.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `POST_NOT_FOUND` | 해당 게시글을 찾을 수 없습니다. | |
| 409 | `ALREADY_LIKED` | 이미 좋아요를 눌렀습니다. | `POST`인데 이미 좋아요 누른 상태 |
| 400 | `NOT_LIKED_YET` | 좋아요를 누르지 않았습니다. | `DELETE`인데 좋아요 누른 적 없음 |

### `GET /posts/{postId}/comments`

성공 응답 `200`: `PostCommentResponse[]` (위 상세 응답의 `comments` 배열과 동일 형태).

에러 응답: `404 POST_NOT_FOUND`.

### `POST /posts/{postId}/comments`

대댓글이면 `parentCommentId`를 채운다(선택값).
```json
{ "content": "저는 온라인으로 문의해봤는데, 답변이 빠르더라고요.", "parentCommentId": null }
```

성공 응답 `201`: `PostCommentResponse` 단건.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `POST_NOT_FOUND` | 해당 게시글을 찾을 수 없습니다. | |
| 404 | `COMMENT_NOT_FOUND` | 해당 댓글을 찾을 수 없습니다. | `parentCommentId`가 존재하지 않는 댓글 ID |

### `DELETE /posts/{postId}/comments/{commentId}`

성공 응답 `200`: `data: null`.

에러 응답

| status | code | message | 조건 |
| --- | --- | --- | --- |
| 404 | `COMMENT_NOT_FOUND` | 해당 댓글을 찾을 수 없습니다. | 댓글이 없거나 해당 게시글 소속이 아님 |
| 403 | `FORBIDDEN` | 접근 권한이 없습니다. | 작성자 본인이 아닌 학생이 삭제 시도 |

### `POST /posts/{postId}/comments/{commentId}/like` / `DELETE .../like`

요청 바디 없음. 성공 응답 `200`: `data: null`.

에러 응답: 게시글 좋아요와 동일 (`409 ALREADY_LIKED` / `400 NOT_LIKED_YET`).

### `POST /posts/{postId}/report`, `POST /posts/{postId}/comments/{commentId}/report`

요청
```json
{ "reason": "광고성 게시글입니다." }
```

성공 응답 `201`: `data: null`.

에러 응답: `404 POST_NOT_FOUND` (게시글 신고), `404 COMMENT_NOT_FOUND` (댓글 신고).

### `GET /comments/mine?cursor=&limit=`

본인이 작성한 댓글 목록. `/posts/{postId}/comments`와 달리 최상위 리소스라 별도 컨트롤러
(`CommentController`)에 있다.

성공 응답 `200`
```json
{
  "success": true,
  "data": [
    {
      "id": "cmt_01h...",
      "postId": "post_01h...",
      "parentCommentId": null,
      "anonName": "익명 3",
      "content": "작년에는 시험 기간에 밤 11시까지 했어요.",
      "originalLang": "ko",
      "likeCount": 0,
      "createdAt": "2026-08-03T22:00:00"
    }
  ],
  "meta": { "nextCursor": null, "hasNext": false, "size": 1 },
  "error": null
}
```
어느 게시글의 댓글인지 알아야 프론트가 이동할 수 있어서 `postId`가 포함된다(상세 조회용
`PostCommentResponse`와 다른 별도 DTO). `translated`/`likedByMe`는 없음(내 활동 목록이라 번역/좋아요
상태까지는 안 내려줌).

에러 응답: 별도 없음.

---

## 9. 검색

와이어프레임엔 각 화면(공지, 커뮤니티)마다 검색창이 따로 있지만, 백엔드는 공지+생활정보만 다루는 통합 검색
하나만 있다. 커뮤니티 게시글 검색은 아직 없다(`api.md` 8번 섹션 갭).

### `GET /search?q=&types=`

`types`는 콤마 구분 문자열(예: `notice,honey_tip`), 생략하면 기본값 `notice,honey_tip` 둘 다 검색.

성공 응답 `200`
```json
{
  "success": true,
  "data": {
    "results": [
      { "type": "notice", "id": "notice_01h...", "department": "국제교류처", "category": null, "title": "2024 글로벌 문화 교류 축제", "snippet": "다양한 음식, 공연, 네트워크 기회를 통해..." },
      { "type": "honey_tip", "id": "tip_01h...", "department": null, "category": "비자/체류", "title": "D-2 비자 연장 완벽 가이드", "snippet": "만료일 4개월 전부터..." }
    ]
  },
  "meta": null,
  "error": null
}
```
에러 응답: 별도 없음(공통 에러만 해당).

---

## 남은 갭

기획 확인 결과 지금 단계에서는 보류하기로 한 것들.

- 서류 인증 문서 유형 구분(`documentType`: 학생증/재학증명서/입학허가서/성적증명서) — 지금
  `SubmitVerificationDocumentRequest`는 파일만 받고 어떤 서류인지 구분하지 않는다. 보류.
- 생활정보(꿀팁) 구조화 필드(`fee`/`estimatedDuration`/`externalLink`/`documentTags`) — 지금
  `HoneyTip.content`는 단일 텍스트라 프론트가 마크다운 등으로 파싱해야 한다. 보류.
- 서비스 이용약관 전체보기 API(`GET /terms/service`) — 정적 페이지/앱 내 하드코딩으로 대체. 보류.
