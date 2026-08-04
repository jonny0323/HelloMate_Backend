# 학생 앱 화면 기준 API 정리

`docs/resources/Hellomate.png` 와이어프레임(학생 앱: 로그인·회원가입·공지사항·커뮤니티·클럽·정보·알람·마이페이지 8개 섹션)을
기준으로, 화면에 필요한 API를 현재 구현 상태와 대조해 정리한다. 로고 시스템 섹션은 디자인 자산이라 API와
무관해 제외했고, 담당자 웹/관리자 콘솔 화면은 이미지에 없어 범위 밖이다.

**범례**: ✅ 이미 구현됨 · 🔧 있지만 화면 요구사항과 어긋나거나 보완 필요 · 🆕 신규 API/도메인 필요

공통 규약(모두 적용됨, 화면별 표에서 반복 언급하지 않음): 응답은 `ApiResponse<T>`로 감싸짐, 목록은 커서
페이지네이션(`cursor`/`limit` → `CursorMeta`), 인증은 `Authorization` 헤더 + `@CurrentUser AuthPrincipal`.

---

## 1. 로그인 / 비밀번호 찾기

화면: 로그인, 비밀번호 찾기(이메일 입력 → 6자리 인증번호 입력 → 새 비밀번호 설정).

| 상태 | Method | Path | 설명 |
| --- | --- | --- | --- |
| ✅ | POST | `/auth/students/login` | 아이디(이메일)/비밀번호 로그인 |
| ✅ | POST | `/auth/students/refresh` | 리프레시 토큰으로 재발급 |
| ✅ | POST | `/auth/students/logout` | 로그아웃 |
| 🆕 | POST | `/auth/students/password-reset/email` | 비밀번호 재설정용 인증번호 이메일 발송 |
| 🆕 | POST | `/auth/students/password-reset/verify` | 인증번호 확인 (재설정용 임시 토큰 발급) |
| 🆕 | PATCH | `/auth/students/password-reset` | 임시 토큰 + 새 비밀번호로 재설정 |

- 비밀번호 찾기 플로우 자체가 아직 없다. 회원가입 이메일 인증(2번 섹션)과 인증번호 발송/확인 로직을 공유할 수
  있으니, `global` 또는 `auth` 안에 재사용 가능한 이메일 인증코드 컴포넌트로 설계하는 걸 권장.

---

## 2. 회원가입

화면: 1/3(로그인 정보) → 2/3(기본 정보) → 3/3(학교 정보+약관) → 서비스 이용약관 전체보기 → 학생 인증 방식 선택
(이메일 vs 서류) → 이메일 인증(학교 이메일 + 6자리 코드) / 서류 인증(문서 유형 선택 + 사진 업로드 → 검토 중 →
완료 또는 재제출).

| 상태 | Method | Path | 설명 |
| --- | --- | --- | --- |
| ✅ | POST | `/auth/students/signup` | `StudentSignUpRequest(email, name, password, country, language, studentType, major, grade, universityId)` |
| ✅ | POST | `/students/me/verification-documents` | 서류 인증 제출 (`fileId`) — `VerificationController` |
| ✅ | POST | `/files/presigned-url` | 서류 사진 업로드용 presigned URL |
| 🔧 | POST | `/auth/students/check-email` (또는 GET `?email=`) | 이메일(로그인 아이디) 중복 확인 — 1/3 화면에 "중복 확인" 버튼이 있는데 대응 API가 없음 |
| 🆕 | POST | `/auth/students/email-verifications` | 학교 이메일로 인증번호 발송 (가입 단계, 서류 인증과 별개 트랙) |
| 🆕 | POST | `/auth/students/email-verifications/confirm` | 인증번호 확인 |
| 🆕 | GET | `/students/me/verification-documents` | 본인 서류 인증 상태 조회 — "인증 정보를 확인하고 있어요"/"인증 완료"/"다시 확인해주세요" 폴링용. 현재 제출(POST)만 있고 조회가 없음 |
| 🆕 | GET | `/universities/{universityId}/majors?query=` | 전공 검색 자동완성 — `Student.major`가 자유 문자열이라 전공 마스터 데이터가 없음. 학과 목록을 어디서 관리할지(정적 데이터 vs 신규 테이블) 결정 필요 |
| 🔧 | `SubmitVerificationDocumentRequest`에 문서 유형 필드(`documentType`: 학생증/재학증명서/입학허가서/성적증명서) 추가 검토 — 지금은 파일만 받고 어떤 서류인지 구분하지 않음 |
| 🆕 | GET | `/terms/service` (선택) | "서비스 이용약관" 전체 텍스트 — 정적 페이지/앱 내 하드코딩으로 처리해도 되지만, 약관 개정 시마다 배포 없이 바꾸려면 API로 빼는 게 나음 |

- 온보딩 전체 흐름이 "이메일 인증 완료 → 로그인 가능" 경로와 "서류 인증 검토 중 → 승인/반려" 경로 두 개로
  갈리는데, 지금 `VerificationDocument`는 서류 인증만 다룬다. 이메일 인증 완료만으로 로그인을 막지 않을지,
  서류 인증 결과에 따라 계정 상태(제한 기능 등)를 어떻게 둘지는 기획 확인이 필요.

---

## 3. 공지사항

화면: 홈(놓치면 안 되는 소식 배너 + 최근 공지), 전체 공지사항(검색 + 카테고리 필터: 전체/국제교류처/학생처/
장학처), 상세, "담당자에게 질문하기" → 채팅으로 이동.

| 상태 | Method | Path | 설명 |
| --- | --- | --- | --- |
| ✅ | GET | `/notices` | 커서 목록, `groupBy=department`로 부서별 그룹핑 지원 |
| ✅ | GET | `/notices/{noticeId}` | 상세 조회 + 읽음 처리 + 번역(`Accept-Language`) |
| ✅ | PATCH | `/notices/{noticeId}/read` | 읽음 처리 |
| ✅ | GET | `/notices/unread-count` | 안 읽은 공지 수 |
| ✅ | GET | `/notices/{noticeId}/files` | 첨부파일 목록 |
| ✅ | POST | `/chats/threads` | `noticeId` 전달 가능 — "담당자에게 질문하기" 딥링크 채팅 이미 지원됨 (`ChatThread.notice` FK) |
| 🔧 | — | `/notices?q=` | 검색어 입력 API가 없음. `/search`가 notice/honey_tip 통합 검색을 지원하니 이걸 재사용할지, `/notices`에 `q` 파라미터를 추가할지 결정 필요 (통합 검색은 부서 필터를 같이 못 걺) |

- CLAUDE.md에 "ChatMessage에 context_type/context_id 없음"이라고 적혀 있지만, 실제 코드는
  `ChatThread.notice`(nullable FK)로 공지 딥링크 채팅을 이미 지원한다. 클럽 등 다른 컨텍스트까지 일반화하려면
  여전히 갭이지만(4번 섹션 참고), 공지 문의 자체는 이미 동작한다 — CLAUDE.md 갭 목록이 최신 상태를 못
  따라간 부분으로 보임.

---

## 4. 클럽

화면: 전체 클럽/내 클럽 목록, 클럽 만들기, 클럽 상세(소개+모집현황+멤버), 참여/참여완료, 클럽 그룹 채팅방
(여러 멤버가 함께 있는 방).

| 상태 | Method | Path | 설명 |
| --- | --- | --- | --- |
| ✅ | GET | `/clubs?status=` | 클럽 목록 |
| ✅ | POST | `/clubs` | 클럽 만들기 |
| ✅ | GET | `/clubs/mine` | 내 클럽 목록 |
| ✅ | GET | `/clubs/{clubId}` | 클럽 상세 |
| ✅ | PATCH | `/clubs/{clubId}` | 클럽 수정 |
| ✅ | DELETE | `/clubs/{clubId}` | 클럽 삭제 |
| ✅ | POST | `/clubs/{clubId}/join` | 참여 |
| ✅ | DELETE | `/clubs/{clubId}/leave` | 나가기 |
| ✅ | GET | `/clubs/{clubId}/members` | 멤버 목록 |
| 🆕 | POST | `/clubs/{clubId}/messages` | 클럽 그룹 채팅 메시지 전송 |
| 🆕 | GET | `/clubs/{clubId}/messages` | 클럽 그룹 채팅 메시지 목록 (커서) |

- 가장 큰 갭이다. 지금 `chat` 도메인의 `ChatThread`는 `(student_id, staff_id)` 1:1 전용이라(unique
  constraint) 클럽처럼 여러 멤버가 들어오는 그룹 채팅을 못 담는다. 클럽 채팅을 `chat` 도메인 안에 확장할지
  (스레드 타입을 1:1/그룹으로 분기), `club` 도메인 안에 별도 메시지 엔티티를 둘지는 설계 결정이 필요 — 옆
  도메인 패턴을 그대로 베끼기 애매한 지점이라 미리 로드맵에서 방향을 정하는 게 좋다.
- "알람" 화면의 "클럽 채팅" 필터/"클럽 그룹 채팅" 알림 토글도 이 신규 채팅 메시지를 전제로 한다.

---

## 5. 정보 (생활정보/꿀팁)

화면: 홈(CITY GUIDE 배너 캐러셀 + 생활정보 리스트), 정보글 상세(단계별 가이드, 예상 수수료/처리 기간, 문서
태그, "정보 수정 요청하기", "링크로 접속하기"), 수정 요청 보내기 바텀시트.

| 상태 | Method | Path | 설명 |
| --- | --- | --- | --- |
| ✅ | GET | `/honey-tips?category=` | 목록 (카테고리 필터) — `honeytip` 도메인 |
| ✅ | GET | `/honey-tips/{honeyTipId}` | 상세 (조회수 증가) |
| ✅ | POST | `/honey-tips/{honeyTipId}/edit-requests` | 수정 요청 보내기 |
| 🔧 | — | — | `HoneyTip.content`가 단일 텍스트 필드라, 와이어프레임의 "단계 1/2/3/4 + 예상 수수료 + 처리 기간 + 문서 태그 + 외부 링크" 같은 구조화된 표현은 지금 프론트에서 마크다운/리치텍스트를 파싱해서 그려야 한다. 구조를 그대로 서버에서 내려주고 싶다면 `fee`, `estimatedDuration`, `externalLink`, `documentTags` 같은 필드를 추가해야 하는데, 지금 단계에서 꼭 필요한 확장인지는 기획 확인 필요 |

- CITY GUIDE 배너는 아마 특정 카테고리("배너"류)로 필터링해서 같은 `/honey-tips` 목록을 재사용하는 걸로
  보인다 — 별도 API는 필요 없어 보이지만, 카테고리 값 목록(코드 테이블)이 있으면 좋다.

---

## 6. 알람 (채팅/알림)

화면: 채팅 탭(1:1 문의/클럽 채팅 필터), 알림 탭(전체/공지/커뮤니티/클럽/생활정보 필터, 오늘/어제 구분), 알림
설정(채팅 알림 토글 2개 + 서비스 알림 토글 5개, "시스템 안내"는 필수 수신).

| 상태 | Method | Path | 설명 |
| --- | --- | --- | --- |
| ✅ | GET | `/chats/threads` | 채팅 스레드 목록 — "채팅" 탭의 1:1 문의 부분 |
| ✅ | GET | `/chats/threads/{threadId}/messages` | 스레드 메시지 (커서) |
| ✅ | POST | `/chats/threads/{threadId}/messages` | 메시지 전송 |
| ✅ | PATCH | `/chats/threads/{threadId}/read` | 읽음 처리 |
| ✅ | GET | `/chats/unread-count` | 안읽은 채팅 수 |
| 🆕 | GET | `/notifications` | 알림 피드 (공지/커뮤니티/클럽/생활정보 통합, 오늘/어제 그룹) — 커서 목록 |
| 🆕 | PATCH | `/notifications/{notificationId}/read` | 알림 개별 읽음 처리 |
| 🆕 | GET | `/notifications/unread-count` | 안읽은 알림 수 (뱃지) |
| 🆕 | GET | `/notifications/settings` | 카테고리별 수신 설정 조회 |
| 🆕 | PATCH | `/notifications/settings` | 카테고리별 수신 설정 변경 (채팅: 1:1/클럽그룹, 서비스: 공지/커뮤니티/클럽활동/생활정보/시스템안내) |

- "채팅" 탭은 이미 있는 `chat` 도메인으로 충당되지만(클럽 채팅 부분은 4번 섹션 갭과 연결), "알림" 탭 + "알림
  설정"은 완전히 새로운 도메인이다. 지금 코드베이스에 `Notification` 계열 엔티티가 전혀 없다 — CLAUDE.md
  갭 목록에는 이 얘기가 없지만 실제로는 빠져 있다.
  - 알림 피드는 공지/게시글/클럽/생활정보 각 도메인 이벤트를 구독해서 쌓는 구조(팬아웃 테이블)가 필요해
    보인다. 새 도메인(`domain/notification`)으로 빼는 게 기존 "도메인 주도 패키지 + 공통 로직은 global에만"
    원칙과 맞다.
  - "시스템 안내"처럼 끄지 못하는 필수 알림이 있어서, 설정 응답에 `required: boolean` 같은 플래그가 필요.

---

## 7. 마이페이지

화면: 홈(계정 관리/커뮤니티 메뉴, 로그아웃), 내 정보 수정(이름/국적/언어/학생유형/출생연도/전공/학년), 비밀번호
재설정(로그인 상태에서), 내가 작성한 글, 내가 작성한 댓글.

| 상태 | Method | Path | 설명 |
| --- | --- | --- | --- |
| ✅ | GET | `/students/me` | 내 정보 조회 |
| ✅ | PATCH | `/students/me` | 내 정보 수정 — 현재 `StudentProfileUpdateRequest(language, major, grade)`만 지원 |
| 🔧 | — | — | 와이어프레임엔 이름/국적/출생연도 수정 필드도 있는데 `StudentProfileUpdateRequest`엔 없다. 이름·국적을 수정 가능하게 열지(신분증과 실명 일치 요구사항과 충돌할 수 있음), 출생연도를 아예 안 받고 있는 `Student` 엔티티에 새 컬럼을 추가할지 결정 필요 — `ddl-auto: update` 쓰는 중이라 컬럼 추가는 직접 확인하면서 진행 |
| 🆕 | PATCH | `/students/me/password` | 로그인 상태에서 비밀번호 재설정 (현재 비밀번호 확인 없이, 이메일 인증코드 방식으로 보임) |
| 🆕 | GET | `/posts/mine` | 내가 작성한 글 목록 (커뮤니티+클럽 필터 탭 포함) — 지금 `/posts`는 전체 목록만 있고 작성자 필터가 없음 |
| 🆕 | GET | `/comments/mine` | 내가 작성한 댓글 목록 — 현재 댓글은 `/posts/{postId}/comments`로만 조회 가능, 사용자 기준 조회가 없음 |

- "이메일 인증: 학생 인증됨" 뱃지는 2번 섹션의 `GET /students/me/verification-documents`(상태 조회, 신규)
  결과를 재사용하면 된다.

---

## 8. 커뮤니티

와이어프레임엔 별도 표로 안 뺐지만 화면(자유게시판 목록/검색, 게시글 상세+댓글, 새 글 작성 — AI 번역 안내
포함)에 필요한 API는 이미 대부분 구현돼 있다.

| 상태 | Method | Path | 설명 |
| --- | --- | --- | --- |
| ✅ | GET | `/posts` | 목록 (커서) |
| ✅ | POST | `/posts` | 작성 (익명, 원문 언어 그대로 저장 후 서버에서 번역) |
| ✅ | GET | `/posts/{postId}` | 상세 (댓글 포함, `Accept-Language`로 번역) |
| ✅ | DELETE | `/posts/{postId}` | 삭제 |
| ✅ | POST/DELETE | `/posts/{postId}/like` | 좋아요/취소 |
| ✅ | GET/POST | `/posts/{postId}/comments` | 댓글 조회/작성 |
| ✅ | DELETE | `/posts/{postId}/comments/{commentId}` | 댓글 삭제 |
| ✅ | POST/DELETE | `/posts/{postId}/comments/{commentId}/like` | 댓글 좋아요/취소 |
| ✅ | POST | `/posts/{postId}/report`, `/posts/{postId}/comments/{commentId}/report` | 신고 |
| 🔧 | — | `/posts?q=` | 자유게시판 상단 검색어 입력 — `/search`가 커뮤니티 게시글까지는 다루지 않는 듯(현재 기본 타입이 `notice`, `honey_tip`뿐). 커뮤니티 검색을 어디에 붙일지 확인 필요 |

---

## 요약 — 신규로 필요한 것 중 영향이 큰 항목

1. **알림(Notification) 도메인 신설** — 알람 탭의 "알림" 피드 + 알림 설정. 완전히 새로운 도메인이라 작업량이
   가장 크다.
2. **클럽 그룹 채팅** — 기존 `chat`(1:1 전용)과 구조가 달라 확장 방식을 먼저 정해야 한다.
3. **비밀번호 찾기 / 회원가입 이메일 인증코드** — 같은 "이메일로 코드 발송 → 확인" 로직을 두 군데서 쓰므로
   공통 컴포넌트로 설계하는 걸 권장.
4. **서류 인증 상태 조회 API** — 제출(POST)만 있고 조회가 없어서, 회원가입 중 "검토 중"/"완료"/"재제출" 화면과
   마이페이지 인증 뱃지 둘 다 못 만든다. 신규 API 중 가장 간단하니 먼저 처리해도 좋다.
5. **"내가 작성한 글/댓글" 필터, 이메일 중복확인, 전공 검색** — 각 도메인에 파라미터/엔드포인트 하나씩 추가하는
   수준으로 비교적 가볍다.

각 항목을 실제로 구현할 때는 CLAUDE.md 작업 순서대로 `docs/roadmaps/`에 로드맵을 먼저 쓰고, 옆 도메인 패턴을
그대로 따라가면 된다.
