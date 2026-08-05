-- 담당자 콘솔의 공지 생명주기(임시저장 → 발송 → 재발송/수정/삭제)를 담기 위한 컬럼.
-- 기존 컬럼은 건드리지 않고 추가만 한다.

-- DRAFT(임시저장) / SENT(발송됨). 기존 행은 전부 발송된 공지다.
ALTER TABLE notice ADD COLUMN status VARCHAR(20) DEFAULT 'SENT' NOT NULL;

-- created_at은 초안 생성 시각이 될 수 있어 실제 발송 시각을 따로 둔다.
ALTER TABLE notice ADD COLUMN sent_at TIMESTAMP;

-- 하드 삭제를 걷어낸다. chat_thread.notice_id가 notice를 FK로 물고 있어 물리 삭제가 FK 위반이고,
-- 학생이 이미 읽은 공지의 열람 이력(누가 언제 읽었는지)도 통째로 사라진다.
ALTER TABLE notice ADD COLUMN deleted_at TIMESTAMP;

-- 발송함에 "전체 학생 / 베트남 유학생, 컴퓨터공학과 / 3명 개별 선택"처럼 보여줄 대상 스냅샷.
-- 발송 시점의 조건을 문자열로 굳혀두지 않으면 나중에 학생 정보가 바뀌었을 때 재현이 안 된다.
ALTER TABLE notice ADD COLUMN audience_mode VARCHAR(20);
ALTER TABLE notice ADD COLUMN audience_label VARCHAR(255);

-- 재발송 남용 방지(쿨다운 판정)와 이력 추적.
ALTER TABLE notice ADD COLUMN resend_count INTEGER DEFAULT 0 NOT NULL;
ALTER TABLE notice ADD COLUMN last_resent_at TIMESTAMP;

UPDATE notice SET sent_at = created_at WHERE sent_at IS NULL;
UPDATE notice SET audience_mode = 'ALL', audience_label = '전체 학생' WHERE audience_mode IS NULL;
