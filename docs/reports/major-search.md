# 보고서 — 전공 검색 자동완성 (GET /universities/{universityId}/majors)

로드맵: `docs/roadmaps/major-search.md`

## 한 일

`university` 도메인에 `Major` 엔티티/리포지토리/서비스/컨트롤러를 추가했다. `GET
/universities/{universityId}/majors?query=`는 인증 불필요(회원가입 3/3 단계, 로그인 전 호출)라
`SecurityConfig.PERMIT_ALL_PATTERNS`에 `/universities/**`를 추가했다.

## 시드 데이터

- `University`를 시드하는 코드가 프로젝트에 전혀 없었다(확인함) — `Major`가 FK로 요구해서, 이번
  마이그레이션(`V4__add_major.sql`)에 문서 예시와 동일한 `univ-inu`(인천대학교) 한 행을 같이 넣었다.
- `Major` 시드 10개는 **플레이스홀더**다(컴퓨터공학부/경영학과 등 흔한 학과명) — 인천대 공식 학과
  목록이 아니다. 실제 데이터로 교체 필요.
- 원래 `INSERT ... ON CONFLICT (id) DO NOTHING`으로 안전하게 넣으려 했는데, H2(PostgreSQL 호환 모드,
  테스트 DB로 씀)가 이 구문에서 문법 오류를 냈다(`./gradlew test`가 Flyway를 H2에도 돌린다는 걸 이번에
  확인함 — 아래 참고). 빈 DB 기준 마이그레이션이라 `ON CONFLICT` 없이 평범한 `INSERT`로 바꿨다.

## 발견한 것 — `./gradlew test`가 Flyway를 H2에도 실행한다

이전 세션에서 "Flyway가 테스트에는 안 돈다"고 판단했었는데 틀렸다. `ddl-auto: create-drop`이 Flyway가
만든 테이블을 곧바로 지우고 엔티티 기준으로 다시 만들어서 결과적으로는 문제가 안 됐을 뿐, Flyway
자체는 H2에도 매번 돈다(Gradle이 통과한 테스트의 stdout을 숨겨서 안 보였던 것). 즉 `./gradlew test`
만으로도 마이그레이션 SQL 문법 오류는 어느 정도 잡힌다 — 이번에 그 덕분에 `ON CONFLICT` 문제를 바로
잡을 수 있었다.

## 검증

`./gradlew test`/`build` 통과. Flyway+H2(PostgreSQL 모드)+`ddl-auto: validate` 조합으로 `V1`~`V5` 전체
migrate 후 Hibernate 검증까지 확인(임시 파일은 확인 후 삭제).

## 남은 이슈

- `Major` 실제 인천대 학과 데이터로 교체 필요.
- `University` 시드가 이 마이그레이션에만 의존하는 상태라, 나중에 다른 대학이 추가되면 같은 방식으로
  마이그레이션을 늘리거나 관리자 콘솔에 학교 등록 기능을 붙여야 한다(지금은 없음).

## 커밋 메시지 제안

```
✨ Feature : 전공 검색 자동완성 API 추가
```
