# 보고서 — 남은 갭 마무리 (프로필 확장 / 공지 검색 / 커뮤니티 검색)

로드맵: `docs/roadmaps/remaining-gaps.md`

## 한 일

세 도메인이 서로 안 겹쳐서 로드맵대로 각각 독립적으로 구현했다.

1. **프로필 확장** — `Student`에 `birthYear`(Integer, nullable) 컬럼 추가. `updateProfile`을
   `name`/`country`/`birthYear`까지 받도록 확장(전부 null-safe). `PATCH /students/me` 요청/응답에 반영.
2. **공지사항 검색** — `NoticeReceptionRepository`의 커서 쿼리에 `keyword` 파라미터 추가(제목+본문
   LIKE). `GET /notices?q=&groupBy=&cursor=&limit=` — `q`와 `groupBy` 동시 사용 가능.
3. **커뮤니티 검색** — `PostRepository`의 커서 쿼리에 동일하게 `keyword` 파라미터 추가.
   `GET /posts?q=&cursor=&limit=`. `/posts/mine`은 검색 대상 아님(로드맵대로 안 건드림).

## 로드맵 대비 달라진 점

없음. 사용자가 확인해준 대로: 이름/국적 수정 허용(실명 불일치 리스크는 서류 인증 검토 단계에서
담당자가 걸러내는 걸 전제), `/notices`·`/posts`에 각각 `q` 직접 추가(통합 검색 재사용/별도 신설 아님).

## 보류된 항목 (이번에 안 함, 사용자 확인함)

- 서류 인증 `documentType` 구분
- 생활정보 구조화 필드(fee/처리기간/링크/문서태그)
- `GET /terms/service`

## 검증

`./gradlew test`/`build` 통과. Flyway+H2(PostgreSQL 모드)+`ddl-auto: validate`로 `V1`~`V6` 전체 migrate
후 Hibernate 검증까지 확인(임시 파일은 확인 후 삭제) — `V6`의 `ALTER TABLE users ADD COLUMN birth_year
INTEGER`도 이 과정에서 실제로 검증됨.

## 커밋 메시지 제안

기능별로 3개:
```
✨ Feature : 마이페이지 이름/국적/출생연도 수정 지원
✨ Feature : 공지사항 검색어(q) 파라미터 추가
✨ Feature : 커뮤니티 게시글 검색어(q) 파라미터 추가
```
