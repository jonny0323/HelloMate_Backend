# 로드맵 — 전공 검색 자동완성 (GET /universities/{universityId}/majors?query=)

## 배경

`docs/resources/api.md` 2번 섹션 🆕. `Student.major`가 자유 문자열이라 전공 마스터 데이터가 없다. 사용자
확인 결과: **구조(엔티티/API)만 먼저 만들고, 시드 데이터는 플레이스홀더로 넣는다** — 실제 인천대 학과
전체 목록은 나중에 정확한 자료로 교체.

## 설계

- `university` 도메인에 `Major` 엔티티 신설(`id`, `university` FK, `name`). 다른 필드(학과 코드 등)는
  지금 필요하지 않아 안 넣음.
- `GET /universities/{universityId}/majors?query=` — `query` 없으면 전체 목록, 있으면 이름
  `LIKE` 검색(대소문자 무시).
- `universityId`가 없는 학교면 `400 INVALID_INPUT`("존재하지 않는 학교입니다.") — 기존 회원가입 에러와
  동일 패턴 재사용.
- 이 엔드포인트는 회원가입 3/3 단계(로그인 전)에서도 호출돼야 해서 `SecurityConfig`의
  `PERMIT_ALL_PATTERNS`에 `/universities/**`를 추가한다(인증 불필요).

## 시드 데이터 관련 중요 사항

- **`University`를 시드하는 코드/마이그레이션이 프로젝트에 아예 없다** — `docs/resources/api-reference.md`
  등 문서 예시가 쓰는 `univ-inu`/`인천대학교`도 실제로 DB에 넣어주는 곳이 없다. `Major`가 FK로
  `university_id`를 요구하므로, 이 기능이 동작하려면 최소 하나의 University 행이 있어야 한다. 그래서
  이번 마이그레이션에 `univ-inu`(인천대학교) 한 행을 문서 예시와 동일한 ID로 같이 넣는다. `ON CONFLICT`
  로 안전하게 처리하려 했지만 H2(PostgreSQL 호환 모드, 테스트에서 씀)가 이 구문을 못 받아들여서 그냥
  평범한 `INSERT`로 갔다 — 빈 DB 기준 마이그레이션이라 문제 없고, 이미 같은 ID의 University가 있는
  환경이 있다면 적용 전에 직접 확인해야 한다.
- `Major` 시드는 **플레이스홀더**다(컴퓨터공학부/경영학과 등 흔한 학과명 10개). 인천대 공식 학과
  목록이 아니다 — 실제 데이터로 교체 전까지 자동완성 결과가 실제와 다를 수 있음을 프론트/기획에
  공유해야 한다.

## 구현 순서

1. `Major` 엔티티, `MajorRepository`(검색 쿼리)
2. `MajorResponse` DTO, `MajorService`, `MajorController`
3. `SecurityConfig`에 `/universities/**` permit-all 추가
4. Flyway `V4__add_major.sql` (테이블 + University/Major 플레이스홀더 시드)
5. `./gradlew test`
