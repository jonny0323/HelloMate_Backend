-- 커뮤니티/정보글/공지/클럽 화면 보강.

-- 새 글 작성 화면의 익명/실명 토글. 지금까지는 전면 익명이라 토글이 동작할 수 없었다.
ALTER TABLE post ADD COLUMN anonymous BOOLEAN DEFAULT TRUE NOT NULL;

-- 정보글 상세는 본문이 평문이 아니라 팁 박스 + 번호 STEP 리스트 + 수수료/처리기간/외부링크 구조다.
-- STEP은 배열이라 별도 테이블 대신 JSON 문자열로 담는다(관리자가 통으로 저장/수정하고,
-- 서버는 조회 시 파싱만 하며 STEP 단위로 질의할 일이 없다).
ALTER TABLE honey_tip ADD COLUMN tip_message VARCHAR(300);
ALTER TABLE honey_tip ADD COLUMN steps_json TEXT;
ALTER TABLE honey_tip ADD COLUMN estimated_fee VARCHAR(50);
ALTER TABLE honey_tip ADD COLUMN processing_period VARCHAR(50);
ALTER TABLE honey_tip ADD COLUMN external_link VARCHAR(500);

-- 공지 홈 상단 배너에 찍히는 노출 기간(예: 2026.05.20 - 2026.06.15).
ALTER TABLE notice ADD COLUMN banner_start_date DATE;
ALTER TABLE notice ADD COLUMN banner_end_date DATE;
