# 로드맵 — 내가 작성한 글/댓글 (GET /posts/mine, GET /comments/mine)

## 배경

`docs/resources/api.md` 7번 섹션(마이페이지) 🆕 항목. 마이페이지에서 "내가 쓴 글"/"내가 쓴 댓글" 목록을
보여주기 위한 API. 기존 `/posts`, `/posts/{postId}/comments`는 전체/게시글 단위 조회만 있고 작성자 기준
조회가 없다.

## 기존 코드 확인

- `PostController`/`PostService`/`PostRepository` — `/posts` 목록이 이미 커서 페이지네이션
  (`CursorPageUtil`, `Slice`, `CursorMeta`) 패턴을 쓰고 있어 그대로 베낀다.
- `ClubController`의 `/clubs/mine`처럼 `@GetMapping("/mine")`을 `/{postId}`보다 먼저 등록해도 Spring이
  리터럴 경로를 우선 매칭하므로 순서 문제 없음(이미 검증된 패턴).
- `anonName`은 `PostAnonService.getOrAssignAnonName(post, author)`로 게시글마다 다시 조회해야 한다
  (댓글도 마찬가지) — 캐싱된 필드가 아님.
- `api.md`는 "내가 작성한 글 목록 (커뮤니티+클럽 필터 탭 포함)"이라고 적었지만, 클럽에는 "글" 개념이 없다
  (클럽은 멤버십 + 그룹 채팅만 있음). 이 API는 커뮤니티 게시글만 다룬다 — 와이어프레임 필터 탭 전체를
  구현하는 건 범위 밖.

## 설계

- `PostRepository`/`PostCommentRepository`에 `authorId` 기준 커서 쿼리 추가.
- `GET /comments/mine`은 `/posts` 하위가 아니라 최상위 리소스라 새 `CommentController`(`/comments`)를
  만든다. 로직은 `PostService`에 위임(새 도메인/서비스를 따로 만들 정도는 아님).
- 댓글 응답에는 어느 게시글의 댓글인지 알아야 프론트가 이동할 수 있으므로 `postId`를 포함하는 신규
  `MyCommentResponse` DTO를 쓴다(기존 `PostCommentResponse`는 상세 조회 컨텍스트라 `postId`가 없음).
- 에러 케이스 없음(본인 목록 조회라 404 대상이 없다 — `api-errors.md`도 동일하게 정리해둠).

## 구현 순서

1. `PostRepository.findByAuthorIdOrderByCreatedAtDesc`, `PostCommentRepository.findByAuthorIdOrderByCreatedAtDesc`
2. `MyCommentResponse` DTO
3. `PostService`에 `getMyPostSlice`, `getMyCommentSlice`, `toMyCommentList`, `commentCursorMetaOf` 추가
4. `PostController`에 `GET /mine` 추가, 신규 `CommentController`에 `GET /mine` 추가
5. `./gradlew test`
