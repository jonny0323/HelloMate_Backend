# 보고서 — 알림(Notification) 도메인

로드맵: `docs/roadmaps/notification.md`

## 한 일

신규 `domain/notification` 패키지: `Notification`/`NotificationSetting` 엔티티, 리포지토리, `notify()`
진입점을 가진 `NotificationService`, `GET /notifications`(카테고리 필터+커서), `PATCH
/{notificationId}/read`, `GET /unread-count`, `GET/PATCH /settings` 5개 엔드포인트.

전체 팬아웃 요청대로 기존 서비스 4곳에 훅을 연결했다:
- `NoticeService.createAndSend` — 수신 대상 전원에게 `NOTICE`
- `PostService.like`/`createComment`/`likeComment` — 글/댓글 작성자에게 `COMMUNITY`(본인 행동은 스킵)
- `ClubService.join` — 클럽 개설자에게 `CLUB`
- `HoneyTipService.createHoneyTip` — 대학 전체 학생에게 `HONEY_TIP`

## 설계 결정 — 카테고리 범위를 문서 재해석해서 좁힘

`api.md`의 "알림 설정"은 7개 토글(채팅 1:1/클럽그룹 + 서비스 공지/커뮤니티/클럽활동/생활정보/시스템)을
말하지만, "알림 탭"(피드) 필터는 "전체/공지/커뮤니티/클럽/생활정보" 4개뿐이고 채팅이 빠져 있다. 채팅은
이미 `ChatThread.studentUnread`/`chats/unread-count`로 자체 배지를 관리하고 있어서, **`CHAT_DIRECT`/
`CHAT_CLUB`은 설정 토글로만 존재하고 `Notification` 행을 만들지 않는다**(설정 API 계약은 7개 다
맞춰뒀지만 뒤에 실제 발행 주체가 없음). `SYSTEM`도 마찬가지로 끌 수 없는 필수 카테고리라는 것만 반영하고,
지금 코드베이스에 "시스템 공지"를 발행하는 관리자 기능 자체가 없어서 발행 훅은 안 넣었다. 이 두 판단은
로드맵에 근거와 함께 적어뒀다 — `api.md`가 명시적으로 정하지 않은 부분을 문서 안의 다른 단서(필터
목록)로 추론한 것이라 오독 가능성이 있으면 알려달라.

`NOT_CLUB_MEMBER`류의 다른 결정과 마찬가지로, "시스템 안내를 끄려는 요청" 에러는 전용 `ErrorCode`를
새로 파지 않고 기존 `INVALID_INPUT` + 커스텀 메시지 패턴을 재사용했다.

## 검증

`./gradlew test`/`build` 통과. Flyway+H2(PostgreSQL 모드)+`ddl-auto: validate`로 `V1`~`V5` 전체
migrate 후 Hibernate 검증까지 확인(임시 파일은 확인 후 삭제).

## 남은 이슈 / 다음 작업 후보

- `SYSTEM` 카테고리는 실제로 알림을 만드는 주체가 없다 — 관리자 공지/브로드캐스트 기능이 생기면 그때
  `notify(..., SYSTEM, ...)` 호출을 추가하면 됨.
- `CHAT_DIRECT`/`CHAT_CLUB` 토글은 지금 아무 동작도 안 바꾼다 — 나중에 푸시 알림을 붙일 때 이 설정값을
  참조하게 될 자리만 마련해둔 상태.
- 커뮤니티 대댓글(답글)의 부모 댓글 작성자에게는 알림이 안 간다(게시글 작성자에게만 감) — 필요하면
  후속 작업.

## 커밋 메시지 제안

```
✨ Feature : 알림(Notification) 도메인 추가 및 팬아웃 연결
```
