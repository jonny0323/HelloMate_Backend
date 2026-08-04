# 보고서 — 클럽 그룹 채팅 (POST/GET /clubs/{clubId}/messages)

로드맵: `docs/roadmaps/club-chat.md`

## 한 일

`chat` 도메인의 `ChatThread`(1:1 전용, `(student_id, staff_id)` 유니크)를 확장하지 않고, `club` 도메인
안에 별도 `ClubMessage` 엔티티(`club_id` + `sender_id` + `content`)를 새로 만들었다. `club_id` 자체가
방 키 역할을 하므로 스레드 엔티티 없이 메시지 테이블 하나로 끝났다. 커서 페이지네이션은 `ChatService`
패턴을 그대로 베꼈다.

`ClubController`에 `POST/GET /{clubId}/messages`를 추가하고, 접근 제어는 기존
`ClubMemberRepository.existsByClubIdAndStudentId`를 재사용해 멤버가 아니면 `NOT_CLUB_MEMBER`(400)를
던지도록 했다.

Flyway `V3__add_club_message.sql` 추가.

## 로드맵 대비 설계 결정 확정

- **`chat` 확장이 아니라 `club` 도메인 내 별도 엔티티** — 로드맵에 적어둔 이유(1:1 불변식과 그룹 모델이
  근본적으로 다름) 그대로 확정.
- **`NOT_CLUB_MEMBER`를 403이 아니라 기존 400 그대로 재사용** — `api-errors.md`가 "통일 필요"라고
  표시했던 부분. `DELETE /clubs/{clubId}/leave`가 이미 같은 에러를 400으로 쓰고 있어서 통일성을
  우선했다.
- 클럽 멤버는 서로를 아는 사이라 커뮤니티처럼 익명화하지 않고 `senderName`을 그대로 노출한다
  (`ClubMemberResponse`가 이미 `studentName`을 노출하는 것과 같은 기준).

## 검증

`./gradlew test` 통과(H2 create-drop). 추가로 Flyway + H2(PostgreSQL 호환 모드) + `ddl-auto: validate`
조합으로 `V1`~`V3`를 실제로 migrate시켜 Hibernate 검증까지 통과하는 것을 확인했다(검증용 임시 파일은
확인 후 삭제).

## 남은 이슈 / 다음 작업 후보

- "알람" 화면의 "클럽 채팅" 필터/알림 토글은 아직 없는 `알림(Notification)` 도메인이 전제로 하고 있어서
  이번 범위에는 없음.

## 커밋 메시지 제안

```
✨ Feature : 클럽 그룹 채팅 API 추가
```
