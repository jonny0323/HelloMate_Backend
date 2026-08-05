# API 명세서

HelloMate 백엔드 전체 엔드포인트 명세. 학생 앱 · 담당자 웹 · 관리자 콘솔 세 클라이언트가 같은 서버를 쓴다.

## 공통 규약

모든 엔드포인트에 공통으로 적용되므로 개별 API 표에서는 반복하지 않는다.

**응답 봉투**

모든 응답은 아래 형태로 감싸진다. 각 API의 `Response` 표는 `data` 안의 필드만 기술한다.

| 필드 | 타입 | 설명 |
|---|---|---|
| success | boolean | 성공 여부 |
| data | object / array / null | 실제 응답 본문 |
| meta | object / null | 페이지네이션 정보 (목록 API만) |
| error | object / null | 실패 시 `{code, message}` |

**JSON 표기**: 요청·응답 모두 snake_case. (예: `login_id`, `created_at`)

**인증**: `Authorization: Bearer {accessToken}` 헤더. 아래 경로만 인증 불필요 — `/auth/**`, `/universities/**`, `/docs/**`, `/swagger-ui/**`, `/v3/api-docs/**`. `/admin/**`은 `STAFF` 역할만 접근 가능하며, 학생 토큰으로 호출하면 403.

**페이지네이션 (2종)**

- 커서 방식(기본): 요청 `cursor`(직전 마지막 항목의 커서, 첫 페이지는 생략) + `limit`(기본 20) → `meta: {next_cursor, has_next, size}`
- 오프셋 방식(관리자 목록): 요청 `page`(기본 1) + `size` → `meta: {page, size, total}`

**번역**: `Accept-Language` 헤더를 보내면 지원 엔드포인트가 원문과 함께 `translated: {lang, text}`를 내려준다. 헤더가 없거나 원문 언어와 같으면 `null`.

**공통 에러**

| HTTP | 설명 |
|---|---|
| 400 | 요청 값 검증 실패 (`INVALID_INPUT`) |
| 401 | 토큰 없음 / 만료 / 위조 (`UNAUTHORIZED`) |
| 403 | 권한 없음 (`FORBIDDEN`) |
| 500 | 서버 내부 오류 (`INTERNAL_ERROR`) |

**주요 Enum 값**

| 이름 | 값 |
|---|---|
| student_type | `교환학생`, `정규과정생`, `어학당 수강생`, `한국인 대학생` |
| verification_status (학생) | `REGISTERED`, `DOC_PENDING`, `DOC_REJECTED`, `VERIFIED` |
| status (서류 심사) | `pending`, `approved`, `rejected` |
| document_type | `STUDENT_ID`, `ENROLLMENT_CERTIFICATE`, `ADMISSION_LETTER`, `TRANSCRIPT` |
| notice type | `urgent`, `normal` |
| notice status | `draft`, `sent` |
| audience mode | `all`, `group`, `individual` |
| notification category | `chat_direct`, `chat_club`, `notice`, `community`, `club`, `honey_tip`, `system` |
| club card_state | `joinable`, `joined`, `closed` |
| sender_type (채팅) | `user`, `teacher` |
| edit request status | `pending`, `approved`, `rejected` |
| report status | `pending`, `resolved`, `rejected` |

---

## 학생 인증

### 아이디 중복 확인

- **Method**: POST
- **URL**: /auth/students/check-login-id
- **설명**: 회원가입 1단계에서 아이디 사용 가능 여부 확인
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| login_id | string | O | 공백 불가 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| available | boolean | 항상 true (중복이면 409로 떨어짐) |
| message | string | 안내 문구 |

#### Error

| HTTP | 설명 |
|---|---|
| 409 | 이미 사용 중인 아이디 |

---

### 이메일 중복 확인

- **Method**: POST
- **URL**: /auth/students/check-email
- **설명**: 가입 가능한 이메일인지 확인
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| email | string | O | 이메일 형식 |

#### Response (200)

아이디 중복 확인과 동일 (`available`, `message`).

#### Error

| HTTP | 설명 |
|---|---|
| 409 | 이미 가입된 계정 |

---

### 가입용 인증번호 발송

- **Method**: POST
- **URL**: /auth/students/email-verifications
- **설명**: 학교 이메일로 6자리 인증번호 발송
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| email | string | O | 학교 공식 이메일만 허용 |

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 학교 공식 이메일이 아님 |
| 429 | 재발송 대기 시간이 지나지 않음 |

---

### 가입용 인증번호 확인

- **Method**: POST
- **URL**: /auth/students/email-verifications/confirm
- **설명**: 발송된 인증번호 검증
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| email | string | O | 이메일 형식 |
| code | string | O | 숫자 6자리 |

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 인증번호 불일치 / 만료 |
| 429 | 인증 시도 횟수 초과 (재발송 필요) |

---

### 회원가입

- **Method**: POST
- **URL**: /auth/students/signup
- **설명**: 3단계 입력값을 한 번에 제출해 학생 계정 생성
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| login_id | string | O | 영문 소문자 · 숫자 · `_` 조합 4~20자 |
| email | string | O | 이메일 형식 |
| name | string | O | |
| password | string | O | 8자 이상, 영문+숫자 포함 |
| country | string | O | 국가 |
| language | string | O | 사용 언어 |
| student_type | string | O | student_type enum |
| major | string | X | 학과 |
| grade | string | X | 학년 |
| birth_year | number | X | 1900~2100 |
| university_id | string | O | 학교 UUID |
| terms_agreed | boolean | O | `true`여야 함 |
| privacy_agreed | boolean | O | `true`여야 함 |

#### Response (201)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 생성된 학생 UUID |
| name | string | 이름 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 존재하지 않는 학교 |
| 409 | 아이디 또는 이메일 중복 |

---

### 로그인

- **Method**: POST
- **URL**: /auth/students/login
- **설명**: 아이디/비밀번호 로그인, 토큰 발급
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| login_id | string | O | |
| password | string | O | |
| auto_login | boolean | X | true면 리프레시 토큰 30일, 아니면 1일 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| access_token | string | 액세스 토큰 |
| refresh_token | string | 리프레시 토큰 |

#### Error

| HTTP | 설명 |
|---|---|
| 401 | 아이디 또는 비밀번호 불일치 |
| 403 | 탈퇴한 계정 |
| 423 | 로그인 5회 실패로 계정 잠김 |

---

### 토큰 재발급

- **Method**: POST
- **URL**: /auth/students/refresh
- **설명**: 리프레시 토큰으로 액세스 토큰 재발급
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| refresh_token | string | O | |

#### Response (200)

로그인과 동일 (`access_token`, `refresh_token`).

#### Error

| HTTP | 설명 |
|---|---|
| 401 | 리프레시 토큰이 유효하지 않거나 만료됨 |

---

### 로그아웃

- **Method**: POST
- **URL**: /auth/students/logout
- **설명**: 저장된 리프레시 토큰 폐기
- **인증**: 필요

#### Request

없음.

#### Response (200)

`data`는 `null`.

---

### 비밀번호 재설정 — 인증번호 발송

- **Method**: POST
- **URL**: /auth/students/password-reset/email
- **설명**: 가입된 이메일로 재설정용 인증번호 발송
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| email | string | O | 이메일 형식 |

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 해당 이메일의 학생 없음 |
| 429 | 재발송 대기 시간이 지나지 않음 |

---

### 비밀번호 재설정 — 인증번호 확인

- **Method**: POST
- **URL**: /auth/students/password-reset/verify
- **설명**: 인증번호를 검증하고 재설정용 임시 토큰 발급
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| email | string | O | 이메일 형식 |
| code | string | O | 숫자 6자리 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| reset_token | string | 비밀번호 재설정에 사용할 임시 토큰 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 인증번호 불일치 / 만료 |
| 429 | 인증 시도 횟수 초과 |

---

### 비밀번호 재설정 — 변경

- **Method**: PATCH
- **URL**: /auth/students/password-reset
- **설명**: 임시 토큰으로 새 비밀번호 설정
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| reset_token | string | O | 인증번호 확인 단계에서 받은 토큰 |
| new_password | string | O | |

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 401 | 재설정 토큰 만료 또는 무효 |
| 404 | 학생 없음 |

---

## 담당자 인증

### 담당자 회원가입

- **Method**: POST
- **URL**: /auth/staff/signup
- **설명**: 부서별 1회용 초대 코드로 담당자 계정 생성 (SSO 없음)
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| invite_code | string | O | 부서별 1회용 코드 |
| email | string | O | 학교 이메일 |
| name | string | O | |
| position | string | O | 직위 |
| password | string | O | |

#### Response (201)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 담당자 UUID |
| name | string | 이름 |
| verified | boolean | 승인 여부 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 초대 코드가 무효하거나 이미 사용됨 |
| 409 | 이미 가입된 계정 |

---

### 담당자 로그인 / 재발급 / 로그아웃

- **Method**: POST
- **URL**: /auth/staff/login, /auth/staff/refresh, /auth/staff/logout
- **설명**: 학생 인증과 동일한 흐름. 로그인 식별자만 이메일이고 `auto_login`이 없다.
- **인증**: 로그인·재발급 불필요 / 로그아웃 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| email | string | O | login에서만 |
| password | string | O | login에서만 |
| refresh_token | string | O | refresh에서만 |

#### Response (200)

로그인·재발급은 `access_token`, `refresh_token`. 로그아웃은 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 401 | 이메일/비밀번호 불일치, 리프레시 토큰 무효 |
| 403 | 아직 승인되지 않은 담당자 계정 |

---

## 학생 프로필

### 내 프로필 조회

- **Method**: GET
- **URL**: /students/me
- **설명**: 마이페이지 기본 정보
- **인증**: 필요

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 학생 UUID |
| login_id | string | 아이디 |
| email | string | 이메일 |
| name | string | 이름 |
| country | string | 국가 |
| birth_year | number | 출생연도 |
| language | string | 사용 언어 |
| student_type | string | student_type enum |
| major | string | 학과 |
| grade | string | 학년 |
| university_name | string | 학교명 |
| verification_status | string | 학생 인증 상태 |
| verified | boolean | 인증 완료 여부 |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 학생 없음 |

---

### 내 프로필 수정

- **Method**: PATCH
- **URL**: /students/me
- **설명**: 전달한 필드만 부분 수정
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| name | string | X | |
| country | string | X | |
| birth_year | number | X | |
| language | string | X | |
| major | string | X | |
| grade | string | X | |

#### Response (200)

내 프로필 조회와 동일.

---

### 비밀번호 변경

- **Method**: PATCH
- **URL**: /students/me/password
- **설명**: 이메일 인증번호 확인 후 비밀번호 변경. 변경 시 기존 리프레시 토큰이 모두 폐기된다.
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| code | string | O | 숫자 6자리 (비밀번호 재설정용 인증번호 발송 API로 미리 받는다) |
| new_password | string | O | |

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 인증번호 불일치 / 만료 |
| 429 | 인증 시도 횟수 초과 |

---

### 회원 탈퇴

- **Method**: DELETE
- **URL**: /students/me
- **설명**: 계정을 탈퇴 상태로 전환 (작성한 글·댓글은 유지)
- **인증**: 필요

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 이미 탈퇴한 계정 |

---

## 재학 인증 (학생)

### 인증 서류 제출

- **Method**: POST
- **URL**: /students/me/verification-documents
- **설명**: 미리 업로드한 파일로 재학 인증 서류 제출
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| file_id | string | O | 파일 업로드 API로 받은 `file_id` |
| document_type | string | O | document_type enum |

#### Response (201)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 서류 UUID |
| student_id | string | 학생 UUID |
| student_name | string | 학생 이름 |
| file_url | string | 서류 파일 URL |
| document_type | string | 서류 종류 |
| status | string | `pending` / `approved` / `rejected` |
| reject_reason | string | 반려 사유 |
| reviewed_at | string | 심사 일시 |
| created_at | string | 제출 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 파일 없음 |

---

### 내 인증 서류 조회

- **Method**: GET
- **URL**: /students/me/verification-documents
- **설명**: 가장 최근 제출한 서류 1건
- **인증**: 필요

#### Response (200)

서류 제출 응답과 동일.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 제출한 서류 없음 |

---

### 내 인증 상태 조회

- **Method**: GET
- **URL**: /students/me/verification-documents/status
- **설명**: 인증 화면 분기용. 서류 미제출자도 200으로 응답한다.
- **인증**: 필요

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| verification_status | string | `REGISTERED` / `DOC_PENDING` / `DOC_REJECTED` / `VERIFIED` |
| verified | boolean | 인증 완료 여부 |
| latest_document | object | 최근 서류. 없으면 `null` |

---

## 공지사항 (학생)

### 공지 홈

- **Method**: GET
- **URL**: /notices/home
- **설명**: 상단 배너 캐러셀 + 최근 공지를 한 번에 조회
- **인증**: 필요

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| banners | array | 배너 목록: `id`, `title`, `department`, `banner_start_date`, `banner_end_date` |
| recent_notices | array | 최근 공지: `id`, `title`, `department`, `type`, `is_read`, `created_at` |

---

### 내 공지 목록

- **Method**: GET
- **URL**: /notices
- **설명**: 나에게 발송된 공지 목록 (커서 페이지네이션)
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| group_by | query string | X | `department`이면 부서별로 묶은 객체로 반환, 그 외엔 배열 |
| q | query string | X | 제목/내용 검색어 |
| cursor | query string | X | 다음 페이지 커서 |
| limit | query number | X | 기본 20 |

#### Response (200)

`data`는 공지 요약 배열 (`group_by=department`면 부서명을 키로 하는 객체).

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 공지 UUID |
| title | string | 제목 |
| department | string | 발신 부서 |
| type | string | `urgent` / `normal` |
| is_read | boolean | 열람 여부 |
| created_at | string | 작성 일시 |

---

### 공지 상세

- **Method**: GET
- **URL**: /notices/{noticeId}
- **설명**: 공지 상세 조회. 조회 시 자동으로 읽음 처리된다.
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| noticeId | path string | O | 공지 UUID |
| Accept-Language | header | X | 지정 시 `translated` 채워짐 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 공지 UUID |
| title | string | 제목 |
| content | string | 본문 |
| translated | object | `{lang, text}` 또는 `null` |
| department | string | 발신 부서 |
| type | string | `urgent` / `normal` |
| is_read | boolean | 열람 여부 |
| files | array | 첨부파일: `id`, `filename`, `file_url` |
| created_at | string | 작성 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 공지 없음 또는 내게 발송되지 않은 공지 |

---

### 공지 읽음 처리

- **Method**: PATCH
- **URL**: /notices/{noticeId}/read
- **설명**: 상세를 열지 않고 목록에서 바로 읽음 처리
- **인증**: 필요

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 공지 없음 |

---

### 공지 미열람 수

- **Method**: GET
- **URL**: /notices/unread-count
- **설명**: 하단 탭 뱃지용 미열람 공지 수
- **인증**: 필요

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| count | number | 미열람 공지 수 |

---

### 공지 첨부파일 목록

- **Method**: GET
- **URL**: /notices/{noticeId}/files
- **설명**: 공지에 붙은 첨부파일만 조회
- **인증**: 필요

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 첨부 UUID |
| filename | string | 파일명 |
| file_url | string | 다운로드 URL |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 공지 없음 |

---

## 공지사항 (담당자)

`/admin/notices/**`는 모두 STAFF 전용이며 인증이 필요하다.

### 수신자 수 미리보기

- **Method**: POST
- **URL**: /admin/notices/audience/count
- **설명**: 발송 전 "N명에게 발송됩니다" 문구용. 중복 학생을 제거한 실제 인원을 계산한다.
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| mode | string | O | `all` / `group` / `individual` |
| country_codes | array | X | `group` 모드. 축 안에서 OR |
| majors | array | X | `group` 모드. 국가와는 AND로 결합 |
| student_ids | array | X | `individual` 모드 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| recipient_count | number | 실제 수신자 수 |
| audience_label | string | 화면 표시용 대상 요약 문구 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 대상 그룹 미선택, 조건에 해당하는 수신자 없음 |

---

### 공지 작성·발송

- **Method**: POST
- **URL**: /admin/notices
- **설명**: 공지를 생성하고 즉시 발송. 발신 부서는 작성자 소속으로 서버가 확정한다(요청으로 받지 않음).
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| title | string | O | 최대 255자 |
| content | string | O | |
| type | string | O | `urgent` / `normal` |
| audience | object | O | 수신자 수 미리보기와 동일한 구조 |
| files | array | X | 업로드된 `file_id` 목록. 최대 5개 |
| banner_start_date | string | X | 배너 노출 시작일 (yyyy-MM-dd) |
| banner_end_date | string | X | 배너 노출 종료일 |

#### Response (201)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 공지 UUID |
| total_recipient_count | number | 발송된 수신자 수 |
| sent_at | string | 발송 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 수신자 없음, 첨부 5개 초과, 지원하지 않는 첨부 형식 |
| 404 | 첨부 파일 없음 |

---

### 발송함 목록

- **Method**: GET
- **URL**: /admin/notices
- **설명**: 발송된 공지 목록 (오프셋 페이지네이션)
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| department | query string | X | 부서 필터 |
| keyword | query string | X | 제목 검색어 |
| page | query number | X | 기본 1 |
| size | query number | X | 기본 20 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 공지 UUID |
| title | string | 제목 |
| department | string | 발신 부서 |
| type | string | `urgent` / `normal` |
| audience_label | string | 수신 대상 요약 |
| total_recipient_count | number | 수신자 수 |
| read_count | number | 열람 수 |
| read_rate | number | 열람률 (0~1) |
| resend_count | number | 재발송 횟수 |
| can_manage | boolean | 재발송·삭제 가능 여부 (타 부서 공지는 false) |
| sent_at | string | 발송 일시 |

---

### 공지 상세 (담당자)

- **Method**: GET
- **URL**: /admin/notices/{noticeId}
- **설명**: 발송 통계를 포함한 상세
- **인증**: 필요 (STAFF)

#### Response (200)

발송함 목록 필드에 더해:

| 필드 | 타입 | 설명 |
|---|---|---|
| content | string | 본문 |
| status | string | `draft` / `sent` |
| last_resent_at | string | 마지막 재발송 일시 |
| banner_start_date | string | 배너 시작일 |
| banner_end_date | string | 배너 종료일 |
| files | array | 첨부파일 목록 |

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 다른 학교의 공지 |
| 404 | 공지 없음 |

---

### 공지 수정

- **Method**: PATCH
- **URL**: /admin/notices/{noticeId}
- **설명**: 발송 후 오탈자 수정용. 수신자 목록은 바뀌지 않는다.
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| title | string | X | 최대 255자 |
| content | string | X | |
| type | string | X | `urgent` / `normal` |
| banner_start_date | string | X | |
| banner_end_date | string | X | |

#### Response (200)

공지 상세(담당자)와 동일.

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 다른 부서가 발송한 공지 |
| 404 | 공지 없음 |

---

### 공지 삭제

- **Method**: DELETE
- **URL**: /admin/notices/{noticeId}
- **설명**: 발송한 공지 삭제
- **인증**: 필요 (STAFF)

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 다른 부서가 발송한 공지 |
| 404 | 공지 없음 |

---

### 수신자별 열람 현황

- **Method**: GET
- **URL**: /admin/notices/{noticeId}/receptions
- **설명**: 누가 읽었는지 목록 (오프셋 페이지네이션, `size` 기본 50)
- **인증**: 필요 (STAFF)

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| student_id | string | 학생 UUID |
| student_name | string | 이름 |
| country | string | 국가 |
| is_read | boolean | 열람 여부 |
| read_at | string | 열람 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 다른 학교의 공지 |
| 404 | 공지 없음 |

---

### 재발송

- **Method**: POST
- **URL**: /admin/notices/{noticeId}/resend
- **설명**: 미열람자에게만 다시 알림 발송. 24시간에 1회로 제한된다.
- **인증**: 필요 (STAFF)

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| notified_count | number | 재발송된 인원 수 |

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 다른 부서가 발송한 공지 |
| 404 | 공지 없음 |
| 409 | 아직 발송되지 않은 공지 |
| 429 | 24시간 내 재발송 이력 있음 |

---

### 임시저장 목록 / 저장 / 수정

- **Method**: GET, POST, PATCH
- **URL**: /admin/notices/drafts, /admin/notices/drafts/{noticeId}
- **설명**: 작성 중인 공지 초안. 수신 대상은 발송 시점에 정하므로 초안에 담지 않는다.
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| title | string | X | 최대 255자. 작성 중이라 비어 있을 수 있음 |
| content | string | X | |
| type | string | X | `urgent` / `normal` |

#### Response (200 / 201)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 초안 UUID |
| title | string | 제목 |
| content | string | 본문 |
| type | string | 공지 유형 |
| updated_at | string | 최종 저장 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 초안 없음 |
| 409 | 이미 발송된 공지 (수정 불가) |

---

### 임시저장 발송

- **Method**: POST
- **URL**: /admin/notices/drafts/{noticeId}/send
- **설명**: 초안에 수신 대상을 붙여 발송
- **인증**: 필요 (STAFF)

#### Request

공지 작성·발송과 동일한 본문.

#### Response (200)

공지 작성·발송과 동일 (`id`, `total_recipient_count`, `sent_at`).

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 수신자 없음, 첨부 제한 위반 |
| 404 | 초안 없음 |
| 409 | 이미 발송된 공지 |

---

## 커뮤니티

### 게시글 목록

- **Method**: GET
- **URL**: /posts
- **설명**: 같은 학교 게시글 목록 (커서 페이지네이션)
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| q | query string | X | 제목/내용 검색어 |
| cursor | query string | X | 다음 페이지 커서 |
| limit | query number | X | 기본 20 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 게시글 UUID |
| author_name | string | 익명이면 "익명 N", 실명이면 작성자 이름 |
| anonymous | boolean | 익명 여부 |
| title | string | 제목 |
| content | string | 본문 |
| original_lang | string | 원문 언어 |
| like_count | number | 좋아요 수 |
| comment_count | number | 댓글 수 |
| created_at | string | 작성 일시 |

---

### 내가 쓴 글 / 내가 쓴 댓글

- **Method**: GET
- **URL**: /posts/mine, /comments/mine
- **설명**: 마이페이지의 내 활동 목록 (커서 페이지네이션)
- **인증**: 필요

#### Request

`cursor`, `limit` (게시글 목록과 동일)

#### Response (200)

`/posts/mine`은 게시글 목록과 동일한 필드.

`/comments/mine`:

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 댓글 UUID |
| post_id | string | 원글 UUID |
| parent_comment_id | string | 대댓글이면 부모 댓글 UUID |
| anon_name | string | 익명 표시명 |
| content | string | 내용 |
| original_lang | string | 원문 언어 |
| like_count | number | 좋아요 수 |
| created_at | string | 작성 일시 |

---

### 게시글 작성

- **Method**: POST
- **URL**: /posts
- **설명**: 게시글 등록
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| title | string | O | 최대 255자 |
| content | string | O | 최대 5000자 |
| anonymous | boolean | X | 생략하면 익명(`true`)으로 처리 |

#### Response (201)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 게시글 UUID |
| author_name | string | 표시 이름 |
| anonymous | boolean | 익명 여부 |
| created_at | string | 작성 일시 |

---

### 게시글 상세

- **Method**: GET
- **URL**: /posts/{postId}
- **설명**: 게시글 본문 + 댓글 목록
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| postId | path string | O | 게시글 UUID |
| Accept-Language | header | X | 지정 시 본문·댓글에 `translated` 채워짐 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 게시글 UUID |
| author_name | string | 표시 이름 |
| anonymous | boolean | 익명 여부 |
| mine | boolean | 내가 쓴 글인지 |
| title | string | 제목 |
| original_lang | string | 원문 언어 |
| content | string | 본문 |
| translated | object | `{lang, text}` 또는 `null` |
| like_count | number | 좋아요 수 |
| liked_by_me | boolean | 내가 좋아요 눌렀는지 |
| comment_count | number | 댓글 수 |
| comments | array | 댓글 목록 (아래 참고) |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 게시글 없음 |

---

### 게시글 수정 / 삭제

- **Method**: PATCH, DELETE
- **URL**: /posts/{postId}
- **설명**: 작성자 본인만 가능 (삭제는 STAFF도 가능)
- **인증**: 필요

#### Request (PATCH)

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| title | string | O | 최대 255자 |
| content | string | O | 최대 5000자 |

#### Response (200)

PATCH는 게시글 요약 필드, DELETE는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 작성자가 아님 |
| 404 | 게시글 없음 |

---

### 게시글 좋아요 / 취소

- **Method**: POST, DELETE
- **URL**: /posts/{postId}/like
- **설명**: 좋아요 등록 및 취소
- **인증**: 필요

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 좋아요를 누르지 않은 상태에서 취소 |
| 404 | 게시글 없음 |
| 409 | 이미 좋아요를 누름 |

---

### 댓글 목록

- **Method**: GET
- **URL**: /posts/{postId}/comments
- **설명**: 게시글의 댓글만 조회 (상세 응답의 `comments`와 동일)
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| Accept-Language | header | X | 지정 시 `translated` 채워짐 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 댓글 UUID |
| parent_comment_id | string | 대댓글이면 부모 UUID, 아니면 `null` |
| anon_name | string | 익명 표시명 |
| content | string | 내용 |
| original_lang | string | 원문 언어 |
| translated | object | `{lang, text}` 또는 `null` |
| like_count | number | 좋아요 수 |
| liked_by_me | boolean | 내가 좋아요 눌렀는지 |
| created_at | string | 작성 일시 |

---

### 댓글 작성

- **Method**: POST
- **URL**: /posts/{postId}/comments
- **설명**: 댓글 또는 대댓글 작성. 대댓글에 다시 답글은 달 수 없다(2단계까지).
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| content | string | O | 공백 불가 |
| parent_comment_id | string | X | 있으면 대댓글 |

#### Response (201)

댓글 목록 항목과 동일.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 대댓글에 답글 시도 |
| 404 | 게시글 또는 부모 댓글 없음 |

---

### 댓글 삭제 / 좋아요 / 좋아요 취소

- **Method**: DELETE, POST, DELETE
- **URL**: /posts/{postId}/comments/{commentId}, /posts/{postId}/comments/{commentId}/like
- **설명**: 삭제는 작성자 본인 또는 STAFF만 가능
- **인증**: 필요

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 좋아요를 누르지 않은 상태에서 취소 |
| 403 | 작성자가 아님 |
| 404 | 댓글 없음 |
| 409 | 이미 좋아요를 누름 |

---

### 게시글 / 댓글 신고

- **Method**: POST
- **URL**: /posts/{postId}/report, /posts/{postId}/comments/{commentId}/report
- **설명**: 신고 접수. 접수된 건은 관리자 모더레이션 큐로 넘어간다.
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| reason | string | O | 신고 사유 |

#### Response (201)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 게시글 또는 댓글 없음 |

---

## 클럽

### 클럽 목록

- **Method**: GET
- **URL**: /clubs
- **설명**: 같은 학교 클럽 목록
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | query string | X | `open`이면 모집 중만, 그 외 값이면 마감된 것만 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 클럽 UUID |
| title | string | 클럽명 |
| introduction | string | 소개 |
| creator_id | string | 클럽장 UUID |
| creator_name | string | 클럽장 이름 |
| max_members | number | 최대 인원 |
| current_members | number | 현재 인원 |
| remaining_seats | number | 남은 자리 |
| full | boolean | 정원 마감 여부 |
| card_state | string | `joinable` / `joined` / `closed` |
| deadline | string | 모집 마감일 |
| created_at | string | 생성 일시 |

---

### 내 클럽 목록 / 클럽 상세

- **Method**: GET
- **URL**: /clubs/mine, /clubs/{clubId}
- **설명**: 참여 중인 클럽 목록, 단건 상세
- **인증**: 필요

#### Response (200)

클럽 목록 항목과 동일.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 클럽 없음 |

---

### 클럽 개설

- **Method**: POST
- **URL**: /clubs
- **설명**: 클럽 생성. 생성자가 클럽장이 된다.
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| title | string | O | 클럽명 |
| introduction | string | O | 소개 |
| max_members | number | O | 1 이상 |
| deadline | string | O | 미래 날짜여야 함 (yyyy-MM-dd) |

#### Response (201)

클럽 목록 항목과 동일.

---

### 클럽 수정 / 삭제

- **Method**: PATCH, DELETE
- **URL**: /clubs/{clubId}
- **설명**: 클럽장만 가능 (삭제는 STAFF도 가능)
- **인증**: 필요

#### Request (PATCH)

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| title | string | X | |
| introduction | string | X | |
| max_members | number | X | |
| deadline | string | X | |

#### Response (200)

PATCH는 클럽 정보, DELETE는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 클럽장이 아님 |
| 404 | 클럽 없음 |

---

### 클럽 참여 / 탈퇴

- **Method**: POST, DELETE
- **URL**: /clubs/{clubId}/join, /clubs/{clubId}/leave
- **설명**: 참여 및 탈퇴. 클럽장은 위임하거나 클럽을 삭제해야 나갈 수 있다.
- **인증**: 필요

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 클럽 멤버가 아님, 클럽장은 탈퇴 불가 |
| 404 | 클럽 없음 |
| 409 | 이미 참여 중, 정원 초과, 모집 마감 |

---

### 클럽장 위임

- **Method**: PATCH
- **URL**: /clubs/{clubId}/owner
- **설명**: 다른 멤버에게 클럽장 권한 이전
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| new_creator_id | string | O | 위임 대상 학생 UUID (클럽 멤버여야 함) |

#### Response (200)

클럽 정보.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 위임 대상이 클럽 멤버가 아님 |
| 403 | 클럽장이 아님 |
| 404 | 클럽 없음 |

---

### 클럽 멤버 목록

- **Method**: GET
- **URL**: /clubs/{clubId}/members
- **설명**: 참여 멤버 조회 (멤버만 가능)
- **인증**: 필요

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| student_id | string | 학생 UUID |
| student_name | string | 이름 |
| joined_at | string | 참여 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 클럽 멤버가 아님 |
| 404 | 클럽 없음 |

---

### 클럽 단체 채팅

- **Method**: GET, POST
- **URL**: /clubs/{clubId}/messages
- **설명**: 클럽 멤버 간 단체 대화. 조회는 커서 페이지네이션.
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| content | string | O | POST 본문. 공백 불가 |
| cursor | query string | X | GET. 다음 페이지 커서 |
| limit | query number | X | GET. 기본 20 |

#### Response (200 / 201)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 메시지 UUID |
| sender_id | string | 보낸 사람 UUID |
| sender_name | string | 보낸 사람 이름 |
| content | string | 내용 |
| created_at | string | 전송 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 클럽 멤버가 아님 |
| 404 | 클럽 없음 |

---

## 정보글 (허니팁)

### 정보글 목록 / 상세

- **Method**: GET
- **URL**: /honey-tips, /honey-tips/{honeyTipId}
- **설명**: 비자·보험 등 생활 정보 안내. 상세 조회 시 조회수가 증가한다.
- **인증**: 목록 필요 / 상세 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| category | query string | X | 목록에서 카테고리 필터 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 정보글 UUID |
| category | string | 카테고리 |
| title | string | 제목 |
| content | string | 본문 |
| tip_message | string | 팁 한 줄 |
| steps | array | 단계 카드: `order`, `title`, `description`, `tags` |
| estimated_fee | string | 예상 비용 |
| processing_period | string | 처리 기간 |
| external_link | string | 외부 링크 |
| disclaimer | string | 고정 면책 문구 (항상 내려감, 클라이언트 상시 노출 필요) |
| view_count | number | 조회수 |
| created_at | string | 작성 일시 |
| updated_at | string | 수정 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 정보글 없음 |

---

### 정보 수정 요청 (학생)

- **Method**: POST
- **URL**: /honey-tips/{honeyTipId}/edit-requests
- **설명**: 내용이 틀렸을 때 학생이 수정 제안을 보낸다
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| content | string | O | 수정 제안 내용 |

#### Response (201)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 정보글 없음 |

---

### 정보글 등록 / 수정 (담당자)

- **Method**: POST, PATCH
- **URL**: /admin/honey-tips, /admin/honey-tips/{honeyTipId}
- **설명**: 정보글 작성 및 수정
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| category | string | POST만 O | 카테고리 |
| title | string | POST만 O | 제목 |
| content | string | POST만 O | 본문 |
| tip_message | string | X | 최대 300자 |
| steps | array | X | 단계 카드 목록 |
| estimated_fee | string | X | 최대 50자 |
| processing_period | string | X | 최대 50자 |
| external_link | string | X | 최대 500자 |

#### Response (200 / 201)

정보글 상세와 동일.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 정보글 없음 (PATCH) |

---

### 수정 요청 목록 (담당자)

- **Method**: GET
- **URL**: /admin/honey-tips/edit-requests, /admin/honey-tips/{honeyTipId}/edit-requests
- **설명**: 전체 수정 요청 큐(오프셋 페이지네이션) 또는 특정 정보글의 요청 목록
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | query string | X | `pending` / `approved` / `rejected` |
| page | query number | X | 기본 1 (전체 큐만) |
| size | query number | X | 기본 20 (전체 큐만) |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 요청 UUID |
| honey_tip_id | string | 대상 정보글 UUID |
| honey_tip_title | string | 정보글 제목 |
| requester_name | string | 요청자 이름 |
| content | string | 요청 내용 |
| status | string | 처리 상태 |
| created_at | string | 요청 일시 |

---

### 수정 요청 처리 (담당자)

- **Method**: PATCH
- **URL**: /admin/honey-tips/edit-requests/{reqId}
- **설명**: 수정 요청 승인 또는 반려
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | string | O | `approved` / `rejected` |

#### Response (200)

수정 요청 목록 항목과 동일.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 수정 요청 없음 |

---

## 1:1 채팅

### 대화 시작 (학생 → 담당자)

- **Method**: POST
- **URL**: /chats/threads
- **설명**: 담당자와의 스레드를 열고 첫 메시지 전송. 이미 스레드가 있으면 재사용한다.
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| teacher_id | string | O | 담당자 UUID |
| notice_id | string | X | 특정 공지 문의로 시작하는 경우 |
| message | string | O | 첫 메시지 |

#### Response (201)

| 필드 | 타입 | 설명 |
|---|---|---|
| thread_id | string | 스레드 UUID |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 담당자 또는 공지 없음 |

---

### 대화 시작 (담당자 → 학생)

- **Method**: POST
- **URL**: /admin/chats/threads
- **설명**: 담당자가 학생 목록에서 골라 먼저 대화를 시작
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| student_id | string | O | 학생 UUID |
| message | string | O | 최대 2000자 |

#### Response (201)

| 필드 | 타입 | 설명 |
|---|---|---|
| thread_id | string | 스레드 UUID |

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 다른 학교 학생 |
| 404 | 학생 없음 |

---

### 내 대화 목록

- **Method**: GET
- **URL**: /chats/threads
- **설명**: 학생·담당자 공용. 상대 정보와 마지막 메시지를 함께 준다.
- **인증**: 필요

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| thread_id | string | 스레드 UUID |
| counterpart_id | string | 상대방 UUID |
| counterpart_name | string | 상대방 이름 |
| last_message | string | 마지막 메시지 |
| last_message_at | string | 마지막 메시지 일시 |
| unread | boolean | 안 읽은 메시지 존재 여부 |
| notice_id | string | 공지 문의로 시작했으면 해당 공지 UUID |
| initiated_by | string | `student` / `staff` |

---

### 메시지 목록

- **Method**: GET
- **URL**: /chats/threads/{threadId}/messages
- **설명**: 스레드 메시지 조회 (커서 페이지네이션)
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| cursor | query string | X | 다음 페이지 커서 |
| limit | query number | X | 기본 20 |
| Accept-Language | header | X | 지정 시 `translated` 채워짐 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 메시지 UUID |
| sender_type | string | `user` / `teacher` |
| content | string | 원문 |
| original_lang | string | 원문 언어 |
| translated | object | `{lang, text}` 또는 `null` |
| created_at | string | 전송 일시 |

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 내 스레드가 아님 |
| 404 | 스레드 없음 |

---

### 메시지 전송

- **Method**: POST
- **URL**: /chats/threads/{threadId}/messages
- **설명**: 스레드에 메시지 추가
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| content | string | O | 공백 불가 |

#### Response (201)

메시지 목록 항목과 동일 (`translated`는 `null`).

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 내 스레드가 아님 |
| 404 | 스레드 없음 |

---

### 대화 읽음 처리 / 미확인 수

- **Method**: PATCH, GET
- **URL**: /chats/threads/{threadId}/read, /chats/unread-count
- **설명**: 스레드 읽음 처리, 안 읽은 메시지 총 개수
- **인증**: 필요

#### Response (200)

읽음 처리는 `null`, 미확인 수는 `count` (number).

#### Error

| HTTP | 설명 |
|---|---|
| 403 | 내 스레드가 아님 |
| 404 | 스레드 없음 |

---

## 알림

### 알림 피드

- **Method**: GET
- **URL**: /notifications
- **설명**: 알림 목록 (커서 페이지네이션)
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| category | query string | X | notification category enum |
| cursor | query string | X | 다음 페이지 커서 |
| limit | query number | X | 기본 20 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 알림 UUID |
| category | string | 알림 카테고리 |
| title | string | 알림 문구 |
| link_type | string | 이동 대상 종류 (예: `chat`, `notice`) |
| link_id | string | 이동 대상 UUID |
| read | boolean | 읽음 여부 |
| created_at | string | 생성 일시 |

---

### 알림 읽음 처리

- **Method**: PATCH
- **URL**: /notifications/{notificationId}/read, /notifications/read-all
- **설명**: 개별 읽음 처리 / 전체 읽음 처리
- **인증**: 필요

#### Response (200)

개별은 `null`, 전체 읽음은 갱신된 `count` (number).

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 알림 없음 |

---

### 미확인 알림 수

- **Method**: GET
- **URL**: /notifications/unread-count
- **설명**: 뱃지용 미확인 알림 수
- **인증**: 필요

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| count | number | 미확인 알림 수 |

---

### 알림 설정 조회 / 변경

- **Method**: GET, PATCH
- **URL**: /notifications/settings
- **설명**: 카테고리별 수신 여부 관리. `system`은 필수라 끌 수 없다.
- **인증**: 필요

#### Request (PATCH)

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| settings | array | O | 최소 1건 이상 |
| settings[].category | string | O | notification category enum |
| settings[].enabled | boolean | O | 수신 여부 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| category | string | 카테고리 |
| enabled | boolean | 수신 여부 |
| required | boolean | 필수 카테고리라 끌 수 없는지 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 필수 카테고리(`system`)를 끄려고 함 |

---

## 통합 검색

### 검색

- **Method**: GET
- **URL**: /search
- **설명**: 공지와 정보글을 한 번에 검색
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| q | query string | O | 검색어 |
| types | query string | X | 쉼표 구분. 생략 시 `notice,honey_tip` 모두 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| results | array | 검색 결과 배열 |
| results[].type | string | `notice` / `honey_tip` |
| results[].id | string | 대상 UUID |
| results[].department | string | 공지일 때만 채워짐 |
| results[].category | string | 정보글일 때만 채워짐 |
| results[].title | string | 제목 |
| results[].snippet | string | 본문 발췌 |

---

## 번역

### 일반 번역

- **Method**: POST
- **URL**: /translations
- **설명**: 텍스트 번역. 현재 스텁 구현이며 NLLB-200 연동 예정.
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| text | string | O | 원문 |
| target_lang | string | O | 목표 언어 코드 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| translated_text | string | 번역 결과 |
| detected_source_lang | string | 감지된 원문 언어 |
| model | string | 사용 모델 식별자 |

---

### 공문 번역

- **Method**: POST
- **URL**: /translations/official
- **설명**: 공문서용 번역. 사람 검토 필요 여부와 신뢰도를 함께 준다.
- **인증**: 필요

#### Request

일반 번역과 동일.

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| translated_text | string | 번역 결과 |
| model | string | 사용 모델 식별자 |
| needs_human_review | boolean | 사람 검토 필요 여부 |
| confidence | number | 신뢰도 |

---

## 파일

### 업로드 URL 발급

- **Method**: POST
- **URL**: /files/presigned-url
- **설명**: 업로드 URL과 `file_id`를 발급받은 뒤, 클라이언트가 `upload_url`로 직접 올리고 `file_id`를 다른 API(공지 첨부, 재학 인증 서류 등)에 전달한다. 현재 로컬 스텁 구현이며 S3 연동 예정.
- **인증**: 필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| filename | string | O | 원본 파일명 |
| content_type | string | O | MIME 타입 |
| purpose | string | O | 용도 구분자. 허용 값 목록은 **확인 필요** (현재 서버에서 검증하지 않음) |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| upload_url | string | 실제 업로드할 URL |
| file_id | string | 파일 UUID |
| file_url | string | 업로드 후 접근 URL |

---

## 학교 / 학과

### 학과 검색

- **Method**: GET
- **URL**: /universities/{universityId}/majors
- **설명**: 회원가입 학교 정보 단계의 학과 자동완성
- **인증**: 불필요

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| universityId | path string | O | 학교 UUID |
| query | query string | X | 학과명 부분 검색어. 생략 시 전체 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 학과 UUID |
| name | string | 학과명 |

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 존재하지 않는 학교 |

---

## 관리자 콘솔

`/admin/**`은 모두 STAFF 전용이다.

### 대시보드

- **Method**: GET
- **URL**: /admin/dashboard
- **설명**: 상단 통계 카드, 최근 공지, 답변 대기 스레드를 한 번에 조회
- **인증**: 필요 (STAFF)

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| stats.sent_notice_count | number | 발송 공지 수 |
| stats.average_read_rate | number | 가중 평균 열람률 (0~1) |
| stats.active_student_count | number | 활성 학생 수 |
| stats.pending_reply_count | number | 답변 대기 스레드 수 |
| recent_notices | array | `id`, `department`, `title`, `read_rate`, `sent_at` |
| unanswered_threads | array | `thread_id`, `student_id`, `student_name`, `last_message`, `last_message_at` |

---

### 학생 목록

- **Method**: GET
- **URL**: /admin/students
- **설명**: 학생 디렉토리 검색 (오프셋 페이지네이션)
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| keyword | query string | X | 이름/이메일 검색어 |
| country | query string | X | 국가 필터 |
| major | query string | X | 학과 필터 |
| grade | query string | X | 학년 필터 |
| page | query number | X | 기본 1 |
| size | query number | X | 기본 20 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 학생 UUID |
| name | string | 이름 |
| email | string | 이메일 |
| country | string | 국가 |
| student_type | string | student_type enum |
| major | string | 학과 |
| grade | string | 학년 |

---

### 공지 대상 그룹 목록

- **Method**: GET
- **URL**: /admin/students/target-groups
- **설명**: 공지 작성 화면의 '학과 · 국가별' 탭 카드. `group_key`를 그대로 `audience.country_codes` / `audience.majors`에 실어 보내야 한다.
- **인증**: 필요 (STAFF)

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| group_key | string | 요청에 되돌려 보낼 키 |
| label | string | 화면 표시용 라벨 |
| type | string | `COUNTRY` / `MAJOR` |
| count | number | 해당 그룹 인원 수 |

---

### 학생 상세

- **Method**: GET
- **URL**: /admin/students/{studentId}
- **설명**: 학생 프로필 단건 조회
- **인증**: 필요 (STAFF)

#### Response (200)

학생 프로필 조회(`/students/me`)와 동일.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 학생 없음 |

---

### 재학 인증 서류 목록

- **Method**: GET
- **URL**: /admin/verification-documents
- **설명**: 심사 대기/처리된 서류 목록 (오프셋 페이지네이션)
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | query string | X | `pending` / `approved` / `rejected`. 생략 시 전체 |
| page | query number | X | 기본 1 |
| size | query number | X | 기본 20 |

#### Response (200)

재학 인증 서류 응답과 동일한 필드 배열.

---

### 재학 인증 서류 심사

- **Method**: PATCH
- **URL**: /admin/verification-documents/{documentId}
- **설명**: 서류 승인 또는 반려
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| status | string | O | `approved` / `rejected` |
| reject_reason | string | 조건부 | `rejected`일 때 필수. 최대 500자 |

#### Response (200)

재학 인증 서류 응답과 동일.

#### Error

| HTTP | 설명 |
|---|---|
| 400 | 반려인데 사유 누락 |
| 404 | 서류 없음 |

---

### 신고 게시글 큐

- **Method**: GET
- **URL**: /admin/posts
- **설명**: 신고 접수된 게시글 목록 (오프셋 페이지네이션)
- **인증**: 필요 (STAFF)

#### Request

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| flagged | query boolean | X | 기본 false. 현재 서버 조회 조건에 반영되지 않음 — **확인 필요** |
| page | query number | X | 기본 1 |
| size | query number | X | 기본 20 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| report_id | string | 신고 UUID |
| post_id | string | 게시글 UUID |
| post_title | string | 게시글 제목 |
| reason | string | 신고 사유 |
| status | string | `pending` / `resolved` / `rejected` |
| reported_at | string | 신고 일시 |

---

### 게시글 강제 삭제

- **Method**: DELETE
- **URL**: /admin/posts/{postId}
- **설명**: 모더레이션 목적의 게시글 삭제
- **인증**: 필요 (STAFF)

#### Response (200)

`data`는 `null`.

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 게시글 없음 |

---

### 담당자 내 프로필 조회 / 수정

- **Method**: GET, PATCH
- **URL**: /admin/staff/me
- **설명**: 담당자 프로필. 부서는 초대 코드로 정해지므로 변경할 수 없다.
- **인증**: 필요 (STAFF)

#### Request (PATCH)

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| name | string | X | 최대 100자 |
| position | string | X | 최대 100자 |

#### Response (200)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | string | 담당자 UUID |
| email | string | 이메일 |
| name | string | 이름 |
| position | string | 직위 |
| department | string | 소속 부서 (수정 불가) |
| verified | boolean | 승인 여부 |
| university_name | string | 학교명 |

#### Error

| HTTP | 설명 |
|---|---|
| 404 | 담당자 없음 |
