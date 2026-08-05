-- HTTP Client 테스트 스위트 전용 담당자(Staff) 초대 코드 시드.
--
-- 담당자 초대 코드를 발급하는 API가 프로젝트에 아직 없어서(관리자 콘솔에도 없음 — 알려진 갭),
-- POST /auth/staff/signup 을 쓰는 .http 파일들은 이 코드가 DB에 미리 있어야 동작한다.
-- 코드는 1회용(used=true로 소모)이라 .http 파일끼리 코드를 공유하지 않고 파일 하나당 하나씩 쓴다.
--
-- 실행: docker compose up -d 로 띄운 로컬 Postgres에 한 번 실행.
--   psql "postgresql://hellomate:hellomate@localhost:5432/hellomate" -f http-client/seed-invite-codes.sql
--
-- 03-staff-auth.http 는 회원가입 성공 케이스에서 코드를 실제로 소모하므로, 그 파일을 다시 돌리려면
-- 이 스크립트를 다시 실행해서 코드를 초기화해야 한다(다른 파일의 코드는 조회/승인 목적이라 재실행해도
-- 안전 — 이미 있으면 스킵됨).

INSERT INTO staff_invite_code (id, university_id, department, code, used, expires_at, created_at, updated_at)
VALUES
    ('http-test-invite-staffauth', 'univ-inu', 'QA팀', 'HTTP-TEST-STAFFAUTH', false, '2099-01-01 00:00:00', now(), now()),
    ('http-test-invite-verification', 'univ-inu', 'QA팀', 'HTTP-TEST-VERIFICATION-ADMIN', false, '2099-01-01 00:00:00', now(), now()),
    ('http-test-invite-honeytip', 'univ-inu', 'QA팀', 'HTTP-TEST-HONEYTIP-ADMIN', false, '2099-01-01 00:00:00', now(), now()),
    ('http-test-invite-notice', 'univ-inu', 'QA팀', 'HTTP-TEST-NOTICE-ADMIN', false, '2099-01-01 00:00:00', now(), now()),
    ('http-test-invite-chat', 'univ-inu', 'QA팀', 'HTTP-TEST-CHAT', false, '2099-01-01 00:00:00', now(), now()),
    ('http-test-invite-search', 'univ-inu', 'QA팀', 'HTTP-TEST-SEARCH', false, '2099-01-01 00:00:00', now(), now()),
    ('http-test-invite-dashboard', 'univ-inu', 'QA팀', 'HTTP-TEST-DASHBOARD', false, '2099-01-01 00:00:00', now(), now()),
    ('http-test-invite-students', 'univ-inu', 'QA팀', 'HTTP-TEST-STUDENTS', false, '2099-01-01 00:00:00', now(), now()),
    ('http-test-invite-posts', 'univ-inu', 'QA팀', 'HTTP-TEST-POSTS', false, '2099-01-01 00:00:00', now(), now())
ON CONFLICT (id) DO UPDATE SET
    used = false,
    expires_at = EXCLUDED.expires_at;
