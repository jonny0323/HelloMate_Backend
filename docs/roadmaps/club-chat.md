# 로드맵 — 클럽 그룹 채팅 (POST/GET /clubs/{clubId}/messages)

## 배경

`docs/resources/api.md` 4번 섹션 🆕, "가장 큰 갭"으로 표시됨. 여러 멤버가 같이 있는 클럽 채팅방 —
기존 `chat` 도메인의 `ChatThread`는 `(student_id, staff_id)` 1:1 전용(유니크 제약)이라 그대로 못 쓴다.

## 설계 결정 (`chat` 확장 vs `club` 도메인 내 별도 엔티티)

`api.md`가 명시적으로 "설계 결정이 필요"라고 표시한 지점. `chat` 도메인 확장(스레드 타입을 1:1/그룹으로
분기)과 `club` 도메인 내 별도 메시지 엔티티, 두 가지를 검토했다.

**`club` 도메인 안에 별도 `ClubMessage` 엔티티를 두는 쪽으로 결정.** 이유:
- `ChatThread`는 "학생 1명 ↔ 담당자 1명"이 핵심 불변식이고(`student_id`/`staff_id` FK, 유니크 제약,
  `studentUnread`/`staffUnread` 필드 등 전부 이 가정 위에 설계됨), 클럽은 "학생 N명이 같은 방"이라
  모델이 근본적으로 다르다. `ChatThread`를 억지로 그룹까지 담게 확장하면 기존 1:1 로직에 분기가 늘어나고
  회귀 위험이 커진다.
- 클럽은 이미 `club_id` 자체가 "방 키" 역할을 한다(멤버십은 `ClubMember`로 이미 관리 중) — 별도
  스레드 엔티티 없이 `club_id` + `sender_id` + `content`만 있는 메시지 테이블로 충분하다.
- CLAUDE.md의 "도메인 넘나드는 참조는 Repository/Service 직접 주입" 원칙과도 맞다 — `club` 도메인
  안에서 끝나고 `chat` 도메인을 건드릴 필요가 없다.

## 기존 코드 확인

- `ClubService`의 `getClub`/`requireCreator` 패턴, `ClubMemberRepository.existsByClubIdAndStudentId` 재사용.
- `ChatService.getMessageSlice`/`sendMessage`/커서 페이지네이션 구조를 그대로 베껴서 `ClubMessage`에 적용.
- 멤버가 아닌 학생이 채팅방에 접근하면 `NOT_CLUB_MEMBER`를 던진다 — `api-errors.md`는 "400을 그대로 쓸지
  403으로 통일할지 결정 필요"라고 적어뒀는데, 이미 `DELETE /clubs/{clubId}/leave`가 같은 에러를 400으로
  쓰고 있어서(`ClubService.leave`) **통일성 우선으로 400 그대로 재사용**한다(새 상태코드 분기를 만들 만큼
  가치가 크지 않음).
- 클럽 멤버는 서로를 아는 사이라 커뮤니티 게시글처럼 익명화하지 않는다(`ClubMemberResponse`가 이미
  `studentName`을 노출하는 것과 동일한 기준) — 메시지도 발신자 이름을 그대로 내려준다.

## 구현 순서

1. `ClubMessage` 엔티티 (`club` 도메인, `BaseTimeEntity` 상속)
2. `ClubMessageRepository` (커서 쿼리)
3. `SendClubMessageRequest`, `ClubMessageResponse` DTO
4. `ClubService`에 `sendMessage`/`getMessageSlice`/`toMessageList`/`messageCursorMetaOf`/`requireMember` 추가
5. `ClubController`에 `POST/GET /{clubId}/messages` 추가
6. Flyway `V3__add_club_message.sql`
7. `./gradlew test`
