# 2026-08-05 — 공지 즉시발송 버그 수정 + http-client 커버리지 보강 (보고서)

로드맵: `docs/roadmaps/2026-08-05-notice-send-fix-http-coverage.md`

## 요약

| 항목 | 시작 | 끝 |
| --- | --- | --- |
| `./gradlew test` | 23개 중 **5개 실패** | **24개 전부 통과** |
| `./gradlew build` | FAILED | **SUCCESSFUL** |
| `.http` 엔드포인트 커버리지 | 104개 중 94개 (90%) | **104개 중 104개 (100%)**, 요청 282건 |
| 고친 프로덕션 버그 | — | 3건 (공지 즉시발송 / 모임 인원 / enum 쿼리 파라미터) |

로드맵에 적었던 것보다 범위가 커졌다. 로드맵은 원인을 `Notice.sent()` 팩토리 하나로 봤는데,
서버를 실제로 띄워 확인해보니 **그 아래에 더 넓은 JPA 문제가 깔려 있었다** (아래 2번).

## 1. 테스트 실행 환경

- 이 프로젝트는 Java 21 툴체인인데 `JAVA_HOME`이 설정돼 있지 않아 `gradlew`가 JDK를 못 찾는다.
  IntelliJ가 받아둔 `C:\Users\wjdxo\.jdks\ms-21.0.11`을 `JAVA_HOME`으로 지정해서 실행했다.
  프로젝트 설정 파일은 하나도 건드리지 않았다. 터미널에서 바로 돌리고 싶으면 시스템 환경변수에
  `JAVA_HOME`을 한 번 박아두면 된다.
- 테스트는 H2라 Postgres 없이 통과한다(설계대로). 다만 아래 버그들은 H2 단위 테스트로는 안 잡혀서,
  `docker compose`의 Postgres + `bootRun`(포트 8081)으로 **실제 서버를 띄워 검증했다.**
  검증용으로 만든 계정/공지/모임/초대코드는 전부 지웠다.
  8080은 이미 다른 인스턴스가 점유 중이라 건드리지 않았다.

## 2. 고친 것

### (1) 공지 즉시발송이 항상 실패 — `POST /admin/notices`

`NoticeService.createAndSend()`가 `Notice.sent()`로 이미 `status = SENT`인 공지를 만든 뒤
`dispatch()`에서 `markSent()`를 호출했다. `markSent()`는 이미 SENT면 예외를 던지므로 **담당자 웹의
공지 발송이 100% 409 NOTICE_ALREADY_SENT로 실패**하는 상태였다. `NoticeTest` 실패 5건도 테스트
헬퍼가 같은 패턴을 쓴 탓이다.

- `Notice.sent()` 팩토리 제거 — 공지는 항상 DRAFT로 태어나고 `markSent()`로만 SENT가 된다.
  즉시발송과 초안발송(`sendDraft`)이 같은 경로를 타게 됐다.
- `NoticeTest` 헬퍼를 `draft() + markSent()`로 교체하고, 이 불변식을 고정하는 테스트
  (`갓 만든 공지는 발송 전 상태라 바로 발송할 수 있다`)를 하나 추가했다.

### (2) `save()` 뒤의 변경이 DB에 안 써지는 문제 — 더 깊은 원인

(1)만 고치고 서버에서 확인했더니 **응답은 201인데 DB의 공지는 여전히 `DRAFT` / `sent_at = NULL` /
`total_recipient_count = 0`** 이었다. 학생 목록 쿼리는 `status = SENT`만 보므로, 발송했다는 응답을
받고도 학생에게는 공지가 안 보이는 상태다.

원인은 PK 전략이다. 이 프로젝트는 PK를 앱에서 `UuidCreator.create()`로 채워 넣는데, Spring Data JPA는
**id가 non-null이면 "새 엔티티가 아니다"라고 판단해 `persist()`가 아니라 `merge()`를 호출**한다.
`merge()`는 관리 상태의 *복사본*을 반환하고 인자로 넘긴 인스턴스는 detached로 남는다. 그래서
`save(x)` 이후 `x`를 변경하면 그 변경은 flush되지 않고 조용히 사라진다.

같은 패턴을 전수 조사해서 실제 피해가 있는 곳을 찾았다:

| 위치 | 증상 (수정 전, 실측) |
| --- | --- |
| `NoticeService.createAndSend` | API는 `total_recipient_count: 2`, DB는 `DRAFT / sent_at NULL / 0` |
| `ClubService.createClub` | API는 `current_members: 1`, DB는 `0` |

둘 다 `save()`의 **반환값(관리 인스턴스)** 을 쓰도록 고쳤고, 왜 그래야 하는지 주석으로 남겼다.
수정 후 재확인: 공지는 `SENT / sent_at 채워짐 / 3`, 모임은 `current_members = 1`.

> **남은 리스크 (다음 작업 후보).** 호출부 2곳만 고쳤으므로 함정 자체는 남아 있다. 앞으로도
> `save()` 직후 같은 인스턴스를 변경하는 코드를 쓰면 똑같이 조용히 유실된다. 근본 해결은
> `BaseTimeEntity`에 `Persistable`을 붙여 `isNew()`를 `createdAt == null`로 판정하게 하는 것이다
> (엔티티 26개 영향, insert마다 나가던 불필요한 SELECT도 사라짐). 전역 변경이라 이번엔 미뤘다.

### (3) enum 쿼리 파라미터가 소문자를 거부 + 500으로 나감

`?status=pending` → **500 INTERNAL_ERROR**, `?status=PENDING`만 200이었다. 응답 JSON과 요청 바디는
enum을 전부 소문자로 쓰는데(`@JsonValue`/`@JsonCreator`) 쿼리 파라미터만 Spring 기본 변환기가
`Enum.valueOf`를 그대로 써서 대소문자를 가렸다. 기존 `05-verification.http`의
`?status=pending` 테스트도 이것 때문에 실패한다.

- `CaseInsensitiveEnumConverterFactory`를 `global/common/util`에 추가하고 `WebConfig`에 등록 —
  쿼리 파라미터도 소문자를 받는다.
- `GlobalExceptionHandler`에 `MethodArgumentTypeMismatchException` 핸들러 추가 — 알 수 없는 값은
  500이 아니라 **400 INVALID_INPUT**으로 나간다. (그동안 `@ExceptionHandler(Exception.class)`가
  먼저 잡아 500으로 내보내고 있었다.)

둘 다 받아주는 범위를 넓히는 방향이라 기존 동작이 깨지지 않는다. 실측 확인:
`?status=pending` → 200, `?status=PENDING` → 200, `?status=zzz` → 400 INVALID_INPUT.

## 3. http-client 검증 결과

### 정적 대조 — 문제 없음

컨트롤러/DTO와 `.http` 282건을 스크립트로 전수 대조했다.

- **경로·메서드**: 오타나 존재하지 않는 경로 없음.
- **성공 status**: `@ResponseStatus` 기준 201/200 전부 일치.
- **에러 코드**: `.http`가 검증하는 30개 코드 전부 `ErrorCode`에 실재.
- **요청 바디 필드**: 아래 1건 외 DTO와 전부 일치 (필수 필드 누락도 없음).
- **enum 표기**: 바디의 소문자 표기(`"type": "urgent"` 등)는 각 enum의 `@JsonCreator`가 받아준다.
  쿼리 파라미터 쪽은 위 (3)에서 고쳤다.

### 추가한 커버리지 (90% → 100%)

| 파일 | 추가 |
| --- | --- |
| `09-notice.http` | `POST /admin/notices/audience/count`(전체·개별·검증실패), 초안 CRUD 4종(`POST/GET/PATCH drafts`, `POST drafts/{id}/send`), `PATCH /admin/notices/{id}`, 발송된 초안 재발송 409 |
| `03-staff-auth.http` | `PATCH /admin/staff/me` (전체 수정 / 부분 수정 시 나머지 필드 유지) |
| `08-honeytip.http` | `GET /admin/honey-tips/edit-requests` (status 필터 / 페이지 메타 / 잘못된 status 400) |
| `10-chat.http` | `POST /admin/chats/threads` (생성·스레드 재사용·학생 쪽 미읽음 확인·404) |
| `15-admin-student-directory.http` | `GET /admin/students/target-groups` (200 / 학생 토큰 403) |

새로 쓴 단언은 전부 **실제 서버 응답을 찍어보고** 필드명을 맞췄다
(`thread_id`, `initiated_by: "staff"`, `unread: true`, `type: "COUNTRY"`, `meta.total` 등).

### 안 고친 것 (알고만 있으면 되는 것)

- `09/12`번 파일의 공지 작성 바디에 있는 `department` 필드 4곳 — `CreateNoticeRequest`에 없는
  필드라 Jackson이 조용히 무시한다. 부서는 작성자에게서 가져오는 게 설계 의도(부서 사칭 방지)라
  동작에는 문제가 없지만, "요청으로 부서를 지정할 수 있다"는 오해를 준다. 지우는 게 맞다.
- 이메일 인증 코드 해피패스와 담당자 초대 코드 발급 — 기존 README에 적힌 그대로, 코드에 기능이
  없어서 `.http`만으로는 자동화 불가.

## 4. 빌드/테스트 결과

```
./gradlew build   → BUILD SUCCESSFUL
24 tests, 0 failures   (NoticeTest 9, StudentTest 6, ClubTest 5, SemesterCalculatorTest 3, 컨텍스트 로딩 1)
```

`.http` 스위트는 서버를 띄워야 돌아가므로 CI 대상이 아니다. 이번엔 주요 경로를 수동으로 찍어
확인했고, 전체 실행은 `docker compose up -d` → `seed-invite-codes.sql` → `bootRun` 순으로 하면 된다.

## 5. 커밋 메시지 제안

```
🪲 Fix : 공지 즉시발송/모임 인원 저장 누락 및 enum 쿼리 파라미터 처리 수정
```

## 6. 다음 작업 후보

1. **`BaseTimeEntity`에 `Persistable` 적용** — 위 (2)의 함정을 구조적으로 없앤다. 우선순위 높음.
   지금은 새 기능을 짤 때마다 같은 실수가 재발할 수 있는 상태다.
2. `.http`의 죽은 `department` 필드 정리.
3. 서비스 계층 통합 테스트가 없다. 이번 버그 2건(공지·모임) 모두 "API 응답은 맞는데 DB가 틀린"
   유형이라 단위 테스트로는 안 잡힌다. `@SpringBootTest` + H2로 발송/가입 플로우만이라도 커버하면
   같은 유형을 조기에 잡을 수 있다.
