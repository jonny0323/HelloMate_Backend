# 로드맵 — 알림(Notification) 도메인 (GET /notifications 등 5종 + 팬아웃)

## 배경

`docs/resources/api.md`가 "완전히 새로운 도메인이라 작업량이 가장 크다"고 표시한 항목. 사용자 확인 결과
**전체 팬아웃까지 다 연결**하기로 함 — 공지/커뮤니티/클럽/생활정보 각 도메인 이벤트가 실제로 알림 행을
만들도록 기존 서비스에 훅을 건다.

## 카테고리 범위 판단 (문서 재해석 — 중요)

`api.md` 126번째 줄: "알림 설정(채팅 알림 토글 2개 + 서비스 알림 토글 5개, '시스템 안내'는 필수 수신)".
`설정` 화면은 카테고리 7개(1:1 채팅, 클럽그룹 채팅, 공지, 커뮤니티, 클럽활동, 생활정보, 시스템안내)를
토글하지만, **`알림 탭`(피드) 필터는 "전체/공지/커뮤니티/클럽/생활정보" 4개뿐**이고 채팅은 목록에 없다
(135번째 줄) — 채팅은 이미 있는 "채팅" 탭에서 자체 읽음/안읽음(`ChatThread.studentUnread`,
`unread-count`)으로 관리되고 있어서다.

그래서:
- **알림 피드(`Notification` 테이블)에 실제로 쌓이는 카테고리는 4개 + 시스템**: `NOTICE`, `COMMUNITY`,
  `CLUB`, `HONEY_TIP`(+`SYSTEM`, 지금은 발행 주체가 없어 실제로 안 쌓임 — 아래 참고).
- **`CHAT_DIRECT`/`CHAT_CLUB`은 설정 API의 토글 항목으로만 존재**하고 알림 행을 만들지 않는다 — 채팅은
  이미 자체 배지 시스템이 있어서 지금 단계에서 중복으로 알림 행을 쌓을 이유가 없다. 나중에 푸시 알림을
  붙일 때 이 설정값을 참조하게 될 자리만 마련해둔다.
- `SYSTEM`(시스템 안내)은 끌 수 없는 필수 카테고리라는 것만 설정 API에 반영하고, 지금 코드베이스에
  "시스템 공지"를 발행하는 주체가 없어서(관리자 브로드캐스트 기능 없음) 실제 알림을 만드는 훅은 이번에
  안 넣는다 — 나중에 관리자 공지 기능이 생기면 그때 `notify(..., SYSTEM, ...)`을 호출하면 된다.

## 설계

- 신규 `domain/notification` 패키지. `Notification`(수신자별 알림 행: `student`, `category`, `title`,
  `linkType`/`linkId`로 딥링크, `read`/`readAt`)과 `NotificationSetting`(학생별 카테고리 on/off, 없으면
  기본값 `true`로 취급 — opt-out 모델이라 가입 시점에 7개 행을 미리 만들 필요 없음).
- `NotificationService.notify(Student, category, title, linkType, linkId)`를 다른 도메인 서비스가
  호출하는 진입점으로 둔다. 설정이 꺼져 있으면 조용히 스킵.
- 팬아웃 훅 4곳:
  - `NoticeService.createAndSend` — 수신 대상 학생 전원에게 `NOTICE`
  - `PostService.like`/`createComment`/`likeComment` — 게시글·댓글 작성자에게 `COMMUNITY`(본인 행동은
    스킵 — 예: 본인 글에 본인이 좋아요를 눌러도 알림 안 감)
  - `ClubService.join` — 클럽 개설자에게 `CLUB`
  - `HoneyTipService.createHoneyTip` — 대학 전체 학생에게 `HONEY_TIP`(기존 `NoticeService`가 이미 쓰는
    `StudentRepository.findAllByUniversityId` 재사용)
- 커서 페이지네이션은 기존 도메인들과 동일 패턴(`CursorPageUtil`/`Slice`/`CursorMeta`).
- 설정 변경 API는 `{category, enabled}` 배열을 받아 여러 카테고리를 한 번에 반영한다. `SYSTEM`을
  끄려는 요청은 `INVALID_INPUT`("시스템 안내는 끌 수 없습니다.")으로 막는다 — `api-errors.md`가 전용
  코드 신설 여부를 물었던 부분인데, 이 한 가지 케이스만을 위해 새 `ErrorCode`를 파는 대신 기존
  `INVALID_INPUT` + 커스텀 메시지 패턴을 재사용하기로 했다(다른 곳에서도 이미 쓰는 패턴).

## 구현 순서

1. `NotificationCategory` enum(+`required` 플래그), `Notification`/`NotificationSetting` 엔티티
2. `NotificationRepository`/`NotificationSettingRepository`
3. DTO 5종(`NotificationResponse`, `NotificationSettingResponse`, `NotificationUnreadCountResponse`,
   `NotificationSettingUpdateItem`, `UpdateNotificationSettingsRequest`)
4. `NotificationService`(팬아웃 진입점 `notify` 포함)
5. `NotificationController` (`GET /notifications`, `PATCH /{id}/read`, `GET /unread-count`,
   `GET/PATCH /settings`)
6. `ErrorCode`에 `NOTIFICATION_NOT_FOUND` 추가
7. `NoticeService`/`PostService`/`ClubService`/`HoneyTipService`에 훅 연결
8. Flyway `V5__add_notification.sql`
9. `./gradlew test`
