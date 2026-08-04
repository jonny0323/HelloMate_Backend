# 보고서 — 내가 작성한 글/댓글 (GET /posts/mine, GET /comments/mine)

로드맵: `docs/roadmaps/posts-mine.md`

## 한 일

로드맵대로 `PostRepository`/`PostCommentRepository`에 작성자 기준 커서 쿼리를 추가하고, `PostService`에
집계/변환 메서드를 추가했다. `GET /posts/mine`은 기존 `PostController`에, `GET /comments/mine`은 최상위
리소스라 새 `CommentController`(`/comments`)를 만들어 넣었다(로직은 `PostService`에 위임, 새 서비스는
안 만듦).

댓글 응답은 어느 게시글 소속인지 알아야 해서 `postId`를 포함하는 `MyCommentResponse`를 새로 만들었다
(기존 `PostCommentResponse`는 게시글 상세 컨텍스트라 `postId`가 없어서 재사용 불가).

## 로드맵 대비 달라진 점

없음. `api.md`가 "커뮤니티+클럽 필터 탭"이라고 적었지만 클럽에는 "글" 개념이 없어서(멤버십+그룹채팅만
존재) 커뮤니티 게시글만 다룬다고 로드맵에 이미 명시했고, 그대로 구현했다.

## 빌드/테스트 결과

`./gradlew test` 통과(H2 create-drop). 새 엔티티/테이블이 없어 마이그레이션 변경 없음.

## 남은 이슈

없음.

## 커밋 메시지 제안

```
✨ Feature : 내가 작성한 글/댓글 조회 API 추가
```
