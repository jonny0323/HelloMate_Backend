# 담당자 콘솔(hellomate-admin) ↔ 백엔드 로컬 연동

## 배경

담당자 웹과 백엔드를 따로 개발해서, 각각은 돌지만 붙여본 적이 없다.
로컬 PC에서 두 개를 동시에 띄워 실제로 로그인 → 공지 발송 → 1:1 메시지까지 되게 만든다.

## 현황 점검 결과

프론트는 이미 목데이터가 아니라 `src/api/*.js`로 실제 API를 호출하고 있었다.
붙지 않는 원인은 아래 넷이다.

1. **CORS 미설정** — 백엔드에 CORS 설정이 아예 없다. 브라우저가 로그인 요청부터 막는다.
2. **없는 엔드포인트** — 프론트가 `GET /admin/students/target-groups`를 부르는데 백엔드에 없다.
   `/admin/students/{studentId}` 매핑에 걸려 404가 난다.
3. **응답 스키마 불일치**
   - 대시보드: 서버 `stats` ↔ 화면 `summary`, 필드명도 전부 다름
     (`sentNoticeCount`/`activeStudentCount` ↔ `noticeCount`/`totalStudentCount`).
     화면이 `summary.noticeCount`를 읽어서 렌더 시점에 그대로 터진다.
   - 발송함: 서버 `sentAt` ↔ 화면 `createdAt` (날짜가 빈칸)
   - 채팅: 서버는 enum을 소문자로 직렬화(`"teacher"`)하는데 화면은 `'TEACHER'`로 비교.
     담당자 말풍선이 전부 학생 쪽으로 붙는다.
   - 공지 발송: 서버 `audience.countryCodes/majors` ↔ 화면 `countryKeys/majorKeys`.
     그룹 발송이 조건 없는 GROUP으로 나가 400.
4. **빈 DB** — 대학·초대코드·담당자 계정·학생이 하나도 없어 로그인 자체가 불가능하다.

## 할 일 / 건드릴 파일

### 백엔드

- `global/config/CorsConfig.java` (신규) — 허용 오리진을 `hellomate.cors.allowed-origins`로 외부화
- `global/security/SecurityConfig.java` — `.cors(...)` 연결
- `domain/admin/dto/response/TargetGroupResponse.java`, `TargetGroupType.java` (신규)
- `domain/admin/util/CountryLabel.java` (신규) — 국가 코드 표시용 라벨
- `domain/student/dto/response/StudentGroupCountResponse.java` (신규) — 집계 프로젝션
- `domain/student/repository/StudentRepository.java` — 국가/학과별 집계 쿼리 2개
- `domain/admin/service/AdminStudentService.java` + `controller/AdminStudentController.java`
  — `GET /admin/students/target-groups`
- `global/config/LocalSeedDataInitializer.java` (신규) — `hellomate.seed.enabled=true`일 때만 도는 데모 시더
- `application.yaml`, `.env`, `.env.production`, `src/test/resources/application.yaml`

`AudienceRequest`는 손대지 않는다 — 학생 앱도 같은 계약을 쓰므로 프론트를 서버에 맞춘다.
`department`도 서버가 작성자 소속으로 굳히는 현재 동작을 유지한다(부서 사칭 방지).

### 프론트

- `views/Dashboard.jsx`, `views/Compose.jsx`, `views/SentList.jsx`,
  `components/messages/ChatPane.jsx`, `api/notices.js`, `api/chats.js`, `api/dashboard.js`
- `.env`, `.gitignore`, `README.md`

### 실행 편의

- `Programming/hellomate-dev.bat` — Postgres → 백엔드 → 프론트 순차 기동
- `docs/resources/local-run.md` — 실행 절차, 데모 계정, 자주 나는 문제

## 순서

1. CORS → 2. target-groups 엔드포인트 → 3. 프론트 스키마 정합 → 4. 시더 →
5. 실행 스크립트/문서 → 6. `./gradlew build` + `npm run build` 검증
