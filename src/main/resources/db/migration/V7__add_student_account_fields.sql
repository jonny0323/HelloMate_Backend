-- 피그마 로그인/회원가입 화면 기준 계정 필드 보강.
-- 기존 컬럼은 건드리지 않고 추가만 한다.

-- 디자인의 로그인 화면은 이메일이 아니라 '아이디'로 로그인한다(회원가입 1/3에 중복 확인 버튼 존재).
ALTER TABLE users ADD COLUMN login_id VARCHAR(30);

-- 회원가입 3/3의 필수 약관 2종 동의 시각.
ALTER TABLE users ADD COLUMN terms_agreed_at TIMESTAMP;
ALTER TABLE users ADD COLUMN privacy_agreed_at TIMESTAMP;

-- 로그인 5회 실패 시 잠금(디자인에는 없지만 크리덴셜 스터핑 방어에 필요).
ALTER TABLE users ADD COLUMN login_fail_count INTEGER DEFAULT 0 NOT NULL;
ALTER TABLE users ADD COLUMN locked_until TIMESTAMP;

-- 마이페이지 회원 탈퇴. 탈퇴 계정은 행을 지우지 않고 상태만 바꾼다
-- (게시글/댓글 FK가 users를 참조하고 있어 물리 삭제하면 커뮤니티 이력이 깨진다).
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL;
ALTER TABLE users ADD COLUMN withdrawn_at TIMESTAMP;

-- 이메일 인증과 서류 인증 두 경로의 결과를 하나로 합친 상태.
-- 마이페이지 '학생 인증됨' 뱃지와 서비스 이용 권한 판정이 이 값 하나만 본다.
ALTER TABLE users ADD COLUMN verification_status VARCHAR(20) DEFAULT 'REGISTERED' NOT NULL;
ALTER TABLE users ADD COLUMN verified_at TIMESTAMP;

-- login_id 백필: 이메일 로컬파트를 쓰되, 학교가 달라 로컬파트가 겹치는 계정은 UUID로 대체한다
-- (해당 계정은 로그인 아이디 재설정 안내가 필요하다).
UPDATE users SET login_id = SUBSTRING(email FROM 1 FOR POSITION('@' IN email) - 1);
UPDATE users u SET login_id = u.id
WHERE EXISTS (SELECT 1 FROM users o WHERE o.login_id = u.login_id AND o.id <> u.id);

ALTER TABLE users ALTER COLUMN login_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT uq_users_login_id UNIQUE (login_id);

-- 인증번호 무제한 재시도는 6자리(10^6)를 뚫을 수 있어 시도 횟수를 센다.
ALTER TABLE email_verification ADD COLUMN attempt_count INTEGER DEFAULT 0 NOT NULL;
