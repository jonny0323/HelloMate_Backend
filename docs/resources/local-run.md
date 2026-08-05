# 로컬 실행 가이드 (백엔드 + 담당자 콘솔)

담당자 웹(`hellomate-admin`)과 이 백엔드를 한 대의 PC에서 같이 띄우는 절차다.
두 저장소가 같은 부모 폴더(`Desktop/Programming`)에 나란히 있다고 가정한다.

## 0. 준비물

- Docker Desktop (Postgres 16 컨테이너용)
- JDK 21 (Gradle toolchain이 21을 요구한다)
- Node 18 이상

## 1. 한 번에 띄우기 (권장)

```
Desktop\Programming\hellomate-dev.bat
```

Postgres → 백엔드 → 프론트 순으로 각각 별도 창에 띄운다.
처음 실행이면 `npm install`도 자동으로 돌린다.

## 2. 수동으로 띄우기

```bash
# 1) DB
cd HelloMate_Backend
docker compose up -d

# 2) 백엔드 (:8080)
./gradlew bootRun          # Windows: gradlew.bat bootRun

# 3) 담당자 콘솔 (:5173) — 새 터미널
cd ../hellomate-admin
npm install
npm run dev
```

브라우저에서 http://localhost:5173 접속.

## 3. 데모 계정 / 데이터

`.env`의 `SEED_ENABLED=true`가 켜져 있으면 **DB가 비어 있을 때만** 데모 데이터를 넣는다
(`LocalSeedDataInitializer`). 이미 데이터가 있으면 통째로 건너뛴다.

| 항목 | 값 |
| --- | --- |
| 담당자 계정 | `admin@inu.ac.kr` / `hellomate1!` (국제교류처) |
| 담당자 계정 | `scholarship@inu.ac.kr` / `hellomate1!` (장학처) |
| 미사용 초대코드 | `INU-INTL-2026`, `INU-DORM-2026` (회원가입 화면 테스트용) |
| 학생 | 30명 (국가 6종 / 학과 6종) |

초대 코드는 1회용이라 회원가입에 쓰면 소진된다. 다시 테스트하려면 DB를 초기화한다.

```bash
docker compose down -v && docker compose up -d
```

## 4. 두 코드베이스가 맞물리는 지점

- **CORS** — 백엔드 `hellomate.cors.allowed-origins`(env `CORS_ALLOWED_ORIGINS`)에
  프론트 오리진이 들어 있어야 브라우저가 요청을 보낸다. 기본값에 `:5173`, `:4173`이 있다.
- **API 베이스 URL** — 프론트 `.env`의 `VITE_API_BASE_URL`. 기본 `http://localhost:8080`.
- **네이밍** — 서버 JSON은 전역 snake_case다. 프론트 `src/api/client.js`가 요청/응답 키를
  camelCase ↔ snake_case로 재귀 변환하므로 프론트 코드는 camelCase만 쓴다.
- **enum 직렬화** — `NoticeType`, `SenderType` 등은 `@JsonValue`로 **소문자** 문자열이 나간다
  (`URGENT` → `"urgent"`). 프론트에서 비교할 때 대문자로 쓰면 조용히 안 맞는다.
- **부서** — 공지 작성 시 `department`를 보내지 않는다. 서버가 작성자 소속으로 굳힌다(부서 사칭 방지).
  화면의 '보내는 부서' 선택은 미리보기 표시용이다.

## 5. 자주 나는 문제

| 증상 | 원인 / 해결 |
| --- | --- |
| 로그인 버튼을 눌러도 아무 반응이 없고 콘솔에 CORS 에러 | `CORS_ALLOWED_ORIGINS`에 접속 중인 오리진이 없음 |
| 앱 기동 실패 `Schema-validation: missing table` | 엔티티만 고치고 Flyway 마이그레이션을 안 만든 경우 |
| 로그인 시 "아직 승인되지 않은 담당자 계정입니다" | 초대 코드로 가입한 계정이 아님. 시드 계정을 쓰거나 초대 코드로 재가입 |
| 대시보드가 비어 있음 | 시드가 꺼져 있거나 공지를 아직 보내지 않음 |
| 파일 첨부가 안 붙음 | `FileStorageService`는 아직 스텁이다. presigned URL만 발급되고 실제 업로드는 무시된다 |
