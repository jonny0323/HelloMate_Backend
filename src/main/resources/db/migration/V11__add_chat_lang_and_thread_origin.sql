-- 1:1 문의 채팅 보강.

-- 학생은 모국어로 질문하는데 담당자는 한국어만 읽는다. 게시글과 동일하게 원문 언어를 기록해두고
-- 조회 시 Accept-Language 기준으로 번역해서 내려준다.
ALTER TABLE chat_message ADD COLUMN original_lang VARCHAR(10);

-- 담당자가 먼저 말을 건 스레드인지 구분한다. 학생 입장에서 '요청한 적 없는 DM'이라
-- 알림/차단 정책을 나중에 다르게 걸 수 있어야 한다.
ALTER TABLE chat_thread ADD COLUMN initiated_by VARCHAR(20) DEFAULT 'STUDENT' NOT NULL;
