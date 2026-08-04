# 로드맵 — 남은 갭 마무리 (프로필 확장 / 공지 검색 / 커뮤니티 검색)

## 배경

`docs/resources/api-reference.md` "남은 갭"에 남아 있던 6개 중, 사용자 확인 결과 3개만 이번에 진행한다.

| 항목 | 결정 |
| --- | --- |
| 마이페이지 이름/국적/출생연도 수정 | **진행** — 이름/국적까지 다 열고, `Student`에 `birthYear` 컬럼 추가 |
| 공지사항 검색 (`GET /notices?q=`) | **진행** — `/notices`에 `q` 파라미터 직접 추가(부서 필터와 같이 쓸 수 있게) |
| 커뮤니티 게시글 검색 (`GET /posts?q=`) | **진행** — `/posts`에 `q` 파라미터 직접 추가(별도 신설이 아니라 기존 목록 쿼리 확장) |
| 서류 인증 문서 유형 구분(`documentType`) | 보류 — 지금처럼 파일만 받는다 |
| 생활정보 구조화 필드 | 보류 — `content` 단일 텍스트 유지 |
| `GET /terms/service` | 보류 — 정적 페이지/앱 하드코딩으로 충분 |

세 항목이 서로 다른 도메인(student/notice/community)이라 파일이 안 겹친다 — 로드맵/커밋도 하나로
묶지 않고 기능별로 나눈다(문서만 이 파일 하나에 모음).

## 설계

### 1. 프로필 수정 확장
- `Student`에 `birthYear`(Integer, nullable) 컬럼 추가. 가입 시점엔 안 받고(스키마 갭 아님 — 기존
  가입 흐름 그대로), 마이페이지에서만 채워 넣을 수 있게 한다.
- `Student.updateProfile`을 `name`/`country`/`birthYear`까지 받도록 확장(전부 null-safe, 기존 패턴
  그대로 — null이면 안 바꿈).
- `StudentProfileUpdateRequest`/`StudentProfileResponse`에 필드 추가.
- 이름/국적을 열어주는 것과 서류 인증(재학증명서 등) 실명 불일치 리스크는 사용자가 확인하고 진행하기로
  한 것 — 서류 인증 검토 단계에서 담당자가 육안으로 걸러내는 걸 전제로 한다(자동 검증 로직은 없음).

### 2. 공지사항 검색
- `NoticeReceptionRepository`의 기존 커서 쿼리(`findByStudentIdOrderByCreatedAtDesc`)에 `keyword`
  선택 파라미터를 추가한다(제목+본문 LIKE). `/search`가 이미 쓰는 `searchByStudentIdAndTitle`은 통합
  검색 전용으로 그대로 둔다(커서 페이지네이션이 없는 별도 쿼리라 재사용 안 함).
- `groupBy=department`와 `q`를 동시에 쓸 수 있다(둘 다 커서 목록 위에서 동작).

### 3. 커뮤니티 게시글 검색
- `PostRepository.findByUniversityIdOrderByCreatedAtDesc`에 `keyword` 선택 파라미터 추가(제목+본문
  LIKE). `/posts/mine`(작성자 필터)은 검색 대상 아님 — 건드리지 않는다.

## 구현 순서

1. 프로필: `Student`(+마이그레이션), `StudentProfileUpdateRequest`/`Response`, `StudentService`
2. 공지 검색: `NoticeReceptionRepository`, `NoticeQueryService`, `StudentNoticeController`
3. 커뮤니티 검색: `PostRepository`, `PostService`, `PostController`
4. `./gradlew test`
