# 담당자 콘솔 ↔ 백엔드 로컬 연동 — 작업 보고서

로드맵: `docs/roadmaps/2026-08-05-admin-console-integration.md`

## 결론

두 저장소를 한 PC에서 같이 띄우면 로그인 → 대시보드 → 공지 발송 → 1:1 메시지까지 돈다.
프론트는 원래부터 실제 API를 부르고 있었고, 안 붙던 원인은 CORS · 누락 엔드포인트 ·
응답 스키마 4곳 불일치 · 빈 DB 네 가지였다.

## 백엔드 변경

| 파일 | 내용 |
| --- | --- |
| `global/config/CorsConfig.java` (신규) | 허용 오리진을 `hellomate.cors.allowed-origins`(env `CORS_ALLOWED_ORIGINS`)로 외부화. JWT 헤더 인증이라 `allowCredentials`는 끔 |
| `global/security/SecurityConfig.java` | `.cors(...)` 연결 |
| `domain/admin/dto/response/TargetGroupResponse.java` / `TargetGroupType.java` (신규) | 대상 그룹 카드 응답 |
| `domain/admin/util/CountryLabel.java` (신규) | 국가 코드 → 한글 표시명. 모르는 코드는 코드 그대로 |
| `domain/student/dto/response/StudentGroupCountResponse.java` (신규) | 집계 프로젝션 |
| `domain/student/repository/StudentRepository.java` | `countActiveGroupByCountry`, `countActiveGroupByMajor` 추가 |
| `domain/admin/service/AdminStudentService.java` / `controller/AdminStudentController.java` | `GET /admin/students/target-groups` |
| `global/config/LocalSeedDataInitializer.java` (신규) | 데모 데이터 시더 |
| `application.yaml` / `src/test/resources/application.yaml` / `.env` / `.env.production` | cors · seed 설정 |

### 설계 판단

- **`AudienceRequest`는 안 고쳤다.** 학생 앱도 같은 계약을 쓰므로 프론트를 서버에 맞췄다.
- **`department`도 안 받는다.** 서버가 작성자 소속으로 굳히는 기존 동작을 유지했다(부서 사칭 방지).
  화면의 '보내는 부서' 선택은 미리보기 표시용으로 남겼다 — 실제 발송 부서와 다를 수 있다(아래 남은 이슈).
- **target-groups의 `groupKey`는 발송 조회와 표기를 맞췄다.** 국가는 `findAudienceByCountry`가
  `upper(s.country)`로 비교하므로 집계도 대문자로 뽑는다. 라벨을 되돌려 보내면 수신자 0명이 된다.
- **학생이 0명인 국가/학과는 안 내려준다.** 고르는 순간 `NOTICE_AUDIENCE_EMPTY`로 막히는 카드다.
- **시더는 `@ConditionalOnProperty`로 걸었다.** 프로파일 활성화 타이밍(dotenv가 `SPRING_PROFILES_ACTIVE`를
  주입하는 시점)에 기대지 않으려고 프로파일 대신 단순 플래그를 썼다. 기본값 `false` + 대학이 이미 있으면
  전체 스킵이라 재기동/배포에서 안전하다.

## 프론트 변경

| 파일 | 내용 |
| --- | --- |
| `views/Dashboard.jsx` | `summary.*` → `stats.*` (`sentNoticeCount`, `activeStudentCount`, `pendingReplyCount`) |
| `views/Compose.jsx` | 전체 학생 수 출처 변경, `countryKeys/majorKeys` → `countryCodes/majors`, `department` 미전송 |
| `views/SentList.jsx` | `n.createdAt` → `n.sentAt` |
| `components/messages/ChatPane.jsx` | `senderType === 'TEACHER'` → `'teacher'` (서버가 enum을 소문자로 직렬화) |
| `api/notices.js` | 발송 페이로드를 `AudienceRequest` 계약에 맞춤 |
| `api/chats.js` | 서버가 안 받는 `noticeId` 제거 |
| `.env` / `.gitignore` / `README.md` | 신규 · 갱신 |

## 실행 편의

- `Programming/hellomate-dev.bat` — Postgres 기동 → `pg_isready` 대기 → 백엔드 · 프론트를 각각 별도 창으로
- `docs/resources/local-run.md` — 실행 절차, 데모 계정, 두 코드베이스가 맞물리는 지점, 자주 나는 문제

데모 계정: `admin@inu.ac.kr` / `hellomate1!` (국제교류처), `scholarship@inu.ac.kr` / `hellomate1!` (장학처).
미사용 초대코드 `INU-INTL-2026`, `INU-DORM-2026`. 학생 30명 / 국가 6종 / 학과 6종.

## 빌드·테스트 결과

- 프론트 `npm run build` — 통과 (54 modules, 178.69 kB).
- 백엔드 `./gradlew build` — **미실행.** 작업 환경에서 JDK 21과 Maven Central 접근이 막혀 있어 컴파일을
  돌리지 못했다. 반드시 로컬에서 한 번 돌려 확인할 것.
- 엔티티 변경이 없어 Flyway 마이그레이션 추가는 불필요하다(신규 쿼리·DTO·설정만 추가).

## 남은 이슈 / 다음 작업 후보

- **'보내는 부서' 선택이 실제 발송 부서와 다를 수 있다.** 서버가 작성자 소속으로 굳히므로,
  화면도 `currentUser.department`를 고정 표시하도록 바꾸는 게 맞다.
- **임시저장 버튼이 토스트만 띄운다.** `POST /admin/notices/drafts`가 이미 있으니 연결만 하면 된다.
- **발송 전 인원 미리보기 미연동.** `POST /admin/notices/audience/count`를 붙이면
  "N명에게 발송됩니다"를 실제 수치로 보여줄 수 있다(그룹 중복 인원 이중 계산 방지).
- **채팅 폴링/실시간 없음.** 학생이 보낸 메시지가 새로고침 전까지 안 보인다.
- **국가 라벨이 코드 하드코딩.** 국가 테이블이 생기면 `CountryLabel`을 걷어낼 것.
- **첨부파일은 여전히 스텁.** presigned URL만 발급되고 실제 업로드는 무시된다.

## 커밋 메시지 추천

```
✨ Feature : 담당자 콘솔 로컬 연동 (CORS · 대상 그룹 API · 데모 시더)
```
