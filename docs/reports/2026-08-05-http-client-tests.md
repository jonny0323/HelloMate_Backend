# HTTP Client 자동 검증 테스트 스위트 — 결과 보고

## 한 일

`http-client/` 밑에 IntelliJ HTTP Client(`.http`) 테스트 16개 파일을 작성했다. 22개 컨트롤러(학생 앱 +
담당자 웹 + 관리자 콘솔) 전체의 엔드포인트를 커버하며, 요청마다 `client.test()` 응답 핸들러로
status/응답 바디를 자동 assert한다 — 사람이 응답을 눈으로 안 봐도 통과/실패를 알 수 있다.

```
http-client/
  http-client.env.json           # host=http://localhost:8080
  seed-invite-codes.sql          # 담당자 초대 코드 9개 수동 시드
  README.md                      # 실행 방법 + 알려진 한계
  01-university-major.http … 16-admin-post-moderation.http
```

각 파일은 실행 순서 의존 없이 독립적으로 돌아가게 짰다 — 파일 맨 위에서 `Date.now()` 기반 `runId`로
그 실행 전용 학생/담당자 계정을 직접 만든다. 도메인별 파일 구성과 컨벤션은
`docs/roadmaps/2026-08-05-http-client-tests.md`, `http-client/README.md` 참고.

## 로드맵 대비 달라진 점

- 로드맵엔 없었지만, 조사 중 `docs/resources/api-reference.md`가 실제 응답 형식(SNAKE_CASE)과
  다르게 camelCase 예시로 적혀 있는 걸 확인해서 `.http` 파일은 전부 실제 Jackson 설정 기준
  snake_case로 작성했다. (이 문서 자체를 고치는 건 이번 작업 범위 밖이라 손 안 댔다 — 필요하면 별도
  작업으로 제안.)
- `docs/resources/api.md`/`api-reference.md`는 리팩토링(`🔨 오푸스로 전면 개편`) 이전 상태를 일부
  덜 반영하고 있어서, 문서 대신 실제 컨트롤러/서비스/DTO 코드를 1차 소스로 삼았다(클럽 카드 상태,
  꿀팁 스텝, 공지 배너, 학생 인증 상태/서류 유형, 클럽장 위임, 관리자 대시보드/학생 디렉토리/게시글
  모더레이션 등 리팩토링에서 새로 생긴 필드·엔드포인트 다수 포함).

## 빌드/테스트 결과

`./gradlew build`/`test`는 이번 작업에서 소스 코드를 건드리지 않았으므로 실행하지 않았다(변경 대상이
`http-client/`, `docs/` 뿐이라 Java 컴파일에 영향 없음).

대신 로컬 Postgres(`docker compose up -d`) + `./gradlew bootRun`으로 실제 서버를 띄우고, 초대 코드
시드 후 **`curl`로 대표 시나리오를 수동 검증**했다(환경에 IntelliJ `ijhttp` CLI가 없어 `.http` 파일을
그대로 자동 실행하지는 못했다 — 요청 문법과 필드명은 컨트롤러/DTO 코드를 직접 대조해서 작성했고,
아래 curl 검증으로 핵심 플로우가 실제로 그 계약대로 동작하는지 확인했다).

검증한 것: 학생 회원가입/로그인/프로필 조회(snake_case 응답 확인), 전공 검색, 클럽 생성/참여/정원
초과, 담당자 회원가입/로그인, 공지 생성, 알림 설정 변경. 이 과정에서 **이번 작업과 무관한 기존 버그
3건**을 발견했다 (아래 참고). 검증 후 서버는 종료했고, 로컬 DB에 스모크 테스트로 만든 계정/클럽/공지
몇 건이 남아 있다 — 필요하면 `docker compose down -v` 후 `seed-invite-codes.sql`을 다시 실행해서
초기화하면 된다.

## 발견한 기존 버그 (이번 작업 범위 밖이라 코드는 안 고쳤음)

### 1. `GET /universities/{id}/majors`에 `query` 생략 시 500 (Postgres 전용)

`MajorRepository.search`의 JPQL:
```java
@Query("select m from Major m where m.university.id = :universityId "
        + "and (:query is null or lower(m.name) like lower(concat('%', :query, '%'))) order by m.name asc")
```
`query`가 null이면 Postgres가 `:query` 바인드 파라미터 타입을 추론 못 해 `function lower(bytea) does not
exist`로 500이 난다. H2(`ddl-auto: create-drop`, 테스트 프로파일)에서는 안 터져서 `./gradlew test`로는
못 잡는다 — CLAUDE.md가 경고한 "Postgres 전용 이슈"의 실제 사례. 값이 있을 때는 정상 동작 확인함.
제안 수정: `cast(:query as string)`로 명시 캐스팅하거나 `query`가 null일 때 별도 쿼리 메서드로 분기.

`01-university-major.http`의 1번 요청이 이 버그로 현재 실패한다(200 기대, 실제 500).

### 2. UUID PK 엔티티를 `save()` 직후 변형하면 그 변경이 저장되지 않음 (더 넓은 범위 문제)

`ClubService.createClub`:
```java
Club club = new Club(UuidCreator.create(), ...);   // ID를 미리 직접 할당
clubRepository.save(club);
clubMemberRepository.save(new ClubMember(...));
club.increaseMember();                              // <- 이 변경이 DB에 반영 안 됨
return ClubResponse.of(club, true);                 // 응답엔 반영된 것처럼 보임 (착시)
```
`Club`처럼 `@GeneratedValue` 없이 ID를 직접 할당하는 엔티티는, Spring Data JPA가 "이미 ID가 있으니
새 엔티티가 아니다"로 판단해서 `save()`가 내부적으로 `entityManager.persist()`가 아니라
`entityManager.merge()`를 탄다. `merge()`는 인자로 준 객체를 그대로 안 바꾸고 **다른 매니지드
복사본을 반환**한다. `club.increaseMember()`처럼 `save()` 이후에도 원래 `club` 변수를 계속 써서
바꾸면, 그 변경은 반환값을 안 받았으니 그냥 버려진다 — DB엔 절대 안 들어간다.

실제로 정원 1명 클럽을 만들면:
- 생성 직후 응답: `current_members: 1, full: true` (원본 객체 기준, 맞는 것처럼 보임)
- 바로 다시 조회하면: `current_members: 0, full: false` (실제 DB 값)
- 그 상태에서 다른 학생이 참여를 시도하면 **정원이 이미 찼는데도 200으로 성공한다** (`409 CLUB_FULL`이
  나와야 함) — 정원 제한이 사실상 항상 뚫려 있다.

같은 패턴이 `NoticeService.createAndSend`에도 있다: `noticeRepository.save(notice)` 뒤에
`notice.assignRecipientCount(recipients.size())`를 호출하는데, 이것도 저장 안 됨 — 생성 직후 응답엔
정확한 수신자 수가 찍히지만, 이후 `GET /admin/notices`, `GET /admin/notices/{id}`에서 보이는
`total_recipient_count`는 항상 0으로 남고, `read_rate`도 분모가 0이라 항상 0.0으로 고정된다.

(반대로 `PostService`의 좋아요/댓글 카운트 증가나 `NotificationService`의 설정 저장은 안전하다 —
`post`/`setting`이 새로 만든 객체가 아니라 리포지토리 조회로 가져온 **이미 매니지드된** 엔티티거나,
`setting = repository.save(...)`처럼 반환값을 제대로 받아쓰기 때문. 버그는 "새 엔티티를 만들어 저장한
뒤 반환값을 안 받고 원본 참조로 계속 변형하는" 코드에서만 난다.)

`06-club.http`의 30번(정원 초과 참여 시도 → 409 기대), `09-notice.http`의 16번(읽음률 > 0 기대)이
이 버그로 현재 실패한다.

**제안 수정** (`club`/`notice`뿐 아니라 같은 패턴 전체에 적용 가능한 근본 해결책 중 하나):
`BaseTimeEntity`에 `Persistable<String>` 구현을 추가해서(`isNew()`를 `createdAt == null`로 판단하게)
`save()`가 항상 `persist()`를 타게 만들거나, 각 호출부에서 `club = clubRepository.save(club)`처럼
반환값을 받아 쓰도록 고치는 것. 전자가 한 번에 전체 도메인을 고치는 방법이라 더 나아 보인다.

### 3. (버그는 아님, 참고) `data`가 없으면 JSON에서 필드 자체가 사라짐

`ApiResponse`에 `default-property-inclusion: non_null`이 걸려 있어서, `data`가 `null`인 응답(로그아웃,
삭제 등)은 `"data": null`이 아니라 **`data` 키 자체가 없다**. `.http` 파일에서
`response.body.data === null || response.body.data === undefined`로 검증하도록 반영해뒀다(처음엔
`=== null`로만 썼다가 실제 서버로 확인하고 고침).

## 자동화 못 한 부분 (알려진 한계)

- 이메일 인증 코드(`StubEmailService`)가 서버 로그에만 남고 조회 API가 없어서, 회원가입 이메일 인증 /
  비밀번호 재설정 / 마이페이지 비밀번호 변경의 **정확한 코드로 성공하는 해피패스**는 `.http`만으로
  검증 불가능하다. 발송 성공(200)과 틀린 코드 에러(400)까지만 자동 검증했다.
- 담당자 초대 코드 발급 API가 없어서 `seed-invite-codes.sql`을 수동으로 먼저 실행해야 한다. 파일마다
  전용 코드를 하나씩 배정해서 파일 간 재실행 충돌은 없게 했지만, `03-staff-auth.http`의 회원가입
  성공 케이스는 코드를 소모하니 재실행하려면 시드를 다시 넣어야 한다.
- `NoticeQueryService.toResponseData`의 `groupBy=department` 분기, `ClubService`의
  `CLUB_RECRUIT_CLOSED`(마감일 경과) 경로는 테스트에서 다루지 않았다 — 전자는 커버리지를 더 채울
  여지, 후자는 미래 날짜 대기 없이는 자동화가 어려워서 뺐다.

## 다음 후보

1. 위 버그 2건 수정 여부 결정 (원하면 바로 착수 가능 — `Persistable` 구현이 제일 근본적인 수정).
2. `ijhttp` CLI 설치해서 `.http` 스위트를 실제로 한 번씩 돌려보고 이 보고서의 예상 실패 케이스들을
   실측으로 재확인.
3. `docs/resources/api-reference.md`를 리팩토링 이후 실제 코드 기준으로 갱신(현재 문서와 실제 DTO
   필드 이름/필드 개수가 상당히 벌어져 있음 — camelCase vs snake_case 포함).
